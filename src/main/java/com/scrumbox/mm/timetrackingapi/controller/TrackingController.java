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
    public Tracking addTracking(@RequestBody Tracking tracking) {
        return trackingService.save(tracking);
    }

    @GetMapping("/{dni}")
    public Tracking trackTime(@PathVariable Integer dni) {
        return trackingService.trackTime(dni);
    }

    @GetMapping("/dni")
    public Tracking findByDni(@RequestParam Integer dni) {
        return trackingService.findByDni(dni);
    }
}
