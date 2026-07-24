package it.simulatore.bollette.repository;

import it.simulatore.bollette.entity.MeseBolletta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeseBollettaRepository extends JpaRepository<MeseBolletta, Long> {
}
