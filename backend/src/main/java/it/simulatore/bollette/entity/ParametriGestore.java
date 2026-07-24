package it.simulatore.bollette.entity;

import it.simulatore.bollette.enums.BaseQuantita;
import it.simulatore.bollette.enums.CategoriaVoce;
import it.simulatore.bollette.enums.LivelloTensione;
import it.simulatore.bollette.enums.OrigineParametro;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SORGENTE DATI "B": la bolletta del gestore, ovvero l'area configurabile.
 * <p>
 * Raccoglie i parametri nazionali ARERA e i corrispettivi commerciali del gestore. Il
 * metodo {@link #toVoci()} converte i parametri in righe di calcolo, ciascuna con la sua
 * {@link OrigineParametro}. Le quote energia per fascia NON stanno qui: sono specifiche
 * dell'offerta.
 */
@Entity
@Table(name = "parametri_gestore")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParametriGestore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nomeProfilo;

    private String descrizione;

    @Builder.Default
    private Boolean predefinito = false;

    private String nomeGestore;
    private LocalDate validoDal;
    private LocalDate validoAl;

    // ═══ PERDITE DI RETE ═══
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LivelloTensione livelloTensioneDefault = LivelloTensione.BT;

    @Column(precision = 8, scale = 6) private BigDecimal percentualePerdite;

    @Builder.Default
    private Boolean arrotondaPerdite = true;

    // ═══ TRASPORTO (nazionale) ═══
    @Column(precision = 14, scale = 8) private BigDecimal trasportoKwMese;
    @Column(precision = 14, scale = 8) private BigDecimal trasportoPodMese;
    @Column(precision = 14, scale = 8) private BigDecimal trasportoKwh;

    // ═══ ONERI DI SISTEMA (nazionale) ═══
    @Column(precision = 14, scale = 8) private BigDecimal asosQuotaFissa;
    @Column(precision = 14, scale = 8) private BigDecimal asosQuotaVariabile;
    @Column(precision = 14, scale = 8) private BigDecimal arimQuotaVariabile;

    // ═══ IMPOSTE (nazionale) ═══
    @Column(precision = 14, scale = 8) private BigDecimal accisaDomestico;
    @Column(precision = 14, scale = 8) private BigDecimal accisaNonDomestico;

    @Builder.Default
    private Boolean applicaSogliaEsenzione = false;

    @Column(precision = 12, scale = 2) private BigDecimal sogliaEsenzioneKwhAnno;
    @Column(precision = 12, scale = 2) private BigDecimal sogliaMassimaKwhAnno;

    // ═══ IVA ═══
    @Column(precision = 6, scale = 4) private BigDecimal ivaDomestico;
    @Column(precision = 6, scale = 4) private BigDecimal ivaNonDomestico;

    /** Se true usa l'aliquota della bolletta concorrente invece di derivarla dal tipo cliente. */
    @Builder.Default
    private Boolean usaAliquotaIvaBolletta = true;

    /** Se true le altre partite soggette entrano nell'imponibile IVA (comportamento fattura reale). */
    @Builder.Default
    private Boolean altrePartiteInImponibile = true;

    // ═══ DISPACCIAMENTO — base KWH_CON_PERDITE (nazionale) ═══
    @Column(precision = 14, scale = 8) private BigDecimal corrMercatoCapacita;
    @Column(precision = 14, scale = 8) private BigDecimal corrDisRtn;
    @Column(precision = 14, scale = 8) private BigDecimal corrInt;
    @Column(precision = 14, scale = 8) private BigDecimal corrMsd;
    @Column(precision = 14, scale = 8) private BigDecimal corrUesSicurezza;
    @Column(precision = 14, scale = 8) private BigDecimal corrSal;
    @Column(precision = 14, scale = 8) private BigDecimal corrSbilanciamento;
    @Column(precision = 14, scale = 8) private BigDecimal corrAggregazioneMisure;
    @Column(precision = 14, scale = 8) private BigDecimal dispbt;
    @Column(precision = 14, scale = 8) private BigDecimal corrGestioneCapacita;

    // ═══ CORRISPETTIVI COMMERCIALI DEL GESTORE ═══
    @Column(precision = 14, scale = 8) private BigDecimal commercializzazioneMese;
    @Column(precision = 14, scale = 8) private BigDecimal pcvVariabile;
    @Column(precision = 14, scale = 8) private BigDecimal spreadEnergia;

    @CreationTimestamp private LocalDateTime createdAt;
    @UpdateTimestamp private LocalDateTime updatedAt;

    /**
     * Converte i parametri in righe di calcolo, ciascuna con la sua {@link OrigineParametro}.
     * Non include le quote energia per fascia (specifiche dell'offerta) ne' le perdite di rete
     * (calcolate sui consumi). L'accisa e' inclusa solo quando la soglia di esenzione non e'
     * attiva: in tal caso e' una semplice voce KWH_NETTI.
     */
    public List<VoceCorrispettivo> toVoci() {
        List<VoceCorrispettivo> voci = new ArrayList<>();
        int[] ordine = {0};

        // --- MATERIA ENERGIA ---
        add(voci, ordine, "Commercializzazione e vendita", CategoriaVoce.MATERIA_ENERGIA,
                BaseQuantita.FISSO_MESE, OrigineParametro.GESTORE, commercializzazioneMese, "EUR/mese");
        add(voci, ordine, "DISPBT", CategoriaVoce.MATERIA_ENERGIA,
                BaseQuantita.FISSO_MESE, OrigineParametro.NAZIONALE, dispbt, "EUR");
        add(voci, ordine, "Corrispettivo mercato capacita", CategoriaVoce.MATERIA_ENERGIA,
                BaseQuantita.KWH_CON_PERDITE, OrigineParametro.NAZIONALE, corrMercatoCapacita, "EUR/kWh");
        add(voci, ordine, "DIS / RTN (art. 46)", CategoriaVoce.MATERIA_ENERGIA,
                BaseQuantita.KWH_CON_PERDITE, OrigineParametro.NAZIONALE, corrDisRtn, "EUR/kWh");
        add(voci, ordine, "INT (art. 73)", CategoriaVoce.MATERIA_ENERGIA,
                BaseQuantita.KWH_CON_PERDITE, OrigineParametro.NAZIONALE, corrInt, "EUR/kWh");
        add(voci, ordine, "MSD (art. 44)", CategoriaVoce.MATERIA_ENERGIA,
                BaseQuantita.KWH_CON_PERDITE, OrigineParametro.NAZIONALE, corrMsd, "EUR/kWh");
        add(voci, ordine, "UES / Sicurezza+reintegro (art. 45)", CategoriaVoce.MATERIA_ENERGIA,
                BaseQuantita.KWH_CON_PERDITE, OrigineParametro.NAZIONALE, corrUesSicurezza, "EUR/kWh");
        add(voci, ordine, "SAL (art. 48)", CategoriaVoce.MATERIA_ENERGIA,
                BaseQuantita.KWH_CON_PERDITE, OrigineParametro.NAZIONALE, corrSal, "EUR/kWh");
        add(voci, ordine, "Corrispettivo PCV variabile", CategoriaVoce.MATERIA_ENERGIA,
                BaseQuantita.KWH_CON_PERDITE, OrigineParametro.GESTORE, pcvVariabile, "EUR/kWh");
        add(voci, ordine, "Oneri sbilanciamento contatori non orari", CategoriaVoce.MATERIA_ENERGIA,
                BaseQuantita.KWH_CON_PERDITE, OrigineParametro.GESTORE, corrSbilanciamento, "EUR/kWh");
        add(voci, ordine, "Aggregazione misure (Del. 107/09)", CategoriaVoce.MATERIA_ENERGIA,
                BaseQuantita.FISSO_MESE, OrigineParametro.NAZIONALE, corrAggregazioneMisure, "EUR/mese");

        // --- TRASPORTO E GESTIONE CONTATORE ---
        add(voci, ordine, "Corrispettivo EUR/kW", CategoriaVoce.TRASPORTO,
                BaseQuantita.KW_MESE, OrigineParametro.NAZIONALE, trasportoKwMese, "EUR/kW");
        add(voci, ordine, "Corrispettivo EUR/pod/mese", CategoriaVoce.TRASPORTO,
                BaseQuantita.POD_MESE, OrigineParametro.NAZIONALE, trasportoPodMese, "EUR/pod/mese");
        add(voci, ordine, "Corrispettivo EUR/kWh scaglione 1", CategoriaVoce.TRASPORTO,
                BaseQuantita.KWH_NETTI, OrigineParametro.NAZIONALE, trasportoKwh, "EUR/kWh");

        // --- ONERI DI SISTEMA ---
        add(voci, ordine, "Asos quota fissa", CategoriaVoce.ONERI_SISTEMA,
                BaseQuantita.POD_MESE, OrigineParametro.NAZIONALE, asosQuotaFissa, "EUR/pod/mese");
        add(voci, ordine, "Arim quota variabile", CategoriaVoce.ONERI_SISTEMA,
                BaseQuantita.KWH_NETTI, OrigineParametro.NAZIONALE, arimQuotaVariabile, "EUR/kWh");
        add(voci, ordine, "Asos quota variabile", CategoriaVoce.ONERI_SISTEMA,
                BaseQuantita.KWH_NETTI, OrigineParametro.NAZIONALE, asosQuotaVariabile, "EUR/kWh");

        // --- IMPOSTE (solo se soglia esenzione non attiva) ---
        if (!Boolean.TRUE.equals(applicaSogliaEsenzione)) {
            add(voci, ordine, "Accisa", CategoriaVoce.IMPOSTE,
                    BaseQuantita.KWH_NETTI, OrigineParametro.NAZIONALE, accisaDomestico, "EUR/kWh");
        }

        return voci;
    }

    private void add(List<VoceCorrispettivo> voci, int[] ordine, String descr, CategoriaVoce cat,
                     BaseQuantita base, OrigineParametro origine, BigDecimal valore, String um) {
        if (valore == null) {
            return;
        }
        voci.add(VoceCorrispettivo.builder()
                .descrizione(descr)
                .categoria(cat)
                .base(base)
                .origine(origine)
                .corrispettivo(valore)
                .unitaMisura(um)
                .ordine(ordine[0]++)
                .attiva(true)
                .variabilePerMese(false)
                .build());
    }
}
