package it.unibs.ingsoft.v2.support;

import it.unibs.ingsoft.v2.domain.Campo;
import it.unibs.ingsoft.v2.domain.Categoria;
import it.unibs.ingsoft.v2.domain.Proposta;
import it.unibs.ingsoft.v2.domain.TipoDato;
import it.unibs.ingsoft.v2.presentation.view.contract.IAppView;
import it.unibs.ingsoft.v2.presentation.view.contract.OperationCancelledException;
import it.unibs.ingsoft.v2.presentation.view.contract.ProposalFieldValidator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class ScriptedAppView implements IAppView {

    private static final String CANCEL_KEYWORD = "annulla";
    private static final Object CANCEL_INPUT = new Object();

    private final Deque<String> stringInputs = new ArrayDeque<>();
    private final Deque<String> passwordInputs = new ArrayDeque<>();
    private final Deque<Object> intInputs = new ArrayDeque<>();
    private final Deque<Object> yesNoInputs = new ArrayDeque<>();
    private final Deque<Object> tipoDatoInputs = new ArrayDeque<>();
    private final Deque<Object> listInputs = new ArrayDeque<>();
    private final Deque<Optional<Map<String, String>>> formResults = new ArrayDeque<>();
    private final Deque<Integer> categorySelections = new ArrayDeque<>();

    private final List<String> outputs = new ArrayList<>();
    private final List<List<Campo>> printedCampiBatches = new ArrayList<>();
    private final List<List<Categoria>> printedCategorieBatches = new ArrayList<>();
    private final List<Map<String, List<Proposta>>> shownBacheche = new ArrayList<>();
    private final List<Proposta> shownSummaries = new ArrayList<>();

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

    public ScriptedAppView addCancelledIntegers(int count) {
        addCancellations(intInputs, count);
        return this;
    }

    public ScriptedAppView addYesNo(Boolean... values) {
        Collections.addAll(yesNoInputs, values);
        return this;
    }

    public ScriptedAppView addCancelledYesNo(int count) {
        addCancellations(yesNoInputs, count);
        return this;
    }

    public ScriptedAppView addTipoDati(TipoDato... values) {
        Collections.addAll(tipoDatoInputs, values);
        return this;
    }

    public ScriptedAppView addCancelledTipoDati(int count) {
        addCancellations(tipoDatoInputs, count);
        return this;
    }

    @SafeVarargs
    public final ScriptedAppView addNameLists(List<String>... values) {
        Collections.addAll(listInputs, values);
        return this;
    }

    public ScriptedAppView addCancelledNameLists(int count) {
        addCancellations(listInputs, count);
        return this;
    }

    public ScriptedAppView addFormResult(Map<String, String> values) {
        formResults.addLast(Optional.of(new LinkedHashMap<>(values)));
        return this;
    }

    public ScriptedAppView addCancelledForm() {
        formResults.addLast(Optional.empty());
        return this;
    }

    public ScriptedAppView addCategorySelections(Integer... values) {
        Collections.addAll(categorySelections, values);
        return this;
    }

    public boolean containsOutput(String text) {
        return outputs.stream().anyMatch(line -> line.contains(text));
    }

    public List<String> getOutputs() {
        return Collections.unmodifiableList(outputs);
    }

    public List<List<Campo>> getPrintedCampiBatches() {
        return Collections.unmodifiableList(printedCampiBatches);
    }

    public List<List<Categoria>> getPrintedCategorieBatches() {
        return Collections.unmodifiableList(printedCategorieBatches);
    }

    public List<Map<String, List<Proposta>>> getShownBacheche() {
        return Collections.unmodifiableList(shownBacheche);
    }

    public List<Proposta> getShownSummaries() {
        return Collections.unmodifiableList(shownSummaries);
    }

    @Override
    public String acquisisciStringa(String prompt) {
        String value = poll(stringInputs, "string input for prompt: " + prompt);
        if (CANCEL_KEYWORD.equalsIgnoreCase(value)) {
            throw new OperationCancelledException();
        }
        return value;
    }

    @Override
    public String acquisisciStringaConValidazione(String prompt, Predicate<String> validatore, String errorMsg) {
        while (true) {
            String value = acquisisciStringa(prompt);
            if (validatore.test(value)) {
                return value;
            }
            outputs.add("ERROR: " + errorMsg);
        }
    }

    @Override
    public String acquisisciPassword(String prompt) {
        String value = poll(passwordInputs, "password input for prompt: " + prompt);
        if (CANCEL_KEYWORD.equalsIgnoreCase(value)) {
            throw new OperationCancelledException();
        }
        return value;
    }

    @Override
    public int acquisisciIntero(String prompt, int min, int max) {
        Object raw = poll(intInputs, "integer input for prompt: " + prompt);
        if (raw == CANCEL_INPUT) {
            throw new OperationCancelledException();
        }
        Integer value = (Integer) raw;
        if (value < min || value > max) {
            throw new AssertionError("Input fuori range per prompt '" + prompt + "': " + value);
        }
        return value;
    }

    @Override
    public boolean acquisisciSiNo(String prompt) {
        Object raw = poll(yesNoInputs, "yes/no input for prompt: " + prompt);
        if (raw == CANCEL_INPUT) {
            throw new OperationCancelledException();
        }
        return (Boolean) raw;
    }

    @Override
    public TipoDato acquisisciTipoDato(String prompt) {
        Object raw = poll(tipoDatoInputs, "tipo dato input for prompt: " + prompt);
        if (raw == CANCEL_INPUT) {
            throw new OperationCancelledException();
        }
        return (TipoDato) raw;
    }

    @Override
    public List<String> acquisisciListaNomi(String titolo) {
        Object raw = poll(listInputs, "name list input for title: " + titolo);
        if (raw == CANCEL_INPUT) {
            throw new OperationCancelledException();
        }
        @SuppressWarnings("unchecked")
        List<String> values = (List<String>) raw;
        return new ArrayList<>(values);
    }

    @Override
    public <T> Optional<T> selezionaElemento(String prompt, List<T> elementi) {
        if (elementi.isEmpty()) {
            outputs.add("INFO: nessun elemento disponibile");
            return Optional.empty();
        }

        try {
            int choice = acquisisciIntero(prompt, 0, elementi.size());
            return choice == 0 ? Optional.empty() : Optional.of(elementi.get(choice - 1));
        } catch (OperationCancelledException e) {
            return Optional.empty();
        }
    }

    @Override
    public <T> Optional<T> selezionaElementoConInfo(String prompt, List<T> elementi, Function<T, String> infoMapper) {
        return selezionaElemento(prompt, elementi);
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
        printedCampiBatches.add(campi.stream().map(Campo::new).collect(Collectors.toList()));
        outputs.add("CAMPI: " + campi.size());
    }

    @Override
    public void stampaCategorie(List<Categoria> categorie) {
        printedCategorieBatches.add(categorie.stream().map(Categoria::new).collect(Collectors.toList()));
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
        Map<String, List<Proposta>> copy = new LinkedHashMap<>();
        bacheca.forEach((categoria, proposte) -> copy.put(categoria, List.copyOf(proposte)));
        shownBacheche.add(copy);
        outputs.add("BACHECA: " + bacheca.size());
    }

    @Override
    public void mostraRiepilogoProposta(Proposta proposta) {
        shownSummaries.add(proposta);
        outputs.add("PROPOSTA: " + proposta.getValoriCampi().getOrDefault("Titolo", ""));
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
        Integer selection = poll(categorySelections, "category selection");
        if (selection < 0) {
            return OptionalInt.empty();
        }
        if (selection >= categorie.size()) {
            throw new AssertionError("Indice categoria fuori range: " + selection);
        }
        return OptionalInt.of(selection);
    }

    private static <T> T poll(Deque<T> queue, String description) {
        T value = queue.pollFirst();
        if (value == null) {
            throw new AssertionError("Nessun input disponibile per " + description);
        }
        return value;
    }

    private static void addCancellations(Deque<Object> queue, int count) {
        for (int i = 0; i < count; i++) {
            queue.addLast(CANCEL_INPUT);
        }
    }
}
