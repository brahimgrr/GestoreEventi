package it.unibs.ingsoft.v5.controller;

import it.unibs.ingsoft.v5.model.AppConstants;
import it.unibs.ingsoft.v5.model.Campo;
import it.unibs.ingsoft.v5.model.Categoria;
import it.unibs.ingsoft.v5.model.Proposta;
import it.unibs.ingsoft.v5.model.TipoDato;
import it.unibs.ingsoft.v5.service.BatchImportService;
import it.unibs.ingsoft.v5.service.CategoriaService;
import it.unibs.ingsoft.v5.service.IscrizioneService;
import it.unibs.ingsoft.v5.service.PropostaService;
import it.unibs.ingsoft.v5.view.FieldValidator;
import it.unibs.ingsoft.v5.view.FormField;
import it.unibs.ingsoft.v5.view.IAppView;
import it.unibs.ingsoft.v5.view.StepByStepFormRunner;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles all configuratore menu interactions (V5: adds menuImportaDaFile with user-provided path).
 */
public final class ConfiguratoreController
{
    private static final String[] MENU_PRINCIPALE =
            {
                    "Gestire campi COMUNI",
                    "Gestire CATEGORIE e campi SPECIFICI",
                    "Visualizzare categorie e campi",
                    "Creare una proposta di iniziativa",
                    "Visualizzare la bacheca",
                    "Ritira una proposta",
                    "Importa da file",
                    "Visualizzare l'archivio proposte"
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

    private final IAppView          ui;
    private final CategoriaService  cs;
    private final PropostaService   ps;
    private final IscrizioneService is;
    private final BatchImportService bs;

    public ConfiguratoreController(IAppView ui, CategoriaService cs,
                                   PropostaService ps, IscrizioneService is,
                                   BatchImportService bs)
    {
        this.ui = ui;
        this.cs = cs;
        this.ps = ps;
        this.is = is;
        this.bs = bs;
    }

    public void run()
    {
        if (!cs.isCampiBaseFissati())
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
                case 1: menuCampiComuni();       break;
                case 2: menuCategorie();         break;
                case 3: menuVisualizza();        break;
                case 4: menuCreaProposta();      break;
                case 5: menuBacheca();           break;
                case 6: menuRitiraProposta();    break;
                case 7: menuImportaDaFile();     break;
                case 8: menuArchivio();          break;
                case 0: return;
            }
        }
    }

    // ---------------------------------------------------------------
    // PRIMA CONFIGURAZIONE
    // ---------------------------------------------------------------

    private void menuCampiBaseExtra()
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
            ui.pausa();
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
                    String nome  = ui.acquisisciStringa("Nome campo: ").trim();
                    TipoDato td  = ui.acquisisciTipoDato("Tipo del campo \"" + nome + "\":");
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
                        menuCampiSpecifici(nomeCat);
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
            ui.pausa();
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
            ui.pausa();
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

    // ---------------------------------------------------------------
    // CREA PROPOSTA
    // ---------------------------------------------------------------

    private void menuCreaProposta()
    {
        ui.header("CREA PROPOSTA");

        List<Categoria> categorie = cs.getCategorie();
        if (categorie.isEmpty())
        {
            ui.stampa("Nessuna categoria disponibile. Crea almeno una categoria prima.");
            ui.newLine();
            ui.pausa();
            return;
        }

        ui.stampaSezione("Categorie disponibili");
        ui.stampaCategorieSelezione(categorie);
        ui.newLine();

        int idx = ui.acquisisciIntero("Scegli categoria (0 per annullare): ", 0, categorie.size());
        if (idx == 0) return;
        String nomeCategoria = categorie.get(idx - 1).getNome();

        Proposta proposta;
        try
        {
            proposta = ps.creaProposta(nomeCategoria);
        }
        catch (IllegalArgumentException e)
        {
            ui.stampaErrore(e.getMessage());
            ui.newLine();
            ui.pausa();
            return;
        }

        ui.newLine();
        ui.stampa("Digita 'annulla' per abortire l'operazione.");
        ui.stampa("(*) = obbligatorio | il tipo è indicato tra [  ]");
        ui.newLine();

        try
        {
            StepByStepFormRunner runner = new StepByStepFormRunner(ui, ui, buildFormFields(proposta));
            proposta.putAllValoriCampi(runner.run());

            List<String> errori = ps.validaProposta(proposta);

            while (!errori.isEmpty())
            {
                ui.newLine();
                ui.stampa("La proposta NON è valida per i seguenti motivi:");
                for (String err : errori)
                    ui.stampaErrore(err);

                ui.newLine();
                if (!ui.acquisisciSiNo("Vuoi correggere i campi errati?"))
                {
                    ui.stampa("Proposta scartata.");
                    ui.newLine();
                    ui.pausa();
                    return;
                }

                ui.correggiCampiNonValidi(proposta.getValoriCampi(),
                        ps.getCampiConErrore(proposta, errori));
                errori = ps.validaProposta(proposta);
            }

            ui.newLine();
            ui.mostraRiepilogoProposta(proposta, ps.getTuttiCampi(proposta));

            if (ui.acquisisciSiNo("Vuoi pubblicare la proposta in bacheca?"))
            {
                try
                {
                    ps.pubblicaProposta(proposta);
                    ui.stampaSuccesso("Proposta pubblicata in bacheca!");
                }
                catch (Exception e)
                {
                    ui.stampaErrore(e.getMessage());
                }
            }
            else
            {
                ui.stampa("Proposta non pubblicata. Verrà scartata alla fine della sessione.");
            }
        }
        catch (it.unibs.ingsoft.v5.view.ConsoleUI.CancelException e)
        {
            ui.stampa("Operazione annullata.");
        }

        ui.newLine();
        ui.pausa();
    }

    // ---------------------------------------------------------------
    // BACHECA / ARCHIVIO
    // ---------------------------------------------------------------

    private void menuBacheca()
    {
        ui.header("BACHECA");
        ui.mostraBacheca(ps.getBachecaPerCategoria(), ps::getTuttiCampi);
        ui.newLine();
        ui.pausa();
    }

    private void menuArchivio()
    {
        ui.header("ARCHIVIO PROPOSTE");
        ui.mostraArchivio(ps.getArchivio(), ps::getTuttiCampi);
        ui.newLine();
        ui.pausa();
    }

    // ---------------------------------------------------------------
    // RITIRA PROPOSTA
    // ---------------------------------------------------------------

    private void menuRitiraProposta()
    {
        ui.header("RITIRA PROPOSTA");

        List<Proposta> ritirabili = ps.getProposteRitirabili();

        if (ritirabili.isEmpty())
        {
            ui.stampa("Nessuna proposta aperta o confermata da ritirare.");
            ui.newLine();
            ui.pausa();
            return;
        }

        ui.stampa("Proposte ritirabili:");
        ui.newLine();

        ui.stampaProposteRitirabili(ritirabili);

        ui.newLine();
        int scelta = ui.acquisisciIntero(
                "Scegli proposta da ritirare (0 per annullare): ", 0, ritirabili.size());

        if (scelta == 0)
            return;

        try
        {
            is.ritira(ritirabili.get(scelta - 1));
            ui.stampa("Proposta ritirata con successo. Tutti gli iscritti sono stati notificati.");
        }
        catch (IllegalStateException e)
        {
            ui.stampa("Errore: " + e.getMessage());
        }

        ui.newLine();
        ui.pausa();
    }

    // ---------------------------------------------------------------
    // IMPORTA DA FILE
    // ---------------------------------------------------------------

    private void menuImportaDaFile()
    {
        ui.header("IMPORTA DA FILE");

        ui.stampa("1) Campi comuni");
        ui.stampa("2) Categorie");
        ui.stampa("3) Proposte");
        ui.stampa("0) Torna indietro");
        ui.newLine();

        int scelta = ui.acquisisciIntero("Scelta: ", 0, 3);
        if (scelta == 0) return;

        String pathStr = ui.acquisisciStringa("Percorso file CSV: ").trim();
        Path file = Path.of(pathStr);

        List<String> report;
        switch (scelta)
        {
            case 1: report = bs.importaCampiComuni(file); break;
            case 2: report = bs.importaCategorie(file);   break;
            case 3: report = bs.importaProposte(file);    break;
            default: return;
        }

        ui.newLine();
        ui.stampaSezione("Report importazione");
        for (String line : report)
            ui.stampa("  " + line);

        ui.newLine();
        ui.pausa();
    }

    // ---------------------------------------------------------------
    // FORM BUILDER (controller-side: knows both service and view types)
    // ---------------------------------------------------------------

    private List<FormField> buildFormFields(Proposta proposta)
    {
        List<FormField> fields = new ArrayList<>();
        Map<String, String> valori = proposta.getValoriCampi();

        for (Campo c : ps.getTuttiCampi(proposta))
        {
            List<FieldValidator> validators = new ArrayList<>();

            if (PropostaService.CAMPO_TERMINE_ISCRIZIONE.equals(c.getNome()))
            {
                validators.add((input, ctx) -> {
                    try {
                        LocalDate d = LocalDate.parse(input.trim(), AppConstants.DATE_FMT);
                        if (!d.isAfter(LocalDate.now()))
                            return "\"" + PropostaService.CAMPO_TERMINE_ISCRIZIONE + "\" deve essere successivo alla data odierna.";
                        return null;
                    } catch (Exception e) { return null; }
                });
            }
            else if (PropostaService.CAMPO_DATA.equals(c.getNome()))
            {
                validators.add((input, ctx) -> {
                    String termineStr = ctx.get(PropostaService.CAMPO_TERMINE_ISCRIZIONE);
                    if (termineStr == null || termineStr.isBlank()) return null;
                    try {
                        LocalDate termine = LocalDate.parse(termineStr.trim(), AppConstants.DATE_FMT);
                        LocalDate data    = LocalDate.parse(input.trim(), AppConstants.DATE_FMT);
                        if (!data.isAfter(termine.plusDays(1)))
                            return "\"" + PropostaService.CAMPO_DATA + "\" deve essere almeno 2 giorni dopo \""
                                    + PropostaService.CAMPO_TERMINE_ISCRIZIONE + "\" ("
                                    + termine.format(AppConstants.DATE_FMT) + "). Min: "
                                    + termine.plusDays(2).format(AppConstants.DATE_FMT) + ".";
                        return null;
                    } catch (Exception e) { return null; }
                });
            }
            else if (PropostaService.CAMPO_DATA_CONCLUSIVA.equals(c.getNome()))
            {
                validators.add((input, ctx) -> {
                    String dataStr = ctx.get(PropostaService.CAMPO_DATA);
                    if (dataStr == null || dataStr.isBlank()) return null;
                    try {
                        LocalDate data      = LocalDate.parse(dataStr.trim(), AppConstants.DATE_FMT);
                        LocalDate conclusiva = LocalDate.parse(input.trim(), AppConstants.DATE_FMT);
                        if (conclusiva.isBefore(data))
                            return "\"" + PropostaService.CAMPO_DATA_CONCLUSIVA + "\" non può essere precedente a \"" + PropostaService.CAMPO_DATA + "\".";
                        return null;
                    } catch (Exception e) { return null; }
                });
            }

            fields.add(new FormField(
                    c.getNome(),
                    c.getNome(),
                    c.getTipoDato(),
                    c.isObbligatorio(),
                    valori.get(c.getNome()),
                    validators
            ));
        }

        return fields;
    }
}
