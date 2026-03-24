package it.unibs.ingsoft.v4.application;

import it.unibs.ingsoft.v4.domain.*;
import it.unibs.ingsoft.v4.persistence.api.IBachecaRepository;

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
 * date boundary checks used by {@link #validaProposta}.
 */
public final class PropostaService
{
    // ---- field-name constants — aliases of AppConstants (single source of truth in domain layer) ----
    public static final String CAMPO_TITOLO              = AppConstants.CAMPO_TITOLO;
    public static final String CAMPO_TERMINE_ISCRIZIONE  = AppConstants.CAMPO_TERMINE_ISCRIZIONE;
    public static final String CAMPO_DATA                = AppConstants.CAMPO_DATA;
    public static final String CAMPO_DATA_CONCLUSIVA     = AppConstants.CAMPO_DATA_CONCLUSIVA;
    public static final String CAMPO_ORA                 = AppConstants.CAMPO_ORA;
    public static final String CAMPO_LUOGO               = AppConstants.CAMPO_LUOGO;
    public static final String CAMPO_QUOTA               = AppConstants.CAMPO_QUOTA;
    public static final String CAMPO_NUM_PARTECIPANTI    = AppConstants.CAMPO_NUM_PARTECIPANTI;

    private final IBachecaRepository bachecaRepo;

    /** In-memory list of valid but unpublished proposals (session-scoped, discarded on logout). */
    private final List<Proposta> proposteValide = new ArrayList<>();

    public PropostaService(IBachecaRepository bachecaRepo)
    {
        this.bachecaRepo = Objects.requireNonNull(bachecaRepo);
    }
    /**
     * Saves a valid proposal in memory for later publication.
     *
     * @pre p.getStato() == StatoProposta.VALIDA
     */
    public void salvaProposta(Proposta p)
    {
        if (p.getStato() != StatoProposta.VALIDA)
            throw new IllegalStateException("Solo una proposta VALIDA può essere salvata.");
        proposteValide.add(p);
    }

    /** Returns the list of valid proposals saved in memory (not yet published). */
    public List<Proposta> getProposteValide()
    {
        return Collections.unmodifiableList(proposteValide);
    }

    /** Removes a proposal from the in-memory valid list (e.g. after publication). */
    public void rimuoviPropostaValida(Proposta p)
    {
        proposteValide.remove(p);
    }

    /** Discards all unpublished valid proposals (called on logout). */
    public void clearProposteValide()
    {
        proposteValide.clear();
    }

    private Bacheca bacheca() {
        return bachecaRepo.get();
    }

    // ----------------------------------------------------------------
    // STATIC DATE HELPERS — reused by application-level validation
    // ----------------------------------------------------------------

    /**
     * True when {@code termine} is strictly after today (as required by spec).
     */
    public static boolean isTermineIscrizioneValido(LocalDate termine)
    {
        return termine != null && termine.isAfter(LocalDate.now(AppConstants.clock));
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
    public Proposta creaProposta(Categoria categoria, List<Campo> campiBase, List<Campo> campiComuni)
    {
        return new Proposta(categoria, campiBase, campiComuni);
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
        // Revert to BOZZA silently (no history entry) so the method is idempotent
        p.revertToBozzaSilent();

        List<String> errori = new ArrayList<>();
        Map<String, String> valori = p.getValoriCampi();
        Categoria cat = p.getCategoria();

        // 1. Mandatory fields
        controllaCampiObbligatori(p.getCampi(), valori, errori);

        // 2. Numero di partecipanti must be a positive integer
        String numStr = valori.get(CAMPO_NUM_PARTECIPANTI);
        if (numStr != null && !numStr.isBlank()) {
            try {
                int n = Integer.parseInt(numStr.trim());
                if (n <= 0)
                    errori.add("\"" + CAMPO_NUM_PARTECIPANTI + "\" deve essere un intero positivo.");
            } catch (NumberFormatException e) {
                errori.add("\"" + CAMPO_NUM_PARTECIPANTI + "\" deve essere un intero valido.");
            }
        }

        // 3. Date constraints
        LocalDate oggi        = LocalDate.now(AppConstants.clock);
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

    /**
     * Validates a single field in the context of the current proposal values.
     * This is used during interactive entry to provide immediate business-rule feedback.
     */
    public List<String> validaCampo(Proposta proposta, Map<String, String> valoriCorrenti, String nomeCampo, String valore)
    {
        Map<String, String> valori = new LinkedHashMap<>(proposta.getValoriCampi());
        valori.putAll(valoriCorrenti);
        valori.put(nomeCampo, valore);

        List<String> errori = new ArrayList<>();
        LocalDate termineIscr = parseData(valori.get(CAMPO_TERMINE_ISCRIZIONE));
        LocalDate dataEvento  = parseData(valori.get(CAMPO_DATA));
        LocalDate dataConclus = parseData(valori.get(CAMPO_DATA_CONCLUSIVA));

        switch (nomeCampo)
        {
            case CAMPO_TERMINE_ISCRIZIONE:
                if (termineIscr != null && !isTermineIscrizioneValido(termineIscr))
                    errori.add("\"" + CAMPO_TERMINE_ISCRIZIONE + "\" deve essere successivo alla data odierna.");
                if (termineIscr != null && dataEvento != null && !isDataEventoValida(dataEvento, termineIscr))
                    errori.add("\"" + CAMPO_DATA + "\" deve essere successivo di almeno 2 giorni rispetto a \"" +
                            CAMPO_TERMINE_ISCRIZIONE + "\".");
                break;

            case CAMPO_DATA:
                if (termineIscr != null && dataEvento != null && !isDataEventoValida(dataEvento, termineIscr))
                    errori.add("\"" + CAMPO_DATA + "\" deve essere successivo di almeno 2 giorni rispetto a \"" +
                            CAMPO_TERMINE_ISCRIZIONE + "\".");
                if (dataEvento != null && dataConclus != null && !isDataConclusivaValida(dataConclus, dataEvento))
                    errori.add("\"" + CAMPO_DATA_CONCLUSIVA + "\" non può essere precedente a \"" + CAMPO_DATA + "\".");
                break;

            case CAMPO_DATA_CONCLUSIVA:
                if (dataEvento != null && dataConclus != null && !isDataConclusivaValida(dataConclus, dataEvento))
                    errori.add("\"" + CAMPO_DATA_CONCLUSIVA + "\" non può essere precedente a \"" + CAMPO_DATA + "\".");
                break;

            default:
                break;
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

        LocalDate oggi = LocalDate.now(AppConstants.clock);
        if (p.getTermineIscrizione() != null && !p.getTermineIscrizione().isAfter(oggi))
            throw new IllegalStateException(
                    "Non è più possibile pubblicare: il termine di iscrizione ("
                    + p.getTermineIscrizione() + ") è già scaduto. Rivalidare la proposta.");

        rilevaDuplicato(p);

        p.setStato(StatoProposta.APERTA);
        p.setDataPubblicazione(oggi);
        bacheca().addProposta(p);
        bachecaRepo.save();
    }

    private void rilevaDuplicato(Proposta p)
    {
        String titolo  = p.getValoriCampi().getOrDefault(CAMPO_TITOLO, "").trim();
        String dataStr = p.getValoriCampi().getOrDefault(CAMPO_DATA,   "").trim();
        String ora     = p.getValoriCampi().getOrDefault(CAMPO_ORA,    "").trim();
        String luogo   = p.getValoriCampi().getOrDefault(CAMPO_LUOGO,  "").trim();

        boolean duplicato = bacheca().getProposte().stream().anyMatch(existing ->
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

    /** Returns all proposals (any state) as a flat list. */
    public List<Proposta> getTutteLeProposte()
    {
        return Collections.unmodifiableList(bacheca().getProposte());
    }

    /** Returns all open (APERTA) proposals as a flat list. */
    public List<Proposta> getBacheca()
    {
        return bacheca().getProposte().stream()
                .filter(p -> p.getStato() == StatoProposta.APERTA)
                .collect(Collectors.toList());
    }

    /** Returns all open (APERTA) proposals, grouped by category name. */
    public Map<String, List<Proposta>> getBachecaPerCategoria()
    {
        Map<String, List<Proposta>> mappa = new LinkedHashMap<>();
        for (Proposta p : bacheca().getProposte())
        {
            if (p.getStato() == StatoProposta.APERTA)
                mappa.computeIfAbsent(p.getCategoria().getNome(), k -> new ArrayList<>()).add(p);
        }
        return mappa;
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
        return p.getCampi().stream()
                .filter(c -> {
                    String quoted = "\"" + c.getNome() + "\"";
                    return errori.stream().anyMatch(e -> e.contains(quoted));
                })
                .collect(Collectors.toList());
    }

}
