package it.simulatore.bollette.service;

import it.simulatore.bollette.entity.AltraPartita;
import it.simulatore.bollette.entity.BollettaConcorrente;
import it.simulatore.bollette.entity.MeseBolletta;
import it.simulatore.bollette.exception.ResourceNotFoundException;
import it.simulatore.bollette.repository.BollettaConcorrenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BollettaConcorrenteService {

    private final BollettaConcorrenteRepository repository;

    public List<BollettaConcorrente> findAll() {
        return repository.findAll();
    }

    public BollettaConcorrente findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bolletta concorrente", id));
    }

    @Transactional
    public BollettaConcorrente create(BollettaConcorrente b) {
        b.setId(null);
        collegaFigli(b);
        return repository.save(b);
    }

    @Transactional
    public BollettaConcorrente update(Long id, BollettaConcorrente modifiche) {
        BollettaConcorrente esistente = findById(id);
        modifiche.setId(id);
        modifiche.setCreatedAt(esistente.getCreatedAt());
        // rimpiazza i figli: svuota le collezioni gestite da orphanRemoval
        esistente.getMesi().clear();
        esistente.getAltrePartite().clear();
        copiaCampi(modifiche, esistente);
        esistente.getMesi().addAll(modifiche.getMesi());
        esistente.getAltrePartite().addAll(modifiche.getAltrePartite());
        collegaFigli(esistente);
        return repository.save(esistente);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(findById(id));
    }

    @Transactional
    public BollettaConcorrente duplica(Long id) {
        BollettaConcorrente src = findById(id);
        BollettaConcorrente copia = clona(src);
        return repository.save(copia);
    }

    public boolean quadratura(Long id) {
        return findById(id).quadraturaImportiValida();
    }

    private void collegaFigli(BollettaConcorrente b) {
        if (b.getMesi() != null) {
            for (MeseBolletta m : b.getMesi()) {
                m.setBolletta(b);
            }
        }
        if (b.getAltrePartite() != null) {
            for (AltraPartita a : b.getAltrePartite()) {
                a.setBolletta(b);
            }
        }
    }

    private void copiaCampi(BollettaConcorrente src, BollettaConcorrente dst) {
        dst.setNumeroFattura(src.getNumeroFattura());
        dst.setDataFattura(src.getDataFattura());
        dst.setPeriodoDal(src.getPeriodoDal());
        dst.setPeriodoAl(src.getPeriodoAl());
        dst.setNomeFornitore(src.getNomeFornitore());
        dst.setNomeOfferta(src.getNomeOfferta());
        dst.setCodiceOfferta(src.getCodiceOfferta());
        dst.setRagioneSociale(src.getRagioneSociale());
        dst.setIndirizzoFornitura(src.getIndirizzoFornitura());
        dst.setPod(src.getPod());
        dst.setTipologiaCliente(src.getTipologiaCliente());
        dst.setOpzioneTariffaria(src.getOpzioneTariffaria());
        dst.setPotenzaImpegnata(src.getPotenzaImpegnata());
        dst.setPotenzaDisponibile(src.getPotenzaDisponibile());
        dst.setLivelloTensione(src.getLivelloTensione());
        dst.setFatturatoMateriaEnergia(src.getFatturatoMateriaEnergia());
        dst.setFatturatoTrasporto(src.getFatturatoTrasporto());
        dst.setFatturatoOneriSistema(src.getFatturatoOneriSistema());
        dst.setFatturatoAltrePartite(src.getFatturatoAltrePartite());
        dst.setFatturatoImposte(src.getFatturatoImposte());
        dst.setFatturatoIva(src.getFatturatoIva());
        dst.setFatturatoImponibile(src.getFatturatoImponibile());
        dst.setFatturatoTotale(src.getFatturatoTotale());
        dst.setAliquotaIvaApplicata(src.getAliquotaIvaApplicata());
        dst.setNote(src.getNote());
    }

    private BollettaConcorrente clona(BollettaConcorrente s) {
        BollettaConcorrente c = new BollettaConcorrente();
        copiaCampi(s, c);
        c.setNumeroFattura(s.getNumeroFattura());
        c.setNomeFornitore(s.getNomeFornitore());
        c.setNote((s.getNote() != null ? s.getNote() + " " : "") + "[copia]");
        List<MeseBolletta> mesi = new ArrayList<>();
        for (MeseBolletta m : s.getMesi()) {
            mesi.add(MeseBolletta.builder()
                    .bolletta(c).numeroMese(m.getNumeroMese()).nomeMese(m.getNomeMese())
                    .mese(m.getMese()).anno(m.getAnno())
                    .consumoF1(m.getConsumoF1()).consumoF2(m.getConsumoF2()).consumoF3(m.getConsumoF3())
                    .build());
        }
        c.setMesi(mesi);
        List<AltraPartita> partite = new ArrayList<>();
        for (AltraPartita a : s.getAltrePartite()) {
            partite.add(AltraPartita.builder()
                    .bolletta(c).descrizione(a.getDescrizione()).dal(a.getDal()).al(a.getAl())
                    .importo(a.getImporto()).aliquotaIva(a.getAliquotaIva())
                    .soggettaIva(a.getSoggettaIva()).ordine(a.getOrdine())
                    .build());
        }
        c.setAltrePartite(partite);
        return c;
    }
}
