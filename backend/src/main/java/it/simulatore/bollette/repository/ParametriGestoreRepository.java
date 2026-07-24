package it.simulatore.bollette.repository;

import it.simulatore.bollette.entity.ParametriGestore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParametriGestoreRepository extends JpaRepository<ParametriGestore, Long> {

    Optional<ParametriGestore> findByPredefinitoTrue();

    Optional<ParametriGestore> findByNomeProfilo(String nomeProfilo);

    List<ParametriGestore> findAllByPredefinitoTrue();
}
