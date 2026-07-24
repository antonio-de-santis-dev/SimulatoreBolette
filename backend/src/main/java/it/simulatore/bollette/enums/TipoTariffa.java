package it.simulatore.bollette.enums;

public enum TipoTariffa {
    MONORARIA("Monoraria (F0)"),
    BIORARIA("Bioraria (F1/F2-F3)"),
    TRIORARIA("Trioraria (F1/F2/F3)");

    private final String descrizione;

    TipoTariffa(String descrizione) { this.descrizione = descrizione; }
    public String getDescrizione() { return descrizione; }
}
