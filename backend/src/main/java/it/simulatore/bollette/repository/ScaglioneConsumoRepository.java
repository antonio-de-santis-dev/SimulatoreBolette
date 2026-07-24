package it.simulatore.bollette.repository;

import it.simulatore.bollette.entity.ScaglioneConsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScaglioneConsumoRepository extends JpaRepository<ScaglioneConsumo, Long> {
}
