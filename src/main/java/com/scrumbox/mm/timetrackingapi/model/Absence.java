package com.scrumbox.mm.timetrackingapi.model;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
public class Absence implements Serializable {
    private Integer documentNumber;
    private List<AbsenceDetail> absenceDetails;
}
