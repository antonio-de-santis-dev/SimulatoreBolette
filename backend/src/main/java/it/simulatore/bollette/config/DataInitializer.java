package it.simulatore.bollette.config;

import it.simulatore.bollette.entity.*;
import it.simulatore.bollette.enums.*;
import it.simulatore.bollette.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    @Bean
    CommandLineRunner initData(
            OffertaRepository offertaRepo,
            ParametroARERARepository parametroRepo,
            PunMensileRepository punRepo,
            ParametriGestoreRepository parametriGestoreRepo,
            BollettaConcorrenteRepository bollettaRepo) {

        return args -> {
            seedParametriArera(parametroRepo);
            seedPun(punRepo);
            seedOfferte(offertaRepo);
            seedParametriGestore(parametriGestoreRepo);
            seedBollettaEsempio(bollettaRepo);
        };
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SORGENTE B — Profili parametri del gestore
    // ═══════════════════════════════════════════════════════════════════════════
    private void seedParametriGestore(ParametriGestoreRepository repo) {
        if (repo.count() > 0) {
            return;
        }

        // "Modello Excel" — replica il foglio, per i test di regressione (tutti i flag su compatibilita)
        ParametriGestore excel = ParametriGestore.builder()
                .nomeProfilo("Modello Excel")
                .descrizione("Replica il foglio SIMULATORE_BIMESTRALE_LUCE (flag di compatibilita attivi)")
                .predefinito(false).nomeGestore("Gestore Demo")
                .validoDal(LocalDate.of(2024, 11, 1)).riferimentoDelibera("Foglio Excel")
                .applicaEsenzioneAccisaResidenti(false).applicaScaglioni(false)
                .arrotondaPerdite(true).quotaFissaSoloNonResidenti(false)
                .altrePartiteInImponibile(true).supportaAliquoteMiste(false).usaAliquotaIvaBolletta(true)
                .livelloTensioneDefault(LivelloTensione.BT).percentualePerdite(new BigDecimal("0.10"))
                .trasportoQuotaFissaAnnua(new BigDecimal("22.08"))          // 1,84 x 12
                .trasportoQuotaPotenzaAnnua(new BigDecimal("22.398804"))     // 1,866567 x 12
                .quotaPotenzaSoloNonDomestici(false)
                .asosQuotaFissaAnnua(new BigDecimal("91.5624"))             // 7,6302 x 12
                .arimQuotaFissaAnnua(BigDecimal.ZERO)
                .accisaDomestico(new BigDecimal("0.0227")).accisaNonDomestico(new BigDecimal("0.0227"))
                .sogliaEsenzioneKwhMese(new BigDecimal("150")).potenzaMaxEsenzioneKw(new BigDecimal("3"))
                .sogliaErosioneKwhMese1_5(new BigDecimal("150")).sogliaErosioneKwhMese3(new BigDecimal("220"))
                .ivaDomestico(new BigDecimal("0.10")).ivaNonDomestico(new BigDecimal("0.22"))
                .corrMercatoCapacita(new BigDecimal("0.008995")).corrDisRtn(new BigDecimal("0.000558"))
                .corrInt(new BigDecimal("0.000856")).corrMsd(new BigDecimal("0.001953"))
                .corrUesSicurezza(new BigDecimal("0.004339")).corrSal(new BigDecimal("0.00052"))
                .corrSbilanciamento(new BigDecimal("0.004")).corrAggregazioneMisure(new BigDecimal("0.007"))
                .dispbt(new BigDecimal("0.109858")).corrGestioneCapacita(new BigDecimal("0.01293"))
                .commercializzazioneMese(new BigDecimal("8.95")).pcvVariabile(new BigDecimal("0.0044"))
                .spreadEnergia(new BigDecimal("0.01"))
                .build();
        scaglioneUnico(excel, "TRASPORTO_ENERGIA", "0.0122");
        scaglioneUnico(excel, "ASOS_VARIABILE", "0.029809");
        scaglioneUnico(excel, "ARIM_VARIABILE", "0.008828");
        repo.save(excel);

        // "ARERA 2026 — conforme" — predefinito, tutti i flag su normativa
        ParametriGestore arera = ParametriGestore.builder()
                .nomeProfilo("ARERA 2026 - conforme")
                .descrizione("Normativa ARERA vigente: esenzione accisa residenti, scaglioni, perdite BT 10,40%")
                .predefinito(true).nomeGestore("Gestore Demo")
                .validoDal(LocalDate.of(2026, 1, 1)).riferimentoDelibera("ARERA 604/2024/R/eel")
                .applicaEsenzioneAccisaResidenti(true).applicaScaglioni(true)
                .arrotondaPerdite(false).quotaFissaSoloNonResidenti(true)
                .altrePartiteInImponibile(true).supportaAliquoteMiste(true).usaAliquotaIvaBolletta(true)
                .livelloTensioneDefault(LivelloTensione.BT).percentualePerdite(new BigDecimal("0.1040"))
                .trasportoQuotaFissaAnnua(new BigDecimal("22.08"))
                .trasportoQuotaPotenzaAnnua(new BigDecimal("22.398804"))
                .quotaPotenzaSoloNonDomestici(true)
                .asosQuotaFissaAnnua(new BigDecimal("91.5624")).arimQuotaFissaAnnua(new BigDecimal("3.8568"))
                .accisaDomestico(new BigDecimal("0.0227")).accisaNonDomestico(new BigDecimal("0.0227"))
                .sogliaEsenzioneKwhMese(new BigDecimal("150")).potenzaMaxEsenzioneKw(new BigDecimal("3"))
                .sogliaErosioneKwhMese1_5(new BigDecimal("150")).sogliaErosioneKwhMese3(new BigDecimal("220"))
                .ivaDomestico(new BigDecimal("0.10")).ivaNonDomestico(new BigDecimal("0.22"))
                .corrMercatoCapacita(new BigDecimal("0.009001")).corrDisRtn(new BigDecimal("0.000558"))
                .corrInt(new BigDecimal("0.000856")).corrMsd(new BigDecimal("0.001953"))
                .corrUesSicurezza(new BigDecimal("0.002048")).corrSal(new BigDecimal("0.00052"))
                .corrSbilanciamento(new BigDecimal("0.01")).corrAggregazioneMisure(new BigDecimal("0.007"))
                .dispbt(new BigDecimal("0.109858")).corrGestioneCapacita(new BigDecimal("0.01293"))
                .commercializzazioneMese(new BigDecimal("8.95")).pcvVariabile(new BigDecimal("0.005"))
                .spreadEnergia(new BigDecimal("0.01"))
                .build();
        // Scaglioni: valori del foglio DATI (7,6302 · 6,7709 · 0,3214 · 1,2554) — attribuzione INFERITA,
        // da confermare con la delibera vigente (vedi README).
        scaglione(arera, "TRASPORTO_ENERGIA", "0", "1800", "0.0122", 1);
        scaglione(arera, "TRASPORTO_ENERGIA", "1800", null, "0.0140", 2);
        scaglioneUnico(arera, "ASOS_VARIABILE", "0.029809");
        scaglioneUnico(arera, "ARIM_VARIABILE", "0.008828");
        repo.save(arera);

        // "FuturEnergy — da fattura" — valori estratti dalla fattura reale
        ParametriGestore futur = ParametriGestore.builder()
                .nomeProfilo("FuturEnergy - da fattura")
                .descrizione("Valori estratti dalla fattura reale FuturEnergy n. 1237600")
                .predefinito(false).nomeGestore("FuturEnergy Rinnovabile")
                .validoDal(LocalDate.of(2024, 7, 1)).riferimentoDelibera("Fattura 1237600")
                .applicaEsenzioneAccisaResidenti(true).applicaScaglioni(false)
                .arrotondaPerdite(false).quotaFissaSoloNonResidenti(true)
                .altrePartiteInImponibile(true).supportaAliquoteMiste(true).usaAliquotaIvaBolletta(true)
                .livelloTensioneDefault(LivelloTensione.BT).percentualePerdite(new BigDecimal("0.1040"))
                .trasportoQuotaFissaAnnua(new BigDecimal("22.08"))
                .trasportoQuotaPotenzaAnnua(new BigDecimal("22.398804"))
                .quotaPotenzaSoloNonDomestici(true)
                .asosQuotaFissaAnnua(new BigDecimal("91.5624")).arimQuotaFissaAnnua(new BigDecimal("3.8568"))
                .accisaDomestico(new BigDecimal("0.0227")).accisaNonDomestico(new BigDecimal("0.0227"))
                .sogliaEsenzioneKwhMese(new BigDecimal("150")).potenzaMaxEsenzioneKw(new BigDecimal("3"))
                .sogliaErosioneKwhMese1_5(new BigDecimal("150")).sogliaErosioneKwhMese3(new BigDecimal("220"))
                .ivaDomestico(new BigDecimal("0.10")).ivaNonDomestico(new BigDecimal("0.22"))
                .corrMercatoCapacita(new BigDecimal("0.009001")).corrDisRtn(new BigDecimal("0.000558"))
                .corrInt(new BigDecimal("0.000856")).corrMsd(new BigDecimal("0.001953"))
                .corrUesSicurezza(new BigDecimal("0.002048")).corrSal(new BigDecimal("0.00052"))
                .corrSbilanciamento(new BigDecimal("0.01")).corrAggregazioneMisure(new BigDecimal("0.007"))
                .dispbt(new BigDecimal("0.109858")).corrGestioneCapacita(new BigDecimal("0.01293"))
                .commercializzazioneMese(new BigDecimal("8.95")).pcvVariabile(new BigDecimal("0.005"))
                .spreadEnergia(new BigDecimal("0.01"))
                .build();
        scaglioneUnico(futur, "TRASPORTO_ENERGIA", "0.0122");
        scaglioneUnico(futur, "ASOS_VARIABILE", "0.029809");
        scaglioneUnico(futur, "ARIM_VARIABILE", "0.008828");
        repo.save(futur);
    }

    private void scaglioneUnico(ParametriGestore p, String componente, String valore) {
        scaglione(p, componente, "0", null, valore, 1);
    }

    private void scaglione(ParametriGestore p, String componente, String da, String a, String valore, int ordine) {
        p.getScaglioni().add(ScaglioneConsumo.builder()
                .parametri(p).componente(componente)
                .limiteInferiore(new BigDecimal(da))
                .limiteSuperiore(a == null ? null : new BigDecimal(a))
                .valore(new BigDecimal(valore)).unitaMisura("EUR/kWh").ordine(ordine)
                .build());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SORGENTE A — Bolletta concorrente di esempio (fattura reale FuturEnergy)
    // ═══════════════════════════════════════════════════════════════════════════
    private void seedBollettaEsempio(BollettaConcorrenteRepository repo) {
        if (repo.count() > 0) {
            return;
        }

        BollettaConcorrente b = BollettaConcorrente.builder()
                .numeroFattura("1237600")
                .dataFattura(LocalDate.of(2024, 8, 5))
                .periodoDal(LocalDate.of(2024, 7, 1))
                .periodoAl(LocalDate.of(2024, 7, 31))
                .nomeFornitore("FuturEnergy Rinnovabile")
                .nomeOfferta("dinamico pmi residenziale")
                .codiceOfferta("DINPMF")
                .ragioneSociale("Cliente Esempio")
                .indirizzoFornitura("Via Roma 1")
                .pod("IT001E73237265")
                .tipologiaCliente(TipoCliente.DOMESTICO_NON_RESIDENTE)
                .opzioneTariffaria("Utenza domestica non residente")
                .potenzaImpegnata(new BigDecimal("3.00"))
                .potenzaDisponibile(new BigDecimal("3.30"))
                .livelloTensione(LivelloTensione.BT)
                .fatturatoMateriaEnergia(new BigDecimal("15.31"))
                .fatturatoTrasporto(new BigDecimal("7.65"))
                .fatturatoOneriSistema(new BigDecimal("8.29"))
                .fatturatoAltrePartite(new BigDecimal("75.94"))
                .fatturatoImposte(new BigDecimal("0.39"))
                .fatturatoImponibile(new BigDecimal("107.58"))
                .fatturatoIva(new BigDecimal("10.76"))
                .fatturatoTotale(new BigDecimal("118.34"))
                .aliquotaIvaApplicata(new BigDecimal("0.10"))
                .note("Fattura reale FuturEnergy n. 1237600, periodo 01/07/2024-31/07/2024")
                .build();

        MeseBolletta luglio = MeseBolletta.builder()
                .bolletta(b).numeroMese(1).nomeMese("LUGLIO").mese(7).anno(2024)
                .consumoF1(new BigDecimal("6")).consumoF2(new BigDecimal("4")).consumoF3(new BigDecimal("7"))
                .build();
        b.getMesi().add(luglio);

        b.getAltrePartite().add(AltraPartita.builder()
                .bolletta(b).descrizione("Gestione pratica e Oneri Amministrativi")
                .importo(new BigDecimal("75.94")).aliquotaIva(new BigDecimal("0.10"))
                .soggettaIva(true).ordine(1).build());
        b.getAltrePartite().add(AltraPartita.builder()
                .bolletta(b).descrizione("Tutela miglior prezzo")
                .importo(new BigDecimal("0.03")).aliquotaIva(new BigDecimal("0.10"))
                .soggettaIva(true).ordine(2).build());
        b.getAltrePartite().add(AltraPartita.builder()
                .bolletta(b).descrizione("Oneri commercializzazione parte variabile")
                .importo(new BigDecimal("0.09")).aliquotaIva(new BigDecimal("0.10"))
                .soggettaIva(true).ordine(3).build());

        repo.save(b);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Offerte del gestore (con voci di corrispettivo per il nuovo motore)
    // ═══════════════════════════════════════════════════════════════════════════
    private void seedOfferte(OffertaRepository offertaRepo) {
        if (offertaRepo.count() > 0) {
            return;
        }

        // Offerta demo del gestore con voci (quote energia + perdite) per fascia
        Offerta trioraria = Offerta.builder()
                .nomeFornitore("Gestore Demo").nomeOfferta("Luce Trioraria Web")
                .tipoOfferta(TipoOfferta.PREZZO_FISSO).tipoTariffa(TipoTariffa.TRIORARIA)
                .prezzoFissoF1(new BigDecimal("0.13")).prezzoFissoF2(new BigDecimal("0.12"))
                .prezzoFissoF3(new BigDecimal("0.11")).pcvAnnuo(new BigDecimal("0"))
                .condizioniSpeciali("Offerta demo con voci di corrispettivo per fascia").build();
        trioraria.getVoci().addAll(vociEnergiaTrioraria(trioraria,
                "0.13", "0.12", "0.11"));
        offertaRepo.save(trioraria);

        Offerta biofascia = Offerta.builder()
                .nomeFornitore("Gestore Demo").nomeOfferta("Luce Trioraria Verde")
                .tipoOfferta(TipoOfferta.PREZZO_FISSO).tipoTariffa(TipoTariffa.TRIORARIA)
                .prezzoFissoF1(new BigDecimal("0.145")).prezzoFissoF2(new BigDecimal("0.135"))
                .prezzoFissoF3(new BigDecimal("0.125")).pcvAnnuo(new BigDecimal("0"))
                .condizioniSpeciali("100% energia rinnovabile").build();
        biofascia.getVoci().addAll(vociEnergiaTrioraria(biofascia,
                "0.145", "0.135", "0.125"));
        offertaRepo.save(biofascia);

        // Offerte legacy monorarie/biorarie (compatibilita con /api/simulazioni)
        offertaRepo.save(Offerta.builder()
                .nomeFornitore("Enel Energia").nomeOfferta("Luce Sicura")
                .tipoOfferta(TipoOfferta.PREZZO_FISSO).tipoTariffa(TipoTariffa.MONORARIA)
                .prezzoFissoF0(new BigDecimal("0.2850")).pcvAnnuo(new BigDecimal("120.00"))
                .condizioniSpeciali("Prezzo bloccato 12 mesi").build());
        offertaRepo.save(Offerta.builder()
                .nomeFornitore("Edison").nomeOfferta("PUN Zero")
                .tipoOfferta(TipoOfferta.INDICIZZATA_PUN).tipoTariffa(TipoTariffa.MONORARIA)
                .spreadPunF0(new BigDecimal("0.0150")).pcvAnnuo(new BigDecimal("60.00"))
                .condizioniSpeciali("Solo spread, nessun mark-up").build());
    }

    private List<VoceCorrispettivo> vociEnergiaTrioraria(Offerta offerta, String f1, String f2, String f3) {
        List<VoceCorrispettivo> voci = new ArrayList<>();
        voci.add(voce(offerta, "Quota energia attiva F1", BaseQuantita.KWH_F1, f1, 1));
        voci.add(voce(offerta, "Quota energia attiva F2", BaseQuantita.KWH_F2, f2, 2));
        voci.add(voce(offerta, "Quota energia attiva F3", BaseQuantita.KWH_F3, f3, 3));
        voci.add(voce(offerta, "Perdite di rete F1", BaseQuantita.PERDITE_F1, f1, 4));
        voci.add(voce(offerta, "Perdite di rete F2", BaseQuantita.PERDITE_F2, f2, 5));
        voci.add(voce(offerta, "Perdite di rete F3", BaseQuantita.PERDITE_F3, f3, 6));
        return voci;
    }

    private VoceCorrispettivo voce(Offerta offerta, String descr, BaseQuantita base, String corr, int ordine) {
        return VoceCorrispettivo.builder()
                .offerta(offerta).descrizione(descr)
                .categoria(CategoriaVoce.MATERIA_ENERGIA).base(base)
                .origine(OrigineParametro.OFFERTA).corrispettivo(new BigDecimal(corr))
                .unitaMisura("EUR/kWh").ordine(ordine).attiva(true).build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Parametri ARERA legacy e PUN (per il vecchio motore /api/simulazioni)
    // ═══════════════════════════════════════════════════════════════════════════
    private void seedParametriArera(ParametroARERARepository parametroRepo) {
        if (parametroRepo.count() > 0) {
            return;
        }
        parametroRepo.save(par("TAU_FISSA", "Trasporto quota fissa mensile", "3.80", "EUR/mese"));
        parametroRepo.save(par("TAU_POTENZA", "Trasporto quota potenza", "7.93", "EUR/kW/anno"));
        parametroRepo.save(par("TAU_ENERGIA", "Trasporto quota energia", "0.0156", "EUR/kWh"));
        parametroRepo.save(par("ASOS", "Oneri sistema ASOS", "0.020", "EUR/kWh"));
        parametroRepo.save(par("ARIM", "Oneri sistema ARIM", "0.010", "EUR/kWh"));
        parametroRepo.save(par("ACCISA_AGEVOLATA", "Accisa agevolata residenti", "0.0227", "EUR/kWh"));
    }

    private ParametroARERA par(String nome, String descr, String valore, String um) {
        return ParametroARERA.builder()
                .nomeParametro(nome).descrizione(descr)
                .valore(new BigDecimal(valore)).unitaMisura(um)
                .validoDal(LocalDate.of(2026, 1, 1)).build();
    }

    private void seedPun(PunMensileRepository punRepo) {
        if (punRepo.count() > 0) {
            return;
        }
        punRepo.save(PunMensile.builder().anno(2026).mese(1)
                .punMonorario(new BigDecimal("0.132665"))
                .punF1(new BigDecimal("0.15126")).punF2(new BigDecimal("0.13740"))
                .punF3(new BigDecimal("0.11829")).build());
        punRepo.save(PunMensile.builder().anno(2026).mese(2)
                .punMonorario(new BigDecimal("0.114405"))
                .punF1(new BigDecimal("0.12228")).punF2(new BigDecimal("0.11984"))
                .punF3(new BigDecimal("0.10530")).build());
    }
}
