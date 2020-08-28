package com.scrumbox.mm.timetrackingapi.unit.service;

import com.netflix.discovery.EurekaClient;
import com.scrumbox.mm.timetrackingapi.persistence.domain.Tracking;
import com.scrumbox.mm.timetrackingapi.persistence.repository.TimeTrackingRepository;
import com.scrumbox.mm.timetrackingapi.persistence.repository.TrackingRepository;
import com.scrumbox.mm.timetrackingapi.service.TrackingService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TrackingServiceTest {

    TimeTrackingRepository timeTrackingRepository= Mockito.mock(TimeTrackingRepository.class);
    TrackingRepository trackingRepository= Mockito.mock(TrackingRepository.class);
    EurekaClient eurekaClient= Mockito.mock(EurekaClient.class);
    TrackingService trackingService = new TrackingService(trackingRepository, timeTrackingRepository, eurekaClient);

    @Test
    public void test_findAll_when_has_value() {
        // GIVEN
        Optional<Tracking> tracking = Optional.of(new Tracking(33633264, 0, true));
        List<Tracking> trackings = new ArrayList<Tracking>();
        trackings.add(tracking.get());

        // WHEN
        Mockito.when(trackingRepository.findAll()).thenReturn(trackings);
        List<Tracking> result = trackingService.getAll();

        // THEN
        Assertions.assertTrue(result.size() > 0);
    }

    @Test
    public void test_findByDni_when_has_value() {
        // GIVEN
        Optional<Tracking> tracking = Optional.of(new Tracking(33633264, 0, true));
        Mockito.when(trackingRepository.findByDni(Mockito.anyInt())).thenReturn(tracking);

        // WHEN
        Tracking result = trackingService.findByDni(33633264);

        // THEN
        Assertions.assertTrue(result.getDni() == 33633264);
    }

    @Test
    public void test_findByDni_when_hasNot_value() {
        // GIVEN
        Mockito.when(trackingRepository.findByDni(Mockito.anyInt())).thenReturn(null);

        // WHEN
        Tracking result = trackingService.findByDni(33633264);

        // THEN
        Assertions.assertNull(result);
    }

    @Test
    public void test_save_when_is_ok() {
        // GIVEN
        Optional<Tracking> tracking = Optional.of(new Tracking(33633264, 0, true));
        Mockito.when(trackingRepository.save(Mockito.any())).thenReturn(tracking.get());

        // WHEN
        Tracking result = trackingService.save(tracking.get());

        // THEN
        Assertions.assertTrue(result.getDni() == 33633264);
    }

}
