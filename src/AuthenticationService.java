//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.util.Objects;

public final class AuthenticationService {
    public static final String USERNAME_PREDEFINITO = "config";
    public static final String PASSWORD_PREDEFINITA = "config";
    private final DatabaseService db;
    private final AppData data;

    public AuthenticationService(DatabaseService db, AppData data) {
        this.db = (DatabaseService)Objects.requireNonNull(db);
        this.data = (AppData)Objects.requireNonNull(data);
    }

    public Configuratore login(String username, String password) {
        if (username != null && password != null) {
            if (username.equals("config") && password.equals("config")) {
                return new Configuratore("config");
            } else {
                String salvato = (String)this.data.getConfiguratori().get(username);
                return salvato != null && salvato.equals(password) ? new Configuratore(username) : null;
            }
        } else {
            return null;
        }
    }

    public Configuratore registraNuovoConfiguratore(String newUsername, String newPassword) {
        validaCredenziali(newUsername, newPassword);
        if (this.data.getConfiguratori().containsKey(newUsername)) {
            throw new IllegalArgumentException("Username già esistente.");
        } else if ("config".equalsIgnoreCase(newUsername)) {
            throw new IllegalArgumentException("Username non consentito (riservato).");
        } else {
            this.data.getConfiguratori().put(newUsername, newPassword);
            this.db.save(this.data);
            return new Configuratore(newUsername);
        }
    }

    public boolean esistonoConfiguratori() {
        return !this.data.getConfiguratori().isEmpty();
    }

    private static void validaCredenziali(String username, String password) {
        if (username != null && !username.isBlank()) {
            if (password != null && !password.isBlank()) {
                if (username.trim().length() < 3) {
                    throw new IllegalArgumentException("Username troppo corto (min 3 caratteri).");
                } else if (password.trim().length() < 4) {
                    throw new IllegalArgumentException("Password troppo corta (min 4 caratteri).");
                }
            } else {
                throw new IllegalArgumentException("Password non valida.");
            }
        } else {
            throw new IllegalArgumentException("Username non valido.");
        }
    }
}
