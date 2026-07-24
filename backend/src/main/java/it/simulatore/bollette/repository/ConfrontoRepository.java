package it.simulatore.bollette.repository;

import it.simulatore.bollette.entity.Confronto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfrontoRepository extends JpaRepository<Confronto, Long> {
    List<Confronto> findAllByOrderByCreatedAtDesc();
}
