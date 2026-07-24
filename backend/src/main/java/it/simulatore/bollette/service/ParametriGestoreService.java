package it.simulatore.bollette.service;

import it.simulatore.bollette.calculation.MotoreCalcolo;
import it.simulatore.bollette.calculation.RisultatoBolletta;
import it.simulatore.bollette.calculation.RisultatoMese;
import it.simulatore.bollette.entity.MeseBolletta;
import it.simulatore.bollette.entity.ParametriGestore;
import it.simulatore.bollette.entity.ScaglioneConsumo;
import it.simulatore.bollette.exception.ResourceNotFoundException;
import it.simulatore.bollette.repository.ParametriGestoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParametriGestoreService {

    private final ParametriGestoreRepository repository;
    private final MotoreCalcolo motore;

    public List<ParametriGestore> findAll() {
        return repository.findAll();
    }

    public ParametriGestore findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parametri gestore", id));
    }

    public ParametriGestore findPredefinito() {
        return repository.findByPredefinitoTrue()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nessun profilo parametri predefinito configurato"));
    }

    @Transactional
    public ParametriGestore create(ParametriGestore p) {
        p.setId(null);
        if (Boolean.TRUE.equals(p.getPredefinito())) {
            azzeraPredefiniti();
        }
        return repository.save(p);
    }

    @Transactional
    public ParametriGestore update(Long id, ParametriGestore modifiche) {
        ParametriGestore esistente = findById(id);
        modifiche.setId(id);
        modifiche.setCreatedAt(esistente.getCreatedAt());
        if (Boolean.TRUE.equals(modifiche.getPredefinito())) {
            azzeraPredefiniti();
        }
        return repository.save(modifiche);
    }

    @Transactional
    public void delete(Long id) {
        ParametriGestore p = findById(id);
        repository.delete(p);
    }

    /** Imposta un profilo come predefinito, garantendo che sia l'unico. */
    @Transactional
    public ParametriGestore setPredefinito(Long id) {
        ParametriGestore p = findById(id);
        azzeraPredefiniti();
        p.setPredefinito(true);
        return repository.save(p);
    }

    @Transactional
    public ParametriGestore duplica(Long id) {
        ParametriGestore src = findById(id);
        ParametriGestore copia = clona(src);
        copia.setId(null);
        copia.setPredefinito(false);
        copia.setNomeProfilo(nomeUnivoco(src.getNomeProfilo() + " (copia)"));
        return repository.save(copia);
    }

    /** Anteprima: simula la bolletta su un consumo campione usando solo i parametri del gestore. */
    @Transactional(readOnly = true)
    public RisultatoBolletta anteprima(Long id) {
        ParametriGestore p = findById(id);
        MeseBolletta campione = MeseBolletta.builder()
                .numeroMese(1).nomeMese("CAMPIONE")
                .consumoF1(new BigDecimal("24"))
                .consumoF2(new BigDecimal("37"))
                .consumoF3(new BigDecimal("33"))
                .build();
        RisultatoMese rm = motore.calcolaMese(campione, new BigDecimal("3"), null, p,
                it.simulatore.bollette.enums.TipoCliente.DOMESTICO_RESIDENTE);
        BigDecimal aliquota = p.getIvaDomestico() != null ? p.getIvaDomestico() : new BigDecimal("0.10");
        return motore.aggregaBolletta(List.of(rm), null, null, aliquota);
    }

    private void azzeraPredefiniti() {
        for (ParametriGestore altro : repository.findAllByPredefinitoTrue()) {
            altro.setPredefinito(false);
            repository.save(altro);
        }
    }

    private String nomeUnivoco(String base) {
        String nome = base;
        int i = 2;
        while (repository.findByNomeProfilo(nome).isPresent()) {
            nome = base + " " + i++;
        }
        return nome;
    }

    private ParametriGestore clona(ParametriGestore s) {
        ParametriGestore copia = ParametriGestore.builder()
                .nomeProfilo(s.getNomeProfilo()).descrizione(s.getDescrizione())
                .predefinito(false).nomeGestore(s.getNomeGestore())
                .validoDal(s.getValidoDal()).validoAl(s.getValidoAl())
                .riferimentoDelibera(s.getRiferimentoDelibera())
                .applicaEsenzioneAccisaResidenti(s.getApplicaEsenzioneAccisaResidenti())
                .applicaScaglioni(s.getApplicaScaglioni())
                .arrotondaPerdite(s.getArrotondaPerdite())
                .quotaFissaSoloNonResidenti(s.getQuotaFissaSoloNonResidenti())
                .altrePartiteInImponibile(s.getAltrePartiteInImponibile())
                .supportaAliquoteMiste(s.getSupportaAliquoteMiste())
                .usaAliquotaIvaBolletta(s.getUsaAliquotaIvaBolletta())
                .livelloTensioneDefault(s.getLivelloTensioneDefault())
                .percentualePerdite(s.getPercentualePerdite())
                .trasportoQuotaFissaAnnua(s.getTrasportoQuotaFissaAnnua())
                .trasportoQuotaPotenzaAnnua(s.getTrasportoQuotaPotenzaAnnua())
                .quotaPotenzaSoloNonDomestici(s.getQuotaPotenzaSoloNonDomestici())
                .asosQuotaFissaAnnua(s.getAsosQuotaFissaAnnua())
                .arimQuotaFissaAnnua(s.getArimQuotaFissaAnnua())
                .accisaDomestico(s.getAccisaDomestico()).accisaNonDomestico(s.getAccisaNonDomestico())
                .sogliaEsenzioneKwhMese(s.getSogliaEsenzioneKwhMese())
                .potenzaMaxEsenzioneKw(s.getPotenzaMaxEsenzioneKw())
                .sogliaErosioneKwhMese1_5(s.getSogliaErosioneKwhMese1_5())
                .sogliaErosioneKwhMese3(s.getSogliaErosioneKwhMese3())
                .ivaDomestico(s.getIvaDomestico()).ivaNonDomestico(s.getIvaNonDomestico())
                .corrMercatoCapacita(s.getCorrMercatoCapacita()).corrDisRtn(s.getCorrDisRtn())
                .corrInt(s.getCorrInt()).corrMsd(s.getCorrMsd()).corrUesSicurezza(s.getCorrUesSicurezza())
                .corrSal(s.getCorrSal()).corrSbilanciamento(s.getCorrSbilanciamento())
                .corrAggregazioneMisure(s.getCorrAggregazioneMisure()).dispbt(s.getDispbt())
                .corrGestioneCapacita(s.getCorrGestioneCapacita())
                .commercializzazioneMese(s.getCommercializzazioneMese())
                .pcvVariabile(s.getPcvVariabile()).spreadEnergia(s.getSpreadEnergia())
                .build();
        // copia gli scaglioni
        if (s.getScaglioni() != null) {
            for (ScaglioneConsumo sc : s.getScaglioni()) {
                copia.getScaglioni().add(ScaglioneConsumo.builder()
                        .parametri(copia).componente(sc.getComponente()).tipoCliente(sc.getTipoCliente())
                        .limiteInferiore(sc.getLimiteInferiore()).limiteSuperiore(sc.getLimiteSuperiore())
                        .valore(sc.getValore()).unitaMisura(sc.getUnitaMisura()).ordine(sc.getOrdine())
                        .build());
            }
        }
        return copia;
    }
}
