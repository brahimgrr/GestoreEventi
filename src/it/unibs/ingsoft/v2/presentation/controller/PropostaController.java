package it.unibs.ingsoft.v2.presentation.controller;

import it.unibs.ingsoft.v2.application.PropostaService;
import it.unibs.ingsoft.v2.domain.Campo;
import it.unibs.ingsoft.v2.domain.Proposta;
import it.unibs.ingsoft.v2.presentation.view.cli.FormField;
import it.unibs.ingsoft.v2.presentation.view.cli.PropostaFormBuilder;
import it.unibs.ingsoft.v2.presentation.view.cli.viewmodel.ViewModelMapper;
import it.unibs.ingsoft.v2.presentation.view.contract.IAppView;

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
 * {@link #avviaCreazione(String)}.</p>
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
     *
     * @param nomeCategoria the category already selected by the caller
     */
    public void avviaCreazione(String nomeCategoria)
    {
        ui.header("CREA PROPOSTA");

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

        Optional<Map<String, String>> formResult = ui.runForm(PropostaFormBuilder.build(proposta, ps));
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
        ui.mostraBacheca(ViewModelMapper.toBachecaVM(ps.getBachecaPerCategoria(), ps::getTuttiCampi));
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

            List<FormField> corrFields = PropostaFormBuilder.build(proposta, ps).stream()
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

    /** Shows the proposal summary and asks whether to publish it. */
    private void mostraRiepilogoEPubblica(Proposta proposta)
    {
        ui.newLine();
        ui.mostraRiepilogoProposta(ViewModelMapper.toPropostaVM(proposta, ps.getTuttiCampi(proposta)));

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

        ui.newLine();
        ui.pausa();
    }
}
