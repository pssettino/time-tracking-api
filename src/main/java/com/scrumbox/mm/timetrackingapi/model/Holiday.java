package com.scrumbox.mm.timetrackingapi.model;

import lombok.*;

import java.io.Serializable;

@Data
public class Holiday implements Serializable{
    private String motivo;
    private String tipo;
    private String info;
    private Integer dia;
    private Integer mes;
    private String id;
}
