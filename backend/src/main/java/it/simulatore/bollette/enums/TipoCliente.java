package it.simulatore.bollette.enums;

public enum TipoCliente {
    RESIDENTE("Residente"),
    NON_RESIDENTE("Non residente"),
    DOMESTICO_USI_DIVERSI("Domestico usi diversi"),
    ATTIVITA_PRODUTTIVE("Attivita produttive");

    private final String descrizione;

    TipoCliente(String descrizione) { this.descrizione = descrizione; }
    public String getDescrizione() { return descrizione; }
}
