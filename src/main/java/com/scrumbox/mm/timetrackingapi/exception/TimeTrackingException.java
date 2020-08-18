package com.scrumbox.mm.timetrackingapi.exception;

public class TimeTrackingException extends IllegalArgumentException {

    public TimeTrackingException() {
        super("Error in time tracking");
    }

    public TimeTrackingException(String s) {
        super(s);
    }
}

