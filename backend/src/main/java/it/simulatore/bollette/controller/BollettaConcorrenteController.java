package it.simulatore.bollette.controller;

import it.simulatore.bollette.entity.BollettaConcorrente;
import it.simulatore.bollette.service.BollettaConcorrenteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** API per la SORGENTE "A": le bollette da comparare (dati del concorrente). */
@RestController
@RequestMapping("/api/bollette-concorrenti")
@RequiredArgsConstructor
public class BollettaConcorrenteController {

    private final BollettaConcorrenteService service;

    @GetMapping
    public List<BollettaConcorrente> lista() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public BollettaConcorrente dettaglio(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<BollettaConcorrente> crea(@RequestBody BollettaConcorrente bolletta) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(bolletta));
    }

    @PutMapping("/{id}")
    public BollettaConcorrente aggiorna(@PathVariable Long id, @RequestBody BollettaConcorrente bolletta) {
        return service.update(id, bolletta);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> elimina(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/duplica")
    public ResponseEntity<BollettaConcorrente> duplica(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.duplica(id));
    }

    @GetMapping("/{id}/quadratura")
    public Map<String, Object> quadratura(@PathVariable Long id) {
        boolean valida = service.quadratura(id);
        return Map.of("valida", valida);
    }
}
