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

        DateTime startTime = DateUtils.getNowAsDateTime();
        System.out.println("StartTime -------------> "+ startTime);
        System.out.println("StartTime -------------> "+ startTime.plusHours(9));

        List<TimeTracking> lastTimeTracking = tracking.getTimeTracking();
        TimeTracking timeTracking = new TimeTracking(startTime.toDate(), startTime.plusHours(9).toDate(), tracking);
        //TimeTracking act = lastTimeTracking.stream().reduce((first, second) -> second)
         //       .orElse(new TimeTracking(startTime.toDate(), startTime.plusHours(9).toDate() , tracking));

        //if(act.getDuration() > 9) {
        //    tracking.setAbsences(tracking.getAbsences() + 1);
        // }

        lastTimeTracking.add(timeTracking);

        timeTrackingRepository.saveAll(lastTimeTracking);

        return tracking;
    }

    // @Cacheable
    public Tracking findByDni(Integer dni) {
        Optional<Tracking> fichaje = trackingRepository.findByDni(dni);
        return fichaje.orElse(null);
    }
}
