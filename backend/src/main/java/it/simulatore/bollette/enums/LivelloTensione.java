package it.simulatore.bollette.enums;

import java.math.BigDecimal;

/**
 * Livello di tensione della fornitura, con il relativo coefficiente di perdita di rete.
 * Il modello Excel usa il 10% tondo (profilo "Modello Excel"), ma il valore reale per la
 * bassa tensione e' 10,40% (vedi fattura FuturEnergy).
 */
public enum LivelloTensione {
    BT("Bassa tensione", new BigDecimal("0.1040")),
    MT("Media tensione", new BigDecimal("0.0470")),
    AT_150("<= 150 kW", new BigDecimal("0.0180")),
    AT_220("220 kW", new BigDecimal("0.0110")),
    AT_370("370 kW", new BigDecimal("0.0070"));

    private final String descrizione;
    private final BigDecimal coefficientePerdite;

    LivelloTensione(String descrizione, BigDecimal coefficientePerdite) {
        this.descrizione = descrizione;
        this.coefficientePerdite = coefficientePerdite;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public BigDecimal getCoefficientePerdite() {
        return coefficientePerdite;
    }
}
