package it.simulatore.bollette.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class MeseCalcolatoDTO {
    private Integer numeroMese;
    private String nomeMese;
    private BigDecimal kwhNetti;
    private BigDecimal kwhPerdite;
    private BigDecimal kwhConPerdite;
    private BigDecimal totaleMese;
    private List<RigaCalcolataDTO> righe;
}
