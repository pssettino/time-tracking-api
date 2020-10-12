package com.scrumbox.mm.timetrackingapi.model;

import lombok.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class Employee implements Serializable {
    private Integer documentNumber;
    private String documentType;
    private String fullName;
    private String email;
    private Date startDate;
    private Date endDate;
    private Boolean status;
    private ERole role;
    private Address address;
    private List<String> telephones;
    private String cuil;
    private Date birthday;
    private Integer shiftId;
    private String imageProfile;
    private Boolean extraHoursAvailable;
}
