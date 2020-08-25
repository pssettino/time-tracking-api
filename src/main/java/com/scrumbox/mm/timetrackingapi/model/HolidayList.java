package com.scrumbox.mm.timetrackingapi.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class HolidayList implements Serializable{
    private List<Holiday> holidays;

    public HolidayList() {
        holidays = new ArrayList<>();
    }
}
