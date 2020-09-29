package com.scrumbox.mm.timetrackingapi.controller;

import com.scrumbox.mm.timetrackingapi.exception.TimeTrackingException;
import com.scrumbox.mm.timetrackingapi.model.TrackingRequest;
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
    public void manualTrackTime(@RequestBody TrackingRequest request) { trackingService.trackTime(request); }

    @PutMapping("/{documentNumber}")
    public void automaticTrackTime(@PathVariable Integer documentNumber) {
        trackingService.trackTime(documentNumber);
    }

    @GetMapping("/{documentNumber}")
    public Tracking findByDocumentNumber(@PathVariable Integer documentNumber) {
        return trackingService.findByDocumentNumber(documentNumber);
    }
}
