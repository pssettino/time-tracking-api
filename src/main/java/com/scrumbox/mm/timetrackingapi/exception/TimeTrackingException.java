package com.scrumbox.mm.timetrackingapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.EXPECTATION_FAILED)
public class TimeTrackingException extends RuntimeException {

    public TimeTrackingException() {
        super("Error in time tracking");
    }

    public TimeTrackingException(String s) {
        super(s);
    }
}

