package it.unibs.ingsoft.v2.controller;

import it.unibs.ingsoft.v2.model.*;
import it.unibs.ingsoft.v2.persistence.AppData;
import it.unibs.ingsoft.v2.persistence.DatabaseService;
import it.unibs.ingsoft.v2.service.AuthenticationService;
import it.unibs.ingsoft.v2.service.CategoriaService;
import it.unibs.ingsoft.v2.service.PropostaService;
import it.unibs.ingsoft.v2.view.ConsoleUI;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public final class App
{
    public static final String[] MENU_PRINCIPALE =
            {
                    "Gestire campi COMUNI",
                    "Gestire CATEGORIE e campi SPECIFICI",
                    "Visualizzare categorie e campi",
                    "Creare una proposta di iniziativa",
                    "Visualizzare la bacheca"
            };

    public static final String[] MENU_CAMPI_COMUNI =
            {
                    "Aggiungi campo comune",
                    "Rimuovi campo comune",
                    "Cambia obbligatorietà campo comune"
            };

    public static final String[] MENU_CATEGORIE =
            {
                    "Crea categoria",
                    "Rimuovi categoria",
                    "Gestisci campi specifici di una categoria"
            };

    public static final String[] MENU_CAMPI_SPECIFICI =
            {
                    "Aggiungi campo specifico",
                    "Rimuovi campo specifico",
                    "Cambia obbligatorietà campo specifico"
            };

    // ===== INIZIALIZZAZIONE =====

    public void inizializzazione()
    {
        Path storage = Path.of("data", "appdata2.ser");

        DatabaseService       db          = new DatabaseService(storage);
        AppData               data        = db.loadOrCreate();
        AuthenticationService auth        = new AuthenticationService(db, data);
        CategoriaService      catService  = new CategoriaService(db, data);
        PropostaService       propService = new PropostaService(db, data);

        try (Scanner sc = new Scanner(System.in))
        {
            ConsoleUI ui = new ConsoleUI(sc);

            ui.header("Iniziative - Versione 2");

            do {
                Configuratore logged = doLoginFlow(ui, auth);
                ui.stampa("Benvenuto, " + logged.getUsername() + "!");
                ui.newLine();

                // Primo avvio: i campi base fissi sono già stati inseriti da CategoriaService.
                // Chiedi se il configuratore vuole aggiungere campi base EXTRA.
                if (!catService.isCampiBaseFissati())
                    menuCampiBaseExtra(ui, catService);

                mainMenu(ui, catService, propService);

                ui.stampa("Logout effettuato.");
                ui.newLine();

            } while (ui.acquisisciSiNo("Vuoi accedere di nuovo?"));
        }
    }

    // ===== LOGIN =====

    private static Configuratore doLoginFlow(ConsoleUI ui, AuthenticationService auth)
    {
        while (true)
        {
            ui.stampa("LOGIN CONFIGURATORE");
            String u = ui.acquisisciStringa("Username: ").trim();
            String p = ui.acquisisciStringa("Password: ").trim();

            Configuratore opt = auth.login(u, p);

            if (opt == null)
            {
                ui.stampa("Credenziali non valide.");
                ui.newLine();
                continue;
            }

            ui.stampa("Login riuscito.");

            if (AuthenticationService.USERNAME_PREDEFINITO.equals(opt.getUsername()))
            {
                ui.newLine();
                ui.stampa("Primo accesso con credenziali predefinite.");
                ui.stampa("Devi scegliere credenziali personali per poter operare.");

                while (true)
                {
                    String newU = ui.acquisisciStringa("Nuovo username: ").trim();
                    String newP = ui.acquisisciStringa("Nuova password: ").trim();

                    try
                    {
                        Configuratore registered = auth.registraNuovoConfiguratore(newU, newP);
                        ui.stampa("Registrazione completata.");
                        ui.newLine();
                        return registered;
                    }
                    catch (IllegalArgumentException e)
                    {
                        ui.stampa("Errore: " + e.getMessage());
                    }
                }
            }

            ui.newLine();
            return opt;
        }
    }

    // ===== PRIMA CONFIGURAZIONE: CAMPI BASE EXTRA =====

    /**
     * Mostrata solo al primo accesso (quando i campi base non sono ancora "fissati").
     * I campi base della traccia sono già presenti automaticamente.
     * Qui il configuratore può aggiungere campi base EXTRA (opzionale).
     */
    private static void menuCampiBaseExtra(ConsoleUI ui, CategoriaService cs)
    {
        ui.header("PRIMA CONFIGURAZIONE – Campi base");
        ui.newLine();
        ui.stampa("I seguenti campi base sono già presenti (definiti dalla traccia):");
        ui.stampaCampi(cs.getCampiBase());
        ui.newLine();
        ui.stampa("Puoi aggiungere campi base EXTRA (obbligatori e immutabili).");
        ui.stampa("Questi campi NON potranno essere modificati o rimossi in futuro.");
        ui.newLine();

        boolean aggiungi = ui.acquisisciSiNo("Vuoi aggiungere campi base extra?");

        if (!aggiungi)
        {
            cs.fissaCampiBaseSenzaExtra();
            ui.stampa("Nessun campo extra aggiunto. Configurazione completata.");
            ui.newLine();
            ui.acquisisciStringa("Premi INVIO per continuare...");
            return;
        }

        List<String>   nomi = new ArrayList<>();
        List<TipoDato> tipi = new ArrayList<>();

        ui.stampa("Inserisci i campi base extra. Riga vuota per terminare.");
        ui.newLine();

        while (true)
        {
            String nome = ui.acquisisciStringa("Nome campo (INVIO per terminare): ").trim();
            if (nome.isBlank()) break;

            TipoDato tipo = ui.acquisisciTipoDato("Tipo del campo \"" + nome + "\":");
            nomi.add(nome);
            tipi.add(tipo);
        }

        if (nomi.isEmpty())
        {
            cs.fissaCampiBaseSenzaExtra();
            ui.stampa("Nessun campo extra inserito. Configurazione completata.");
        }
        else
        {
            try
            {
                cs.aggiungiCampiBaseExtra(nomi, tipi);
                ui.stampa("Campi base extra aggiunti e salvati.");
            }
            catch (IllegalArgumentException e)
            {
                ui.stampa("Errore: " + e.getMessage());
                cs.fissaCampiBaseSenzaExtra();
            }
        }

        ui.newLine();
        ui.acquisisciStringa("Premi INVIO per continuare...");
    }

    // ===== MENU PRINCIPALE =====

    private static void mainMenu(ConsoleUI ui, CategoriaService cs, PropostaService ps)
    {
        while (true)
        {
            ui.stampaMenu("MENU PRINCIPALE", MENU_PRINCIPALE);
            int choice = ui.acquisisciIntero("Scelta: ", 0, MENU_PRINCIPALE.length);
            ui.newLine();

            switch (choice)
            {
                case 1:
                    menuCampiComuni(ui, cs);
                    break;

                case 2:
                    menuCategorie(ui, cs);
                    break;

                case 3:
                    menuVisualizza(ui, cs);
                    break;

                case 4:
                    menuCreaProposta(ui, cs, ps);
                    break;

                case 5:
                    menuBacheca(ui, ps);
                    break;

                case 0:
                    return;
            }
        }
    }

    // ===== CAMPI COMUNI =====

    private static void menuCampiComuni(ConsoleUI ui, CategoriaService cs)
    {
        while (true)
        {
            ui.header("CAMPI COMUNI");
            ui.stampaSezione("Campi comuni attuali");
            ui.stampaCampi(cs.getCampiComuni());
            ui.stampaMenu("", MENU_CAMPI_COMUNI);

            int choice = ui.acquisisciIntero("Scelta: ", 0, MENU_CAMPI_COMUNI.length);
            ui.newLine();

            switch (choice)
            {
                case 1:
                    String nome1  = ui.acquisisciStringa("Nome campo: ").trim();
                    TipoDato td1  = ui.acquisisciTipoDato("Tipo del campo \"" + nome1 + "\":");
                    boolean obbl1 = ui.acquisisciSiNo("Obbligatorio?");

                    try
                    {
                        cs.aggiungiCampoComune(nome1, td1, obbl1);
                        ui.stampa("Campo comune aggiunto.");
                    }
                    catch (Exception e)
                    {
                        ui.stampa("Errore: " + e.getMessage());
                    }
                    break;

                case 2:
                    String nome2 = ui.acquisisciStringa("Nome campo da rimuovere: ");
                    ui.stampa(cs.rimuoviCampoComune(nome2) ? "Rimosso." : "Campo non trovato.");
                    break;

                case 3:
                    String nome3  = ui.acquisisciStringa("Nome campo: ");
                    boolean obbl3 = ui.acquisisciSiNo("Impostare come obbligatorio?");
                    ui.stampa(cs.setObbligatorietaCampoComune(nome3, obbl3) ? "Aggiornato." : "Campo non trovato.");
                    break;

                case 0:
                    return;
            }

            ui.newLine();
            ui.acquisisciStringa("Premi INVIO per continuare...");
        }
    }

    // ===== CATEGORIE =====

    private static void menuCategorie(ConsoleUI ui, CategoriaService cs)
    {
        while (true)
        {
            ui.header("CATEGORIE");
            ui.stampaSezione("Categorie attuali");
            ui.stampaCategorie(cs.getCategorie());
            ui.stampaMenu("", MENU_CATEGORIE);

            int choice = ui.acquisisciIntero("Scelta: ", 0, MENU_CATEGORIE.length);
            ui.newLine();

            switch (choice)
            {
                case 1:
                    String nomeNuova = ui.acquisisciStringa("Nome nuova categoria: ").trim();

                    try
                    {
                        cs.creaCategoria(nomeNuova);
                        ui.stampa("Categoria creata.");
                    }
                    catch (Exception e)
                    {
                        ui.stampa("Errore: " + e.getMessage());
                    }
                    break;

                case 2:
                    String nomeRimuovi = ui.acquisisciStringa("Nome categoria da rimuovere: ");
                    ui.stampa(cs.rimuoviCategoria(nomeRimuovi) ? "Rimossa." : "Categoria non trovata.");
                    break;

                case 3:
                    if (cs.getCategorie().isEmpty())
                    {
                        ui.stampa("Nessuna categoria presente.");
                        break;
                    }

                    String nomeCat = ui.acquisisciStringa("Nome categoria: ");

                    try
                    {
                        cs.getCategoriaOrThrow(nomeCat);
                        menuCampiSpecifici(ui, cs, nomeCat);
                    }
                    catch (Exception e)
                    {
                        ui.stampa("Errore: " + e.getMessage());
                    }
                    break;

                case 0:
                    return;
            }

            ui.newLine();
            ui.acquisisciStringa("Premi INVIO per continuare...");
        }
    }

    // ===== CAMPI SPECIFICI =====

    private static void menuCampiSpecifici(ConsoleUI ui, CategoriaService cs, String nomeCategoria)
    {
        while (true)
        {
            Categoria cat = cs.getCategoria(nomeCategoria);

            ui.header("CAMPI SPECIFICI - " + nomeCategoria);
            ui.stampaSezione("Campi BASE");
            ui.stampaCampi(cs.getCampiBase());
            ui.stampaSezione("Campi COMUNI");
            ui.stampaCampi(cs.getCampiComuni());
            ui.stampaSezione("Campi SPECIFICI");
            ui.stampaCampi(cat.getCampiSpecifici());
            ui.newLine();

            ui.stampaMenu("", MENU_CAMPI_SPECIFICI);

            int choice = ui.acquisisciIntero("Scelta: ", 0, MENU_CAMPI_SPECIFICI.length);
            ui.newLine();

            switch (choice)
            {
                case 1:
                    String nome1  = ui.acquisisciStringa("Nome campo specifico: ").trim();
                    TipoDato td1  = ui.acquisisciTipoDato("Tipo del campo \"" + nome1 + "\":");
                    boolean obbl1 = ui.acquisisciSiNo("Obbligatorio?");

                    try
                    {
                        cs.aggiungiCampoSpecifico(nomeCategoria, nome1, td1, obbl1);
                        ui.stampa("Campo specifico aggiunto.");
                    }
                    catch (Exception e)
                    {
                        ui.stampa("Errore: " + e.getMessage());
                    }
                    break;

                case 2:
                    String nome2 = ui.acquisisciStringa("Nome campo specifico da rimuovere: ");
                    ui.stampa(cs.rimuoviCampoSpecifico(nomeCategoria, nome2) ? "Rimosso." : "Campo non trovato.");
                    break;

                case 3:
                    String nome3  = ui.acquisisciStringa("Nome campo specifico: ");
                    boolean obbl3 = ui.acquisisciSiNo("Impostare come obbligatorio?");
                    ui.stampa(cs.setObbligatorietaCampoSpecifico(nomeCategoria, nome3, obbl3) ? "Aggiornato." : "Campo non trovato.");
                    break;

                case 0:
                    return;
            }

            ui.newLine();
            ui.acquisisciStringa("Premi INVIO per continuare...");
        }
    }

    // ===== VISUALIZZA =====

    private static void menuVisualizza(ConsoleUI ui, CategoriaService cs)
    {
        ui.header("RIEPILOGO CATEGORIE E CAMPI");

        ui.stampaSezione("Campi BASE");
        ui.stampaCampi(cs.getCampiBase());
        ui.newLine();

        ui.stampaSezione("Campi COMUNI");
        ui.stampaCampi(cs.getCampiComuni());
        ui.newLine();

        ui.stampaSezione("CATEGORIE");
        List<Categoria> categorie = cs.getCategorie();
        ui.stampaCategorie(categorie);
        ui.newLine();

        for (Categoria cat : categorie)
        {
            ui.stampaSezione("Campi specifici di: " + cat.getNome());
            ui.stampaCampi(cat.getCampiSpecifici());
            ui.newLine();
        }

        ui.acquisisciStringa("Premi INVIO per continuare...");
    }

    // ===== CREA PROPOSTA =====

    private static void menuCreaProposta(ConsoleUI ui, CategoriaService cs, PropostaService ps)
    {
        ui.header("CREA PROPOSTA");

        if (cs.getCategorie().isEmpty())
        {
            ui.stampa("Nessuna categoria disponibile. Crea almeno una categoria prima.");
            ui.newLine();
            ui.acquisisciStringa("Premi INVIO per continuare...");
            return;
        }

        ui.stampaSezione("Categorie disponibili");
        ui.stampaCategorie(cs.getCategorie());
        ui.newLine();

        String nomeCategoria = ui.acquisisciStringa("Nome categoria per la proposta: ").trim();
        Proposta proposta;

        try
        {
            proposta = ps.creaProposta(nomeCategoria);
        }
        catch (IllegalArgumentException e)
        {
            ui.stampa("Errore: " + e.getMessage());
            ui.newLine();
            ui.acquisisciStringa("Premi INVIO per continuare...");
            return;
        }

        while (true)
        {
            ui.newLine();
            ui.stampaSezione("Compilazione campi");
            ui.stampa("(*) = obbligatorio | il tipo è indicato tra [  ]");
            ui.stampa("Per i campi DATA usare il formato gg/mm/aaaa.");
            ui.newLine();

            ui.compilaCampiProposta(
                    proposta.getValoriCampi(),
                    ps.getCampiBase(),
                    ps.getCampiComuni(),
                    proposta.getCategoria().getCampiSpecifici()
            );

            List<String> errori = ps.validaProposta(proposta);

            if (errori.isEmpty())
            {
                ui.stampa("Proposta VALIDA.");
                break;
            }

            ui.newLine();
            ui.stampa("La proposta NON è valida per i seguenti motivi:");

            for (String err : errori)
                ui.stampa("  - " + err);

            ui.newLine();

            boolean correggi = ui.acquisisciSiNo("Vuoi correggere i campi?");

            if (!correggi)
            {
                ui.stampa("Proposta scartata.");
                ui.newLine();
                ui.acquisisciStringa("Premi INVIO per continuare...");
                return;
            }
        }

        ui.newLine();
        boolean pubblica = ui.acquisisciSiNo("Vuoi pubblicare la proposta in bacheca?");

        if (pubblica)
        {
            try
            {
                ps.pubblicaProposta(proposta);
                ui.stampa("Proposta pubblicata in bacheca!");
            }
            catch (Exception e)
            {
                ui.stampa("Errore: " + e.getMessage());
            }
        }
        else
        {
            ui.stampa("Proposta non pubblicata. Verrà scartata alla fine della sessione.");
        }

        ui.newLine();
        ui.acquisisciStringa("Premi INVIO per continuare...");
    }

    // ===== BACHECA =====

    private static void menuBacheca(ConsoleUI ui, PropostaService ps)
    {
        ui.header("BACHECA");

        Map<String, List<Proposta>> bacheca = ps.getBachecaPerCategoria();

        if (bacheca.isEmpty())
        {
            ui.stampa("La bacheca è vuota: nessuna proposta aperta.");
            ui.newLine();
            ui.acquisisciStringa("Premi INVIO per continuare...");
            return;
        }

        for (Map.Entry<String, List<Proposta>> entry : bacheca.entrySet())
        {
            ui.stampaSezione("Categoria: " + entry.getKey());

            List<Proposta> proposte = entry.getValue();

            for (int i = 0; i < proposte.size(); i++)
            {
                Proposta p = proposte.get(i);
                ui.stampa("  [Proposta #" + (i + 1) + "]  Pubblicata il: " + p.getDataPubblicazione());

                for (Campo c : ps.getTuttiCampi(p))
                {
                    String valore = p.getValoriCampi().get(c.getNome());

                    if (valore != null && !valore.isBlank())
                        ui.stampa("    " + c.getNome() + ": " + valore);
                }

                ui.newLine();
            }
        }

        ui.acquisisciStringa("Premi INVIO per continuare...");
    }
}