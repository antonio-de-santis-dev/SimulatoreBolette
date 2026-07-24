package it.simulatore.bollette.calculation;

import it.simulatore.bollette.dto.*;
import it.simulatore.bollette.entity.*;
import it.simulatore.bollette.enums.*;
import it.simulatore.bollette.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BollettaCalculator {

    private final ParametroARERARepository parametroRepo;
    private final PunMensileRepository punRepo;
    private final OffertaRepository offertaRepo;

    private static final BigDecimal PERDITE_RETE = new BigDecimal("1.10");
    private static final BigDecimal IVA_DOMESTICO = new BigDecimal("0.10");
    private static final BigDecimal IVA_NON_DOMESTICO = new BigDecimal("0.22");
    private static final BigDecimal MESE_BIMESTRE = new BigDecimal("2");
    private static final int SCALE = 6;

    public SimulazioneResponse calcolaSimulazione(SimulazioneRequest request) {
        log.info("Calcolo simulazione: {}", request.getNome());

        LocalDate oggi = LocalDate.now();
        ParametriBolletta params = caricaParametri(oggi);

        ConsumiFasce consumi = distribuisciConsumi(request);

        RisultatoCalcolo risultato = calcolaPerOfferta(
            consumi,
            request.getTipoCliente(),
            request.getPotenzaContrattuale(),
            request.getTipoTariffa(),
            params,
            null,
            null
        );

        SimulazioneResponse response = buildResponse(request, consumi, risultato);

        if (request.isConfrontaOfferte()) {
            List<ConfrontoOffertaDTO> confronti = confrontaOfferte(
                consumi, request, params
            );
            response.setConfrontoOfferte(confronti);
        }

        return response;
    }

    public RisultatoCalcolo calcolaPerOfferta(
            ConsumiFasce consumi,
            TipoCliente tipoCliente,
            PotenzaContrattuale potenza,
            TipoTariffa tipoTariffa,
            ParametriBolletta params,
            Offerta offerta,
            List<PunMensile> punMensili) {

        RisultatoCalcolo risultato = new RisultatoCalcolo();

        BigDecimal materiaEnergia = calcolaMateriaEnergia(
            consumi, tipoTariffa, offerta, punMensili
        );
        risultato.setSpesaMateriaEnergia(materiaEnergia);

        BigDecimal trasporto = calcolaTrasporto(consumi, potenza, params);
        risultato.setSpesaTrasporto(trasporto);

        BigDecimal oneri = calcolaOneriSistema(consumi, tipoCliente, params);
        risultato.setSpesaOneriSistema(oneri);

        BigDecimal accise = calcolaAccise(consumi, tipoCliente, params);
        risultato.setSpesaAccise(accise);

        BigDecimal imponibile = materiaEnergia
            .add(trasporto)
            .add(oneri)
            .add(accise);

        BigDecimal aliquotaIva = tipoCliente == TipoCliente.RESIDENTE 
            ? IVA_DOMESTICO : IVA_NON_DOMESTICO;
        BigDecimal iva = imponibile.multiply(aliquotaIva).setScale(4, RoundingMode.HALF_UP);
        risultato.setSpesaIva(iva);

        BigDecimal totaleBimestre = imponibile.add(iva);
        risultato.setTotaleBimestrale(totaleBimestre);
        risultato.setTotaleAnnuale(totaleBimestre.multiply(new BigDecimal("6")));

        BigDecimal prezzoMedio = totaleBimestre.divide(
            consumi.getTotale(), SCALE, RoundingMode.HALF_UP
        );
        risultato.setPrezzoMedioKwh(prezzoMedio);

        return risultato;
    }

    private BigDecimal calcolaMateriaEnergia(
            ConsumiFasce consumi, 
            TipoTariffa tipoTariffa,
            Offerta offerta,
            List<PunMensile> punMensili) {

        BigDecimal totale = BigDecimal.ZERO;

        if (offerta == null) {
            BigDecimal prezzoRiferimento = new BigDecimal("0.2800");
            totale = consumi.getTotale().multiply(prezzoRiferimento);

        } else if (offerta.getTipoOfferta() == TipoOfferta.PREZZO_FISSO) {
            totale = calcolaMateriaFissa(consumi, tipoTariffa, offerta);

        } else if (offerta.getTipoOfferta() == TipoOfferta.INDICIZZATA_PUN) {
            totale = calcolaMateriaIndicizzata(consumi, tipoTariffa, offerta, punMensili);
        }

        if (offerta != null && offerta.getPcvAnnuo() != null) {
            BigDecimal pcvBimestrale = offerta.getPcvAnnuo()
                .divide(new BigDecimal("6"), SCALE, RoundingMode.HALF_UP);
            totale = totale.add(pcvBimestrale);
        }

        return totale.setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal calcolaMateriaFissa(
            ConsumiFasce consumi, 
            TipoTariffa tipoTariffa, 
            Offerta offerta) {

        BigDecimal totale = BigDecimal.ZERO;

        switch (tipoTariffa) {
            case MONORARIA:
                if (offerta.getPrezzoFissoF0() != null) {
                    totale = consumi.getTotale().multiply(offerta.getPrezzoFissoF0());
                }
                break;
            case BIORARIA:
                if (offerta.getPrezzoFissoF1() != null) {
                    totale = totale.add(consumi.getF1().multiply(offerta.getPrezzoFissoF1()));
                }
                if (offerta.getPrezzoFissoF23() != null) {
                    totale = totale.add(consumi.getF23().multiply(offerta.getPrezzoFissoF23()));
                }
                break;
            case TRIORARIA:
                if (offerta.getPrezzoFissoF1() != null) {
                    totale = totale.add(consumi.getF1().multiply(offerta.getPrezzoFissoF1()));
                }
                if (offerta.getPrezzoFissoF2() != null) {
                    totale = totale.add(consumi.getF2().multiply(offerta.getPrezzoFissoF2()));
                }
                if (offerta.getPrezzoFissoF3() != null) {
                    totale = totale.add(consumi.getF3().multiply(offerta.getPrezzoFissoF3()));
                }
                break;
        }

        return totale;
    }

    private BigDecimal calcolaMateriaIndicizzata(
            ConsumiFasce consumi,
            TipoTariffa tipoTariffa,
            Offerta offerta,
            List<PunMensile> punMensili) {

        BigDecimal punMedio = calcolaPunMedio(punMensili);
        BigDecimal punConPerdite = punMedio.multiply(PERDITE_RETE);

        BigDecimal totale = BigDecimal.ZERO;

        switch (tipoTariffa) {
            case MONORARIA:
                BigDecimal prezzoF0 = punConPerdite.add(
                    offerta.getSpreadPunF0() != null ? offerta.getSpreadPunF0() : BigDecimal.ZERO
                );
                totale = consumi.getTotale().multiply(prezzoF0);
                break;
            case BIORARIA:
                BigDecimal prezzoF1 = punConPerdite.add(
                    offerta.getSpreadPunF1() != null ? offerta.getSpreadPunF1() : BigDecimal.ZERO
                );
                BigDecimal prezzoF23 = punConPerdite.add(
                    offerta.getSpreadPunF23() != null ? offerta.getSpreadPunF23() : BigDecimal.ZERO
                );
                totale = consumi.getF1().multiply(prezzoF1)
                    .add(consumi.getF23().multiply(prezzoF23));
                break;
            case TRIORARIA:
                BigDecimal pF1 = punConPerdite.add(
                    offerta.getSpreadPunF1() != null ? offerta.getSpreadPunF1() : BigDecimal.ZERO
                );
                BigDecimal pF2 = punConPerdite.add(
                    offerta.getSpreadPunF2() != null ? offerta.getSpreadPunF2() : BigDecimal.ZERO
                );
                BigDecimal pF3 = punConPerdite.add(
                    offerta.getSpreadPunF3() != null ? offerta.getSpreadPunF3() : BigDecimal.ZERO
                );
                totale = consumi.getF1().multiply(pF1)
                    .add(consumi.getF2().multiply(pF2))
                    .add(consumi.getF3().multiply(pF3));
                break;
        }

        return totale;
    }

    private BigDecimal calcolaPunMedio(List<PunMensile> punMensili) {
        if (punMensili == null || punMensili.isEmpty()) {
            return new BigDecimal("0.1190");
        }
        BigDecimal somma = BigDecimal.ZERO;
        for (PunMensile pm : punMensili) {
            somma = somma.add(pm.getPunMonorario() != null ? pm.getPunMonorario() : BigDecimal.ZERO);
        }
        return somma.divide(new BigDecimal(punMensili.size()), SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calcolaTrasporto(
            ConsumiFasce consumi, 
            PotenzaContrattuale potenza, 
            ParametriBolletta params) {

        BigDecimal quotaFissa = params.getTauFissa() != null ? params.getTauFissa() : new BigDecimal("3.80");
        BigDecimal quotaPotenza = params.getTauPotenza() != null ? params.getTauPotenza() : new BigDecimal("7.93");
        BigDecimal quotaEnergia = params.getTauEnergia() != null ? params.getTauEnergia() : new BigDecimal("0.0156");

        BigDecimal qfBim = quotaFissa.multiply(MESE_BIMESTRE);

        BigDecimal qpBim = quotaPotenza
            .multiply(new BigDecimal(String.valueOf(potenza.getValore())))
            .multiply(MESE_BIMESTRE)
            .divide(new BigDecimal("12"), SCALE, RoundingMode.HALF_UP);

        BigDecimal qe = consumi.getTotale().multiply(quotaEnergia);

        BigDecimal uc = params.getUc3() != null ? params.getUc3() : new BigDecimal("0.008");
        BigDecimal ucTot = consumi.getTotale().multiply(uc);

        return qfBim.add(qpBim).add(qe).add(ucTot).setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal calcolaOneriSistema(
            ConsumiFasce consumi, 
            TipoCliente tipoCliente, 
            ParametriBolletta params) {

        BigDecimal asos = params.getAsos() != null ? params.getAsos() : new BigDecimal("0.020");
        BigDecimal arim = params.getArim() != null ? params.getArim() : new BigDecimal("0.010");

        BigDecimal totale = consumi.getTotale().multiply(asos.add(arim));

        if (tipoCliente != TipoCliente.RESIDENTE) {
            BigDecimal quotaFissaNonRes = params.getOneriFissaNonRes() != null 
                ? params.getOneriFissaNonRes() : new BigDecimal("14.80");
            totale = totale.add(quotaFissaNonRes.multiply(MESE_BIMESTRE).divide(
                new BigDecimal("12"), SCALE, RoundingMode.HALF_UP));
        }

        return totale.setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal calcolaAccise(
            ConsumiFasce consumi, 
            TipoCliente tipoCliente, 
            ParametriBolletta params) {

        if (tipoCliente != TipoCliente.RESIDENTE) {
            BigDecimal accisaPiena = params.getAccisaPiena() != null 
                ? params.getAccisaPiena() : new BigDecimal("0.0227");
            return consumi.getTotale().multiply(accisaPiena).setScale(4, RoundingMode.HALF_UP);
        }

        BigDecimal consumoAnnuo = consumi.getTotale().multiply(new BigDecimal("6"));
        BigDecimal accisa = BigDecimal.ZERO;

        BigDecimal soglia1 = new BigDecimal("1800");
        BigDecimal soglia2 = new BigDecimal("2640");
        BigDecimal aliquotaAgevolata = params.getAccisaAgevolata() != null 
            ? params.getAccisaAgevolata() : new BigDecimal("0.0227");

        if (consumoAnnuo.compareTo(soglia1) > 0) {
            BigDecimal eccedenza = consumoAnnuo.subtract(soglia1);
            if (eccedenza.compareTo(soglia2.subtract(soglia1)) > 0) {
                eccedenza = soglia2.subtract(soglia1);
            }
            BigDecimal eccedenzaBim = eccedenza.divide(new BigDecimal("6"), SCALE, RoundingMode.HALF_UP);
            accisa = eccedenzaBim.multiply(aliquotaAgevolata);
        }

        return accisa.setScale(4, RoundingMode.HALF_UP);
    }

    private List<ConfrontoOffertaDTO> confrontaOfferte(
            ConsumiFasce consumi,
            SimulazioneRequest request,
            ParametriBolletta params) {

        List<Offerta> offerte = offertaRepo.findByAttivaTrue();
        List<ConfrontoOffertaDTO> risultati = new ArrayList<>();

        List<PunMensile> punMensili = recuperaPunMensili(request);

        for (Offerta offerta : offerte) {
            if (offerta.getTipoTariffa() != request.getTipoTariffa()) continue;

            RisultatoCalcolo rc = calcolaPerOfferta(
                consumi, request.getTipoCliente(), 
                request.getPotenzaContrattuale(), 
                request.getTipoTariffa(),
                params, offerta, punMensili
            );

            ConfrontoOffertaDTO dto = new ConfrontoOffertaDTO();
            dto.setOffertaId(offerta.getId());
            dto.setNomeFornitore(offerta.getNomeFornitore());
            dto.setNomeOfferta(offerta.getNomeOfferta());
            dto.setTipoOfferta(offerta.getTipoOfferta().getDescrizione());
            dto.setTipoTariffa(offerta.getTipoTariffa().getDescrizione());
            dto.setPcvAnnuo(offerta.getPcvAnnuo());
            dto.setTotaleBimestrale(rc.getTotaleBimestrale());
            dto.setTotaleAnnuale(rc.getTotaleAnnuale());
            dto.setPrezzoKwhF0(rc.getPrezzoMedioKwh());

            risultati.add(dto);
        }

        risultati.sort(Comparator.comparing(ConfrontoOffertaDTO::getTotaleAnnuale));

        if (!risultati.isEmpty()) {
            BigDecimal peggiore = risultati.get(risultati.size() - 1).getTotaleAnnuale();
            BigDecimal media = risultati.stream()
                .map(ConfrontoOffertaDTO::getTotaleAnnuale)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(risultati.size()), SCALE, RoundingMode.HALF_UP);

            for (int i = 0; i < risultati.size(); i++) {
                ConfrontoOffertaDTO dto = risultati.get(i);
                dto.setPosizioneClassifica(i + 1);
                dto.setMiglioreOfferta(i == 0);
                dto.setRisparmioAnnualeVsMedia(
                    media.subtract(dto.getTotaleAnnuale()).setScale(2, RoundingMode.HALF_UP)
                );
                dto.setRisparmioAnnualeVsPeggiore(
                    peggiore.subtract(dto.getTotaleAnnuale()).setScale(2, RoundingMode.HALF_UP)
                );
            }
        }

        return risultati;
    }

    private ConsumiFasce distribuisciConsumi(SimulazioneRequest request) {
        ConsumiFasce cf = new ConsumiFasce();
        cf.setTotale(request.getConsumoTotaleKwh());

        if (request.getConsumoF1() != null && request.getConsumoF2() != null && request.getConsumoF3() != null) {
            cf.setF1(request.getConsumoF1());
            cf.setF2(request.getConsumoF2());
            cf.setF3(request.getConsumoF3());
            cf.setF23(request.getConsumoF2().add(request.getConsumoF3()));
        } else {
            switch (request.getTipoTariffa()) {
                case MONORARIA:
                    cf.setF1(request.getConsumoTotaleKwh());
                    cf.setF2(BigDecimal.ZERO);
                    cf.setF3(BigDecimal.ZERO);
                    cf.setF23(BigDecimal.ZERO);
                    break;
                case BIORARIA:
                    cf.setF1(request.getConsumoTotaleKwh().multiply(new BigDecimal("0.40")));
                    cf.setF23(request.getConsumoTotaleKwh().multiply(new BigDecimal("0.60")));
                    cf.setF2(cf.getF23().multiply(new BigDecimal("0.35")));
                    cf.setF3(cf.getF23().multiply(new BigDecimal("0.65")));
                    break;
                case TRIORARIA:
                    cf.setF1(request.getConsumoTotaleKwh().multiply(new BigDecimal("0.35")));
                    cf.setF2(request.getConsumoTotaleKwh().multiply(new BigDecimal("0.25")));
                    cf.setF3(request.getConsumoTotaleKwh().multiply(new BigDecimal("0.40")));
                    cf.setF23(cf.getF2().add(cf.getF3()));
                    break;
            }
        }
        return cf;
    }

    private ParametriBolletta caricaParametri(LocalDate data) {
        ParametriBolletta p = new ParametriBolletta();

        p.setTauFissa(getParametro("TAU_FISSA", data, new BigDecimal("3.80")));
        p.setTauPotenza(getParametro("TAU_POTENZA", data, new BigDecimal("7.93")));
        p.setTauEnergia(getParametro("TAU_ENERGIA", data, new BigDecimal("0.0156")));
        p.setUc3(getParametro("UC3", data, new BigDecimal("0.008")));
        p.setAsos(getParametro("ASOS", data, new BigDecimal("0.020")));
        p.setArim(getParametro("ARIM", data, new BigDecimal("0.010")));
        p.setAccisaAgevolata(getParametro("ACCISA_AGEVOLATA", data, new BigDecimal("0.0227")));
        p.setAccisaPiena(getParametro("ACCISA_PIENA", data, new BigDecimal("0.0227")));
        p.setOneriFissaNonRes(getParametro("ONERI_FISSA_NON_RES", data, new BigDecimal("14.80")));

        return p;
    }

    private BigDecimal getParametro(String nome, LocalDate data, BigDecimal defaultValue) {
        Optional<ParametroARERA> opt = parametroRepo.findValidoPerData(nome, data);
        return opt.map(ParametroARERA::getValore).orElse(defaultValue);
    }

    private List<PunMensile> recuperaPunMensili(SimulazioneRequest request) {
        List<PunMensile> punMensili = new ArrayList<>();

        if (request.getAnnoRiferimento1() != null && request.getMeseRiferimento1() != null) {
            punRepo.findByAnnoAndMese(request.getAnnoRiferimento1(), request.getMeseRiferimento1())
                .ifPresent(punMensili::add);
        }
        if (request.getAnnoRiferimento2() != null && request.getMeseRiferimento2() != null) {
            punRepo.findByAnnoAndMese(request.getAnnoRiferimento2(), request.getMeseRiferimento2())
                .ifPresent(punMensili::add);
        }

        if (punMensili.isEmpty()) {
            LocalDate now = LocalDate.now();
            for (int i = 2; i <= 3; i++) {
                LocalDate ref = now.minusMonths(i);
                punRepo.findByAnnoAndMese(ref.getYear(), ref.getMonthValue())
                    .ifPresent(punMensili::add);
            }
        }

        return punMensili;
    }

    private SimulazioneResponse buildResponse(SimulazioneRequest request, ConsumiFasce consumi, RisultatoCalcolo risultato) {
        SimulazioneResponse r = new SimulazioneResponse();
        r.setNome(request.getNome());
        r.setTipoCliente(request.getTipoCliente());
        r.setPotenzaContrattuale(request.getPotenzaContrattuale());
        r.setTipoTariffa(request.getTipoTariffa());
        r.setConsumoTotaleKwh(consumi.getTotale());
        r.setConsumoF1(consumi.getF1());
        r.setConsumoF2(consumi.getF2());
        r.setConsumoF3(consumi.getF3());

        r.setSpesaMateriaEnergia(risultato.getSpesaMateriaEnergia());
        r.setSpesaTrasporto(risultato.getSpesaTrasporto());
        r.setSpesaOneriSistema(risultato.getSpesaOneriSistema());
        r.setSpesaAccise(risultato.getSpesaAccise());
        r.setSpesaIva(risultato.getSpesaIva());
        r.setTotaleBimestrale(risultato.getTotaleBimestrale());
        r.setTotaleAnnuale(risultato.getTotaleAnnuale());
        r.setPrezzoMedioKwh(risultato.getPrezzoMedioKwh());

        BigDecimal totale = risultato.getTotaleBimestrale();
        if (totale.compareTo(BigDecimal.ZERO) > 0) {
            r.setPctMateriaEnergia(percentuale(risultato.getSpesaMateriaEnergia(), totale));
            r.setPctTrasporto(percentuale(risultato.getSpesaTrasporto(), totale));
            r.setPctOneriSistema(percentuale(risultato.getSpesaOneriSistema(), totale));
            r.setPctAccise(percentuale(risultato.getSpesaAccise(), totale));
            r.setPctIva(percentuale(risultato.getSpesaIva(), totale));
        }

        return r;
    }

    private BigDecimal percentuale(BigDecimal parte, BigDecimal totale) {
        return parte.multiply(new BigDecimal("100"))
            .divide(totale, 2, RoundingMode.HALF_UP);
    }

    @lombok.Data
    public static class ConsumiFasce {
        private BigDecimal totale;
        private BigDecimal f1;
        private BigDecimal f2;
        private BigDecimal f3;
        private BigDecimal f23;
    }

    @lombok.Data
    public static class ParametriBolletta {
        private BigDecimal tauFissa;
        private BigDecimal tauPotenza;
        private BigDecimal tauEnergia;
        private BigDecimal uc3;
        private BigDecimal asos;
        private BigDecimal arim;
        private BigDecimal accisaAgevolata;
        private BigDecimal accisaPiena;
        private BigDecimal oneriFissaNonRes;
    }

    @lombok.Data
    public static class RisultatoCalcolo {
        private BigDecimal spesaMateriaEnergia;
        private BigDecimal spesaTrasporto;
        private BigDecimal spesaOneriSistema;
        private BigDecimal spesaAccise;
        private BigDecimal spesaIva;
        private BigDecimal totaleBimestrale;
        private BigDecimal totaleAnnuale;
        private BigDecimal prezzoMedioKwh;
    }
}
