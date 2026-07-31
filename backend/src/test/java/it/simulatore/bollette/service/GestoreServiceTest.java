package it.simulatore.bollette.service;

import it.simulatore.bollette.dto.GestoreDettaglioRequest;
import it.simulatore.bollette.dto.GestoreDettaglioResponse;
import it.simulatore.bollette.dto.OffertaRequest;
import it.simulatore.bollette.dto.OffertaResponse;
import it.simulatore.bollette.entity.ParametriGestore;
import it.simulatore.bollette.enums.TipoOfferta;
import it.simulatore.bollette.enums.TipoTariffa;
import it.simulatore.bollette.repository.OffertaRepository;
import it.simulatore.bollette.repository.ParametriGestoreRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test del service unificato "Il mio gestore" (prompt 1), incluso il controllo
 * di retrocompatibilita': i vecchi endpoint delegano al nuovo service.
 */
@SpringBootTest
@Transactional
class GestoreServiceTest {

    @Autowired private GestoreService gestoreService;
    @Autowired private OffertaService offertaService; // service deprecato che delega
    @Autowired private ParametriGestoreRepository gestoreRepository;
    @Autowired private OffertaRepository offertaRepository;

    private GestoreDettaglioRequest reqGestore(String nome) {
        GestoreDettaglioRequest r = new GestoreDettaglioRequest();
        r.setNomeProfilo(nome);
        r.setNomeGestore(nome);
        r.setCommercializzazioneMese(new BigDecimal("8.95"));
        r.setPcvVariabile(new BigDecimal("0.0050"));
        r.setSpreadEnergia(new BigDecimal("0.010000"));
        return r;
    }

    private OffertaRequest reqOfferta(String fornitore, String nome) {
        OffertaRequest o = new OffertaRequest();
        o.setNomeFornitore(fornitore);
        o.setNomeOfferta(nome);
        o.setTipoOfferta(TipoOfferta.PREZZO_FISSO);
        o.setTipoTariffa(TipoTariffa.MONORARIA);
        o.setPrezzoFissoF0(new BigDecimal("0.2500"));
        o.setPcvAnnuo(new BigDecimal("100.00"));
        return o;
    }

    @Test
    void dettaglio_includeOfferteDelGestore() {
        GestoreDettaglioResponse g = gestoreService.crea(reqGestore("Test A"));
        gestoreService.aggiungiOfferta(g.getId(), reqOfferta("Forn 1", "Off 1"));
        gestoreService.aggiungiOfferta(g.getId(), reqOfferta("Forn 2", "Off 2"));

        GestoreDettaglioResponse d = gestoreService.dettaglio(g.getId());

        assertThat(d.getOfferte()).hasSize(2);
        assertThat(d.getNumeroOfferte()).isEqualTo(2);
        assertThat(d.getOfferte()).allSatisfy(o -> assertThat(o.getGestoreId()).isEqualTo(g.getId()));
    }

    @Test
    void aggiornamentoParziale_nonSovrascriveCampiNull() {
        GestoreDettaglioResponse g = gestoreService.crea(reqGestore("Nome originale"));

        GestoreDettaglioRequest patch = new GestoreDettaglioRequest();
        patch.setNomeGestore("Nome modificato"); // tutti gli altri campi null

        GestoreDettaglioResponse agg = gestoreService.aggiorna(g.getId(), patch);

        assertThat(agg.getNomeGestore()).isEqualTo("Nome modificato");
        assertThat(agg.getCommercializzazioneMese()).isEqualByComparingTo("8.95"); // invariato
        assertThat(agg.getNomeProfilo()).isEqualTo("Nome originale");             // invariato
    }

    @Test
    void aggiungiOfferta_associataAlGestore() {
        GestoreDettaglioResponse g = gestoreService.crea(reqGestore("Test B"));

        OffertaResponse o = gestoreService.aggiungiOfferta(g.getId(), reqOfferta("Forn X", "Off X"));

        assertThat(o.getGestoreId()).isEqualTo(g.getId());
        assertThat(offertaRepository.findById(o.getId()))
            .isPresent().get()
            .satisfies(e -> assertThat(e.getGestore().getId()).isEqualTo(g.getId()));
    }

    @Test
    void eliminaOfferta_nonEliminaAltreOfferte() {
        GestoreDettaglioResponse g = gestoreService.crea(reqGestore("Test C"));
        OffertaResponse o1 = gestoreService.aggiungiOfferta(g.getId(), reqOfferta("F1", "O1"));
        OffertaResponse o2 = gestoreService.aggiungiOfferta(g.getId(), reqOfferta("F2", "O2"));

        gestoreService.eliminaOfferta(g.getId(), o1.getId());

        assertThat(offertaRepository.findById(o1.getId())).isEmpty();
        assertThat(offertaRepository.findById(o2.getId())).isPresent();
        assertThat(gestoreService.dettaglio(g.getId()).getOfferte()).hasSize(1);
    }

    @Test
    void duplica_copiaConfigMaNonRendePredefinito() {
        GestoreDettaglioRequest req = reqGestore("Test predefinito");
        req.setPredefinito(true);
        GestoreDettaglioResponse orig = gestoreService.crea(req);

        GestoreDettaglioResponse copia = gestoreService.duplica(orig.getId());

        assertThat(copia.getCommercializzazioneMese()).isEqualByComparingTo(orig.getCommercializzazioneMese());
        assertThat(copia.getSpreadEnergia()).isEqualByComparingTo(orig.getSpreadEnergia());
        assertThat(copia.getPredefinito()).isFalse();
        assertThat(copia.getId()).isNotEqualTo(orig.getId());
        assertThat(gestoreService.dettaglio(orig.getId()).getPredefinito()).isTrue();
    }

    @Test
    void impostaPredefinito_azzeraGliAltri() {
        GestoreDettaglioResponse g1 = gestoreService.crea(reqGestore("Test G1"));
        GestoreDettaglioResponse g2 = gestoreService.crea(reqGestore("Test G2"));

        gestoreService.impostaPredefinito(g2.getId());

        assertThat(gestoreService.dettaglio(g2.getId()).getPredefinito()).isTrue();
        assertThat(gestoreService.dettaglio(g1.getId()).getPredefinito()).isFalse();

        long predefiniti = gestoreRepository.findAll().stream()
            .filter(x -> Boolean.TRUE.equals(x.getPredefinito())).count();
        assertThat(predefiniti).isEqualTo(1); // esattamente uno in tutto il repository
    }

    @Test
    void endpointVecchi_delaganoAlNuovoService() {
        ParametriGestore predefinito = gestoreRepository.findByPredefinitoTrue().orElseThrow();

        // creazione tramite il vecchio OffertaService (endpoint /api/offerte)
        OffertaResponse creata = offertaService.creaOfferta(reqOfferta("Legacy", "Offerta legacy"));

        assertThat(creata.getGestoreId()).isEqualTo(predefinito.getId());
        List<OffertaResponse> offerteGestore = gestoreService.dettaglio(predefinito.getId()).getOfferte();
        assertThat(offerteGestore).anySatisfy(o -> assertThat(o.getId()).isEqualTo(creata.getId()));
    }
}
