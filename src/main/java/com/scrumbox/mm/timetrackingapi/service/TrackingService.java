package com.scrumbox.mm.timetrackingapi.service;

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

//@CacheConfig(cacheNames = {"tracking"})
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

    // @Cacheable
    public List<Tracking> getAll() {
        return trackingRepository.findAll();
    }

    public Tracking save(Tracking tracking) {
        return trackingRepository.save(tracking);
    }

    public TimeTracking save(TimeTracking timeTracking) {
        return timeTrackingRepository.save(timeTracking);
    }

    public Tracking trackTime(Integer documentNumber) {
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
        }

        lastTimeTracking.add(act);

        timeTrackingRepository.saveAll(lastTimeTracking);

        return tracking;
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
            throw new TimeTrackingException("Turno incorrecto!");
        }

        List<AbsenceDetail> absenceDetails = getAbsenceDetails(documentNumber);

        if (hasJustification(documentNumber, act, absenceDetails)) {
            // TODO: DISPARAR NOTIFICATION
            log.info(String.format("El numero de documento: %s fichó el día %s que tiene justificado el día por ausencia",
                    documentNumber.toString(), startTime.toDate().toString())
            );
            throw new TimeTrackingException("No puede fichar si tiene justificado el día por ausencia!");
        }

        if (hasSanction(documentNumber, act, absenceDetails)) {
            // TODO: DISPARAR NOTIFICATION
            log.info(String.format("El numero de documento: %s fichó el día %s que tiene una sación vigente",
                    documentNumber.toString(), startTime.toDate().toString())
            );
            throw new TimeTrackingException("No puede fichar si tiene está sancionado!");
        }
    }

    private Tracking initializeTracking(Integer documentNumber, Tracking tracking) {
        if (tracking == null) {
            tracking = new Tracking();
            tracking.setDocumentNumber(documentNumber);
            tracking.setAbsences(0);
            tracking.setActive(true);
            tracking.setTimeTracking(new ArrayList<TimeTracking>());

            tracking = trackingRepository.save(tracking);
        }

        return tracking;
    }

    // @Cacheable
    public Tracking findByDocumentNumber(Integer documentNumber) {
        Optional<Tracking> tracking = trackingRepository.findByDocumentNumber(documentNumber);
        return tracking.isPresent() ? tracking.get() : null;
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

       return absence.getAbsenceDetails();
    }

    private Boolean hasValidShift(Integer documentNumber, TimeTracking timeTracking) {
        try {
            Integer shitId = usersApiClient.findEmployeeByDocumentNumber(documentNumber);

            if(shitId ==  null) {
                return false;
            }

            Shift shift = usersApiClient.findShiftByShiftId(shitId);

            if(shift ==  null) {
                return null;
            }

            List<Integer> daysOfWeek = shift.getDaysOfWeek();
            DateTime day = timeTracking.getDay();
            if (!daysOfWeek.contains(day.getDayOfWeek())) {
                return false;
            }

            Integer hour = shift.getHour();
            // TODO: que onda los minutos?? Integer minutes = shift.getMinutes();

            return day.getHourOfDay() < hour;

        } catch (Exception jse) {
            throw new TimeTrackingException(jse.getMessage());
        }
    }
}
