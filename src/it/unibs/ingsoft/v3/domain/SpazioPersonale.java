package it.unibs.ingsoft.v3.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SpazioPersonale {
    private final List<Notifica> notifiche;

    public SpazioPersonale() {
        this.notifiche = new ArrayList<>();
    }

    @JsonCreator
    public static SpazioPersonale fromJson(
            @JsonProperty("notifiche") List<Notifica> notifiche) {
        SpazioPersonale s = new SpazioPersonale();
        if (notifiche != null) {
            s.notifiche.addAll(notifiche);
        }
        return s;
    }

    public List<Notifica> getNotifiche() {
        return Collections.unmodifiableList(notifiche);
    }

    public void addNotifica(Notifica n) {
        if (!notifiche.contains(n)) {
            notifiche.add(n);
        }
    }

    public void removeNotifica(Notifica n) {
        notifiche.remove(n);
    }
}
