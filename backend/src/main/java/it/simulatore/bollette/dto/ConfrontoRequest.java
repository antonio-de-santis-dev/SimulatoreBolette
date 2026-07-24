package it.simulatore.bollette.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConfrontoRequest {

    @NotNull(message = "La bolletta concorrente e' obbligatoria")
    private Long bollettaConcorrenteId;

    @NotNull(message = "L'offerta del gestore e' obbligatoria")
    private Long offertaGestoreId;

    /** Se null si usa il profilo parametri predefinito. */
    private Long parametriGestoreId;

    /** Se true il confronto viene salvato nello storico. */
    private boolean salva = true;
}
