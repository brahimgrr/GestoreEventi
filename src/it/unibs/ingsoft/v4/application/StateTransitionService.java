package it.unibs.ingsoft.v4.application;

import it.unibs.ingsoft.v4.domain.*;
import it.unibs.ingsoft.v4.persistence.api.IBachecaRepository;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Gestisce i cambi di stato automatici (mezzanotte), immediati
 * (capienza massima raggiunta) e manuali (ritiro da parte del configuratore).
 */
public final class StateTransitionService {

    private final IBachecaRepository bachecaRepo;
    private final NotificationService notificationService;
    private final ReentrantLock lock = new ReentrantLock();

    public StateTransitionService(IBachecaRepository bachecaRepo, NotificationService notificationService) {
        this.bachecaRepo = Objects.requireNonNull(bachecaRepo);
        this.notificationService = Objects.requireNonNull(notificationService);
    }

    /**
     * Da invocare all'avvio dell'applicazione. Controlla tutte le proposte
     * e valuta se devono cambiare stato perche' "e' passata la mezzanotte".
     */
    public void controllaScadenze() {
        lock.lock();
        try {
            LocalDate oggi = LocalDate.now(AppConstants.clock);
            boolean changed = false;

            Bacheca bacheca = bachecaRepo.get();
            for (Proposta p : bacheca.getProposte()) {
                if (p.getStato() == StatoProposta.APERTA) {
                    if (p.getTermineIscrizione() != null && oggi.isAfter(p.getTermineIscrizione())) {
                        if (p.getListaAderenti().size() == p.getNumeroPartecipanti()) {
                            confermaProposta(p);
                        } else {
                            annullaProposta(p);
                        }
                        changed = true;
                    }
                } else if (p.getStato() == StatoProposta.CONFERMATA) {
                    LocalDate dataConclusiva = getDataConclusiva(p);
                    if (dataConclusiva != null && oggi.isAfter(dataConclusiva)) {
                        concludiProposta(p);
                        changed = true;
                    }
                }
            }

            if (changed) {
                bachecaRepo.save();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Transizione manuale o indotta dal raggiungimento della capienza.
     */
    public void confermaProposta(Proposta p) {
        lock.lock();
        try {
            if (p.getStato() != StatoProposta.APERTA) return;
            p.setStato(StatoProposta.CONFERMATA);

            String quota = p.getValoriCampi().getOrDefault(PropostaService.CAMPO_QUOTA, "").trim();
            String info = "La proposta \"" + p.getValoriCampi().getOrDefault(PropostaService.CAMPO_TITOLO, "Senza titolo")
                    + "\" e' stata CONFERMATA.\n"
                    + "Data: " + p.getValoriCampi().getOrDefault(PropostaService.CAMPO_DATA, "") + "\n"
                    + "Ora: " + p.getValoriCampi().getOrDefault(PropostaService.CAMPO_ORA, "") + "\n"
                    + "Luogo: " + p.getValoriCampi().getOrDefault(PropostaService.CAMPO_LUOGO, "") + "\n"
                    + (quota.isBlank() ? "" : "Quota: " + quota + "\n");

            String messaggio = info.trim();
            for (String aderente : p.getListaAderenti()) {
                notificationService.inviaNotifica(aderente, new Notifica(messaggio));
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Ritira una proposta APERTA o CONFERMATA (→ RITIRATA).
     * Notifica tutti gli aderenti del ritiro.
     *
     * @throws IllegalStateException se la proposta non è APERTA né CONFERMATA,
     *                               o se il termine per il ritiro è scaduto.
     */
    public void ritiraProposta(Proposta p) {
        lock.lock();
        try {
            StatoProposta stato = p.getStato();
            if (stato != StatoProposta.APERTA && stato != StatoProposta.CONFERMATA) {
                throw new IllegalStateException(
                        "Impossibile ritirare: la proposta non è APERTA né CONFERMATA.");
            }

            LocalDate oggi = LocalDate.now(AppConstants.clock);
            if (p.getDataEvento() != null && !oggi.isBefore(p.getDataEvento())) {
                throw new IllegalStateException(
                        "Impossibile ritirare: il ritiro è consentito solo entro il giorno precedente la data dell'evento.");
            }

            p.setStato(StatoProposta.RITIRATA);

            String titolo = p.getValoriCampi()
                    .getOrDefault(PropostaService.CAMPO_TITOLO, "Senza titolo");
            String messaggio = "La proposta \"" + titolo
                    + "\" e' stata RITIRATA dal configuratore.";
            for (String aderente : p.getListaAderenti()) {
                notificationService.inviaNotifica(aderente, new Notifica(messaggio));
            }

            bachecaRepo.save();
        } finally {
            lock.unlock();
        }
    }

    private void annullaProposta(Proposta p) {
        if (p.getStato() != StatoProposta.APERTA) return;
        p.setStato(StatoProposta.ANNULLATA);

        String messaggio = "La proposta \"" + p.getValoriCampi().getOrDefault(PropostaService.CAMPO_TITOLO, "Senza titolo")
                + "\" e' stata ANNULLATA per mancato raggiungimento del numero di partecipanti.";

        for (String aderente : p.getListaAderenti()) {
            notificationService.inviaNotifica(aderente, new Notifica(messaggio));
        }
    }

    private void concludiProposta(Proposta p) {
        if (p.getStato() != StatoProposta.CONFERMATA) return;
        p.setStato(StatoProposta.CONCLUSA);
    }

    private LocalDate getDataConclusiva(Proposta p) {
        String s = p.getValoriCampi().get(PropostaService.CAMPO_DATA_CONCLUSIVA);
        if (s == null || s.isBlank()) {
            return p.getDataEvento();
        }
        try {
            return LocalDate.parse(s.trim(), AppConstants.DATE_FMT);
        } catch (DateTimeParseException e) {
            return p.getDataEvento();
        }
    }
}
