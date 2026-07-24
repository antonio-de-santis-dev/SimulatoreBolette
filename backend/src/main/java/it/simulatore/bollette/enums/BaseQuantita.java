package it.simulatore.bollette.enums;

/**
 * Base di quantita su cui si applica un corrispettivo ({@code TOTALE = CORRISPETTIVO x QUANTITA}).
 * <p>
 * Distinzioni critiche:
 * <ul>
 *   <li>{@link #KWH_NETTI} (F1+F2+F3): usata dall'accisa;</li>
 *   <li>{@link #KWH_NETTI_SCAGLIONE}: quota del consumo che ricade in uno scaglione tariffario,
 *       usata da trasporto energia e oneri variabili;</li>
 *   <li>{@link #KWH_CON_PERDITE}: netti + perdite di rete, usata dal dispacciamento e dal
 *       mercato capacita.</li>
 * </ul>
 */
public enum BaseQuantita {
    FISSO_MESE("Importo fisso mensile"),
    POD_MESE("Per POD al mese"),
    KW_MESE("Per kW al mese"),
    KWH_NETTI("kWh consumati"),
    KWH_NETTI_SCAGLIONE("kWh consumati ripartiti per scaglione"),
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
