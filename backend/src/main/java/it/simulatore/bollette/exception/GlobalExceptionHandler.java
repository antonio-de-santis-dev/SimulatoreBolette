package it.simulatore.bollette.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Percorsi tecnici: un client API non deve mai ricevere index.html al posto di JSON.
    private static final String[] PERCORSI_TECNICI = {
        "/api", "/actuator", "/swagger-ui", "/api-docs", "/v3/api-docs"
    };

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("errors", errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Gestisce le risorse statiche non trovate, che in Spring Boot 3.2 arrivano come
     * NoResourceFoundException. E' il fulcro del routing SPA:
     *
     *  - percorso tecnico (/api, /actuator, ...)  -> vero 404 JSON
     *  - qualsiasi altro percorso (rotta React)   -> index.html, cosi' il refresh
     *    su /simulatore o /confronto funziona invece di dare 404/500.
     *
     * Senza questo, il catch-all su Exception trasformava ogni risorsa mancante
     * (incluso vite.svg) in un 500.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleRisorsaNonTrovata(NoResourceFoundException ex, HttpServletRequest request) {
        String path = request.getRequestURI();

        if (isPercorsoTecnico(path)) {
            Map<String, Object> body = new HashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("status", HttpStatus.NOT_FOUND.value());
            body.put("path", path);
            body.put("message", "Risorsa non trovata");
            return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        }

        Resource index = new ClassPathResource("static/index.html");
        if (!index.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(index);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("message", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("message", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean isPercorsoTecnico(String path) {
        if (path == null) return false;
        for (String p : PERCORSI_TECNICI) {
            if (path.startsWith(p)) return true;
        }
        return false;
    }
}
