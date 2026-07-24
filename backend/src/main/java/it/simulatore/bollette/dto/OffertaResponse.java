package it.simulatore.bollette.dto;

import it.simulatore.bollette.enums.TipoOfferta;
import it.simulatore.bollette.enums.TipoTariffa;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OffertaResponse {
    private Long id;
    private String nomeFornitore;
    private String nomeOfferta;
    private TipoOfferta tipoOfferta;
    private TipoTariffa tipoTariffa;
    private BigDecimal prezzoFissoF0;
    private BigDecimal prezzoFissoF1;
    private BigDecimal prezzoFissoF23;
    private BigDecimal spreadPunF0;
    private BigDecimal spreadPunF1;
    private BigDecimal spreadPunF23;
    private BigDecimal pcvAnnuo;
    private Boolean attiva;
    private LocalDateTime createdAt;
}
