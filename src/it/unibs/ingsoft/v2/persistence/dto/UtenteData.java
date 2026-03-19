package it.unibs.ingsoft.v2.persistence.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * JSON-serializable snapshot of configurator credentials (username → password).
 */
public final class UtenteData
{
    private final Map<String, String> configuratori;

    public UtenteData()
    {
        this.configuratori = new HashMap<>();
    }

    /** Jackson deserialisation factory. */
    @JsonCreator
    public static UtenteData fromJson(
            @JsonProperty("configuratori") Map<String, String> configuratori)
    {
        UtenteData d = new UtenteData();
        if (configuratori != null) d.configuratori.putAll(configuratori);
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
}
