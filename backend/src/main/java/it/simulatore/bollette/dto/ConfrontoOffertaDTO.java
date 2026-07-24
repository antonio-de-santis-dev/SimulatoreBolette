package it.simulatore.bollette.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ConfrontoOffertaDTO {
    private Long offertaId;
    private String nomeFornitore;
    private String nomeOfferta;
    private String tipoOfferta;
    private String tipoTariffa;

    private BigDecimal prezzoKwhF0;
    private BigDecimal prezzoKwhF1;
    private BigDecimal prezzoKwhF23;
    private BigDecimal prezzoKwhF2;
    private BigDecimal prezzoKwhF3;

    private BigDecimal pcvAnnuo;
    private BigDecimal totaleBimestrale;
    private BigDecimal totaleAnnuale;
    private BigDecimal risparmioAnnualeVsMedia;
    private BigDecimal risparmioAnnualeVsPeggiore;
    private Integer posizioneClassifica;
    private Boolean miglioreOfferta;
}
