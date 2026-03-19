package it.unibs.ingsoft.v4.view;

import it.unibs.ingsoft.v4.model.AppConstants;
import it.unibs.ingsoft.v4.model.TipoDato;
import it.unibs.ingsoft.v4.view.viewmodel.CategoriaVM;
import it.unibs.ingsoft.v4.view.viewmodel.NotificaVM;
import it.unibs.ingsoft.v4.view.viewmodel.PropostaSelezionabileVM;
import it.unibs.ingsoft.v4.view.viewmodel.PropostaVM;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Scanner;
import java.util.stream.IntStream;

/**
 * Console implementation of {@link IAppView}.
 * Renders view-models to stdout and reads raw input from stdin.
 * Contains no domain-model imports (Proposta, Campo, Categoria, Notifica).
 * Contains no CancelException or BackException — cancellation is signalled
 * via {@link Optional#empty()} returned from {@link #runForm}.
 */
public final class ConsoleUI implements IAppView
{
    /** Re-exported for backward compatibility; defined in AppConstants. */
    public static final java.time.format.DateTimeFormatter DATE_FMT = AppConstants.DATE_FMT;

    private final Scanner scanner;

    public ConsoleUI(Scanner scanner)
    {
        this.scanner = scanner;
    }

    // ================================================================
    // IOutputView
    // ================================================================

    @Override
    public void stampa(String msg) { System.out.println(msg); }

    @Override
    public void newLine() { System.out.println(); }

    @Override
    public void header(String title)
    {
        System.out.println("==================================================");
        System.out.println(title);
        System.out.println("==================================================");
    }

    @Override
    public void stampaSezione(String titolo) { stampa("----- " + titolo + " -----"); }

    @Override
    public void stampaCampi(List<?> campi) { stampaLista(campi, " (nessuno)"); }

    @Override
    public void stampaCategorie(List<?> cat) { stampaLista(cat, " (nessuna)"); }

    @Override
    public void stampaMenu(String titolo, String[] lista)
    {
        if (titolo != null && !titolo.isBlank()) header(titolo);
        if (lista == null || lista.length == 0) return;
        IntStream.range(0, lista.length).forEach(i -> stampa((i + 1) + ") " + lista[i]));
        stampa("0) Esci");
        newLine();
    }

    @Override public void pausa() { acquisisciStringa("Premi INVIO per continuare..."); }

    @Override public void stampaSuccesso(String msg) { stampa("  ✅ " + msg); }
    @Override public void stampaErrore(String msg)   { stampa("  ❌ " + msg); }
    @Override public void stampaAvviso(String msg)   { stampa("  ⚠️  " + msg); }
    @Override public void stampaInfo(String msg)     { stampa("  ℹ️  " + msg); }

    // ================================================================
    // IInputView
    // ================================================================

    @Override
    public String acquisisciStringa(String prompt)
    {
        System.out.print(prompt);
        String line = scanner.nextLine();
        return (line == null) ? "" : line.trim();
    }

    @Override
    public int acquisisciIntero(String prompt, int min, int max)
    {
        while (true)
        {
            String s = acquisisciStringa(prompt);
            try
            {
                int v = Integer.parseInt(s.trim());
                if (v < min || v > max) { stampa("Valore fuori range [" + min + ", " + max + "]."); continue; }
                return v;
            }
            catch (NumberFormatException e) { stampa("Inserisci un intero valido."); }
        }
    }

    @Override
    public boolean acquisisciSiNo(String prompt)
    {
        while (true)
        {
            String s = acquisisciStringa(prompt + " (s/n): ").trim().toLowerCase();
            if (s.equals("s") || s.equals("si") || s.equals("sì")) return true;
            if (s.equals("n") || s.equals("no")) return false;
            stampa("Rispondi con s/n.");
        }
    }

    private static final TipoDato[] TIPI = TipoDato.values();

    @Override
    public TipoDato acquisisciTipoDato(String prompt)
    {
        stampa(prompt);
        for (int i = 0; i < TIPI.length; i++) stampa("  " + (i + 1) + ") " + TIPI[i]);
        int scelta = acquisisciIntero("Tipo: ", 1, TIPI.length);
        return TIPI[scelta - 1];
    }

    @Override
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

    // ================================================================
    // IFormView
    // ================================================================

    @Override
    public Optional<Map<String, String>> runForm(List<FormField> fields)
    {
        return new StepByStepFormRunner(this, this, DefaultTypeValidator.INSTANCE, fields).run();
    }

    // ================================================================
    // IDisplayView
    // ================================================================

    @Override
    public void mostraBacheca(Map<String, List<PropostaVM>> bachecaPerCategoria)
    {
        if (bachecaPerCategoria.isEmpty())
        {
            stampa("La bacheca è vuota: nessuna proposta aperta.");
            return;
        }

        for (Map.Entry<String, List<PropostaVM>> entry : bachecaPerCategoria.entrySet())
        {
            stampaSezione("Categoria: " + entry.getKey());
            List<PropostaVM> proposte = entry.getValue();
            for (int i = 0; i < proposte.size(); i++)
            {
                PropostaVM p = proposte.get(i);
                stampa("  [Proposta #" + (i + 1) + "]  Pubblicata il: " + p.dataPubblicazione()
                        + "  |  Termine iscrizione: " + p.termineIscrizione()
                        + "  |  Iscritti: " + p.numeroIscritti());
                for (String nome : p.campiOrdinati())
                {
                    String valore = p.valoriCampi().get(nome);
                    if (valore != null && !valore.isBlank()) stampa("    " + nome + ": " + valore);
                }
                newLine();
            }
        }
    }

    @Override
    public void mostraArchivio(List<PropostaVM> archivio)
    {
        if (archivio.isEmpty()) { stampa("L'archivio è vuoto."); return; }

        for (int i = 0; i < archivio.size(); i++)
        {
            PropostaVM p = archivio.get(i);
            stampa("  [" + (i + 1) + "] Stato: " + p.stato()
                    + "  |  Categoria: " + p.categoriaNome()
                    + "  |  Pubblicata: " + p.dataPubblicazione());
            for (String nome : p.campiOrdinati())
            {
                String valore = p.valoriCampi().get(nome);
                if (valore != null && !valore.isBlank()) stampa("    " + nome + ": " + valore);
            }
            newLine();
        }
    }

    @Override
    public void mostraNotifiche(List<NotificaVM> notifiche)
    {
        if (notifiche.isEmpty()) { stampa("Nessuna notifica."); return; }
        for (NotificaVM n : notifiche)
            stampa("  [" + n.index() + "] " + n.messaggio() + "  (ricevuta il: " + n.data() + ")");
    }

    @Override
    public void mostraRiepilogoProposta(PropostaVM proposta)
    {
        stampa("══════════════════════════════════════════════════");
        stampa("  RIEPILOGO PROPOSTA");
        stampa("══════════════════════════════════════════════════");
        stampa(String.format("  %-22s: %s", "Categoria", proposta.categoriaNome()));
        for (String nome : proposta.campiOrdinati())
        {
            String val = proposta.valoriCampi().getOrDefault(nome, "");
            if (!val.isBlank()) stampa(String.format("  %-22s: %s", nome, val));
        }
        stampa("══════════════════════════════════════════════════");
        newLine();
    }

    // ================================================================
    // ISelectionView
    // ================================================================

    @Override
    public OptionalInt selezionaCategoria(List<CategoriaVM> categorie)
    {
        for (CategoriaVM c : categorie) stampa("  " + c.index() + ") " + c.nome());
        newLine();
        int scelta = acquisisciIntero("Scegli categoria (0 per annullare): ", 0, categorie.size());
        return scelta == 0 ? OptionalInt.empty() : OptionalInt.of(scelta - 1);
    }

    @Override
    public OptionalInt selezionaPropostaPerIscrizione(List<PropostaSelezionabileVM> proposte)
    {
        for (PropostaSelezionabileVM p : proposte)
        {
            stampa("  " + p.index() + ") [" + p.categoriaNome() + "] " + p.titolo());
            stampa("     Data: " + p.data() + " | Luogo: " + p.luogo());
            stampa("     Termine: " + p.termineScritto()
                    + " | Iscritti: " + (p.maxPartecipanti() > 0
                        ? p.numeroIscritti() + "/" + p.maxPartecipanti()
                        : String.valueOf(p.numeroIscritti()))
                    + (!p.quota().isBlank() ? " | Quota: €" + p.quota() : ""));
            newLine();
        }
        int scelta = acquisisciIntero("Scegli proposta (0 per annullare): ", 0, proposte.size());
        return scelta == 0 ? OptionalInt.empty() : OptionalInt.of(scelta - 1);
    }

    @Override
    public OptionalInt selezionaPropostaDaDisdire(List<PropostaSelezionabileVM> proposte)
    {
        for (PropostaSelezionabileVM p : proposte)
            stampa("  " + p.index() + ") " + p.titolo() + " | Termine: " + p.termineScritto());
        newLine();
        int scelta = acquisisciIntero("Scegli proposta da disdire (0 per annullare): ", 0, proposte.size());
        return scelta == 0 ? OptionalInt.empty() : OptionalInt.of(scelta - 1);
    }

    @Override
    public OptionalInt selezionaPropostaDaRitirare(List<PropostaSelezionabileVM> proposte)
    {
        for (PropostaSelezionabileVM p : proposte)
            stampa("  " + p.index() + ") [" + p.stato() + "] " + p.titolo()
                    + " | Data evento: " + p.data());
        newLine();
        int scelta = acquisisciIntero("Scegli proposta da ritirare (0 per annullare): ", 0, proposte.size());
        return scelta == 0 ? OptionalInt.empty() : OptionalInt.of(scelta - 1);
    }

    @Override
    public OptionalInt selezionaNotificaDaEliminare(List<NotificaVM> notifiche)
    {
        if (notifiche.isEmpty()) { stampa("Nessuna notifica da eliminare."); return OptionalInt.empty(); }
        mostraNotifiche(notifiche);
        newLine();
        int scelta = acquisisciIntero("Numero notifica da eliminare (0 per annullare): ", 0, notifiche.size());
        return scelta == 0 ? OptionalInt.empty() : OptionalInt.of(scelta - 1);
    }

    // ================================================================
    // Private helpers
    // ================================================================

    private void stampaLista(List<?> elementi, String emptyMessage)
    {
        if (elementi == null || elementi.isEmpty()) { stampa(emptyMessage); return; }
        for (Object e : elementi) stampa(" - " + e);
    }
}
