package it.simulatore.bollette.exception;

/**
 * Risorsa non trovata → HTTP 404. Distinta dalle altre RuntimeException, che diventano 500.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String risorsa, Object id) {
        super(risorsa + " non trovata con id " + id);
    }
}
