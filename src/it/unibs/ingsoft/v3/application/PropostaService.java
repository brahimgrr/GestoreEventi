package it.unibs.ingsoft.v3.application;

import it.unibs.ingsoft.v3.domain.*;
import it.unibs.ingsoft.v3.domain.Catalogo;
import it.unibs.ingsoft.v3.persistence.api.IPropostaRepository;
import it.unibs.ingsoft.v3.domain.Bacheca;
import it.unibs.ingsoft.v3.domain.AppConstants;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class PropostaService
{
    public static final String CAMPO_TERMINE_ISCRIZIONE = "Termine ultimo di iscrizione";
    public static final String CAMPO_DATA               = "Data";
    public static final String CAMPO_DATA_CONCLUSIVA    = "Data conclusiva";
    public static final String CAMPO_TITOLO             = "Titolo";
    public static final String CAMPO_ORA                = "Ora";
    public static final String CAMPO_LUOGO              = "Luogo";
    public static final String CAMPO_QUOTA              = "Quota individuale";
    public static final String CAMPO_NUM_PARTECIPANTI   = "Numero di partecipanti";

    private final Catalogo catalogo;
    private final IPropostaRepository propostaRepo;
    private final Bacheca bacheca;

    /**
     * @pre catalogo     != null
     * @pre propostaRepo != null
     * @pre proposteData != null
     */
    public PropostaService(Catalogo catalogo, IPropostaRepository propostaRepo, Bacheca bacheca)
    {
        this.catalogo     = Objects.requireNonNull(catalogo);
        this.propostaRepo = Objects.requireNonNull(propostaRepo);
        this.bacheca = Objects.requireNonNull(bacheca);
    }

    public Proposta creaProposta(String nomeCategoria)
    {
        Categoria cat = catalogo.findCategoria(nomeCategoria)
                .orElseThrow(() -> new IllegalArgumentException("Categoria non trovata: " + nomeCategoria));

        Proposta p = new Proposta(cat);
        bacheca.addProposta(p);
        propostaRepo.save(bacheca);
        return p;
    }

    public List<String> validaEPromuovi(Proposta p)
    {
        List<String> errori = new ArrayList<>();
        Categoria cat = p.getCategoria();
        Map<String, String> valori = p.getValoriCampi();

        controllaCampiObbligatori(catalogo.getCampiBase(),    valori, errori);
        controllaCampiObbligatori(catalogo.getCampiComuni(),  valori, errori);
        controllaCampiObbligatori(cat.getCampiSpecifici(), valori, errori);

        LocalDate oggi = LocalDate.now();
        LocalDate termineIscr = parseData(valori.get(CAMPO_TERMINE_ISCRIZIONE));
        LocalDate dataEvento  = parseData(valori.get(CAMPO_DATA));
        LocalDate dataConclus = parseData(valori.get(CAMPO_DATA_CONCLUSIVA));

        if (termineIscr != null)
        {
            if (!termineIscr.isAfter(oggi))
                errori.add("\"" + CAMPO_TERMINE_ISCRIZIONE + "\" deve essere successivo alla data odierna (" + oggi + ").");

            if (dataEvento != null)
            {
                if (!dataEvento.isAfter(termineIscr.plusDays(1)))
                    errori.add("\"" + CAMPO_DATA + "\" deve essere successivo di almeno 2 giorni rispetto a \""
                            + CAMPO_TERMINE_ISCRIZIONE + "\".");
            }
        }

        if (dataEvento != null && dataConclus != null)
        {
            if (dataConclus.isBefore(dataEvento))
                errori.add("\"" + CAMPO_DATA_CONCLUSIVA + "\" non può essere precedente a \"" + CAMPO_DATA + "\".");
        }

        if (errori.isEmpty())
        {
            p.setTermineIscrizione(termineIscr);
            p.setDataEvento(dataEvento);
            p.setDataConclus(dataConclus);
            p.setStato(StatoProposta.VALIDA, LocalDate.now());
        }

        return errori;
    }

    private void controllaCampiObbligatori(List<Campo> campi, Map<String, String> valori, List<String> errori)
    {
        for (Campo c : campi)
        {
            if (c.isObbligatorio())
            {
                String v = valori.get(c.getNome());
                if (v == null || v.isBlank())
                    errori.add("Campo obbligatorio mancante: \"" + c.getNome() + "\".");
            }
        }
    }

    private LocalDate parseData(String s)
    {
        if (s == null || s.isBlank()) return null;
        try { return LocalDate.parse(s, AppConstants.DATE_FMT); }
        catch (Exception e) { return null; }
    }

    public void pubblicaProposta(Proposta p)
    {
        if (p.getStato() != StatoProposta.VALIDA)
            throw new IllegalStateException("La proposta deve essere in stato VALIDA per essere pubblicata.");

        LocalDate oggi = LocalDate.now();
        if (p.getTermineIscrizione() != null && !p.getTermineIscrizione().isAfter(oggi))
            throw new IllegalStateException(
                    "Non è più possibile pubblicare: il termine di iscrizione (" +
                    p.getTermineIscrizione() + ") è già scaduto. Rivalidare la proposta.");

        String titolo  = p.getValoriCampi().getOrDefault(CAMPO_TITOLO, "").trim();
        String dataStr = p.getValoriCampi().getOrDefault(CAMPO_DATA,   "").trim();
        String ora     = p.getValoriCampi().getOrDefault(CAMPO_ORA,    "").trim();
        String luogo   = p.getValoriCampi().getOrDefault(CAMPO_LUOGO,  "").trim();

        boolean duplicato = bacheca.getProposte().stream()
                .filter(existing -> existing != p)   // exclude the proposal being published
                .anyMatch(existing ->
                        existing.getValoriCampi().getOrDefault(CAMPO_TITOLO, "").trim().equalsIgnoreCase(titolo) &&
                        existing.getValoriCampi().getOrDefault(CAMPO_DATA,   "").trim().equals(dataStr)          &&
                        existing.getValoriCampi().getOrDefault(CAMPO_ORA,    "").trim().equalsIgnoreCase(ora)    &&
                        existing.getValoriCampi().getOrDefault(CAMPO_LUOGO,  "").trim().equalsIgnoreCase(luogo)
                );

        if (duplicato)
            throw new IllegalStateException("Esiste già una proposta con lo stesso Titolo, Data, Ora e Luogo.");

        p.setStato(StatoProposta.APERTA, LocalDate.now());
        p.setDataPubblicazione(LocalDate.now());

        // proposal is already in proposteData (added when BOZZA was created)
        propostaRepo.save(bacheca);
    }

    public List<Proposta> getBacheca()
    {
        return bacheca.getProposte().stream()
                .filter(p -> p.getStato() == StatoProposta.APERTA)
                .collect(Collectors.toList());
    }

    public Map<String, List<Proposta>> getBachecaPerCategoria()
    {
        Map<String, List<Proposta>> mappa = new LinkedHashMap<>();
        for (Proposta p : getBacheca())
        {
            String nomeCategoria = p.getCategoria().getNome();
            mappa.computeIfAbsent(nomeCategoria, k -> new ArrayList<>()).add(p);
        }
        return mappa;
    }

    public List<Proposta> getProposteAperte()
    {
        return bacheca.getProposte().stream()
                .filter(p -> p.getStato() == StatoProposta.APERTA)
                .collect(Collectors.toList());
    }

    public List<Proposta> getTutteLeProposte()
    {
        return bacheca.getProposte();
    }

    public List<Proposta> getPropostePerStato(StatoProposta stato)
    {
        return bacheca.getProposte().stream()
                .filter(p -> p.getStato() == stato)
                .collect(Collectors.toList());
    }

    public List<Campo> getTuttiCampi(Proposta p)
    {
        List<Campo> tutti = new ArrayList<>();
        tutti.addAll(catalogo.getCampiBase());
        tutti.addAll(catalogo.getCampiComuni());
        tutti.addAll(p.getCategoria().getCampiSpecifici());
        return tutti;
    }

    public List<Campo> getCampiConErrore(Proposta p, List<String> errori)
    {
        return getTuttiCampi(p).stream()
                .filter(c -> errori.stream().anyMatch(e -> e.contains(c.getNome())))
                .collect(Collectors.toList());
    }

    public List<Campo> getCampiBase()
    {
        return Collections.unmodifiableList(catalogo.getCampiBase());
    }

    public List<Campo> getCampiComuni()
    {
        return Collections.unmodifiableList(catalogo.getCampiComuni());
    }
}
