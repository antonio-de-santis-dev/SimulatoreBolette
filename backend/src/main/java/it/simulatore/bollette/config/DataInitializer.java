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

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    @Bean
    CommandLineRunner initData(
            OffertaRepository offertaRepo,
            ParametroARERARepository parametroRepo,
            PunMensileRepository punRepo) {

        return args -> {
            if (parametroRepo.count() == 0) {
                parametroRepo.save(ParametroARERA.builder()
                    .nomeParametro("TAU_FISSA").descrizione("Trasporto quota fissa mensile")
                    .valore(new BigDecimal("3.80")).unitaMisura("€/mese")
                    .validoDal(LocalDate.of(2026, 4, 1)).build());

                parametroRepo.save(ParametroARERA.builder()
                    .nomeParametro("TAU_POTENZA").descrizione("Trasporto quota potenza")
                    .valore(new BigDecimal("7.93")).unitaMisura("€/kW/anno")
                    .validoDal(LocalDate.of(2026, 4, 1)).build());

                parametroRepo.save(ParametroARERA.builder()
                    .nomeParametro("TAU_ENERGIA").descrizione("Trasporto quota energia")
                    .valore(new BigDecimal("0.0156")).unitaMisura("€/kWh")
                    .validoDal(LocalDate.of(2026, 4, 1)).build());

                parametroRepo.save(ParametroARERA.builder()
                    .nomeParametro("UC3").descrizione("Uso rete e dispacciamento")
                    .valore(new BigDecimal("0.008")).unitaMisura("€/kWh")
                    .validoDal(LocalDate.of(2026, 4, 1)).build());

                parametroRepo.save(ParametroARERA.builder()
                    .nomeParametro("ASOS").descrizione("Oneri sistema ASOS")
                    .valore(new BigDecimal("0.020")).unitaMisura("€/kWh")
                    .validoDal(LocalDate.of(2026, 4, 1)).build());

                parametroRepo.save(ParametroARERA.builder()
                    .nomeParametro("ARIM").descrizione("Oneri sistema ARIM")
                    .valore(new BigDecimal("0.010")).unitaMisura("€/kWh")
                    .validoDal(LocalDate.of(2026, 4, 1)).build());

                parametroRepo.save(ParametroARERA.builder()
                    .nomeParametro("ACCISA_AGEVOLATA").descrizione("Accisa agevolata residenti")
                    .valore(new BigDecimal("0.0227")).unitaMisura("€/kWh")
                    .validoDal(LocalDate.of(2026, 4, 1)).build());
            }

            if (punRepo.count() == 0) {
                punRepo.save(PunMensile.builder().anno(2026).mese(1)
                    .punMonorario(new BigDecimal("0.132665"))
                    .punF1(new BigDecimal("0.15126")).punF2(new BigDecimal("0.13740"))
                    .punF3(new BigDecimal("0.11829")).build());

                punRepo.save(PunMensile.builder().anno(2026).mese(2)
                    .punMonorario(new BigDecimal("0.114405"))
                    .punF1(new BigDecimal("0.12228")).punF2(new BigDecimal("0.11984"))
                    .punF3(new BigDecimal("0.10530")).build());

                punRepo.save(PunMensile.builder().anno(2026).mese(3)
                    .punMonorario(new BigDecimal("0.14340"))
                    .punF1(new BigDecimal("0.14302")).punF2(new BigDecimal("0.15391"))
                    .punF3(new BigDecimal("0.13809")).build());

                punRepo.save(PunMensile.builder().anno(2026).mese(4)
                    .punMonorario(new BigDecimal("0.11947"))
                    .punF1(new BigDecimal("0.11114")).punF2(new BigDecimal("0.13826"))
                    .punF3(new BigDecimal("0.11663")).build());

                punRepo.save(PunMensile.builder().anno(2026).mese(5)
                    .punMonorario(new BigDecimal("0.11935"))
                    .punF1(new BigDecimal("0.10717")).punF2(new BigDecimal("0.13144"))
                    .punF3(new BigDecimal("0.12081")).build());

                punRepo.save(PunMensile.builder().anno(2026).mese(6)
                    .punMonorario(new BigDecimal("0.13250"))
                    .punF1(new BigDecimal("0.12576")).punF2(new BigDecimal("0.15170"))
                    .punF3(new BigDecimal("0.12724")).build());
            }

            if (offertaRepo.count() == 0) {
                offertaRepo.save(Offerta.builder()
                    .nomeFornitore("Enel Energia").nomeOfferta("Luce Sicura")
                    .tipoOfferta(TipoOfferta.PREZZO_FISSO).tipoTariffa(TipoTariffa.MONORARIA)
                    .prezzoFissoF0(new BigDecimal("0.2850"))
                    .pcvAnnuo(new BigDecimal("120.00"))
                    .condizioniSpeciali("Prezzo bloccato 12 mesi").build());

                offertaRepo.save(Offerta.builder()
                    .nomeFornitore("Enel Energia").nomeOfferta("Luce Sicura Bioraria")
                    .tipoOfferta(TipoOfferta.PREZZO_FISSO).tipoTariffa(TipoTariffa.BIORARIA)
                    .prezzoFissoF1(new BigDecimal("0.3100")).prezzoFissoF23(new BigDecimal("0.2600"))
                    .pcvAnnuo(new BigDecimal("120.00")).build());

                offertaRepo.save(Offerta.builder()
                    .nomeFornitore("Edison").nomeOfferta("PUN Zero")
                    .tipoOfferta(TipoOfferta.INDICIZZATA_PUN).tipoTariffa(TipoTariffa.MONORARIA)
                    .spreadPunF0(new BigDecimal("0.0150"))
                    .pcvAnnuo(new BigDecimal("60.00"))
                    .condizioniSpeciali("Solo spread, nessun mark-up").build());

                offertaRepo.save(Offerta.builder()
                    .nomeFornitore("Eni Plenitude").nomeOfferta("Weboluce")
                    .tipoOfferta(TipoOfferta.PREZZO_FISSO).tipoTariffa(TipoTariffa.MONORARIA)
                    .prezzoFissoF0(new BigDecimal("0.2750"))
                    .pcvAnnuo(new BigDecimal("96.00"))
                    .condizioniSpeciali("Sconto 10% con bolletta digitale").build());

                offertaRepo.save(Offerta.builder()
                    .nomeFornitore("A2A").nomeOfferta("A2A Energia Libera")
                    .tipoOfferta(TipoOfferta.INDICIZZATA_PUN).tipoTariffa(TipoTariffa.BIORARIA)
                    .spreadPunF1(new BigDecimal("0.0120")).spreadPunF23(new BigDecimal("0.0080"))
                    .pcvAnnuo(new BigDecimal("84.00")).build());

                offertaRepo.save(Offerta.builder()
                    .nomeFornitore("Fastweb Energia").nomeOfferta("Luce Super")
                    .tipoOfferta(TipoOfferta.PREZZO_FISSO).tipoTariffa(TipoTariffa.MONORARIA)
                    .prezzoFissoF0(new BigDecimal("0.2680"))
                    .pcvAnnuo(new BigDecimal("0"))
                    .condizioniSpeciali("Zero PCV per clienti Fastweb").build());
            }
        };
    }
}
