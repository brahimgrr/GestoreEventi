package it.unibs.ingsoft.v2.presentation.controller;

import it.unibs.ingsoft.v2.application.CampoService;
import it.unibs.ingsoft.v2.application.CategoriaService;
import it.unibs.ingsoft.v2.domain.Categoria;
import it.unibs.ingsoft.v2.domain.TipoDato;
import it.unibs.ingsoft.v2.presentation.view.cli.viewmodel.ViewModelMapper;
import it.unibs.ingsoft.v2.presentation.view.contract.IAppView;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.OptionalInt;

/**
 * Handles field- and category-management menus for the Configuratore.
 *
 * <p>Delegates base/common field operations to {@link CampoService};
 * category and specific-field operations to {@link CategoriaService};
 * proposal creation and bulletin-board display to {@link PropostaController}.</p>
 *
 * <p>This class has a single reason to change: when the field/category
 * management menus change. Proposal-lifecycle changes are isolated in
 * {@link PropostaController}; form-assembly changes are isolated in
 * {@link it.unibs.ingsoft.v2.presentation.view.cli.PropostaFormBuilder}.</p>
 */
public final class ConfiguratoreController
{
    private static final String[] MENU_PRINCIPALE =
            {
                    "Gestire campi COMUNI",
                    "Gestire CATEGORIE e campi SPECIFICI",
                    "Visualizzare categorie e campi",
                    "Creare una proposta di iniziativa",
                    "Visualizzare la bacheca"
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

    private final IAppView           ui;
    private final CampoService       campo;
    private final CategoriaService   cat;
    private final PropostaController propostaCtrl;

    public ConfiguratoreController(IAppView ui, CampoService campo,
                                   CategoriaService cat, PropostaController propostaCtrl)
    {
        this.ui           = ui;
        this.campo        = campo;
        this.cat          = cat;
        this.propostaCtrl = propostaCtrl;
    }

    /** Runs the configuratore session: optional base-field setup, then the main menu loop. */
    public void run()
    {
        if (!campo.isCampiBaseFissati())
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
                case 1: menuCampiComuni();              break;
                case 2: menuCategorie();                break;
                case 3: menuVisualizza();               break;
                case 4: avviaCreazioneProposta();       break;
                case 5: propostaCtrl.mostraBacheca();   break;
                case 0: return;
            }
        }
    }

    // ---------------------------------------------------------------
    // PRIMA CONFIGURAZIONE – CAMPI BASE EXTRA
    // ---------------------------------------------------------------

    private void menuCampiBaseExtra()
    {
        ui.header("PRIMA CONFIGURAZIONE – Campi base");
        ui.newLine();
        ui.stampa("I seguenti campi base sono già presenti (definiti dalla traccia):");
        ui.stampaCampi(campo.getCampiBase());
        ui.newLine();
        ui.stampa("Puoi aggiungere campi base EXTRA (obbligatori e immutabili).");
        ui.stampa("Questi campi NON potranno essere modificati o rimossi in futuro.");
        ui.newLine();

        if (!ui.acquisisciSiNo("Vuoi aggiungere campi base extra?"))
        {
            campo.fissaCampiBaseSenzaExtra();
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
            campo.aggiungiCampiBaseExtra(nomi, tipi);
            ui.stampaSuccesso("Campi base extra aggiunti e fissati.");
        }
        catch (IllegalArgumentException | IllegalStateException e)
        {
            ui.stampaErrore(e.getMessage());
            campo.fissaCampiBaseSenzaExtra();
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
            ui.stampaSezione("Campi comuni attuali");
            ui.stampaCampi(campo.getCampiComuni());
            ui.stampaMenu("CAMPI COMUNI", MENU_CAMPI_COMUNI);
            int choice = ui.acquisisciIntero("Scelta: ", 0, MENU_CAMPI_COMUNI.length);
            ui.newLine();
            if (choice == 0) return;

            switch (choice)
            {
                case 1:
                {
                    String nome = ui.acquisisciStringa("Nome campo comune: ").trim();
                    TipoDato td = ui.acquisisciTipoDato("Tipo dato:");
                    boolean obb = ui.acquisisciSiNo("Obbligatorio?");
                    try
                    {
                        campo.addCampoComune(nome, td, obb);
                        ui.stampaSuccesso("Campo comune aggiunto.");
                    }
                    catch (IllegalArgumentException e) { ui.stampaErrore(e.getMessage()); }
                    break;
                }
                case 2:
                {
                    String nome = ui.acquisisciStringa("Nome campo da rimuovere: ").trim();
                    if (campo.removeCampoComune(nome))
                        ui.stampaSuccesso("Campo rimosso.");
                    else
                        ui.stampaAvviso("Campo non trovato.");
                    break;
                }
                case 3:
                {
                    String nome = ui.acquisisciStringa("Nome campo: ").trim();
                    boolean obb = ui.acquisisciSiNo("Rendere obbligatorio?");
                    if (campo.setObbligatorietaCampoComune(nome, obb))
                        ui.stampaSuccesso("Obbligatorietà aggiornata.");
                    else
                        ui.stampaAvviso("Campo non trovato.");
                    break;
                }
            }
            ui.newLine();
            ui.pausa();
        }
    }

    // ---------------------------------------------------------------
    // CATEGORIE
    // ---------------------------------------------------------------

    private void menuCategorie()
    {
        while (true)
        {
            ui.stampaSezione("Categorie attuali");
            ui.stampaCategorie(cat.getCategorie());
            ui.stampaMenu("CATEGORIE", MENU_CATEGORIE);
            int choice = ui.acquisisciIntero("Scelta: ", 0, MENU_CATEGORIE.length);
            ui.newLine();
            if (choice == 0) return;

            switch (choice)
            {
                case 1:
                {
                    String nome = ui.acquisisciStringa("Nome nuova categoria: ").trim();
                    try
                    {
                        cat.createCategoria(nome);
                        ui.stampaSuccesso("Categoria \"" + nome + "\" creata.");
                    }
                    catch (IllegalArgumentException e) { ui.stampaErrore(e.getMessage()); }
                    break;
                }
                case 2:
                {
                    String nome = ui.acquisisciStringa("Nome categoria da rimuovere: ").trim();
                    if (ui.acquisisciSiNo("Confermi la rimozione di \"" + nome + "\"?"))
                    {
                        if (cat.removeCategoria(nome))
                            ui.stampaSuccesso("Categoria rimossa.");
                        else
                            ui.stampaAvviso("Categoria non trovata.");
                    }
                    break;
                }
                case 3:
                {
                    String nome = ui.acquisisciStringa("Nome categoria: ").trim();
                    try { menuCampiSpecifici(cat.getCategoriaOrThrow(nome)); }
                    catch (NoSuchElementException e) { ui.stampaErrore(e.getMessage()); }
                    break;
                }
            }
            ui.newLine();
            ui.pausa();
        }
    }

    private void menuCampiSpecifici(Categoria categoria)
    {
        while (true)
        {
            ui.stampaSezione("Campi specifici di \"" + categoria.getNome() + "\"");
            ui.stampaCampi(categoria.getCampiSpecifici());
            ui.stampaMenu("CAMPI SPECIFICI", MENU_CAMPI_SPECIFICI);
            int choice = ui.acquisisciIntero("Scelta: ", 0, MENU_CAMPI_SPECIFICI.length);
            ui.newLine();
            if (choice == 0) return;

            switch (choice)
            {
                case 1:
                {
                    String nome = ui.acquisisciStringa("Nome campo specifico: ").trim();
                    TipoDato td = ui.acquisisciTipoDato("Tipo dato:");
                    boolean obb = ui.acquisisciSiNo("Obbligatorio?");
                    try
                    {
                        cat.addCampoSpecifico(categoria.getNome(), nome, td, obb);
                        ui.stampaSuccesso("Campo specifico aggiunto.");
                    }
                    catch (IllegalArgumentException e) { ui.stampaErrore(e.getMessage()); }
                    break;
                }
                case 2:
                {
                    String nome = ui.acquisisciStringa("Nome campo da rimuovere: ").trim();
                    if (cat.removeCampoSpecifico(categoria.getNome(), nome))
                        ui.stampaSuccesso("Campo rimosso.");
                    else
                        ui.stampaAvviso("Campo non trovato.");
                    break;
                }
                case 3:
                {
                    String nome = ui.acquisisciStringa("Nome campo: ").trim();
                    boolean obb = ui.acquisisciSiNo("Rendere obbligatorio?");
                    if (cat.setObbligatorietaCampoSpecifico(categoria.getNome(), nome, obb))
                        ui.stampaSuccesso("Obbligatorietà aggiornata.");
                    else
                        ui.stampaAvviso("Campo non trovato.");
                    break;
                }
            }
            ui.newLine();
            ui.pausa();
        }
    }

    // ---------------------------------------------------------------
    // VISUALIZZAZIONE
    // ---------------------------------------------------------------

    private void menuVisualizza()
    {
        ui.header("VISUALIZZAZIONE");
        ui.stampaSezione("Campi base");
        ui.stampaCampi(campo.getCampiBase());
        ui.stampaSezione("Campi comuni");
        ui.stampaCampi(campo.getCampiComuni());
        ui.stampaSezione("Categorie e campi specifici");
        ui.stampaCategorie(cat.getCategorie());
        ui.newLine();
        ui.pausa();
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
        List<Categoria> categorie = cat.getCategorie();
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
        propostaCtrl.avviaCreazione(nomeCategoria);
    }
}
