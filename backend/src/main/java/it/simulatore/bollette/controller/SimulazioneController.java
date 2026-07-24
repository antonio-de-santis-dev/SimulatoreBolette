package it.simulatore.bollette.controller;

import it.simulatore.bollette.dto.*;
import it.simulatore.bollette.service.SimulazioneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/simulazioni")
@RequiredArgsConstructor
public class SimulazioneController {

    private final SimulazioneService simulazioneService;

    @PostMapping
    public ResponseEntity<SimulazioneResponse> creaSimulazione(@Valid @RequestBody SimulazioneRequest request) {
        return new ResponseEntity<>(simulazioneService.creaSimulazione(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SimulazioneResponse>> getAllSimulazioni() {
        return ResponseEntity.ok(simulazioneService.getAllSimulazioni());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SimulazioneResponse> getSimulazione(@PathVariable Long id) {
        return ResponseEntity.ok(simulazioneService.getSimulazione(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSimulazione(@PathVariable Long id) {
        simulazioneService.deleteSimulazione(id);
        return ResponseEntity.noContent().build();
    }
}
