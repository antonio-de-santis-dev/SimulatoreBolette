package it.simulatore.bollette.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Esito salvato di un confronto tra una bolletta concorrente e un'offerta del gestore.
 * Conserva i totali calcolati con i corrispettivi del gestore e il risparmio rispetto al
 * concorrente, per lo storico.
 */
@Entity
@Table(name = "confronti")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Confronto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long bollettaConcorrenteId;
    private String nomeConcorrente;
    private Long offertaGestoreId;
    private String nomeOffertaGestore;
    private Long parametriGestoreId;
    private String nomeProfiloParametri;

    // Totali calcolati con i corrispettivi del gestore
    @Column(precision = 12, scale = 4) private BigDecimal gestoreMateriaEnergia;
    @Column(precision = 12, scale = 4) private BigDecimal gestoreTrasporto;
    @Column(precision = 12, scale = 4) private BigDecimal gestoreOneriSistema;
    @Column(precision = 12, scale = 4) private BigDecimal gestoreImposte;
    @Column(precision = 12, scale = 4) private BigDecimal gestoreAltrePartite;
    @Column(precision = 12, scale = 4) private BigDecimal gestoreImponibile;
    @Column(precision = 12, scale = 4) private BigDecimal gestoreIva;
    @Column(precision = 12, scale = 4) private BigDecimal gestoreTotale;

    // Totale fatturato dal concorrente (termine di paragone)
    @Column(precision = 12, scale = 2) private BigDecimal concorrenteTotale;

    // Risparmio
    @Column(precision = 12, scale = 2) private BigDecimal risparmioBimestrale;
    @Column(precision = 12, scale = 2) private BigDecimal risparmioAnnuale;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
