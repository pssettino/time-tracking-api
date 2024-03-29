package com.scrumbox.mm.timetrackingapi.unit.service;

import com.scrumbox.mm.timetrackingapi.persistence.domain.TimeTracking;
import com.scrumbox.mm.timetrackingapi.utils.DateUtils;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TimeTrackingTest {

    @Test
    public void testCalculateDuration() {
        TimeTracking act;
        DateTime startTime = new DateTime(DateUtils.getNowAsDateTime());

        act = new TimeTracking(startTime.toDate(), startTime.plusMinutes(45).toDate(), null);
        Assertions.assertEquals(0.75, act.getDuration(), 0);

        act = new TimeTracking(startTime.toDate(), startTime.plusMinutes(30).toDate(), null);
        Assertions.assertEquals(0.5, act.getDuration(), 0);

        act = new TimeTracking(startTime.toDate(), startTime.plusHours(1).plusMinutes(30).toDate(), null);
        Assertions.assertEquals(1.5, act.getDuration(), 0);

        act = new TimeTracking(startTime.toDate(), startTime.plusMinutes(20).toDate(), null);
        Assertions.assertEquals(1.0/3, act.getDuration(), 0);
    }

    /**
     * Tests that start and end times are on the same day,
     * unless end time is at 0:00h in which case end date is on the next day.
     */
    @Test
    public void testStartAndEndOnSameDay() {
        TimeTracking act = new TimeTracking(new DateTime(2009, 1, 1, 0, 0, 0, 0).toDate(),
                new DateTime(2009, 1, 1, 23, 0, 0 ,0).toDate(), null);

        DateTime start = new DateTime(act.getStart());
        DateTime end = new DateTime(act.getEnd());

        Assertions.assertEquals(1, start.getDayOfMonth());
        Assertions.assertEquals(1, end.getDayOfMonth());

        // when end is at 0:00h it must be on the next day
        act.setEndTime(0, 0);
        end = new DateTime(act.getEnd());
        Assertions.assertEquals(2, end.getDayOfMonth());

        // otherwise it must be on the same day as start
        act.setEndTime(12, 0);
        end = new DateTime(act.getEnd());
        Assertions.assertEquals(1, end.getDayOfMonth());

        // test again: when end is at 0:00h it must be on the next day
        act.setEndTime(0, 0);
        end = new DateTime(act.getEnd());
        Assertions.assertEquals(2, end.getDayOfMonth());

        // start day must not change:
        act.setEndTime(11, 55);
        start = new DateTime(act.getStart());
        Assertions.assertEquals(1, start.getDayOfMonth());

        act.setEndTime(0, 0);
        start = new DateTime(act.getStart());
        Assertions.assertEquals(1, start.getDayOfMonth());
    }

    /**
     * Tests that an exception is thrown when someone tries to set
     * end < start.
     */
    @Test
    public void testStartNotAfterEnd() {
        try {
            new TimeTracking(new DateTime(2009, 1, 1, 13, 0, 0 ,0).toDate(),
                    new DateTime(2009, 1, 1, 12, 0, 0, 0).toDate(), null);
            Assertions.fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            // ok, expected
        }

        try {
            TimeTracking act = new TimeTracking(new DateTime(2009, 1, 1, 11, 0, 0 ,0).toDate(),
                    new DateTime(2009, 1, 1, 12, 0, 0, 0).toDate(), null);
            act.setEndTime(10, 0);
            Assertions.fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            // ok, expected
        }

        try {
            TimeTracking act = new TimeTracking(new DateTime(2009, 1, 1, 11, 0, 0 ,0).toDate(),
                    new DateTime(2009, 1, 1, 12, 0, 0, 0).toDate(), null);
            act.setEndTime(13, 0);
        } catch (RuntimeException e) {
            Assertions.fail("Unexpected RuntimeException");
        }
    }

    /**
     * Tests the setDay method.
     */
    @Test
    public void testSetDay() {
        {
            TimeTracking act = new TimeTracking(
                    new DateTime(2009, 1, 1, 11, 0, 0 ,0).toDate(),
                    new DateTime(2009, 1, 1, 12, 47, 0, 0).toDate(),
                    null
            );
            DateTime day = act.getDay();
            Assertions.assertEquals(1, day.getDayOfMonth());
            Assertions.assertEquals(1, day.getMonthOfYear());
            Assertions.assertEquals(2009, day.getYear());

            act.setDay(new DateTime(2020, 7, 13, 11, 0, 0 ,0));
            day = act.getDay();
            Assertions.assertEquals(13, day.getDayOfMonth());
            Assertions.assertEquals(7, day.getMonthOfYear());
            Assertions.assertEquals(2020, day.getYear());

            DateTime end = new DateTime(act.getEnd());
            Assertions.assertEquals(13, end.getDayOfMonth());
            Assertions.assertEquals(7, end.getMonthOfYear());
            Assertions.assertEquals(2020, end.getYear());
            Assertions.assertEquals(12, end.getHourOfDay());
            Assertions.assertEquals(47, end.getMinuteOfHour());
        }

        // these time with an activity ending at 0:00h
        {
            TimeTracking act = new TimeTracking(
                    new DateTime(2009, 1, 1, 11, 0, 0 ,0).toDate(),
                    new DateTime(2009, 1, 2, 0, 0, 0, 0).toDate(),
                    null
            );
            DateTime day = act.getDay();
            Assertions.assertEquals(1, day.getDayOfMonth());
            Assertions.assertEquals(1, day.getMonthOfYear());
            Assertions.assertEquals(2009, day.getYear());

            act.setDay(new DateTime(2020, 7, 13, 11, 0, 0 ,0));
            day = act.getDay();
            Assertions.assertEquals(13, day.getDayOfMonth());
            Assertions.assertEquals(7, day.getMonthOfYear());
            Assertions.assertEquals(2020, day.getYear());

            DateTime end = new DateTime(act.getEnd());
            Assertions.assertEquals(14, end.getDayOfMonth());
            Assertions.assertEquals(7, end.getMonthOfYear());
            Assertions.assertEquals(2020, end.getYear());
            Assertions.assertEquals(0, end.getHourOfDay());
            Assertions.assertEquals(0, end.getMinuteOfHour());
        }
    }
}
