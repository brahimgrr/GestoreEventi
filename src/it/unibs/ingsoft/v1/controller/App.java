package it.unibs.ingsoft.v1.controller;

import it.unibs.ingsoft.v1.model.Categoria;
import it.unibs.ingsoft.v1.model.Configuratore;
import it.unibs.ingsoft.v1.persistence.AppData;
import it.unibs.ingsoft.v1.persistence.DatabaseService;
import it.unibs.ingsoft.v1.service.AuthenticationService;
import it.unibs.ingsoft.v1.service.CategoriaService;
import it.unibs.ingsoft.v1.ui.ConsoleUI;

import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public final class App
{
    public static final String [] VOCI_MENU_PRINCIPALE = {  "Fissare campi BASE (solo se non ancora fissati)",
                                                            "Gestire campi COMUNI",
                                                            "Gestire CATEGORIE e campi SPECIFICI",
                                                            "Visualizzare categorie e campi"};

    public static final String [] VOCI_MENU_CAMPI_BASE = {  "Fissare campi BASE (solo se non ancora fissati)",
                                                            "Gestire campi COMUNI",
                                                            "Gestire CATEGORIE e campi SPECIFICI",
                                                            "Visualizzare categorie e campi"};


    public static final String [] VOCI_MENU_CAMPI_COMUNI = {"Aggiungi campo comune",
                                                            "Rimuovi campo comune",
                                                            "Cambia obbligatorietà campo comune",};

    public static final String [] VOCI_MENU_CATEGORIE = {"Crea categoria",
                                                         "Rimuovi categoria",
                                                         "Gestisci campi specifici di una categoria"};

    public static final String[] VOCI_MENU_CAMPI_SPECIFICI = {  "Aggiungi campo specifico",
                                                                "Rimuovi campo specifico",
                                                                "Cambia obbligatorietà campo specifico"};

    public void init ()
    {
        Path storage = Path.of("data", "appdata.ser");

        DatabaseService db = new DatabaseService(storage);
        AppData data = db.loadOrCreate();

        AuthenticationService auth = new AuthenticationService(db, data);
        CategoriaService catService = new CategoriaService(db, data);

        try (Scanner sc = new Scanner(System.in))
        {
            ConsoleUI ui = new ConsoleUI(sc);

            ui.header("Iniziative - Versione 1 (solo configuratore)");

            while (true)
            {
                Configuratore logged = doLoginFlow(ui, auth);

                ui.stampa("Benvenuto, " + logged.getUsername() + "!");
                ui.newLine();

                if (catService.getCampiBase().isEmpty())
                {
                    ui.header("PRIMA CONFIGURAZIONE");

                    ui.stampa("Non sono ancora stati definiti i campi base.");
                    ui.stampa("Il primo configuratore deve inserirli.");

                    menuCampiBase(ui, catService);
                }

                mainMenu(ui, catService);

                ui.stampa("Logout effettuato.");
                ui.newLine();
            }
        }
    }

    private static Configuratore doLoginFlow(ConsoleUI ui, AuthenticationService auth)
    {
        while (true)
        {
            ui.stampa("LOGIN CONFIGURATORE");
            String u = ui.acquisisciStringa("Username: ").trim();
            String p = ui.acquisisciStringa("Password: ").trim();

            Configuratore opt = auth.login(u, p);

            if (opt != null)
                ui.stampa("Login riuscito credenziali valide");

            else
            {
                ui.stampa("Credenziali non valide.");
                ui.newLine();
                continue;
            }

            //LOGIN PREDEFINITO QUINDI CHIEDO CREDENZIALI PERSONALIZZATE
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

                    } catch (IllegalArgumentException e)
                    {
                        ui.stampa("Errore: " + e.getMessage());
                    }
                }
            }

            ui.newLine();
            return opt;
        }
    }

    private static void mainMenu(ConsoleUI ui, CategoriaService cs)
    {
        while (true)
        {
            ui.stampaMenu("MENU PRINCIPALE", VOCI_MENU_PRINCIPALE);

            int choice = ui.acquisisciIntero("Scelta: ", 0, 4);
            ui.newLine();

            switch (choice)
            {
                case 1:
                    menuCampiBase(ui, cs);
                    break;

                case 2:
                    menuCampiComuni(ui, cs);
                    break;

                case 3:
                    menuCategorie(ui, cs);
                    break;

                case 4:
                    menuVisualizza(ui, cs);
                    break;

                case 0:
                    return;
            }
        }
    }

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

        } catch (IllegalStateException e) {
            ui.stampa("Operazione non consentita: " + e.getMessage());

        } catch (IllegalArgumentException e) {
            ui.stampa("Errore: " + e.getMessage());
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

            ui.stampaMenu("", VOCI_MENU_CAMPI_COMUNI);

            int choice = ui.acquisisciIntero("Scelta: ", 0, 3);
            ui.newLine();

            switch (choice)
            {
                case 1:
                    String nome1 = ui.acquisisciStringa("Nome campo: ");
                    boolean obbl1 = ui.acquisisciSiNo("Obbligatorio?");

                    try {
                        cs.addCampoComune(nome1, obbl1);
                        ui.stampa("it.unibs.ingsoft.v1.model.Campo comune aggiunto.");

                    } catch (IllegalArgumentException e) {
                        ui.stampa("Errore: " + e.getMessage());
                    }

                    ui.newLine();
                    ui.acquisisciStringa("Premi INVIO per continuare...");
                    break;

                case 2:
                    String nome2 = ui.acquisisciStringa("Nome campo da rimuovere: ");
                    boolean ok2 = cs.removeCampoComune(nome2);
                    ui.stampa(ok2 ? "Rimosso." : "it.unibs.ingsoft.v1.model.Campo non trovato.");
                    ui.newLine();
                    ui.acquisisciStringa("Premi INVIO per continuare...");
                    break;

                case 3 :
                    String nome3 = ui.acquisisciStringa("Nome campo: ");
                    boolean obbl3 = ui.acquisisciSiNo("Impostare come obbligatorio?");
                    boolean ok3 = cs.setObbligatorietaCampoComune(nome3, obbl3);
                    ui.stampa(ok3 ? "Aggiornato." : "it.unibs.ingsoft.v1.model.Campo non trovato.");
                    ui.newLine();
                    ui.acquisisciStringa("Premi INVIO per continuare...");
                    break;

                case 0 :
                    return;
            }
        }
    }

    private static void menuCategorie(ConsoleUI ui, CategoriaService cs)
    {
        while (true)
        {
            ui.header("CATEGORIE");

            ui.stampaSezione("Categorie attuali");
            ui.stampaCategorie(cs.getCategorie());

            ui.stampaMenu("CATEGORIE", VOCI_MENU_CATEGORIE);

            int choice = ui.acquisisciIntero("Scelta: ", 0, 3);
            ui.newLine();

            switch (choice)
            {
                case 1 :
                    String nome1 = ui.acquisisciStringa("Nome nuova categoria: ");

                    try
                    {
                        cs.createCategoria(nome1);
                        ui.stampa("it.unibs.ingsoft.v1.model.Categoria creata.");

                    } catch (IllegalArgumentException e) {
                        ui.stampa("Errore: " + e.getMessage());
                    }

                    ui.newLine();
                    ui.acquisisciStringa("Premi INVIO per continuare...");
                    break;

                case 2 :
                    String nome2 = ui.acquisisciStringa("Nome categoria da rimuovere: ");
                    boolean ok2 = cs.removeCategoria(nome2);
                    ui.stampa(ok2 ? "Rimossa." : "it.unibs.ingsoft.v1.model.Categoria non trovata.");
                    ui.newLine();
                    ui.acquisisciStringa("Premi INVIO per continuare...");
                    break;

                case 3 :
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
                        cs.getCategoriaOrThrow(nomeCat); // validate
                        menuCampiSpecifici(ui, cs, nomeCat);

                    } catch (IllegalArgumentException e) {
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

            ui.stampaMenu("CAMPI SPECIFICI", VOCI_MENU_CAMPI_SPECIFICI);
            ui.newLine();

            int choice = ui.acquisisciIntero("Scelta: ", 0, 3);
            ui.newLine();

            switch (choice)
            {
                case 1 :
                    String nomeCampo1 = ui.acquisisciStringa("Nome campo specifico: ");
                    boolean obbl1 = ui.acquisisciSiNo("Obbligatorio?");
                    try {
                        cs.addCampoSpecifico(nomeCategoria, nomeCampo1, obbl1);
                        ui.stampa("it.unibs.ingsoft.v1.model.Campo specifico aggiunto.");

                    } catch (IllegalArgumentException e) {
                        ui.stampa("Errore: " + e.getMessage());
                    }

                    ui.newLine();
                    ui.acquisisciStringa("Premi INVIO per continuare...");
                    break;

                case 2:
                    String nomeCampo2 = ui.acquisisciStringa("Nome campo specifico da rimuovere: ");
                    boolean ok2 = cs.removeCampoSpecifico(nomeCategoria, nomeCampo2);
                    ui.stampa(ok2 ? "Rimosso." : "it.unibs.ingsoft.v1.model.Campo non trovato.");
                    ui.newLine();
                    ui.acquisisciStringa("Premi INVIO per continuare...");
                    break;

                case 3:
                    String nomeCampo3 = ui.acquisisciStringa("Nome campo specifico: ");
                    boolean obbl3 = ui.acquisisciSiNo("Impostare come obbligatorio?");
                    boolean ok3 = cs.setObbligatorietaCampoSpecifico(nomeCategoria, nomeCampo3, obbl3);
                    ui.stampa(ok3 ? "Aggiornato." : "it.unibs.ingsoft.v1.model.Campo non trovato.");
                    ui.newLine();
                    ui.acquisisciStringa("Premi INVIO per continuare...");
                    break;

                case 0:
                    return;

            }
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
}
