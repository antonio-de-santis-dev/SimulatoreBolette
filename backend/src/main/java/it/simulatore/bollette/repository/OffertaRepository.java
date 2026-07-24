package it.simulatore.bollette.repository;

import it.simulatore.bollette.entity.Offerta;
import it.simulatore.bollette.enums.TipoOfferta;
import it.simulatore.bollette.enums.TipoTariffa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OffertaRepository extends JpaRepository<Offerta, Long> {
    List<Offerta> findByAttivaTrue();
    List<Offerta> findByTipoOffertaAndAttivaTrue(TipoOfferta tipoOfferta);
    List<Offerta> findByTipoTariffaAndAttivaTrue(TipoTariffa tipoTariffa);
}
