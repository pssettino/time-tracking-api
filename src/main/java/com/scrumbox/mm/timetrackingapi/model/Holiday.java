package com.scrumbox.mm.timetrackingapi.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Holiday {
    private String motivo;
    private String tipo;
    private String info;
    private Integer dia;
    private Integer mes;
    private String id;
}
