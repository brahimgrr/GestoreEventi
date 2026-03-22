package it.unibs.ingsoft.v3.presentation.controller;

import it.unibs.ingsoft.v3.application.PropostaService;
import it.unibs.ingsoft.v3.domain.Campo;
import it.unibs.ingsoft.v3.domain.Categoria;
import it.unibs.ingsoft.v3.domain.Proposta;
import it.unibs.ingsoft.v3.presentation.controller.ConfiguratoreController;
import it.unibs.ingsoft.v3.presentation.view.cli.FormField;
import it.unibs.ingsoft.v3.presentation.view.cli.PropostaFormBuilder;
import it.unibs.ingsoft.v3.presentation.view.contract.IAppView;
import it.unibs.ingsoft.v3.presentation.view.viewmodel.ViewModelMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles the proposal workflow for the Configuratore:
 * creation, form fill-in, validation loop, publication, and bulletin-board display.
 *
 * <p>Extracted from {@link ConfiguratoreController} so that each class has a
 * single reason to change: this class changes only when the proposal lifecycle
 * or its UI changes; {@code ConfiguratoreController} changes only when the
 * field/category management menus change.</p>
 *
 * <p>Dependencies: only {@link IAppView} and {@link PropostaService}.
 * Category selection is performed by the caller before invoking
 */
public final class PropostaController
{
    private final IAppView       ui;
    private final PropostaService ps;

    public PropostaController(IAppView ui, PropostaService ps)
    {
        this.ui = ui;
        this.ps = ps;
    }

    // ---------------------------------------------------------------
    // PUBLIC API (called by ConfiguratoreController)
    // ---------------------------------------------------------------

    /**
     * Runs the full proposal-creation workflow for the given category.
     */
    public void avviaCreazione(Categoria categoria, List<Campo> campiBase, List<Campo> campiComuni)
    {
        ui.header("CREA PROPOSTA");

        Proposta proposta;
        try
        {
            proposta = ps.creaProposta(categoria, campiBase, campiComuni);
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

        Optional<Map<String, String>> formResult = ui.runForm(PropostaFormBuilder.build(proposta));
        if (formResult.isEmpty())
        {
            ui.stampa("Operazione annullata.");
            ui.newLine();
            ui.pausa();
            return;
        }
        proposta.putAllValoriCampi(formResult.get());

        List<String> errori = ps.validaProposta(proposta);
        boolean abortito = correggiFinchéValida(proposta, errori);
        if (abortito) return;

        mostraRiepilogoEPubblica(proposta);
    }

    /** Displays the bulletin board (APERTA proposals grouped by category). */
    public void mostraBacheca()
    {
        ui.header("BACHECA");
        ui.mostraBacheca(ViewModelMapper.toBachecaVM(ps.getBachecaPerCategoria()));
        ui.newLine();
        ui.pausa();
    }

    // ---------------------------------------------------------------
    // PRIVATE WORKFLOW HELPERS
    // ---------------------------------------------------------------

    /**
     * Runs the validation-and-correction loop until the proposal is valid or the user aborts.
     *
     * @return {@code true} if the user chose to discard the proposal, {@code false} if it is valid
     */
    private boolean correggiFinchéValida(Proposta proposta, List<String> errori)
    {
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
                return true;
            }

            Set<String> nomiConErrore = ps.getCampiConErrore(proposta, errori).stream()
                    .map(Campo::getNome)
                    .collect(Collectors.toSet());

            List<FormField> corrFields = PropostaFormBuilder.build(proposta).stream()
                    .filter(f -> nomiConErrore.contains(f.getName()))
                    .collect(Collectors.toList());

            Optional<Map<String, String>> corrResult = ui.runForm(corrFields);
            if (corrResult.isEmpty())
            {
                ui.stampa("Proposta scartata.");
                ui.newLine();
                ui.pausa();
                return true;
            }

            proposta.putAllValoriCampi(corrResult.get());
            errori = ps.validaProposta(proposta);
        }
        return false;
    }

    /** Shows the proposal summary and saves it in memory for later publication. */
    private void mostraRiepilogoEPubblica(Proposta proposta)
    {
        ui.newLine();
        ui.mostraRiepilogoProposta(ViewModelMapper.toPropostaVM(proposta));

        ps.salvaProposta(proposta);
        ui.stampaSuccesso("Proposta valida salvata. Puoi pubblicarla dal menu 'Pubblicare una proposta di iniziativa'.");

        ui.newLine();
        ui.pausa();
    }

    /**
     * Lists saved valid proposals, lets the user select one, and publishes it.
     */
    public void pubblicaPropostaSalvata()
    {
        ui.header("PUBBLICA PROPOSTA");

        List<Proposta> valide = ps.getProposteValide();
        if (valide.isEmpty())
        {
            ui.stampa("Nessuna proposta valida da pubblicare.");
            ui.newLine();
            ui.pausa();
            return;
        }

        // Build display labels from the saved proposals
        List<String> labels = new java.util.ArrayList<>();
        for (int i = 0; i < valide.size(); i++)
        {
            Proposta p = valide.get(i);
            String titolo = p.getValoriCampi().getOrDefault("Titolo", "(senza titolo)");
            String cat    = p.getCategoria().getNome();
            labels.add((i + 1) + ". " + titolo + "  [" + cat + "]");
        }

        ui.stampa("Proposte valide disponibili:");
        for (String label : labels)
            ui.stampa("  " + label);
        ui.stampa("  0. Torna");
        ui.newLine();

        int scelta = ui.acquisisciIntero("Scelta: ", 0, valide.size());
        if (scelta == 0)
            return;

        Proposta selezionata = valide.get(scelta - 1);

        ui.newLine();
        ui.mostraRiepilogoProposta(ViewModelMapper.toPropostaVM(selezionata));

        if (ui.acquisisciSiNo("Vuoi pubblicare questa proposta in bacheca?"))
        {
            try
            {
                ps.pubblicaProposta(selezionata);
                ps.rimuoviPropostaValida(selezionata);
                ui.stampaSuccesso("Proposta pubblicata in bacheca!");
            }
            catch (Exception e)
            {
                ui.stampaErrore(e.getMessage());
            }
        }
        else
        {
            ui.stampa("Pubblicazione annullata. La proposta resta disponibile per la pubblicazione.");
        }

        ui.newLine();
        ui.pausa();
    }
}
