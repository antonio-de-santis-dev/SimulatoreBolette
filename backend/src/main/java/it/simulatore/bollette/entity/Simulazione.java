package it.simulatore.bollette.entity;

import it.simulatore.bollette.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "simulazioni")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Simulazione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCliente tipoCliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PotenzaContrattuale potenzaContrattuale;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal consumoTotaleKwh;

    @Column(precision = 10, scale = 2)
    private BigDecimal consumoF1;

    @Column(precision = 10, scale = 2)
    private BigDecimal consumoF2;

    @Column(precision = 10, scale = 2)
    private BigDecimal consumoF3;

    @Column(precision = 12, scale = 4)
    private BigDecimal spesaMateriaEnergia;

    @Column(precision = 12, scale = 4)
    private BigDecimal spesaTrasporto;

    @Column(precision = 12, scale = 4)
    private BigDecimal spesaOneriSistema;

    @Column(precision = 12, scale = 4)
    private BigDecimal spesaAccise;

    @Column(precision = 12, scale = 4)
    private BigDecimal spesaIva;

    @Column(precision = 12, scale = 4)
    private BigDecimal totaleBimestrale;

    @Column(precision = 12, scale = 4)
    private BigDecimal totaleAnnuale;

    @Column(precision = 10, scale = 6)
    private BigDecimal prezzoMedioKwh;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
