package it.simulatore.bollette.repository;

import it.simulatore.bollette.entity.Simulazione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SimulazioneRepository extends JpaRepository<Simulazione, Long> {
    List<Simulazione> findByNomeContainingIgnoreCase(String nome);
    List<Simulazione> findTop10ByOrderByCreatedAtDesc();
}
