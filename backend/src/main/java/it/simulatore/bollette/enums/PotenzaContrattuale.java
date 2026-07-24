package it.simulatore.bollette.enums;

public enum PotenzaContrattuale {
    KW_1_5("1,5 kW", 1.5),
    KW_3("3 kW", 3.0),
    KW_4_5("4,5 kW", 4.5),
    KW_6("6 kW", 6.0),
    KW_10("10 kW", 10.0);

    private final String descrizione;
    private final double valore;

    PotenzaContrattuale(String descrizione, double valore) {
        this.descrizione = descrizione;
        this.valore = valore;
    }

    public String getDescrizione() { return descrizione; }
    public double getValore() { return valore; }
}
