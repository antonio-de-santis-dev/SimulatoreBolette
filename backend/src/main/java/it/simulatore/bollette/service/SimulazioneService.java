package it.simulatore.bollette.service;

import it.simulatore.bollette.calculation.BollettaCalculator;
import it.simulatore.bollette.dto.SimulazioneRequest;
import it.simulatore.bollette.dto.SimulazioneResponse;
import it.simulatore.bollette.entity.Simulazione;
import it.simulatore.bollette.mapper.SimulazioneMapper;
import it.simulatore.bollette.repository.SimulazioneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SimulazioneService {

    private final SimulazioneRepository simulazioneRepository;
    private final BollettaCalculator calculator;
    private final SimulazioneMapper mapper;

    @Transactional
    public SimulazioneResponse creaSimulazione(SimulazioneRequest request) {
        SimulazioneResponse response = calculator.calcolaSimulazione(request);

        Simulazione entity = mapper.toEntity(request, response);
        Simulazione saved = simulazioneRepository.save(entity);
        response.setId(saved.getId());

        return response;
    }

    @Transactional(readOnly = true)
    public List<SimulazioneResponse> getAllSimulazioni() {
        return simulazioneRepository.findTop10ByOrderByCreatedAtDesc()
            .stream()
            .map(mapper::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SimulazioneResponse getSimulazione(Long id) {
        Simulazione sim = simulazioneRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Simulazione non trovata: " + id));
        return mapper.toResponse(sim);
    }

    @Transactional
    public void deleteSimulazione(Long id) {
        simulazioneRepository.deleteById(id);
    }
}
