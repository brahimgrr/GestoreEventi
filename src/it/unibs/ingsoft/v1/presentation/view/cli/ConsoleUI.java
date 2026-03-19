package it.unibs.ingsoft.v1.presentation.view.cli;

import it.unibs.ingsoft.v1.domain.Categoria;
import it.unibs.ingsoft.v1.presentation.view.contract.IAppView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/*
 * Classe che fornisce output formattato e lettura sicura dell'input.
 *
 * UX design rules enforced here:
 *  - Every string read checks for CANCEL_KEYWORD / BACK_KEYWORD and throws accordingly.
 *  - Validation is inline (field-by-field), never deferred to post-submission.
 *  - Batch input (acquisisciListaNomi) shows a running count, detects duplicates inline,
 *    and requires an explicit review/confirm step before returning.
 *  - Password input uses System.console().readPassword() when available.
 */

public final class ConsoleUI implements IAppView
{
    public static final String CANCEL_KEYWORD = "annulla";
    public static final String BACK_KEYWORD   = "indietro";

    /** Context hint shown at the start of every form that accepts free-text input. */
    public static final String HINT_ANNULLA =
            "Digita '" + CANCEL_KEYWORD + "' per annullare.";

    /** Thrown when the user types the cancel keyword during any string input. */
    public static class CancelException extends RuntimeException
    {
        public CancelException() { super("Operazione annullata dall'utente."); }
    }

    /** Thrown when the user types the back keyword during any string input. */
    public static class BackException extends RuntimeException
    {
        public BackException() { super(); }
    }

    private final Scanner scanner;

    public ConsoleUI(Scanner scanner)
    {
        this.scanner = scanner;
    }

    // ---------------------------------------------------------------
    // Basic output
    // ---------------------------------------------------------------

    public void stampa(String msg)
    {
        System.out.println(msg);
    }

    public void newLine()
    {
        System.out.println();
    }

    public void header(String title)
    {
        System.out.println("==================================================");
        System.out.println(title);
        System.out.println("==================================================");
    }

    public void stampaCampi(List<?> campi)
    {
        if (campi.isEmpty())
        {
            stampa("    (nessuno)");
            return;
        }

        for (Object c : campi)
            stampa("    · " + c);
    }

    public void stampaCategorie(List<?> categorie)
    {
        if (categorie.isEmpty())
        {
            stampa("    (nessuna)");
            return;
        }

        for (Object c : categorie)
            stampa("    · " + c);
    }

    public void stampaCategorieDettaglio(List<Categoria> categorie)
    {
        if (categorie.isEmpty())
        {
            stampa("    (nessuna)");
            return;
        }

        for (Categoria cat : categorie)
        {
            stampa("    · " + cat.getNome());
            if (cat.getCampiSpecifici().isEmpty())
                stampa("          (nessun campo specifico)");
            else
                cat.getCampiSpecifici().forEach(c -> stampa("          · " + c));
        }
    }

    public void stampaSezione(String titolo)
    {
        stampa("  " + titolo + ":");
    }

    public void pausa()
    {
        System.out.print("Premi INVIO per continuare...");
        scanner.nextLine();
    }

    public void stampaSuccesso(String msg) { stampa("  ✅ " + msg); }
    public void stampaErrore(String msg)   { stampa("  ❌ " + msg); }
    public void stampaAvviso(String msg)   { stampa("  ⚠️  " + msg); }
    public void stampaInfo(String msg)     { stampa("  ℹ️  " + msg); }

    public void stampaMenu(String titolo, String[] lista, String uscitaLabel)
    {
        if (!titolo.isBlank())
            header(titolo);

        if (lista.length == 0)
            return;

        IntStream.range(0, lista.length)
                .forEach(i -> stampa((i + 1) + ") " + lista[i]));

        stampa("0) " + uscitaLabel);
        newLine();
    }

    // ---------------------------------------------------------------
    // Core string input — keyword detection lives here
    // ---------------------------------------------------------------

    /**
     * Reads one line from stdin, trims it, then checks for cancel/back keywords.
     *
     * @throws CancelException if the trimmed input equals {@link #CANCEL_KEYWORD} (case-insensitive)
     * @throws BackException   if the trimmed input equals {@link #BACK_KEYWORD}   (case-insensitive)
     */
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

    /**
     * Acquires a string, re-prompting inline until {@code validator} passes.
     * Automatically propagates {@link CancelException} / {@link BackException}
     * because it calls {@link #acquisisciStringa} internally.
     *
     * @param messaggioErrore displayed with ❌ on each failed validation attempt
     */
    public String acquisisciStringaConValidazione(String prompt,
                                                   Predicate<String> validator,
                                                   String messaggioErrore)
    {
        while (true)
        {
            String val = acquisisciStringa(prompt);
            if (validator.test(val)) return val;
            stampaErrore(messaggioErrore);
        }
    }

    // ---------------------------------------------------------------
    // Password input (masked when System.console() is available)
    // ---------------------------------------------------------------

    /**
     * Acquires a password.
     * Uses {@link java.io.Console#readPassword} (masked) when available;
     * falls back to plain {@link #acquisisciStringa} with a one-time warning otherwise.
     */
    public String acquisisciPassword(String prompt)
    {
        java.io.Console console = System.console();

        if (console != null)
        {
            char[] pwd = console.readPassword(prompt);
            if (pwd == null) return "";
            String result = new String(pwd);
            Arrays.fill(pwd, '\0');
            return result;
        }

        // Fallback: IDE / piped input — masking not available, proceed silently
        return acquisisciStringa(prompt);
    }

    // ---------------------------------------------------------------
    // Integer and boolean input
    // ---------------------------------------------------------------

    public int acquisisciIntero(String prompt, int min, int max)
    {
        while (true)
        {
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

    public boolean acquisisciSiNo(String prompt)
    {
        while (true)
        {
            String s = acquisisciStringa(prompt + " (s/n): ").toLowerCase();
            if (s.equals("s") || s.equals("si") || s.equals("sì"))
                return true;
            if (s.equals("n") || s.equals("no"))
                return false;

            stampa("Rispondi con s/n.");
        }
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
    public List<String> acquisisciListaNomi(String titolo)
    {
        while (true)
        {
            stampa(titolo);
            stampaInfo("Riga vuota per terminare. " + HINT_ANNULLA);
            newLine();

            List<String> list = new ArrayList<>();

            while (true)
            {
                System.out.print("[" + list.size() + "] > ");
                String line    = scanner.nextLine();
                String trimmed = (line == null) ? "" : line.trim();

                // Cancel keyword check (must mirror acquisisciStringa behaviour)
                if (CANCEL_KEYWORD.equalsIgnoreCase(trimmed)) throw new CancelException();
                if (BACK_KEYWORD.equalsIgnoreCase(trimmed))   throw new BackException();

                // Blank line → end of input
                if (trimmed.isEmpty()) break;

                // Inline duplicate detection
                boolean duplicato = list.stream()
                        .anyMatch(n -> n.equalsIgnoreCase(trimmed));
                if (duplicato)
                {
                    stampaAvviso("'" + trimmed + "' già presente nella lista, ignorato.");
                    continue;
                }

                list.add(trimmed);
            }

            // Edge-case: nothing entered
            if (list.isEmpty())
            {
                stampaAvviso("Nessun nome inserito.");
                if (!acquisisciSiNo("Vuoi riprovare?")) return list;
                newLine();
                continue;
            }

            // Review step
            String riepilogo = list.stream().collect(Collectors.joining(", "));
            stampaInfo(list.size() + " element" + (list.size() == 1 ? "o" : "i") +
                       " inserit" + (list.size() == 1 ? "o" : "i") + ": " + riepilogo);

            if (acquisisciSiNo("Confermare?")) return list;

            // User said no → restart
            newLine();
        }
    }

    // ---------------------------------------------------------------
    // Element selection
    // ---------------------------------------------------------------

    public <T> Optional<T> selezionaElemento(String prompt, List<T> elementi)
    {
        if (elementi.isEmpty())
        {
            stampa("(nessun elemento disponibile)");
            return Optional.empty();
        }

        stampa(prompt);
        for (int i = 0; i < elementi.size(); i++)
            stampa("  " + (i + 1) + ") " + elementi.get(i));
        stampa("  0) Annulla");
        newLine();

        int choice = acquisisciIntero("Scelta: ", 0, elementi.size());
        return choice == 0 ? Optional.empty() : Optional.of(elementi.get(choice - 1));
    }

    /**
     * Like {@link #selezionaElemento} but appends extra info per element.
     *
     * <pre>
     *   1) Durata    [obbligatorio]
     *   2) Note      [facoltativo]
     *   0) Annulla
     * </pre>
     */
    public <T> Optional<T> selezionaElementoConInfo(String prompt,
                                                     List<T> elementi,
                                                     Function<T, String> infoMapper)
    {
        if (elementi.isEmpty())
        {
            stampa("(nessun elemento disponibile)");
            return Optional.empty();
        }

        stampa(prompt);
        for (int i = 0; i < elementi.size(); i++)
            stampa("  " + (i + 1) + ") " + elementi.get(i) +
                   "  [" + infoMapper.apply(elementi.get(i)) + "]");
        stampa("  0) Annulla");
        newLine();

        int choice = acquisisciIntero("Scelta: ", 0, elementi.size());
        return choice == 0 ? Optional.empty() : Optional.of(elementi.get(choice - 1));
    }
}
