package com.scrumbox.mm.timetrackingapi.persistence.domain;

import com.scrumbox.mm.timetrackingapi.exception.TimeTrackingException;
import lombok.*;
import org.joda.time.DateTime;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "time_tracking")
public class TimeTracking implements Serializable, Comparable<TimeTracking> {

    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tracking_id", referencedColumnName = "id")
    private Tracking tracking;

    private Date start;
    private Date end;

    public TimeTracking() {
    }

    public TimeTracking(final Date start, final Date end, final Tracking tracking) {
        DateTime startDate = new DateTime(start);
        DateTime endDate = new DateTime(end);
        if (startDate.isAfter(endDate)) {
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
        DateTime newStartDay = new DateTime(getStart());
        DateTime startDay = newStartDay.withYear(day.getYear()).withMonthOfYear(day.getMonthOfYear())
                .withDayOfMonth(day.getDayOfMonth());

        this.start = startDay.toDate();

        DateTime newEndDay = new DateTime(getEnd());
        newEndDay = newEndDay.withYear(day.getYear()).withMonthOfYear(day.getMonthOfYear())
                .withDayOfMonth(day.getDayOfMonth());
        if (newEndDay.getHourOfDay() == 0 && newEndDay.getMinuteOfHour() == 0) {
            newEndDay = newEndDay.plusDays(1);
        }

        this.end = newEndDay.toDate();
    }

    /**
     * Returns the day of the activity.
     * Hours, minutes, seconds of the returned value are to be ignored.
     */
    public DateTime getDay() {
        return new DateTime(getStart()).withMillisOfDay(0);
    }

    public Date getEnd() {
        return end;
    }

    public Date getStart() {
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
        DateTime endDate = new DateTime(getEnd());
        DateTime starDate = new DateTime(getStart());
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

        if (endDate.isBefore(starDate)) {
            throw new TimeTrackingException("End time may not be before start time!");
        }

        this.end = endDate.toDate();
    }


    @Override
    public int compareTo(final TimeTracking activity) {
        if (activity == null) {
            return 0;
        }

        // Sort by start date but the other way round. That way the latest
        // activity is always on top.
        DateTime starDate = new DateTime(getStart());
        DateTime activityStart = new DateTime(activity.getStart());
        final DateTime startDateTime =  this.getDay().withHourOfDay(starDate.getHourOfDay()).withMinuteOfHour(starDate.getMinuteOfHour());
        final DateTime startDateTimeOther =  activity.getDay().withHourOfDay(activityStart.getHourOfDay()).withMinuteOfHour(activityStart.getMinuteOfHour());

        return startDateTime.compareTo(startDateTimeOther) * -1;
    }

    /**
     * Calculate the duration of the given activity in decimal hours.
     * @return decimal value of the duration (e.g. for 30 minutes, 0.5 and so on)
     */
    public final double getDuration() {
        DateTime startDate = new DateTime(start);
        DateTime endDate = new DateTime(end);
        final long timeMilliSec = endDate.getMillis() - startDate.getMillis();
        final double timeMin = timeMilliSec / 1000.0 / 60.0;
        return timeMin / 60.0;
    }

    public void setTracking(Tracking tracking) {
        this.tracking = tracking;
        tracking.getTimeTracking().add(this);
    }
}
