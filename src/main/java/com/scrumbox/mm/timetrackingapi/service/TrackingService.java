package com.scrumbox.mm.timetrackingapi.service;

import com.google.common.base.Strings;
import com.scrumbox.mm.timetrackingapi.client.UsersApiClient;
import com.scrumbox.mm.timetrackingapi.exception.TimeTrackingException;
import com.scrumbox.mm.timetrackingapi.model.*;
import com.scrumbox.mm.timetrackingapi.persistence.domain.Tracking;
import com.scrumbox.mm.timetrackingapi.persistence.domain.TimeTracking;
import com.scrumbox.mm.timetrackingapi.persistence.repository.TimeTrackingRepository;
import com.scrumbox.mm.timetrackingapi.persistence.repository.TrackingRepository;
import com.scrumbox.mm.timetrackingapi.utils.DateUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Service
public class TrackingService {

    private static final Logger log = LoggerFactory.getLogger(TrackingService.class);

    private TimeTrackingRepository timeTrackingRepository;
    private TrackingRepository trackingRepository;

    private UsersApiClient usersApiClient;

    @Autowired
    public TrackingService(TrackingRepository trackingRepository,
                           TimeTrackingRepository timeTrackingRepository,
                           UsersApiClient usersApiClient) {

        this.trackingRepository = trackingRepository;
        this.timeTrackingRepository = timeTrackingRepository;
        this.usersApiClient = usersApiClient;
    }

    public List<Tracking> getAll() {
        return trackingRepository.findAll();
    }

    public void trackTime(TrackingRequest request) {
        // 1.- Obtengo el timeTracking que viene en el request, dentro de tracking como list.
        // 2.- Si la lista de timeTracking esta vacía lanzo exception
        // 3.- Caso contrario obtengo el primer elemento y me fijo si tiene el tracking creado o ya existe.
        // 4.- si no tiene tracking lo creo y luego al timeTracking le asigno su tracking y guardo el timeTracking

        Tracking tracking = findByDocumentNumber(request.getDocumentNumber());

        tracking = initializeTracking(request.getDocumentNumber(), tracking);

        List<TimeTracking> timeTrackingList = tracking.getTimeTracking();

        // TODO: SE PUEDE FICHAR A PASADO y A FUTURO, OVIAMENTE SIN SUPERPONER FECHAS.?
        validateStartAndEndDay(timeTrackingList, request);

        TimeTracking timeTracking = new TimeTracking(request.getStart(), request.getEnd(), tracking);

        validateDay(request.getDocumentNumber(), new DateTime(timeTracking.getStart()), timeTracking);

        timeTrackingRepository.save(timeTracking);
    }

    public void trackTime(Integer documentNumber) {
        Tracking tracking = findByDocumentNumber(documentNumber);

        tracking = initializeTracking(documentNumber, tracking);

        List<TimeTracking> lastTimeTracking = tracking.getTimeTracking();
        DateTime startTime = DateUtils.getNowAsDateTime();

        TimeTracking act = lastTimeTracking.stream().reduce((first, second) -> second)
                .orElse(new TimeTracking(startTime.toDate(), startTime.toDate(), tracking));

        validateDay(documentNumber, startTime, act);

        // LOS AUSENTES SE CALCULAN COMPARANDO LAS SUM(DURACION DE HORAS TRABAJAS DEL EMPLEADO DEL MES) CONTRA  TOTALES * DIAS HABILES DEL MES

        if (act.getDay().getDayOfYear() == startTime.getDayOfYear()) {
            act.setEndTime(startTime.getHourOfDay(), startTime.getMinuteOfHour());
        } else {
            act =  new TimeTracking(startTime.toDate(), startTime.toDate(), tracking);
        }

        lastTimeTracking.add(act);

        timeTrackingRepository.saveAll(lastTimeTracking);
    }

    public Tracking findByDocumentNumber(Integer documentNumber) {
        return trackingRepository.findByDocumentNumber(documentNumber).orElse(null);
    }

    private void validateStartAndEndDay(List<TimeTracking> timeTracking, TrackingRequest request) {
        if (timeTracking != null && !timeTracking.isEmpty()) {
            Supplier<Stream<TimeTracking>> timeTrackingStream = () -> timeTracking.stream();

            // Si el ingreso y el egreso ya existe, no se puede fichar.
            boolean periodExist = timeTrackingStream.get().filter(it ->
                    request.getStart().equals(it.getStart()) &&
                            request.getEnd().equals(it.getEnd())
            ).count() > 0;

            if (periodExist) {
                throw new TimeTrackingException("Same period!");
            }

            // Si ingreso o el egreso esta dentro de un periodo ya existente, no se puede fichar.
            boolean isInvalidPeriod = timeTrackingStream.get().filter(it ->
                    (request.getStart().after(it.getStart()) &&
                            request.getStart().before(it.getEnd())) ||
                            (request.getEnd().after(it.getStart()) &&
                                    request.getEnd().before(it.getEnd()))
            ).count() > 0;

            if (isInvalidPeriod) {
                throw new TimeTrackingException("Period does exist");
            }
        }
    }

    private void validateDay(Integer documentNumber, DateTime startTime, TimeTracking act) {
        if (usersApiClient.isHoliday(startTime)) {
            // TODO: DISPARAR NOTIFICATION
            // throw new TimeTrackingException("Es Feriado!");
            log.info(String.format("El numero de documento: %s fichó el día %s que es feriado",
                    documentNumber.toString(), startTime.toDate().toString())
            );
        }

        if (startTime.getDayOfWeek() == 7) {
            // TODO: DISPARAR NOTIFICATION
            // throw new TimeTrackingException("Es domingo!");
            log.info(String.format("El numero de documento: %s fichó el día %s que es domingo",
                    documentNumber.toString(), startTime.toDate().toString())
            );
        }

        if (!hasValidShift(documentNumber, act)) {
            // TODO: DISPARAR NOTIFICATION
            log.info(String.format("El numero de documento: %s fichó el día %s que es el turno incorrecto",
                    documentNumber.toString(), startTime.toDate().toString())
            );
            throw new TimeTrackingException("Wrong Shift!");
        }

        List<AbsenceDetail> absenceDetails = getAbsenceDetails(documentNumber);

        if (absenceDetails != null && hasJustification(documentNumber, act, absenceDetails)) {
            // TODO: DISPARAR NOTIFICATION
            log.info(String.format("El numero de documento: %s fichó el día %s que tiene justificado el día por ausencia",
                    documentNumber.toString(), startTime.toDate().toString())
            );
            throw new TimeTrackingException("You can't track with absence!");
        }

        if (absenceDetails != null && hasSanction(documentNumber, act, absenceDetails)) {
            // TODO: DISPARAR NOTIFICATION
            log.info(String.format("El numero de documento: %s fichó el día %s que tiene una sación vigente",
                    documentNumber.toString(), startTime.toDate().toString())
            );
            throw new TimeTrackingException("You can't track with sanction!");
        }
    }

    private Tracking initializeTracking(Integer documentNumber, Tracking tracking) {
        if (tracking == null) {
            tracking = new Tracking();
            tracking.setDocumentNumber(documentNumber);
            tracking.setAbsences(0);
            tracking.setActive(true);
            tracking.setTimeTracking(new ArrayList<>());

            tracking = trackingRepository.save(tracking);
        }
        if(tracking.getTimeTracking() == null) {
            tracking.setTimeTracking(new ArrayList<>());
        }

        return tracking;
    }

    private Boolean hasJustification(Integer documentNumber, TimeTracking today, List<AbsenceDetail> absenceDetail) {
        if(absenceDetail.isEmpty()) {
            return false;
        }

        // TODO: Remove hardcode values
        return absenceDetail.stream().filter(it -> it.getType().equalsIgnoreCase("justificacion") &&
                (it.getStart().compareTo(today.getStart()) * today.getStart().compareTo(it.getEnd()) >= 0)
        ).count() > 0;
    }

    private Boolean hasSanction(Integer documentNumber, TimeTracking today, List<AbsenceDetail> absenceDetail) {
        if(absenceDetail.isEmpty()) {
            return false;
        }

        // TODO: Remove hardcode values
        return absenceDetail.stream().filter(it ->  it.getType().equalsIgnoreCase("sancion") &&
                (it.getStart().compareTo(today.getStart()) * today.getStart().compareTo(it.getEnd()) >= 0)
        ).count() > 0;
    }

    private List<AbsenceDetail> getAbsenceDetails(Integer documentNumber) {
        Absence absence = usersApiClient.findAbsenceByDocumentNumber(documentNumber);

        if(absence==null) {
            return null;
        }

       return absence.getAbsenceDetails();
    }

    private Boolean hasValidShift(Integer documentNumber, TimeTracking timeTracking) {
        try {
            String shitId = usersApiClient.findEmployeeByDocumentNumber(documentNumber);

            if(Strings.isNullOrEmpty(shitId)) {
                return false;
            }

            Shift shift = usersApiClient.findShiftByShiftId(shitId);

            if(shift ==  null) {
                return false;
            }

            List<Integer> daysOfWeek = shift.getDaysOfWeek();
            DateTime day = timeTracking.getDay();
            if (!daysOfWeek.contains(day.getDayOfWeek())) {
                return false;
            }

            Integer hour = shift.getStartHour();
            // TODO: que onda los minutos?? Integer minutes = shift.getMinutes();

            return day.getHourOfDay() < hour;

        } catch (Exception jse) {
            throw new TimeTrackingException(jse.getMessage());
        }
    }
}
