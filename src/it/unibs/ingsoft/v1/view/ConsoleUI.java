package it.unibs.ingsoft.v1.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.IntStream;

/**
 * Classe che fornisce output formattato e lettura sicura dell'input.
 */
public final class ConsoleUI
{
    private final Scanner scanner;

    public ConsoleUI(Scanner scanner)
    {
        this.scanner = scanner;
    }

    // ---------------------------------------------------------------
    // OUTPUT BASE
    // ---------------------------------------------------------------

    public void stampa(String msg)
    {
        System.out.println(msg);
    }

    public void newLine()
    {
        System.out.println();
    }

    public void header(String titolo)
    {
        System.out.println("==================================================");
        System.out.println(titolo);
        System.out.println("==================================================");
    }

    public void stampaSezione(String titolo)
    {
        stampa("----- " + titolo + " -----");
    }

    // ---------------------------------------------------------------
    // INPUT BASE
    // ---------------------------------------------------------------

    public String acquisisciStringa(String prompt)
    {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    public int acquisisciIntero(String prompt, int min, int max)
    {
        while (true)
        {
            String s = acquisisciStringa(prompt);

            try
            {
                int v = Integer.parseInt(s.trim());

                if (v < min || v > max)
                {
                    stampa("Valore fuori range [" + min + ", " + max + "].");
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
            String s = acquisisciStringa(prompt + " (s/n): ").trim().toLowerCase();

            if (s.equals("s") || s.equals("si") || s.equals("sì"))
                return true;

            if (s.equals("n") || s.equals("no"))
                return false;

            stampa("Rispondi con s/n.");
        }
    }

    public List<String> acquisisciListaNomi(String titolo)
    {
        stampa(titolo);
        stampa("Inserisci un nome per riga. Riga vuota per terminare.");
        newLine();

        List<String> lista = new ArrayList<>();

        while (true)
        {
            String s = acquisisciStringa("> ");

            if (s == null || s.isBlank())
                break;

            lista.add(s.trim());
        }

        return lista;
    }

    // ---------------------------------------------------------------
    // STAMPA LISTE
    // ---------------------------------------------------------------

    public void stampaLista(List<?> elementi, String messaggioVuoto)
    {
        if (elementi == null || elementi.isEmpty())
        {
            stampa(messaggioVuoto);
            return;
        }

        for (Object e : elementi)
            stampa(" - " + e);
    }

    public void stampaCampi(List<?> campi)
    {
        stampaLista(campi, " (nessuno)");
    }

    public void stampaCategorie(List<?> categorie)
    {
        stampaLista(categorie, " (nessuna)");
    }

    // ---------------------------------------------------------------
    // MENU
    // ---------------------------------------------------------------

    public void stampaMenu(String titolo, String[] voci)
    {
        if (titolo != null && !titolo.isBlank())
            header(titolo);

        if (voci == null || voci.length == 0)
            return;

        IntStream.range(0, voci.length)
                .forEach(i -> stampa((i + 1) + ") " + voci[i]));

        stampa("0) Esci");
        newLine();
    }
}