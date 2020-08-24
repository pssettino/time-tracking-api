package com.scrumbox.mm.timetrackingapi.persistence.repository;

import com.scrumbox.mm.timetrackingapi.persistence.domain.TimeTracking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeTrackingRepository extends JpaRepository<TimeTracking, Integer> {
}
