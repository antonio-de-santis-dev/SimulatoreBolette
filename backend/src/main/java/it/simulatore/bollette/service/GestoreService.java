package it.simulatore.bollette.service;

import it.simulatore.bollette.calculation.RisultatoBolletta;
import it.simulatore.bollette.dto.*;
import it.simulatore.bollette.entity.Offerta;
import it.simulatore.bollette.entity.ParametriGestore;
import it.simulatore.bollette.exception.ResourceNotFoundException;
import it.simulatore.bollette.mapper.GestoreMapper;
import it.simulatore.bollette.mapper.OffertaMapper;
import it.simulatore.bollette.repository.OffertaRepository;
import it.simulatore.bollette.repository.ParametriGestoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service unificato dell'area "Il mio gestore" (prompt 1): presenta la
 * configurazione commerciale del gestore e le sue offerte come un tutt'uno.
 * Riusa {@link ParametriGestoreService} per predefinito/duplica/anteprima.
 *
 * NOTA: i parametri nazionali ARERA restano in ParametriGestore e non vengono
 * toccati qui (prompt 2).
 */
@Service
@RequiredArgsConstructor
public class GestoreService {

    private final ParametriGestoreRepository gestoreRepository;
    private final OffertaRepository offertaRepository;
    private final ParametriGestoreService parametriGestoreService;
    private final GestoreMapper gestoreMapper;
    private final OffertaMapper offertaMapper;

    // ── Gestori ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<GestoreListaResponse> lista() {
        return gestoreRepository.findAll().stream()
            .map(gestoreMapper::toLista)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GestoreDettaglioResponse dettaglio(Long id) {
        return gestoreMapper.toDettaglio(findGestore(id));
    }

    @Transactional
    public GestoreDettaglioResponse crea(GestoreDettaglioRequest req) {
        ParametriGestore gestore = gestoreMapper.toEntity(req);
        if (gestore.getNomeProfilo() == null || gestore.getNomeProfilo().isBlank()) {
            throw new IllegalArgumentException("Il nome profilo e obbligatorio");
        }
        boolean predefinito = Boolean.TRUE.equals(req.getPredefinito());
        gestore.setPredefinito(false);
        ParametriGestore salvato = gestoreRepository.save(gestore);
        if (predefinito) {
            parametriGestoreService.setPredefinito(salvato.getId());
        }
        return gestoreMapper.toDettaglio(findGestore(salvato.getId()));
    }

    /** Aggiornamento con modifica parziale: i campi null non sovrascrivono. */
    @Transactional
    public GestoreDettaglioResponse aggiorna(Long id, GestoreDettaglioRequest req) {
        ParametriGestore gestore = findGestore(id);
        gestoreMapper.updateEntity(req, gestore);
        gestoreRepository.save(gestore);
        if (Boolean.TRUE.equals(req.getPredefinito())) {
            parametriGestoreService.setPredefinito(id);
        }
        return gestoreMapper.toDettaglio(findGestore(id));
    }

    /**
     * Elimina il gestore. Con orphanRemoval=false le offerte NON sono cancellate
     * a cascata: vengono esplicitamente disassociate (rese orfane).
     */
    @Transactional
    public void elimina(Long id) {
        ParametriGestore gestore = findGestore(id);
        for (Offerta o : new ArrayList<>(gestore.getOfferte())) {
            o.setGestore(null);
            offertaRepository.save(o);
        }
        gestore.getOfferte().clear();
        gestoreRepository.delete(gestore);
    }

    @Transactional
    public GestoreDettaglioResponse duplica(Long id) {
        ParametriGestore copia = parametriGestoreService.duplica(id); // predefinito=false garantito
        return gestoreMapper.toDettaglio(findGestore(copia.getId()));
    }

    @Transactional
    public GestoreDettaglioResponse impostaPredefinito(Long id) {
        ParametriGestore p = parametriGestoreService.setPredefinito(id);
        return gestoreMapper.toDettaglio(findGestore(p.getId()));
    }

    @Transactional(readOnly = true)
    public RisultatoBolletta anteprima(Long id) {
        return parametriGestoreService.anteprima(id);
    }

    // ── Offerte come sotto-risorsa ───────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<OffertaResponse> offerteDelGestore(Long gestoreId) {
        return findGestore(gestoreId).getOfferte().stream()
            .map(offertaMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public OffertaResponse aggiungiOfferta(Long gestoreId, OffertaRequest req) {
        ParametriGestore gestore = findGestore(gestoreId);
        Offerta offerta = offertaMapper.toEntity(req);
        offerta.setGestore(gestore);
        Offerta salvata = offertaRepository.save(offerta);
        gestore.getOfferte().add(salvata);
        return offertaMapper.toResponse(salvata);
    }

    @Transactional
    public OffertaResponse aggiornaOfferta(Long gestoreId, Long offertaId, OffertaRequest req) {
        Offerta offerta = findOffertaDelGestore(gestoreId, offertaId);
        offertaMapper.updateEntity(req, offerta);
        return offertaMapper.toResponse(offertaRepository.save(offerta));
    }

    @Transactional
    public void eliminaOfferta(Long gestoreId, Long offertaId) {
        Offerta offerta = findOffertaDelGestore(gestoreId, offertaId);
        ParametriGestore gestore = offerta.getGestore();
        if (gestore != null) {
            gestore.getOfferte().remove(offerta);
        }
        offertaRepository.delete(offerta);
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private ParametriGestore findGestore(Long id) {
        return gestoreRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Gestore", id));
    }

    private Offerta findOffertaDelGestore(Long gestoreId, Long offertaId) {
        Offerta offerta = offertaRepository.findById(offertaId)
            .orElseThrow(() -> new ResourceNotFoundException("Offerta", offertaId));
        Long ownerId = offerta.getGestore() != null ? offerta.getGestore().getId() : null;
        if (ownerId == null || !ownerId.equals(gestoreId)) {
            throw new ResourceNotFoundException(
                "L'offerta " + offertaId + " non appartiene al gestore " + gestoreId);
        }
        return offerta;
    }
}
