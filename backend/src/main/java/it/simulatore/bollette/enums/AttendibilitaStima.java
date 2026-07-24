package it.simulatore.bollette.enums;

/**
 * Attendibilita della stima del risparmio annuale. Il semplice {@code bimestrale x 6}
 * dell'Excel e' statisticamente fragile per via della stagionalita dei consumi.
 */
public enum AttendibilitaStima {
    ALTA("Stima affidabile"),
    MEDIA("Stima indicativa"),
    BASSA("Stima poco attendibile");

    private final String descrizione;

    AttendibilitaStima(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getDescrizione() {
        return descrizione;
    }
}
