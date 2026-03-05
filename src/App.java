//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import it.unibs.ingsoft.v1.ui.ConsoleUI;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public final class App {
    public void init() {
        Path storage = Path.of("data", "appdata.ser");
        DatabaseService db = new DatabaseService(storage);
        AppData data = db.loadOrCreate();
        AuthenticationService auth = new AuthenticationService(db, data);
        CategoriaService catService = new CategoriaService(db, data);
        Throwable var6 = null;
        Object var7 = null;

        try {
            Scanner sc = new Scanner(System.in);

            try {
                ConsoleUI ui = new ConsoleUI(sc);
                ui.header("Iniziative - Versione 1 (solo configuratore)");
                Configuratore logged = doLoginFlow(ui, auth);
                ui.stampa("Benvenuto, " + logged.getUsername() + "!");
                ui.newLine();
                mainMenu(ui, catService);
                ui.stampa("Arrivederci!");
            } finally {
                if (sc != null) {
                    sc.close();
                }

            }

        } catch (Throwable var16) {
            if (var6 == null) {
                var6 = var16;
            } else if (var6 != var16) {
                var6.addSuppressed(var16);
            }

            throw var16;
        }
    }

    private static Configuratore doLoginFlow(ConsoleUI ui, AuthenticationService auth) {
        while(true) {
            ui.stampa("LOGIN CONFIGURATORE");
            String u = ui.acquisisciStringa("Username: ").trim();
            String p = ui.acquisisciStringa("Password: ").trim();
            Configuratore opt = auth.login(u, p);
            if (opt != null) {
                ui.stampa("Login riuscito credenziali valide");
                if (!"config".equals(opt.getUsername())) {
                    ui.newLine();
                    return opt;
                }

                ui.newLine();
                ui.stampa("Primo accesso con credenziali predefinite.");
                ui.stampa("Devi scegliere credenziali personali per poter operare.");

                while(true) {
                    String newU = ui.acquisisciStringa("Nuovo username: ").trim();
                    String newP = ui.acquisisciStringa("Nuova password: ").trim();

                    try {
                        Configuratore registered = auth.registraNuovoConfiguratore(newU, newP);
                        ui.stampa("Registrazione completata.");
                        ui.newLine();
                        return registered;
                    } catch (IllegalArgumentException e) {
                        ui.stampa("Errore: " + e.getMessage());
                    }
                }
            }

            ui.stampa("Credenziali non valide.");
            ui.newLine();
        }
    }

    private static void mainMenu(ConsoleUI ui, CategoriaService cs) {
        while(true) {
            ui.header("MENU PRINCIPALE");
            ui.stampa("1) Fissare campi BASE (solo se non ancora fissati)");
            ui.stampa("2) Gestire campi COMUNI");
            ui.stampa("3) Gestire CATEGORIE e campi SPECIFICI");
            ui.stampa("4) Visualizzare categorie e campi");
            ui.stampa("0) Esci");
            ui.newLine();
            int choice = ui.acquisisciIntero("Scelta: ", 0, 4);
            ui.newLine();
            switch (choice) {
                case 0:
                    return;
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
            }
        }
    }

    private static void menuCampiBase(ConsoleUI ui, CategoriaService cs) {
        ui.header("CAMPI BASE");
        if (!cs.getCampiBase().isEmpty()) {
            ui.stampa("Stato: FISSATI (immutabili)");
            ui.stampa("Elenco campi base:");
            cs.getCampiBase().forEach((c) -> ui.stampa(" - " + String.valueOf(c)));
            ui.newLine();
        } else {
            try {
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
    }

    private static void menuCampiComuni(ConsoleUI ui, CategoriaService cs) {
        while(true) {
            ui.header("CAMPI COMUNI");
            ui.stampa("Campi comuni attuali:");
            if (cs.getCampiComuni().isEmpty()) {
                ui.stampa(" (nessuno)");
            }

            cs.getCampiComuni().forEach((c) -> ui.stampa(" - " + String.valueOf(c)));
            ui.newLine();
            ui.stampa("1) Aggiungi campo comune");
            ui.stampa("2) Rimuovi campo comune");
            ui.stampa("3) Cambia obbligatorietà campo comune");
            ui.stampa("0) Indietro");
            ui.newLine();
            int choice = ui.acquisisciIntero("Scelta: ", 0, 3);
            ui.newLine();
            switch (choice) {
                case 0:
                    return;
                case 1:
                    String nome1 = ui.acquisisciStringa("Nome campo: ");
                    boolean obbl1 = ui.acquisisciSiNo("Obbligatorio?");

                    try {
                        cs.addCampoComune(nome1, obbl1);
                        ui.stampa("Campo comune aggiunto.");
                    } catch (IllegalArgumentException e) {
                        ui.stampa("Errore: " + e.getMessage());
                    }

                    ui.newLine();
                    ui.acquisisciStringa("Premi INVIO per continuare...");
                    break;
                case 2:
                    String nome2 = ui.acquisisciStringa("Nome campo da rimuovere: ");
                    boolean ok2 = cs.removeCampoComune(nome2);
                    ui.stampa(ok2 ? "Rimosso." : "Campo non trovato.");
                    ui.newLine();
                    ui.acquisisciStringa("Premi INVIO per continuare...");
                    break;
                case 3:
                    String nome3 = ui.acquisisciStringa("Nome campo: ");
                    boolean obbl3 = ui.acquisisciSiNo("Impostare come obbligatorio?");
                    boolean ok3 = cs.setObbligatorietaCampoComune(nome3, obbl3);
                    ui.stampa(ok3 ? "Aggiornato." : "Campo non trovato.");
                    ui.newLine();
                    ui.acquisisciStringa("Premi INVIO per continuare...");
            }
        }
    }

    private static void menuCategorie(ConsoleUI ui, CategoriaService cs) {
        while(true) {
            ui.header("CATEGORIE");
            ui.stampa("Categorie attuali:");
            if (cs.getCategorie().isEmpty()) {
                ui.stampa(" (nessuna)");
            }

            cs.getCategorie().forEach((c) -> ui.stampa(" - " + String.valueOf(c)));
            ui.newLine();
            ui.stampa("1) Crea categoria");
            ui.stampa("2) Rimuovi categoria");
            ui.stampa("3) Gestisci campi specifici di una categoria");
            ui.stampa("0) Indietro");
            ui.newLine();
            int choice = ui.acquisisciIntero("Scelta: ", 0, 3);
            ui.newLine();
            switch (choice) {
                case 0:
                    return;
                case 1:
                    String nome1 = ui.acquisisciStringa("Nome nuova categoria: ");

                    try {
                        cs.createCategoria(nome1);
                        ui.stampa("Categoria creata.");
                    } catch (IllegalArgumentException e) {
                        ui.stampa("Errore: " + e.getMessage());
                    }

                    ui.newLine();
                    ui.acquisisciStringa("Premi INVIO per continuare...");
                    break;
                case 2:
                    String nome2 = ui.acquisisciStringa("Nome categoria da rimuovere: ");
                    boolean ok2 = cs.removeCategoria(nome2);
                    ui.stampa(ok2 ? "Rimossa." : "Categoria non trovata.");
                    ui.newLine();
                    ui.acquisisciStringa("Premi INVIO per continuare...");
                    break;
                case 3:
                    if (cs.getCategorie().isEmpty()) {
                        ui.stampa("Nessuna categoria presente.");
                        ui.newLine();
                        ui.acquisisciStringa("Premi INVIO per continuare...");
                    } else {
                        String nomeCat = ui.acquisisciStringa("Nome categoria: ");

                        try {
                            cs.getCategoriaOrThrow(nomeCat);
                            menuCampiSpecifici(ui, cs, nomeCat);
                        } catch (IllegalArgumentException e) {
                            ui.stampa("Errore: " + e.getMessage());
                            ui.newLine();
                            ui.acquisisciStringa("Premi INVIO per continuare...");
                        }
                    }
            }
        }
    }

    private static void menuCampiSpecifici(ConsoleUI ui, CategoriaService cs, String nomeCategoria) {
        while(true) {
            ui.header("CAMPI SPECIFICI - " + nomeCategoria);
            ui.stampa(cs.renderSchemaPerCategoria(nomeCategoria));
            ui.newLine();
            ui.stampa("1) Aggiungi campo specifico");
            ui.stampa("2) Rimuovi campo specifico");
            ui.stampa("3) Cambia obbligatorietà campo specifico");
            ui.stampa("0) Indietro");
            ui.newLine();
            int choice = ui.acquisisciIntero("Scelta: ", 0, 3);
            ui.newLine();
            switch (choice) {
                case 0:
                    return;
                case 1:
                    String nomeCampo1 = ui.acquisisciStringa("Nome campo specifico: ");
                    boolean obbl1 = ui.acquisisciSiNo("Obbligatorio?");

                    try {
                        cs.addCampoSpecifico(nomeCategoria, nomeCampo1, obbl1);
                        ui.stampa("Campo specifico aggiunto.");
                    } catch (IllegalArgumentException e) {
                        ui.stampa("Errore: " + e.getMessage());
                    }

                    ui.newLine();
                    ui.acquisisciStringa("Premi INVIO per continuare...");
                    break;
                case 2:
                    String nomeCampo2 = ui.acquisisciStringa("Nome campo specifico da rimuovere: ");
                    boolean ok2 = cs.removeCampoSpecifico(nomeCategoria, nomeCampo2);
                    ui.stampa(ok2 ? "Rimosso." : "Campo non trovato.");
                    ui.newLine();
                    ui.acquisisciStringa("Premi INVIO per continuare...");
                    break;
                case 3:
                    String nomeCampo3 = ui.acquisisciStringa("Nome campo specifico: ");
                    boolean obbl3 = ui.acquisisciSiNo("Impostare come obbligatorio?");
                    boolean ok3 = cs.setObbligatorietaCampoSpecifico(nomeCategoria, nomeCampo3, obbl3);
                    ui.stampa(ok3 ? "Aggiornato." : "Campo non trovato.");
                    ui.newLine();
                    ui.acquisisciStringa("Premi INVIO per continuare...");
            }
        }
    }

    private static void menuVisualizza(ConsoleUI ui, CategoriaService cs) {
        ui.header("VISUALIZZAZIONE");
        ui.stampa("Campi BASE:");
        if (cs.getCampiBase().isEmpty()) {
            ui.stampa(" (non fissati)");
        }

        cs.getCampiBase().forEach((c) -> ui.stampa(" - " + String.valueOf(c)));
        ui.newLine();
        ui.stampa("Campi COMUNI:");
        if (cs.getCampiComuni().isEmpty()) {
            ui.stampa(" (nessuno)");
        }

        cs.getCampiComuni().forEach((c) -> ui.stampa(" - " + String.valueOf(c)));
        ui.newLine();
        ui.stampa("Categorie:");
        if (cs.getCategorie().isEmpty()) {
            ui.stampa(" (nessuna)");
        }

        for(Categoria cat : cs.getCategorie()) {
            ui.newLine();
            ui.stampa(cs.renderSchemaPerCategoria(cat.getNome()));
        }

        ui.newLine();
        ui.acquisisciStringa("Premi INVIO per continuare...");
    }
}
