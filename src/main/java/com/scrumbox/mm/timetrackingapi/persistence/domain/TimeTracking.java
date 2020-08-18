package com.scrumbox.mm.timetrackingapi.persistence.domain;

import com.scrumbox.mm.timetrackingapi.exception.TimeTrackingException;
import lombok.*;
import org.joda.time.DateTime;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity(name = "time_tracking")
public class TimeTracking implements Serializable, Comparable<TimeTracking> {

    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    private Tracking tracking;

    private DateTime start;
    private DateTime end;

    public TimeTracking() {
    }

    public TimeTracking(final DateTime start, final DateTime end, final Tracking tracking) {
        if (start.isAfter(end)) {
            throw new TimeTrackingException("End time may not be before start time!");
        }
        this.start = start;
        this.end = end;
        this.tracking = tracking;
    }

    /**
     * Sets the day of the activity.
     * @param day the new activity day.
     *   Hours, minutes, seconds and so on in the passed value are ignored.
     */
    public void setDay(final DateTime day) {
        DateTime newStartDay = getStart();
        this.start = newStartDay.withYear(day.getYear()).withMonthOfYear(day.getMonthOfYear())
                .withDayOfMonth(day.getDayOfMonth());

        DateTime newEndDay = getEnd();
        newEndDay = newEndDay.withYear(day.getYear()).withMonthOfYear(day.getMonthOfYear())
                .withDayOfMonth(day.getDayOfMonth());
        if (newEndDay.getHourOfDay() == 0 && newEndDay.getMinuteOfHour() == 0) {
            newEndDay = newEndDay.plusDays(1);
        }

        this.end = newEndDay;
    }

    /**
     * Returns the day of the activity.
     * Hours, minutes, seconds of the returned value are to be ignored.
     */
    public DateTime getDay() {
        return getStart().withMillisOfDay(0);
    }

    public DateTime getEnd() {
        return end;
    }

    public DateTime getStart() {
        return start;
    }

    /**
     * Sets the end hours and minutes while respecting the class invariants.
     *
     * Note: When setting the end date to 0:00h it is always supposed to mean
     * midnight i.e. 0:00h the next day!
     * @throws IllegalArgumentException if end time is before start time
     */
    public void setEndTime(final int hours, final int minutes) {
        DateTime endDate = getEnd();
        if (hours == endDate.getHourOfDay() && minutes == endDate.getMinuteOfHour()) {
            return;
        }

        if (endDate.getHourOfDay() == 0
                && endDate.getMinuteOfHour() == 0) { // adjust day if old end was on midnight
            endDate = endDate.minusDays(1);
        } else if (hours == 0 && minutes == 0) { // adjust day if new end is on midnight
            endDate = endDate.plusDays(1);
        }

        endDate = endDate.withHourOfDay(hours).withMinuteOfHour(minutes);

        if (endDate.isBefore(getStart())) {
            throw new TimeTrackingException("End time may not be before start time!");
        }

        this.end = endDate;
    }


    @Override
    public int compareTo(final TimeTracking activity) {
        if (activity == null) {
            return 0;
        }

        // Sort by start date but the other way round. That way the latest
        // activity is always on top.
        final DateTime startDateTime =  this.getDay().withHourOfDay(getStart().getHourOfDay()).withMinuteOfHour(getStart().getMinuteOfHour());
        final DateTime startDateTimeOther =  activity.getDay().withHourOfDay(activity.getStart().getHourOfDay()).withMinuteOfHour(activity.getStart().getMinuteOfHour());

        return startDateTime.compareTo(startDateTimeOther) * -1;
    }

    /**
     * Calculate the duration of the given activity in decimal hours.
     * @return decimal value of the duration (e.g. for 30 minutes, 0.5 and so on)
     */
    public final double getDuration() {
        final long timeMilliSec = end.getMillis() - start.getMillis();
        final double timeMin = timeMilliSec / 1000.0 / 60.0;
        return timeMin / 60.0;
    }
}
