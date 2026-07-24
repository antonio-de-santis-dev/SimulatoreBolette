package it.simulatore.bollette.calculation;

import it.simulatore.bollette.entity.ParametriGestore;
import it.simulatore.bollette.entity.ScaglioneConsumo;
import it.simulatore.bollette.entity.VoceCorrispettivo;
import it.simulatore.bollette.enums.AttendibilitaStima;
import it.simulatore.bollette.enums.BaseQuantita;
import it.simulatore.bollette.enums.CategoriaVoce;
import it.simulatore.bollette.enums.TipoCliente;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test di conformita normativa (comportamento predefinito) e di fragilita del risparmio.
 */
class ConformitaNormativaTest {

    private final MotoreCalcolo motore = new MotoreCalcolo();

    private ParametriGestore parametriNormativi() {
        return ParametriGestore.builder()
                .nomeProfilo("test")
                .applicaEsenzioneAccisaResidenti(true).applicaScaglioni(true)
                .arrotondaPerdite(false).quotaFissaSoloNonResidenti(true)
                .quotaPotenzaSoloNonDomestici(true)
                .accisaDomestico(new BigDecimal("0.0227")).accisaNonDomestico(new BigDecimal("0.0227"))
                .sogliaEsenzioneKwhMese(new BigDecimal("150")).potenzaMaxEsenzioneKw(new BigDecimal("3"))
                .asosQuotaFissaAnnua(new BigDecimal("91.5624")).arimQuotaFissaAnnua(new BigDecimal("3.8568"))
                .trasportoQuotaFissaAnnua(new BigDecimal("22.08"))
                .trasportoQuotaPotenzaAnnua(new BigDecimal("22.398804"))
                .build();
    }

    private ConsumiFasce consumi(String f1, String f2, String f3) {
        ConsumiFasce c = new ConsumiFasce();
        c.setF1(new BigDecimal(f1));
        c.setF2(new BigDecimal(f2));
        c.setF3(new BigDecimal(f3));
        return c;
    }

    // ── ACCISA ─────────────────────────────────────────────────────────────────
    @Test
    void accisa_residente3kw_esenteFino150KwhMese() {
        BigDecimal a = motore.calcolaAccisa(consumi("50", "30", "20"), // 100 kWh < 150
                TipoCliente.DOMESTICO_RESIDENTE, new BigDecimal("3"), parametriNormativi(), 30);
        assertThat(a).isEqualByComparingTo("0");
    }

    @Test
    void accisa_nonResidente_dalPrimoKwh() {
        BigDecimal a = motore.calcolaAccisa(consumi("50", "30", "20"), // 100 kWh
                TipoCliente.DOMESTICO_NON_RESIDENTE, new BigDecimal("3"), parametriNormativi(), 30);
        assertThat(a).isEqualByComparingTo("2.2700"); // 100 x 0,0227
    }

    @Test
    void accisa_fatturaFuturEnergy_17kwh_0_39() {
        BigDecimal a = motore.calcolaAccisa(consumi("6", "4", "7"),
                TipoCliente.DOMESTICO_NON_RESIDENTE, new BigDecimal("3"), parametriNormativi(), 31);
        assertThat(a.setScale(2, java.math.RoundingMode.HALF_UP)).isEqualByComparingTo("0.39");
    }

    // ── QUOTA FISSA ONERI ────────────────────────────────────────────────────────
    @Test
    void quotaFissaOneri_nonApplicataAiResidenti() {
        List<VoceCorrispettivo> voci = parametriNormativi().toVoci(TipoCliente.DOMESTICO_RESIDENTE);
        boolean haQuotaFissaOneri = voci.stream().anyMatch(v ->
                v.getCategoria() == CategoriaVoce.ONERI_SISTEMA && v.getBase() == BaseQuantita.POD_MESE);
        assertThat(haQuotaFissaOneri).isFalse();
    }

    @Test
    void quotaFissaOneri_applicataAiNonResidenti() {
        List<VoceCorrispettivo> voci = parametriNormativi().toVoci(TipoCliente.DOMESTICO_NON_RESIDENTE);
        boolean haQuotaFissaOneri = voci.stream().anyMatch(v ->
                v.getCategoria() == CategoriaVoce.ONERI_SISTEMA && v.getBase() == BaseQuantita.POD_MESE);
        assertThat(haQuotaFissaOneri).isTrue();
    }

    // ── QUOTA POTENZA ────────────────────────────────────────────────────────────
    @Test
    void quotaPotenza_maiApplicataAiDomestici() {
        List<VoceCorrispettivo> dom = parametriNormativi().toVoci(TipoCliente.DOMESTICO_RESIDENTE);
        assertThat(dom.stream().anyMatch(v -> v.getBase() == BaseQuantita.KW_MESE)).isFalse();

        List<VoceCorrispettivo> nonDom = parametriNormativi().toVoci(TipoCliente.ALTRI_USI_BT);
        assertThat(nonDom.stream().anyMatch(v -> v.getBase() == BaseQuantita.KW_MESE)).isTrue();
    }

    // ── SCAGLIONI ─────────────────────────────────────────────────────────────────
    @Test
    void scaglioni_ripartizioneCorretta() {
        ScaglioneConsumo primo = ScaglioneConsumo.builder()
                .limiteInferiore(BigDecimal.ZERO).limiteSuperiore(new BigDecimal("1800")).build();
        ScaglioneConsumo secondo = ScaglioneConsumo.builder()
                .limiteInferiore(new BigDecimal("1800")).limiteSuperiore(null).build();
        BigDecimal consumo = new BigDecimal("2000");
        assertThat(motore.quantitaNelloScaglione(consumo, primo)).isEqualByComparingTo("1800");
        assertThat(motore.quantitaNelloScaglione(consumo, secondo)).isEqualByComparingTo("200");
    }

    // ── PERDITE ───────────────────────────────────────────────────────────────────
    @Test
    void perdite_senzaArrotondamento_scale6() {
        ConsumiFasce c = consumi("24", "0", "0");
        c.calcolaPerdite(new BigDecimal("0.1040"), false);
        // 24 x 0,1040 = 2,496000 (nessun arrotondamento a intero)
        assertThat(c.getPerditeF1()).isEqualByComparingTo("2.496000");
    }

    @Test
    void perdite_conArrotondamento_replicaExcel() {
        ConsumiFasce c = consumi("24", "0", "0");
        c.calcolaPerdite(new BigDecimal("0.10"), true);
        assertThat(c.getPerditeF1()).isEqualByComparingTo("2");
    }

    @Test
    void perdite_nonApplicateATrasportoOneriAccisa() {
        // Il trasporto (KWH_NETTI/scaglione) e l'accisa (KWH_NETTI) usano i soli kWh netti,
        // non i kWh con perdite.
        ConsumiFasce c = consumi("24", "37", "33"); // 94 netti
        c.calcolaPerdite(new BigDecimal("0.1040"), false);
        assertThat(motore.risolviQuantita(BaseQuantita.KWH_NETTI, c, BigDecimal.ONE))
                .isEqualByComparingTo("94");
        assertThat(motore.risolviQuantita(BaseQuantita.KWH_CON_PERDITE, c, BigDecimal.ONE))
                .isGreaterThan(new BigDecimal("94"));
    }

    // ── IVA ALIQUOTE MISTE ──────────────────────────────────────────────────────────
    @Test
    void iva_aliquoteMiste_totalizzatePerAliquota() {
        Map<BigDecimal, BigDecimal> imponibile = new TreeMap<>();
        imponibile.put(new BigDecimal("0.10"), new BigDecimal("100.00")); // domestico
        imponibile.put(new BigDecimal("0.22"), new BigDecimal("50.00"));  // non domestico
        Map<BigDecimal, BigDecimal> iva = CalcoloIva.perAliquota(imponibile);
        assertThat(iva.get(new BigDecimal("0.10"))).isEqualByComparingTo("10.0000");
        assertThat(iva.get(new BigDecimal("0.22"))).isEqualByComparingTo("11.0000");
        assertThat(CalcoloIva.totale(iva)).isEqualByComparingTo("21.0000");
    }

    // ── FRAGILITA DEL RISPARMIO ─────────────────────────────────────────────────────
    @Test
    void risparmioAnnuale_meseAZero_attendibilitaBassa() {
        AnalisiRisparmio.Stima s = AnalisiRisparmio.calcola(new BigDecimal("50.00"),
                List.of(BigDecimal.ZERO, new BigDecimal("94")), new BigDecimal("94"), null);
        assertThat(s.attendibilita()).isEqualTo(AttendibilitaStima.BASSA);
    }

    @Test
    void risparmioAnnuale_consumiSquilibrati_attendibilitaMedia() {
        // 100 vs 130: scarto 30/130 = 23% → MEDIA
        AnalisiRisparmio.Stima s = AnalisiRisparmio.calcola(new BigDecimal("20.00"),
                List.of(new BigDecimal("100"), new BigDecimal("130")), new BigDecimal("230"), null);
        assertThat(s.attendibilita()).isEqualTo(AttendibilitaStima.MEDIA);
    }

    @Test
    void risparmioAnnuale_conConsumoAnnuoNoto_usaQuelloNonEstrapolazione() {
        // Consumo annuo noto 600, bimestre 200 → fattore 3, non x6
        AnalisiRisparmio.Stima s = AnalisiRisparmio.calcola(new BigDecimal("30.00"),
                List.of(new BigDecimal("100"), new BigDecimal("100")), new BigDecimal("200"),
                new BigDecimal("600"));
        assertThat(s.attendibilita()).isEqualTo(AttendibilitaStima.ALTA);
        assertThat(s.risparmioAnnualeStimato()).isEqualByComparingTo("90.00"); // 30 x 3, non x6
    }
}
