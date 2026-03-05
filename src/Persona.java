//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import java.io.Serializable;
import java.util.Objects;

public abstract class Persona implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String username;

    protected Persona(String username) {
        if (username != null && !username.isBlank()) {
            this.username = username.trim();
        } else {
            throw new IllegalArgumentException("Username non valido.");
        }
    }

    public String getUsername() {
        return this.username;
    }

    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            Persona persona = (Persona)o;
            return this.username.equals(persona.username);
        } else {
            return false;
        }
    }

    public final int hashCode() {
        return Objects.hash(new Object[]{this.username});
    }

    public String toString() {
        String var10000 = this.getClass().getSimpleName();
        return var10000 + "{username='" + this.username + "'}";
    }
}
