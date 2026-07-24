package it.simulatore.bollette.mapper;

import it.simulatore.bollette.dto.OffertaRequest;
import it.simulatore.bollette.dto.OffertaResponse;
import it.simulatore.bollette.entity.Offerta;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-24T09:27:06+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Ubuntu)"
)
@Component
public class OffertaMapperImpl implements OffertaMapper {

    @Override
    public Offerta toEntity(OffertaRequest request) {
        if ( request == null ) {
            return null;
        }

        Offerta.OffertaBuilder offerta = Offerta.builder();

        offerta.nomeFornitore( request.getNomeFornitore() );
        offerta.nomeOfferta( request.getNomeOfferta() );
        offerta.tipoOfferta( request.getTipoOfferta() );
        offerta.tipoTariffa( request.getTipoTariffa() );
        offerta.prezzoFissoF0( request.getPrezzoFissoF0() );
        offerta.prezzoFissoF1( request.getPrezzoFissoF1() );
        offerta.prezzoFissoF23( request.getPrezzoFissoF23() );
        offerta.prezzoFissoF2( request.getPrezzoFissoF2() );
        offerta.prezzoFissoF3( request.getPrezzoFissoF3() );
        offerta.spreadPunF0( request.getSpreadPunF0() );
        offerta.spreadPunF1( request.getSpreadPunF1() );
        offerta.spreadPunF23( request.getSpreadPunF23() );
        offerta.spreadPunF2( request.getSpreadPunF2() );
        offerta.spreadPunF3( request.getSpreadPunF3() );
        offerta.pcvAnnuo( request.getPcvAnnuo() );
        offerta.condizioniSpeciali( request.getCondizioniSpeciali() );

        offerta.attiva( true );

        return offerta.build();
    }

    @Override
    public OffertaResponse toResponse(Offerta entity) {
        if ( entity == null ) {
            return null;
        }

        OffertaResponse offertaResponse = new OffertaResponse();

        offertaResponse.setId( entity.getId() );
        offertaResponse.setNomeFornitore( entity.getNomeFornitore() );
        offertaResponse.setNomeOfferta( entity.getNomeOfferta() );
        offertaResponse.setTipoOfferta( entity.getTipoOfferta() );
        offertaResponse.setTipoTariffa( entity.getTipoTariffa() );
        offertaResponse.setPrezzoFissoF0( entity.getPrezzoFissoF0() );
        offertaResponse.setPrezzoFissoF1( entity.getPrezzoFissoF1() );
        offertaResponse.setPrezzoFissoF23( entity.getPrezzoFissoF23() );
        offertaResponse.setSpreadPunF0( entity.getSpreadPunF0() );
        offertaResponse.setSpreadPunF1( entity.getSpreadPunF1() );
        offertaResponse.setSpreadPunF23( entity.getSpreadPunF23() );
        offertaResponse.setPcvAnnuo( entity.getPcvAnnuo() );
        offertaResponse.setAttiva( entity.getAttiva() );
        offertaResponse.setCreatedAt( entity.getCreatedAt() );

        return offertaResponse;
    }

    @Override
    public void updateEntity(OffertaRequest request, Offerta entity) {
        if ( request == null ) {
            return;
        }

        entity.setNomeFornitore( request.getNomeFornitore() );
        entity.setNomeOfferta( request.getNomeOfferta() );
        entity.setTipoOfferta( request.getTipoOfferta() );
        entity.setTipoTariffa( request.getTipoTariffa() );
        entity.setPrezzoFissoF0( request.getPrezzoFissoF0() );
        entity.setPrezzoFissoF1( request.getPrezzoFissoF1() );
        entity.setPrezzoFissoF23( request.getPrezzoFissoF23() );
        entity.setPrezzoFissoF2( request.getPrezzoFissoF2() );
        entity.setPrezzoFissoF3( request.getPrezzoFissoF3() );
        entity.setSpreadPunF0( request.getSpreadPunF0() );
        entity.setSpreadPunF1( request.getSpreadPunF1() );
        entity.setSpreadPunF23( request.getSpreadPunF23() );
        entity.setSpreadPunF2( request.getSpreadPunF2() );
        entity.setSpreadPunF3( request.getSpreadPunF3() );
        entity.setPcvAnnuo( request.getPcvAnnuo() );
        entity.setCondizioniSpeciali( request.getCondizioniSpeciali() );
    }
}
