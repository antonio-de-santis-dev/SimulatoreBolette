package it.simulatore.bollette.controller;

import it.simulatore.bollette.calculation.RisultatoBolletta;
import it.simulatore.bollette.dto.*;
import it.simulatore.bollette.service.GestoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller unificato dell'area "Il mio gestore" (prompt 1): anagrafica +
 * configurazione commerciale + offerte (sotto-risorsa) in un'unica API.
 */
@RestController
@RequestMapping("/api/gestori")
@RequiredArgsConstructor
public class GestoreController {

    private final GestoreService gestoreService;

    @GetMapping
    public List<GestoreListaResponse> lista() {
        return gestoreService.lista();
    }

    @GetMapping("/{id}")
    public GestoreDettaglioResponse dettaglio(@PathVariable Long id) {
        return gestoreService.dettaglio(id);
    }

    @PostMapping
    public ResponseEntity<GestoreDettaglioResponse> crea(@RequestBody GestoreDettaglioRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gestoreService.crea(req));
    }

    @PutMapping("/{id}")
    public GestoreDettaglioResponse aggiorna(@PathVariable Long id,
                                             @RequestBody GestoreDettaglioRequest req) {
        return gestoreService.aggiorna(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> elimina(@PathVariable Long id) {
        gestoreService.elimina(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/predefinito")
    public GestoreDettaglioResponse impostaPredefinito(@PathVariable Long id) {
        return gestoreService.impostaPredefinito(id);
    }

    @PostMapping("/{id}/duplica")
    public ResponseEntity<GestoreDettaglioResponse> duplica(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gestoreService.duplica(id));
    }

    @GetMapping("/{id}/anteprima")
    public RisultatoBolletta anteprima(@PathVariable Long id) {
        return gestoreService.anteprima(id);
    }

    // ── Offerte come sotto-risorsa ───────────────────────────────────────────

    @GetMapping("/{id}/offerte")
    public List<OffertaResponse> offerte(@PathVariable Long id) {
        return gestoreService.offerteDelGestore(id);
    }

    @PostMapping("/{id}/offerte")
    public ResponseEntity<OffertaResponse> aggiungiOfferta(@PathVariable Long id,
                                                           @Valid @RequestBody OffertaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gestoreService.aggiungiOfferta(id, req));
    }

    @PutMapping("/{id}/offerte/{offertaId}")
    public OffertaResponse aggiornaOfferta(@PathVariable Long id, @PathVariable Long offertaId,
                                           @Valid @RequestBody OffertaRequest req) {
        return gestoreService.aggiornaOfferta(id, offertaId, req);
    }

    @DeleteMapping("/{id}/offerte/{offertaId}")
    public ResponseEntity<Void> eliminaOfferta(@PathVariable Long id, @PathVariable Long offertaId) {
        gestoreService.eliminaOfferta(id, offertaId);
        return ResponseEntity.noContent().build();
    }
}
