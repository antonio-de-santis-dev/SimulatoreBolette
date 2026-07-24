package it.simulatore.bollette.enums;

/**
 * Le cinque categorie in cui il modello Excel raggruppa le voci di una bolletta.
 * L'ordine riflette quello del "quadro sintetico" della fattura.
 */
public enum CategoriaVoce {
    MATERIA_ENERGIA("Spesa per la materia energia"),
    TRASPORTO("Trasporto e gestione contatore"),
    ONERI_SISTEMA("Oneri di sistema"),
    IMPOSTE("Imposte"),
    ALTRE_PARTITE("Altre partite");

    private final String descrizione;

    CategoriaVoce(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getDescrizione() {
        return descrizione;
    }
}
