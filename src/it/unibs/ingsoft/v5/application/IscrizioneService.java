package it.unibs.ingsoft.v5.application;

import it.unibs.ingsoft.v5.domain.AppConstants;
import it.unibs.ingsoft.v5.domain.Fruitore;
import it.unibs.ingsoft.v5.domain.Proposta;
import it.unibs.ingsoft.v5.domain.StatoProposta;
import it.unibs.ingsoft.v5.persistence.api.IBachecaRepository;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Gestisce l'iscrizione e la disiscrizione a una proposta.
 */
public final class IscrizioneService {
    
    private final IBachecaRepository bachecaRepo;
    private final StateTransitionService stateTransitionService;

    public IscrizioneService(IBachecaRepository bachecaRepo, StateTransitionService stateTransitionService) {
        this.bachecaRepo = Objects.requireNonNull(bachecaRepo);
        this.stateTransitionService = Objects.requireNonNull(stateTransitionService);
    }

    /**
     * Iscrive il fruitore alla proposta specificata.
     * 
     * @param p la proposta a cui iscriversi
     * @param f il fruitore che richiede l'iscrizione
     * @throws IllegalStateException se la proposta non è APERTA, è scaduta, piena o l'utente è già iscritto.
     */
    public void iscrivi(Proposta p, Fruitore f) {
        if (p.getStato() != StatoProposta.APERTA) {
            throw new IllegalStateException("Impossibile iscriversi: la proposta non è APERTA.");
        }
        
        LocalDate oggi = LocalDate.now(AppConstants.clock);
        if (p.getTermineIscrizione() != null && oggi.isAfter(p.getTermineIscrizione())) {
            throw new IllegalStateException("Impossibile iscriversi: il termine di iscrizione è scaduto (" + p.getTermineIscrizione() + ").");
        }
        
        if (p.getListaAderenti().contains(f.getUsername())) {
            throw new IllegalStateException("Sei già iscritto a questa proposta.");
        }
        
        int numeroPartecipantiPrevisto = p.getNumeroPartecipanti();
        if (p.getListaAderenti().size() >= numeroPartecipantiPrevisto) {
            throw new IllegalStateException("Impossibile iscriversi: la proposta ha già raggiunto il numero massimo di partecipanti.");
        }
        
        // Tutto OK: esegui iscrizione
        p.addAderente(f.getUsername());
        
        // Controlla se abbiamo raggiunto il max
        if (p.getListaAderenti().size() == numeroPartecipantiPrevisto) {
            stateTransitionService.confermaProposta(p);
        }
        
        bachecaRepo.save();
    }

    /**
     * Disdice l'iscrizione del fruitore alla proposta specificata.
     *
     * @param p la proposta da cui disdire l'iscrizione
     * @param f il fruitore che richiede la disiscrizione
     * @throws IllegalStateException se la proposta non è APERTA, il termine è scaduto,
     *                               o l'utente non è iscritto.
     */
    public void disiscrivi(Proposta p, Fruitore f) {
        if (p.getStato() != StatoProposta.APERTA) {
            throw new IllegalStateException("Impossibile disdire: la proposta non è APERTA.");
        }

        LocalDate oggi = LocalDate.now(AppConstants.clock);
        if (p.getTermineIscrizione() != null && oggi.isAfter(p.getTermineIscrizione())) {
            throw new IllegalStateException("Impossibile disdire: il termine di iscrizione è scaduto (" + p.getTermineIscrizione() + ").");
        }

        if (!p.getListaAderenti().contains(f.getUsername())) {
            throw new IllegalStateException("Non sei iscritto a questa proposta.");
        }

        p.removeAderente(f.getUsername());
        bachecaRepo.save();
    }
}
