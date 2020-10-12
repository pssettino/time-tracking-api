package com.scrumbox.mm.timetrackingapi.model;

import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class Shift implements Serializable{
    private Integer shiftId;
    private String descripcion;
    private List<Integer> daysOfWeek;
    private Integer startHour;
    private Integer startMinutes;
    private Integer endtHour;
    private Integer endtMinutes;
}

