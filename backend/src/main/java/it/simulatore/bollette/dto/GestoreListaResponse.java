package it.simulatore.bollette.dto;

import lombok.Data;

import java.math.BigDecimal;

/** Vista compatta per l'elenco dei gestori/profili. */
@Data
public class GestoreListaResponse {
    private Long id;
    private String nomeGestore;
    private String nomeProfilo;
    private Boolean predefinito;
    private int numeroOfferte;
    private BigDecimal commercializzazioneMese;
}
