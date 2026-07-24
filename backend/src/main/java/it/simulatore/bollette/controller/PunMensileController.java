package it.simulatore.bollette.controller;

import it.simulatore.bollette.entity.PunMensile;
import it.simulatore.bollette.repository.PunMensileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pun")
@RequiredArgsConstructor
public class PunMensileController {

    private final PunMensileRepository punRepo;

    @GetMapping
    public ResponseEntity<List<PunMensile>> getAll() {
        return ResponseEntity.ok(punRepo.findAll());
    }

    @PostMapping
    public ResponseEntity<PunMensile> save(@RequestBody PunMensile pun) {
        return ResponseEntity.ok(punRepo.save(pun));
    }
}
