package it.unibs.ingsoft.v5.service;

import it.unibs.ingsoft.v5.model.Campo;
import it.unibs.ingsoft.v5.model.Categoria;
import it.unibs.ingsoft.v5.model.Proposta;
import it.unibs.ingsoft.v5.model.StatoProposta;
import it.unibs.ingsoft.v5.persistence.AppData;
import it.unibs.ingsoft.v5.persistence.DatabaseService;
import it.unibs.ingsoft.v5.view.ConsoleUI;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class PropostaService
{
    // I campi base con nomi riservati per la logica di date
    public static final String CAMPO_TERMINE_ISCRIZIONE = "Termine ultimo di iscrizione";
    public static final String CAMPO_DATA = "Data";
    public static final String CAMPO_DATA_CONCLUSIVA = "Data conclusiva";

    private final DatabaseService db;
    private final AppData data;

    public PropostaService(DatabaseService db, AppData data)
    {
        this.db   = Objects.requireNonNull(db);
        this.data = Objects.requireNonNull(data);
    }

    // ------------------------------------------------
    // CREAZIONE PROPOSTA
    // ------------------------------------------------

    /**
     * Crea una nuova proposta in stato BOZZA per la categoria indicata.
     *
     * @param nomeCategoria nome della categoria
     * @return la nuova proposta (non ancora salvata)
     * @throws IllegalArgumentException se la categoria non esiste
     */
    public Proposta creaProposta(String nomeCategoria)
    {
        Categoria cat = data.findCategoria(nomeCategoria);

        if (cat == null)
            throw new IllegalArgumentException("Categoria non trovata: " + nomeCategoria);

        return new Proposta(cat);
    }

    // ------------------------------------------------
    // VALIDAZIONE PROPOSTA
    // ------------------------------------------------

    /**
     * Valida la proposta controllando:
     * 1. Tutti i campi obbligatori (base, comuni, specifici) devono avere un valore.
     * 2. "Termine ultimo di iscrizione" deve essere successivo alla data odierna.
     * 3. "Data" deve essere successiva di almeno 2 giorni a "Termine ultimo di iscrizione".
     * 4. "Data conclusiva" non può essere precedente a "Data".
     *
     * Se tutti i controlli passano, imposta lo stato a VALIDA e restituisce true.
     * In caso contrario restituisce false e lascia la proposta in stato BOZZA.
     *
     * @param p la proposta da validare
     * @return lista di errori (vuota se valida)
     */
    public List<String> validaProposta(Proposta p)
    {
        List<String> errori = new ArrayList<>();
        Categoria cat = p.getCategoria();
        Map<String, String> valori = p.getValoriCampi();

        // --- 1. Campi obbligatori ---
        controllaCampiObbligatori(data.getCampiBase(),    valori, errori);
        controllaCampiObbligatori(data.getCampiComuni(),  valori, errori);
        controllaCampiObbligatori(cat.getCampiSpecifici(), valori, errori);

        // --- 2. Vincoli sulle date ---
        LocalDate oggi = LocalDate.now();
        LocalDate termineIscr = parseData(valori.get(CAMPO_TERMINE_ISCRIZIONE));
        LocalDate dataEvento = parseData(valori.get(CAMPO_DATA));
        LocalDate dataConclus = parseData(valori.get(CAMPO_DATA_CONCLUSIVA));

        if (termineIscr != null)
        {
            if (!termineIscr.isAfter(oggi))
                errori.add("\"" + CAMPO_TERMINE_ISCRIZIONE + "\" deve essere successivo alla data odierna (" + oggi + ").");

            if (dataEvento != null)
            {
                // data evento deve essere >= termine + 2 giorni
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
            // Aggiorna i campi data nella proposta per comodità futura
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

    /**
     * Tenta di interpretare una stringa come LocalDate nel formato dd/MM/yyyy.
     * Restituisce null se la stringa è nulla, vuota o non parsabile.
     */
    private LocalDate parseData(String s)
    {
        if (s == null || s.isBlank())

            return null;
        try
        {
            return LocalDate.parse(s, ConsoleUI.DATE_FMT);

        }	catch (Exception e) {
            return null;
        }
    }

    // ------------------------------------------------
    // PUBBLICAZIONE PROPOSTA
    // ------------------------------------------------

    /**
     * Pubblica una proposta valida in bacheca (stato → APERTA).
     * Salva in forma persistente.
     *
     * @param p la proposta da pubblicare
     * @throws IllegalStateException se la proposta non è in stato VALIDA
     */
    public void pubblicaProposta(Proposta p)
    {
        if (p.getStato() != StatoProposta.VALIDA)
            throw new IllegalStateException("La proposta deve essere in stato VALIDA per essere pubblicata.");

        // Duplicate check: same Titolo + Data + Ora + Luogo = same event
        String titolo = p.getValoriCampi().getOrDefault("Titolo", "").trim().toLowerCase();
        String dataStr   = p.getValoriCampi().getOrDefault("Data", "").trim();
        String ora    = p.getValoriCampi().getOrDefault("Ora", "").trim().toLowerCase();
        String luogo  = p.getValoriCampi().getOrDefault("Luogo", "").trim().toLowerCase();

        boolean duplicato = data.getProposte().stream()
                .anyMatch(existing ->
                        existing.getValoriCampi().getOrDefault("Titolo", "").trim().equalsIgnoreCase(titolo) &&
                                existing.getValoriCampi().getOrDefault("Data",   "").trim().equals(dataStr)             &&
                                existing.getValoriCampi().getOrDefault("Ora",    "").trim().equalsIgnoreCase(ora)    &&
                                existing.getValoriCampi().getOrDefault("Luogo",  "").trim().equalsIgnoreCase(luogo)
                );

        if (duplicato)
            throw new IllegalStateException(
                    "Esiste già una proposta con lo stesso Titolo, Data, Ora e Luogo.");

        p.setStato(StatoProposta.APERTA, LocalDate.now());
        p.setDataPubblicazione(LocalDate.now());

        data.getProposte().add(p);
        db.save(data);
    }

    // ------------------------------------------------
    // BACHECA
    // ------------------------------------------------

    /**
     * Restituisce tutte le proposte aperte.
     */
    public List<Proposta> getBacheca()
    {
        return data.getProposte().stream()
                .filter(p -> p.getStato() == StatoProposta.APERTA)
                .collect(Collectors.toList());
    }

    /**
     * Returns all proposals regardless of state.
     */
    public List<Proposta> getProposte()
    {
        return Collections.unmodifiableList(data.getProposte());
    }

    /**
     * Restituisce le proposte aperte raggruppate per categoria.
     * Le categorie senza proposte aperte non compaiono nella mappa.
     *
     * @return mappa categoria → lista proposte aperte
     */
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

    /**
     * Restituisce tutti i campi (base + comuni + specifici) della categoria
     * della proposta, nell'ordine in cui devono essere presentati all'utente.
     */
    public List<Campo> getTuttiCampi(Proposta p)
    {
        List<Campo> tutti = new ArrayList<>();
        tutti.addAll(data.getCampiBase());
        tutti.addAll(data.getCampiComuni());
        tutti.addAll(p.getCategoria().getCampiSpecifici());

        return tutti;
    }

    // Accessor per i tipi di campi separati (usati dalla UI)
    public List<Campo> getCampiBase()
    {
        return Collections.unmodifiableList(data.getCampiBase());
    }

    public List<Campo> getCampiComuni()
    {
        return Collections.unmodifiableList(data.getCampiComuni());
    }
}