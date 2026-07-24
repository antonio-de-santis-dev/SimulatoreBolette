package it.simulatore.bollette.calculation;

import it.simulatore.bollette.enums.CategoriaVoce;
import it.simulatore.bollette.enums.OrigineParametro;

import java.math.BigDecimal;

/**
 * Riga di dettaglio calcolata: {@code importo = corrispettivo x quantita}. Riporta anche la
 * categoria e l'origine (nazionale/gestore/offerta) per la replica visiva dell'Excel.
 */
public record RigaCalcolata(
        String descrizione,
        String unitaMisura,
        BigDecimal corrispettivo,
        BigDecimal quantita,
        BigDecimal importo,
        CategoriaVoce categoria,
        OrigineParametro origine
) {
}
