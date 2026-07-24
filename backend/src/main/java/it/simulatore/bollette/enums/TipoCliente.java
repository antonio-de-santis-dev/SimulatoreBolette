package it.simulatore.bollette.enums;

/**
 * Tipologia cliente con i due attributi che pilotano le regole normative:
 * <ul>
 *   <li>{@code usoDomestico} → aliquota IVA (10% domestico, 22% non domestico) e struttura
 *       tariffaria domestica;</li>
 *   <li>{@code residente} → esenzione accisa fino a 3 kW e non applicazione della quota
 *       fissa degli oneri di sistema.</li>
 * </ul>
 * Riferimenti: TUA (DLgs 504/95) per l'accisa; delibere ARERA per oneri e trasporto.
 */
public enum TipoCliente {
    DOMESTICO_RESIDENTE("Domestico residente", true, true),
    DOMESTICO_NON_RESIDENTE("Domestico non residente", true, false),
    DOMESTICO_USI_DIVERSI("Domestico usi diversi", true, false),
    ALTRI_USI_BT("Altri usi in bassa tensione", false, false),
    ILLUMINAZIONE_PUBBLICA("Illuminazione pubblica", false, false);

    private final String descrizione;
    private final boolean usoDomestico;
    private final boolean residente;

    TipoCliente(String descrizione, boolean usoDomestico, boolean residente) {
        this.descrizione = descrizione;
        this.usoDomestico = usoDomestico;
        this.residente = residente;
    }

    public String getDescrizione() { return descrizione; }

    /** true → IVA 10% e tariffa domestica; false → IVA 22%. */
    public boolean isUsoDomestico() { return usoDomestico; }

    /** true → esenzione accisa ≤3 kW e nessuna quota fissa oneri. */
    public boolean isResidente() { return residente; }
}
