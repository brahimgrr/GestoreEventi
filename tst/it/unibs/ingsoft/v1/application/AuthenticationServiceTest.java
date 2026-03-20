package it.unibs.ingsoft.v1.application;

import it.unibs.ingsoft.v1.domain.Configuratore;
import it.unibs.ingsoft.v1.persistence.api.IUtenteRepository;
import it.unibs.ingsoft.v1.persistence.dto.UtenteData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V1 – AuthenticationService")
class AuthenticationServiceTest
{
    private UtenteData utenti;
    private AuthenticationService auth;

    @BeforeEach
    void setUp()
    {
        utenti = new UtenteData();
        IUtenteRepository mockRepo = new IUtenteRepository()
        {
            @Override public UtenteData load()       { return utenti; }
            @Override public void save(UtenteData d) {}
        };
        auth = new AuthenticationService(mockRepo, utenti);
    }

    // ───────────────────── Login ─────────────────────

    @Test
    @DisplayName("Login with default credentials returns Configuratore")
    void login_defaultCredentials_returnsConfiguratore()
    {
        Optional<Configuratore> result = auth.login(
                AuthenticationService.USERNAME_PREDEFINITO,
                AuthenticationService.PASSWORD_PREDEFINITA);
        assertTrue(result.isPresent());
        assertEquals(AuthenticationService.USERNAME_PREDEFINITO, result.get().getUsername());
    }

    @Test
    @DisplayName("Login with saved credentials returns Configuratore")
    void login_savedCredentials_returnsConfiguratore()
    {
        auth.registraNuovoConfiguratore("mario", "pass1234");
        Optional<Configuratore> result = auth.login("mario", "pass1234");
        assertTrue(result.isPresent());
        assertEquals("mario", result.get().getUsername());
    }

    @Test
    @DisplayName("Login with wrong password returns empty")
    void login_wrongPassword_returnsEmpty()
    {
        auth.registraNuovoConfiguratore("mario", "pass1234");
        assertTrue(auth.login("mario", "wrongpass").isEmpty());
    }

    @Test
    @DisplayName("Login with unknown username returns empty")
    void login_unknownUsername_returnsEmpty()
    {
        assertTrue(auth.login("sconosciuto", "pass1234").isEmpty());
    }

    @Test
    @DisplayName("Login with null username returns empty")
    void login_nullUsername_returnsEmpty()
    {
        assertTrue(auth.login(null, "pass1234").isEmpty());
    }

    @Test
    @DisplayName("Login with null password returns empty")
    void login_nullPassword_returnsEmpty()
    {
        assertTrue(auth.login("mario", null).isEmpty());
    }

    @Test
    @DisplayName("Login is case-insensitive on username")
    void login_caseInsensitiveUsername()
    {
        auth.registraNuovoConfiguratore("mario", "pass1234");
        assertTrue(auth.login("MARIO", "pass1234").isPresent());
    }

    // ───────────────────── Registration ─────────────────────

    @Test
    @DisplayName("Register new configurator with valid credentials")
    void registra_validCredentials_success()
    {
        Configuratore c = auth.registraNuovoConfiguratore("mario", "pass1234");
        assertNotNull(c);
        assertEquals("mario", c.getUsername());
    }

    @Test
    @DisplayName("Register with too short username throws")
    void registra_shortUsername_throwsException()
    {
        assertThrows(IllegalArgumentException.class,
                () -> auth.registraNuovoConfiguratore("ab", "pass1234"));
    }

    @Test
    @DisplayName("Register with too short password throws")
    void registra_shortPassword_throwsException()
    {
        assertThrows(IllegalArgumentException.class,
                () -> auth.registraNuovoConfiguratore("mario", "pas"));
    }

    @Test
    @DisplayName("Register with reserved username throws")
    void registra_reservedUsername_throwsException()
    {
        assertThrows(IllegalArgumentException.class,
                () -> auth.registraNuovoConfiguratore(
                        AuthenticationService.USERNAME_PREDEFINITO, "pass1234"));
    }

    @Test
    @DisplayName("Register with duplicate username throws")
    void registra_duplicateUsername_throwsException()
    {
        auth.registraNuovoConfiguratore("mario", "pass1234");
        assertThrows(IllegalArgumentException.class,
                () -> auth.registraNuovoConfiguratore("mario", "pass5678"));
    }

    @Test
    @DisplayName("Register with null username throws")
    void registra_nullUsername_throwsException()
    {
        assertThrows(IllegalArgumentException.class,
                () -> auth.registraNuovoConfiguratore(null, "pass1234"));
    }

    @Test
    @DisplayName("Register with blank password throws")
    void registra_blankPassword_throwsException()
    {
        assertThrows(IllegalArgumentException.class,
                () -> auth.registraNuovoConfiguratore("mario", "   "));
    }

    // ───────────────────── esistonoConfiguratori ─────────────────────

    @Test
    @DisplayName("esistonoConfiguratori returns false when empty")
    void esistonoConfiguratori_empty_returnsFalse()
    {
        assertFalse(auth.esistonoConfiguratori());
    }

    @Test
    @DisplayName("esistonoConfiguratori returns true after registration")
    void esistonoConfiguratori_afterRegistration_returnsTrue()
    {
        auth.registraNuovoConfiguratore("mario", "pass1234");
        assertTrue(auth.esistonoConfiguratori());
    }

    // ───────────────────── esisteUsername ─────────────────────

    @Test
    @DisplayName("esisteUsername is case-insensitive")
    void esisteUsername_caseInsensitive()
    {
        auth.registraNuovoConfiguratore("mario", "pass1234");
        assertTrue(auth.esisteUsername("MARIO"));
        assertTrue(auth.esisteUsername("Mario"));
    }

    @Test
    @DisplayName("esisteUsername with null returns false")
    void esisteUsername_null_returnsFalse()
    {
        assertFalse(auth.esisteUsername(null));
    }
}
