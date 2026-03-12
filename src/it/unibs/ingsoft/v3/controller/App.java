package it.unibs.ingsoft.v3.controller;

import it.unibs.ingsoft.v3.model.*;
import it.unibs.ingsoft.v3.persistence.AppData;
import it.unibs.ingsoft.v3.persistence.DatabaseService;
import it.unibs.ingsoft.v3.service.*;
import it.unibs.ingsoft.v3.view.ConsoleUI;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public final class App
{
    // ---- Configuratore menus ----
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

    // ---- Fruitore menus ----
    public static final String[] MENU_FRUITORE =
            {
                    "Visualizzare la bacheca",
                    "Iscriversi a una proposta",
                    "Spazio personale (notifiche)"
            };

    public static final String[] MENU_NOTIFICHE =
            {
                    "Elimina una notifica"
            };

    // ===== INITIALIZATION =====

    public void inizializzazione()
    {
        Path storage = Path.of("data", "appdata3.ser");

        DatabaseService       db          = new DatabaseService(storage);
        AppData               data        = db.loadOrCreate();
        AuthenticationService auth        = new AuthenticationService(db, data);
        CategoriaService      catService  = new CategoriaService(db, data);
        PropostaService       propService = new PropostaService(db, data);
        FruitoreService       fruService  = new FruitoreService(db, data);
        IscrizioneService     iscService  = new IscrizioneService(db, data, fruService);

        // Check expired proposals on every startup
        iscService.controllaScadenzeAlAvvio();

        try (Scanner sc = new Scanner(System.in))
        {
            ConsoleUI ui = new ConsoleUI(sc);
            ui.header("Iniziative - Versione 3");

            while (true)
            {
                ui.stampa("Accedi come:");
                ui.stampa("1) Configuratore");
                ui.stampa("2) Fruitore");
                ui.stampa("0) Esci");
                ui.newLine();
                int tipoUtente = ui.acquisisciIntero("Scelta: ", 0, 2);

                if (tipoUtente == 0)
                    break;

                if (tipoUtente == 1)
                {
                    Configuratore logged = doLoginConfiguratore(ui, auth);
                    ui.stampa("Benvenuto, " + logged.getUsername());
                    ui.newLine();

                    if (!catService.isCampiBaseFissati())
                        menuCampiBaseExtra(ui, catService);

                    mainMenuConfiguratore(ui, catService, propService);
                    ui.stampa("Logout effettuato.");
                    ui.newLine();
                }
                else
                {
                    Fruitore logged = doLoginFruitore(ui, fruService);
                    ui.stampa("Benvenuto, " + logged.getUsername());
                    ui.newLine();

                    mainMenuFruitore(ui, logged, propService, iscService, fruService);
                    ui.stampa("Logout effettuato.");
                    ui.newLine();
                }
            }
        }
    }

    // ===== CONFIGURATORE LOGIN =====

    private static Configuratore doLoginConfiguratore(ConsoleUI ui, AuthenticationService auth)
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
                ui.stampa("Devi scegliere credenziali personali.");

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

    // ===== FRUITORE LOGIN =====

    private static Fruitore doLoginFruitore(ConsoleUI ui, FruitoreService fruService)
    {
        while (true)
        {
            ui.stampa("LOGIN / REGISTRAZIONE FRUITORE");
            ui.stampa("1) Login");
            ui.stampa("2) Registrati");
            int scelta = ui.acquisisciIntero("Scelta: ", 1, 2);

            String u = ui.acquisisciStringa("Username: ").trim();
            String p = ui.acquisisciStringa("Password: ").trim();

            if (scelta == 1)
            {
                Fruitore f = fruService.login(u, p);

                if (f != null)
                {
                    ui.stampa("Login riuscito.");
                    ui.newLine();
                    return f;
                }

                ui.stampa("Credenziali non valide.");
                ui.newLine();
            }
            else
            {
                try
                {
                    Fruitore f = fruService.registraFruitore(u, p);
                    ui.stampa("Registrazione completata. Benvenuto!");
                    ui.newLine();
                    return f;
                }
                catch (IllegalArgumentException e)
                {
                    ui.stampa("Errore: " + e.getMessage());
                    ui.newLine();
                }
            }
        }
    }

    // ===== FRUITORE MAIN MENU =====

    private static void mainMenuFruitore(
            ConsoleUI ui,
            Fruitore fruitore,
            PropostaService ps,
            IscrizioneService is,
            FruitoreService fs)
    {
        while (true)
        {
            ui.stampaMenu("MENU FRUITORE", MENU_FRUITORE);
            int choice = ui.acquisisciIntero("Scelta: ", 0, MENU_FRUITORE.length);
            ui.newLine();

            switch (choice)
            {
                case 1:
                    menuBachecaFruitore(ui, ps);
                    break;

                case 2:
                    menuIscrizione(ui, fruitore, ps, is);
                    break;

                case 3:
                    menuSpazioPersonale(ui, fruitore, fs);
                    break;

                case 0:
                    return;
            }
        }
    }

    // ===== BACHECA FRUITORE =====

    private static void menuBachecaFruitore(ConsoleUI ui, PropostaService ps)
    {
        ui.header("BACHECA");

        Map<String, List<Proposta>> bacheca = ps.getBachecaPerCategoria();

        if (bacheca.isEmpty())
        {
            ui.stampa("La bacheca è vuota.");
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
                ui.stampa("  [Proposta #" + (i + 1) + "]" +
                        "  Pubblicata il: " + p.getDataPubblicazione() +
                        " | Iscritti: " + p.getNumeroIscritti() +
                        " | Termine: " + p.getTermineIscrizione());

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

    // ===== ISCRIZIONE =====

    private static void menuIscrizione(
            ConsoleUI ui,
            Fruitore fruitore,
            PropostaService ps,
            IscrizioneService is)
    {
        ui.header("ISCRIZIONE A UNA PROPOSTA");

        List<Proposta> tutte = ps.getBacheca();

        if (tutte.isEmpty())
        {
            ui.stampa("Nessuna proposta aperta disponibile.");
            ui.newLine();
            ui.acquisisciStringa("Premi INVIO per continuare...");
            return;
        }

        ui.stampa("Proposte aperte disponibili:");
        ui.newLine();

        for (int i = 0; i < tutte.size(); i++)
        {
            Proposta p    = tutte.get(i);
            String titolo = p.getValoriCampi().getOrDefault("Titolo", "senza titolo");
            String cat    = p.getCategoria().getNome();
            ui.stampa("  " + (i + 1) + ") [" + cat + "] " + titolo +
                    " | Termine: " + p.getTermineIscrizione() +
                    " | Iscritti: " + p.getNumeroIscritti());
        }

        ui.newLine();
        int scelta = ui.acquisisciIntero(
                "Scegli proposta (0 per annullare): ", 0, tutte.size());

        if (scelta == 0)
            return;

        Proposta sceltaProposta = tutte.get(scelta - 1);

        try
        {
            is.iscrivi(fruitore, sceltaProposta);
            ui.stampa("Iscrizione completata!");
        }
        catch (IllegalStateException e)
        {
            ui.stampa("Errore: " + e.getMessage());
        }

        ui.newLine();
        ui.acquisisciStringa("Premi INVIO per continuare...");
    }

    // ===== SPAZIO PERSONALE =====

    private static void menuSpazioPersonale(
            ConsoleUI ui,
            Fruitore fruitore,
            FruitoreService fs)
    {
        while (true)
        {
            ui.header("SPAZIO PERSONALE - " + fruitore.getUsername());

            List<Notifica> notifiche = fs.getNotifiche(fruitore.getUsername());

            if (notifiche.isEmpty())
                ui.stampa("Nessuna notifica.");
            else
            {
                ui.stampaSezione("Le tue notifiche");
                for (int i = 0; i < notifiche.size(); i++)
                    ui.stampa("  " + (i + 1) + ") " + notifiche.get(i));
            }

            ui.newLine();
            ui.stampaMenu("", MENU_NOTIFICHE);
            int choice = ui.acquisisciIntero("Scelta: ", 0, MENU_NOTIFICHE.length);
            ui.newLine();

            switch (choice)
            {
                case 1:
                    if (notifiche.isEmpty())
                    {
                        ui.stampa("Nessuna notifica da eliminare.");
                        break;
                    }

                    int idx = ui.acquisisciIntero(
                            "Numero notifica da eliminare: ", 1, notifiche.size());
                    boolean ok = fs.eliminaNotifica(fruitore.getUsername(), idx - 1);
                    ui.stampa(ok ? "Notifica eliminata." : "Errore nell'eliminazione.");
                    break;

                case 0:
                    return;
            }

            ui.newLine();
            ui.acquisisciStringa("Premi INVIO per continuare...");
        }
    }

    // ===== CONFIGURATORE MENUS (unchanged from V2) =====

    private static void mainMenuConfiguratore(ConsoleUI ui, CategoriaService cs, PropostaService ps)
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
                    menuBachecaConfiguratore(ui, ps);
                    break;
                case 0:
                    return;
            }
        }
    }

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

    private static void menuCampiComuni(ConsoleUI ui, CategoriaService cs)
    {
        while (true)
        {
            ui.header("CAMPI COMUNI");
            ui.stampaSezione("Campi comuni attuali");
            ui.stampaCampi(cs.getCampiComuni());
            ui.stampaMenu("", MENU_CAMPI_COMUNI);

            int choice = ui.acquisisciIntero("Scelta: ", 0, 3);
            ui.newLine();

            switch (choice)
            {
                case 1:
                    String nome = ui.acquisisciStringa("Nome campo: ").trim();
                    TipoDato td = ui.acquisisciTipoDato("Tipo del campo \"" + nome + "\":");
                    boolean obbl = ui.acquisisciSiNo("Obbligatorio?");
                    try
                    {
                        cs.addCampoComune(nome, td, obbl);
                        ui.stampa("Campo comune aggiunto.");
                    }
                    catch (Exception e)
                    {
                        ui.stampa("Errore: " + e.getMessage());
                    }
                    break;

                case 2:
                    String nome2 = ui.acquisisciStringa("Nome campo da rimuovere: ");
                    ui.stampa(cs.removeCampoComune(nome2) ? "Rimosso." : "Campo non trovato.");
                    break;

                case 3:
                    String nome3  = ui.acquisisciStringa("Nome campo: ");
                    boolean obbl3 = ui.acquisisciSiNo("Impostare come obbligatorio?");
                    ui.stampa(cs.setObbligatorietaCampoComune(nome3, obbl3) ?
                            "Aggiornato." : "Campo non trovato.");
                    break;

                case 0:
                    return;
            }

            ui.newLine();
            ui.acquisisciStringa("Premi INVIO per continuare...");
        }
    }

    private static void menuCategorie(ConsoleUI ui, CategoriaService cs)
    {
        while (true)
        {
            ui.header("CATEGORIE");
            ui.stampaSezione("Categorie attuali");
            ui.stampaCategorie(cs.getCategorie());
            ui.stampaMenu("CATEGORIE", MENU_CATEGORIE);

            int choice = ui.acquisisciIntero("Scelta: ", 0, 3);
            ui.newLine();

            switch (choice)
            {
                case 1:
                    String nome = ui.acquisisciStringa("Nome nuova categoria: ");
                    try
                    {
                        cs.createCategoria(nome);
                        ui.stampa("Categoria creata.");
                    }
                    catch (Exception e)
                    {
                        ui.stampa("Errore: " + e.getMessage());
                    }
                    break;

                case 2:
                    String nome2 = ui.acquisisciStringa("Nome categoria da rimuovere: ");
                    ui.stampa(cs.removeCategoria(nome2) ? "Rimossa." : "Categoria non trovata.");
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

    private static void menuCampiSpecifici(ConsoleUI ui, CategoriaService cs, String nomeCategoria)
    {
        while (true)
        {
            Categoria cat = cs.getCategoria(nomeCategoria);
            ui.header("CAMPI SPECIFICI - " + nomeCategoria);
            ui.stampaSezione("Campi SPECIFICI");
            ui.stampaCampi(cat.getCampiSpecifici());
            ui.stampaMenu("CAMPI SPECIFICI", MENU_CAMPI_SPECIFICI);

            int choice = ui.acquisisciIntero("Scelta: ", 0, 3);
            ui.newLine();

            switch (choice)
            {
                case 1:
                    String nome  = ui.acquisisciStringa("Nome campo specifico: ").trim();
                    TipoDato td  = ui.acquisisciTipoDato("Tipo del campo \"" + nome + "\":");
                    boolean obbl = ui.acquisisciSiNo("Obbligatorio?");
                    try
                    {
                        cs.addCampoSpecifico(nomeCategoria, nome, td, obbl);
                        ui.stampa("Campo specifico aggiunto.");
                    }
                    catch (Exception e)
                    {
                        ui.stampa("Errore: " + e.getMessage());
                    }
                    break;

                case 2:
                    String nome2 = ui.acquisisciStringa("Nome campo specifico da rimuovere: ");
                    ui.stampa(cs.removeCampoSpecifico(nomeCategoria, nome2) ?
                            "Rimosso." : "Campo non trovato.");
                    break;

                case 3:
                    String nome3  = ui.acquisisciStringa("Nome campo specifico: ");
                    boolean obbl3 = ui.acquisisciSiNo("Impostare come obbligatorio?");
                    ui.stampa(cs.setObbligatorietaCampoSpecifico(nomeCategoria, nome3, obbl3) ?
                            "Aggiornato." : "Campo non trovato.");
                    break;

                case 0:
                    return;
            }

            ui.newLine();
            ui.acquisisciStringa("Premi INVIO per continuare...");
        }
    }

    private static void menuVisualizza(ConsoleUI ui, CategoriaService cs)
    {
        ui.header("VISUALIZZAZIONE");
        ui.stampaSezione("Campi BASE");
        ui.stampaCampi(cs.getCampiBase());
        ui.stampaSezione("Campi COMUNI");
        ui.stampaCampi(cs.getCampiComuni());
        ui.stampaSezione("Categorie");
        ui.stampaCategorie(cs.getCategorie());
        ui.newLine();
        ui.acquisisciStringa("Premi INVIO per continuare...");
    }

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

    private static void menuBachecaConfiguratore(ConsoleUI ui, PropostaService ps)
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
                ui.stampa("  [Proposta #" + (i + 1) + "]  Pubblicata il: " +
                        p.getDataPubblicazione());

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