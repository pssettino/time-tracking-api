package com.scrumbox.mm.timetrackingapi.utils;

import org.joda.time.DateTime;

public abstract class DateUtils {

    public static DateTime getNowAsDateTime() {
        final DateTime now = new DateTime();
        return now.minuteOfDay().roundHalfCeilingCopy();
    }
}
