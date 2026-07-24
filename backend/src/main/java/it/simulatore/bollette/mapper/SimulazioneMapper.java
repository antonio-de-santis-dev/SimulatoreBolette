package it.simulatore.bollette.mapper;

import it.simulatore.bollette.dto.SimulazioneRequest;
import it.simulatore.bollette.dto.SimulazioneResponse;
import it.simulatore.bollette.entity.Simulazione;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SimulazioneMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    // I campi di input arrivano dal request, quelli calcolati dal response:
    // disambiguazione esplicita per le proprieta' presenti in entrambe le sorgenti.
    @Mapping(target = "nome", source = "request.nome")
    @Mapping(target = "tipoCliente", source = "request.tipoCliente")
    @Mapping(target = "potenzaContrattuale", source = "request.potenzaContrattuale")
    @Mapping(target = "consumoTotaleKwh", source = "request.consumoTotaleKwh")
    @Mapping(target = "consumoF1", source = "request.consumoF1")
    @Mapping(target = "consumoF2", source = "request.consumoF2")
    @Mapping(target = "consumoF3", source = "request.consumoF3")
    Simulazione toEntity(SimulazioneRequest request, SimulazioneResponse response);

    SimulazioneResponse toResponse(Simulazione entity);
}
