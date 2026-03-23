package it.unibs.ingsoft.v2.presentation.controller;

import it.unibs.ingsoft.v2.application.AuthenticationService;
import it.unibs.ingsoft.v2.domain.Configuratore;
import it.unibs.ingsoft.v2.presentation.view.contract.IAppView;
import it.unibs.ingsoft.v2.presentation.view.contract.OperationCancelledException;

/**
 * Handles configuratore login and first-time credential setup.
 * Contains no business logic — only UI interaction for authentication.
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
     * Loops until a successful login is performed.
     * If default credentials are used, forces the user to register personal ones
     * with field-level inline validation and re-prompt (only the failed field is re-asked).
     *
     * @return the authenticated Configuratore
     */
    public Configuratore loginConfiguratore()
    {
        while (true)
        {
            ui.newLine();
            ui.stampa("LOGIN CONFIGURATORE");
            String u = ui.acquisisciStringa("Username: ");
            String p = ui.acquisisciStringa("Password: ");

            var result = auth.login(u, p);

            if (result.isEmpty())
            {
                ui.stampaErrore("Credenziali non valide. Riprova.");
                ui.newLine();
                continue;
            }

            Configuratore logged = result.get();
            ui.stampaSuccesso("Login riuscito.");

            // First login with default credentials — force personal registration
            if (AuthenticationService.USERNAME_PREDEFINITO.equals(logged.getUsername()))
            {
                ui.newLine();
                ui.stampa("Primo accesso con credenziali predefinite.");
                ui.stampa("Scegli le tue credenziali personali.");
                try
                {
                    Configuratore registered = registrazioneInterattiva();
                    ui.newLine();
                    return registered;
                }
                catch (OperationCancelledException e)
                {
                    ui.stampaInfo("Registrazione annullata. Effettua nuovamente il login.");
                    ui.newLine();
                    continue;
                }
            }

            ui.newLine();
            return logged;
        }
    }

    /**
     * Guides the user through first-time credential registration with field-level re-prompts.
     * Throws {@link OperationCancelledException} if the user aborts.
     */
    private Configuratore registrazioneInterattiva()
    {
        // Show all constraints upfront before starting the form
        ui.stampaInfo("Username: minimo 3 caratteri, non può essere '" +
                      AuthenticationService.USERNAME_PREDEFINITO + "'.");
        ui.stampaInfo("Password: minimo 4 caratteri.");
        ui.stampaInfo(IAppView.HINT_ANNULLA);
        ui.newLine();

        while (true)
        {
            String newU = raccogliUsername();
            String newP = raccogliPassword();

            try
            {
                if (!ui.acquisisciSiNo("Confermi la registrazione con username \"" + newU + "\"?"))
                {
                    ui.stampaInfo("Registrazione non confermata. Inserisci nuovamente i dati.");
                    ui.newLine();
                    continue;
                }
                Configuratore registered = auth.registraNuovoConfiguratore(newU, newP);
                ui.stampaSuccesso("Registrazione completata. Benvenuto, " + newU + "!");
                return registered;
            }
            catch (IllegalArgumentException e)
            {
                ui.stampaErrore(e.getMessage());
                ui.newLine();
            }
        }
    }

    /** Inline-validated username acquisition — only re-asks this field on error. */
    private String raccogliUsername()
    {
        while (true)
        {
            String newU = ui.acquisisciStringaConValidazione(
                    "Nuovo username: ",
                    u -> !u.isBlank() && u.trim().length() >= 3,
                    "Username troppo corto (minimo 3 caratteri)."
            ).trim();

            if (newU.equalsIgnoreCase(AuthenticationService.USERNAME_PREDEFINITO))
            {
                ui.stampaErrore("Username riservato. Scegli un nome diverso.");
                continue;
            }

            if (auth.esisteUsername(newU))
            {
                ui.stampaErrore("Username già in uso. Scegli un nome diverso.");
                continue;
            }

            return newU;
        }
    }

    /** Inline-validated password acquisition — only re-asks this field on error. */
    private String raccogliPassword()
    {
        while (true)
        {
            String newP = ui.acquisisciPassword("Nuova password: ");

            if (newP == null || newP.isBlank() || newP.trim().length() < 4)
            {
                ui.stampaErrore("Password troppo corta (minimo 4 caratteri).");
                continue;
            }

            return newP;
        }
    }
}
