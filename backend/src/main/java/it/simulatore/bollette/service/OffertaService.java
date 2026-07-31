package it.simulatore.bollette.service;

import it.simulatore.bollette.dto.OffertaRequest;
import it.simulatore.bollette.dto.OffertaResponse;
import it.simulatore.bollette.entity.Offerta;
import it.simulatore.bollette.entity.ParametriGestore;
import it.simulatore.bollette.mapper.OffertaMapper;
import it.simulatore.bollette.repository.OffertaRepository;
import it.simulatore.bollette.repository.ParametriGestoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @deprecated In dismissione a favore di {@link GestoreService} e degli endpoint
 * {@code /api/gestori}. Mantenuto per retrocompatibilita' (Confronto e
 * Simulatore usano {@code /api/offerte}); le scritture delegano al nuovo
 * {@link GestoreService}, associando le offerte al gestore predefinito.
 */
@Deprecated
@Service
@RequiredArgsConstructor
public class OffertaService {

    private final OffertaRepository offertaRepository;
    private final ParametriGestoreRepository gestoreRepository;
    private final GestoreService gestoreService;
    private final OffertaMapper mapper;

    @Transactional(readOnly = true)
    public List<OffertaResponse> getAllOfferte() {
        return offertaRepository.findAll().stream()
            .map(mapper::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OffertaResponse> getOfferteAttive() {
        return offertaRepository.findByAttivaTrue().stream()
            .map(mapper::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public OffertaResponse creaOfferta(OffertaRequest request) {
        // Delega al nuovo service: associa l'offerta al gestore predefinito.
        ParametriGestore predefinito = gestoreRepository.findByPredefinitoTrue().orElse(null);
        if (predefinito != null) {
            return gestoreService.aggiungiOfferta(predefinito.getId(), request);
        }
        // Fallback (nessun gestore predefinito): offerta orfana.
        Offerta offerta = mapper.toEntity(request);
        return mapper.toResponse(offertaRepository.save(offerta));
    }

    @Transactional
    public OffertaResponse aggiornaOfferta(Long id, OffertaRequest request) {
        Offerta existing = offertaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Offerta non trovata"));
        if (existing.getGestore() != null) {
            return gestoreService.aggiornaOfferta(existing.getGestore().getId(), id, request);
        }
        mapper.updateEntity(request, existing);
        return mapper.toResponse(offertaRepository.save(existing));
    }

    @Transactional
    public void deleteOfferta(Long id) {
        Offerta existing = offertaRepository.findById(id).orElse(null);
        if (existing != null && existing.getGestore() != null) {
            gestoreService.eliminaOfferta(existing.getGestore().getId(), id);
            return;
        }
        offertaRepository.deleteById(id);
    }
}
