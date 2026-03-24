package it.unibs.ingsoft.v4.presentation.controller;

import it.unibs.ingsoft.v4.application.IscrizioneService;
import it.unibs.ingsoft.v4.application.PropostaService;
import it.unibs.ingsoft.v4.domain.Fruitore;
import it.unibs.ingsoft.v4.domain.Proposta;
import it.unibs.ingsoft.v4.presentation.view.contract.IAppView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class FruitoreController {
    private static final String[] MENU_PRINCIPALE = {
        "Visualizza bacheca (per categoria)",
        "Disdici iscrizione a una proposta",
        "Spazio Personale (Notifiche)",
    };

    private final Fruitore fruitore;
    private final IAppView ui;
    private final PropostaService propostaService;
    private final IscrizioneService iscrizioneService;
    private final SpazioPersonaleController spazioPersonaleController;

    public FruitoreController(Fruitore fruitore, IAppView ui, PropostaService propostaService,
                              IscrizioneService iscrizioneService, SpazioPersonaleController spazioPersonaleController) {
        this.fruitore = fruitore;
        this.ui = ui;
        this.propostaService = propostaService;
        this.iscrizioneService = iscrizioneService;
        this.spazioPersonaleController = spazioPersonaleController;
    }

    public void run() {
        while (true) {
            ui.stampaMenu("MENU PRINCIPALE ALBO (FRUITORE)", MENU_PRINCIPALE, "Logout");
            int choice = ui.acquisisciIntero("Scelta: ", 0, MENU_PRINCIPALE.length);
            ui.newLine();

            switch (choice) {
                case 1:
                    mostraBachecaEiscrizione();
                    break;
                case 2:
                    disdiciIscrizione();
                    break;
                case 3:
                    spazioPersonaleController.run();
                    break;
                case 0:
                    return;
            }
        }
    }

    private void mostraBachecaEiscrizione() {
        Map<String, List<Proposta>> bachecaPerCategoria = propostaService.getBachecaPerCategoria();
        
        if (bachecaPerCategoria.isEmpty()) {
            ui.stampa("La bacheca è vuota. Nessuna proposta aperta al momento.");
            ui.pausaConSpaziatura();
            return;
        }

        ui.header("BACHECA PROPOSTE");
        
        // Costruisci lista lineare per la selezione
        List<Proposta> proposteSelezionabili = new java.util.ArrayList<>();
        int indice = 1;

        for (Map.Entry<String, List<Proposta>> entry : bachecaPerCategoria.entrySet()) {
            ui.stampaSezione("Categoria: " + entry.getKey());
            for (Proposta p : entry.getValue()) {
                proposteSelezionabili.add(p);
                String titolo = p.getValoriCampi().getOrDefault(PropostaService.CAMPO_TITOLO, "Senza Titolo");
                String liberi = (p.getNumeroPartecipanti() - p.getListaAderenti().size()) + " posti liberi";
                ui.stampa(String.format(" %d) %s (Scadenza: %s, %s)", 
                        indice++, 
                        titolo, 
                        p.getTermineIscrizione(),
                        liberi));
            }
            ui.newLine();
        }

        ui.stampa("Digita il numero della proposta per i dettagli o per iscriverti (0 per tornare indietro).");
        int subChoice = ui.acquisisciIntero("Scelta: ", 0, proposteSelezionabili.size());
        
        if (subChoice == 0) return;

        Proposta selezionata = proposteSelezionabili.get(subChoice - 1);
        dettagliEIscrizione(selezionata);
    }

    private void disdiciIscrizione() {
        List<Proposta> mieProposte = new ArrayList<>();
        for (Proposta p : propostaService.getBacheca()) {
            if (p.getListaAderenti().contains(fruitore.getUsername())) {
                mieProposte.add(p);
            }
        }

        if (mieProposte.isEmpty()) {
            ui.stampa("Non sei iscritto a nessuna proposta aperta.");
            ui.pausaConSpaziatura();
            return;
        }

        ui.header("DISDICI ISCRIZIONE");
        int indice = 1;
        for (Proposta p : mieProposte) {
            String titolo = p.getValoriCampi().getOrDefault(PropostaService.CAMPO_TITOLO, "Senza Titolo");
            ui.stampa(String.format(" %d) %s (Scadenza: %s)", indice++, titolo, p.getTermineIscrizione()));
        }
        ui.newLine();

        ui.stampa("Seleziona la proposta da cui disdire l'iscrizione (0 per tornare indietro).");
        int subChoice = ui.acquisisciIntero("Scelta: ", 0, mieProposte.size());

        if (subChoice == 0) return;

        Proposta selezionata = mieProposte.get(subChoice - 1);
        ui.mostraRiepilogoProposta(selezionata);
        ui.newLine();

        if (ui.acquisisciSiNo("Vuoi davvero disdire l'iscrizione a questa proposta?")) {
            try {
                iscrizioneService.disiscrivi(selezionata, fruitore);
                ui.stampaSuccesso("Iscrizione disdetta con successo.");
            } catch (IllegalStateException e) {
                ui.stampaErrore("Errore durante la disiscrizione: " + e.getMessage());
            }
        }
        ui.pausaConSpaziatura();
    }

    private void dettagliEIscrizione(Proposta p) {
        ui.header("DETTAGLI PROPOSTA");
        ui.mostraRiepilogoProposta(p);

        ui.newLine();
        
        if (p.getListaAderenti().contains(fruitore.getUsername())) {
            ui.stampaAvviso("Sei già iscritto a questa proposta.");
            ui.pausaConSpaziatura();
            return;
        }

        int postiDisponibili = p.getNumeroPartecipanti() - p.getListaAderenti().size();
        if (postiDisponibili <= 0) {
            ui.stampaAvviso("Questa proposta ha raggiunto il numero massimo di partecipanti.");
            ui.pausaConSpaziatura();
            return;
        }

        if (ui.acquisisciSiNo("Vuoi iscriverti a questa proposta?")) {
            try {
                iscrizioneService.iscrivi(p, fruitore);
                ui.stampaSuccesso("Iscrizione effettuata con successo!");
            } catch (IllegalStateException e) {
                ui.stampaErrore("Errore durante l'iscrizione: " + e.getMessage());
            }
        }
        ui.pausaConSpaziatura();
    }
}
