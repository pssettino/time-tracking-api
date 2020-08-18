package com.scrumbox.mm.timetrackingapi.service;

import com.scrumbox.mm.timetrackingapi.persistence.domain.Tracking;
import com.scrumbox.mm.timetrackingapi.persistence.domain.TimeTracking;
import com.scrumbox.mm.timetrackingapi.persistence.repository.TrackingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TrackingService {

    private TrackingRepository trackingRepository;

    @Autowired
    public TrackingService(TrackingRepository trackingRepository) {
        this.trackingRepository = trackingRepository;
    }

    @Cacheable("tracking")
    public List<Tracking> getAll() {
        return trackingRepository.findAll();
    }

    public Tracking save(Tracking tracking) {
        return trackingRepository.save(tracking);
    }

    @Cacheable("tracking")
    public Tracking findByDni(Integer dni) {
        Optional<Tracking> fichaje = trackingRepository.findByDni(dni);
        return fichaje.orElse(null);
    }
}
