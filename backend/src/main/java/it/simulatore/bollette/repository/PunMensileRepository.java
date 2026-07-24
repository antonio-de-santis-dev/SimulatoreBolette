package it.simulatore.bollette.repository;

import it.simulatore.bollette.entity.PunMensile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PunMensileRepository extends JpaRepository<PunMensile, Long> {
    Optional<PunMensile> findByAnnoAndMese(Integer anno, Integer mese);
}
