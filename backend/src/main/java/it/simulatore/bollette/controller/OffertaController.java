package it.simulatore.bollette.controller;

import it.simulatore.bollette.dto.OffertaRequest;
import it.simulatore.bollette.dto.OffertaResponse;
import it.simulatore.bollette.service.OffertaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offerte")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class OffertaController {

    private final OffertaService offertaService;

    @GetMapping
    public ResponseEntity<List<OffertaResponse>> getAllOfferte() {
        return ResponseEntity.ok(offertaService.getAllOfferte());
    }

    @GetMapping("/attive")
    public ResponseEntity<List<OffertaResponse>> getOfferteAttive() {
        return ResponseEntity.ok(offertaService.getOfferteAttive());
    }

    @PostMapping
    public ResponseEntity<OffertaResponse> creaOfferta(@Valid @RequestBody OffertaRequest request) {
        return new ResponseEntity<>(offertaService.creaOfferta(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OffertaResponse> aggiornaOfferta(@PathVariable Long id, @Valid @RequestBody OffertaRequest request) {
        return ResponseEntity.ok(offertaService.aggiornaOfferta(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOfferta(@PathVariable Long id) {
        offertaService.deleteOfferta(id);
        return ResponseEntity.noContent().build();
    }
}
