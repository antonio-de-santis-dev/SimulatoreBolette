package it.simulatore.bollette.enums;

/**
 * Tipologia cliente cosi' come dichiarata nelle fatture reali.
 * Nota: l'aliquota IVA NON si deriva da questo enum, ma dal campo aliquotaIva della
 * bolletta da comparare o dai parametri del gestore.
 */
public enum TipoCliente {
    RESIDENTE("Domestico residente"),
    NON_RESIDENTE("Domestico non residente"),
    DOMESTICO_USI_DIVERSI("Domestico usi diversi"),
    ATTIVITA_PRODUTTIVE("Attivita produttive"),
    PMI_RESIDENZIALE("PMI residenziale");

    private final String descrizione;

    TipoCliente(String descrizione) { this.descrizione = descrizione; }
    public String getDescrizione() { return descrizione; }
}
