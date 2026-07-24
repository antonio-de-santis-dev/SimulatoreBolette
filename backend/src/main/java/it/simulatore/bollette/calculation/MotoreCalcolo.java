package it.simulatore.bollette.calculation;

import it.simulatore.bollette.entity.MeseBolletta;
import it.simulatore.bollette.entity.Offerta;
import it.simulatore.bollette.entity.ParametriGestore;
import it.simulatore.bollette.entity.VoceCorrispettivo;
import it.simulatore.bollette.enums.BaseQuantita;
import it.simulatore.bollette.enums.CategoriaVoce;
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
            case KWH_NETTI -> c.getTotaleNetto();
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
     * Calcola un mese assemblando le voci del gestore (parametri.toVoci) e quelle
     * dell'offerta (quote energia per fascia e perdite di rete), filtrate per mese.
     */
    public RisultatoMese calcolaMese(MeseBolletta mese, BigDecimal potenzaKw,
                                     Offerta offerta, ParametriGestore parametri) {
        List<VoceCorrispettivo> voci = new ArrayList<>(parametri.toVoci());
        if (offerta != null && offerta.getVoci() != null) {
            for (VoceCorrispettivo v : offerta.getVoci()) {
                if (!Boolean.FALSE.equals(v.getAttiva()) && appartieneAlMese(v, mese.getNumeroMese())) {
                    voci.add(v);
                }
            }
        }
        return calcolaMese(mese.getConsumoF1(), mese.getConsumoF2(), mese.getConsumoF3(),
                potenzaKw, voci, parametri.getPercentualePerdite(),
                Boolean.TRUE.equals(parametri.getArrotondaPerdite()));
    }

    private boolean appartieneAlMese(VoceCorrispettivo v, Integer numeroMese) {
        Integer m = v.getNumeroMese();
        return m == null || m == 0 || m.equals(numeroMese);
    }

    private static BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}
