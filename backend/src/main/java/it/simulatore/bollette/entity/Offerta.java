package it.simulatore.bollette.entity;

import it.simulatore.bollette.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
