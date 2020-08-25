package com.scrumbox.mm.timetrackingapi.service;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.scrumbox.mm.timetrackingapi.exception.TimeTrackingException;
import com.scrumbox.mm.timetrackingapi.model.Holiday;
import com.scrumbox.mm.timetrackingapi.model.HolidayList;
import com.scrumbox.mm.timetrackingapi.model.Shift;
import com.scrumbox.mm.timetrackingapi.persistence.domain.Tracking;
import com.scrumbox.mm.timetrackingapi.persistence.domain.TimeTracking;
import com.scrumbox.mm.timetrackingapi.persistence.repository.TimeTrackingRepository;
import com.scrumbox.mm.timetrackingapi.persistence.repository.TrackingRepository;
import com.scrumbox.mm.timetrackingapi.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

//@CacheConfig(cacheNames = {"tracking"})
@Service
public class TrackingService {

    private static final Logger log = LoggerFactory.getLogger(TrackingService.class);

    private final String USER_SERVICE = "users-api";
    private final String USER_API = "http://%s:%s/" + USER_SERVICE + "/api/";

    private TimeTrackingRepository timeTrackingRepository;
    private TrackingRepository trackingRepository;
    private RestTemplate restTemplate;
    private EurekaClient eurekaClient;

    @Autowired
    public TrackingService(TrackingRepository trackingRepository,
                           TimeTrackingRepository timeTrackingRepository,
                           @Qualifier("eurekaClient") final EurekaClient discoveryClient) {

        this.trackingRepository = trackingRepository;
        this.timeTrackingRepository = timeTrackingRepository;
        this.eurekaClient = discoveryClient;
        restTemplate = new RestTemplate();
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

        if(isHoliday(startTime)) {
            // TODO: HACE FALTA QUE SE PERIMTA FICHAR IGUAL?
            // DISPARAR NOTIFICATION
            throw new TimeTrackingException("Es Feriado!");
        }

        if(startTime.getDayOfWeek() == 6) {
            // TODO: HACE FALTA QUE SE PERIMTA FICHAR IGUAL?
            // DISPARAR NOTIFICATION
            throw new TimeTrackingException("Es domingo!");
        }

        if(!hasValidShift(dni, act)) {
            // TODO: HACE FALTA QUE SE PERIMTA FICHAR IGUAL?
            // TODO: VERIFICAR TURNOS DENTRO DEL MISMO DIA
            // DISPARAR NOTIFICATION
            throw new TimeTrackingException("Turno incorrecto!");
        }

        if(hasJustification(dni)) {
            // TODO: HACE FALTA QUE SE PERIMTA FICHAR IGUAL?
            // DISPARAR NOTIFICATION
            // TENER EN CUENTA LOS EN USER-API LOS PERIODOS DE VACACIONES, LAS JUSTICACIONES POR ENFERMEDAD, ECT
            throw new TimeTrackingException("No puede fichar si tiene justificado el día por ausencia!");
        }

        // NO HACE FALTA SETEAR LOS AUSENTES YA QUE SON CALCULABLES.
        // LOS AUSENTES SE CALCULAN COMPARANDO LAS SUM(DURACION DE HORAS TRABAJAS DEL EMPLEADO DEL MES) CONTRA  TOTALES * DIAS HABILES DEL MES

        if(act.getDay().getDayOfYear() == startTime.getDayOfYear()) {
            log.info("NUEVO HORARIO DE FIN CON DURACION:  "+ act.getDuration());
            act.setEndTime(startTime.getHourOfDay(), startTime.getMinuteOfHour());
        }

        lastTimeTracking.add(act);

        timeTrackingRepository.saveAll(lastTimeTracking);

        return tracking;
    }

    private Tracking initializeTracking(Integer dni, Tracking tracking) {
        if(tracking == null) {
            tracking = new Tracking();
            tracking.setDni(dni);
            tracking.setAbsences(0);
            tracking.setStatus(true);
            tracking.setTimeTracking( new ArrayList<TimeTracking>());
        }
        return tracking;
    }

    // @Cacheable
    public Tracking findByDni(Integer dni) {
        Optional<Tracking> fichaje = trackingRepository.findByDni(dni);
        return fichaje.orElse(null);
    }

    private Boolean hasJustification(Integer dni) {
        // TODO: Call to JustificationService
        return true; // No tiene justificativo de ausencia.
    }

    private Boolean hasValidShift(Integer dni, TimeTracking timeTracking) {
        try{
            Integer shitId = findEmployeeByDni(dni);
            Shift shift = findShiftByShiftId(shitId);

            Date start = shift.getStart();
            Date end = shift.getEnd();

            return timeTracking.getStart().after(start) && timeTracking.getStart().before(end);
        } catch (JSONException jse){
            throw new TimeTrackingException(jse.getMessage());
        }
    }
    private Integer findEmployeeByDni(Integer dni) throws JSONException {

        //Find the User Microservice
        InstanceInfo userInstance = eurekaClient.getApplication(USER_SERVICE).getInstances().get(0);

        //Get the service URL
        String serviceUrl = String.format(USER_API + "users?dni=%s",userInstance.getIPAddr(), userInstance.getPort(),dni.toString());

        log.info("User service url: {}", serviceUrl);

        //Get the user
        ResponseEntity<JSONObject> response = restTemplate.getForEntity(serviceUrl, JSONObject.class);

        log.info("Response: {}", response.getBody());

        return (Integer) Objects.requireNonNull(response.getBody()).get("shift_id");
    }

    private Shift findShiftByShiftId(Integer shiftId) {

        InstanceInfo userInstance = eurekaClient.getApplication(USER_SERVICE).getInstances().get(0);

        String serviceUrl = String.format(USER_API + "api/shift?shitId=%s",userInstance.getIPAddr(), userInstance.getPort(),shiftId.toString());

        log.info("User service url: {}", serviceUrl);

        //Get the user
        ResponseEntity<Shift> response = restTemplate.getForEntity(serviceUrl, Shift.class);

        log.info("Response: {}", response.getBody());

        return response.getBody();
    }

    private Boolean isHoliday(DateTime today){
        String serviceUrl = String.format("https://nolaborables.com.ar/api/v2/feriados/%s", today.getYear());
        ResponseEntity<HolidayList> response = restTemplate.getForEntity(serviceUrl, HolidayList.class);


        List<Holiday> holidays = response.getBody().getHolidays();

        return holidays.stream().filter(it -> it.getDia() == today.getDayOfMonth() && it.getMes() == today.getMonthOfYear()).count() > 0;

    }
}
