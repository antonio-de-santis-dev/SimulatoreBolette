package it.simulatore.bollette.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RigaCalcolataDTO {
    private String descrizione;
    private String unitaMisura;
    private BigDecimal corrispettivo;
    private BigDecimal quantita;
    private BigDecimal importo;
    private String categoria;
    private String origine;
    private String origineDescrizione;
}
