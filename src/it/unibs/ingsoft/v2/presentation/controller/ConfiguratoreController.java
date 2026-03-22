package it.unibs.ingsoft.v2.presentation.controller;

import it.unibs.ingsoft.v2.application.CampoService;
import it.unibs.ingsoft.v2.application.CategoriaService;
import it.unibs.ingsoft.v2.domain.Categoria;
import it.unibs.ingsoft.v2.domain.TipoDato;
import it.unibs.ingsoft.v2.presentation.view.viewmodel.ViewModelMapper;
import it.unibs.ingsoft.v2.presentation.view.contract.IAppView;
import it.unibs.ingsoft.v2.presentation.view.contract.OperationCancelledException;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

/**
 * Handles field- and category-management menus for the Configuratore.
 * Delegates base/common field operations to {@link CampoService};
 * category and specific-field operations to {@link CategoriaService};
 * proposal creation and bulletin-board display to {@link PropostaController}.</p>
 */
public final class ConfiguratoreController {
    private static final String[] MENU_PRINCIPALE = {
        "Gestire campi BASE",
        "Gestire campi COMUNI",
        "Gestire CATEGORIE e campi SPECIFICI",
        "Visualizzare categorie e campi",
        "Creare una proposta di iniziativa",
        "Visualizzare la bacheca"
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

    private final IAppView       ui;
    private final CampoService campoService;
    private final CategoriaService categoriaService;
    private final PropostaController propostaController;

    public ConfiguratoreController(IAppView ui, CampoService campoService,
                                   CategoriaService categoriaService, PropostaController propostaController) {
        this.ui  = ui;
        this.campoService = campoService;
        this.categoriaService = categoriaService;
        this.propostaController = propostaController;
    }

    /** Runs the configuratore session: optional base-field setup, then the main menu loop. */
    public void run()
    {
        if (!campoService.isCampiBaseFissati())
            menuCampiBaseExtra();
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
                case 1:
                    menuCampiBase();
                    break;
                case 2:
                    menuCampiComuni();
                    break;
                case 3:
                    menuCategorie();
                    break;
                case 4:
                    menuVisualizza();
                    break;
                case 5:
                    avviaCreazioneProposta();
                    break;
                case 6:
                    propostaController.mostraBacheca();
                    break;
                case 0:
                    return;
            }
        }
    }

    // ---------------------------------------------------------------
    // CAMPI BASE (first-time only)
    // ---------------------------------------------------------------

    private void menuCampiBaseExtra()
    {
        ui.header("PRIMA CONFIGURAZIONE – Campi base");
        ui.newLine();
        ui.stampa("I seguenti campi base sono già presenti (definiti dalla traccia):");
        ui.stampaCampi(campoService.getCampiBase());
        ui.newLine();
        ui.stampa("Puoi aggiungere campi base EXTRA (obbligatori e immutabili).");
        ui.stampa("Questi campi NON potranno essere modificati o rimossi in futuro.");
        ui.newLine();

        if (!ui.acquisisciSiNo("Vuoi aggiungere campi base extra?"))
        {
            campoService.fissaCampiBaseSenzaExtra();
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
            campoService.aggiungiCampiBaseExtra(nomi, tipi);
            ui.stampaSuccesso("Campi base extra aggiunti e fissati.");
        }
        catch (IllegalArgumentException | IllegalStateException e)
        {
            ui.stampaErrore(e.getMessage());
            campoService.fissaCampiBaseSenzaExtra();
        }

        ui.newLine();
        ui.pausa();
    }

    // ---------------------------------------------------------------
    // CAMPI BASE (read-only view)
    // ---------------------------------------------------------------

    private void menuCampiBase()
    {
        ui.header("CAMPI BASE");
        ui.stampa("Stato: FISSATI (immutabili)");
        ui.stampaCampi(campoService.getCampiBase());
        ui.newLine();
        ui.pausaConSpaziatura();
    }

    // ---------------------------------------------------------------
    // VISUALIZZAZIONE
    // ---------------------------------------------------------------

    private void menuVisualizza()
    {
        ui.header("VISUALIZZAZIONE");
        ui.stampaSezione("Campi BASE");
        ui.stampaCampi(campoService.getCampiBase());
        ui.stampaSezione("Campi COMUNI");
        ui.stampaCampi(campoService.getCampiComuni());
        ui.stampaSezione("Categorie");
        ui.stampaCategorie(categoriaService.getCategorie());
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
            ui.stampaCampi(campoService.getCampiComuni());
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
                                n -> !n.isBlank() && !campoService.nomeEsiste(n),
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
                        campoService.addCampoComune(nome, tipoDato, obbl);
                        ui.stampaSuccesso("Campo comune aggiunto.");
                    } catch (OperationCancelledException e) {
                        ui.stampaInfo("Operazione annullata.");
                    } catch (IllegalArgumentException e) {
                        ui.stampaErrore(e.getMessage());
                    }
                    ui.pausaConSpaziatura();
                    break;

                case 2:
                    ui.selezionaElemento("Seleziona campo comune da rimuovere:", campoService.getCampiComuni())
                      .ifPresentOrElse(
                          c -> {
                              if (!ui.acquisisciSiNo("Rimuovere il campo '" + c.getNome() + "'?"))
                              {
                                  ui.stampaInfo("Operazione annullata.");
                                  return;
                              }
                              if (campoService.removeCampoComune(c.getNome()))
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
                            campoService.getCampiComuni(),
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
                        if (campoService.setObbligatorietaCampoComune(c.getNome(), obbl))
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
            ui.stampaCategorie(categoriaService.getCategorie());
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
                        categoriaService.createCategoria(nome);
                        ui.stampaSuccesso("Categoria creata.");
                    } catch (OperationCancelledException e) {
                        ui.stampaInfo("Operazione annullata.");
                    } catch (IllegalArgumentException e) {
                        ui.stampaErrore(e.getMessage());
                    }
                    ui.pausaConSpaziatura();
                    break;

                case 2:
                    ui.selezionaElemento("Seleziona categoria da rimuovere:", categoriaService.getCategorie())
                      .ifPresentOrElse(c -> {
                          if (!ui.acquisisciSiNo("Rimuovere '" + c.getNome() + "' e tutti i suoi campi specifici?")) {
                              ui.stampaInfo("Operazione annullata.");
                              return;
                          }
                          if (categoriaService.removeCategoria(c.getNome()))
                              ui.stampaSuccesso("Categoria rimossa.");
                          else
                              ui.stampaErrore("Categoria non trovata.");
                      }, () -> ui.stampaInfo("Operazione annullata."));
                    ui.pausaConSpaziatura();
                    break;

                case 3:
                    ui.selezionaElemento("Seleziona categoria:", categoriaService.getCategorie())
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
            Categoria categoria = categoriaService.getCategoriaOrThrow(nomeCategoria);
            ui.header(nomeCategoria);
            ui.stampaSezione("Campi BASE");
            ui.stampaCampi(campoService.getCampiBase());
            ui.stampaSezione("Campi COMUNI");
            ui.stampaCampi(campoService.getCampiComuni());
            ui.stampaSezione("Campi SPECIFICI");
            ui.stampaCampi(categoria.getCampiSpecifici());
            ui.newLine();
            ui.stampaMenu("", MENU_CAMPI_SPECIFICI, "Torna");
            int choice = ui.acquisisciIntero("Scelta: ", 0, MENU_CAMPI_SPECIFICI.length);
            ui.newLine();

            switch (choice) {
                case 1:
                    try
                    {
                        ui.stampaInfo(IAppView.HINT_ANNULLA);
                        String nome = ui.acquisisciStringaConValidazione(
                                "Nome campo specifico: ",
                                n -> !n.isBlank() && !campoService.nomeEsiste(n),
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
                        categoriaService.addCampoSpecifico(nomeCategoria, nome, tipoDato, obbl);
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
                              if (categoriaService.removeCampoSpecifico(nomeCategoria, cs.getNome()))
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
                        if (categoriaService.setObbligatorietaCampoSpecifico(nomeCategoria, cs.getNome(), obbl))
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
    // CATEGORIA SELECTION → PROPOSTA DELEGATION
    // ---------------------------------------------------------------

    /**
     * Lets the user choose a category, then delegates to {@link PropostaController}
     * for the creation workflow. Category selection stays here because it requires
     * {@link CategoriaService}, which {@code PropostaController} does not own.
     */
    private void avviaCreazioneProposta()
    {
        List<Categoria> categorie = categoriaService.getCategorie();
        if (categorie.isEmpty())
        {
            ui.stampa("Nessuna categoria disponibile. Crea almeno una categoria prima.");
            ui.newLine();
            ui.pausa();
            return;
        }

        ui.stampaSezione("Categorie disponibili");
        OptionalInt idxOpt = ui.selezionaCategoria(ViewModelMapper.toCategoriaVMList(categorie));
        if (idxOpt.isEmpty()) return;

        String nomeCategoria = categorie.get(idxOpt.getAsInt()).getNome();
        propostaController.avviaCreazione(nomeCategoria);
    }
}
