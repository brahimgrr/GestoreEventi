package it.unibs.ingsoft.v2.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Pure DTO for user credentials.
 * Security is explicitly out-of-scope per project spec.
 */
public final class Credenziali {
    private final Map<String, String> configuratori;

    public Credenziali() {
        this.configuratori = new HashMap<>();
    }

    /** Jackson deserialisation factory. */
    @JsonCreator
    public static Credenziali fromJson(
            @JsonProperty("configuratori") Map<String, String> configuratori)
    {
        Credenziali d = new Credenziali();
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
