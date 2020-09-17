package com.scrumbox.mm.timetrackingapi.controller;

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
        List<TimeTracking> timeTrackingList = tracking.getTimeTracking();
        System.out.println("IS_EMPTY:" + timeTrackingList.isEmpty());
        if(!timeTrackingList.isEmpty()){
            TimeTracking timeTracking = timeTrackingList.get(0);
            System.out.println("timeTracking:" + timeTracking.getStart());
            Boolean hasNotTracking = trackingService.findByDocumentNumber(tracking.getDocumentNumber()) == null ? true : false;
            if(hasNotTracking) {
                System.out.println("tracking : NULL");
                tracking = trackingService.save(tracking);
                System.out.println("tracking new: "+ tracking.getId());
            }
            System.out.println("tracking not null: ");
            timeTracking.setTracking(tracking);
            trackingService.save(timeTracking);
        } else {
            trackingService.save(tracking);
        }
    }

    @PutMapping("/{documentNumber}")
    public void trackTime(@PathVariable Integer documentNumber) {
        trackingService.trackTime(documentNumber);
    }

    @GetMapping("/documentNumber")
    public Tracking findByDocumentNumber(@RequestParam Integer documentNumber) {
        return trackingService.findByDocumentNumber(documentNumber);
    }
}
