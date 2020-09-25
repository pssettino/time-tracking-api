package com.scrumbox.mm.timetrackingapi.controller;

import com.scrumbox.mm.timetrackingapi.exception.TimeTrackingException;
import com.scrumbox.mm.timetrackingapi.persistence.domain.TimeTracking;
import com.scrumbox.mm.timetrackingapi.persistence.domain.Tracking;
import com.scrumbox.mm.timetrackingapi.service.TrackingService;
import com.scrumbox.mm.timetrackingapi.utils.DateUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/tracking")
public class TrackingController {
    @Autowired
    private TrackingService trackingService;

    @GetMapping("/")
    public List<Tracking> getAll() {
        return trackingService.getAll();
    }


    @PutMapping("/")
    public void addTracking(@RequestBody Tracking tracking) {

        // 1.- Obtengo el timeTracking que viene en el request, dentro de tracking como list.
        // 2.- Si la lista de timeTracking esta vacía lanzo exception
        // 3.- Caso contrario obtengo el primer elemento y me fijo si tiene el tracking creado o ya existe.
        // 4.- si no tiene tracking lo creo y luego al timeTracking le asigno su tracking y guardo el timeTracking

        List<TimeTracking> timeTrackingList = tracking.getTimeTracking();

        if(!timeTrackingList.isEmpty()) {
            TimeTracking timeTracking = timeTrackingList.get(0);
            Tracking dbTracking = trackingService.findByDocumentNumber(tracking.getDocumentNumber());

            tracking = flushTracking(dbTracking, tracking);

            validateStartAndEndDay(timeTracking, tracking);

            timeTracking.setTracking(tracking);
            trackingService.save(timeTracking);
        } else {
            throw new TimeTrackingException("Time Tracking is mandatory!");
        }
    }

    private Tracking flushTracking(Tracking dbTracking, Tracking tracking) {
        if (dbTracking == null) {
            return trackingService.save(tracking);
        }

        return dbTracking;
    }

    private void validateStartAndEndDay(TimeTracking timeTracking, Tracking dbTracking) {
        if(!dbTracking.getTimeTracking().isEmpty()) {
            Supplier<Stream<TimeTracking>> timeTrackingStream = () -> dbTracking.getTimeTracking().stream();
            boolean hasBeforeStartDay = timeTrackingStream.get().filter(it -> timeTracking.getStart().before(it.getStart())).count() > 0;

            if (hasBeforeStartDay) {
                throw new TimeTrackingException("Has before start day");
            }

            boolean hasBeforeEndDay = timeTrackingStream.get().filter(it -> timeTracking.getEnd().before(it.getEnd())).count() > 0;

            if (hasBeforeEndDay) {
                throw new TimeTrackingException("Has before end day");
            }
        }
    }

    @PutMapping("/{documentNumber}")
    public void trackTime(@PathVariable Integer documentNumber) {
        trackingService.trackTime(documentNumber);
    }

    @GetMapping("/{documentNumber}")
    public Tracking findByDocumentNumber(@PathVariable Integer documentNumber) {
        return trackingService.findByDocumentNumber(documentNumber);
    }
}
