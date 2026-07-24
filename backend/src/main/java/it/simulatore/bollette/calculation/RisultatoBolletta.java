package it.simulatore.bollette.calculation;

import it.simulatore.bollette.enums.CategoriaVoce;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Esito completo di una bolletta (uno o due mesi aggregati): totali per categoria,
 * imponibile, IVA e totale finale.
 */
public record RisultatoBolletta(
        List<RisultatoMese> mesi,
        Map<CategoriaVoce, BigDecimal> totaliPerCategoria,
        BigDecimal altrePartiteSoggette,
        BigDecimal altrePartiteNonSoggette,
        BigDecimal imponibile,
        BigDecimal aliquotaIva,
        BigDecimal iva,
        BigDecimal totale
) {
    public BigDecimal totale(CategoriaVoce c) {
        return totaliPerCategoria.getOrDefault(c, BigDecimal.ZERO);
    }
}
