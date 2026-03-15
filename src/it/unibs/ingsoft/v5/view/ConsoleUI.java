package it.unibs.ingsoft.v5.view;

import it.unibs.ingsoft.v5.model.Campo;
import it.unibs.ingsoft.v5.model.TipoDato;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.IntStream;

/**
 * Classe che fornisce output formattato e lettura sicura dell'input.
 */
public final class ConsoleUI
{
    public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Scanner scanner;

    public ConsoleUI(Scanner scanner)
    {
        this.scanner = scanner;
    }

    /*
     * OUTPUT BASE
     */
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

    public void stampaSezione(String titolo)
    {
        stampa("----- " + titolo + " -----");
    }

    /*
     * INPUT BASE
     */
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

            }	catch (NumberFormatException e) {
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

    public LocalDate acquisisciData(String prompt)
    {
        while (true)
        {
            String s = acquisisciStringa(prompt + " (gg/mm/aaaa): ").trim();

            try
            {
                return LocalDate.parse(s, DATE_FMT);

            }	catch (DateTimeParseException e) {
                stampa("Data non valida. Usa il formato gg/mm/aaaa.");
            }
        }
    }

    /**
     * SCELTA TIPO DATO
     */
    private static final TipoDato[] TIPI = TipoDato.values();

    /**
     * Mostra un menu con i tipi di dato disponibili e restituisce quello scelto.
     */
    public TipoDato acquisisciTipoDato(String prompt)
    {
        stampa(prompt);

        for (int i = 0; i < TIPI.length; i++)
            stampa("  " + (i + 1) + ") " + TIPI[i]);

        int scelta = acquisisciIntero("Tipo: ", 1, TIPI.length);

        return TIPI[scelta - 1];
    }

    // ---------------------------------------------------------------
    // INPUT LISTE
    // ---------------------------------------------------------------

    public List<String> acquisisciListaNomi(String titolo)
    {
        stampa(titolo);
        stampa("Inserisci un nome per riga. Riga vuota per terminare.");
        newLine();
        List<String> list = new ArrayList<>();
        while (true)
        {
            String s = acquisisciStringa("> ");
            if (s == null || s.isBlank()) break;
            list.add(s.trim());
        }
        return list;
    }

    // ---------------------------------------------------------------
    // COMPILAZIONE PROPOSTA
    // ---------------------------------------------------------------

    /**
     * Guida il configuratore nella compilazione dei campi di una proposta,
     * suddivisi per sezione (base -> comuni -> specifici).
     * Usa il TipoDato di ciascun campo per validare l'input.
     */
    public void compilaCampiProposta(
            Map<String, String> valoriCampi,
            List<Campo> campiBase,
            List<Campo> campiComuni,
            List<Campo> campiSpecifici
    )
    {
        if (!campiBase.isEmpty())
        {
            stampaSezione("Campi BASE (obbligatori)");

            for (Campo c : campiBase)
                acquisisciValoreCampo(valoriCampi, c);

        }

        if (!campiComuni.isEmpty())
        {
            newLine();
            stampaSezione("Campi COMUNI");

            for (Campo c : campiComuni)
                acquisisciValoreCampo(valoriCampi, c);

        }

        if (!campiSpecifici.isEmpty())
        {
            newLine();
            stampaSezione("Campi SPECIFICI della categoria");

            for (Campo c : campiSpecifici)
                acquisisciValoreCampo(valoriCampi, c);

        }
    }

    /**
     * Acquisisce e valida il valore per un singolo campo in base al suo TipoDato.
     * - Campi obbligatori: non accetta input vuoto.
     * - Campi facoltativi: INVIO senza testo = non compilato.
     * - Mostra il valore attuale tra parentesi se gia presente (utile in fase di correzione).
     */
    public void acquisisciValoreCampo(Map<String, String> valoriCampi, Campo c)
    {
        String esistente = valoriCampi.get(c.getNome());
        String etichetta = buildEtichetta(c, esistente);

        while (true)
        {
            String input = acquisisciStringa(etichetta + ": ").trim();

            // Campo facoltativo lasciato vuoto: mantieni eventuale valore precedente
            if (input.isBlank())
            {
                if (c.isObbligatorio())
                {
                    stampa("  Campo obbligatorio. Inserisci un valore.");
                    continue;
                }

                break;
            }

            // Valida in base al tipo
            String errore = validaInput(input, c.getTipoDato());
            if (errore != null)
            {
                stampa("  " + errore);
                continue;
            }

            valoriCampi.put(c.getNome(), input);
            break;
        }
    }

    // ---------------------------------------------------------------
    // VALIDAZIONE INPUT PER TIPO
    // ---------------------------------------------------------------

    /**
     * Valida una stringa rispetto al TipoDato.
     * Restituisce null se il valore e valido, oppure un messaggio d'errore.
     */
    public static String validaInput(String valore, TipoDato tipo)
    {
        if (valore == null || valore.isBlank())
            return "Valore vuoto.";

        return switch (tipo)
        {
            case STRINGA ->
                    null;

            case INTERO ->
            {
                try
                {
                    Integer.parseInt(valore.trim());
                    yield null;

                }	catch (NumberFormatException e) {
                    yield "Valore non valido per tipo INTERO (es. 10).";
                }
            }

            case DECIMALE ->
            {
                try
                {
                    Double.parseDouble(valore.trim().replace(',', '.'));
                    yield null;

                }	catch (NumberFormatException e) {
                    yield "Valore non valido per tipo DECIMALE (es. 15.50 o 15,50).";
                }
            }

            case DATA ->
            {
                try
                {
                    LocalDate.parse(valore.trim(), DATE_FMT);
                    yield null;

                }	catch (DateTimeParseException e) {
                    yield "Formato data non valido. Usa gg/mm/aaaa (es. 25/12/2025).";
                }
            }

            case BOOLEANO ->
            {
                String v = valore.trim().toLowerCase();

                if (v.equals("s") || v.equals("si") || v.equals("sì") ||
                        v.equals("n") || v.equals("no") || v.equals("true") || v.equals("false"))
                    yield null;

                yield "Valore non valido per tipo BOOLEANO. Usa s/n oppure true/false.";
            }
        };
    }

    // ---------------------------------------------------------------
    // STAMPA LISTE
    // ---------------------------------------------------------------

    public void stampaLista(List<?> elementi, String emptyMessage)
    {
        if (elementi == null || elementi.isEmpty())
        {
            stampa(emptyMessage);
            return;
        }

        for (Object e : elementi)
            stampa(" - " + e);

    }

    public void stampaCampi(List<?> campi)
    {
        stampaLista(campi, " (nessuno)");
    }

    public void stampaCategorie(List<?> cat)
    {
        stampaLista(cat, " (nessuna)");
    }

    // ---------------------------------------------------------------
    // MENU
    // ---------------------------------------------------------------

    public void stampaMenu(String titolo, String[] lista)
    {
        if (titolo != null && !titolo.isBlank())
            header(titolo);

        if (lista == null || lista.length == 0)
            return;

        IntStream.range(0, lista.length)
                .forEach(i -> stampa((i + 1) + ") " + lista[i]));

        stampa("0) Esci");
        newLine();
    }

    // ---------------------------------------------------------------
    // UTILITY PRIVATE
    // ---------------------------------------------------------------

    private String buildEtichetta(Campo c, String valoreAttuale)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(c.getNome());
        sb.append(c.isObbligatorio() ? " (*)" : " (facoltativo)");
        sb.append(" [").append(c.getTipoDato()).append("]");
        if (valoreAttuale != null && !valoreAttuale.isBlank())
            sb.append(" [attuale: ").append(valoreAttuale).append("]");
        return sb.toString();
    }
}