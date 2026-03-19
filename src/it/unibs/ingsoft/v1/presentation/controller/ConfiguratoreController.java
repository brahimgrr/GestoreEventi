package it.unibs.ingsoft.v1.presentation.controller;

import it.unibs.ingsoft.v1.domain.Categoria;
import it.unibs.ingsoft.v1.application.CatalogoService;
import it.unibs.ingsoft.v1.presentation.view.cli.ConsoleUI;
import it.unibs.ingsoft.v1.presentation.view.contract.IAppView;

import java.util.List;

/**
 * Handles all configuratore menu interactions.
 * Delegates all field and category operations to {@link CatalogoService}.
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

    private final IAppView       ui;
    private final CatalogoService cat;

    public ConfiguratoreController(IAppView ui, CatalogoService cat)
    {
        this.ui  = ui;
        this.cat = cat;
    }

    /**
     * Runs the configuratore session: enforces first-time base-field setup (cannot skip),
     * then the main menu loop until the user logs out.
     */
    public void run()
    {
        // BUG-001 fix: loop until base fields are actually provided
        while (cat.getCampiBase().isEmpty())
        {
            ui.header("PRIMA CONFIGURAZIONE");
            ui.stampaInfo("Non sono ancora stati definiti i campi base.");
            ui.stampaInfo("Il primo configuratore deve inserirli prima di procedere.");
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
            ui.stampaMenu("MENU PRINCIPALE", MENU_PRINCIPALE); // "0) Esci" — correct for top level
            int choice = ui.acquisisciIntero("Scelta: ", 0, MENU_PRINCIPALE.length);
            ui.newLine();

            switch (choice)
            {
                case 1: menuCampiComuni(); break;
                case 2: menuCategorie();   break;
                case 3: menuVisualizza();  break;
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

        if (!cat.getCampiBase().isEmpty())
        {
            ui.stampa("Stato: FISSATI (immutabili)");
            ui.stampaCampi(cat.getCampiBase());
            ui.newLine();
            return;
        }

        try
        {
            List<String> nomi = ui.acquisisciListaNomi("Inserisci i campi base (tutti obbligatori).");
            cat.fissareCampiBase(nomi);
            ui.stampaSuccesso("Campi base fissati e salvati.");
        }
        catch (ConsoleUI.CancelException e)
        {
            ui.stampaInfo("Operazione annullata. I campi base non sono stati salvati.");
        }
        catch (IllegalStateException e)
        {
            ui.stampaErrore("Operazione non consentita: " + e.getMessage());
        }
        catch (IllegalArgumentException e)
        {
            ui.stampaErrore(e.getMessage());
        }

        ui.newLine();
        ui.pausaConSpaziatura();
    }

    // ---------------------------------------------------------------
    // CAMPI COMUNI
    // ---------------------------------------------------------------

    private void menuCampiComuni()
    {
        while (true)
        {
            ui.header("CAMPI COMUNI");
            ui.stampaCampi(cat.getCampiComuni());
            ui.newLine();
            ui.stampaMenu("", MENU_CAMPI_COMUNI, "Torna");

            int choice = ui.acquisisciIntero("Scelta: ", 0, MENU_CAMPI_COMUNI.length);
            ui.newLine();

            switch (choice)
            {
                case 1:
                    try
                    {
                        ui.stampaInfo(ConsoleUI.HINT_ANNULLA);
                        String nome = ui.acquisisciStringaConValidazione(
                                "Nome campo comune: ",
                                n -> !n.isBlank() && !cat.nomeEsiste(n),
                                "Nome non valido o già esistente."
                        );
                        boolean obbl = ui.acquisisciSiNo("Obbligatorio?");
                        String flagStr = obbl ? "obbligatorio" : "facoltativo";
                        if (!ui.acquisisciSiNo("Aggiungere '" + nome + "' (" + flagStr + ")?"))
                        {
                            ui.stampaInfo("Operazione annullata.");
                            break;
                        }
                        cat.addCampoComune(nome, obbl);
                        ui.stampaSuccesso("Campo comune aggiunto.");
                    }
                    catch (ConsoleUI.CancelException e)
                    {
                        ui.stampaInfo("Operazione annullata.");
                    }
                    catch (IllegalArgumentException e)
                    {
                        ui.stampaErrore(e.getMessage());
                    }
                    ui.pausaConSpaziatura();
                    break;

                case 2:
                    ui.selezionaElemento("Seleziona campo comune da rimuovere:", cat.getCampiComuni())
                      .ifPresentOrElse(
                          c -> {
                              boolean rimosso = cat.removeCampoComune(c.getNome());
                              if (rimosso) ui.stampaSuccesso("Campo rimosso.");
                              else ui.stampaErrore("Campo non trovato.");
                          },
                          () -> ui.stampaInfo("Operazione annullata.")
                      );
                    ui.pausaConSpaziatura();
                    break;

                case 3:
                    ui.selezionaElementoConInfo(
                            "Seleziona campo comune:",
                            cat.getCampiComuni(),
                            c -> c.isObbligatorio() ? "obbligatorio" : "facoltativo"
                    ).ifPresentOrElse(c -> {
                        String stato = c.isObbligatorio() ? "obbligatorio" : "facoltativo";
                        ui.stampaInfo("'" + c.getNome() + "' è attualmente " + stato + ".");
                        boolean obbl = ui.acquisisciSiNo("Impostare come obbligatorio?");
                        if (obbl == c.isObbligatorio())
                        {
                            ui.stampaAvviso("Nessuna modifica: il campo è già " + stato + ".");
                            return;
                        }
                        boolean ok = cat.setObbligatorietaCampoComune(c.getNome(), obbl);
                        if (ok) ui.stampaSuccesso("Aggiornato.");
                        else    ui.stampaErrore("Campo non trovato.");
                    }, () -> ui.stampaInfo("Operazione annullata."));
                    ui.pausaConSpaziatura();
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
            ui.stampaCategorie(cat.getCategorie());
            ui.newLine();
            ui.stampaMenu("", MENU_CATEGORIE, "Torna");

            int choice = ui.acquisisciIntero("Scelta: ", 0, MENU_CATEGORIE.length);
            ui.newLine();

            switch (choice)
            {
                case 1:
                    try
                    {
                        ui.stampaInfo(ConsoleUI.HINT_ANNULLA);
                        String nome = ui.acquisisciStringaConValidazione(
                                "Nome nuova categoria: ",
                                n -> !n.isBlank(),
                                "Il nome non può essere vuoto."
                        );
                        cat.createCategoria(nome);
                        ui.stampaSuccesso("Categoria creata.");
                    }
                    catch (ConsoleUI.CancelException e)
                    {
                        ui.stampaInfo("Operazione annullata.");
                    }
                    catch (IllegalArgumentException e)
                    {
                        ui.stampaErrore(e.getMessage());
                    }
                    ui.pausaConSpaziatura();
                    break;

                case 2:
                    // FUNC-005: confirm before irreversible deletion
                    ui.selezionaElemento("Seleziona categoria da rimuovere:", cat.getCategorie())
                      .ifPresentOrElse(c -> {
                          if (!ui.acquisisciSiNo("Rimuovere '" + c.getNome() + "' e tutti i suoi campi specifici?"))
                          {
                              ui.stampaInfo("Operazione annullata.");
                              return;
                          }
                          boolean rimossa = cat.removeCategoria(c.getNome());
                          if (rimossa) ui.stampaSuccesso("Categoria rimossa.");
                          else         ui.stampaErrore("Categoria non trovata.");
                      }, () -> ui.stampaInfo("Operazione annullata."));
                    ui.pausaConSpaziatura();
                    break;

                case 3:
                    ui.selezionaElemento("Seleziona categoria:", cat.getCategorie())
                      .ifPresentOrElse(
                          c -> menuCampiSpecifici(c.getNome()),
                          () -> ui.stampaInfo("Operazione annullata.")
                      );
                    ui.pausaConSpaziatura();
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
            Categoria c = cat.getCategoriaOrThrow(nomeCategoria);
            ui.header("CAMPI SPECIFICI - " + nomeCategoria);
            ui.stampaSezione("Campi BASE");
            ui.stampaCampi(cat.getCampiBase());
            ui.stampaSezione("Campi COMUNI");
            ui.stampaCampi(cat.getCampiComuni());
            ui.stampaSezione("Campi SPECIFICI");
            ui.stampaCampi(c.getCampiSpecifici());
            ui.newLine();
            ui.stampaMenu("", MENU_CAMPI_SPECIFICI, "Torna");

            int choice = ui.acquisisciIntero("Scelta: ", 0, MENU_CAMPI_SPECIFICI.length);
            ui.newLine();

            switch (choice)
            {
                case 1:
                    try
                    {
                        ui.stampaInfo(ConsoleUI.HINT_ANNULLA);
                        String nome = ui.acquisisciStringaConValidazione(
                                "Nome campo specifico: ",
                                n -> !n.isBlank() && !cat.nomeEsiste(n),
                                "Nome non valido o già esistente."
                        );
                        boolean obbl = ui.acquisisciSiNo("Obbligatorio?");
                        String flagStr = obbl ? "obbligatorio" : "facoltativo";
                        if (!ui.acquisisciSiNo("Aggiungere '" + nome + "' (" + flagStr + ")?"))
                        {
                            ui.stampaInfo("Operazione annullata.");
                            break;
                        }
                        cat.addCampoSpecifico(nomeCategoria, nome, obbl);
                        ui.stampaSuccesso("Campo specifico aggiunto.");
                    }
                    catch (ConsoleUI.CancelException e)
                    {
                        ui.stampaInfo("Operazione annullata.");
                    }
                    catch (IllegalArgumentException e)
                    {
                        ui.stampaErrore(e.getMessage());
                    }
                    ui.pausaConSpaziatura();
                    break;

                case 2:
                    ui.selezionaElemento("Seleziona campo specifico da rimuovere:", c.getCampiSpecifici())
                      .ifPresentOrElse(
                          cs -> {
                              boolean rimosso = cat.removeCampoSpecifico(nomeCategoria, cs.getNome());
                              if (rimosso) ui.stampaSuccesso("Campo rimosso.");
                              else         ui.stampaErrore("Campo non trovato.");
                          },
                          () -> ui.stampaInfo("Operazione annullata.")
                      );
                    ui.pausaConSpaziatura();
                    break;

                case 3:
                    ui.selezionaElementoConInfo(
                            "Seleziona campo specifico:",
                            c.getCampiSpecifici(),
                            cs -> cs.isObbligatorio() ? "obbligatorio" : "facoltativo"
                    ).ifPresentOrElse(cs -> {
                        String stato = cs.isObbligatorio() ? "obbligatorio" : "facoltativo";
                        ui.stampaInfo("'" + cs.getNome() + "' è attualmente " + stato + ".");
                        boolean obbl = ui.acquisisciSiNo("Impostare come obbligatorio?");
                        if (obbl == cs.isObbligatorio())
                        {
                            ui.stampaAvviso("Nessuna modifica: il campo è già " + stato + ".");
                            return;
                        }
                        boolean ok = cat.setObbligatorietaCampoSpecifico(nomeCategoria, cs.getNome(), obbl);
                        if (ok) ui.stampaSuccesso("Aggiornato.");
                        else    ui.stampaErrore("Campo non trovato.");
                    }, () -> ui.stampaInfo("Operazione annullata."));
                    ui.pausaConSpaziatura();
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
        ui.stampaSezione("Campi condivisi da tutte le categorie");
        ui.stampaCampi(cat.getCampiCondivisi());
        ui.newLine();
        ui.stampaSezione("Categorie");
        ui.stampaCategorieDettaglio(cat.getCategorie());
        ui.newLine();
        ui.pausaConSpaziatura();
    }
}
