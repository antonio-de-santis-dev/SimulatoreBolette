package it.simulatore.bollette.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Un mese di consumi all'interno di una {@link BollettaConcorrente}. Un bimestre ha due
 * mesi; e' ammesso anche un solo mese (fatture mensili come FuturEnergy).
 * <p>
 * I campi F1/F2/F3 sono i "tre input gialli" del modello Excel: gli unici veri dati di
 * input per la simulazione.
 */
@Entity
@Table(name = "mesi_bolletta")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "bolletta")
@EqualsAndHashCode(exclude = "bolletta")
public class MeseBolletta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bolletta_id")
    @JsonBackReference
    private BollettaConcorrente bolletta;

    @Column(nullable = false)
    private Integer numeroMese; // 1 o 2

    private String nomeMese; // "NOVEMBRE"
    private Integer mese; // 1-12
    private Integer anno;

    // --- I 3 INPUT GIALLI dell'Excel ---
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal consumoF1;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal consumoF2;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal consumoF3;

    // Letture contatore, opzionali
    @Column(precision = 12, scale = 2) private BigDecimal letturaAttualeF1;
    @Column(precision = 12, scale = 2) private BigDecimal letturaPrecedenteF1;
    @Column(precision = 12, scale = 2) private BigDecimal letturaAttualeF2;
    @Column(precision = 12, scale = 2) private BigDecimal letturaPrecedenteF2;
    @Column(precision = 12, scale = 2) private BigDecimal letturaAttualeF3;
    @Column(precision = 12, scale = 2) private BigDecimal letturaPrecedenteF3;

    /** Somma dei kWh netti del mese (F1+F2+F3). */
    public BigDecimal getTotaleNetto() {
        return nz(consumoF1).add(nz(consumoF2)).add(nz(consumoF3));
    }

    private static BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}
