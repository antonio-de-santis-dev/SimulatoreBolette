package it.simulatore.bollette.controller;

import it.simulatore.bollette.calculation.RisultatoBolletta;
import it.simulatore.bollette.entity.ParametriGestore;
import it.simulatore.bollette.service.ParametriGestoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** API per la SORGENTE "B": i parametri del gestore (area configurazione). */
@RestController
@RequestMapping("/api/parametri-gestore")
@RequiredArgsConstructor
public class ParametriGestoreController {

    private final ParametriGestoreService service;

    @GetMapping
    public List<ParametriGestore> lista() {
        return service.findAll();
    }

    @GetMapping("/predefinito")
    public ParametriGestore predefinito() {
        return service.findPredefinito();
    }

    @GetMapping("/{id}")
    public ParametriGestore dettaglio(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<ParametriGestore> crea(@RequestBody ParametriGestore p) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(p));
    }

    @PutMapping("/{id}")
    public ParametriGestore aggiorna(@PathVariable Long id, @RequestBody ParametriGestore p) {
        return service.update(id, p);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> elimina(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/predefinito")
    public ParametriGestore impostaPredefinito(@PathVariable Long id) {
        return service.setPredefinito(id);
    }

    @PostMapping("/{id}/duplica")
    public ResponseEntity<ParametriGestore> duplica(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.duplica(id));
    }

    @GetMapping("/{id}/anteprima")
    public RisultatoBolletta anteprima(@PathVariable Long id) {
        return service.anteprima(id);
    }
}
