package it.unibs.ingsoft.v5.presentation.controller;

import it.unibs.ingsoft.v5.application.NotificationService;
import it.unibs.ingsoft.v5.domain.Fruitore;
import it.unibs.ingsoft.v5.domain.Notifica;
import it.unibs.ingsoft.v5.presentation.view.contract.IAppView;

import java.time.format.DateTimeFormatter;
import java.util.List;

public final class SpazioPersonaleController {
    
    private final Fruitore fruitore;
    private final IAppView ui;
    private final NotificationService notificationService;

    public SpazioPersonaleController(Fruitore fruitore, IAppView ui, NotificationService notificationService) {
        this.fruitore = fruitore;
        this.ui = ui;
        this.notificationService = notificationService;
    }

    public void run() {
        while (true) {
            ui.header("SPAZIO PERSONALE DI " + fruitore.getUsername());
            
            List<Notifica> notifiche = notificationService.getNotifiche(fruitore.getUsername());
            
            if (notifiche.isEmpty()) {
                ui.stampa("Nessuna notifica presente.");
                ui.newLine();
                ui.pausa();
                return;
            }

            for (int i = 0; i < notifiche.size(); i++) {
                Notifica n = notifiche.get(i);
                ui.stampa((i + 1) + ". [" + n.getDataCreazione()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "] " + n.getMessaggio());
            }
            ui.newLine();

            ui.stampa("Digita il numero di una notifica per eliminarla (0 per tornare indietro).");
            int choice = ui.acquisisciIntero("Scelta: ", 0, notifiche.size());

            if (choice == 0) {
                return;
            }

            Notifica daEliminare = notifiche.get(choice - 1);
            if (ui.acquisisciSiNo("Confermi l'eliminazione di questa notifica?")) {
                notificationService.cancellaNotifica(fruitore.getUsername(), daEliminare);
                ui.stampaSuccesso("Notifica eliminata.");
                ui.newLine();
            }
        }
    }
}
