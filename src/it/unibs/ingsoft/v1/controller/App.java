package it.unibs.ingsoft.v1.controller;

import it.unibs.ingsoft.v1.model.Categoria;
import it.unibs.ingsoft.v1.model.Configuratore;
import it.unibs.ingsoft.v1.persistence.AppData;
import it.unibs.ingsoft.v1.persistence.DatabaseService;
import it.unibs.ingsoft.v1.service.AuthenticationService;
import it.unibs.ingsoft.v1.service.CategoriaService;
import it.unibs.ingsoft.v1.view.ConsoleUI;

import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public final class App
{
    public static final String[] MENU_PRINCIPALE =
            {
                    "Gestire campi COMUNI",
                    "Gestire CATEGORIE e campi SPECIFICI",
                    "Visualizzare categorie e campi"
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
        Path storage = Path.of("data", "appdata.ser");

        DatabaseService       db         = new DatabaseService(storage);
        AppData               data       = db.loadOrCreate();
        AuthenticationService auth       = new AuthenticationService(db, data);
        CategoriaService      catService = new CategoriaService(db, data);

        try (Scanner sc = new Scanner(System.in))
        {
            ConsoleUI ui = new ConsoleUI(sc);

            ui.header("Iniziative - Versione 1 (solo configuratore)");

            do {
                Configuratore logged = doLoginFlow(ui, auth);

                ui.stampa("Benvenuto, " + logged.getUsername() + "!");
                ui.newLine();

                if (catService.getCampiBase().isEmpty()) {
                    ui.header("PRIMA CONFIGURAZIONE");
                    ui.stampa("Non sono ancora stati definiti i campi base.");
                    ui.stampa("Il primo configuratore deve inserirli.");
                    menuCampiBase(ui, catService);
                }

                mainMenu(ui, catService);

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

    // ===== MENU PRINCIPALE =====

    private static void mainMenu(ConsoleUI ui, CategoriaService cs)
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

                case 0:
                    return;
            }
        }
    }

    // ===== CAMPI BASE =====

    private static void menuCampiBase(ConsoleUI ui, CategoriaService cs)
    {
        ui.header("CAMPI BASE");

        if (!cs.getCampiBase().isEmpty())
        {
            ui.stampa("Stato: FISSATI (immutabili)");
            ui.stampaSezione("Elenco campi base");
            ui.stampaCampi(cs.getCampiBase());
            ui.newLine();
            return;
        }

        try
        {
            List<String> nomi = ui.acquisisciListaNomi("Inserisci i campi base (tutti obbligatori).");
            cs.fissareCampiBase(nomi);
            ui.stampa("Campi base fissati e salvati.");
        }
        catch (IllegalStateException e)
        {
            ui.stampa("Operazione non consentita: " + e.getMessage());
        }
        catch (IllegalArgumentException e)
        {
            ui.stampa("Errore: " + e.getMessage());
        }

        ui.newLine();
        ui.acquisisciStringa("Premi INVIO per continuare...");
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
                    String nome1  = ui.acquisisciStringa("Nome campo: ");
                    boolean obbl1 = ui.acquisisciSiNo("Obbligatorio?");

                    try
                    {
                        cs.aggiungiCampoComune(nome1, obbl1);
                        ui.stampa("Campo comune aggiunto.");
                    }
                    catch (IllegalArgumentException e)
                    {
                        ui.stampa("Errore: " + e.getMessage());
                    }

                    ui.newLine();
                    ui.acquisisciStringa("Premi INVIO per continuare...");
                    break;

                case 2:
                    String nome2 = ui.acquisisciStringa("Nome campo da rimuovere: ");
                    ui.stampa(cs.rimuoviCampoComune(nome2) ? "Rimosso." : "Campo non trovato.");
                    ui.newLine();
                    ui.acquisisciStringa("Premi INVIO per continuare...");
                    break;

                case 3:
                    String nome3  = ui.acquisisciStringa("Nome campo: ");
                    boolean obbl3 = ui.acquisisciSiNo("Impostare come obbligatorio?");
                    ui.stampa(cs.setObbligatorietaCampoComune(nome3, obbl3) ? "Aggiornato." : "Campo non trovato.");
                    ui.newLine();
                    ui.acquisisciStringa("Premi INVIO per continuare...");
                    break;

                case 0:
                    return;
            }
        }
    }

    // ===== CATEGORIE =====

    private static void menuCategorie(ConsoleUI ui, CategoriaService cs)
    {
        while (true)
        {
            ui.stampaMenu("CATEGORIE", MENU_CATEGORIE);

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
                    catch (IllegalArgumentException e)
                    {
                        ui.stampa("Errore: " + e.getMessage());
                    }

                    ui.newLine();
                    ui.acquisisciStringa("Premi INVIO per continuare...");
                    break;

                case 2:
                    String nomeRimuovi = ui.acquisisciStringa("Nome categoria da rimuovere: ");
                    ui.stampa(cs.rimuoviCategoria(nomeRimuovi) ? "Rimossa." : "Categoria non trovata.");
                    ui.newLine();
                    ui.acquisisciStringa("Premi INVIO per continuare...");
                    break;

                case 3:
                    if (cs.getCategorie().isEmpty())
                    {
                        ui.stampa("Nessuna categoria presente.");
                        ui.newLine();
                        ui.acquisisciStringa("Premi INVIO per continuare...");
                        continue;
                    }

                    String nomeCat = ui.acquisisciStringa("Nome categoria: ");

                    try
                    {
                        cs.getCategoriaOrThrow(nomeCat);
                        menuCampiSpecifici(ui, cs, nomeCat);
                    }
                    catch (IllegalArgumentException e)
                    {
                        ui.stampa("Errore: " + e.getMessage());
                        ui.newLine();
                        ui.acquisisciStringa("Premi INVIO per continuare...");
                    }
                    break;

                case 0:
                    return;
            }
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
                    String nomeCampo1 = ui.acquisisciStringa("Nome campo specifico: ");
                    boolean obbl1     = ui.acquisisciSiNo("Obbligatorio?");

                    try
                    {
                        cs.aggiungiCampoSpecifico(nomeCategoria, nomeCampo1, obbl1);
                        ui.stampa("Campo specifico aggiunto.");
                    }
                    catch (IllegalArgumentException e)
                    {
                        ui.stampa("Errore: " + e.getMessage());
                    }

                    ui.newLine();
                    ui.acquisisciStringa("Premi INVIO per continuare...");
                    break;

                case 2:
                    String nomeCampo2 = ui.acquisisciStringa("Nome campo specifico da rimuovere: ");
                    ui.stampa(cs.rimuoviCampoSpecifico(nomeCategoria, nomeCampo2) ? "Rimosso." : "Campo non trovato.");
                    ui.newLine();
                    ui.acquisisciStringa("Premi INVIO per continuare...");
                    break;

                case 3:
                    String nomeCampo3  = ui.acquisisciStringa("Nome campo: ");
                    boolean obbl3      = ui.acquisisciSiNo("Impostare come obbligatorio?");
                    ui.stampa(cs.setObbligatorietaCampoSpecifico(nomeCategoria, nomeCampo3, obbl3) ? "Aggiornato." : "Campo non trovato.");
                    ui.newLine();
                    ui.acquisisciStringa("Premi INVIO per continuare...");
                    break;

                case 0:
                    return;
            }
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
}