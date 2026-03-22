package it.unibs.ingsoft.v1.presentation.controller;

import it.unibs.ingsoft.v1.domain.*;
import it.unibs.ingsoft.v1.application.CatalogoService;
import it.unibs.ingsoft.v1.presentation.view.contract.IAppView;
import it.unibs.ingsoft.v1.presentation.view.contract.OperationCancelledException;

import java.util.List;

/**
 * Handles all configuratore menu interactions.
 * Delegates all field and category operations to {@link CatalogoService}.
 */
public final class ConfiguratoreController {
    private static final String[] MENU_PRINCIPALE = {
        "Gestire campi BASE",
        "Gestire campi COMUNI",
        "Gestire CATEGORIE e campi SPECIFICI"
    };

    private static final String[] MENU_CAMPI_COMUNI = {
        "Aggiungi campo comune",
        "Rimuovi campo comune",
        "Cambia obbligatorietà campo comune"
    };

    private static final String[] MENU_CATEGORIE = {
        "Crea categoria",
        "Rimuovi categoria",
        "Gestisci campi specifici di una categoria"
    };

    private static final String[] MENU_CAMPI_SPECIFICI = {
        "Aggiungi campo specifico",
        "Rimuovi campo specifico",
        "Cambia obbligatorietà campo specifico"
    };

    private final Configuratore configuratore;
    private final IAppView       ui;
    private final CatalogoService catalogoService;

    public ConfiguratoreController(Configuratore configuratore, IAppView ui, CatalogoService catalogoService) {
        this.configuratore = configuratore;
        this.ui  = ui;
        this.catalogoService = catalogoService;
    }

    /**
     * Runs the configuratore session: enforces first-time base-field setup (cannot skip),
     * then the main menu loop until the user logs out.
     */
    public void run()
    {
        // BUG-001 fix: loop until base fields are actually provided
        while (catalogoService.getCampiBase().isEmpty())
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
            ui.stampaMenu("MENU PRINCIPALE", MENU_PRINCIPALE, "Logout");
            int choice = ui.acquisisciIntero("Scelta: ", 0, MENU_PRINCIPALE.length);
            ui.newLine();

            switch (choice)
            {
                case 1:
                    menuCampiBase();
                    break;
                case 2:
                    menuCampiComuni();
                    break;
                case 3:
                    menuCategorie();
                    break;
                case 0:
                    return;
            }
        }
    }

    // ---------------------------------------------------------------
    // CAMPI BASE (first-time only)
    // ---------------------------------------------------------------

    private void menuCampiBase()
    {
        ui.header("CAMPI BASE");

        if (!catalogoService.getCampiBase().isEmpty())
        {
            ui.stampa("Stato: FISSATI (immutabili)");
            ui.stampaCampi(catalogoService.getCampiBase());
            ui.newLine();
            ui.pausa();
            ui.newLine();
            return;
        }

        ui.stampaInfo("I seguenti 8 campi base predefiniti verranno impostati automaticamente:");
        ui.stampaCampi(CampoBaseDefinito.getAll());
        ui.newLine();

        try
        {
            if (ui.acquisisciSiNo("Vuoi aggiungere campi base extra?"))
            {
                List<String> extra = ui.acquisisciListaNomi(
                        "Inserisci i campi base aggiuntivi (tutti obbligatori).");
                catalogoService.fissareCampiBaseConExtra(extra);
            }
            else
            {
                catalogoService.fissareCampiBase();
            }
            ui.stampaSuccesso("Campi base fissati e salvati.");
        }
        catch (OperationCancelledException e)
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

    private void xmenuCampiComuni()
    {
        while (true)
        {
            ui.header("CAMPI COMUNI");
            ui.stampaCampi(catalogoService.getCampiComuni());
            ui.newLine();
            ui.stampaMenu("", MENU_CAMPI_COMUNI, "Torna");
            int choice = ui.acquisisciIntero("Scelta: ", 0, MENU_CAMPI_COMUNI.length);
            ui.newLine();

            switch (choice) {
                case 1:
                    try {
                        ui.stampaInfo(IAppView.HINT_ANNULLA);
                        String nome = ui.acquisisciStringaConValidazione(
                                "Nome campo comune: ",
                                n -> !n.isBlank() && !catalogoService.nomeEsistente(n),
                                "Nome non valido o già esistente."
                        );
                        TipoDato tipoDato = ui.acquisisciTipoDato("Tipo di dato:");
                        boolean obbl = ui.acquisisciSiNo("Obbligatorio?");
                        String flagStr = obbl ? "obbligatorio" : "facoltativo";
                        if (!ui.acquisisciSiNo("Aggiungere '" + nome + "' [" + tipoDato + ", " + flagStr + "]?"))
                        {
                            ui.stampaInfo("Operazione annullata.");
                            break;
                        }
                        catalogoService.addCampoComune(nome, tipoDato, obbl);
                        ui.stampaSuccesso("Campo comune aggiunto.");
                    } catch (OperationCancelledException e) {
                        ui.stampaInfo("Operazione annullata.");
                    } catch (IllegalArgumentException e) {
                        ui.stampaErrore(e.getMessage());
                    }
                    ui.pausaConSpaziatura();
                    break;

                case 2:
                    ui.selezionaElemento("Seleziona campo comune da rimuovere:", catalogoService.getCampiComuni())
                      .ifPresentOrElse(
                          c -> {
                              if (!ui.acquisisciSiNo("Rimuovere il campo '" + c.getNome() + "'?"))
                              {
                                  ui.stampaInfo("Operazione annullata.");
                                  return;
                              }
                              if (catalogoService.removeCampoComune(c.getNome()))
                                  ui.stampaSuccesso("Campo rimosso.");
                              else
                                  ui.stampaErrore("Campo non trovato.");
                          }, () -> ui.stampaInfo("Operazione annullata.")
                      );
                    ui.pausaConSpaziatura();
                    break;

                case 3:
                    ui.selezionaElementoConInfo(
                            "Seleziona campo comune:",
                            catalogoService.getCampiComuni(),
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
                        if (catalogoService.setObbligatorietaCampoComune(c.getNome(), obbl))
                            ui.stampaSuccesso("Aggiornato.");
                        else
                            ui.stampaErrore("Campo non trovato.");
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
            ui.stampaCategorie(catalogoService.getCategorie());
            ui.newLine();
            ui.stampaMenu("", MENU_CATEGORIE, "Torna");
            int choice = ui.acquisisciIntero("Scelta: ", 0, MENU_CATEGORIE.length);
            ui.newLine();

            switch (choice)
            {
                case 1:
                    try {
                        ui.stampaInfo(IAppView.HINT_ANNULLA);
                        String nome = ui.acquisisciStringaConValidazione(
                                "Nome nuova categoria: ",
                                n -> !n.isBlank(),
                                "Il nome non può essere vuoto."
                        );
                        catalogoService.createCategoria(nome);
                        ui.stampaSuccesso("Categoria creata.");
                    } catch (OperationCancelledException e) {
                        ui.stampaInfo("Operazione annullata.");
                    } catch (IllegalArgumentException e) {
                        ui.stampaErrore(e.getMessage());
                    }
                    //ui.pausaConSpaziatura();
                    break;

                case 2:
                    ui.selezionaElemento("Seleziona categoria da rimuovere:", catalogoService.getCategorie())
                      .ifPresentOrElse(c -> {
                          if (!ui.acquisisciSiNo("Rimuovere '" + c.getNome() + "' e tutti i suoi campi specifici?")) {
                              ui.stampaInfo("Operazione annullata.");
                              return;
                          }
                          if (catalogoService.removeCategoria(c.getNome()))
                              ui.stampaSuccesso("Categoria rimossa.");
                          else
                              ui.stampaErrore("Categoria non trovata.");
                      }, () -> ui.stampaInfo("Operazione annullata."));
                    //ui.pausaConSpaziatura();
                    break;

                case 3:
                    ui.selezionaElemento("Seleziona categoria:", catalogoService.getCategorie())
                      .ifPresentOrElse(
                          c -> menuCampiSpecifici(c.getNome()),
                          () -> ui.stampaInfo("Operazione annullata.")
                      );
                    //ui.pausaConSpaziatura();
                    break;

                case 0:
                    return;
            }
        }
    }

    // ---------------------------------------------------------------
    // CAMPI SPECIFICI
    // ---------------------------------------------------------------

    private void xmenuCampiSpecifici(String nomeCategoria)
    {
        while (true)
        {
            Categoria categoria = catalogoService.getCategoriaOrThrow(nomeCategoria);
            ui.header(nomeCategoria);
            ui.stampaSezione("Campi BASE");
            ui.stampaCampi(catalogoService.getCampiBase());
            ui.stampaSezione("Campi COMUNI");
            ui.stampaCampi(catalogoService.getCampiComuni());
            ui.stampaSezione("Campi SPECIFICI");
            ui.stampaCampi(categoria.getCampiSpecifici());
            ui.newLine();
            ui.stampaMenu("", MENU_CAMPI_SPECIFICI, "Torna");
            int choice = ui.acquisisciIntero("Scelta: ", 0, MENU_CAMPI_SPECIFICI.length);
            ui.newLine();

            switch (choice) {
                case 1:
                    try {
                        ui.stampaInfo(IAppView.HINT_ANNULLA);
                        String nome = ui.acquisisciStringaConValidazione(
                                "Nome campo specifico: ",
                                n -> !n.isBlank() && !catalogoService.nomeEsistente(n),
                                "Nome non valido o già esistente."
                        );
                        TipoDato tipoDato = ui.acquisisciTipoDato("Tipo di dato:");
                        boolean obbl = ui.acquisisciSiNo("Obbligatorio?");
                        String flagStr = obbl ? "obbligatorio" : "facoltativo";
                        if (!ui.acquisisciSiNo("Aggiungere '" + nome + "' [" + tipoDato + ", " + flagStr + "]?"))
                        {
                            ui.stampaInfo("Operazione annullata.");
                            break;
                        }
                        catalogoService.addCampoSpecifico(nomeCategoria, nome, tipoDato, obbl);
                        ui.stampaSuccesso("Campo specifico aggiunto.");
                    } catch (OperationCancelledException e) {
                        ui.stampaInfo("Operazione annullata.");
                    } catch (IllegalArgumentException e) {
                        ui.stampaErrore(e.getMessage());
                    }
                    ui.pausaConSpaziatura();
                    break;

                case 2:
                    ui.selezionaElemento("Seleziona campo specifico da rimuovere:", categoria.getCampiSpecifici())
                      .ifPresentOrElse(
                          cs -> {
                              if (catalogoService.removeCampoSpecifico(nomeCategoria, cs.getNome()))
                                  ui.stampaSuccesso("Campo rimosso.");
                              else
                                  ui.stampaErrore("Campo non trovato.");
                          }, () -> ui.stampaInfo("Operazione annullata.")
                      );
                    ui.pausaConSpaziatura();
                    break;

                case 3:
                    ui.selezionaElementoConInfo(
                            "Seleziona campo specifico:",
                            categoria.getCampiSpecifici(),
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
                        if (catalogoService.setObbligatorietaCampoSpecifico(nomeCategoria, cs.getNome(), obbl))
                            ui.stampaSuccesso("Aggiornato.");
                        else
                            ui.stampaErrore("Campo non trovato.");
                    }, () -> ui.stampaInfo("Operazione annullata."));
                    ui.pausaConSpaziatura();
                    break;

                case 0:
                    return;
            }
        }
    }

    private void menuCampiGenerico(String titolo, GestioneCampi ops)
    {
        while (true)
        {
            ui.header(titolo);
            ui.stampaCampi(ops.getCampi());
            ui.newLine();
            ui.stampaMenu("", MENU_CAMPI_COMUNI, "Torna"); // same menu
            int choice = ui.acquisisciIntero("Scelta: ", 0, MENU_CAMPI_COMUNI.length);
            ui.newLine();

            switch (choice)
            {
                case 1: // ADD
                    try {
                        ui.stampaInfo(IAppView.HINT_ANNULLA);
                        String nome = ui.acquisisciStringaConValidazione(
                                "Nome campo: ",
                                n -> !n.isBlank() && !catalogoService.nomeEsistente(n),
                                "Nome non valido o già esistente."
                        );

                        TipoDato tipo = ui.acquisisciTipoDato("Tipo di dato:");
                        boolean obbl = ui.acquisisciSiNo("Obbligatorio?");

                        if (!ui.acquisisciSiNo(
                                "Aggiungere '" + nome + "' [" + tipo + ", " +
                                        (obbl ? "obbligatorio" : "facoltativo") + "]?"))
                        {
                            ui.stampaInfo("Operazione annullata.");
                            break;
                        }

                        ops.add(nome, tipo, obbl);
                        ui.stampaSuccesso("Campo aggiunto.");
                    }
                    catch (OperationCancelledException e) {
                        ui.stampaInfo("Operazione annullata.");
                    }
                    catch (IllegalArgumentException e) {
                        ui.stampaErrore(e.getMessage());
                    }
                    ui.pausaConSpaziatura();
                    break;

                case 2: // REMOVE
                    ui.selezionaElemento("Seleziona campo da rimuovere:", ops.getCampi())
                            .ifPresentOrElse(c -> {
                                if (!ui.acquisisciSiNo("Rimuovere '" + c.getNome() + "'?"))
                                {
                                    ui.stampaInfo("Operazione annullata.");
                                    return;
                                }
                                if (ops.remove(c.getNome()))
                                    ui.stampaSuccesso("Campo rimosso.");
                                else
                                    ui.stampaErrore("Campo non trovato.");
                            }, () -> ui.stampaInfo("Operazione annullata."));
                    ui.pausaConSpaziatura();
                    break;

                case 3: // TOGGLE
                    ui.selezionaElementoConInfo(
                            "Seleziona campo:",
                            ops.getCampi(),
                            c -> c.isObbligatorio() ? "obbligatorio" : "facoltativo"
                    ).ifPresentOrElse(c -> {
                        boolean nuovo = ui.acquisisciSiNo("Impostare come obbligatorio?");
                        if (nuovo == c.isObbligatorio())
                        {
                            ui.stampaAvviso("Nessuna modifica.");
                            return;
                        }

                        if (ops.setObbl(c.getNome(), nuovo))
                            ui.stampaSuccesso("Aggiornato.");
                        else
                            ui.stampaErrore("Campo non trovato.");
                    }, () -> ui.stampaInfo("Operazione annullata."));
                    ui.pausaConSpaziatura();
                    break;

                case 0:
                    return;
            }
        }
    }

    private void menuCampiComuni()
    {
        menuCampiGenerico("CAMPI COMUNI", new GestioneCampi() {
            public List<Campo> getCampi() {
                return catalogoService.getCampiComuni();
            }

            public void add(String nome, TipoDato tipo, boolean obbl) {
                catalogoService.addCampoComune(nome, tipo, obbl);
            }

            public boolean remove(String nome) {
                return catalogoService.removeCampoComune(nome);
            }

            public boolean setObbl(String nome, boolean obbl) {
                return catalogoService.setObbligatorietaCampoComune(nome, obbl);
            }
        });
    }

    private void menuCampiSpecifici(String nomeCategoria)
    {
        menuCampiGenerico(nomeCategoria, new GestioneCampi() {
            public List<Campo> getCampi() {
                return catalogoService.getCategoriaOrThrow(nomeCategoria).getCampiSpecifici();
            }

            public void add(String nome, TipoDato tipo, boolean obbl) {
                catalogoService.addCampoSpecifico(nomeCategoria, nome, tipo, obbl);
            }

            public boolean remove(String nome) {
                return catalogoService.removeCampoSpecifico(nomeCategoria, nome);
            }

            public boolean setObbl(String nome, boolean obbl) {
                return catalogoService.setObbligatorietaCampoSpecifico(nomeCategoria, nome, obbl);
            }
        });
    }

    private interface GestioneCampi {
        List<Campo> getCampi();
        void add(String nome, TipoDato tipo, boolean obbl);
        boolean remove(String nome);
        boolean setObbl(String nome, boolean obbl);
    }
}
