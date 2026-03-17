package it.unibs.ingsoft.v3.service;

import it.unibs.ingsoft.v3.model.*;
import it.unibs.ingsoft.v3.persistence.AppData;
import it.unibs.ingsoft.v3.persistence.IPersistenceService;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public final class IscrizioneService
{
    // Reuse the constant defined in PropostaService — single source of truth
    private static final String CAMPO_NUM_PARTECIPANTI = PropostaService.CAMPO_NUM_PARTECIPANTI;

    private final IPersistenceService db;
    private final AppData             data;
    private final NotificaListener    notificaListener;

    /**
     * @pre db               != null
     * @pre data             != null
     * @pre notificaListener != null
     */
    public IscrizioneService(IPersistenceService db, AppData data, NotificaListener notificaListener)
    {
        this.db               = Objects.requireNonNull(db);
        this.data             = Objects.requireNonNull(data);
        this.notificaListener = Objects.requireNonNull(notificaListener);
    }

    // ---------------------------------------------------------------
    // STARTUP CHECK
    // ---------------------------------------------------------------

    /**
     * Called once on every startup.
     * Loops through all open proposals and transitions any whose
     * "Termine ultimo di iscrizione" has passed.
     */
    public void controllaScadenzeAlAvvio()
    {
        LocalDate oggi = LocalDate.now();
        boolean modificato = false;

        for (Proposta p : data.getProposte())
        {
            if (p.getStato() != StatoProposta.APERTA)
                continue;

            // Deadline has passed (strictly after termineIscrizione)
            if (p.getTermineIscrizione() != null && !oggi.isAfter(p.getTermineIscrizione()))
                continue;

            int iscritti = p.getNumeroIscritti();
            int maxPart;
            try
            {
                maxPart = getMaxPartecipanti(p);
            }
            catch (IllegalStateException e)
            {
                annulla(p, oggi);
                modificato = true;
                continue;
            }

            if (iscritti >= maxPart)
                conferma(p, oggi);
            else
                annulla(p, oggi);

            modificato = true;
        }

        for (Proposta p : data.getProposte())
        {
            if (p.getStato() != StatoProposta.CONFERMATA)
                continue;

            LocalDate dataConclus = p.getDataConclus();
            if (dataConclus != null && oggi.isAfter(dataConclus))
            {
                p.setStato(StatoProposta.CONCLUSA, oggi);
                modificato = true;
            }
        }

        if (modificato)
            db.save(data);
    }

    // ---------------------------------------------------------------
    // ISCRIZIONE
    // ---------------------------------------------------------------

    /**
     * Signs up a fruitore to an open proposal.
     * Checks:
     * - Proposal must be APERTA
     * - Deadline must not have passed
     * - Fruitore must not already be signed up
     * - Proposal must not be full
     *
     * @pre fruitore != null
     * @pre proposta != null
     * @pre proposta.getStato() == StatoProposta.APERTA
     * @pre !proposta.isIscrittoFruitore(fruitore.getUsername())
     * @pre LocalDate.now().isAfter(proposta.getTermineIscrizione()) == false
     * @pre proposta.getNumeroIscritti() &lt; maxPartecipanti
     * @post proposta.isIscrittoFruitore(fruitore.getUsername())
     * @throws IllegalStateException if any precondition is violated
     */
    public void iscrivi(Fruitore fruitore, Proposta proposta)
    {
        // 1. Must be open
        if (proposta.getStato() != StatoProposta.APERTA)
            throw new IllegalStateException("La proposta non è aperta alle iscrizioni.");

        // 2. Deadline must not have passed
        LocalDate oggi = LocalDate.now();
        if (proposta.getTermineIscrizione() != null &&
                oggi.isAfter(proposta.getTermineIscrizione()))
            throw new IllegalStateException("Il termine di iscrizione è scaduto.");

        // 3. Not already signed up
        if (proposta.isIscrittoFruitore(fruitore.getUsername()))
            throw new IllegalStateException("Sei già iscritto a questa proposta.");

        // 4. Capacity check
        int max = getMaxPartecipanti(proposta);
        if (proposta.getNumeroIscritti() >= max)
            throw new IllegalStateException(
                    "La proposta ha già raggiunto il numero massimo di partecipanti (" + max + ").");

        // All checks passed — register
        Iscrizione i = new Iscrizione(fruitore, oggi);
        proposta.addIscrizione(i);
        db.save(data);
    }

    // ---------------------------------------------------------------
    // STATE TRANSITIONS
    // ---------------------------------------------------------------

    /**
     * Confirms a proposal and notifies all adherents.
     */
    private void conferma(Proposta p, LocalDate oggi)
    {
        p.setStato(StatoProposta.CONFERMATA, oggi);

        String titolo = p.getValoriCampi().getOrDefault(PropostaService.CAMPO_TITOLO, "senza titolo");
        String data   = p.getValoriCampi().getOrDefault(PropostaService.CAMPO_DATA,   "");
        String ora    = p.getValoriCampi().getOrDefault(PropostaService.CAMPO_ORA,    "");
        String luogo  = p.getValoriCampi().getOrDefault(PropostaService.CAMPO_LUOGO,  "");
        String quota  = p.getValoriCampi().getOrDefault(PropostaService.CAMPO_QUOTA,  "");

        String messaggio =
                "✔ L'iniziativa \"" + titolo + "\" è CONFERMATA!\n" +
                        "  Data: " + data + " | Ora: " + ora + "\n" +
                        "  Luogo: " + luogo + "\n" +
                        "  Quota individuale: " + quota;

        notificaTutti(p, messaggio);
    }

    /**
     * Cancels a proposal and notifies all adherents.
     */
    private void annulla(Proposta p, LocalDate oggi)
    {
        p.setStato(StatoProposta.ANNULLATA, oggi);

        String titolo    = p.getValoriCampi().getOrDefault(PropostaService.CAMPO_TITOLO, "senza titolo");
        String messaggio = "✘ L'iniziativa \"" + titolo + "\" è stata ANNULLATA " +
                "per insufficienza di iscrizioni.";

        notificaTutti(p, messaggio);
    }

    /**
     * Sends a notification to all fruitori signed up to a proposal.
     */
    private void notificaTutti(Proposta p, String messaggio)
    {
        for (Iscrizione i : p.getIscrizioni())
            notificaListener.notifica(i.getFruitore().getUsername(), messaggio);
    }

    // ---------------------------------------------------------------
    // UTILITY
    // ---------------------------------------------------------------

    /**
     * Reads "Numero di partecipanti" from the proposal's field values.
     * Throws IllegalStateException if the field is missing or not a valid integer.
     */
    private int getMaxPartecipanti(Proposta p)
    {
        String val = p.getValoriCampi().get(CAMPO_NUM_PARTECIPANTI);

        if (val == null || val.isBlank())
            throw new IllegalStateException(
                    "Campo \"" + CAMPO_NUM_PARTECIPANTI + "\" mancante nella proposta.");

        try
        {
            return Integer.parseInt(val.trim());
        }
        catch (NumberFormatException e)
        {
            throw new IllegalStateException(
                    "Campo \"" + CAMPO_NUM_PARTECIPANTI + "\" non è un intero valido: " + val);
        }
    }
}