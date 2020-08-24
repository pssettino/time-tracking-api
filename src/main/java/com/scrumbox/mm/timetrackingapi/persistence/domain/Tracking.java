package com.scrumbox.mm.timetrackingapi.persistence.domain;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="tracking")
public class Tracking {

    @Id
    @GeneratedValue
    private Integer id;

    private Integer dni;
    private Integer absences;
    private Boolean status;

    @OneToMany(
            mappedBy = "tracking",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY
    )
    private List<TimeTracking> timeTracking;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDni() {
        return dni;
    }

    public void setDni(Integer dni) {
        this.dni = dni;
    }

    public Integer getAbsences() {
        return absences;
    }

    public void setAbsences(Integer absences) {
        this.absences = absences;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public List<TimeTracking> getTimeTracking() {
        return timeTracking;
    }

    public void setTimeTracking(List<TimeTracking> timeTracking) {
        this.timeTracking = timeTracking;
    }
}
