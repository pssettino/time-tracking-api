package com.scrumbox.mm.timetrackingapi.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Sanction implements Serializable {
    private Integer documentNumber;
    private List<SanctionDetail> sanctionDetail;
}
