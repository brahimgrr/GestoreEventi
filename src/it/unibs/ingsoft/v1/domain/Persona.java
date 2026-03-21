package it.unibs.ingsoft.v1.domain;

/**
 * Abstract base class for all application users.
 */
public abstract class Persona
{
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
            throw new IllegalArgumentException("Lo username non può essere vuoto.");
        this.username = username.trim();
    }

    public String getUsername() {
        return username;
    }

    /**
     * Two {@code Persona} instances are equal iff they are of the same concrete type
     * and have the same username. This uses {@code getClass()} comparison (not {@code instanceof})
     * intentionally: a {@code Configuratore} and a {@code Fruitore} with the same username
     * are considered distinct entities, which is correct since they represent different user roles.
     */
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
        return username.hashCode();
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "[" + username + "]";
    }
}
