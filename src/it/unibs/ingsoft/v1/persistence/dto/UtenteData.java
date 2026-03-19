package it.unibs.ingsoft.v1.persistence.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

/**
 * Pure DTO for user credentials.
 * Security is explicitly out-of-scope per project spec.
 */
public final class UtenteData
{
    private final Map<String, String> configuratori = new HashMap<>();

    public UtenteData() {}

    /** Jackson deserialization factory. */
    @JsonCreator
    public static UtenteData fromJson(
            @JsonProperty("configuratori") Map<String, String> configuratori)
    {
        UtenteData d = new UtenteData();
        if (configuratori != null)
            d.configuratori.putAll(configuratori);
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
