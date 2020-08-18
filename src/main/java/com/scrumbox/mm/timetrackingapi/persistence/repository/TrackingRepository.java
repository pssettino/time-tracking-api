package com.scrumbox.mm.timetrackingapi.persistence.repository;

import com.scrumbox.mm.timetrackingapi.persistence.domain.Tracking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface TrackingRepository extends JpaRepository<Tracking, Integer> {
    Optional<Tracking> findByDni(Integer dni);
}
