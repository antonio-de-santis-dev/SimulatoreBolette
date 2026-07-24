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
    Simulazione toEntity(SimulazioneRequest request, SimulazioneResponse response);

    SimulazioneResponse toResponse(Simulazione entity);
}
