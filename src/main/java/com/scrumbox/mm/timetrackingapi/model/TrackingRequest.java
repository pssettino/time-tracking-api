package com.scrumbox.mm.timetrackingapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
public class TrackingRequest implements Serializable {
    private Integer documentNumber;
    private Date start;
    private Date end;
}
