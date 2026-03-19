package it.unibs.ingsoft.v3.persistence;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * Pure serializable DTO for registered configurator credentials.
 */
public final class UtenteData implements Serializable
{
    @Serial private static final long serialVersionUID = 1L;

    private final Map<String, String> configuratori = new HashMap<>();

    public Map<String, String> getConfiguratori()
    {
        return Collections.unmodifiableMap(configuratori);
    }

    public void addConfiguratore(String username, String password)
    {
        configuratori.put(username, password);
    }
}
