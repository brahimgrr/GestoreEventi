package it.unibs.ingsoft.v2.presentation.view.cli;

import it.unibs.ingsoft.v2.domain.Campo;
import it.unibs.ingsoft.v2.domain.Categoria;
import it.unibs.ingsoft.v2.domain.TipoDato;
import it.unibs.ingsoft.v2.presentation.view.cli.viewmodel.CategoriaVM;
import it.unibs.ingsoft.v2.presentation.view.cli.viewmodel.PropostaVM;
import it.unibs.ingsoft.v2.presentation.view.contract.IAppView;

import java.io.Console;
import java.util.*;
import java.util.function.Predicate;

/**
 * Console (stdin/stdout) implementation of {@link IAppView}.
 */
public final class ConsoleUI implements IAppView
{
    private static final String SEPARATORE = "─".repeat(60);
    private static final String SEPARATORE_DOPPIO = "═".repeat(60);

    private final Scanner scanner;

    public ConsoleUI(Scanner scanner)
    {
        this.scanner = scanner;
    }

    // ----------------------------------------------------------------
    // IOutputView
    // ----------------------------------------------------------------

    @Override public void stampa(String testo)    { System.out.println(testo); }
    @Override public void newLine()               { System.out.println(); }

    @Override
    public void header(String titolo)
    {
        newLine();
        System.out.println(SEPARATORE_DOPPIO);
        System.out.println("  " + titolo.toUpperCase());
        System.out.println(SEPARATORE_DOPPIO);
        newLine();
    }

    @Override
    public void stampaSezione(String titolo)
    {
        newLine();
        System.out.println("── " + titolo + " " + "─".repeat(Math.max(0, 56 - titolo.length())));
    }

    @Override
    public void stampaCampi(List<Campo> campi)
    {
        if (campi.isEmpty()) { System.out.println("  (nessun campo)"); return; }
        for (Campo c : campi)
            System.out.println("  • " + c);
    }

    @Override
    public void stampaCategorie(List<Categoria> categorie)
    {
        if (categorie.isEmpty()) { System.out.println("  (nessuna categoria)"); return; }
        for (Categoria cat : categorie)
        {
            System.out.println("  ▶ " + cat.getNome());
            for (Campo c : cat.getCampiSpecifici())
                System.out.println("      – " + c);
        }
    }

    @Override
    public void stampaMenu(String titolo, String[] voci)
    {
        newLine();
        System.out.println(SEPARATORE);
        System.out.println("  " + titolo);
        System.out.println(SEPARATORE);
        for (int i = 0; i < voci.length; i++)
            System.out.println("  " + (i + 1) + ". " + voci[i]);
        System.out.println("  0. Esci");
        System.out.println(SEPARATORE);
    }

    @Override
    public void pausa()
    {
        System.out.print("Premere INVIO per continuare...");
        scanner.nextLine();
    }

    @Override public void stampaSuccesso(String msg) { System.out.println("✅ " + msg); }
    @Override public void stampaErrore(String msg)   { System.out.println("❌ " + msg); }
    @Override public void stampaAvviso(String msg)   { System.out.println("⚠️  " + msg); }
    @Override public void stampaInfo(String msg)     { System.out.println("ℹ️  " + msg); }

    @Override
    public void mostraBacheca(Map<String, List<PropostaVM>> bacheca)
    {
        if (bacheca.isEmpty())
        {
            System.out.println("  La bacheca è vuota.");
            return;
        }
        bacheca.forEach((categoria, proposte) ->
        {
            stampaSezione("Categoria: " + categoria);
            for (int i = 0; i < proposte.size(); i++)
            {
                PropostaVM p = proposte.get(i);
                System.out.println("  [" + (i + 1) + "] Proposta — Pubblicata il: "
                        + (p.dataPubblicazione() != null ? p.dataPubblicazione() : "N/A"));
                System.out.println("      Termine iscrizioni: "
                        + (p.termineIscrizione() != null ? p.termineIscrizione() : "N/A"));
                for (String campo : p.campiOrdinati())
                {
                    String valore = p.valoriCampi().getOrDefault(campo, "");
                    if (!valore.isBlank())
                        System.out.println("      " + campo + ": " + valore);
                }
                newLine();
            }
        });
    }

    @Override
    public void mostraRiepilogoProposta(PropostaVM p)
    {
        newLine();
        System.out.println(SEPARATORE);
        System.out.println("  RIEPILOGO PROPOSTA — Categoria: " + p.categoriaNome()
                + " | Stato: " + p.stato());
        System.out.println(SEPARATORE);
        for (String campo : p.campiOrdinati())
        {
            String valore = p.valoriCampi().getOrDefault(campo, "");
            System.out.println("  " + campo + ": " + (valore.isBlank() ? "(non compilato)" : valore));
        }
        System.out.println(SEPARATORE);
    }

    // ----------------------------------------------------------------
    // IInputView
    // ----------------------------------------------------------------

    @Override
    public String acquisisciStringa(String prompt)
    {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    @Override
    public String acquisisciStringaConValidazione(String prompt, Predicate<String> validatore, String errorMsg)
    {
        while (true)
        {
            String val = acquisisciStringa(prompt).trim();
            if (validatore.test(val)) return val;
            stampaErrore(errorMsg);
        }
    }

    @Override
    public String acquisisciPassword(String prompt)
    {
        Console console = System.console();
        if (console != null)
        {
            char[] pwd = console.readPassword(prompt);
            return pwd != null ? new String(pwd) : "";
        }
        System.out.print(prompt);
        return scanner.nextLine();
    }

    @Override
    public int acquisisciIntero(String prompt, int min, int max)
    {
        while (true)
        {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try
            {
                int val = Integer.parseInt(line);
                if (val >= min && val <= max) return val;
                System.out.println("❌ Inserire un valore tra " + min + " e " + max + ".");
            }
            catch (NumberFormatException e)
            {
                System.out.println("❌ Inserire un numero intero.");
            }
        }
    }

    @Override
    public boolean acquisisciSiNo(String prompt)
    {
        while (true)
        {
            System.out.print(prompt + " (s/n): ");
            String r = scanner.nextLine().trim().toLowerCase();
            if (r.equals("s") || r.equals("si") || r.equals("sì")) return true;
            if (r.equals("n") || r.equals("no"))                   return false;
            System.out.println("❌ Rispondere con s (sì) o n (no).");
        }
    }

    @Override
    public TipoDato acquisisciTipoDato(String prompt)
    {
        TipoDato[] tipi = TipoDato.values();
        System.out.println(prompt);
        for (int i = 0; i < tipi.length; i++)
            System.out.println("  " + (i + 1) + ". " + tipi[i]);
        int scelta = acquisisciIntero("Scelta: ", 1, tipi.length);
        return tipi[scelta - 1];
    }

    @Override
    public List<String> acquisisciListaNomi(String prompt)
    {
        System.out.println(prompt + " (riga vuota per terminare)");
        List<String> nomi = new ArrayList<>();
        while (true)
        {
            System.out.print("  Nome: ");
            String line = scanner.nextLine().trim();
            if (line.isBlank()) break;
            nomi.add(line);
        }
        return nomi;
    }

    // ----------------------------------------------------------------
    // IFormView
    // ----------------------------------------------------------------

    @Override
    public Optional<Map<String, String>> runForm(List<FormField> fields)
    {
        return new StepByStepFormRunner(this, this, DefaultTypeValidator.INSTANCE, fields).run();
    }

    // ----------------------------------------------------------------
    // ISelectionView
    // ----------------------------------------------------------------

    @Override
    public OptionalInt selezionaCategoria(List<CategoriaVM> categorie)
    {
        if (categorie.isEmpty())
        {
            stampaAvviso("Nessuna categoria disponibile.");
            return OptionalInt.empty();
        }
        for (CategoriaVM c : categorie)
            System.out.println("  " + c.indice() + ". " + c.nome());
        System.out.println("  0. Annulla");

        int scelta = acquisisciIntero("Scelta: ", 0, categorie.size());
        if (scelta == 0) return OptionalInt.empty();
        return OptionalInt.of(scelta - 1);
    }
}
