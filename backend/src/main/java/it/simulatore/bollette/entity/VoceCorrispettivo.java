package it.simulatore.bollette.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import it.simulatore.bollette.enums.BaseQuantita;
import it.simulatore.bollette.enums.CategoriaVoce;
import it.simulatore.bollette.enums.OrigineParametro;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Riga generica del motore di calcolo: {@code TOTALE = corrispettivo x quantita}, dove la
 * quantita e' determinata dalla {@link BaseQuantita}.
 * <p>
 * Ogni voce dichiara la sua {@link OrigineParametro} (nazionale / gestore / offerta): e'
 * cio' che rende visibile all'utente la separazione tra le due sorgenti dati.
 */
@Entity
@Table(name = "voci_corrispettivo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "offerta")
@EqualsAndHashCode(exclude = "offerta")
public class VoceCorrispettivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offerta_id")
    @JsonBackReference
    private Offerta offerta;

    @Column(nullable = false)
    private String descrizione;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaVoce categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BaseQuantita base;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrigineParametro origine;

    @Column(nullable = false, precision = 16, scale = 8)
    private BigDecimal corrispettivo;

    @Column(precision = 6, scale = 4)
    private BigDecimal aliquotaIva;

    @Column(length = 20)
    private String unitaMisura;

    private Integer ordine;

    @Builder.Default
    private Boolean attiva = true;

    /** Se true il corrispettivo cambia tra mese 1 e mese 2 (es. quote energia indicizzate). */
    @Builder.Default
    private Boolean variabilePerMese = false;

    /**
     * Mese a cui la voce si applica: null o 0 = tutti i mesi; 1 = solo primo mese; 2 = solo
     * secondo mese. Permette di modellare i corrispettivi che nel foglio Excel differiscono
     * tra i due mesi del bimestre.
     */
    private Integer numeroMese;
}
