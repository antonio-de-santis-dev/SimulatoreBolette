package it.simulatore.bollette.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import it.simulatore.bollette.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "offerte")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Offerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nomeFornitore;

    @Column(nullable = false)
    private String nomeOfferta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoOfferta tipoOfferta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTariffa tipoTariffa;

    // ─────────────────────────────────────────────────────────────────────────
    // CAMPI LEGACY: mantenuti @Deprecated per retrocompatibilita' con il vecchio
    // motore /api/simulazioni. Il nuovo modello a due sorgenti usa la lista `voci`.
    // ─────────────────────────────────────────────────────────────────────────
    @Deprecated
    @Column(precision = 10, scale = 6)
    private BigDecimal prezzoFissoF0;

    @Column(precision = 10, scale = 6)
    private BigDecimal prezzoFissoF1;

    @Column(precision = 10, scale = 6)
    private BigDecimal prezzoFissoF23;

    @Column(precision = 10, scale = 6)
    private BigDecimal prezzoFissoF2;

    @Column(precision = 10, scale = 6)
    private BigDecimal prezzoFissoF3;

    @Column(precision = 10, scale = 6)
    private BigDecimal spreadPunF0;

    @Column(precision = 10, scale = 6)
    private BigDecimal spreadPunF1;

    @Column(precision = 10, scale = 6)
    private BigDecimal spreadPunF23;

    @Column(precision = 10, scale = 6)
    private BigDecimal spreadPunF2;

    @Column(precision = 10, scale = 6)
    private BigDecimal spreadPunF3;

    @Column(precision = 10, scale = 2)
    private BigDecimal pcvAnnuo;

    @Column(length = 1000)
    private String condizioniSpeciali;

    @Column
    private Boolean attiva = true;

    /**
     * Voci di corrispettivo dell'offerta (origine OFFERTA): tipicamente le quote energia per
     * fascia F1/F2/F3 e le relative perdite di rete. Sono il complemento dei parametri del
     * gestore nel motore a righe.
     */
    @OneToMany(mappedBy = "offerta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordine ASC")
    @JsonManagedReference
    @Builder.Default
    private List<VoceCorrispettivo> voci = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
