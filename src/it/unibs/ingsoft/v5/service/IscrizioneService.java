package it.unibs.ingsoft.v5.service;

import it.unibs.ingsoft.v5.model.*;
import it.unibs.ingsoft.v5.persistence.INotificaRepository;
import it.unibs.ingsoft.v5.persistence.IPropostaRepository;
import it.unibs.ingsoft.v5.persistence.NotificaData;
import it.unibs.ingsoft.v5.persistence.PropostaData;

import java.time.LocalDate;
import java.util.Objects;

public final class IscrizioneService
{
    private static final String CAMPO_NUM_PARTECIPANTI = PropostaService.CAMPO_NUM_PARTECIPANTI;

    private final IPropostaRepository propostaRepo;
    private final PropostaData        proposteData;
    private final INotificaRepository notificaRepo;
    private final NotificaData        notificaData;
    private final NotificaListener    notificaListener;

    /**
     * @pre propostaRepo     != null
     * @pre proposteData     != null
     * @pre notificaRepo     != null
     * @pre notificaData     != null
     * @pre notificaListener != null
     */
    public IscrizioneService(IPropostaRepository propostaRepo, PropostaData proposteData,
                             INotificaRepository notificaRepo, NotificaData notificaData,
                             NotificaListener notificaListener)
    {
        this.propostaRepo     = Objects.requireNonNull(propostaRepo);
        this.proposteData     = Objects.requireNonNull(proposteData);
        this.notificaRepo     = Objects.requireNonNull(notificaRepo);
        this.notificaData     = Objects.requireNonNull(notificaData);
        this.notificaListener = Objects.requireNonNull(notificaListener);
    }

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
            notificaRepo.save(notificaData);
        }
    }

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

    public void disdici(Fruitore fruitore, Proposta proposta)
    {
        if (proposta.getStato() != StatoProposta.APERTA)
            throw new IllegalStateException("Non puoi disdire: la proposta non è aperta.");

        LocalDate oggi = LocalDate.now();
        if (proposta.getTermineIscrizione() != null &&
                oggi.isAfter(proposta.getTermineIscrizione()))
            throw new IllegalStateException("Non puoi disdire: il termine di iscrizione è scaduto.");

        if (!proposta.isIscrittoFruitore(fruitore.getUsername()))
            throw new IllegalStateException("Non sei iscritto a questa proposta.");

        proposta.removeIscrizione(fruitore.getUsername());
        propostaRepo.save(proposteData);
    }

    public void ritira(Proposta proposta)
    {
        StatoProposta stato = proposta.getStato();
        if (stato != StatoProposta.APERTA && stato != StatoProposta.CONFERMATA)
            throw new IllegalStateException(
                    "Non puoi ritirare: la proposta deve essere aperta o confermata.");

        LocalDate oggi = LocalDate.now();
        if (proposta.getDataEvento() == null || !oggi.isBefore(proposta.getDataEvento()))
            throw new IllegalStateException(
                    "Non puoi ritirare: il ritiro è consentito solo fino al giorno prima dell'evento.");

        proposta.setStato(StatoProposta.RITIRATA, oggi);

        String titolo    = proposta.getValoriCampi().getOrDefault(PropostaService.CAMPO_TITOLO, "senza titolo");
        String messaggio = "⚠ L'iniziativa \"" + titolo + "\" è stata RITIRATA dal configuratore.";
        notificaTutti(proposta, messaggio);

        propostaRepo.save(proposteData);
        notificaRepo.save(notificaData);
    }

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
            notificaListener.notifica(i.getFruitore().getUsername(), messaggio);
    }

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
