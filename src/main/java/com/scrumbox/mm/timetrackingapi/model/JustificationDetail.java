package com.scrumbox.mm.timetrackingapi.model;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Data
public class JustificationDetail implements Serializable {
    private Date start;
    private Date end;
    private String description;
}
