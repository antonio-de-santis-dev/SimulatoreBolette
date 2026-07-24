package it.simulatore.bollette.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import it.simulatore.bollette.enums.TipoCliente;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Scaglione di consumo per le componenti tariffarie progressive (trasporto energia, ASOS e
 * ARIM variabili). E' la struttura che il foglio Excel NON ha: la riga "Scag1mese1" e i
 * quattro valori orfani del foglio DATI (7,6302 · 6,7709 · 0,3214 · 1,2554) suggeriscono
 * che gli scaglioni fossero previsti ma mai implementati.
 * <p>
 * Riferimento: struttura tariffaria ARERA per i clienti domestici (quota energia a scaglioni
 * di consumo annuo). In modalita compatibilita Excel si usa un unico scaglione illimitato.
 */
@Entity
@Table(name = "scaglioni_consumo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "parametri")
@EqualsAndHashCode(exclude = "parametri")
public class ScaglioneConsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parametri_id")
    @JsonBackReference
    private ParametriGestore parametri;

    /** Componente a cui si applica: "TRASPORTO_ENERGIA", "ASOS_VARIABILE", "ARIM_VARIABILE". */
    @Column(nullable = false)
    private String componente;

    @Enumerated(EnumType.STRING)
    private TipoCliente tipoCliente;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal limiteInferiore;

    /** null = illimitato (ultimo scaglione). */
    @Column(precision = 12, scale = 2)
    private BigDecimal limiteSuperiore;

    @Column(nullable = false, precision = 14, scale = 8)
    private BigDecimal valore;

    private String unitaMisura;
    private Integer ordine;
}
