package com.scrumbox.mm.timetrackingapi.persistence.repository;

import com.scrumbox.mm.timetrackingapi.persistence.domain.Fichaje;
import com.scrumbox.mm.timetrackingapi.persistence.domain.Fichaje;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface FichajeRepository  extends MongoRepository<Fichaje, String> {
    Optional<Fichaje> findByDni(Integer dni);
}
