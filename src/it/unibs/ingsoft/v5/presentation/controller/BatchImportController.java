package it.unibs.ingsoft.v5.presentation.controller;

import it.unibs.ingsoft.v5.application.batch.BatchImportService;
import it.unibs.ingsoft.v5.application.batch.ImportResult;
import it.unibs.ingsoft.v5.presentation.view.contract.IAppView;
import it.unibs.ingsoft.v5.presentation.view.contract.OperationCancelledException;

import java.io.IOException;
import java.nio.file.Path;

/**
 * UI controller for the batch-import workflow.
 * Prompts the Configuratore for a file path, delegates to {@link BatchImportService},
 * and displays the import results.
 */
public final class BatchImportController {

    private final IAppView ui;
    private final BatchImportService importService;

    public BatchImportController(IAppView ui, BatchImportService importService) {
        this.ui            = ui;
        this.importService = importService;
    }

    /**
     * Runs the batch-import workflow: prompts for a file path, imports, and shows results.
     */
    public void avviaImportazione() {
        ui.header("IMPORTAZIONE BATCH");
        ui.stampaInfo("Importa categorie, campi comuni e proposte da un file JSON.");
        ui.stampaInfo(IAppView.HINT_ANNULLA);
        ui.newLine();

        try {
            String percorso = ui.acquisisciStringa("Percorso del file JSON: ");
            if (percorso == null || percorso.isBlank()) {
                ui.stampaErrore("Percorso non valido.");
                ui.pausaConSpaziatura();
                return;
            }

            Path filePath = Path.of(percorso.trim());

            ui.stampa("Importazione in corso...");
            ui.newLine();

            ImportResult result = importService.importa(filePath);

            mostraRisultato(result);

        } catch (OperationCancelledException e) {
            ui.stampaInfo("Operazione annullata.");
        } catch (IOException e) {
            ui.stampaErrore("Errore durante l'importazione: " + e.getMessage());
        }

        ui.pausaConSpaziatura();
    }

    private void mostraRisultato(ImportResult result) {
        ui.stampaSezione("RISULTATO IMPORTAZIONE");

        ui.stampa("  Campi comuni importati: " + result.getCampiComuniImportati());
        ui.stampa("  Categorie importate:    " + result.getCategorieImportate());
        ui.stampa("  Proposte importate:     " + result.getProposteImportate());
        ui.newLine();

        if (result.hasErrors()) {
            ui.stampaAvviso("Errori riscontrati (" + result.getErrori().size() + "):");
            for (String errore : result.getErrori())
                ui.stampaErrore(errore);
            ui.newLine();
        }

        if (result.totaleImportati() == 0 && !result.hasErrors()) {
            ui.stampaInfo("Il file non contiene dati da importare.");
        } else if (result.totaleImportati() > 0) {
            ui.stampaSuccesso("Importazione completata: " + result.totaleImportati() + " elementi importati.");
            if (result.getProposteImportate() > 0) {
                ui.stampaInfo("Le proposte valide possono essere pubblicate dal menu 'Pubblicare una proposta di iniziativa'.");
            }
        }
    }
}
