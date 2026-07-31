package it.simulatore.bollette.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Vista completa che alimenta la pagina dettaglio "Il mio gestore":
 * anagrafica + configurazione commerciale + flag di conformita + offerte.
 *
 * NOTA: i parametri nazionali (accise, trasporto, oneri, dispacciamento, IVA,
 * perdite) NON compaiono qui — restano in ParametriGestore e saranno gestiti
 * dal secondo intervento (prompt 2).
 */
@Data
public class GestoreDettaglioResponse {

    // anagrafica
    private Long id;
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

    // offerte del gestore
    private List<OffertaResponse> offerte;

    // conteggio sintetico per la lista
    private int numeroOfferte;
}
