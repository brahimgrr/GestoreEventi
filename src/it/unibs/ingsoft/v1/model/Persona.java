package it.unibs.ingsoft.v1.model;

import java.io.Serializable;
import java.util.Objects;

public abstract class Persona implements Serializable
{
    private static final long serialVersionUID = 1L;
    private final String username;

    /**
     * @pre  username != null &amp;&amp; !username.isBlank()
     * @post getUsername().equals(username.trim())
     * @post !getUsername().isBlank()
     * @throws IllegalArgumentException if username is null or blank
     */
    protected Persona(String username)
    {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Username non valido.");

        this.username = username.trim();

        // Class invariant: username must never be blank after construction
        if (this.username.isBlank())
            throw new IllegalStateException("Invariant violated: username must not be blank after construction.");
    }

    public String getUsername()
    {
        return username;
    }

    @Override
    public final boolean equals(Object o)
    {
        if (this == o)
            return true;

        if (o == null || this.getClass() != o.getClass())
            return false;

        Persona persona = (Persona) o;
        return username.equals(persona.username);
    }

    @Override
    public final int hashCode()
    {
        return Objects.hash(username);
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "{username='" + username + "'}";
    }
}