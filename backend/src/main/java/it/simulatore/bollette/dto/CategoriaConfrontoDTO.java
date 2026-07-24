package it.simulatore.bollette.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Confronto di una categoria: importo fatturato dal concorrente contro importo calcolato
 * con i corrispettivi del gestore.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaConfrontoDTO {
    private String categoria;
    private String descrizione;
    private BigDecimal fatturatoConcorrente;
    private BigDecimal calcolatoGestore;
    private BigDecimal differenza;          // fatturato - gestore (positivo = risparmio)
    private BigDecimal differenzaPercentuale;
}
