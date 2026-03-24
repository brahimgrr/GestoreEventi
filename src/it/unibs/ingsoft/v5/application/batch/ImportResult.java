package it.unibs.ingsoft.v5.application.batch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Value object holding the outcome of a batch import operation.
 * Tracks per-entity success counts and a list of structured error messages.
 */
public final class ImportResult {

    private int campiComuniImportati;
    private int categorieImportate;
    private int proposteImportate;
    private final List<String> errori = new ArrayList<>();

    public void incrementCampiComuni()  { campiComuniImportati++; }
    public void incrementCategorie()    { categorieImportate++; }
    public void incrementProposte()     { proposteImportate++; }

    public void addErrore(String messaggio) {
        errori.add(messaggio);
    }

    public int getCampiComuniImportati() { return campiComuniImportati; }
    public int getCategorieImportate()   { return categorieImportate; }
    public int getProposteImportate()    { return proposteImportate; }

    public List<String> getErrori() {
        return Collections.unmodifiableList(errori);
    }

    public boolean hasErrors() {
        return !errori.isEmpty();
    }

    public int totaleImportati() {
        return campiComuniImportati + categorieImportate + proposteImportate;
    }
}
