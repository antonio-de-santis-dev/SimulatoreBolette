package it.simulatore.bollette.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import it.simulatore.bollette.enums.BaseQuantita;
import it.simulatore.bollette.enums.CategoriaVoce;
import it.simulatore.bollette.enums.LivelloTensione;
import it.simulatore.bollette.enums.OrigineParametro;
import it.simulatore.bollette.enums.TipoCliente;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SORGENTE DATI "B": la bolletta del gestore (area configurabile).
 * <p>
 * Implementa la normativa ARERA come comportamento predefinito, con flag booleani che ne
 * disattivano le regole per riprodurre il comportamento (semplificato ed errato) del foglio
 * Excel — necessario per i test di regressione. Ogni flag e' documentato nel README.
 * <p>
 * I corrispettivi di trasporto e oneri sono memorizzati su base <b>annua</b> e divisi per 12
 * nel calcolo: cosi' l'aggiornamento trimestrale ARERA si fa inserendo il valore di delibera.
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
    private String nomeGestore;
    @Builder.Default
    private Boolean predefinito = false;
    private LocalDate validoDal;
    private LocalDate validoAl;
    private String riferimentoDelibera;

    // ═══ FLAG DI CONFORMITA NORMATIVA (default = normativa) ═══
    @Builder.Default private Boolean applicaEsenzioneAccisaResidenti = true;
    @Builder.Default private Boolean applicaScaglioni = true;
    @Builder.Default private Boolean arrotondaPerdite = false;
    @Builder.Default private Boolean quotaFissaSoloNonResidenti = true;
    @Builder.Default private Boolean altrePartiteInImponibile = true;
    @Builder.Default private Boolean supportaAliquoteMiste = true;
    @Builder.Default private Boolean usaAliquotaIvaBolletta = true;

    // ═══ PERDITE ═══
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LivelloTensione livelloTensioneDefault = LivelloTensione.BT;
    @Column(precision = 8, scale = 6) private BigDecimal percentualePerdite;

    // ═══ TRASPORTO (valori ANNUI, /12 nel calcolo) ═══
    @Column(precision = 14, scale = 8) private BigDecimal trasportoQuotaFissaAnnua;
    @Column(precision = 14, scale = 8) private BigDecimal trasportoQuotaPotenzaAnnua;
    @Builder.Default private Boolean quotaPotenzaSoloNonDomestici = true;

    // ═══ ONERI DI SISTEMA (valori ANNUI, /12) ═══
    @Column(precision = 14, scale = 8) private BigDecimal asosQuotaFissaAnnua;
    @Column(precision = 14, scale = 8) private BigDecimal arimQuotaFissaAnnua;

    // ═══ IMPOSTE ═══
    @Column(precision = 14, scale = 8) private BigDecimal accisaDomestico;
    @Column(precision = 14, scale = 8) private BigDecimal accisaNonDomestico;
    @Column(precision = 12, scale = 2) private BigDecimal sogliaEsenzioneKwhMese;   // 150
    @Column(precision = 12, scale = 2) private BigDecimal potenzaMaxEsenzioneKw;    // 3
    @Column(precision = 12, scale = 2) private BigDecimal sogliaErosioneKwhMese1_5; // 150
    @Column(precision = 12, scale = 2) private BigDecimal sogliaErosioneKwhMese3;   // 220

    // ═══ IVA ═══
    @Column(precision = 6, scale = 4) private BigDecimal ivaDomestico;    // 0,10
    @Column(precision = 6, scale = 4) private BigDecimal ivaNonDomestico; // 0,22

    // ═══ DISPACCIAMENTO (base KWH_CON_PERDITE) ═══
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

    // ═══ CORRISPETTIVI COMMERCIALI ═══
    @Column(precision = 14, scale = 8) private BigDecimal commercializzazioneMese;
    @Column(precision = 14, scale = 8) private BigDecimal pcvVariabile;
    @Column(precision = 14, scale = 8) private BigDecimal spreadEnergia;

    // ═══ SCAGLIONI ═══
    @OneToMany(mappedBy = "parametri", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("componente ASC, limiteInferiore ASC")
    @JsonManagedReference
    @Builder.Default
    private List<ScaglioneConsumo> scaglioni = new ArrayList<>();

    // ═══ OFFERTE DEL GESTORE (prompt 1: relazione uno-a-molti) ═══
    // orphanRemoval = false: eliminare un gestore non cancella a cascata le offerte;
    // la disassociazione e' gestita esplicitamente nel GestoreService.
    // @JsonIgnore: le offerte sono esposte tramite i DTO di /api/gestori, non nella
    // serializzazione diretta di /api/parametri-gestore (che resta invariata).
    @OneToMany(mappedBy = "gestore", cascade = CascadeType.ALL, orphanRemoval = false)
    @OrderBy("nomeOfferta ASC")
    @com.fasterxml.jackson.annotation.JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<Offerta> offerte = new ArrayList<>();

    @CreationTimestamp private LocalDateTime createdAt;
    @UpdateTimestamp private LocalDateTime updatedAt;

    private static final BigDecimal DODICI = new BigDecimal("12");

    /**
     * Genera le voci a corrispettivo fisso o su kWh-con-perdite dipendenti dal tipo cliente.
     * NON include le componenti a scaglione (trasporto energia, ASOS/ARIM variabili) ne'
     * l'accisa: quelle sono calcolate dal motore perche' dipendono dal consumo e dai giorni.
     * Le quote energia per fascia arrivano invece dall'offerta.
     */
    public List<VoceCorrispettivo> toVoci(TipoCliente tipoCliente) {
        List<VoceCorrispettivo> voci = new ArrayList<>();
        int[] o = {0};

        // MATERIA ENERGIA — commercializzazione (gestore) + dispacciamento (nazionale)
        add(voci, o, "Commercializzazione e vendita (PCV)", CategoriaVoce.MATERIA_ENERGIA,
                BaseQuantita.FISSO_MESE, OrigineParametro.GESTORE, commercializzazioneMese, "EUR/mese", null);
        add(voci, o, "DispBT", CategoriaVoce.MATERIA_ENERGIA,
                BaseQuantita.FISSO_MESE, OrigineParametro.NAZIONALE, dispbt, "EUR/mese", "Del. ARERA");
        add(voci, o, "Corrispettivo mercato capacita", CategoriaVoce.MATERIA_ENERGIA,
                BaseQuantita.KWH_CON_PERDITE, OrigineParametro.NAZIONALE, corrMercatoCapacita, "EUR/kWh", "Del. 566/21");
        add(voci, o, "DIS / RTN (art. 46)", CategoriaVoce.MATERIA_ENERGIA,
                BaseQuantita.KWH_CON_PERDITE, OrigineParametro.NAZIONALE, corrDisRtn, "EUR/kWh", "Art. 46 Del. 111/06");
        add(voci, o, "INT (art. 73)", CategoriaVoce.MATERIA_ENERGIA,
                BaseQuantita.KWH_CON_PERDITE, OrigineParametro.NAZIONALE, corrInt, "EUR/kWh", "Art. 73 Del. 111/06");
        add(voci, o, "MSD (art. 44)", CategoriaVoce.MATERIA_ENERGIA,
                BaseQuantita.KWH_CON_PERDITE, OrigineParametro.NAZIONALE, corrMsd, "EUR/kWh", "Art. 44 Del. 111/06");
        add(voci, o, "UES / Sicurezza (art. 45)", CategoriaVoce.MATERIA_ENERGIA,
                BaseQuantita.KWH_CON_PERDITE, OrigineParametro.NAZIONALE, corrUesSicurezza, "EUR/kWh", "Art. 45 Del. 111/06");
        add(voci, o, "SAL (art. 48)", CategoriaVoce.MATERIA_ENERGIA,
                BaseQuantita.KWH_CON_PERDITE, OrigineParametro.NAZIONALE, corrSal, "EUR/kWh", "Art. 48 Del. 111/06");
        add(voci, o, "PCV variabile", CategoriaVoce.MATERIA_ENERGIA,
                BaseQuantita.KWH_CON_PERDITE, OrigineParametro.GESTORE, pcvVariabile, "EUR/kWh", null);
        add(voci, o, "Oneri sbilanciamento", CategoriaVoce.MATERIA_ENERGIA,
                BaseQuantita.KWH_CON_PERDITE, OrigineParametro.GESTORE, corrSbilanciamento, "EUR/kWh", null);
        add(voci, o, "Aggregazione misure (Del. 107/09)", CategoriaVoce.MATERIA_ENERGIA,
                BaseQuantita.FISSO_MESE, OrigineParametro.NAZIONALE, corrAggregazioneMisure, "EUR/mese", "Del. 107/09");

        // TRASPORTO — quota fissa e quota potenza (annue / 12)
        add(voci, o, "Trasporto - quota fissa", CategoriaVoce.TRASPORTO,
                BaseQuantita.POD_MESE, OrigineParametro.NAZIONALE, mensile(trasportoQuotaFissaAnnua), "EUR/pod/mese", "Tariffa TD ARERA");
        // Quota potenza: NON si applica ai domestici (regola ARERA)
        boolean applicaPotenza = !(Boolean.TRUE.equals(quotaPotenzaSoloNonDomestici)
                && tipoCliente != null && tipoCliente.isUsoDomestico());
        if (applicaPotenza) {
            add(voci, o, "Trasporto - quota potenza", CategoriaVoce.TRASPORTO,
                    BaseQuantita.KW_MESE, OrigineParametro.NAZIONALE, mensile(trasportoQuotaPotenzaAnnua), "EUR/kW/mese", "Tariffa TD ARERA");
        }

        // ONERI DI SISTEMA — quote fisse ASOS/ARIM: solo ai NON residenti (regola ARERA)
        boolean applicaQuotaFissa = !(Boolean.TRUE.equals(quotaFissaSoloNonResidenti)
                && tipoCliente != null && tipoCliente.isResidente());
        if (applicaQuotaFissa) {
            add(voci, o, "ASOS - quota fissa", CategoriaVoce.ONERI_SISTEMA,
                    BaseQuantita.POD_MESE, OrigineParametro.NAZIONALE, mensile(asosQuotaFissaAnnua), "EUR/pod/mese", "ASOS quota fissa");
            add(voci, o, "ARIM - quota fissa", CategoriaVoce.ONERI_SISTEMA,
                    BaseQuantita.POD_MESE, OrigineParametro.NAZIONALE, mensile(arimQuotaFissaAnnua), "EUR/pod/mese", "ARIM quota fissa");
        }

        return voci;
    }

    private BigDecimal mensile(BigDecimal annuo) {
        return annuo == null ? null : annuo.divide(DODICI, 8, RoundingMode.HALF_UP);
    }

    private void add(List<VoceCorrispettivo> voci, int[] o, String descr, CategoriaVoce cat,
                     BaseQuantita base, OrigineParametro origine, BigDecimal valore, String um, String rif) {
        if (valore == null) {
            return;
        }
        voci.add(VoceCorrispettivo.builder()
                .descrizione(descr).categoria(cat).base(base).origine(origine)
                .corrispettivo(valore).unitaMisura(um).ordine(o[0]++)
                .attiva(true).variabilePerMese(false).riferimentoNormativo(rif)
                .build());
    }
}
