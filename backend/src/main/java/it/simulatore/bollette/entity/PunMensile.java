package it.simulatore.bollette.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pun_mensili")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PunMensile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer anno;

    @Column(nullable = false)
    private Integer mese;

    @Column(precision = 10, scale = 6)
    private BigDecimal punMonorario;

    @Column(precision = 10, scale = 6)
    private BigDecimal punF1;

    @Column(precision = 10, scale = 6)
    private BigDecimal punF2;

    @Column(precision = 10, scale = 6)
    private BigDecimal punF3;

    @Column(precision = 10, scale = 6)
    private BigDecimal punF23;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
