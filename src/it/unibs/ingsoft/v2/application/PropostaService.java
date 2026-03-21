package it.unibs.ingsoft.v2.application;

import it.unibs.ingsoft.v2.domain.*;
import it.unibs.ingsoft.v2.persistence.api.IPropostaRepository;
import it.unibs.ingsoft.v2.persistence.dto.CatalogoData;
import it.unibs.ingsoft.v2.persistence.dto.PropostaData;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages the proposal lifecycle: creation, validation, publication, and bacheca queries.
 *
 * <h3>State transitions</h3>
 * <pre>  creaProposta()   → BOZZA
 *   validaProposta() → VALIDA (or stays BOZZA on failure)
 *   pubblicaProposta() → APERTA (persisted)</pre>
 *
 * <h3>Date helpers</h3>
 * The three static {@code is*Valido} methods are the single source of truth for
 * date boundary checks.  They are called both from {@link #validaProposta} and
 * from the form validators in the controller, eliminating duplication.
 */
public final class PropostaService
{
    // ---- canonical field-name constants (single source of truth) ----
    public static final String CAMPO_TITOLO              = "Titolo";
    public static final String CAMPO_TERMINE_ISCRIZIONE  = "Termine ultimo di iscrizione";
    public static final String CAMPO_DATA                = "Data";
    public static final String CAMPO_DATA_CONCLUSIVA     = "Data conclusiva";
    public static final String CAMPO_ORA                 = "Ora";
    public static final String CAMPO_LUOGO               = "Luogo";
    public static final String CAMPO_QUOTA               = "Quota individuale";
    public static final String CAMPO_NUM_PARTECIPANTI    = "Numero di partecipanti";

    private final CatalogoData       catalogo;
    private final IPropostaRepository propostaRepo;
    private final PropostaData        proposteData;

    public PropostaService(IPropostaRepository propostaRepo, CategoriaService categoriaService)
    {
        this.propostaRepo = Objects.requireNonNull(propostaRepo);
        this.proposteData = propostaRepo.load();
        this.catalogo = categoriaService.getCatalogo();
    }

    // ----------------------------------------------------------------
    // STATIC DATE HELPERS — shared with form validators (DRY)
    // ----------------------------------------------------------------

    /**
     * True when {@code termine} is strictly after today (as required by spec).
     */
    public static boolean isTermineIscrizioneValido(LocalDate termine)
    {
        return termine != null && termine.isAfter(LocalDate.now());
    }

    /**
     * True when {@code dataEvento} is at least 2 days after {@code termine}.
     */
    public static boolean isDataEventoValida(LocalDate dataEvento, LocalDate termine)
    {
        return dataEvento != null && termine != null && dataEvento.isAfter(termine.plusDays(1));
    }

    /**
     * True when {@code conclusiva} is not before {@code data}.
     */
    public static boolean isDataConclusivaValida(LocalDate conclusiva, LocalDate data)
    {
        return conclusiva != null && data != null && !conclusiva.isBefore(data);
    }

    // ----------------------------------------------------------------
    // CREAZIONE
    // ----------------------------------------------------------------

    /**
     * Creates a new draft proposal for the given category.
     *
     * @throws IllegalArgumentException if the category does not exist
     */
    public Proposta creaProposta(String nomeCategoria)
    {
        Categoria cat = catalogo.findCategoria(nomeCategoria)
                .orElseThrow(() -> new IllegalArgumentException("Categoria non trovata: " + nomeCategoria));
        return new Proposta(cat);
    }

    // ----------------------------------------------------------------
    // VALIDAZIONE
    // ----------------------------------------------------------------

    /**
     * Validates the proposal against mandatory-field and date constraints.
     *
     * <p>Idempotent: if the proposal is currently VALIDA, it is reverted to BOZZA
     * before re-validation so the state always reflects the latest outcome.</p>
     *
     * @return empty list on success (proposal set to VALIDA);
     *         list of error messages on failure (proposal reverted to BOZZA)
     */
    public List<String> validaProposta(Proposta p)
    {
        // Revert to BOZZA so the method is idempotent for VALIDA proposals
        if (p.getStato() == StatoProposta.VALIDA)
            p.setStato(StatoProposta.BOZZA);

        List<String> errori = new ArrayList<>();
        Map<String, String> valori = p.getValoriCampi();
        Categoria cat = p.getCategoria();

        // 1. Mandatory fields
        controllaCampiObbligatori(catalogo.getCampiBase(),   valori, errori);
        controllaCampiObbligatori(catalogo.getCampiComuni(), valori, errori);
        controllaCampiObbligatori(cat.getCampiSpecifici(),   valori, errori);

        // 2. Date constraints
        LocalDate oggi        = LocalDate.now();
        LocalDate termineIscr = parseData(valori.get(CAMPO_TERMINE_ISCRIZIONE));
        LocalDate dataEvento  = parseData(valori.get(CAMPO_DATA));
        LocalDate dataConclus = parseData(valori.get(CAMPO_DATA_CONCLUSIVA));

        if (termineIscr != null && !isTermineIscrizioneValido(termineIscr))
            errori.add("\"" + CAMPO_TERMINE_ISCRIZIONE + "\" deve essere successivo alla data odierna (" + oggi + ").");

        if (termineIscr != null && dataEvento != null && !isDataEventoValida(dataEvento, termineIscr))
            errori.add("\"" + CAMPO_DATA + "\" deve essere successivo di almeno 2 giorni rispetto a \""
                    + CAMPO_TERMINE_ISCRIZIONE + "\".");

        if (dataEvento != null && dataConclus != null && !isDataConclusivaValida(dataConclus, dataEvento))
            errori.add("\"" + CAMPO_DATA_CONCLUSIVA + "\" non può essere precedente a \"" + CAMPO_DATA + "\".");

        if (errori.isEmpty())
        {
            p.setTermineIscrizione(termineIscr);
            p.setDataEvento(dataEvento);
            p.setStato(StatoProposta.VALIDA);
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
        try { return LocalDate.parse(s.trim(), AppConstants.DATE_FMT); }
        catch (Exception e) { return null; }
    }

    // ----------------------------------------------------------------
    // PUBBLICAZIONE
    // ----------------------------------------------------------------

    /**
     * Publishes a valid proposal to the bulletin board (VALIDA → APERTA) and persists it.
     *
     * @pre p.getStato() == StatoProposta.VALIDA
     * @throws IllegalStateException if the proposal is not VALIDA, if the subscription
     *                               deadline has already passed, or if a duplicate exists
     */
    public void pubblicaProposta(Proposta p)
    {
        if (p.getStato() != StatoProposta.VALIDA)
            throw new IllegalStateException("La proposta deve essere in stato VALIDA per essere pubblicata.");

        LocalDate oggi = LocalDate.now();
        if (p.getTermineIscrizione() != null && !p.getTermineIscrizione().isAfter(oggi))
            throw new IllegalStateException(
                    "Non è più possibile pubblicare: il termine di iscrizione ("
                    + p.getTermineIscrizione() + ") è già scaduto. Rivalidare la proposta.");

        rilevaDuplicato(p);

        p.setStato(StatoProposta.APERTA);
        p.setDataPubblicazione(oggi);
        proposteData.addProposta(p);
        propostaRepo.save(proposteData);
    }

    private void rilevaDuplicato(Proposta p)
    {
        String titolo  = p.getValoriCampi().getOrDefault(CAMPO_TITOLO, "").trim();
        String dataStr = p.getValoriCampi().getOrDefault(CAMPO_DATA,   "").trim();
        String ora     = p.getValoriCampi().getOrDefault(CAMPO_ORA,    "").trim();
        String luogo   = p.getValoriCampi().getOrDefault(CAMPO_LUOGO,  "").trim();

        boolean duplicato = proposteData.getProposte().stream().anyMatch(existing ->
                existing.getValoriCampi().getOrDefault(CAMPO_TITOLO, "").trim().equalsIgnoreCase(titolo) &&
                existing.getValoriCampi().getOrDefault(CAMPO_DATA,   "").trim().equals(dataStr)          &&
                existing.getValoriCampi().getOrDefault(CAMPO_ORA,    "").trim().equalsIgnoreCase(ora)    &&
                existing.getValoriCampi().getOrDefault(CAMPO_LUOGO,  "").trim().equalsIgnoreCase(luogo)
        );

        if (duplicato)
            throw new IllegalStateException("Esiste già una proposta con lo stesso Titolo, Data, Ora e Luogo.");
    }

    // ----------------------------------------------------------------
    // BACHECA
    // ----------------------------------------------------------------

    /** Returns all open (APERTA) proposals as a flat list. */
    public List<Proposta> getBacheca()
    {
        return proposteData.getProposte().stream()
                .filter(p -> p.getStato() == StatoProposta.APERTA)
                .collect(Collectors.toList());
    }

    /** Returns all open (APERTA) proposals, grouped by category name. */
    public Map<String, List<Proposta>> getBachecaPerCategoria()
    {
        Map<String, List<Proposta>> mappa = new LinkedHashMap<>();
        for (Proposta p : proposteData.getProposte())
        {
            if (p.getStato() == StatoProposta.APERTA)
                mappa.computeIfAbsent(p.getCategoria().getNome(), k -> new ArrayList<>()).add(p);
        }
        return mappa;
    }

    /** Returns all fields (base + common + category-specific) for the given proposal. */
    public List<Campo> getTuttiCampi(Proposta p)
    {
        List<Campo> tutti = new ArrayList<>();
        tutti.addAll(catalogo.getCampiBase());
        tutti.addAll(catalogo.getCampiComuni());
        tutti.addAll(p.getCategoria().getCampiSpecifici());
        return tutti;
    }

    /**
     * Returns the subset of fields whose names appear in the error list.
     *
     * <p>Uses quoted matching ({@code "\"nome\""}) so that a field named
     * {@code "Data"} is not spuriously matched by an error message that
     * mentions {@code "Data conclusiva"}.</p>
     */
    public List<Campo> getCampiConErrore(Proposta p, List<String> errori)
    {
        return getTuttiCampi(p).stream()
                .filter(c -> {
                    String quoted = "\"" + c.getNome() + "\"";
                    return errori.stream().anyMatch(e -> e.contains(quoted));
                })
                .collect(Collectors.toList());
    }

    public List<Campo> getCampiBase()   { return Collections.unmodifiableList(catalogo.getCampiBase()); }
    public List<Campo> getCampiComuni() { return Collections.unmodifiableList(catalogo.getCampiComuni()); }
}
