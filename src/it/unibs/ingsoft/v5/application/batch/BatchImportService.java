package it.unibs.ingsoft.v5.application.batch;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unibs.ingsoft.v5.application.CatalogoService;
import it.unibs.ingsoft.v5.application.PropostaService;
import it.unibs.ingsoft.v5.application.batch.dto.*;
import it.unibs.ingsoft.v5.domain.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Orchestrates batch import of common fields, categories, and proposals from a JSON file.
 *
 * <p>Processing order respects the dependency chain:
 * <ol>
 *   <li>Common fields (may be referenced by proposals)</li>
 *   <li>Categories with specific fields (proposals reference categories)</li>
 *   <li>Proposals (depend on categories and fields)</li>
 * </ol>
 *
 * <p>Uses a <em>best-effort</em> strategy: each entity is processed independently.
 * Invalid entries are skipped with an error message; valid entries are imported.
 */
public final class BatchImportService {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final CatalogoService catalogoService;
    private final PropostaService propostaService;

    public BatchImportService(CatalogoService catalogoService, PropostaService propostaService) {
        this.catalogoService = Objects.requireNonNull(catalogoService);
        this.propostaService = Objects.requireNonNull(propostaService);
    }

    /**
     * Imports entities from the given JSON file.
     *
     * @param filePath path to the import JSON file
     * @return result containing success counts and error messages
     * @throws IOException if the file cannot be read or parsed
     */
    public ImportResult importa(Path filePath) throws IOException {
        if (!Files.exists(filePath))
            throw new IOException("File non trovato: " + filePath);
        if (!Files.isReadable(filePath))
            throw new IOException("File non leggibile: " + filePath);

        ImportData data = MAPPER.readValue(filePath.toFile(), ImportData.class);
        ImportResult result = new ImportResult();

        importaCampiComuni(data.getCampiComuni(), result);
        importaCategorie(data.getCategorie(), result);
        importaProposte(data.getProposte(), result);

        return result;
    }

    // ----------------------------------------------------------------
    // CAMPI COMUNI
    // ----------------------------------------------------------------

    private void importaCampiComuni(List<CampoImportDTO> campi, ImportResult result) {
        Set<String> nomiVisti = new HashSet<>();

        for (CampoImportDTO dto : campi) {
            String nome = dto.getNome();

            // Pre-validation
            if (nome == null || nome.isBlank()) {
                result.addErrore("[Campo comune] nome vuoto o mancante — campo ignorato.");
                continue;
            }

            // Intra-file duplicate check
            if (!nomiVisti.add(nome.toLowerCase())) {
                result.addErrore("[Campo comune] '" + nome + "': duplicato nel file di importazione.");
                continue;
            }

            // Parse TipoDato
            TipoDato tipoDato = parseTipoDato(dto.getTipoDato());
            if (tipoDato == null) {
                result.addErrore("[Campo comune] '" + nome + "': tipoDato non valido: \"" + dto.getTipoDato() + "\".");
                continue;
            }

            // Delegate to existing service
            try {
                catalogoService.addCampoComune(nome, tipoDato, dto.isObbligatorio());
                result.incrementCampiComuni();
            } catch (IllegalArgumentException e) {
                result.addErrore("[Campo comune] '" + nome + "': " + e.getMessage());
            }
        }
    }

    // ----------------------------------------------------------------
    // CATEGORIE
    // ----------------------------------------------------------------

    private void importaCategorie(List<CategoriaImportDTO> categorie, ImportResult result) {
        Set<String> nomiVisti = new HashSet<>();

        for (CategoriaImportDTO dto : categorie) {
            String nome = dto.getNome();

            if (nome == null || nome.isBlank()) {
                result.addErrore("[Categoria] nome vuoto o mancante — categoria ignorata.");
                continue;
            }

            if (!nomiVisti.add(nome.toLowerCase())) {
                result.addErrore("[Categoria] '" + nome + "': duplicata nel file di importazione.");
                continue;
            }

            // Create category
            try {
                catalogoService.createCategoria(nome);
            } catch (IllegalArgumentException e) {
                result.addErrore("[Categoria] '" + nome + "': " + e.getMessage());
                continue; // skip specific fields if category creation failed
            }

            // Add specific fields
            boolean allFieldsOk = true;
            for (CampoSpecificoImportDTO campoDTO : dto.getCampiSpecifici()) {
                String nomeCampo = campoDTO.getNome();

                if (nomeCampo == null || nomeCampo.isBlank()) {
                    result.addErrore("[Campo specifico] in categoria '" + nome + "': nome vuoto — campo ignorato.");
                    allFieldsOk = false;
                    continue;
                }

                TipoDato tipoDato = parseTipoDato(campoDTO.getTipoDato());
                if (tipoDato == null) {
                    result.addErrore("[Campo specifico] '" + nomeCampo + "' in categoria '" + nome
                            + "': tipoDato non valido: \"" + campoDTO.getTipoDato() + "\".");
                    allFieldsOk = false;
                    continue;
                }

                try {
                    catalogoService.addCampoSpecifico(nome, nomeCampo, tipoDato, campoDTO.isObbligatorio());
                } catch (IllegalArgumentException e) {
                    result.addErrore("[Campo specifico] '" + nomeCampo + "' in categoria '" + nome + "': " + e.getMessage());
                    allFieldsOk = false;
                }
            }

            // Category counts as imported even if some specific fields failed
            result.incrementCategorie();
        }
    }

    // ----------------------------------------------------------------
    // PROPOSTE
    // ----------------------------------------------------------------

    private void importaProposte(List<PropostaImportDTO> proposte, ImportResult result) {
        // Track proposals imported within this batch to detect intra-file duplicates
        Set<String> chiaviBatch = new HashSet<>();

        for (PropostaImportDTO dto : proposte) {
            String nomeCategoria = dto.getCategoria();
            Map<String, String> valori = dto.getValoriCampi();
            String titolo = valori.getOrDefault(AppConstants.CAMPO_TITOLO, "(senza titolo)");

            // Resolve category
            if (nomeCategoria == null || nomeCategoria.isBlank()) {
                result.addErrore("[Proposta] '" + titolo + "': nome categoria mancante.");
                continue;
            }

            Categoria categoria = trovaCategoriaPerNome(nomeCategoria);
            if (categoria == null) {
                result.addErrore("[Proposta] '" + titolo + "': categoria '" + nomeCategoria + "' non trovata nel catalogo.");
                continue;
            }

            // Intra-file duplicate detection (Titolo + Data + Ora + Luogo, case-insensitive).
            // Cross-source duplicates (bacheca + proposteValide) are caught by salvaProposta().
            String chiave = Proposta.chiaveIdentita(valori);
            if (!chiaviBatch.add(chiave)) {
                result.addErrore("[Proposta] '" + titolo + "': duplicata nel file di importazione (stesso Titolo, Data, Ora, Luogo).");
                continue;
            }

            // Get field definitions
            List<Campo> campiBase = catalogoService.getCampiBase();
            List<Campo> campiComuni = catalogoService.getCampiComuni();

            // Type-validate each field value against its TipoDato
            List<Campo> tuttiCampi = new ArrayList<>();
            tuttiCampi.addAll(campiBase);
            tuttiCampi.addAll(campiComuni);
            tuttiCampi.addAll(categoria.getCampiSpecifici());

            List<String> erroriTipo = new ArrayList<>();

            for (Campo campo : tuttiCampi) {
                String valore = valori.get(campo.getNome());
                if (valore != null && !valore.isBlank()) {
                    String errore = DefaultTypeValidator.INSTANCE.validate(valore, campo.getTipoDato());
                    if (errore != null) {
                        erroriTipo.add("campo \"" + campo.getNome() + "\": " + errore);
                    }
                }
            }

            if (!erroriTipo.isEmpty()) {
                for (String e : erroriTipo)
                    result.addErrore("[Proposta] '" + titolo + "': " + e);
                continue;
            }

            // Create proposal, set values, validate business rules
            try {
                Proposta proposta = propostaService.creaProposta(categoria, campiBase, campiComuni);
                proposta.putAllValoriCampi(valori);

                List<String> erroriValidazione = propostaService.validaProposta(proposta);
                if (!erroriValidazione.isEmpty()) {
                    for (String e : erroriValidazione)
                        result.addErrore("[Proposta] '" + titolo + "': " + e);
                    continue;
                }

                propostaService.salvaProposta(proposta);
                result.incrementProposte();

            } catch (IllegalArgumentException | IllegalStateException e) {
                result.addErrore("[Proposta] '" + titolo + "': " + e.getMessage());
            }
        }
    }

    // ----------------------------------------------------------------
    // HELPERS
    // ----------------------------------------------------------------

    private Categoria trovaCategoriaPerNome(String nome) {
        return catalogoService.getCategorie().stream()
                .filter(c -> c.getNome().equalsIgnoreCase(nome))
                .findFirst()
                .orElse(null);
    }

    private static TipoDato parseTipoDato(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return TipoDato.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
