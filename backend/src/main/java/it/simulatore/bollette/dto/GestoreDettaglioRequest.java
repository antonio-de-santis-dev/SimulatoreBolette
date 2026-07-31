package it.simulatore.bollette.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Request per creazione/aggiornamento di un gestore.
 *
 * Tutti i campi sono opzionali (nullable) per la MODIFICA PARZIALE: in
 * aggiornamento un campo {@code null} NON sovrascrive il valore esistente
 * (merge, non replace). Contiene solo la parte commerciale/gestore: i parametri
 * nazionali ARERA non sono qui (prompt 2). Le offerte si gestiscono come
 * sotto-risorsa dedicata.
 */
@Data
public class GestoreDettaglioRequest {

    // anagrafica
    private String nomeProfilo;
    private String nomeGestore;
    private String descrizione;
    private Boolean predefinito;

    // configurazione commerciale del gestore
    private BigDecimal commercializzazioneMese;
    private BigDecimal pcvVariabile;
    private BigDecimal spreadEnergia;

    // flag di conformita
    private Boolean applicaEsenzioneAccisaResidenti;
    private Boolean applicaScaglioni;
    private Boolean arrotondaPerdite;
    private Boolean quotaFissaSoloNonResidenti;
    private Boolean altrePartiteInImponibile;
    private Boolean supportaAliquoteMiste;
    private Boolean usaAliquotaIvaBolletta;
}
