package it.unibs.ingsoft.v3.presentation.controller;

import it.unibs.ingsoft.v3.domain.Configuratore;
import it.unibs.ingsoft.v3.domain.Fruitore;
import it.unibs.ingsoft.v3.application.AuthenticationService;
import it.unibs.ingsoft.v3.presentation.view.contract.IAppView;

/**
 * Handles login and registration for both configuratori and fruitori.
 */
public final class AuthController
{
    private final IAppView              ui;
    private final AuthenticationService auth;

    public AuthController(IAppView ui, AuthenticationService auth)
    {
        this.ui   = ui;
        this.auth = auth;
    }

    /**
     * Loops until a successful configuratore login.
     * Forces personal credential registration on first access.
     */
    public Configuratore loginConfiguratore()
    {
        while (true)
        {
            ui.stampa("LOGIN CONFIGURATORE");
            String u = ui.acquisisciStringa("Username: ").trim();
            String p = ui.acquisisciPassword("Password: ").trim();
            Configuratore opt = auth.loginConfiguratore(u, p);

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
                    String newP = ui.acquisisciPassword("Nuova password: ").trim();
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

    /**
     * Shows login/registration for fruitori.
     *
     * @return the authenticated Fruitore, or null if the user chose to go back
     */
    public Fruitore loginFruitore()
    {
        while (true)
        {
            ui.stampa("LOGIN / REGISTRAZIONE FRUITORE");
            ui.stampa("1) Login");
            ui.stampa("2) Registrati");
            ui.stampa("0) Torna indietro");
            int scelta = ui.acquisisciIntero("Scelta: ", 0, 2);

            if (scelta == 0)
                return null;

            String u = ui.acquisisciStringa("Username: ").trim();
            String p = ui.acquisisciPassword("Password: ").trim();

            if (scelta == 1)
            {
                Fruitore f = auth.loginFruitore(u, p);

                if (f != null)
                {
                    ui.stampa("Login riuscito.");
                    ui.newLine();
                    return f;
                }

                ui.stampa("Credenziali non valide.");
                ui.newLine();
            }
            else
            {
                try
                {
                    Fruitore f = auth.registraFruitore(u, p);
                    ui.stampa("Registrazione completata. Benvenuto!");
                    ui.newLine();
                    return f;
                }
                catch (IllegalArgumentException e)
                {
                    ui.stampa("Errore: " + e.getMessage());
                    ui.newLine();
                }
            }
        }
    }
}
