package com.scrumbox.mm.timetrackingapi.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Shift implements Serializable{
    private String id;
    private Integer shiftId;
    private String descripcion;
    private Date start;
    private Date end;
}

