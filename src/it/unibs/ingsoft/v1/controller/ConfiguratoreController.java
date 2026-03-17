package it.unibs.ingsoft.v1.controller;

import it.unibs.ingsoft.v1.model.Categoria;
import it.unibs.ingsoft.v1.service.CategoriaService;
import it.unibs.ingsoft.v1.view.IAppView;

import java.util.List;

/**
 * Handles all configuratore menu interactions.
 * Delegates business operations to CategoriaService; UI interactions to IAppView.
 */
public final class ConfiguratoreController
{
    private static final String[] MENU_PRINCIPALE =
            {
                    "Gestire campi COMUNI",
                    "Gestire CATEGORIE e campi SPECIFICI",
                    "Visualizzare categorie e campi"
            };

    private static final String[] MENU_CAMPI_COMUNI =
            {
                    "Aggiungi campo comune",
                    "Rimuovi campo comune",
                    "Cambia obbligatorietà campo comune"
            };

    private static final String[] MENU_CATEGORIE =
            {
                    "Crea categoria",
                    "Rimuovi categoria",
                    "Gestisci campi specifici di una categoria"
            };

    private static final String[] MENU_CAMPI_SPECIFICI =
            {
                    "Aggiungi campo specifico",
                    "Rimuovi campo specifico",
                    "Cambia obbligatorietà campo specifico"
            };

    private final IAppView ui;
    private final CategoriaService cs;

    public ConfiguratoreController(IAppView ui, CategoriaService cs)
    {
        this.ui = ui;
        this.cs = cs;
    }

    /**
     * Runs the configuratore session: handles first-time base-field setup,
     * then the main menu loop until the user logs out.
     */
    public void run()
    {
        if (cs.getCampiBase().isEmpty())
        {
            ui.header("PRIMA CONFIGURAZIONE");
            ui.stampa("Non sono ancora stati definiti i campi base.");
            ui.stampa("Il primo configuratore deve inserirli.");
            menuCampiBase();
        }

        mainMenu();
    }

    // ---------------------------------------------------------------
    // MAIN MENU
    // ---------------------------------------------------------------

    private void mainMenu()
    {
        while (true)
        {
            ui.stampaMenu("MENU PRINCIPALE", MENU_PRINCIPALE);
            int choice = ui.acquisisciIntero("Scelta: ", 0, MENU_PRINCIPALE.length);
            ui.newLine();

            switch (choice)
            {
                case 1: menuCampiComuni();  break;
                case 2: menuCategorie();    break;
                case 3: menuVisualizza();   break;
                case 0: return;
            }
        }
    }

    // ---------------------------------------------------------------
    // CAMPI BASE (first-time only)
    // ---------------------------------------------------------------

    private void menuCampiBase()
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
        ui.pausa();
    }

    // ---------------------------------------------------------------
    // CAMPI COMUNI
    // ---------------------------------------------------------------

    private void menuCampiComuni()
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
                    String nome1 = ui.acquisisciStringa("Nome campo: ");
                    boolean obbl1 = ui.acquisisciSiNo("Obbligatorio?");
                    try
                    {
                        cs.addCampoComune(nome1, obbl1);
                        ui.stampa("Campo comune aggiunto.");
                    }
                    catch (IllegalArgumentException e)
                    {
                        ui.stampa("Errore: " + e.getMessage());
                    }
                    ui.newLine();
                    ui.pausa();
                    break;

                case 2:
                    String nome2 = ui.acquisisciStringa("Nome campo da rimuovere: ");
                    ui.stampa(cs.removeCampoComune(nome2) ? "Rimosso." : "Campo non trovato.");
                    ui.newLine();
                    ui.pausa();
                    break;

                case 3:
                    String nome3  = ui.acquisisciStringa("Nome campo: ");
                    boolean obbl3 = ui.acquisisciSiNo("Impostare come obbligatorio?");
                    ui.stampa(cs.setObbligatorietaCampoComune(nome3, obbl3) ?
                            "Aggiornato." : "Campo non trovato.");
                    ui.newLine();
                    ui.pausa();
                    break;

                case 0:
                    return;
            }
        }
    }

    // ---------------------------------------------------------------
    // CATEGORIE
    // ---------------------------------------------------------------

    private void menuCategorie()
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
                    String nome1 = ui.acquisisciStringa("Nome nuova categoria: ");
                    try
                    {
                        cs.createCategoria(nome1);
                        ui.stampa("Categoria creata.");
                    }
                    catch (IllegalArgumentException e)
                    {
                        ui.stampa("Errore: " + e.getMessage());
                    }
                    ui.newLine();
                    ui.pausa();
                    break;

                case 2:
                    String nome2 = ui.acquisisciStringa("Nome categoria da rimuovere: ");
                    ui.stampa(cs.removeCategoria(nome2) ? "Rimossa." : "Categoria non trovata.");
                    ui.newLine();
                    ui.pausa();
                    break;

                case 3:
                    if (cs.getCategorie().isEmpty())
                    {
                        ui.stampa("Nessuna categoria presente.");
                        ui.newLine();
                        ui.pausa();
                        continue;
                    }
                    String nomeCat = ui.acquisisciStringa("Nome categoria: ");
                    try
                    {
                        cs.getCategoriaOrThrow(nomeCat);
                        menuCampiSpecifici(nomeCat);
                    }
                    catch (IllegalArgumentException e)
                    {
                        ui.stampa("Errore: " + e.getMessage());
                        ui.newLine();
                        ui.pausa();
                    }
                    break;

                case 0:
                    return;
            }
        }
    }

    // ---------------------------------------------------------------
    // CAMPI SPECIFICI
    // ---------------------------------------------------------------

    private void menuCampiSpecifici(String nomeCategoria)
    {
        while (true)
        {
            Categoria cat = cs.getCategoriaOrThrow(nomeCategoria);
            ui.header("CAMPI SPECIFICI - " + nomeCategoria);
            ui.stampaSezione("Campi BASE");
            ui.stampaCampi(cs.getCampiBase());
            ui.stampaSezione("Campi COMUNI");
            ui.stampaCampi(cs.getCampiComuni());
            ui.stampaSezione("Campi SPECIFICI");
            ui.stampaCampi(cat.getCampiSpecifici());
            ui.newLine();
            ui.stampaMenu("CAMPI SPECIFICI", MENU_CAMPI_SPECIFICI);
            ui.newLine();

            int choice = ui.acquisisciIntero("Scelta: ", 0, 3);
            ui.newLine();

            switch (choice)
            {
                case 1:
                    String nome1  = ui.acquisisciStringa("Nome campo specifico: ");
                    boolean obbl1 = ui.acquisisciSiNo("Obbligatorio?");
                    try
                    {
                        cs.addCampoSpecifico(nomeCategoria, nome1, obbl1);
                        ui.stampa("Campo specifico aggiunto.");
                    }
                    catch (IllegalArgumentException e)
                    {
                        ui.stampa("Errore: " + e.getMessage());
                    }
                    ui.newLine();
                    ui.pausa();
                    break;

                case 2:
                    String nome2 = ui.acquisisciStringa("Nome campo specifico da rimuovere: ");
                    ui.stampa(cs.removeCampoSpecifico(nomeCategoria, nome2) ?
                            "Rimosso." : "Campo non trovato.");
                    ui.newLine();
                    ui.pausa();
                    break;

                case 3:
                    String nome3  = ui.acquisisciStringa("Nome campo specifico: ");
                    boolean obbl3 = ui.acquisisciSiNo("Impostare come obbligatorio?");
                    ui.stampa(cs.setObbligatorietaCampoSpecifico(nomeCategoria, nome3, obbl3) ?
                            "Aggiornato." : "Campo non trovato.");
                    ui.newLine();
                    ui.pausa();
                    break;

                case 0:
                    return;
            }
        }
    }

    // ---------------------------------------------------------------
    // VISUALIZZA
    // ---------------------------------------------------------------

    private void menuVisualizza()
    {
        ui.header("VISUALIZZAZIONE");
        ui.stampaSezione("Campi BASE");
        ui.stampaCampi(cs.getCampiBase());
        ui.stampaSezione("Campi COMUNI");
        ui.stampaCampi(cs.getCampiComuni());
        ui.stampaSezione("Categorie");
        ui.stampaCategorie(cs.getCategorie());
        ui.newLine();
        ui.pausa();
    }
}
