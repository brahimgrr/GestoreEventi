package it.unibs.ingsoft.v5.service;

import it.unibs.ingsoft.v5.model.Fruitore;
import it.unibs.ingsoft.v5.model.Iscrizione;
import it.unibs.ingsoft.v5.model.Proposta;
import it.unibs.ingsoft.v5.model.StatoProposta;
import it.unibs.ingsoft.v5.persistence.AppData;
import it.unibs.ingsoft.v5.persistence.DatabaseService;

import java.time.LocalDate;
import java.util.Objects;

public final class IscrizioneService
{
    // Name of the base field that stores max participants
    private static final String CAMPO_NUM_PARTECIPANTI = "Numero di partecipanti";

    private final DatabaseService db;
    private final AppData data;
    private final FruitoreService fruitoreService;

    public IscrizioneService(DatabaseService db, AppData data, FruitoreService fruitoreService)
    {
        this.db              = Objects.requireNonNull(db);
        this.data            = Objects.requireNonNull(data);
        this.fruitoreService = Objects.requireNonNull(fruitoreService);
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

            int iscritti      = p.getNumeroIscritti();
            int maxPart       = getMaxPartecipanti(p);

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

            LocalDate dataConclus = p.getDataEvento(); // or parse from valoriCampi "Data conclusiva"
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
        Iscrizione i = new Iscrizione(fruitore, proposta, oggi);
        proposta.addIscrizione(i);
        db.save(data);
    }

    /**
     * Cancels a fruitore's subscription to an open proposal.
     * Only allowed if the proposal is APERTA and the deadline has not passed.
     * After cancelling, the fruitore can re-subscribe.
     */
    public void disdici(Fruitore fruitore, Proposta proposta)
    {
        // 1. Must be open
        if (proposta.getStato() != StatoProposta.APERTA)
            throw new IllegalStateException("Non puoi disdire: la proposta non è aperta.");

        // 2. Deadline must not have passed
        LocalDate oggi = LocalDate.now();
        if (proposta.getTermineIscrizione() != null &&
                oggi.isAfter(proposta.getTermineIscrizione()))
            throw new IllegalStateException("Non puoi disdire: il termine di iscrizione è scaduto.");

        // 3. Must actually be subscribed
        if (!proposta.isIscrittoFruitore(fruitore.getUsername()))
            throw new IllegalStateException("Non sei iscritto a questa proposta.");

        // All checks passed — remove subscription
        proposta.removeIscrizione(fruitore.getUsername());
        db.save(data);
    }

    /**
     * Withdraws a proposal (APERTA or CONFERMATA) before the event date.
     * Only allowed up to 23:59 of the day before "Data".
     * Notifies all subscribed fruitori and freezes the subscribers list.
     */
    public void ritira(Proposta proposta)
    {
        // 1. Must be APERTA or CONFERMATA
        StatoProposta stato = proposta.getStato();
        if (stato != StatoProposta.APERTA && stato != StatoProposta.CONFERMATA)
            throw new IllegalStateException(
                    "Non puoi ritirare: la proposta deve essere aperta o confermata.");

        // 2. Must be before the event date (strictly before "Data")
        LocalDate oggi = LocalDate.now();
        if (proposta.getDataEvento() == null || !oggi.isBefore(proposta.getDataEvento()))
            throw new IllegalStateException(
                    "Non puoi ritirare: il ritiro è consentito solo fino al giorno prima dell'evento.");

        // 3. Transition to RITIRATA
        proposta.setStato(StatoProposta.RITIRATA, oggi);

        // 4. Notify all subscribers
        String titolo    = proposta.getValoriCampi().getOrDefault("Titolo", "senza titolo");
        String messaggio = "⚠ L'iniziativa \"" + titolo + "\" è stata RITIRATA dal configuratore.";
        notificaTutti(proposta, messaggio);

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

        String titolo = p.getValoriCampi().getOrDefault("Titolo", "senza titolo");
        String data   = p.getValoriCampi().getOrDefault("Data", "");
        String ora    = p.getValoriCampi().getOrDefault("Ora", "");
        String luogo  = p.getValoriCampi().getOrDefault("Luogo", "");
        String quota  = p.getValoriCampi().getOrDefault("Quota individuale", "");

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

        String titolo    = p.getValoriCampi().getOrDefault("Titolo", "senza titolo");
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
            fruitoreService.inviaNotifica(i.getFruitore().getUsername(), messaggio);
    }

    // ---------------------------------------------------------------
    // UTILITY
    // ---------------------------------------------------------------

    /**
     * Reads "Numero di partecipanti" from the proposal's field values.
     * Returns Integer.MAX_VALUE as fallback if the field is missing or invalid.
     */
    private int getMaxPartecipanti(Proposta p)
    {
        String val = p.getValoriCampi().get(CAMPO_NUM_PARTECIPANTI);

        if (val == null || val.isBlank())
            return Integer.MAX_VALUE;

        try
        {
            return Integer.parseInt(val.trim());
        }
        catch (NumberFormatException e)
        {
            return Integer.MAX_VALUE;
        }
    }
}