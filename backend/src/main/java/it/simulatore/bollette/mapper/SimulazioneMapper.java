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
    @Mapping(source = "request.nome", target = "nome")
    @Mapping(source = "request.tipoCliente", target = "tipoCliente")
    @Mapping(source = "request.potenzaContrattuale", target = "potenzaContrattuale")
    @Mapping(source = "request.consumoTotaleKwh", target = "consumoTotaleKwh")
    @Mapping(source = "request.consumoF1", target = "consumoF1")
    @Mapping(source = "request.consumoF2", target = "consumoF2")
    @Mapping(source = "request.consumoF3", target = "consumoF3")
    Simulazione toEntity(SimulazioneRequest request, SimulazioneResponse response);

    SimulazioneResponse toResponse(Simulazione entity);
}
