package com.scrumbox.mm.timetrackingapi.service;

import com.scrumbox.mm.timetrackingapi.persistence.domain.Tracking;
import com.scrumbox.mm.timetrackingapi.persistence.domain.TimeTracking;
import com.scrumbox.mm.timetrackingapi.persistence.repository.TimeTrackingRepository;
import com.scrumbox.mm.timetrackingapi.persistence.repository.TrackingRepository;
import com.scrumbox.mm.timetrackingapi.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

//@CacheConfig(cacheNames = {"tracking"})
@Service
public class TrackingService {

    private TimeTrackingRepository timeTrackingRepository;
    private TrackingRepository trackingRepository;

    @Autowired
    public TrackingService(TrackingRepository trackingRepository,
                           TimeTrackingRepository timeTrackingRepository) {

        this.trackingRepository = trackingRepository;
        this.timeTrackingRepository = timeTrackingRepository;
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

        if(tracking == null) {
            tracking = new Tracking();
            tracking.setDni(dni);
            tracking.setAbsences(0);
            tracking.setStatus(true);
            tracking.setTimeTracking( new ArrayList<TimeTracking>());
        }
        List<TimeTracking> lastTimeTracking = tracking.getTimeTracking();
        DateTime startTime = DateUtils.getNowAsDateTime();

        TimeTracking act = lastTimeTracking.stream().reduce((first, second) -> second)
                .orElse(new TimeTracking(startTime.toDate(), startTime.toDate(), tracking));

        if(act.getDay().isAfterNow() && act.getDuration() > 9) {
            System.out.println("ES UN NUEVO DIA CON DURACION:  "+ act.getDuration());
            tracking.setAbsences(tracking.getAbsences() + 1);
        }

        if(act.getDay().getDayOfYear() == startTime.getDayOfYear()) {
            System.out.println("NUEVO HORARIO DE FIN CON DURACION:  "+ act.getDuration());
            act.setEndTime(startTime.getHourOfDay(), startTime.getMinuteOfHour());
        }

        if(act.getDuration() == 9) {
            System.out.println("TRABAJO LAS 9 HORAS");
        } else if(act.getDuration() < 9) {
            System.out.println("TRABAJO MENOS HORAS:  "+ act.getDuration());
        } else {
            System.out.println("TRABAJO MAS HORAS:  "+ act.getDuration());
        }

        lastTimeTracking.add(act);

        timeTrackingRepository.saveAll(lastTimeTracking);

        return tracking;
    }

    // @Cacheable
    public Tracking findByDni(Integer dni) {
        Optional<Tracking> fichaje = trackingRepository.findByDni(dni);
        return fichaje.orElse(null);
    }
}
