package it.unibs.ingsoft.v3.support;

import it.unibs.ingsoft.v3.domain.*;
import it.unibs.ingsoft.v3.presentation.view.contract.IAppView;
import it.unibs.ingsoft.v3.presentation.view.contract.ProposalFieldValidator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public final class ScriptedAppView implements IAppView {

    private final Deque<String> stringInputs = new ArrayDeque<>();
    private final Deque<String> passwordInputs = new ArrayDeque<>();
    private final Deque<Integer> intInputs = new ArrayDeque<>();
    private final Deque<Boolean> yesNoInputs = new ArrayDeque<>();
    private final Deque<Optional<Map<String, String>>> formResults = new ArrayDeque<>();
    private final List<String> outputs = new ArrayList<>();

    public ScriptedAppView addStrings(String... values) {
        Collections.addAll(stringInputs, values);
        return this;
    }

    public ScriptedAppView addPasswords(String... values) {
        Collections.addAll(passwordInputs, values);
        return this;
    }

    public ScriptedAppView addIntegers(Integer... values) {
        Collections.addAll(intInputs, values);
        return this;
    }

    public ScriptedAppView addYesNo(Boolean... values) {
        Collections.addAll(yesNoInputs, values);
        return this;
    }

    public ScriptedAppView addFormResult(Map<String, String> values) {
        formResults.addLast(Optional.of(values));
        return this;
    }

    public ScriptedAppView addCancelledForm() {
        formResults.addLast(Optional.empty());
        return this;
    }

    public List<String> getOutputs() {
        return Collections.unmodifiableList(outputs);
    }

    public boolean containsOutput(String text) {
        return outputs.stream().anyMatch(line -> line.contains(text));
    }

    @Override
    public String acquisisciStringa(String prompt) {
        return poll(stringInputs, "string input for prompt: " + prompt);
    }

    @Override
    public String acquisisciStringaConValidazione(String prompt, Predicate<String> validatore, String errorMsg) {
        while (true) {
            String value = poll(stringInputs, "validated string input for prompt: " + prompt);
            if (validatore.test(value)) {
                return value;
            }
            outputs.add("ERROR: " + errorMsg);
        }
    }

    @Override
    public String acquisisciPassword(String prompt) {
        return poll(passwordInputs, "password input for prompt: " + prompt);
    }

    @Override
    public int acquisisciIntero(String prompt, int min, int max) {
        Integer value = poll(intInputs, "integer input for prompt: " + prompt);
        if (value < min || value > max) {
            throw new AssertionError("Input fuori range per prompt '" + prompt + "': " + value);
        }
        return value;
    }

    @Override
    public boolean acquisisciSiNo(String prompt) {
        return poll(yesNoInputs, "yes/no input for prompt: " + prompt);
    }

    @Override
    public TipoDato acquisisciTipoDato(String prompt) {
        throw new UnsupportedOperationException("TipoDato input non previsto in questo test.");
    }

    @Override
    public List<String> acquisisciListaNomi(String titolo) {
        throw new UnsupportedOperationException("acquisisciListaNomi non previsto in questo test.");
    }

    @Override
    public <T> Optional<T> selezionaElemento(String prompt, List<T> elementi) {
        throw new UnsupportedOperationException("selezionaElemento non previsto in questo test.");
    }

    @Override
    public <T> Optional<T> selezionaElementoConInfo(String prompt, List<T> elementi, Function<T, String> infoMapper) {
        throw new UnsupportedOperationException("selezionaElementoConInfo non previsto in questo test.");
    }

    @Override
    public void stampa(String testo) {
        outputs.add(testo);
    }

    @Override
    public void newLine() {
        outputs.add("");
    }

    @Override
    public void header(String titolo) {
        outputs.add("HEADER: " + titolo);
    }

    @Override
    public void stampaSezione(String titolo) {
        outputs.add("SECTION: " + titolo);
    }

    @Override
    public void stampaCampi(List<Campo> campi) {
        outputs.add("CAMPI: " + campi.size());
    }

    @Override
    public void stampaCategorie(List<Categoria> categorie) {
        outputs.add("CATEGORIE: " + categorie.size());
    }

    @Override
    public void stampaCategorieDettaglio(Map<String, List<String>> categorieConCampi) {
        outputs.add("CATEGORIE_DETTAGLIO: " + categorieConCampi.size());
    }

    @Override
    public void stampaMenu(String titolo, String[] voci) {
        outputs.add("MENU: " + titolo + " (" + voci.length + ")");
    }

    @Override
    public void stampaMenu(String titolo, String[] voci, String uscitaLabel) {
        outputs.add("MENU: " + titolo + " (" + voci.length + ") -> " + uscitaLabel);
    }

    @Override
    public void pausa() {
        outputs.add("PAUSA");
    }

    @Override
    public void stampaSuccesso(String msg) {
        outputs.add("SUCCESS: " + msg);
    }

    @Override
    public void stampaErrore(String msg) {
        outputs.add("ERROR: " + msg);
    }

    @Override
    public void stampaAvviso(String msg) {
        outputs.add("WARN: " + msg);
    }

    @Override
    public void stampaInfo(String msg) {
        outputs.add("INFO: " + msg);
    }

    @Override
    public void mostraBacheca(Map<String, List<Proposta>> bacheca) {
        outputs.add("BACHECA: " + bacheca.size());
    }

    @Override
    public void mostraRiepilogoProposta(Proposta proposta) {
        outputs.add("PROPOSTA: " + proposta.getValoriCampi().getOrDefault("Titolo", ""));
    }

    @Override
    public void mostraAderenti(List<String> aderenti) {

    }

    @Override
    public void mostraCronologiaStati(List<PropostaStateChange> history) {

    }

    @Override
    public Optional<Map<String, String>> acquisisciValoriProposta(Proposta proposta, ProposalFieldValidator validator) {
        return poll(formResults, "proposal form result");
    }

    @Override
    public Optional<Map<String, String>> correggiCampiProposta(Proposta proposta, Set<String> nomiCampi, ProposalFieldValidator validator) {
        return poll(formResults, "proposal correction result");
    }

    @Override
    public OptionalInt selezionaCategoria(List<Categoria> categorie) {
        throw new UnsupportedOperationException("selezionaCategoria non previsto in questo test.");
    }

    private static <T> T poll(Deque<T> queue, String description) {
        T value = queue.pollFirst();
        if (value == null) {
            throw new AssertionError("Nessun input disponibile per " + description);
        }
        return value;
    }
}
