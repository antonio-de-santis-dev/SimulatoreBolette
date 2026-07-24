package it.simulatore.bollette.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import it.simulatore.bollette.enums.LivelloTensione;
import it.simulatore.bollette.enums.TipoCliente;
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
 * SORGENTE DATI "A": la bolletta da comparare.
 * <p>
 * Contiene i dati del concorrente inseriti a mano leggendo il PDF del cliente: anagrafica,
 * consumi F1/F2/F3 per mese (le celle gialle dell'Excel), potenza, e soprattutto gli
 * importi <b>gia' fatturati</b> dal concorrente, che servono come termine di paragone.
 */
@Entity
@Table(name = "bollette_concorrenti")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BollettaConcorrente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Identificativi fattura ---
    private String numeroFattura;
    private LocalDate dataFattura;
    private LocalDate periodoDal;
    private LocalDate periodoAl;

    // --- Fornitore concorrente ---
    @Column(nullable = false)
    private String nomeFornitore;
    private String nomeOfferta;
    private String codiceOfferta;

    // --- Cliente / fornitura ---
    private String ragioneSociale;
    private String indirizzoFornitura;
    private String pod;

    @Enumerated(EnumType.STRING)
    private TipoCliente tipologiaCliente;
    private String opzioneTariffaria;

    @Column(precision = 6, scale = 2) private BigDecimal potenzaImpegnata;
    @Column(precision = 6, scale = 2) private BigDecimal potenzaDisponibile;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LivelloTensione livelloTensione = LivelloTensione.BT;

    // --- Mesi con i consumi (2 per il bimestre, 1 ammesso) ---
    @OneToMany(mappedBy = "bolletta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numeroMese ASC")
    @JsonManagedReference
    @Builder.Default
    private List<MeseBolletta> mesi = new ArrayList<>();

    // --- Altre partite addebitate dal concorrente ---
    @OneToMany(mappedBy = "bolletta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordine ASC")
    @JsonManagedReference
    @Builder.Default
    private List<AltraPartita> altrePartite = new ArrayList<>();

    // --- Importi FATTURATI dal concorrente (termine di paragone) ---
    @Column(precision = 12, scale = 2) private BigDecimal fatturatoMateriaEnergia;
    @Column(precision = 12, scale = 2) private BigDecimal fatturatoTrasporto;
    @Column(precision = 12, scale = 2) private BigDecimal fatturatoOneriSistema;
    @Column(precision = 12, scale = 2) private BigDecimal fatturatoAltrePartite;
    @Column(precision = 12, scale = 2) private BigDecimal fatturatoImposte;
    @Column(precision = 12, scale = 2) private BigDecimal fatturatoIva;
    @Column(precision = 12, scale = 2) private BigDecimal fatturatoImponibile;
    @Column(precision = 12, scale = 2) private BigDecimal fatturatoTotale;

    @Column(precision = 6, scale = 4) private BigDecimal aliquotaIvaApplicata;

    @Column(length = 1000) private String note;

    @CreationTimestamp private LocalDateTime createdAt;
    @UpdateTimestamp private LocalDateTime updatedAt;

    /** Somma dei kWh netti di tutti i mesi. */
    public BigDecimal getConsumoTotaleNetto() {
        BigDecimal tot = BigDecimal.ZERO;
        if (mesi != null) {
            for (MeseBolletta m : mesi) {
                tot = tot.add(m.getTotaleNetto());
            }
        }
        return tot;
    }

    /**
     * Verifica di quadratura: le componenti fatturate sommano al totale dichiarato?
     * imponibile = materia + trasporto + oneri + altre partite (soggette) + imposte;
     * totale = imponibile + IVA. Tolleranza di 1 centesimo.
     */
    public boolean quadraturaImportiValida() {
        if (fatturatoImponibile == null || fatturatoTotale == null) {
            return false;
        }
        BigDecimal sommaComponenti = nz(fatturatoMateriaEnergia)
                .add(nz(fatturatoTrasporto))
                .add(nz(fatturatoOneriSistema))
                .add(nz(fatturatoAltrePartite))
                .add(nz(fatturatoImposte));
        BigDecimal tolleranza = new BigDecimal("0.01");
        boolean imponibileOk = sommaComponenti.subtract(fatturatoImponibile).abs().compareTo(tolleranza) <= 0;
        boolean totaleOk = nz(fatturatoImponibile).add(nz(fatturatoIva))
                .subtract(fatturatoTotale).abs().compareTo(tolleranza) <= 0;
        return imponibileOk && totaleOk;
    }

    /** EUR/kWh medio lordo (totale fatturato / kWh netti) — indicatore di confronto sintetico. */
    public BigDecimal getPrezzoMedioLordoKwh() {
        BigDecimal kwh = getConsumoTotaleNetto();
        if (fatturatoTotale == null || kwh.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return fatturatoTotale.divide(kwh, 6, java.math.RoundingMode.HALF_UP);
    }

    private static BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}
