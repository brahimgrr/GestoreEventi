package it.unibs.ingsoft.v5.view;

import it.unibs.ingsoft.v5.model.AppConstants;
import it.unibs.ingsoft.v5.model.Campo;
import it.unibs.ingsoft.v5.model.Categoria;
import it.unibs.ingsoft.v5.model.Notifica;
import it.unibs.ingsoft.v5.model.Proposta;
import it.unibs.ingsoft.v5.model.TipoDato;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Classe che fornisce output formattato e lettura sicura dell'input.
 */
public final class ConsoleUI implements IAppView
{
    /** Re-exported for backward compatibility; defined in AppConstants. */
    public static final DateTimeFormatter DATE_FMT = AppConstants.DATE_FMT;

    public static final String CANCEL_KEYWORD = "annulla";
    public static final String BACK_KEYWORD   = "indietro";

    /** Lanciata quando l'utente digita 'annulla' durante un form. */
    public static class CancelException extends RuntimeException
    {
        public CancelException() { super("Operazione annullata dall'utente."); }
    }

    /** Lanciata quando l'utente digita 'indietro' durante un form. */
    public static class BackException extends RuntimeException
    {
        public BackException() { super(); }
    }

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
        String line = scanner.nextLine();
        return (line == null) ? "" : line.trim();
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
            String s = acquisisciStringa(prompt + " (gg/mm/aaaa): ");

            if (s.equalsIgnoreCase(CANCEL_KEYWORD)) throw new CancelException();

            try
            {
                return LocalDate.parse(s, DATE_FMT);
            }
            catch (DateTimeParseException e)
            {
                stampa("  ❌ Data non valida. Usa il formato gg/mm/aaaa.");
            }
        }
    }

    public LocalDate acquisisciDataConVincolo(String prompt, LocalDate minData, String nomeMin)
    {
        while (true)
        {
            String s = acquisisciStringa(prompt + " (gg/mm/aaaa): ");

            if (s.equalsIgnoreCase(CANCEL_KEYWORD)) throw new CancelException();
            if (s.equalsIgnoreCase(BACK_KEYWORD))   throw new BackException();

            try
            {
                LocalDate d = LocalDate.parse(s, DATE_FMT);

                if (minData != null && !d.isAfter(minData.plusDays(1)))
                {
                    stampa("  ❌ Deve essere almeno 2 giorni dopo \"" + nomeMin
                           + "\" (" + minData.format(DATE_FMT) + ")."
                           + " Min: " + minData.plusDays(2).format(DATE_FMT) + ".");
                    continue;
                }

                stampa("  ✅ Valido");
                return d;
            }
            catch (DateTimeParseException e)
            {
                stampa("  ❌ Data non valida. Usa il formato gg/mm/aaaa.");
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
    public Map<String, String> compilaCampiProposta(
            Map<String, String> valoriEsistenti,
            List<Campo> campiBase,
            List<Campo> campiComuni,
            List<Campo> campiSpecifici
    )
    {
        Map<String, String> valori = new HashMap<>(valoriEsistenti);
        int totale = campiBase.size() + campiComuni.size() + campiSpecifici.size();
        int[] contatore = {0};

        if (!campiBase.isEmpty())
        {
            newLine();
            stampaSezione("Campi BASE (obbligatori)");

            for (Campo c : campiBase)
            {
                contatore[0]++;
                System.out.print("  [" + contatore[0] + "/" + totale + "] ");
                acquisisciValoreCampo(valori, c);
            }
        }

        if (!campiComuni.isEmpty())
        {
            newLine();
            stampaSezione("Campi COMUNI");

            for (Campo c : campiComuni)
            {
                contatore[0]++;
                System.out.print("  [" + contatore[0] + "/" + totale + "] ");
                acquisisciValoreCampo(valori, c);
            }
        }

        if (!campiSpecifici.isEmpty())
        {
            newLine();
            stampaSezione("Campi SPECIFICI della categoria");

            for (Campo c : campiSpecifici)
            {
                contatore[0]++;
                System.out.print("  [" + contatore[0] + "/" + totale + "] ");
                acquisisciValoreCampo(valori, c);
            }
        }

        return valori;
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

            // INVIO senza testo: mantieni valore esistente se presente, altrimenti blocca se obbligatorio
            if (input.isBlank())
            {
                if (c.isObbligatorio() && (esistente == null || esistente.isBlank()))
                {
                    stampa("  Campo obbligatorio. Inserisci un valore.");
                    continue;
                }

                if (esistente != null && !esistente.isBlank())
                {
                    valoriCampi.put(c.getNome(), esistente);
                    stampa("  ✅ Mantenuto: " + esistente);
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
    public void pausa()
    {
        acquisisciStringa("Premi INVIO per continuare...");
    }

    public void stampaSuccesso(String msg) { stampa("  ✅ " + msg); }
    public void stampaErrore(String msg)   { stampa("  ❌ " + msg); }
    public void stampaAvviso(String msg)   { stampa("  ⚠️  " + msg); }
    public void stampaInfo(String msg)     { stampa("  ℹ️  " + msg); }


    // ---------------------------------------------------------------
    // BACHECA
    // ---------------------------------------------------------------

    public void mostraBacheca(Map<String, List<Proposta>> bacheca,
                              Function<Proposta, List<Campo>> campiProvider)
    {
        if (bacheca.isEmpty())
        {
            stampa("La bacheca è vuota: nessuna proposta aperta.");
            return;
        }

        for (Map.Entry<String, List<Proposta>> entry : bacheca.entrySet())
        {
            stampaSezione("Categoria: " + entry.getKey());

            List<Proposta> proposte = entry.getValue();
            for (int i = 0; i < proposte.size(); i++)
            {
                Proposta p = proposte.get(i);
                stampa("  [Proposta #" + (i + 1) + "]  Pubblicata il: " + p.getDataPubblicazione()
                        + "  |  Termine iscrizione: " + p.getTermineIscrizione()
                        + "  |  Iscritti: " + p.getNumeroIscritti());

                for (Campo c : campiProvider.apply(p))
                {
                    String valore = p.getValoriCampi().get(c.getNome());
                    if (valore != null && !valore.isBlank())
                        stampa("    " + c.getNome() + ": " + valore);
                }

                newLine();
            }
        }
    }

    // ---------------------------------------------------------------
    // ARCHIVIO
    // ---------------------------------------------------------------

    public void mostraArchivio(List<Proposta> archivio, Function<Proposta, List<Campo>> campiProvider)
    {
        if (archivio.isEmpty())
        {
            stampa("L'archivio è vuoto.");
            return;
        }

        for (int i = 0; i < archivio.size(); i++)
        {
            Proposta p = archivio.get(i);
            stampa("  [" + (i + 1) + "] Stato: " + p.getStato()
                    + "  |  Categoria: " + p.getCategoria().getNome()
                    + "  |  Pubblicata: " + p.getDataPubblicazione());

            for (Campo c : campiProvider.apply(p))
            {
                String valore = p.getValoriCampi().get(c.getNome());
                if (valore != null && !valore.isBlank())
                    stampa("    " + c.getNome() + ": " + valore);
            }

            newLine();
        }
    }

    // ---------------------------------------------------------------
    // NOTIFICHE
    // ---------------------------------------------------------------

    public void mostraNotifiche(List<Notifica> notifiche)
    {
        if (notifiche.isEmpty())
        {
            stampa("Nessuna notifica.");
            return;
        }

        for (int i = 0; i < notifiche.size(); i++)
            stampa("  [" + (i + 1) + "] " + notifiche.get(i).getMessaggio()
                    + "  (ricevuta il: " + notifiche.get(i).getData() + ")");
    }

    public void mostraRiepilogoProposta(Proposta proposta, List<Campo> tuttiCampi)
    {
        stampa("══════════════════════════════════════════════════");
        stampa("  RIEPILOGO PROPOSTA");
        stampa("══════════════════════════════════════════════════");
        stampa(String.format("  %-22s: %s", "Categoria", proposta.getCategoria().getNome()));
        for (Campo c : tuttiCampi)
        {
            String val = proposta.getValoriCampi().getOrDefault(c.getNome(), "");
            if (!val.isBlank())
                stampa(String.format("  %-22s: %s", c.getNome(), val));
        }
        stampa("══════════════════════════════════════════════════");
        newLine();
    }

    public void correggiCampiNonValidi(Map<String, String> valori, List<Campo> campiConErrore)
    {
        stampaSezione("Correzione campi con errori");
        stampa("  INVIO = mantieni valore attuale.");
        newLine();
        for (Campo c : campiConErrore)
            acquisisciValoreCampo(valori, c);
    }

    public void mostraPropostePerIscrizione(List<Proposta> proposte)
    {
        for (int i = 0; i < proposte.size(); i++)
        {
            Proposta p    = proposte.get(i);
            String titolo = p.getValoriCampi().getOrDefault("Titolo", "senza titolo");
            String cat    = p.getCategoria().getNome();
            String data   = p.getValoriCampi().getOrDefault("Data", "?");
            String luogo  = p.getValoriCampi().getOrDefault("Luogo", "?");
            String quota  = p.getValoriCampi().getOrDefault("Quota individuale", "");
            int    max    = parseIntSafe(p.getValoriCampi().get("Numero di partecipanti"));

            stampa("  " + (i + 1) + ") [" + cat + "] " + titolo);
            stampa("     Data: " + data + " | Luogo: " + luogo);
            stampa("     Termine: " + p.getTermineIscrizione()
                   + " | Iscritti: " + (max > 0
                       ? p.getNumeroIscritti() + "/" + max
                       : String.valueOf(p.getNumeroIscritti()))
                   + (!quota.isBlank() ? " | Quota: €" + quota : ""));
            newLine();
        }
    }

    public void stampaCategorieSelezione(List<Categoria> categorie)
    {
        for (int i = 0; i < categorie.size(); i++)
            stampa("  " + (i + 1) + ") " + categorie.get(i).getNome());
    }

    public void stampaProposteRitirabili(List<Proposta> proposte)
    {
        for (int i = 0; i < proposte.size(); i++)
        {
            Proposta p    = proposte.get(i);
            String titolo = p.getValoriCampi().getOrDefault("Titolo", "senza titolo");
            stampa("  " + (i + 1) + ") [" + p.getStato() + "] " + titolo
                    + " | Data evento: " + p.getDataEvento());
        }
    }

    public void stampaProposteDisdici(List<Proposta> proposte)
    {
        for (int i = 0; i < proposte.size(); i++)
        {
            Proposta p    = proposte.get(i);
            String titolo = p.getValoriCampi().getOrDefault("Titolo", "senza titolo");
            stampa("  " + (i + 1) + ") " + titolo
                    + " | Termine: " + p.getTermineIscrizione());
        }
    }

    private static int parseIntSafe(String s)
    {
        if (s == null || s.isBlank()) return 0;
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return 0; }
    }

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