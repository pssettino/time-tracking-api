package com.scrumbox.mm.timetrackingapi.persistence.domain;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="tracking")
public class Tracking {

    @Id
    @GeneratedValue
    private Integer id;

    private Integer documentNumber;
    private Integer absences;
    private Boolean active;

    @OneToMany(
            mappedBy = "tracking",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY
    )
    private List<TimeTracking> timeTracking;


    public Tracking() {
    }

    public Tracking(Integer documentNumber, Integer absences, Boolean active) {
        this.documentNumber = documentNumber;
        this.absences = absences;
        this.active = active;
    }

    public Tracking(Integer documentNumber, Integer absences, Boolean active, List<TimeTracking> timeTracking) {
        this.documentNumber = documentNumber;
        this.absences = absences;
        this.active = active;
        this.timeTracking = timeTracking;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(Integer documentNumber) {
        this.documentNumber = documentNumber;
    }

    public Integer getAbsences() {
        return absences;
    }

    public void setAbsences(Integer absences) {
        this.absences = absences;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<TimeTracking> getTimeTracking() {
        return timeTracking;
    }

    public void setTimeTracking(List<TimeTracking> timeTracking) {
        this.timeTracking = timeTracking;
    }
}
