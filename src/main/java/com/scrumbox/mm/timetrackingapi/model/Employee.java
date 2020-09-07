package com.scrumbox.mm.timetrackingapi.model;

import lombok.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class Employee implements Serializable {
    private String id;
    private Integer dni;
    private String tipoDni;
    private String fullName;
    private String email;
    private Date startDate;
    private Date endDate;
    private Boolean status;
    private ERole role;
    private Address address;
    private List<String> telefonos;
    private String cuil;
    private Date fechaNacimiento;
    private Integer shiftId;
    private String imageProfile;
}
