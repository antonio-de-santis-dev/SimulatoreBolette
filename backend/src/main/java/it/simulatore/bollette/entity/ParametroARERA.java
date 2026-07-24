package it.simulatore.bollette.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "parametri_arera")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParametroARERA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nomeParametro;

    @Column(nullable = false)
    private String descrizione;

    @Column(nullable = false, precision = 12, scale = 8)
    private BigDecimal valore;

    @Column(nullable = false)
    private String unitaMisura;

    @Column(nullable = false)
    private LocalDate validoDal;

    @Column
    private LocalDate validoAl;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
