package it.simulatore.bollette.enums;

/**
 * Sorgente del dato di una voce di corrispettivo. E' cio che rende visibile all'utente
 * la separazione tra le due sorgenti dati del simulatore: i parametri nazionali (ARERA),
 * i corrispettivi commerciali del gestore e i valori letti dalla bolletta da comparare.
 */
public enum OrigineParametro {
    NAZIONALE("Parametro nazionale ARERA"),
    GESTORE("Corrispettivo commerciale del gestore"),
    OFFERTA("Specifico dell'offerta"),
    BOLLETTA("Letto dalla bolletta da comparare");

    private final String descrizione;

    OrigineParametro(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getDescrizione() {
        return descrizione;
    }
}
