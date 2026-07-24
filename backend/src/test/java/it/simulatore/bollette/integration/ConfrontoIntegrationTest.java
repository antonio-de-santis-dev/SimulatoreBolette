package it.simulatore.bollette.integration;

import it.simulatore.bollette.dto.ConfrontoRequest;
import it.simulatore.bollette.dto.ConfrontoResponse;
import it.simulatore.bollette.entity.BollettaConcorrente;
import it.simulatore.bollette.entity.Offerta;
import it.simulatore.bollette.entity.ParametriGestore;
import it.simulatore.bollette.repository.BollettaConcorrenteRepository;
import it.simulatore.bollette.repository.OffertaRepository;
import it.simulatore.bollette.repository.ParametriGestoreRepository;
import it.simulatore.bollette.service.ConfrontoService;
import it.simulatore.bollette.service.ParametriGestoreService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test di integrazione: verifica migrazione Flyway + validate, seed dei due profili e della
 * bolletta di esempio, unicita del profilo predefinito e calcolo del risparmio.
 */
@SpringBootTest
class ConfrontoIntegrationTest {

    @Autowired private ConfrontoService confrontoService;
    @Autowired private ParametriGestoreService parametriService;
    @Autowired private BollettaConcorrenteRepository bollettaRepo;
    @Autowired private OffertaRepository offertaRepo;
    @Autowired private ParametriGestoreRepository parametriRepo;

    @Test
    void parametriGestore_unSoloPredefinito() {
        long predefiniti = parametriRepo.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getPredefinito())).count();
        assertThat(predefiniti).isEqualTo(1);

        // Imposta come predefinito un profilo diverso: deve restare uno solo
        ParametriGestore nonPredefinito = parametriRepo.findAll().stream()
                .filter(p -> !Boolean.TRUE.equals(p.getPredefinito()))
                .findFirst().orElseThrow();
        parametriService.setPredefinito(nonPredefinito.getId());

        long dopo = parametriRepo.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getPredefinito())).count();
        assertThat(dopo).isEqualTo(1);
        assertThat(parametriService.findPredefinito().getId()).isEqualTo(nonPredefinito.getId());
    }

    @Test
    void confronto_bollettaConcorrenteVsOffertaGestore_calcolaRisparmio() {
        BollettaConcorrente bolletta = bollettaRepo.findAll().stream()
                .filter(b -> "FuturEnergy Rinnovabile".equals(b.getNomeFornitore()))
                .findFirst().orElseThrow();
        Offerta offerta = offertaRepo.findAll().stream()
                .filter(o -> "Luce Trioraria Web".equals(o.getNomeOfferta()))
                .findFirst().orElseThrow();

        ConfrontoRequest req = new ConfrontoRequest();
        req.setBollettaConcorrenteId(bolletta.getId());
        req.setOffertaGestoreId(offerta.getId());
        req.setSalva(true);

        ConfrontoResponse r = confrontoService.confronta(req);

        assertThat(r.getConfrontoId()).isNotNull();
        assertThat(r.getCategorie()).hasSize(5);
        assertThat(r.getMesi()).hasSize(1);
        assertThat(r.getGestoreTotale()).isNotNull();
        assertThat(r.getConcorrenteTotale()).isEqualByComparingTo("118.34");
        // Risparmio = totale concorrente - totale gestore
        BigDecimal atteso = r.getConcorrenteTotale().subtract(r.getGestoreTotale())
                .setScale(2, java.math.RoundingMode.HALF_UP);
        assertThat(r.getRisparmioBimestrale()).isEqualByComparingTo(atteso);
        assertThat(r.getRisparmioAnnuale()).isEqualByComparingTo(atteso.multiply(new BigDecimal("6")));
        // L'aliquota IVA proviene dalla bolletta (10%), non dal tipo cliente
        assertThat(r.getAliquotaIvaApplicata()).isEqualByComparingTo("0.10");
    }

    @Test
    void anteprima_profilo_produceBolletta() {
        Long id = parametriService.findPredefinito().getId();
        var anteprima = parametriService.anteprima(id);
        assertThat(anteprima.totale()).isGreaterThan(BigDecimal.ZERO);
        assertThat(anteprima.imponibile()).isGreaterThan(BigDecimal.ZERO);
    }
}
