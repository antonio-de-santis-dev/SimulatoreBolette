package it.simulatore.bollette.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Riga di "altra partita" addebitata nella bolletta da comparare (es. gestione pratica,
 * canone, sconti). L'importo puo' essere negativo.
 * <p>
 * Il flag {@link #soggettaIva} decide se la partita entra nell'imponibile IVA (true, es.
 * gestione pratica FuturEnergy 75,94) o se viene sommata al totale dopo l'IVA (false, es.
 * il -11,40 "non soggetto ad IVA" del foglio Excel GENNAIO-FEBBRAIO).
 */
@Entity
@Table(name = "altre_partite")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "bolletta")
@EqualsAndHashCode(exclude = "bolletta")
public class AltraPartita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bolletta_id")
    @JsonBackReference
    private BollettaConcorrente bolletta;

    @Column(nullable = false)
    private String descrizione;

    private LocalDate dal;
    private LocalDate al;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal importo; // puo' essere negativo

    @Column(precision = 6, scale = 4)
    private BigDecimal aliquotaIva;

    @Builder.Default
    private Boolean soggettaIva = true;

    private Integer ordine;
}
