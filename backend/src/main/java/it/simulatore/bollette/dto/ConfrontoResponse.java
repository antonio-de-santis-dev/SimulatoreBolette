package it.simulatore.bollette.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Esito completo di un confronto: totali del gestore per categoria, confronto voce per voce
 * con gli importi fatturati dal concorrente, dettaglio dei mesi e risparmio.
 */
@Data
public class ConfrontoResponse {

    private Long confrontoId;

    // Contesto
    private Long bollettaConcorrenteId;
    private String nomeConcorrente;
    private String nomeOffertaConcorrente;
    private Long offertaGestoreId;
    private String nomeOffertaGestore;
    private String nomeProfiloParametri;
    private BigDecimal aliquotaIvaApplicata;

    // Confronto per categoria
    private List<CategoriaConfrontoDTO> categorie;

    // Totali gestore
    private BigDecimal gestoreImponibile;
    private BigDecimal gestoreIva;
    private BigDecimal gestoreTotale;

    // Totali concorrente (fatturati)
    private BigDecimal concorrenteImponibile;
    private BigDecimal concorrenteIva;
    private BigDecimal concorrenteTotale;

    // Risparmio
    private BigDecimal risparmioBimestrale;
    private BigDecimal risparmioAnnuale;
    private String attendibilitaStima;   // ALTA / MEDIA / BASSA
    private String avvertenzaRisparmio;

    // Indicatori sintetici
    private BigDecimal prezzoMedioLordoConcorrente;  // EUR/kWh
    private BigDecimal prezzoMedioLordoGestore;       // EUR/kWh

    // Dettaglio riga per riga dei mesi (replica visiva dell'Excel)
    private List<MeseCalcolatoDTO> mesi;
}
