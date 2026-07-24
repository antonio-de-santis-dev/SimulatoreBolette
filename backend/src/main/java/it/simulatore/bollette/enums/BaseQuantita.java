package it.simulatore.bollette.enums;

/**
 * Base di quantita su cui si applica un corrispettivo per ottenere l'importo di riga
 * ({@code TOTALE = CORRISPETTIVO x QUANTITA}).
 * <p>
 * Distinzione critica del modello Excel: {@link #KWH_NETTI} (F1+F2+F3) contro
 * {@link #KWH_CON_PERDITE} (netti + perdite di rete arrotondate). Le due basi non
 * coincidono e vanno usate dalle voci corrette.
 */
public enum BaseQuantita {
    FISSO_MESE("Importo fisso mensile"),
    POD_MESE("Per POD al mese"),
    KW_MESE("Per kW al mese"),
    KWH_NETTI("kWh consumati"),
    KWH_CON_PERDITE("kWh consumati + perdite di rete"),
    KWH_F1("kWh fascia F1"),
    KWH_F2("kWh fascia F2"),
    KWH_F3("kWh fascia F3"),
    PERDITE_F1("Perdite di rete F1"),
    PERDITE_F2("Perdite di rete F2"),
    PERDITE_F3("Perdite di rete F3");

    private final String descrizione;

    BaseQuantita(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getDescrizione() {
        return descrizione;
    }
}
