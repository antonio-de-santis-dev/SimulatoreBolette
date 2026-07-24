package it.simulatore.bollette.service;

import it.simulatore.bollette.calculation.AnalisiRisparmio;
import it.simulatore.bollette.calculation.MotoreCalcolo;
import it.simulatore.bollette.calculation.RigaCalcolata;
import it.simulatore.bollette.calculation.RisultatoMese;
import it.simulatore.bollette.dto.*;
import it.simulatore.bollette.entity.*;
import it.simulatore.bollette.enums.CategoriaVoce;
import it.simulatore.bollette.enums.TipoCliente;
import it.simulatore.bollette.exception.ResourceNotFoundException;
import it.simulatore.bollette.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Servizio del confronto: presi i kWh della bolletta del concorrente, ricalcola la bolletta
 * con i corrispettivi del gestore e mostra la differenza voce per voce.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfrontoService {

    private static final BigDecimal SEI = new BigDecimal("6");
    private static final int SCALE_IVA = 4;

    private final MotoreCalcolo motore;
    private final BollettaConcorrenteRepository bollettaRepo;
    private final OffertaRepository offertaRepo;
    private final ParametriGestoreRepository parametriRepo;
    private final ConfrontoRepository confrontoRepo;

    @Transactional
    public ConfrontoResponse confronta(ConfrontoRequest request) {
        BollettaConcorrente bolletta = bollettaRepo.findById(request.getBollettaConcorrenteId())
                .orElseThrow(() -> new ResourceNotFoundException("Bolletta concorrente", request.getBollettaConcorrenteId()));
        Offerta offerta = offertaRepo.findById(request.getOffertaGestoreId())
                .orElseThrow(() -> new ResourceNotFoundException("Offerta gestore", request.getOffertaGestoreId()));
        ParametriGestore parametri = risolviParametri(request.getParametriGestoreId());

        ConfrontoResponse response = esegui(bolletta, offerta, parametri);

        if (request.isSalva()) {
            Confronto salvato = confrontoRepo.save(toEntity(response, bolletta, offerta, parametri));
            response.setConfrontoId(salvato.getId());
        }
        return response;
    }

    /** Confronta una bolletta concorrente contro piu' offerte del gestore. */
    @Transactional
    public List<ConfrontoResponse> confrontaMultiplo(Long bollettaId, List<Long> offerteIds, Long parametriId) {
        List<ConfrontoResponse> risultati = new ArrayList<>();
        for (Long offertaId : offerteIds) {
            ConfrontoRequest req = new ConfrontoRequest();
            req.setBollettaConcorrenteId(bollettaId);
            req.setOffertaGestoreId(offertaId);
            req.setParametriGestoreId(parametriId);
            req.setSalva(false);
            risultati.add(confronta(req));
        }
        risultati.sort((a, b) -> b.getRisparmioAnnuale().compareTo(a.getRisparmioAnnuale()));
        return risultati;
    }

    public List<Confronto> storico() {
        return confrontoRepo.findAllByOrderByCreatedAtDesc();
    }

    public Confronto getById(Long id) {
        return confrontoRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Confronto", id));
    }

    /** Calcolo puro (senza persistenza) — usato anche dai test. */
    public ConfrontoResponse esegui(BollettaConcorrente bolletta, Offerta offerta, ParametriGestore parametri) {
        BigDecimal potenza = bolletta.getPotenzaImpegnata();

        // 1. Calcolo con i corrispettivi del gestore, sui kWh della bolletta concorrente
        List<RisultatoMese> risultati = new ArrayList<>();
        for (MeseBolletta mese : bolletta.getMesi()) {
            risultati.add(motore.calcolaMese(mese, potenza, offerta, parametri, bolletta.getTipologiaCliente()));
        }

        Map<CategoriaVoce, BigDecimal> totali = sommaPerCategoria(risultati);

        // 2. Imponibile — le altre partite (soggette) del gestore entrano se il flag e' attivo
        BigDecimal imponibile = totali.get(CategoriaVoce.MATERIA_ENERGIA)
                .add(totali.get(CategoriaVoce.TRASPORTO))
                .add(totali.get(CategoriaVoce.ONERI_SISTEMA))
                .add(totali.get(CategoriaVoce.IMPOSTE));
        if (Boolean.TRUE.equals(parametri.getAltrePartiteInImponibile())) {
            imponibile = imponibile.add(totali.get(CategoriaVoce.ALTRE_PARTITE));
        }
        imponibile = imponibile.setScale(SCALE_IVA, RoundingMode.HALF_UP);

        // 3. IVA — dall'aliquota della bolletta oppure derivata dal tipo cliente
        BigDecimal aliquota = risolviAliquota(bolletta, parametri);
        BigDecimal iva = imponibile.multiply(aliquota).setScale(SCALE_IVA, RoundingMode.HALF_UP);
        BigDecimal totaleGestore = imponibile.add(iva).setScale(SCALE_IVA, RoundingMode.HALF_UP);

        // 4. Confronto voce per voce contro il fatturato
        return buildResponse(bolletta, offerta, parametri, totali, imponibile, iva, totaleGestore, aliquota, risultati);
    }

    private ConfrontoResponse buildResponse(BollettaConcorrente bolletta, Offerta offerta, ParametriGestore parametri,
                                            Map<CategoriaVoce, BigDecimal> totali, BigDecimal imponibile, BigDecimal iva,
                                            BigDecimal totaleGestore, BigDecimal aliquota, List<RisultatoMese> risultati) {
        ConfrontoResponse r = new ConfrontoResponse();
        r.setBollettaConcorrenteId(bolletta.getId());
        r.setNomeConcorrente(bolletta.getNomeFornitore());
        r.setNomeOffertaConcorrente(bolletta.getNomeOfferta());
        r.setOffertaGestoreId(offerta.getId());
        r.setNomeOffertaGestore(offerta.getNomeFornitore() + " - " + offerta.getNomeOfferta());
        r.setNomeProfiloParametri(parametri.getNomeProfilo());
        r.setAliquotaIvaApplicata(aliquota);

        List<CategoriaConfrontoDTO> categorie = new ArrayList<>();
        categorie.add(cat(CategoriaVoce.MATERIA_ENERGIA, bolletta.getFatturatoMateriaEnergia(), totali.get(CategoriaVoce.MATERIA_ENERGIA)));
        categorie.add(cat(CategoriaVoce.TRASPORTO, bolletta.getFatturatoTrasporto(), totali.get(CategoriaVoce.TRASPORTO)));
        categorie.add(cat(CategoriaVoce.ONERI_SISTEMA, bolletta.getFatturatoOneriSistema(), totali.get(CategoriaVoce.ONERI_SISTEMA)));
        categorie.add(cat(CategoriaVoce.IMPOSTE, bolletta.getFatturatoImposte(), totali.get(CategoriaVoce.IMPOSTE)));
        categorie.add(cat(CategoriaVoce.ALTRE_PARTITE, bolletta.getFatturatoAltrePartite(), totali.get(CategoriaVoce.ALTRE_PARTITE)));
        r.setCategorie(categorie);

        r.setGestoreImponibile(scala2(imponibile));
        r.setGestoreIva(scala2(iva));
        r.setGestoreTotale(scala2(totaleGestore));

        r.setConcorrenteImponibile(bolletta.getFatturatoImponibile());
        r.setConcorrenteIva(bolletta.getFatturatoIva());
        r.setConcorrenteTotale(bolletta.getFatturatoTotale());

        BigDecimal concorrenteTotale = bolletta.getFatturatoTotale() != null
                ? bolletta.getFatturatoTotale() : BigDecimal.ZERO;
        BigDecimal risparmioBim = concorrenteTotale.subtract(totaleGestore).setScale(2, RoundingMode.HALF_UP);

        // Stima annuale con indice di attendibilita (non un semplice x6 cieco)
        List<BigDecimal> consumiMese = bolletta.getMesi().stream()
                .map(it.simulatore.bollette.entity.MeseBolletta::getTotaleNetto).toList();
        AnalisiRisparmio.Stima stima = AnalisiRisparmio.calcola(risparmioBim, consumiMese,
                bolletta.getConsumoTotaleNetto(), bolletta.getConsumoAnnuoKwh());
        r.setRisparmioBimestrale(stima.risparmioBimestrale());
        r.setRisparmioAnnuale(stima.risparmioAnnualeStimato());
        r.setAttendibilitaStima(stima.attendibilita().name());
        r.setAvvertenzaRisparmio(stima.avvertenza());

        // Indicatori sintetici €/kWh medio lordo
        r.setPrezzoMedioLordoConcorrente(bolletta.getPrezzoMedioLordoKwh());
        BigDecimal kwhNetti = bolletta.getConsumoTotaleNetto();
        if (kwhNetti.compareTo(BigDecimal.ZERO) > 0) {
            r.setPrezzoMedioLordoGestore(totaleGestore.divide(kwhNetti, 6, RoundingMode.HALF_UP));
        }

        r.setMesi(buildMesi(bolletta, risultati));
        return r;
    }

    private List<MeseCalcolatoDTO> buildMesi(BollettaConcorrente bolletta, List<RisultatoMese> risultati) {
        List<MeseCalcolatoDTO> mesi = new ArrayList<>();
        for (int i = 0; i < risultati.size(); i++) {
            RisultatoMese rm = risultati.get(i);
            MeseBolletta mb = bolletta.getMesi().get(i);
            MeseCalcolatoDTO dto = new MeseCalcolatoDTO();
            dto.setNumeroMese(mb.getNumeroMese());
            dto.setNomeMese(mb.getNomeMese());
            dto.setKwhNetti(rm.getConsumi().getTotaleNetto());
            dto.setKwhPerdite(rm.getConsumi().getTotalePerdite());
            dto.setKwhConPerdite(rm.getConsumi().getTotaleConPerdite());
            dto.setTotaleMese(scala2(rm.getTotaleMese()));
            List<RigaCalcolataDTO> righe = new ArrayList<>();
            for (RigaCalcolata riga : rm.getRighe()) {
                righe.add(new RigaCalcolataDTO(riga.descrizione(), riga.unitaMisura(),
                        riga.corrispettivo(), riga.quantita(), scala2(riga.importo()),
                        riga.categoria().name(), riga.origine().name(), riga.origine().getDescrizione()));
            }
            dto.setRighe(righe);
            mesi.add(dto);
        }
        return mesi;
    }

    private CategoriaConfrontoDTO cat(CategoriaVoce categoria, BigDecimal fatturato, BigDecimal gestore) {
        BigDecimal f = fatturato != null ? fatturato : BigDecimal.ZERO;
        BigDecimal g = scala2(gestore != null ? gestore : BigDecimal.ZERO);
        BigDecimal diff = f.subtract(g).setScale(2, RoundingMode.HALF_UP);
        BigDecimal diffPct = f.compareTo(BigDecimal.ZERO) != 0
                ? diff.multiply(new BigDecimal("100")).divide(f, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        return new CategoriaConfrontoDTO(categoria.name(), categoria.getDescrizione(), f, g, diff, diffPct);
    }

    private Map<CategoriaVoce, BigDecimal> sommaPerCategoria(List<RisultatoMese> risultati) {
        Map<CategoriaVoce, BigDecimal> totali = new EnumMap<>(CategoriaVoce.class);
        for (CategoriaVoce cat : CategoriaVoce.values()) {
            totali.put(cat, BigDecimal.ZERO);
        }
        for (RisultatoMese rm : risultati) {
            for (CategoriaVoce cat : CategoriaVoce.values()) {
                totali.merge(cat, rm.getTotale(cat), BigDecimal::add);
            }
        }
        return totali;
    }

    private BigDecimal risolviAliquota(BollettaConcorrente bolletta, ParametriGestore parametri) {
        if (Boolean.TRUE.equals(parametri.getUsaAliquotaIvaBolletta())
                && bolletta.getAliquotaIvaApplicata() != null) {
            return bolletta.getAliquotaIvaApplicata();
        }
        return aliquotaDaTipoCliente(bolletta.getTipologiaCliente(), parametri);
    }

    private BigDecimal aliquotaDaTipoCliente(TipoCliente tipo, ParametriGestore parametri) {
        boolean nonDomestico = tipo != null && !tipo.isUsoDomestico();
        BigDecimal aliquota = nonDomestico ? parametri.getIvaNonDomestico() : parametri.getIvaDomestico();
        if (aliquota == null) {
            aliquota = parametri.getIvaDomestico();
        }
        return aliquota != null ? aliquota : new BigDecimal("0.10");
    }

    private ParametriGestore risolviParametri(Long id) {
        if (id != null) {
            return parametriRepo.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Parametri gestore", id));
        }
        return parametriRepo.findByPredefinitoTrue()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nessun profilo parametri predefinito configurato"));
    }

    private Confronto toEntity(ConfrontoResponse r, BollettaConcorrente bolletta,
                               Offerta offerta, ParametriGestore parametri) {
        Map<String, BigDecimal> byCat = new java.util.HashMap<>();
        for (CategoriaConfrontoDTO c : r.getCategorie()) {
            byCat.put(c.getCategoria(), c.getCalcolatoGestore());
        }
        return Confronto.builder()
                .bollettaConcorrenteId(bolletta.getId())
                .nomeConcorrente(bolletta.getNomeFornitore())
                .offertaGestoreId(offerta.getId())
                .nomeOffertaGestore(r.getNomeOffertaGestore())
                .parametriGestoreId(parametri.getId())
                .nomeProfiloParametri(parametri.getNomeProfilo())
                .gestoreMateriaEnergia(byCat.get(CategoriaVoce.MATERIA_ENERGIA.name()))
                .gestoreTrasporto(byCat.get(CategoriaVoce.TRASPORTO.name()))
                .gestoreOneriSistema(byCat.get(CategoriaVoce.ONERI_SISTEMA.name()))
                .gestoreImposte(byCat.get(CategoriaVoce.IMPOSTE.name()))
                .gestoreAltrePartite(byCat.get(CategoriaVoce.ALTRE_PARTITE.name()))
                .gestoreImponibile(r.getGestoreImponibile())
                .gestoreIva(r.getGestoreIva())
                .gestoreTotale(r.getGestoreTotale())
                .concorrenteTotale(r.getConcorrenteTotale())
                .risparmioBimestrale(r.getRisparmioBimestrale())
                .risparmioAnnuale(r.getRisparmioAnnuale())
                .build();
    }

    private BigDecimal scala2(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v.setScale(2, RoundingMode.HALF_UP);
    }
}
