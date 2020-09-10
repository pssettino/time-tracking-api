package com.scrumbox.mm.timetrackingapi.model;

import lombok.*;

import java.io.Serializable;

@Data
public class Address implements Serializable {
    private String address;
    private String city;
    private String country;
    private String postalCode;
}
