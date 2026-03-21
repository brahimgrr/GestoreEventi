package it.unibs.ingsoft.v3.persistence.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

/**
 * JSON-serializable DTO for all user credentials (configuratori and fruitori).
 * Serialises to/from a single JSON file with two maps:
 * <pre>
 * {
 *   "configuratori": { "alice": "s3cr3t" },
 *   "fruitori":      { "bob":   "p4ss"  }
 * }
 * </pre>
 */
public final class UsersData
{
    private final Map<String, String> configuratori = new HashMap<>();
    private final Map<String, String> fruitori      = new HashMap<>();

    public UsersData() {}

    /** Jackson deserialisation factory. */
    @JsonCreator
    public static UsersData fromJson(@JsonProperty("configuratori") Map<String, String> configuratori,
                                     @JsonProperty("fruitori")      Map<String, String> fruitori)
    {
        UsersData d = new UsersData();
        if (configuratori != null) configuratori.forEach(d::addConfiguratore);
        if (fruitori      != null) fruitori     .forEach(d::addFruitore);
        return d;
    }

    public Map<String, String> getConfiguratori()
    {
        return Collections.unmodifiableMap(configuratori);
    }

    public void addConfiguratore(String username, String password)
    {
        configuratori.put(username, password);
    }

    public Map<String, String> getFruitori()
    {
        return Collections.unmodifiableMap(fruitori);
    }

    public void addFruitore(String username, String password)
    {
        fruitori.put(username, password);
    }
}
