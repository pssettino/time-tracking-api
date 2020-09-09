package com.scrumbox.mm.timetrackingapi.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class SanctionDetail implements Serializable {
    private Date start;
    private Date end;
    private String description;
}
