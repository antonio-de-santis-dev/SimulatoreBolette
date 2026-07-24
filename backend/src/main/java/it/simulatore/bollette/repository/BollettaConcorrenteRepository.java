package it.simulatore.bollette.repository;

import it.simulatore.bollette.entity.BollettaConcorrente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BollettaConcorrenteRepository extends JpaRepository<BollettaConcorrente, Long> {
}
