package com.scrumbox.mm.timetrackingapi.model;

import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class Shift implements Serializable{
    private String id;
    private Integer shiftId;
    private String descripcion;
    private List<Integer> daysOfWeek;
    private Integer hour;
    private Integer minutes;
    private Boolean extraHoursAvailable;
}

