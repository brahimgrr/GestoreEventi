package it.unibs.ingsoft.v5.persistence;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Pure serializable DTO holding fruitore credentials.
 */
public final class FruitoreData implements Serializable
{
    private static final long serialVersionUID = 1L;

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
