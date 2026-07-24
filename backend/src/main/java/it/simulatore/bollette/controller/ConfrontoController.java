package it.simulatore.bollette.controller;

import it.simulatore.bollette.dto.ConfrontoRequest;
import it.simulatore.bollette.dto.ConfrontoResponse;
import it.simulatore.bollette.entity.Confronto;
import it.simulatore.bollette.service.ConfrontoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** API del confronto: kWh del concorrente ricalcolati con i corrispettivi del gestore. */
@RestController
@RequestMapping("/api/confronti")
@RequiredArgsConstructor
@CrossOrigin
public class ConfrontoController {

    private final ConfrontoService service;

    @PostMapping
    public ConfrontoResponse confronta(@Valid @RequestBody ConfrontoRequest request) {
        return service.confronta(request);
    }

    @PostMapping("/multiplo")
    public List<ConfrontoResponse> confrontaMultiplo(@RequestBody Map<String, Object> body) {
        Long bollettaId = ((Number) body.get("bollettaConcorrenteId")).longValue();
        Long parametriId = body.get("parametriGestoreId") != null
                ? ((Number) body.get("parametriGestoreId")).longValue() : null;
        @SuppressWarnings("unchecked")
        List<Number> offerte = (List<Number>) body.get("offerteGestoreIds");
        List<Long> offerteIds = offerte.stream().map(Number::longValue).toList();
        return service.confrontaMultiplo(bollettaId, offerteIds, parametriId);
    }

    @GetMapping
    public List<Confronto> storico() {
        return service.storico();
    }

    @GetMapping("/{id}")
    public Confronto dettaglio(@PathVariable Long id) {
        return service.getById(id);
    }
}
