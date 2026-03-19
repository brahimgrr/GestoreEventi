package it.unibs.ingsoft.v5.controller;

import it.unibs.ingsoft.v5.model.AppConstants;
import it.unibs.ingsoft.v5.model.Campo;
import it.unibs.ingsoft.v5.model.Categoria;
import it.unibs.ingsoft.v5.model.Proposta;
import it.unibs.ingsoft.v5.model.TipoDato;
import it.unibs.ingsoft.v5.service.BatchImportService;
import it.unibs.ingsoft.v5.service.CampoService;
import it.unibs.ingsoft.v5.service.CategoriaService;
import it.unibs.ingsoft.v5.service.IscrizioneService;
import it.unibs.ingsoft.v5.service.PropostaService;
import it.unibs.ingsoft.v5.view.FieldValidator;
import it.unibs.ingsoft.v5.view.FormField;
import it.unibs.ingsoft.v5.view.IAppView;
import it.unibs.ingsoft.v5.view.viewmodel.ViewModelMapper;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles all configuratore menu interactions (V5: adds menuImportaDaFile).
 * Delegates base/common field operations to {@link CampoService};
 * category and specific field operations to {@link CategoriaService};
 * proposal operations to {@link PropostaService};
 * withdrawal to {@link IscrizioneService};
 * batch CSV import to {@link BatchImportService}.
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
    private final CampoService      campo;
    private final CategoriaService  cat;
    private final PropostaService   ps;
    private final IscrizioneService is;
    private final BatchImportService bs;

    public ConfiguratoreController(IAppView ui, CampoService campo,
                                   CategoriaService cat, PropostaService ps,
                                   IscrizioneService is, BatchImportService bs)
    {
        this.ui    = ui;
        this.campo = campo;
        this.cat   = cat;
        this.ps    = ps;
        this.is    = is;
        this.bs    = bs;
    }

    public void run()
    {
        if (!campo.isCampiBaseFissati())
            menuCampiBaseExtra();

        mainMenu();
    }

    private void mainMenu()
    {
        while (true)
        {
            ui.stampaMenu("MENU PRINCIPALE", MENU_PRINCIPALE);
            int choice = ui.acquisisciIntero("Scelta: ", 0, MENU_PRINCIPALE.length);
            ui.newLine();

            switch (choice)
            {
                case 1: menuCampiComuni();    break;
                case 2: menuCategorie();      break;
                case 3: menuVisualizza();     break;
                case 4: menuCreaProposta();   break;
                case 5: menuBacheca();        break;
                case 6: menuRitiraProposta(); break;
                case 7: menuImportaDaFile();  break;
                case 8: menuArchivio();       break;
                case 0: return;
            }
        }
    }

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

        boolean aggiungi = ui.acquisisciSiNo("Vuoi aggiungere campi base extra?");

        if (!aggiungi)
        {
            campo.fissaCampiBaseSenzaExtra();
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
            campo.fissaCampiBaseSenzaExtra();
            ui.stampa("Nessun campo extra inserito. Configurazione completata.");
        }
        else
        {
            try
            {
                campo.aggiungiCampiBaseExtra(nomi, tipi);
                ui.stampa("Campi base extra aggiunti e salvati.");
            }
            catch (IllegalArgumentException e)
            {
                ui.stampa("Errore: " + e.getMessage());
                campo.fissaCampiBaseSenzaExtra();
            }
        }

        ui.newLine();
        ui.pausa();
    }

    private void menuCampiComuni()
    {
        while (true)
        {
            ui.header("CAMPI COMUNI");
            ui.stampaSezione("Campi comuni attuali");
            ui.stampaCampi(campo.getCampiComuni());
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
                        campo.addCampoComune(nome, td, obbl);
                        ui.stampa("Campo comune aggiunto.");
                    }
                    catch (Exception e) { ui.stampa("Errore: " + e.getMessage()); }
                    break;

                case 2:
                    String nome2 = ui.acquisisciStringa("Nome campo da rimuovere: ");
                    ui.stampa(campo.removeCampoComune(nome2) ? "Rimosso." : "Campo non trovato.");
                    break;

                case 3:
                    String nome3  = ui.acquisisciStringa("Nome campo: ");
                    boolean obbl3 = ui.acquisisciSiNo("Impostare come obbligatorio?");
                    ui.stampa(campo.setObbligatorietaCampoComune(nome3, obbl3) ?
                            "Aggiornato." : "Campo non trovato.");
                    break;

                case 0: return;
            }

            ui.newLine();
            ui.pausa();
        }
    }

    private void menuCategorie()
    {
        while (true)
        {
            ui.header("CATEGORIE");
            ui.stampaSezione("Categorie attuali");
            ui.stampaCategorie(cat.getCategorie());
            ui.stampaMenu("CATEGORIE", MENU_CATEGORIE);

            int choice = ui.acquisisciIntero("Scelta: ", 0, 3);
            ui.newLine();

            switch (choice)
            {
                case 1:
                    String nome = ui.acquisisciStringa("Nome nuova categoria: ");
                    try { cat.createCategoria(nome); ui.stampa("Categoria creata."); }
                    catch (Exception e) { ui.stampa("Errore: " + e.getMessage()); }
                    break;

                case 2:
                    String nome2 = ui.acquisisciStringa("Nome categoria da rimuovere: ");
                    ui.stampa(cat.removeCategoria(nome2) ? "Rimossa." : "Categoria non trovata.");
                    break;

                case 3:
                    if (cat.getCategorie().isEmpty()) { ui.stampa("Nessuna categoria presente."); break; }
                    String nomeCat = ui.acquisisciStringa("Nome categoria: ");
                    try { cat.getCategoriaOrThrow(nomeCat); menuCampiSpecifici(nomeCat); }
                    catch (Exception e) { ui.stampa("Errore: " + e.getMessage()); }
                    break;

                case 0: return;
            }

            ui.newLine();
            ui.pausa();
        }
    }

    private void menuCampiSpecifici(String nomeCategoria)
    {
        while (true)
        {
            Categoria c = cat.getCategoriaOrThrow(nomeCategoria);
            ui.header("CAMPI SPECIFICI - " + nomeCategoria);
            ui.stampaSezione("Campi SPECIFICI");
            ui.stampaCampi(c.getCampiSpecifici());
            ui.stampaMenu("CAMPI SPECIFICI", MENU_CAMPI_SPECIFICI);

            int choice = ui.acquisisciIntero("Scelta: ", 0, 3);
            ui.newLine();

            switch (choice)
            {
                case 1:
                    String nome  = ui.acquisisciStringa("Nome campo specifico: ").trim();
                    TipoDato td  = ui.acquisisciTipoDato("Tipo del campo \"" + nome + "\":");
                    boolean obbl = ui.acquisisciSiNo("Obbligatorio?");
                    try { cat.addCampoSpecifico(nomeCategoria, nome, td, obbl); ui.stampa("Campo specifico aggiunto."); }
                    catch (Exception e) { ui.stampa("Errore: " + e.getMessage()); }
                    break;

                case 2:
                    String nome2 = ui.acquisisciStringa("Nome campo specifico da rimuovere: ");
                    ui.stampa(cat.removeCampoSpecifico(nomeCategoria, nome2) ? "Rimosso." : "Campo non trovato.");
                    break;

                case 3:
                    String nome3  = ui.acquisisciStringa("Nome campo specifico: ");
                    boolean obbl3 = ui.acquisisciSiNo("Impostare come obbligatorio?");
                    ui.stampa(cat.setObbligatorietaCampoSpecifico(nomeCategoria, nome3, obbl3) ?
                            "Aggiornato." : "Campo non trovato.");
                    break;

                case 0: return;
            }

            ui.newLine();
            ui.pausa();
        }
    }

    private void menuVisualizza()
    {
        ui.header("VISUALIZZAZIONE");
        ui.stampaSezione("Campi BASE");
        ui.stampaCampi(campo.getCampiBase());
        ui.stampaSezione("Campi COMUNI");
        ui.stampaCampi(campo.getCampiComuni());
        ui.stampaSezione("Categorie");
        ui.stampaCategorie(cat.getCategorie());
        ui.newLine();
        ui.pausa();
    }

    private void menuCreaProposta()
    {
        ui.header("CREA PROPOSTA");

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

        Optional<Map<String, String>> formResult = ui.runForm(buildFormFields(proposta));
        if (formResult.isEmpty())
        {
            ui.stampa("Operazione annullata.");
            ui.newLine();
            ui.pausa();
            return;
        }
        proposta.putAllValoriCampi(formResult.get());

        List<String> errori = ps.validaProposta(proposta);

        while (!errori.isEmpty())
        {
            ui.newLine();
            ui.stampa("La proposta NON è valida per i seguenti motivi:");
            for (String err : errori) ui.stampaErrore(err);

            ui.newLine();
            if (!ui.acquisisciSiNo("Vuoi correggere i campi errati?"))
            {
                ui.stampa("Proposta scartata.");
                ui.newLine();
                ui.pausa();
                return;
            }

            Set<String> nomiConErrore = ps.getCampiConErrore(proposta, errori).stream()
                    .map(Campo::getNome).collect(Collectors.toSet());
            List<FormField> corrFields = buildFormFields(proposta).stream()
                    .filter(f -> nomiConErrore.contains(f.getName()))
                    .collect(Collectors.toList());

            Optional<Map<String, String>> corrResult = ui.runForm(corrFields);
            if (corrResult.isEmpty())
            {
                ui.stampa("Proposta scartata.");
                ui.newLine();
                ui.pausa();
                return;
            }
            proposta.putAllValoriCampi(corrResult.get());
            errori = ps.validaProposta(proposta);
        }

        ui.newLine();
        ui.mostraRiepilogoProposta(
                ViewModelMapper.toPropostaVM(proposta, ps.getTuttiCampi(proposta)));

        if (ui.acquisisciSiNo("Vuoi pubblicare la proposta in bacheca?"))
        {
            try
            {
                ps.pubblicaProposta(proposta);
                ui.stampaSuccesso("Proposta pubblicata in bacheca!");
            }
            catch (Exception e) { ui.stampaErrore(e.getMessage()); }
        }
        else
        {
            ui.stampa("Proposta non pubblicata. Verrà scartata alla fine della sessione.");
        }

        ui.newLine();
        ui.pausa();
    }

    private void menuBacheca()
    {
        ui.header("BACHECA");
        ui.mostraBacheca(ViewModelMapper.toBachecaVM(ps.getBachecaPerCategoria(), ps::getTuttiCampi));
        ui.newLine();
        ui.pausa();
    }

    private void menuArchivio()
    {
        ui.header("ARCHIVIO PROPOSTE");
        ui.mostraArchivio(ViewModelMapper.toPropostaVMList(ps.getArchivio(), ps::getTuttiCampi));
        ui.newLine();
        ui.pausa();
    }

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

        OptionalInt scelta = ui.selezionaPropostaDaRitirare(
                ViewModelMapper.toSelezionabileVMList(ritirabili));
        if (scelta.isEmpty()) return;

        try
        {
            is.ritira(ritirabili.get(scelta.getAsInt()));
            ui.stampa("Proposta ritirata con successo. Tutti gli iscritti sono stati notificati.");
        }
        catch (IllegalStateException e) { ui.stampa("Errore: " + e.getMessage()); }

        ui.newLine();
        ui.pausa();
    }

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
        for (String line : report) ui.stampa("  " + line);

        ui.newLine();
        ui.pausa();
    }

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
