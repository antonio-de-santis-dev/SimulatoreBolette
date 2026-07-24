package it.simulatore.bollette.mapper;

import it.simulatore.bollette.dto.SimulazioneRequest;
import it.simulatore.bollette.dto.SimulazioneResponse;
import it.simulatore.bollette.entity.Simulazione;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-24T09:22:38+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Ubuntu)"
)
@Component
public class SimulazioneMapperImpl implements SimulazioneMapper {

    @Override
    public Simulazione toEntity(SimulazioneRequest request, SimulazioneResponse response) {
        if ( request == null && response == null ) {
            return null;
        }

        Simulazione.SimulazioneBuilder simulazione = Simulazione.builder();

        if ( request != null ) {
            simulazione.nome( request.getNome() );
            simulazione.tipoCliente( request.getTipoCliente() );
            simulazione.potenzaContrattuale( request.getPotenzaContrattuale() );
            simulazione.consumoTotaleKwh( request.getConsumoTotaleKwh() );
            simulazione.consumoF1( request.getConsumoF1() );
            simulazione.consumoF2( request.getConsumoF2() );
            simulazione.consumoF3( request.getConsumoF3() );
        }
        if ( response != null ) {
            simulazione.spesaMateriaEnergia( response.getSpesaMateriaEnergia() );
            simulazione.spesaTrasporto( response.getSpesaTrasporto() );
            simulazione.spesaOneriSistema( response.getSpesaOneriSistema() );
            simulazione.spesaAccise( response.getSpesaAccise() );
            simulazione.spesaIva( response.getSpesaIva() );
            simulazione.totaleBimestrale( response.getTotaleBimestrale() );
            simulazione.totaleAnnuale( response.getTotaleAnnuale() );
            simulazione.prezzoMedioKwh( response.getPrezzoMedioKwh() );
        }

        return simulazione.build();
    }

    @Override
    public SimulazioneResponse toResponse(Simulazione entity) {
        if ( entity == null ) {
            return null;
        }

        SimulazioneResponse simulazioneResponse = new SimulazioneResponse();

        simulazioneResponse.setId( entity.getId() );
        simulazioneResponse.setNome( entity.getNome() );
        simulazioneResponse.setTipoCliente( entity.getTipoCliente() );
        simulazioneResponse.setPotenzaContrattuale( entity.getPotenzaContrattuale() );
        simulazioneResponse.setConsumoTotaleKwh( entity.getConsumoTotaleKwh() );
        simulazioneResponse.setConsumoF1( entity.getConsumoF1() );
        simulazioneResponse.setConsumoF2( entity.getConsumoF2() );
        simulazioneResponse.setConsumoF3( entity.getConsumoF3() );
        simulazioneResponse.setSpesaMateriaEnergia( entity.getSpesaMateriaEnergia() );
        simulazioneResponse.setSpesaTrasporto( entity.getSpesaTrasporto() );
        simulazioneResponse.setSpesaOneriSistema( entity.getSpesaOneriSistema() );
        simulazioneResponse.setSpesaAccise( entity.getSpesaAccise() );
        simulazioneResponse.setSpesaIva( entity.getSpesaIva() );
        simulazioneResponse.setTotaleBimestrale( entity.getTotaleBimestrale() );
        simulazioneResponse.setTotaleAnnuale( entity.getTotaleAnnuale() );
        simulazioneResponse.setPrezzoMedioKwh( entity.getPrezzoMedioKwh() );
        simulazioneResponse.setCreatedAt( entity.getCreatedAt() );

        return simulazioneResponse;
    }
}
