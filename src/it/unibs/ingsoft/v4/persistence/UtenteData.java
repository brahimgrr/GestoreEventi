package it.unibs.ingsoft.v4.persistence;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Pure serializable DTO holding configuratore credentials.
 */
public final class UtenteData implements Serializable
{
    private static final long serialVersionUID = 1L;

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
