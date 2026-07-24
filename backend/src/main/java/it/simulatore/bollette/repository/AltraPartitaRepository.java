package it.simulatore.bollette.repository;

import it.simulatore.bollette.entity.AltraPartita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AltraPartitaRepository extends JpaRepository<AltraPartita, Long> {
}
