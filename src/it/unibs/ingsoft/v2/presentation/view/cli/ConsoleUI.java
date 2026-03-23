package it.unibs.ingsoft.v2.presentation.view.cli;

import it.unibs.ingsoft.v2.domain.Campo;
import it.unibs.ingsoft.v2.domain.Categoria;
import it.unibs.ingsoft.v2.domain.Proposta;
import it.unibs.ingsoft.v2.domain.TipoDato;
import it.unibs.ingsoft.v2.presentation.view.contract.BackException;
import it.unibs.ingsoft.v2.presentation.view.contract.CancelException;
import it.unibs.ingsoft.v2.presentation.view.contract.IAppView;
import it.unibs.ingsoft.v2.presentation.view.contract.OperationCancelledException;
import it.unibs.ingsoft.v2.presentation.view.contract.ProposalFieldValidator;
import it.unibs.ingsoft.v2.presentation.view.validation.DefaultTypeValidator;
import it.unibs.ingsoft.v2.presentation.view.validation.TypeValidator;

import java.io.Console;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * Console (stdin/stdout) implementation of {@link IAppView}.
 *
 * UX design rules enforced here:
 *  - Every string read checks for CANCEL_KEYWORD / BACK_KEYWORD and throws accordingly.
 *  - Validation is inline (field-by-field), never deferred to post-submission.
 *  - Batch input (acquisisciListaNomi) shows a running count, detects duplicates inline,
 *    and requires an explicit review/confirm step before returning.
 *  - Password input uses System.console().readPassword() when available.
 */
public final class ConsoleUI implements IAppView {
    public static final String CANCEL_KEYWORD = "annulla";
    public static final String BACK_KEYWORD   = "indietro";

    /** Context hint shown at the start of every form that accepts free-text input. */
    public static final String HINT_ANNULLA =
            "Digita '" + CANCEL_KEYWORD + "' per annullare.";

    private static final String SEPARATORE       = "-".repeat(60);
    private static final String SEPARATORE_DOPPIO = "═".repeat(60);

    private final Scanner scanner;

    public ConsoleUI(Scanner scanner) {
        this.scanner = scanner;
    }

    // ----------------------------------------------------------------
    // IOutputView — basic output
    // ----------------------------------------------------------------

    @Override
    public void stampa(String testo) {
        System.out.println(testo);
    }

    @Override
    public void newLine() {
        System.out.println();
    }

    @Override
    public void header(String titolo) {
        newLine();
        System.out.println(SEPARATORE_DOPPIO);
        System.out.println("  " + titolo.toUpperCase());
        System.out.println(SEPARATORE_DOPPIO);
    }

    @Override
    public void stampaSezione(String titolo) {
        newLine();
        System.out.println("-- " + titolo + " " + "-".repeat(Math.max(0, 56 - titolo.length())));
    }

    @Override
    public void stampaCampi(List<Campo> campi)
    {
        if (campi.isEmpty()) {
            stampa("    (nessuno campo)");
            return;
        }
        for (Campo c : campi)
            stampa("  - " + c);
    }

    @Override
    public void stampaCategorie(List<Categoria> categorie)
    {
        if (categorie.isEmpty()) {
            stampa("  (nessuna categoria)");
            return;
        }
        for (Categoria cat : categorie)
        {
           stampa("  - " + cat.getNome());
            for (Campo c : cat.getCampiSpecifici())
                stampa("      - " + c);
        }
    }

    @Override
    public void stampaCategorieDettaglio(Map<String, List<String>> categorieConCampi)
    {
        if (categorieConCampi.isEmpty())
        {
            stampa("    (nessuna categoria)");
            return;
        }
        for (Map.Entry<String, List<String>> entry : categorieConCampi.entrySet())
        {
            stampa("    - " + entry.getKey());
            if (entry.getValue().isEmpty())
                stampa("          (nessun campo specifico)");
            else
                entry.getValue().forEach(c -> stampa("          - " + c));
        }
    }

    @Override
    public void pausa() {
        System.out.print("Premere INVIO per continuare...");
        scanner.nextLine();
    }

    @Override
    public void stampaSuccesso(String msg) {
        stampa("  ✅ " + msg);
    }

    @Override
    public void stampaErrore(String msg) {
        stampa("  ❌ " + msg);
    }

    @Override
    public void stampaAvviso(String msg) {
        stampa("  ⚠️  " + msg);
    }

    @Override
    public void stampaInfo(String msg) {
        stampa("  ℹ️  " + msg);
    }

    @Override
    public void pausaConSpaziatura() {
        IAppView.super.pausaConSpaziatura();
    }

    @Override
    public void stampaMenu(String titolo, String[] lista, String uscitaLabel) {
        if (!titolo.isBlank())
            header(titolo);

        if (lista.length == 0)
            return;

        IntStream.range(0, lista.length)
                .forEach(i -> stampa((i + 1) + ") " + lista[i]));

        stampa("0) " + uscitaLabel);
        newLine();
    }

    @Override
    public void stampaMenu(String titolo, String[] lista) {
        stampaMenu(titolo, lista, "Torna");
    }


    @Override
    public void mostraBacheca(Map<String, List<Proposta>> bacheca)
    {
        if (bacheca.isEmpty())
        {
            System.out.println("  La bacheca è vuota.");
            return;
        }
        bacheca.forEach((categoria, proposte) ->
        {
            stampaSezione("Categoria: " + categoria);
            for (Proposta p : proposte) {
                //System.out.println("  [" + (i + 1) + "] Proposta — Pubblicata il: "
                //        + (p.dataPubblicazione() != null ? p.dataPubblicazione() : "N/A"));
                //System.out.println("      Termine iscrizioni:   "
                //        + (p.termineIscrizione() != null ? p.termineIscrizione() : "N/A"));
                for (String campo : p.getValoriCampi().keySet()) {
                    String valore = p.getValoriCampi().getOrDefault(campo, "");
                    if (!valore.isBlank())
                        System.out.println("      " + campo + ": " + valore);
                }
                newLine();
            }
        });
    }

    @Override
    public void mostraRiepilogoProposta(Proposta p)
    {
        newLine();
        System.out.println(SEPARATORE);
        System.out.println("  RIEPILOGO PROPOSTA — Categoria: " + p.getCategoria().getNome()
                + " | Stato: " + p.getStato());
        System.out.println(SEPARATORE);
        for (String campo : p.getValoriCampi().keySet())
        {
            String valore = p.getValoriCampi().getOrDefault(campo, "");
            System.out.println("  " + campo + ": " + (valore.isBlank() ? "(non compilato)" : valore));
        }
        System.out.println(SEPARATORE);
    }

    // ----------------------------------------------------------------
    // IInputView — core string input with keyword detection
    // ----------------------------------------------------------------

    /**
     * Reads one line from stdin, trims it, then checks for cancel/back keywords.
     *
     * @throws CancelException if the trimmed input equals {@link #CANCEL_KEYWORD} (case-insensitive)
     * @throws BackException   if the trimmed input equals {@link #BACK_KEYWORD}   (case-insensitive)
     */
    @Override
    public String acquisisciStringa(String prompt)
    {
        System.out.print(prompt);
        String line    = scanner.nextLine();
        String trimmed = (line == null) ? "" : line.trim();

        if (CANCEL_KEYWORD.equalsIgnoreCase(trimmed)) throw new CancelException();
        if (BACK_KEYWORD.equalsIgnoreCase(trimmed))   throw new BackException();

        return trimmed;
    }

    // ---------------------------------------------------------------
    // Inline-validated string input
    // ---------------------------------------------------------------

    @Override
    public String acquisisciStringaConValidazione(String prompt,
                                                   Predicate<String> validatore,
                                                   String messaggioErrore)
    {
        while (true)
        {
            String val = acquisisciStringa(prompt);
            if (validatore.test(val)) return val;
            stampaErrore(messaggioErrore);
        }
    }

    // ---------------------------------------------------------------
    // Password input (masked when System.console() is available)
    // ---------------------------------------------------------------

    @Override
    public String acquisisciPassword(String prompt)
    {
        Console console = System.console();
        if (console != null)
        {
            char[] pwd = console.readPassword(prompt);
            String value = pwd != null ? new String(pwd) : "";
            String trimmed = value.trim();

            if (CANCEL_KEYWORD.equalsIgnoreCase(trimmed)) throw new CancelException();
            if (BACK_KEYWORD.equalsIgnoreCase(trimmed))   throw new BackException();

            return value;
        }

        return acquisisciStringa(prompt);
    }

    // ---------------------------------------------------------------
    // Integer and boolean input
    // ---------------------------------------------------------------
    @Override
    public int acquisisciIntero(String prompt, int min, int max)
    {
        while (true) {
            String s = acquisisciStringa(prompt);
            try
            {
                int v = Integer.parseInt(s);

                if (v < min || v > max)
                {
                    stampa("Inserisci un numero tra " + min + " e " + max + ".");
                    continue;
                }

                return v;
            }
            catch (NumberFormatException e)
            {
                stampa("Inserisci un intero valido.");
            }
        }
    }

    @Override
    public boolean acquisisciSiNo(String prompt)
    {
        while (true) {
            String s = acquisisciStringa(prompt + " (s/n): ").toLowerCase();
            if (s.equals("s") || s.equals("si") || s.equals("sì"))
                return true;
            if (s.equals("n") || s.equals("no"))
                return false;

            stampa("Rispondi con s/n.");
        }
    }

    @Override
    public TipoDato acquisisciTipoDato(String prompt)
    {
        TipoDato[] valori = TipoDato.values();
        stampa(prompt);
        for (int i = 0; i < valori.length; i++)
            stampa("  " + (i + 1) + ") " + valori[i]);
        newLine();

        int choice = acquisisciIntero("Scelta: ", 1, valori.length);
        return valori[choice - 1];
    }

    // ---------------------------------------------------------------
    // Batch name list with inline duplicate detection and review step
    // ---------------------------------------------------------------

    /**
     * Interactively collects a list of names:
     * <ul>
     *   <li>Running count shown in the prompt ({@code [N] > }).</li>
     *   <li>Case-insensitive duplicate detection — duplicates are warned and ignored.</li>
     *   <li>Blank line terminates entry.</li>
     *   <li>Review/confirm step before returning.</li>
     *   <li>If the user types the cancel keyword, {@link CancelException} is thrown.</li>
     * </ul>
     */
    @Override
    public List<String> acquisisciListaNomi(String titolo)
    {
        while (true) {
            stampa(titolo);
            stampaInfo("Riga vuota per terminare. " + HINT_ANNULLA);
            newLine();

            List<String> list = new ArrayList<>();

            while (true)
            {
                System.out.print("[" + list.size() + "] > ");
                String line    = scanner.nextLine();
                String trimmed = (line == null) ? "" : line.trim();

                if (CANCEL_KEYWORD.equalsIgnoreCase(trimmed)) throw new CancelException();
                if (BACK_KEYWORD.equalsIgnoreCase(trimmed))   throw new BackException();

                if (trimmed.isEmpty()) break;

                boolean duplicato = list.stream().anyMatch(n -> n.equalsIgnoreCase(trimmed));
                if (duplicato) {
                    stampaAvviso("'" + trimmed + "' già presente nella lista, ignorato.");
                    continue;
                }

                list.add(trimmed);
            }

            if (list.isEmpty())
            {
                stampaAvviso("Nessun nome inserito.");
                if (!acquisisciSiNo("Vuoi riprovare?")) return list;
                newLine();
                continue;
            }

            // Review step
            String riepilogo = String.join(", ", list);
            stampaInfo(list.size() + " element" + (list.size() == 1 ? "o" : "i") +
                       " inserit" + (list.size() == 1 ? "o" : "i") + ": " + riepilogo);

            if (acquisisciSiNo("Confermare?")) return list;

            // User said no -> restart
            newLine();
        }
    }

    // ----------------------------------------------------------------
    // IInputView — element selection
    // ----------------------------------------------------------------

    @Override
    public <T> Optional<T> selezionaElemento(String prompt, List<T> elementi)
    {
        if (elementi.isEmpty())
        {
            stampa("  (nessun elemento disponibile)");
            return Optional.empty();
        }

        stampa(prompt);
        for (int i = 0; i < elementi.size(); i++)
            stampa("  " + (i + 1) + ") " + elementi.get(i));
        stampa("  0) Annulla");
        newLine();

        try
        {
            int choice = acquisisciIntero("Scelta: ", 0, elementi.size());
            return choice == 0 ? Optional.empty() : Optional.of(elementi.get(choice - 1));
        }
        catch (OperationCancelledException e)
        {
            return Optional.empty();
        }
    }

    @Override
    public <T> Optional<T> selezionaElementoConInfo(String prompt, List<T> elementi,
                                                     Function<T, String> infoMapper)
    {
        if (elementi.isEmpty())
        {
            stampa("  (nessun elemento disponibile)");
            return Optional.empty();
        }

        stampa(prompt);
        for (int i = 0; i < elementi.size(); i++)
            stampa("  " + (i + 1) + ") " + elementi.get(i) +
                   "  [" + infoMapper.apply(elementi.get(i)) + "]");
        stampa("  0) Annulla");
        newLine();

        try
        {
            int choice = acquisisciIntero("Scelta: ", 0, elementi.size());
            return choice == 0 ? Optional.empty() : Optional.of(elementi.get(choice - 1));
        }
        catch (OperationCancelledException e)
        {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Map<String, String>> acquisisciValoriProposta(Proposta proposta, ProposalFieldValidator validator)
    {
        return eseguiForm(proposta, proposta.getCampi(), validator);
    }

    @Override
    public Optional<Map<String, String>> correggiCampiProposta(Proposta proposta, Set<String> nomiCampi, ProposalFieldValidator validator)
    {
        List<Campo> campiDaCorreggere = proposta.getCampi().stream()
                .filter(c -> nomiCampi.contains(c.getNome()))
                .toList();
        return eseguiForm(proposta, campiDaCorreggere, validator);
    }

    private Optional<Map<String, String>> eseguiForm(Proposta proposta, List<Campo> campi, ProposalFieldValidator validator)
    {
        Map<String, String> ctx = new LinkedHashMap<>(proposta.getValoriCampi());
        TypeValidator typeValidator = DefaultTypeValidator.INSTANCE;
        int i = 0;

        while (i < campi.size())
        {
            Campo campo = campi.get(i);
            String nome = campo.getNome();
            String current = ctx.get(nome);

            String obbLabel = campo.isObbligatorio() ? "(*) " : "";
            String attualeLabel = (current != null && !current.isBlank())
                    ? " [attuale: " + current + "]"
                    : "";
            String prompt = "[" + (i + 1) + "/" + campi.size() + "] " + obbLabel
                    + nome + " [" + campo.getTipoDato() + "]" + attualeLabel + ": ";

            String raw;
            try
            {
                raw = acquisisciStringa(prompt).trim();
            }
            catch (CancelException e)
            {
                return Optional.empty();
            }
            catch (BackException e)
            {
                if (i > 0) i--;
                continue;
            }

            if (raw.isBlank())
            {
                if (current != null && !current.isBlank())
                {
                    stampaSuccesso("  Campo invariato: " + current);
                    i++;
                }
                else if (!campo.isObbligatorio())
                {
                    i++;
                }
                else
                {
                    stampaErrore("  Campo obbligatorio. Inserire un valore.");
                }
                continue;
            }

            String typeError = typeValidator.validate(raw, campo.getTipoDato());
            if (typeError != null)
            {
                stampaErrore("  " + typeError);
                continue;
            }

            List<String> businessErrors = validator.validate(
                    proposta,
                    Collections.unmodifiableMap(ctx),
                    nome,
                    raw
            );
            if (!businessErrors.isEmpty())
            {
                for (String businessError : businessErrors)
                    stampaErrore("  " + businessError);
                continue;
            }

            ctx.put(nome, raw);
            stampaSuccesso("  OK");
            i++;
        }

        return Optional.of(ctx);
    }

    // ----------------------------------------------------------------
    // Input specializations
    // ----------------------------------------------------------------

    @Override
    public OptionalInt selezionaCategoria(List<Categoria> categorie)
    {
        if (categorie.isEmpty())
        {
            stampaAvviso("Nessuna categoria disponibile.");
            return OptionalInt.empty();
        }
        for (int i = 0; i < categorie.size(); i++)
            System.out.println("  " + (i + 1) + ". " + categorie.get(i).getNome());
        System.out.println("  0. Annulla");

        try
        {
            int scelta = acquisisciIntero("Scelta: ", 0, categorie.size());
            if (scelta == 0) return OptionalInt.empty();
            return OptionalInt.of(scelta - 1);
        }
        catch (OperationCancelledException e)
        {
            return OptionalInt.empty();
        }
    }
}
