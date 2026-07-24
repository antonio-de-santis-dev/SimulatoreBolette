package it.simulatore.bollette.calculation;

import it.simulatore.bollette.enums.CategoriaVoce;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Esito del calcolo di un singolo mese: consumi (con perdite), righe di dettaglio e totali
 * per categoria.
 */
@Data
public class RisultatoMese {

    private final ConsumiFasce consumi;
    private final List<RigaCalcolata> righe;
    private final Map<CategoriaVoce, BigDecimal> totaliPerCategoria;

    public BigDecimal getTotale(CategoriaVoce categoria) {
        return totaliPerCategoria.getOrDefault(categoria, BigDecimal.ZERO);
    }

    /** Somma di tutte le categorie del mese (senza IVA). */
    public BigDecimal getTotaleMese() {
        return totaliPerCategoria.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
