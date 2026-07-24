package it.simulatore.bollette.repository;

import it.simulatore.bollette.entity.ParametroARERA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParametroARERARepository extends JpaRepository<ParametroARERA, Long> {

    @Query("SELECT p FROM ParametroARERA p WHERE p.nomeParametro = :nome " +
           "AND p.validoDal <= :data AND (p.validoAl IS NULL OR p.validoAl >= :data)")
    Optional<ParametroARERA> findValidoPerData(@Param("nome") String nome, @Param("data") LocalDate data);

    List<ParametroARERA> findByNomeParametro(String nomeParametro);
}
