package it.simulatore.bollette.enums;

public enum TipoOfferta {
    PREZZO_FISSO("Prezzo fisso"),
    INDICIZZATA_PUN("Indicizzata PUN");

    private final String descrizione;

    TipoOfferta(String descrizione) { this.descrizione = descrizione; }
    public String getDescrizione() { return descrizione; }
}
