package it.unibs.ingsoft.v3.application;

import it.unibs.ingsoft.v3.domain.*;
import it.unibs.ingsoft.v3.domain.Bacheca;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public final class IscrizioneService
{
    private static final String CAMPO_NUM_PARTECIPANTI = PropostaService.CAMPO_NUM_PARTECIPANTI;

    private final IPropostaRepository      propostaRepo;
    private final Bacheca proposteData;
    private final List<NotificaListener>   listeners;

    /**
     * @pre propostaRepo != null
     * @pre proposteData != null
     * @pre listeners    != null (may be empty)
     */
    public IscrizioneService(IPropostaRepository propostaRepo, Bacheca proposteData,
                             List<NotificaListener> listeners)
    {
        this.propostaRepo = Objects.requireNonNull(propostaRepo);
        this.proposteData = Objects.requireNonNull(proposteData);
        this.listeners    = List.copyOf(Objects.requireNonNull(listeners));
    }

    // ---------------------------------------------------------------
    // STARTUP CHECK
    // ---------------------------------------------------------------

    /**
     * Called once on every startup.
     * Loops through all open proposals and transitions any whose deadline has passed.
     * Notifies all enrolled fruitori via the registered listeners.
     */
    public void controllaScadenzeAlAvvio()
    {
        LocalDate oggi = LocalDate.now();
        boolean modificato = false;

        for (Proposta p : proposteData.getProposte())
        {
            if (p.getStato() != StatoProposta.APERTA)
                continue;

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

        for (Proposta p : proposteData.getProposte())
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
        {
            propostaRepo.save(proposteData);
            listeners.forEach(NotificaListener::commit);
        }
    }

    // ---------------------------------------------------------------
    // ISCRIZIONE
    // ---------------------------------------------------------------

    /**
     * Signs up a fruitore to an open proposal.
     *
     * @pre fruitore != null
     * @pre proposta != null &amp;&amp; proposta.getStato() == StatoProposta.APERTA
     * @throws IllegalStateException if any precondition is violated
     */
    public void iscrivi(Fruitore fruitore, Proposta proposta)
    {
        if (proposta.getStato() != StatoProposta.APERTA)
            throw new IllegalStateException("La proposta non è aperta alle iscrizioni.");

        LocalDate oggi = LocalDate.now();
        if (proposta.getTermineIscrizione() != null &&
                oggi.isAfter(proposta.getTermineIscrizione()))
            throw new IllegalStateException("Il termine di iscrizione è scaduto.");

        if (proposta.isIscrittoFruitore(fruitore.getUsername()))
            throw new IllegalStateException("Sei già iscritto a questa proposta.");

        int max = getMaxPartecipanti(proposta);
        if (proposta.getNumeroIscritti() >= max)
            throw new IllegalStateException(
                    "La proposta ha già raggiunto il numero massimo di partecipanti (" + max + ").");

        Iscrizione i = new Iscrizione(fruitore, oggi);
        proposta.addIscrizione(i);
        propostaRepo.save(proposteData);
    }

    // ---------------------------------------------------------------
    // STATE TRANSITIONS
    // ---------------------------------------------------------------

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

    private void annulla(Proposta p, LocalDate oggi)
    {
        p.setStato(StatoProposta.ANNULLATA, oggi);

        String titolo    = p.getValoriCampi().getOrDefault(PropostaService.CAMPO_TITOLO, "senza titolo");
        String messaggio = "✘ L'iniziativa \"" + titolo + "\" è stata ANNULLATA " +
                "per insufficienza di iscrizioni.";

        notificaTutti(p, messaggio);
    }

    private void notificaTutti(Proposta p, String messaggio)
    {
        for (Iscrizione i : p.getIscrizioni())
            listeners.forEach(l -> l.notifica(i.getFruitore().getUsername(), messaggio));
    }

    // ---------------------------------------------------------------
    // UTILITY
    // ---------------------------------------------------------------

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
