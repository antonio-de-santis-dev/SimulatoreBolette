package it.simulatore.bollette.calculation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.TreeMap;

/**
 * Calcolo dell'IVA con supporto ad aliquote miste, totalizzata per aliquota.
 * <p>
 * Il foglio Excel ha una colonna {@code IVA%} per riga mai usata: l'IVA e' un'unica
 * percentuale sull'imponibile. Qui si permette la coesistenza di aliquote diverse (es. 10%
 * sull'energia e 22% su una partita non domestica), con totalizzazione separata.
 */
public final class CalcoloIva {

    private CalcoloIva() {
    }

    /** Dato l'imponibile per aliquota, restituisce l'IVA per aliquota (scale 4). */
    public static Map<BigDecimal, BigDecimal> perAliquota(Map<BigDecimal, BigDecimal> imponibilePerAliquota) {
        Map<BigDecimal, BigDecimal> iva = new TreeMap<>();
        imponibilePerAliquota.forEach((al, imp) ->
                iva.put(al, imp.multiply(al).setScale(4, RoundingMode.HALF_UP)));
        return iva;
    }

    /** Somma di tutte le IVA per aliquota. */
    public static BigDecimal totale(Map<BigDecimal, BigDecimal> ivaPerAliquota) {
        return ivaPerAliquota.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
