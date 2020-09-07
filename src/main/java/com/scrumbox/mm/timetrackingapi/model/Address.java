package com.scrumbox.mm.timetrackingapi.model;

import lombok.*;

import java.io.Serializable;

@Data
public class Address implements Serializable {
    private String address;
    private String localidad;
    private String provincia;
    private String codigoPostal;
}
