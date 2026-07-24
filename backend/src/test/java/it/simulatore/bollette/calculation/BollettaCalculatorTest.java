package it.simulatore.bollette.calculation;

import it.simulatore.bollette.entity.BollettaConcorrente;
import it.simulatore.bollette.enums.BaseQuantita;
import it.simulatore.bollette.enums.CategoriaVoce;
import it.simulatore.bollette.enums.OrigineParametro;
import it.simulatore.bollette.entity.VoceCorrispettivo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Casi di verita' ricavati dal foglio SIMULATORE_BIMESTRALE_LUCE.xlsx e dalla fattura reale
 * FuturEnergy. Tolleranza +/- 0,01 EUR. Ogni mese e' modellato con le sue voci esatte
 * (i corrispettivi cambiano tra i due mesi del bimestre, come nel foglio).
 */
class BollettaCalculatorTest {

    private static final BigDecimal TOLL = new BigDecimal("0.01");
    private static final BigDecimal COEFF_EXCEL = new BigDecimal("0.10");
    private static final BigDecimal IVA_10 = new BigDecimal("0.10");
    private static final BigDecimal POTENZA_3 = new BigDecimal("3");

    private final MotoreCalcolo motore = new MotoreCalcolo();

    // ─────────────────────────────────────────────────────────────────────────────
    // Helper costruzione voci
    // ─────────────────────────────────────────────────────────────────────────────
    private static VoceCorrispettivo v(CategoriaVoce cat, BaseQuantita base, String c) {
        return VoceCorrispettivo.builder()
                .descrizione(cat + "/" + base)
                .categoria(cat).base(base).origine(OrigineParametro.NAZIONALE)
                .corrispettivo(new BigDecimal(c)).attiva(true).variabilePerMese(false)
                .build();
    }

    /** Mese NOVEMBRE (foglio NOVEMBRE-DICEMBRE, primo mese): commercializzazione 20. */
    private List<VoceCorrispettivo> vociNovembre() {
        List<VoceCorrispettivo> l = new ArrayList<>();
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.FISSO_MESE, "20"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.FISSO_MESE, "0.109858"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_F1, "0.13378"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_F2, "0.13663"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_F3, "0.11527"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.PERDITE_F1, "0.13378"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.PERDITE_F2, "0.13663"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.PERDITE_F3, "0.11527"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_CON_PERDITE, "0.008995"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_CON_PERDITE, "0.000558"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_CON_PERDITE, "0.000856"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_CON_PERDITE, "0.001953"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_CON_PERDITE, "0.004339"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_CON_PERDITE, "0.0044"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_CON_PERDITE, "0.004"));
        aggiungiTrasportoOneriImposte(l, "7.6302", "0.008828", "0.029809");
        return l;
    }

    /** Mese DICEMBRE / GENNAIO (secondo mese NOV-DIC e DIC-GEN): commercializzazione 8.95, asos fissa 0. */
    private List<VoceCorrispettivo> vociDicembre() {
        List<VoceCorrispettivo> l = new ArrayList<>();
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.FISSO_MESE, "8.95"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.FISSO_MESE, "0.109858"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_F1, "0.16052"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_F2, "0.15381"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_F3, "0.13074"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.PERDITE_F1, "0.16052"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.PERDITE_F2, "0.15381"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.PERDITE_F3, "0.13074"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_CON_PERDITE, "0.001953")); // MSD
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_CON_PERDITE, "0.002048")); // UES
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_CON_PERDITE, "0.000558")); // RTN
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_CON_PERDITE, "0.000856")); // INT
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_CON_PERDITE, "0.00052"));  // SAL
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.FISSO_MESE, "0.007"));         // aggregazione
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_CON_PERDITE, "0.01"));     // sbilanciamento
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_CON_PERDITE, "0.005"));    // PCV
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_CON_PERDITE, "0.00093"));  // capacita mercato
        aggiungiTrasportoOneriImposte(l, "0", "0.002829", "0.02936");
        return l;
    }

    private void aggiungiTrasportoOneriImposte(List<VoceCorrispettivo> l, String asosFissa,
                                               String arim, String asosVar) {
        // Trasporto
        l.add(v(CategoriaVoce.TRASPORTO, BaseQuantita.KW_MESE, "1.866567"));
        l.add(v(CategoriaVoce.TRASPORTO, BaseQuantita.POD_MESE, "1.84"));
        l.add(v(CategoriaVoce.TRASPORTO, BaseQuantita.KWH_NETTI, "0.0122"));
        // Oneri
        l.add(v(CategoriaVoce.ONERI_SISTEMA, BaseQuantita.POD_MESE, asosFissa));
        l.add(v(CategoriaVoce.ONERI_SISTEMA, BaseQuantita.KWH_NETTI, arim));
        l.add(v(CategoriaVoce.ONERI_SISTEMA, BaseQuantita.KWH_NETTI, asosVar));
        // Imposte
        l.add(v(CategoriaVoce.IMPOSTE, BaseQuantita.KWH_NETTI, "0.0227"));
    }

    private RisultatoMese mese(String f1, String f2, String f3, List<VoceCorrispettivo> voci) {
        return motore.calcolaMese(new BigDecimal(f1), new BigDecimal(f2), new BigDecimal(f3),
                POTENZA_3, voci, COEFF_EXCEL, true);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // CASI DI VERITA' BIMESTRALI
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    void bimestreNovembreDicembre_totaleFattura_107_974746() {
        RisultatoMese nov = mese("24", "37", "33", vociNovembre());
        RisultatoMese dic = mese("24", "37", "33", vociDicembre());
        RisultatoBolletta b = motore.aggregaBolletta(List.of(nov, dic), null, null, IVA_10);

        assertThat(b.totale(CategoriaVoce.MATERIA_ENERGIA)).isCloseTo(new BigDecimal("62.430414"), within("0.0001"));
        assertThat(b.totale(CategoriaVoce.TRASPORTO).add(b.totale(CategoriaVoce.ONERI_SISTEMA)))
                .isCloseTo(new BigDecimal("31.460846"), within("0.0001"));
        assertThat(b.totale(CategoriaVoce.IMPOSTE)).isCloseTo(new BigDecimal("4.2676"), within("0.0001"));
        assertThat(b.imponibile()).isCloseTo(new BigDecimal("98.15886"), TOLL_OFFSET());
        assertThat(b.iva()).isCloseTo(new BigDecimal("9.815886"), TOLL_OFFSET());
        assertThat(b.totale()).isCloseTo(new BigDecimal("107.974746"), TOLL_OFFSET());
    }

    @Test
    void bimestreDicembreGennaio_primoMeseAZero_44_276529() {
        // Primo mese completamente a zero (nel foglio tutte le quantita sono 0)
        RisultatoMese primo = mese("0", "0", "0", new ArrayList<>());
        RisultatoMese gennaio = mese("24", "37", "33", vociDicembre());
        RisultatoBolletta b = motore.aggregaBolletta(List.of(primo, gennaio), null, null, IVA_10);

        assertThat(b.totale(CategoriaVoce.MATERIA_ENERGIA)).isCloseTo(new BigDecimal("26.505323"), within("0.0001"));
        assertThat(b.totale()).isCloseTo(new BigDecimal("44.276529"), TOLL_OFFSET());
    }

    @Test
    void bimestreGennaioFebbraio_conAltrePartiteNegative_59_112642() {
        RisultatoMese febbraio = mese("7", "2", "2", vociFebbraio());
        RisultatoMese marzo = mese("37", "3", "5", vociMarzo());
        // Altra partita non soggetta a IVA: -11,40 (sommata dopo l'IVA)
        RisultatoBolletta b = motore.aggregaBolletta(List.of(febbraio, marzo),
                null, new BigDecimal("-11.40"), IVA_10);

        assertThat(b.totale(CategoriaVoce.MATERIA_ENERGIA)).isCloseTo(new BigDecimal("30.134688"), within("0.0001"));
        assertThat(b.totale(CategoriaVoce.TRASPORTO).add(b.totale(CategoriaVoce.ONERI_SISTEMA)))
                .isCloseTo(new BigDecimal("32.696514"), within("0.0001"));
        assertThat(b.imponibile()).isCloseTo(new BigDecimal("64.102402"), TOLL_OFFSET());
        assertThat(b.totale()).isCloseTo(new BigDecimal("59.1126422"), TOLL_OFFSET());
    }

    private List<VoceCorrispettivo> vociFebbraio() {
        List<VoceCorrispettivo> l = new ArrayList<>();
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.FISSO_MESE, "10"));
        // DISPBT azzerato nel foglio FEBBRAIO
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_F1, "0.16764"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_F2, "0.16895"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_F3, "0.14991"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.PERDITE_F1, "0.16764"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.PERDITE_F2, "0.16895"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.PERDITE_F3, "0.14991"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_CON_PERDITE, "0.001953"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_CON_PERDITE, "0.002048"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_CON_PERDITE, "0.000558"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_CON_PERDITE, "0.000856"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_CON_PERDITE, "0.00052"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_CON_PERDITE, "0.01"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_CON_PERDITE, "0.005"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_CON_PERDITE, "0.01093"));
        aggiungiTrasportoOneriImposte(l, "7.6302", "0.008828", "0.029809");
        return l;
    }

    private List<VoceCorrispettivo> vociMarzo() {
        List<VoceCorrispettivo> l = new ArrayList<>();
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.FISSO_MESE, "10"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.FISSO_MESE, "0.109858"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_F1, "0.13168"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_F2, "0.14486"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_F3, "0.12165"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.PERDITE_F1, "0.13168"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.PERDITE_F2, "0.14486"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.PERDITE_F3, "0.12165"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_CON_PERDITE, "0.001953"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_CON_PERDITE, "0.002048"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_CON_PERDITE, "0.000558"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_CON_PERDITE, "0.000856"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_CON_PERDITE, "0.00052"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.FISSO_MESE, "0.007"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_CON_PERDITE, "0.01"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_CON_PERDITE, "0.005"));
        l.add(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_CON_PERDITE, "0.00093"));
        aggiungiTrasportoOneriImposte(l, "7.6302", "0.002829", "0.02936");
        return l;
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // FATTURA REALE FuturEnergy
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    void fatturaFuturEnergy_impostaErariale_0_39() {
        // Accisa senza soglia: 0,0227 x 17 kWh
        RisultatoMese m = motore.calcolaMese(new BigDecimal("6"), new BigDecimal("4"), new BigDecimal("7"),
                POTENZA_3, List.of(v(CategoriaVoce.IMPOSTE, BaseQuantita.KWH_NETTI, "0.0227")),
                COEFF_EXCEL, true);
        assertThat(m.getTotale(CategoriaVoce.IMPOSTE).setScale(2, java.math.RoundingMode.HALF_UP))
                .isEqualByComparingTo("0.39");
    }

    @Test
    void fatturaFuturEnergy_imponibileIncludeAltrePartite_107_58() {
        // 15,31 + 7,65 + 8,29 + 75,94 + 0,39 = 107,58
        BigDecimal imponibile = new BigDecimal("15.31")
                .add(new BigDecimal("7.65"))
                .add(new BigDecimal("8.29"))
                .add(new BigDecimal("75.94"))
                .add(new BigDecimal("0.39"));
        assertThat(imponibile).isEqualByComparingTo("107.58");
    }

    @Test
    void fatturaFuturEnergy_iva10_10_76() {
        BigDecimal iva = new BigDecimal("107.58").multiply(IVA_10)
                .setScale(2, java.math.RoundingMode.HALF_UP);
        assertThat(iva).isEqualByComparingTo("10.76");
    }

    @Test
    void fatturaFuturEnergy_quadratura_componentiSommanoAlTotale() {
        BollettaConcorrente b = BollettaConcorrente.builder()
                .nomeFornitore("FuturEnergy Rinnovabile")
                .fatturatoMateriaEnergia(new BigDecimal("15.31"))
                .fatturatoTrasporto(new BigDecimal("7.65"))
                .fatturatoOneriSistema(new BigDecimal("8.29"))
                .fatturatoAltrePartite(new BigDecimal("75.94"))
                .fatturatoImposte(new BigDecimal("0.39"))
                .fatturatoImponibile(new BigDecimal("107.58"))
                .fatturatoIva(new BigDecimal("10.76"))
                .fatturatoTotale(new BigDecimal("118.34"))
                .build();
        assertThat(b.quadraturaImportiValida()).isTrue();
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // UNIT MIRATI
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    void perditeDiRete_24kwh_coeff10_arrotondaA2() {
        ConsumiFasce c = new ConsumiFasce();
        c.setF1(new BigDecimal("24"));
        c.calcolaPerdite(COEFF_EXCEL, true);
        assertThat(c.getPerditeF1()).isEqualByComparingTo("2");
    }

    @Test
    void perditeDiRete_37kwh_coeff10_arrotondaA4() {
        ConsumiFasce c = new ConsumiFasce();
        c.setF2(new BigDecimal("37"));
        c.calcolaPerdite(COEFF_EXCEL, true);
        assertThat(c.getPerditeF2()).isEqualByComparingTo("4");
    }

    @Test
    void perditeDiRete_coefficienteBT_10_40() {
        assertThat(it.simulatore.bollette.enums.LivelloTensione.BT.getCoefficientePerdite())
                .isEqualByComparingTo("0.1040");
    }

    @Test
    void kwhConPerdite_94netti_da103() {
        ConsumiFasce c = new ConsumiFasce();
        c.setF1(new BigDecimal("24"));
        c.setF2(new BigDecimal("37"));
        c.setF3(new BigDecimal("33"));
        c.calcolaPerdite(COEFF_EXCEL, true);
        assertThat(c.getTotaleNetto()).isEqualByComparingTo("94");
        assertThat(c.getTotaleConPerdite()).isEqualByComparingTo("103");
    }

    @Test
    void asosQuotaFissa_applicataAncheAiResidenti() {
        // Nel motore a righe la quota fissa Asos e' una voce POD_MESE applicata sempre,
        // indipendentemente dal tipo cliente.
        RisultatoMese m = motore.calcolaMese(new BigDecimal("94"), BigDecimal.ZERO, BigDecimal.ZERO,
                POTENZA_3, List.of(v(CategoriaVoce.ONERI_SISTEMA, BaseQuantita.POD_MESE, "7.6302")),
                COEFF_EXCEL, true);
        assertThat(m.getTotale(CategoriaVoce.ONERI_SISTEMA)).isEqualByComparingTo("7.630200");
    }

    @Test
    void accisa_senzaSoglia_applicataSempre() {
        RisultatoMese m = motore.calcolaMese(new BigDecimal("6"), new BigDecimal("4"), new BigDecimal("7"),
                POTENZA_3, List.of(v(CategoriaVoce.IMPOSTE, BaseQuantita.KWH_NETTI, "0.0227")),
                COEFF_EXCEL, true);
        assertThat(m.getTotale(CategoriaVoce.IMPOSTE)).isEqualByComparingTo("0.385900");
    }

    @Test
    void trasportoKwMese_nonDivisoPer12() {
        RisultatoMese m = motore.calcolaMese(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("3"),
                List.of(v(CategoriaVoce.TRASPORTO, BaseQuantita.KW_MESE, "1.866567")),
                COEFF_EXCEL, true);
        // 1,866567 x 3 = 5,599701 (NON diviso per 12)
        assertThat(m.getTotale(CategoriaVoce.TRASPORTO)).isEqualByComparingTo("5.599701");
    }

    @Test
    void altrePartite_inclusoNellImponibileIva() {
        RisultatoMese m = motore.calcolaMese(new BigDecimal("10"), BigDecimal.ZERO, BigDecimal.ZERO,
                POTENZA_3, List.of(v(CategoriaVoce.MATERIA_ENERGIA, BaseQuantita.KWH_F1, "1")),
                COEFF_EXCEL, true);
        // imponibile = 10 (materia) + 5 (altra partita soggetta)
        RisultatoBolletta b = motore.aggregaBolletta(List.of(m), new BigDecimal("5"), null, IVA_10);
        assertThat(b.imponibile()).isEqualByComparingTo("15.000000");
        assertThat(b.iva()).isEqualByComparingTo("1.500000");
    }

    private static org.assertj.core.data.Offset<BigDecimal> within(String v) {
        return org.assertj.core.data.Offset.offset(new BigDecimal(v));
    }

    private static org.assertj.core.data.Offset<BigDecimal> TOLL_OFFSET() {
        return org.assertj.core.data.Offset.offset(TOLL);
    }
}
