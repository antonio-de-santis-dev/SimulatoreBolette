package it.simulatore.bollette.service;

import it.simulatore.bollette.calculation.MotoreCalcolo;
import it.simulatore.bollette.calculation.RisultatoBolletta;
import it.simulatore.bollette.calculation.RisultatoMese;
import it.simulatore.bollette.entity.MeseBolletta;
import it.simulatore.bollette.entity.ParametriGestore;
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
    public RisultatoBolletta anteprima(Long id) {
        ParametriGestore p = findById(id);
        MeseBolletta campione = MeseBolletta.builder()
                .numeroMese(1).nomeMese("CAMPIONE")
                .consumoF1(new BigDecimal("24"))
                .consumoF2(new BigDecimal("37"))
                .consumoF3(new BigDecimal("33"))
                .build();
        RisultatoMese rm = motore.calcolaMese(campione, new BigDecimal("3"), null, p);
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
        return ParametriGestore.builder()
                .nomeProfilo(s.getNomeProfilo()).descrizione(s.getDescrizione())
                .predefinito(false).nomeGestore(s.getNomeGestore())
                .validoDal(s.getValidoDal()).validoAl(s.getValidoAl())
                .livelloTensioneDefault(s.getLivelloTensioneDefault())
                .percentualePerdite(s.getPercentualePerdite()).arrotondaPerdite(s.getArrotondaPerdite())
                .trasportoKwMese(s.getTrasportoKwMese()).trasportoPodMese(s.getTrasportoPodMese())
                .trasportoKwh(s.getTrasportoKwh())
                .asosQuotaFissa(s.getAsosQuotaFissa()).asosQuotaVariabile(s.getAsosQuotaVariabile())
                .arimQuotaVariabile(s.getArimQuotaVariabile())
                .accisaDomestico(s.getAccisaDomestico()).accisaNonDomestico(s.getAccisaNonDomestico())
                .applicaSogliaEsenzione(s.getApplicaSogliaEsenzione())
                .sogliaEsenzioneKwhAnno(s.getSogliaEsenzioneKwhAnno())
                .sogliaMassimaKwhAnno(s.getSogliaMassimaKwhAnno())
                .ivaDomestico(s.getIvaDomestico()).ivaNonDomestico(s.getIvaNonDomestico())
                .usaAliquotaIvaBolletta(s.getUsaAliquotaIvaBolletta())
                .altrePartiteInImponibile(s.getAltrePartiteInImponibile())
                .corrMercatoCapacita(s.getCorrMercatoCapacita()).corrDisRtn(s.getCorrDisRtn())
                .corrInt(s.getCorrInt()).corrMsd(s.getCorrMsd()).corrUesSicurezza(s.getCorrUesSicurezza())
                .corrSal(s.getCorrSal()).corrSbilanciamento(s.getCorrSbilanciamento())
                .corrAggregazioneMisure(s.getCorrAggregazioneMisure()).dispbt(s.getDispbt())
                .corrGestioneCapacita(s.getCorrGestioneCapacita())
                .commercializzazioneMese(s.getCommercializzazioneMese())
                .pcvVariabile(s.getPcvVariabile()).spreadEnergia(s.getSpreadEnergia())
                .build();
    }
}
