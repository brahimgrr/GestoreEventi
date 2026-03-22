package it.unibs.ingsoft.v1.presentation.controller;

import it.unibs.ingsoft.v1.domain.*;
import it.unibs.ingsoft.v1.application.CatalogoService;
import it.unibs.ingsoft.v1.presentation.view.contract.IAppView;
import it.unibs.ingsoft.v1.presentation.view.contract.OperationCancelledException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handles all configuratore menu interactions.
 */
public final class ConfiguratoreController {
    private static final String[] MENU_PRINCIPALE = {
        "Gestire campi COMUNI",
        "Gestire CATEGORIE e campi SPECIFICI",
        "Visualizzare categorie e campi"
    };

    private static final String[] MENU_CAMPI = {
        "Aggiungi campo",
        "Rimuovi campo",
        "Cambia obbligatorietà campo"
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
            menuCampiBaseExtra();
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
                    menuCampiComuni();
                    break;
                case 2:
                    menuCategorie();
                    break;
                case 3:
                    menuVisualizza();
                    break;
                case 0:
                    return;
            }
        }
    }

    private void menuCampiBaseExtra()
    {
        ui.header("PRIMA CONFIGURAZIONE – Campi base");
        ui.newLine();
        ui.stampa("I seguenti campi base sono già presenti (definiti dalla traccia):");
        ui.stampaCampi(Arrays.stream(CampoBaseDefinito.values()).map(CampoBaseDefinito::toCampo).toList());
        ui.newLine();
        ui.stampa("Puoi aggiungere campi base EXTRA (obbligatori e immutabili).");
        ui.stampa("Questi campi NON potranno essere modificati o rimossi in futuro.");
        ui.newLine();

        if (!ui.acquisisciSiNo("Vuoi aggiungere campi base extra?"))
        {
            catalogoService.initiateCampiBase();
            ui.stampaInfo("Nessun campo base extra aggiunto.");
            ui.newLine();
            ui.pausa();
            return;
        }

        List<String>   nomi = new ArrayList<>();
        List<TipoDato> tipi = new ArrayList<>();

        ui.stampa("Inserisci i nomi dei campi extra (riga vuota per terminare):");
        List<String> nomiInput = ui.acquisisciListaNomi("Campi base extra");

        for (String nome : nomiInput)
        {
            TipoDato td = ui.acquisisciTipoDato("Tipo per \"" + nome + "\":");
            nomi.add(nome);
            tipi.add(td);
        }

        try
        {
            //TODO ADD TIPI
            catalogoService.addCampiBaseConExtra(nomi, tipi);
            ui.stampaSuccesso("Campi base extra aggiunti e fissati.");
        }
        catch (IllegalArgumentException | IllegalStateException e)
        {
            ui.stampaErrore(e.getMessage());
            catalogoService.initiateCampiBase();
        }

        ui.newLine();
        ui.pausa();
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
                          c -> menuCampiSpecifici(c),
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
    // VISUALIZZAZIONE
    // ---------------------------------------------------------------

    private void menuVisualizza()
    {
        ui.header("VISUALIZZAZIONE");
        ui.stampaSezione("Campi BASE");
        ui.stampaCampi(catalogoService.getCampiBase());
        ui.stampaSezione("Campi COMUNI");
        ui.stampaCampi(catalogoService.getCampiComuni());
        ui.stampaSezione("Categorie");
        ui.stampaCategorie(catalogoService.getCategorie());
        ui.newLine();
        ui.pausaConSpaziatura();
    }

    // ---------------------------------------------------------------
    // CAMPI COMUNI E SPECIFICI
    // ---------------------------------------------------------------

    private void menuCampiGenerico(String titolo, GestioneCampi ops)
    {
        while (true)
        {
            ui.header(titolo);
            ui.stampaCampi(ops.getCampi());
            ui.newLine();
            ui.stampaMenu("", MENU_CAMPI, "Torna"); // same menu
            int choice = ui.acquisisciIntero("Scelta: ", 0, MENU_CAMPI.length);
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

    private void menuCampiSpecifici(Categoria categoria)
    {
        menuCampiGenerico("CAMPI SPECIFICI", new GestioneCampi() {
            public List<Campo> getCampi() {
                return categoria.getCampiSpecifici();
            }

            public void add(String nome, TipoDato tipo, boolean obbl) {
                catalogoService.addCampoSpecifico(categoria.getNome(), nome, tipo, obbl);
            }

            public boolean remove(String nome) {
                return catalogoService.removeCampoSpecifico(categoria.getNome(), nome);
            }

            public boolean setObbl(String nome, boolean obbl) {
                return catalogoService.setObbligatorietaCampoSpecifico(categoria.getNome(), nome, obbl);
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
