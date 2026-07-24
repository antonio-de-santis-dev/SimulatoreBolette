package it.simulatore.bollette.mapper;

import it.simulatore.bollette.dto.OffertaRequest;
import it.simulatore.bollette.dto.OffertaResponse;
import it.simulatore.bollette.entity.Offerta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface OffertaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "attiva", constant = "true")
    Offerta toEntity(OffertaRequest request);

    OffertaResponse toResponse(Offerta entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(OffertaRequest request, @MappingTarget Offerta entity);
}
