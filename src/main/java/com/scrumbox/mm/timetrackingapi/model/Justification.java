package com.scrumbox.mm.timetrackingapi.model;

import lombok.*;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

@Data
public class Justification implements Serializable {
    private String id;
    private Integer dni;
    private List<JustificationDetail> justificationDetail;
}
