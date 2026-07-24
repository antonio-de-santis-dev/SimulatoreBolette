package it.simulatore.bollette.calculation;

import it.simulatore.bollette.entity.MeseBolletta;
import it.simulatore.bollette.entity.Offerta;
import it.simulatore.bollette.entity.ParametriGestore;
import it.simulatore.bollette.entity.ScaglioneConsumo;
import it.simulatore.bollette.entity.VoceCorrispettivo;
import it.simulatore.bollette.enums.BaseQuantita;
import it.simulatore.bollette.enums.CategoriaVoce;
import it.simulatore.bollette.enums.OrigineParametro;
import it.simulatore.bollette.enums.TipoCliente;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Motore di calcolo a righe, fedele al modello Excel.
 * <p>
 * Per ogni mese: {@code TOTALE = corrispettivo x quantita}, dove la quantita e' risolta
 * dalla {@link BaseQuantita}. Distingue KWH_NETTI da KWH_CON_PERDITE e applica le perdite
 * di rete alla quantita arrotondata.
 */
@Service
public class MotoreCalcolo {

    private static final int SCALE_IMPORTO = 6;

    /**
     * Risolve la quantita da moltiplicare per il corrispettivo, in base al tipo di base.
     */
    public BigDecimal risolviQuantita(BaseQuantita base, ConsumiFasce c, BigDecimal potenzaKw) {
        return switch (base) {
            case FISSO_MESE, POD_MESE -> BigDecimal.ONE;
            case KW_MESE -> nz(potenzaKw);
            case KWH_NETTI, KWH_NETTI_SCAGLIONE -> c.getTotaleNetto();
            case KWH_CON_PERDITE -> c.getTotaleConPerdite();
            case KWH_F1 -> nz(c.getF1());
            case KWH_F2 -> nz(c.getF2());
            case KWH_F3 -> nz(c.getF3());
            case PERDITE_F1 -> nz(c.getPerditeF1());
            case PERDITE_F2 -> nz(c.getPerditeF2());
            case PERDITE_F3 -> nz(c.getPerditeF3());
        };
    }

    /**
     * Calcola un mese a partire dai consumi di fascia e da una lista di voci gia' assemblata.
     * E' il cuore del motore, usato sia dall'applicazione sia dai test dei casi di verita'.
     */
    public RisultatoMese calcolaMese(BigDecimal f1, BigDecimal f2, BigDecimal f3,
                                     BigDecimal potenzaKw, List<VoceCorrispettivo> voci,
                                     BigDecimal coefficientePerdite, boolean arrotondaPerdite) {
        ConsumiFasce consumi = new ConsumiFasce();
        consumi.setF1(nz(f1));
        consumi.setF2(nz(f2));
        consumi.setF3(nz(f3));
        consumi.calcolaPerdite(coefficientePerdite, arrotondaPerdite);

        List<RigaCalcolata> righe = new ArrayList<>();
        Map<CategoriaVoce, BigDecimal> totali = new EnumMap<>(CategoriaVoce.class);
        for (CategoriaVoce cat : CategoriaVoce.values()) {
            totali.put(cat, BigDecimal.ZERO);
        }

        for (VoceCorrispettivo v : voci) {
            BigDecimal qta = risolviQuantita(v.getBase(), consumi, potenzaKw);
            BigDecimal importo = v.getCorrispettivo().multiply(qta)
                    .setScale(SCALE_IMPORTO, RoundingMode.HALF_UP);
            righe.add(new RigaCalcolata(v.getDescrizione(), v.getUnitaMisura(),
                    v.getCorrispettivo(), qta, importo, v.getCategoria(), v.getOrigine()));
            totali.merge(v.getCategoria(), importo, BigDecimal::add);
        }

        return new RisultatoMese(consumi, righe, totali);
    }

    /**
     * Calcola un mese secondo la normativa: assembla le voci del gestore (dipendenti dal tipo
     * cliente) e dell'offerta, poi aggiunge le componenti a scaglione (trasporto energia,
     * ASOS/ARIM variabili) e l'accisa con esenzione residenti.
     */
    public RisultatoMese calcolaMese(MeseBolletta mese, BigDecimal potenzaKw,
                                     Offerta offerta, ParametriGestore parametri, TipoCliente tipo) {
        List<VoceCorrispettivo> voci = new ArrayList<>(parametri.toVoci(tipo));
        if (offerta != null && offerta.getVoci() != null) {
            for (VoceCorrispettivo v : offerta.getVoci()) {
                if (!Boolean.FALSE.equals(v.getAttiva()) && appartieneAlMese(v, mese.getNumeroMese())) {
                    voci.add(v);
                }
            }
        }
        RisultatoMese base = calcolaMese(mese.getConsumoF1(), mese.getConsumoF2(), mese.getConsumoF3(),
                potenzaKw, voci, parametri.getPercentualePerdite(),
                Boolean.TRUE.equals(parametri.getArrotondaPerdite()));

        // Componenti a scaglione + accisa (dipendono dal consumo e dal tipo cliente)
        aggiungiScaglioni(base, parametri, tipo);
        aggiungiAccisa(base, mese, parametri, tipo, potenzaKw);
        return base;
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Componenti a scaglione (ARERA): trasporto energia, ASOS/ARIM variabili
    // ─────────────────────────────────────────────────────────────────────────────
    private void aggiungiScaglioni(RisultatoMese r, ParametriGestore p, TipoCliente tipo) {
        BigDecimal netti = r.getConsumi().getTotaleNetto();
        boolean applica = Boolean.TRUE.equals(p.getApplicaScaglioni());
        aggiungiComponenteScaglione(r, p, "TRASPORTO_ENERGIA", CategoriaVoce.TRASPORTO, tipo, netti, applica);
        aggiungiComponenteScaglione(r, p, "ASOS_VARIABILE", CategoriaVoce.ONERI_SISTEMA, tipo, netti, applica);
        aggiungiComponenteScaglione(r, p, "ARIM_VARIABILE", CategoriaVoce.ONERI_SISTEMA, tipo, netti, applica);
    }

    private void aggiungiComponenteScaglione(RisultatoMese r, ParametriGestore p, String componente,
                                             CategoriaVoce categoria, TipoCliente tipo,
                                             BigDecimal netti, boolean applicaScaglioni) {
        List<ScaglioneConsumo> scaglioni = p.getScaglioni() == null ? List.of() : p.getScaglioni().stream()
                .filter(s -> componente.equals(s.getComponente()))
                .filter(s -> s.getTipoCliente() == null || s.getTipoCliente() == tipo)
                .toList();
        if (scaglioni.isEmpty()) {
            return;
        }
        if (!applicaScaglioni) {
            // Modalita Excel: un unico prezzo su tutto il consumo (primo scaglione)
            ScaglioneConsumo s = scaglioni.get(0);
            aggiungiRiga(r, componente, categoria, s.getValore(), netti);
            return;
        }
        for (ScaglioneConsumo s : scaglioni) {
            BigDecimal qta = quantitaNelloScaglione(netti, s);
            if (qta.compareTo(BigDecimal.ZERO) > 0) {
                aggiungiRiga(r, componente, categoria, s.getValore(), qta);
            }
        }
    }

    private void aggiungiRiga(RisultatoMese r, String descr, CategoriaVoce cat,
                              BigDecimal corrispettivo, BigDecimal qta) {
        BigDecimal importo = corrispettivo.multiply(qta).setScale(SCALE_IMPORTO, RoundingMode.HALF_UP);
        r.getRighe().add(new RigaCalcolata(descr, "EUR/kWh", corrispettivo, qta, importo,
                cat, OrigineParametro.NAZIONALE));
        r.getTotaliPerCategoria().merge(cat, importo, BigDecimal::add);
    }

    /** Porzione di consumo che ricade nello scaglione [limiteInferiore, limiteSuperiore). */
    public BigDecimal quantitaNelloScaglione(BigDecimal consumo, ScaglioneConsumo s) {
        if (s == null) {
            return consumo;
        }
        BigDecimal sotto = consumo.subtract(s.getLimiteInferiore()).max(BigDecimal.ZERO);
        if (s.getLimiteSuperiore() == null) {
            return sotto;
        }
        BigDecimal ampiezza = s.getLimiteSuperiore().subtract(s.getLimiteInferiore());
        return sotto.min(ampiezza);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Accisa con esenzione per i domestici residenti fino a 3 kW (TUA, DLgs 504/95)
    // ─────────────────────────────────────────────────────────────────────────────
    private void aggiungiAccisa(RisultatoMese r, MeseBolletta mese, ParametriGestore p,
                                TipoCliente tipo, BigDecimal potenzaKw) {
        int giorni = mese.getGiorniPeriodo() != null ? mese.getGiorniPeriodo() : 30;
        BigDecimal accisa = calcolaAccisa(r.getConsumi(), tipo, potenzaKw, p, giorni);
        if (accisa.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal kwh = r.getConsumi().getTotaleNetto();
            BigDecimal aliquota = kwh.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                    : accisa.divide(kwh, 8, RoundingMode.HALF_UP);
            r.getRighe().add(new RigaCalcolata("Accisa (imposta erariale)", "EUR/kWh",
                    aliquota, kwh, accisa, CategoriaVoce.IMPOSTE, OrigineParametro.NAZIONALE));
            r.getTotaliPerCategoria().merge(CategoriaVoce.IMPOSTE, accisa, BigDecimal::add);
        }
    }

    /**
     * Accisa con esenzione per i domestici residenti fino a 3 kW.
     * Riferimento: Testo Unico Accise (DLgs 504/95). La soglia mensile e' ragguagliata ai
     * giorni effettivi del periodo. Flag {@code applicaEsenzioneAccisaResidenti}.
     */
    public BigDecimal calcolaAccisa(ConsumiFasce consumi, TipoCliente tipo, BigDecimal potenzaKw,
                                    ParametriGestore p, int giorni) {
        BigDecimal aliquota = tipo != null && tipo.isUsoDomestico()
                ? p.getAccisaDomestico() : p.getAccisaNonDomestico();
        if (aliquota == null) {
            aliquota = p.getAccisaDomestico();
        }
        BigDecimal kwh = consumi.getTotaleNetto();

        boolean esente = Boolean.TRUE.equals(p.getApplicaEsenzioneAccisaResidenti())
                && tipo != null && tipo.isResidente()
                && p.getPotenzaMaxEsenzioneKw() != null
                && nz(potenzaKw).compareTo(p.getPotenzaMaxEsenzioneKw()) <= 0;

        if (!esente) {
            return kwh.multiply(nz(aliquota)).setScale(4, RoundingMode.HALF_UP);
        }

        BigDecimal sogliaMese = p.getSogliaEsenzioneKwhMese() != null
                ? p.getSogliaEsenzioneKwhMese() : new BigDecimal("150");
        BigDecimal soglia = sogliaMese.multiply(BigDecimal.valueOf(giorni))
                .divide(new BigDecimal("30"), 6, RoundingMode.HALF_UP);
        BigDecimal imponibile = kwh.subtract(soglia).max(BigDecimal.ZERO);
        return imponibile.multiply(nz(aliquota)).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * Aggrega uno o due mesi in una bolletta completa: somma le categorie, applica le altre
     * partite (soggette a IVA nell'imponibile, non soggette dopo l'IVA) e calcola l'IVA.
     * Replica la struttura del "quadro sintetico" del foglio Excel.
     *
     * @param altrePartiteSoggette   totale altre partite soggette a IVA (nell'imponibile)
     * @param altrePartiteNonSoggette totale altre partite non soggette (sommate dopo l'IVA)
     */
    public RisultatoBolletta aggregaBolletta(List<RisultatoMese> mesi,
                                             BigDecimal altrePartiteSoggette,
                                             BigDecimal altrePartiteNonSoggette,
                                             BigDecimal aliquotaIva) {
        Map<CategoriaVoce, BigDecimal> totali = new EnumMap<>(CategoriaVoce.class);
        for (CategoriaVoce cat : CategoriaVoce.values()) {
            totali.put(cat, BigDecimal.ZERO);
        }
        for (RisultatoMese rm : mesi) {
            for (CategoriaVoce cat : CategoriaVoce.values()) {
                totali.merge(cat, rm.getTotale(cat), BigDecimal::add);
            }
        }

        BigDecimal soggette = nz(altrePartiteSoggette);
        BigDecimal nonSoggette = nz(altrePartiteNonSoggette);

        BigDecimal imponibile = totali.get(CategoriaVoce.MATERIA_ENERGIA)
                .add(totali.get(CategoriaVoce.TRASPORTO))
                .add(totali.get(CategoriaVoce.ONERI_SISTEMA))
                .add(totali.get(CategoriaVoce.IMPOSTE))
                .add(totali.get(CategoriaVoce.ALTRE_PARTITE))
                .add(soggette)
                .setScale(6, RoundingMode.HALF_UP);

        BigDecimal iva = imponibile.multiply(nz(aliquotaIva)).setScale(6, RoundingMode.HALF_UP);
        BigDecimal totale = imponibile.add(iva).add(nonSoggette).setScale(6, RoundingMode.HALF_UP);

        return new RisultatoBolletta(mesi, totali, soggette, nonSoggette,
                imponibile, nz(aliquotaIva), iva, totale);
    }

    private boolean appartieneAlMese(VoceCorrispettivo v, Integer numeroMese) {
        Integer m = v.getNumeroMese();
        return m == null || m == 0 || m.equals(numeroMese);
    }

    private static BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}
