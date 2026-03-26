package it.unibs.ingsoft.v5.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Pure DTO for user credentials.
 */
public final class Credenziali {
    private final Map<String, String> configuratori;
    private final Map<String, String> fruitori;

    public Credenziali() {
        this.configuratori = new HashMap<>();
        this.fruitori = new HashMap<>();
    }

    @JsonCreator
    public static Credenziali fromJson(
            @JsonProperty("configuratori") Map<String, String> configuratori,
            @JsonProperty("fruitori") Map<String, String> fruitori) {
        Credenziali d = new Credenziali();
        if (configuratori != null)
            d.configuratori.putAll(configuratori);
        if (fruitori != null)
            d.fruitori.putAll(fruitori);
        return d;
    }

    public Map<String, String> getConfiguratori() {
        return Collections.unmodifiableMap(configuratori);
    }

    public void addConfiguratore(String username, String password) {
        configuratori.put(username.trim().toLowerCase(), password);
    }

    public Map<String, String> getFruitori() {
        return Collections.unmodifiableMap(fruitori);
    }

    public void addFruitore(String username, String password) {
        fruitori.put(username.trim().toLowerCase(), password);
    }
}
