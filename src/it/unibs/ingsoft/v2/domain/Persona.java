package it.unibs.ingsoft.v2.domain;

import java.io.Serializable;

/**
 * Abstract base class for all application users.
 */
public abstract class Persona implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final String username;

    protected Persona(String username)
    {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Lo username non può essere vuoto.");
        this.username = username.trim();
    }

    public String getUsername() { return username; }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!(obj instanceof Persona)) return false;
        return username.equals(((Persona) obj).username);
    }

    @Override
    public int hashCode()
    {
        return username.hashCode();
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "[" + username + "]";
    }
}
