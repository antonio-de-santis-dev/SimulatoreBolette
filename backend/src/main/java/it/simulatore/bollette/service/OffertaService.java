package it.simulatore.bollette.service;

import it.simulatore.bollette.dto.OffertaRequest;
import it.simulatore.bollette.dto.OffertaResponse;
import it.simulatore.bollette.entity.Offerta;
import it.simulatore.bollette.mapper.OffertaMapper;
import it.simulatore.bollette.repository.OffertaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OffertaService {

    private final OffertaRepository offertaRepository;
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
        Offerta offerta = mapper.toEntity(request);
        return mapper.toResponse(offertaRepository.save(offerta));
    }

    @Transactional
    public OffertaResponse aggiornaOfferta(Long id, OffertaRequest request) {
        Offerta existing = offertaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Offerta non trovata"));
        mapper.updateEntity(request, existing);
        return mapper.toResponse(offertaRepository.save(existing));
    }

    @Transactional
    public void deleteOfferta(Long id) {
        offertaRepository.deleteById(id);
    }
}
