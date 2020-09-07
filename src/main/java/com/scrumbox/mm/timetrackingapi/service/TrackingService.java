package com.scrumbox.mm.timetrackingapi.service;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.scrumbox.mm.timetrackingapi.client.UsersApiClient;
import com.scrumbox.mm.timetrackingapi.exception.TimeTrackingException;
import com.scrumbox.mm.timetrackingapi.model.Holiday;
import com.scrumbox.mm.timetrackingapi.model.Justification;
import com.scrumbox.mm.timetrackingapi.model.JustificationDetail;
import com.scrumbox.mm.timetrackingapi.model.Shift;
import com.scrumbox.mm.timetrackingapi.persistence.domain.Tracking;
import com.scrumbox.mm.timetrackingapi.persistence.domain.TimeTracking;
import com.scrumbox.mm.timetrackingapi.persistence.repository.TimeTrackingRepository;
import com.scrumbox.mm.timetrackingapi.persistence.repository.TrackingRepository;
import com.scrumbox.mm.timetrackingapi.utils.DateUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

    public Tracking trackTime(Integer dni) {
        Tracking tracking = findByDni(dni);

        tracking = initializeTracking(dni, tracking);

        List<TimeTracking> lastTimeTracking = tracking.getTimeTracking();
        DateTime startTime = DateUtils.getNowAsDateTime();

        TimeTracking act = lastTimeTracking.stream().reduce((first, second) -> second)
                .orElse(new TimeTracking(startTime.toDate(), startTime.toDate(), tracking));

        if (usersApiClient.isHoliday(startTime)) {
            // TODO: DISPARAR NOTIFICATION
            throw new TimeTrackingException("Es Feriado!");
        }

        if (startTime.getDayOfWeek() == 7) {
            // TODO: DISPARAR NOTIFICATION
            throw new TimeTrackingException("Es domingo!");
        }

        if (!hasValidShift(dni, act)) {
            // TODO: DISPARAR NOTIFICATION
            throw new TimeTrackingException("Turno incorrecto!");
        }

        if (hasJustification(dni, act)) {
            // TODO: DISPARAR NOTIFICATION
            throw new TimeTrackingException("No puede fichar si tiene justificado el día por ausencia!");
        }

        // LOS AUSENTES SE CALCULAN COMPARANDO LAS SUM(DURACION DE HORAS TRABAJAS DEL EMPLEADO DEL MES) CONTRA  TOTALES * DIAS HABILES DEL MES

        if (act.getDay().getDayOfYear() == startTime.getDayOfYear()) {
            log.info("NUEVO HORARIO DE FIN CON DURACION:  " + act.getDuration());
            act.setEndTime(startTime.getHourOfDay(), startTime.getMinuteOfHour());
        }

        lastTimeTracking.add(act);

        timeTrackingRepository.saveAll(lastTimeTracking);

        return tracking;
    }

    private Tracking initializeTracking(Integer dni, Tracking tracking) {
        if (tracking == null) {
            tracking = new Tracking();
            tracking.setDni(dni);
            tracking.setAbsences(0);
            tracking.setActive(true);
            tracking.setTimeTracking(new ArrayList<TimeTracking>());
        }
        return tracking;
    }

    // @Cacheable
    public Tracking findByDni(Integer dni) {
        Optional<Tracking> tracking = trackingRepository.findByDni(dni);
        return tracking != null ? tracking.get() : null;
    }


    private Boolean hasJustification(Integer dni, TimeTracking today) {
        Justification justification = usersApiClient.findJustificationByDni(dni);

        if(justification == null) {
            return false;
        }

        List<JustificationDetail> justificationDetail = justification.getJustificationDetail();


        return justificationDetail.stream().filter(it ->
                it.getStart().compareTo(today.getStart()) * today.getStart().compareTo(it.getEnd()) >= 0
        ).count() > 0;
    }

    private Boolean hasValidShift(Integer dni, TimeTracking timeTracking) {
        try {
            Integer shitId = usersApiClient.findEmployeeByDni(dni);
            Shift shift = usersApiClient.findShiftByShiftId(shitId);

            List<Integer> daysOfWeek = shift.getDaysOfWeek();
            DateTime day = timeTracking.getDay();
            if (!daysOfWeek.contains(day.getDayOfWeek())) {
                return false;
            }

            Integer hour = shift.getHour();
            // TODO: que onda los minutos?? Integer minutes = shift.getMinutes();

            return day.getHourOfDay() < hour;

        } catch (JSONException jse) {
            throw new TimeTrackingException(jse.getMessage());
        }
    }
}
