package it.simulatore.bollette.calculation;

import it.simulatore.bollette.enums.AttendibilitaStima;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Stima del risparmio annuale con indice di attendibilita.
 * <p>
 * L'Excel calcola {@code risparmio_bimestrale x 6} confrontando una sola bolletta: e'
 * fragile perche' i consumi sono stagionali (nel foglio DICEMBRE-GENNAIO un mese a zero
 * produce un risparmio annuale di 556,70 EUR, privo di significato). Qui:
 * <ul>
 *   <li>se e' noto il consumo annuo, si usa quello (attendibilita ALTA);</li>
 *   <li>altrimenti si estrapola x6, declassando l'attendibilita quando i due mesi sono
 *       squilibrati, uno e' a zero o il bimestre e' sotto i 100 kWh.</li>
 * </ul>
 */
public final class AnalisiRisparmio {

    private AnalisiRisparmio() {
    }

    public record Stima(BigDecimal risparmioBimestrale,
                        BigDecimal risparmioAnnualeStimato,
                        AttendibilitaStima attendibilita,
                        String avvertenza) {
    }

    /**
     * @param risparmioBimestrale risparmio del bimestre (positivo = a favore del gestore)
     * @param consumiMese         kWh netti di ciascun mese del bimestre
     * @param consumoBimestre     kWh netti totali del bimestre
     * @param consumoAnnuoNoto    kWh annui dichiarati in fattura, oppure null
     */
    public static Stima calcola(BigDecimal risparmioBimestrale, List<BigDecimal> consumiMese,
                                BigDecimal consumoBimestre, BigDecimal consumoAnnuoNoto) {
        BigDecimal bim = risparmioBimestrale != null ? risparmioBimestrale : BigDecimal.ZERO;

        // 1. Consumo annuo noto → nessuna estrapolazione stagionale
        if (consumoAnnuoNoto != null && consumoAnnuoNoto.compareTo(BigDecimal.ZERO) > 0
                && consumoBimestre != null && consumoBimestre.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal fattore = consumoAnnuoNoto.divide(consumoBimestre, 6, RoundingMode.HALF_UP);
            BigDecimal annuale = bim.multiply(fattore).setScale(2, RoundingMode.HALF_UP);
            return new Stima(bim, annuale, AttendibilitaStima.ALTA,
                    "Stima basata sul consumo annuo dichiarato in fattura, non sull'estrapolazione del bimestre.");
        }

        // 2. Estrapolazione x6 con declassamento
        BigDecimal annuale = bim.multiply(new BigDecimal("6")).setScale(2, RoundingMode.HALF_UP);
        AttendibilitaStima att = valutaAttendibilita(consumiMese, consumoBimestre);
        String avvertenza = switch (att) {
            case ALTA -> "Stima ottenuta estrapolando il bimestre x6; i due mesi sono equilibrati.";
            case MEDIA -> "Stima indicativa: i due mesi del bimestre differiscono in modo sensibile, "
                    + "l'estrapolazione x6 puo' discostarsi dal consumo reale.";
            case BASSA -> "Attenzione: stima poco attendibile. Il bimestre non e' rappresentativo "
                    + "(mese a zero, mesi molto squilibrati o consumo troppo basso). "
                    + "Inserisci il consumo annuo per una stima affidabile.";
        };
        return new Stima(bim, annuale, att, avvertenza);
    }

    private static AttendibilitaStima valutaAttendibilita(List<BigDecimal> consumiMese, BigDecimal consumoBimestre) {
        if (consumiMese == null || consumiMese.isEmpty()) {
            return AttendibilitaStima.BASSA;
        }
        // Un mese a zero
        boolean meseAZero = consumiMese.stream().anyMatch(c -> c == null || c.compareTo(BigDecimal.ZERO) == 0);
        // Bimestre sotto i 100 kWh
        boolean bimestreBasso = consumoBimestre != null && consumoBimestre.compareTo(new BigDecimal("100")) < 0;
        if (meseAZero || bimestreBasso) {
            return AttendibilitaStima.BASSA;
        }
        if (consumiMese.size() >= 2) {
            BigDecimal a = consumiMese.get(0);
            BigDecimal b = consumiMese.get(1);
            BigDecimal max = a.max(b);
            if (max.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal scarto = a.subtract(b).abs().divide(max, 4, RoundingMode.HALF_UP);
                if (scarto.compareTo(new BigDecimal("0.50")) > 0) {
                    return AttendibilitaStima.BASSA;
                }
                if (scarto.compareTo(new BigDecimal("0.20")) > 0) {
                    return AttendibilitaStima.MEDIA;
                }
            }
        }
        return AttendibilitaStima.ALTA;
    }
}
