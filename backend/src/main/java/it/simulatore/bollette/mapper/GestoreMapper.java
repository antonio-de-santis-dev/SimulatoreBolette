package it.simulatore.bollette.mapper;

import it.simulatore.bollette.dto.GestoreDettaglioRequest;
import it.simulatore.bollette.dto.GestoreDettaglioResponse;
import it.simulatore.bollette.dto.GestoreListaResponse;
import it.simulatore.bollette.entity.ParametriGestore;
import org.mapstruct.*;

/**
 * Mapper del gestore. Espone solo la parte commerciale/anagrafica + offerte:
 * i parametri nazionali ARERA presenti in ParametriGestore NON vengono toccati
 * (unmappedTargetPolicy IGNORE) ne' sovrascritti in aggiornamento parziale.
 */
@Mapper(componentModel = "spring", uses = OffertaMapper.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GestoreMapper {

    @Mapping(target = "numeroOfferte",
        expression = "java(entity.getOfferte() == null ? 0 : entity.getOfferte().size())")
    GestoreDettaglioResponse toDettaglio(ParametriGestore entity);

    @Mapping(target = "numeroOfferte",
        expression = "java(entity.getOfferte() == null ? 0 : entity.getOfferte().size())")
    GestoreListaResponse toLista(ParametriGestore entity);

    ParametriGestore toEntity(GestoreDettaglioRequest request);

    /** Aggiornamento con MERGE parziale: i campi null nella request non sovrascrivono. */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(GestoreDettaglioRequest request, @MappingTarget ParametriGestore entity);
}
