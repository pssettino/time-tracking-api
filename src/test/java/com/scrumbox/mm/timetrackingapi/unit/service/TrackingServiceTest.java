package com.scrumbox.mm.timetrackingapi.unit.service;

import com.scrumbox.mm.timetrackingapi.client.UsersApiClient;
import com.scrumbox.mm.timetrackingapi.exception.TimeTrackingException;
import com.scrumbox.mm.timetrackingapi.model.Shift;
import com.scrumbox.mm.timetrackingapi.model.TrackingRequest;
import com.scrumbox.mm.timetrackingapi.persistence.domain.TimeTracking;
import com.scrumbox.mm.timetrackingapi.persistence.domain.Tracking;
import com.scrumbox.mm.timetrackingapi.persistence.repository.TimeTrackingRepository;
import com.scrumbox.mm.timetrackingapi.persistence.repository.TrackingRepository;
import com.scrumbox.mm.timetrackingapi.service.TrackingService;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class TrackingServiceTest {

    TimeTrackingRepository timeTrackingRepository= Mockito.mock(TimeTrackingRepository.class);
    TrackingRepository trackingRepository= Mockito.mock(TrackingRepository.class);
    UsersApiClient usersApiClient = Mockito.mock(UsersApiClient.class);
    TrackingService trackingService = new TrackingService(trackingRepository, timeTrackingRepository, usersApiClient);

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
        Mockito.when(trackingRepository.findByDocumentNumber(Mockito.anyInt())).thenReturn(tracking);

        // WHEN
        Tracking result = trackingService.findByDocumentNumber(33633264);

        // THEN
        Assertions.assertTrue(result.getDocumentNumber() == 33633264);
    }

    @Test
    public void test_findByDni_when_hasNot_value() {
        // GIVEN
        Optional<Tracking> tracking = Optional.empty();
        Mockito.when(trackingRepository.findByDocumentNumber(Mockito.anyInt())).thenReturn(tracking);

        // WHEN
        Tracking result = trackingService.findByDocumentNumber(33633264);

        // THEN
        Assertions.assertNull(result);
    }

    @Test
    public void test_save_when_manual_track_is_ok() {
        // GIVEN
        Optional<Tracking> tracking = Optional.of(new Tracking(33633264, 0, true));
        TimeTracking timeTracking = new TimeTracking(new DateTime(2020, 9, 1, 11, 0, 0, 0).toDate(),
                new DateTime(2020, 9, 1, 18, 0, 0 ,0).toDate(), null);
        List<TimeTracking> list = new ArrayList<>();
        list.add(timeTracking);
        tracking.get().setTimeTracking(list);
        Mockito.when(trackingRepository.findByDocumentNumber(Mockito.anyInt())).thenReturn(tracking);
        Mockito.when(trackingRepository.save(Mockito.any())).thenReturn(tracking);
        Mockito.when(usersApiClient.findEmployeeByDocumentNumber(Mockito.anyInt())).thenReturn("1");
        Mockito.when(usersApiClient.findAbsenceByDocumentNumber(Mockito.anyInt())).thenReturn(null);

        Shift shift = new Shift();
        shift.setStartHour(9);
        List<Integer> days = new ArrayList<>();
        for (int i=0;i<8;i++) {
            days.add(i);
        }
        shift.setDaysOfWeek(days);
        Mockito.when(usersApiClient.findShiftByShiftId(Mockito.anyString())).thenReturn(shift);


        // WHEN
        trackingService.trackTime(new TrackingRequest(33633264, new Date(), new Date()));
    }

    @Test
    public void test_save_when_manual_track_has_equal_period() {
        // GIVEN
        Optional<Tracking> tracking = Optional.of(new Tracking(33633264, 0, true));
        TimeTracking timeTracking = new TimeTracking(new DateTime(2020, 9, 1, 11, 0, 0, 0).toDate(),
                new DateTime(2020, 9, 1, 18, 0, 0 ,0).toDate(), null);
        List<TimeTracking> list = new ArrayList<>();
        list.add(timeTracking);
        tracking.get().setTimeTracking(list);
        Mockito.when(trackingRepository.findByDocumentNumber(Mockito.anyInt())).thenReturn(tracking);


        // WHEN
        try{
            trackingService.trackTime(new TrackingRequest(33633264, new DateTime(2020, 9, 1, 11, 0, 0, 0).toDate(), new DateTime(2020, 9, 1, 18, 0, 0, 0).toDate()));
            Assertions.assertTrue(false);
        }catch (TimeTrackingException ex){
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void test_save_when_manual_track_has_invalid_period() {
        // GIVEN
        Optional<Tracking> tracking = Optional.of(new Tracking(33633264, 0, true));
        TimeTracking timeTracking = new TimeTracking(new DateTime(2020, 9, 1, 11, 0, 0, 0).toDate(),
                new DateTime(2020, 9, 1, 18, 0, 0 ,0).toDate(), null);
        List<TimeTracking> list = new ArrayList<>();
        list.add(timeTracking);
        tracking.get().setTimeTracking(list);
        Mockito.when(trackingRepository.findByDocumentNumber(Mockito.anyInt())).thenReturn(tracking);


        // WHEN
        try{
            trackingService.trackTime(new TrackingRequest(33633264, new DateTime(2020, 9, 1, 10, 0, 0, 0).toDate(), new DateTime(2020, 9, 1, 15, 0, 0, 0).toDate()));
            Assertions.assertTrue(false);
        }catch (TimeTrackingException ex){
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void test_save_when_automatic_track_is_ok() {
        // GIVEN
        Tracking tracking = new Tracking(33633264, 0, true);
        Optional<Tracking> optionalTracking = Optional.of(tracking);

        TimeTracking timeTracking = new TimeTracking(new DateTime(2020, 9, 1, 11, 0, 0, 0).toDate(),
                new DateTime(2020, 9, 1, 18, 0, 0 ,0).toDate(), null);

        List<TimeTracking> list = new ArrayList<>();
        //list.add(timeTracking);

        optionalTracking.get().setTimeTracking(list);
        Mockito.when(trackingRepository.findByDocumentNumber(Mockito.anyInt())).thenReturn(optionalTracking);
        Mockito.when(trackingRepository.save(Mockito.any())).thenReturn(tracking);
        Mockito.when(usersApiClient.findEmployeeByDocumentNumber(Mockito.anyInt())).thenReturn("1");
        Mockito.when(usersApiClient.findAbsenceByDocumentNumber(Mockito.anyInt())).thenReturn(null);

        Shift shift = new Shift();
        shift.setStartHour(9);
        List<Integer> days = new ArrayList<>();
        for (int i=0;i<8;i++) {
            days.add(i);
        }
        shift.setDaysOfWeek(days);
        Mockito.when(usersApiClient.findShiftByShiftId(Mockito.anyString())).thenReturn(shift);


        // WHEN
        trackingService.trackTime(33633264);
    }
}
