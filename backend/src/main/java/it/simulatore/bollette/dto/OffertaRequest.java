package it.simulatore.bollette.dto;

import it.simulatore.bollette.enums.TipoOfferta;
import it.simulatore.bollette.enums.TipoTariffa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OffertaRequest {

    @NotBlank
    private String nomeFornitore;

    @NotBlank
    private String nomeOfferta;

    @NotNull
    private TipoOfferta tipoOfferta;

    @NotNull
    private TipoTariffa tipoTariffa;

    private BigDecimal prezzoFissoF0;
    private BigDecimal prezzoFissoF1;
    private BigDecimal prezzoFissoF23;
    private BigDecimal prezzoFissoF2;
    private BigDecimal prezzoFissoF3;

    private BigDecimal spreadPunF0;
    private BigDecimal spreadPunF1;
    private BigDecimal spreadPunF23;
    private BigDecimal spreadPunF2;
    private BigDecimal spreadPunF3;

    private BigDecimal pcvAnnuo;
    private String condizioniSpeciali;
}
