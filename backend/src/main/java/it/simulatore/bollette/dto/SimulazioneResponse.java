package it.simulatore.bollette.dto;

import it.simulatore.bollette.enums.PotenzaContrattuale;
import it.simulatore.bollette.enums.TipoCliente;
import it.simulatore.bollette.enums.TipoTariffa;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SimulazioneResponse {
    private Long id;
    private String nome;
    private TipoCliente tipoCliente;
    private PotenzaContrattuale potenzaContrattuale;
    private TipoTariffa tipoTariffa;

    private BigDecimal consumoTotaleKwh;
    private BigDecimal consumoF1;
    private BigDecimal consumoF2;
    private BigDecimal consumoF3;

    private BigDecimal spesaMateriaEnergia;
    private BigDecimal spesaTrasporto;
    private BigDecimal spesaOneriSistema;
    private BigDecimal spesaAccise;
    private BigDecimal spesaIva;
    private BigDecimal totaleBimestrale;
    private BigDecimal totaleAnnuale;
    private BigDecimal prezzoMedioKwh;

    private BigDecimal pctMateriaEnergia;
    private BigDecimal pctTrasporto;
    private BigDecimal pctOneriSistema;
    private BigDecimal pctAccise;
    private BigDecimal pctIva;

    private List<ConfrontoOffertaDTO> confrontoOfferte;

    private LocalDateTime createdAt;
}
