package it.simulatore.bollette.dto;

import it.simulatore.bollette.enums.PotenzaContrattuale;
import it.simulatore.bollette.enums.TipoCliente;
import it.simulatore.bollette.enums.TipoTariffa;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SimulazioneRequest {

    @NotBlank(message = "Il nome della simulazione e obbligatorio")
    @Size(max = 100)
    private String nome;

    @NotNull(message = "Il tipo cliente e obbligatorio")
    private TipoCliente tipoCliente;

    @NotNull(message = "La potenza contrattuale e obbligatoria")
    private PotenzaContrattuale potenzaContrattuale;

    @NotNull(message = "Il consumo totale e obbligatorio")
    @Positive(message = "Il consumo deve essere positivo")
    @Digits(integer = 6, fraction = 2)
    private BigDecimal consumoTotaleKwh;

    @NotNull(message = "Il tipo tariffa e obbligatorio")
    private TipoTariffa tipoTariffa;

    @Digits(integer = 6, fraction = 2)
    private BigDecimal consumoF1;

    @Digits(integer = 6, fraction = 2)
    private BigDecimal consumoF2;

    @Digits(integer = 6, fraction = 2)
    private BigDecimal consumoF3;

    private Integer meseRiferimento1;
    private Integer annoRiferimento1;
    private Integer meseRiferimento2;
    private Integer annoRiferimento2;

    private boolean confrontaOfferte = false;
}
