package it.unibs.ingsoft.v3.persistence;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * Pure serializable DTO for registered fruitore credentials.
 */
public final class FruitoreData implements Serializable
{
    @Serial private static final long serialVersionUID = 1L;

    private final Map<String, String> fruitori = new HashMap<>();

    public Map<String, String> getFruitori()
    {
        return Collections.unmodifiableMap(fruitori);
    }

    public void addFruitore(String username, String password)
    {
        fruitori.put(username, password);
    }
}
