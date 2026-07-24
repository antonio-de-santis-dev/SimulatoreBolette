package it.simulatore.bollette.calculation;

import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Consumi di un mese suddivisi per fascia, con le perdite di rete calcolate secondo il
 * modello Excel.
 * <p>
 * REGOLA CRITICA: le perdite si applicano alla <b>quantita</b>, arrotondata a intero, non
 * al prezzo: {@code kWh_perdite = ROUND(kWh x coeff, 0)}. Esempio: 24 kWh con coeff 10% →
 * ROUND(2,4) = 2 kWh.
 */
@Data
public class ConsumiFasce {

    private BigDecimal f1 = BigDecimal.ZERO;
    private BigDecimal f2 = BigDecimal.ZERO;
    private BigDecimal f3 = BigDecimal.ZERO;
    private BigDecimal perditeF1 = BigDecimal.ZERO;
    private BigDecimal perditeF2 = BigDecimal.ZERO;
    private BigDecimal perditeF3 = BigDecimal.ZERO;

    /** Replica ROUND(kWh * coeff, 0) di Excel (o scala 6 se l'arrotondamento e' disattivato). */
    public void calcolaPerdite(BigDecimal coefficiente, boolean arrotonda) {
        this.perditeF1 = applica(f1, coefficiente, arrotonda);
        this.perditeF2 = applica(f2, coefficiente, arrotonda);
        this.perditeF3 = applica(f3, coefficiente, arrotonda);
    }

    private BigDecimal applica(BigDecimal kwh, BigDecimal coeff, boolean arrotonda) {
        BigDecimal p = nz(kwh).multiply(coeff);
        return arrotonda ? p.setScale(0, RoundingMode.HALF_UP)
                         : p.setScale(6, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotaleNetto() {
        return nz(f1).add(nz(f2)).add(nz(f3));
    }

    public BigDecimal getTotalePerdite() {
        return nz(perditeF1).add(nz(perditeF2)).add(nz(perditeF3));
    }

    public BigDecimal getTotaleConPerdite() {
        return getTotaleNetto().add(getTotalePerdite());
    }

    private static BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}
