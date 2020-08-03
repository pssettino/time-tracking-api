package com.scrumbox.mm.timetrackingapi.persistence.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;

@Document(collection = "fichajes")
@Setter
@Getter
@AllArgsConstructor
public class Fichaje {

    @Id
    private String id;

    @NotBlank
    @Size(max = 8)
    private Integer dni;

    private Date checkIn;

    private Date checkOut;
}
