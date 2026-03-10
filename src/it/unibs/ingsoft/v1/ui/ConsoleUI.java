package it.unibs.ingsoft.v1.ui;

import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.IntStream;

/*
 * Classe che fornisce output formattato e lettura sicura dell'input.
 */

public final class ConsoleUI
{
    private final Scanner scanner;

    public ConsoleUI(Scanner scanner)
    {
        this.scanner = scanner;
    }

    public void stampa(String msg)
    {
        System.out.println(msg);
    }

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
        List<String> list = new ArrayList<>();

        while (true)
        {
            String s = acquisisciStringa("> ");

            if (s == null || s.isBlank())
                break;

            list.add(s.trim());
        }

        return list;
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
            stampa(" (nessuno)");
            return;
        }

        for (Object c : campi)
            stampa(" - " + c);
    }

    public void stampaCategorie(List<?> categorie)
    {
        if (categorie.isEmpty())
        {
            stampa(" (nessuna)");
            return;
        }

        for (Object c : categorie)
            stampa(" - " + c);
    }

    public void stampaSezione(String titolo)
    {
        stampa("----- " + titolo + " -----");
    }

    public void stampaMenu (String titolo, String[] lista)
    {
        if (!titolo.isBlank())
            header(titolo);

        if (lista.length == 0)
            return;

        IntStream.range(0, lista.length)
                .forEach(i -> stampa((i+1) + ") " + lista[i]));

        stampa(0+") Esci");

        newLine();
    }
}
