package it.unibs.ingsoft.v2.controller;

import it.unibs.ingsoft.v2.model.Configuratore;
import it.unibs.ingsoft.v2.service.AuthenticationService;
import it.unibs.ingsoft.v2.view.IAppView;

/**
 * Handles configuratore login and first-time credential setup.
 */
public final class AuthController
{
    private final IAppView ui;
    private final AuthenticationService auth;

    public AuthController(IAppView ui, AuthenticationService auth)
    {
        this.ui   = ui;
        this.auth = auth;
    }

    /**
     * Loops until a successful login is performed.
     * Forces personal credential registration on first access.
     */
    public Configuratore loginConfiguratore()
    {
        while (true)
        {
            ui.stampa("LOGIN CONFIGURATORE");
            String u = ui.acquisisciStringa("Username: ").trim();
            String p = ui.acquisisciStringa("Password: ").trim();
            Configuratore opt = auth.login(u, p);

            if (opt == null)
            {
                ui.stampa("Credenziali non valide.");
                ui.newLine();
                continue;
            }

            ui.stampa("Login riuscito.");

            if (AuthenticationService.USERNAME_PREDEFINITO.equals(opt.getUsername()))
            {
                ui.newLine();
                ui.stampa("Primo accesso con credenziali predefinite.");
                ui.stampa("Devi scegliere credenziali personali.");

                while (true)
                {
                    String newU = ui.acquisisciStringa("Nuovo username: ").trim();
                    String newP = ui.acquisisciStringa("Nuova password: ").trim();
                    try
                    {
                        Configuratore registered = auth.registraNuovoConfiguratore(newU, newP);
                        ui.stampa("Registrazione completata.");
                        ui.newLine();
                        return registered;
                    }
                    catch (IllegalArgumentException e)
                    {
                        ui.stampa("Errore: " + e.getMessage());
                    }
                }
            }

            ui.newLine();
            return opt;
        }
    }
}
