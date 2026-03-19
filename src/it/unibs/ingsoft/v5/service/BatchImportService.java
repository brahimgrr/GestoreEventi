package it.unibs.ingsoft.v5.service;

import it.unibs.ingsoft.v5.model.TipoDato;
import it.unibs.ingsoft.v5.persistence.IUnitOfWork;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Imports common fields, categories, and proposals from CSV files in a single
 * transactional unit of work. On any failure, the unit of work is rolled back so
 * the system state is unchanged (no partial writes).
 */
public final class BatchImportService
{
    private final CampoService     campoService;
    private final CategoriaService categoriaService;
    private final PropostaService  propostaService;
    private final IUnitOfWork      unitOfWork;

    /**
     * @pre campoService      != null
     * @pre categoriaService  != null
     * @pre propostaService   != null
     * @pre unitOfWork        != null
     */
    public BatchImportService(CampoService campoService, CategoriaService categoriaService,
                              PropostaService propostaService, IUnitOfWork unitOfWork)
    {
        this.campoService      = Objects.requireNonNull(campoService,     "campoService non può essere null.");
        this.categoriaService  = Objects.requireNonNull(categoriaService, "categoriaService non può essere null.");
        this.propostaService   = Objects.requireNonNull(propostaService,  "propostaService non può essere null.");
        this.unitOfWork        = Objects.requireNonNull(unitOfWork,       "unitOfWork non può essere null.");
    }

    // ---------------------------------------------------------------
    // PUBLIC ENTRY POINTS
    // ---------------------------------------------------------------

    /**
     * Imports common fields from a CSV file.
     * Format per line: nome,tipo,obbligatorio
     * Returns a report of what happened (successes and errors).
     * The entire import is transactional: on any I/O error the batch is rolled back.
     */
    public List<String> importaCampiComuni(Path file)
    {
        List<String> report = new ArrayList<>();

        List<String> lines = leggiFile(file, report);
        if (lines == null) return report;

        unitOfWork.begin();
        boolean success = false;

        try
        {
            for (String line : lines)
            {
                if (line.isBlank() || line.startsWith("#")) continue;

                String[] parts = line.split(",", -1);

                if (parts.length < 3)
                {
                    report.add("SKIP (formato errato): " + line);
                    continue;
                }

                String  nome     = parts[0].trim();
                String  tipoStr  = parts[1].trim().toUpperCase();
                boolean obbligat = Boolean.parseBoolean(parts[2].trim());

                TipoDato tipo = parseTipo(tipoStr, line, report);
                if (tipo == null) continue;

                try
                {
                    campoService.addCampoComune(nome, tipo, obbligat);
                    report.add("OK campo comune: " + nome);
                }
                catch (Exception e)
                {
                    report.add("ERRORE campo comune \"" + nome + "\": " + e.getMessage());
                }
            }

            success = true;
        }
        finally
        {
            if (success)
                unitOfWork.commit();
            else
                unitOfWork.rollback();
        }

        return report;
    }

    /**
     * Imports categories and their specific fields from a CSV file.
     * Format:
     *   # categoria
     *   NomeCategoria
     *   # campi specifici
     *   nome,tipo,obbligatorio
     *   ...
     * Returns a report of what happened.
     * The entire import is transactional.
     */
    public List<String> importaCategorie(Path file)
    {
        List<String> report = new ArrayList<>();

        List<String> lines = leggiFile(file, report);
        if (lines == null) return report;

        unitOfWork.begin();
        boolean success = false;

        try
        {
            String  nomeCategoria    = null;
            boolean aspettaCategoria = false;
            boolean aspettaCampi     = false;

            for (String line : lines)
            {
                if (line.isBlank()) continue;

                if (line.startsWith("# categoria"))
                {
                    aspettaCategoria = true;
                    aspettaCampi     = false;
                    continue;
                }

                if (line.startsWith("# campi specifici"))
                {
                    aspettaCampi     = true;
                    aspettaCategoria = false;
                    continue;
                }

                if (line.startsWith("#")) continue;

                if (aspettaCategoria)
                {
                    nomeCategoria    = line.trim();
                    aspettaCategoria = false;

                    try
                    {
                        categoriaService.createCategoria(nomeCategoria);
                        report.add("OK categoria: " + nomeCategoria);
                    }
                    catch (Exception e)
                    {
                        report.add("ERRORE categoria \"" + nomeCategoria + "\": " + e.getMessage());
                        nomeCategoria = null;
                    }
                    continue;
                }

                if (aspettaCampi && nomeCategoria != null)
                {
                    String[] parts = line.split(",", -1);

                    if (parts.length < 3)
                    {
                        report.add("SKIP campo specifico (formato errato): " + line);
                        continue;
                    }

                    String   nome     = parts[0].trim();
                    String   tipoStr  = parts[1].trim().toUpperCase();
                    boolean  obbligat = Boolean.parseBoolean(parts[2].trim());

                    TipoDato tipo = parseTipo(tipoStr, line, report);
                    if (tipo == null) continue;

                    try
                    {
                        categoriaService.addCampoSpecifico(nomeCategoria, nome, tipo, obbligat);
                        report.add("  OK campo specifico \"" + nome + "\" -> " + nomeCategoria);
                    }
                    catch (Exception e)
                    {
                        report.add("  ERRORE campo specifico \"" + nome + "\": " + e.getMessage());
                    }
                }
            }

            success = true;
        }
        finally
        {
            if (success)
                unitOfWork.commit();
            else
                unitOfWork.rollback();
        }

        return report;
    }

    /**
     * Imports proposals from a CSV file.
     * Format: key,value pairs per line, blank line between proposals.
     * Returns a report of what happened.
     * The entire import is transactional.
     */
    public List<String> importaProposte(Path file)
    {
        List<String> report = new ArrayList<>();

        List<String> lines = leggiFile(file, report);
        if (lines == null) return report;

        // Parse all blocks first (no mutation yet)
        List<Map<String, String>> blocks = new ArrayList<>();
        Map<String, String> current = new LinkedHashMap<>();

        for (String line : lines)
        {
            if (line.isBlank() || line.startsWith("#"))
            {
                if (!current.isEmpty())
                {
                    blocks.add(current);
                    current = new LinkedHashMap<>();
                }
                continue;
            }

            String[] parts = line.split(",", 2);
            if (parts.length < 2)
            {
                report.add("SKIP (formato errato): " + line);
                continue;
            }

            current.put(parts[0].trim(), parts[1].trim());
        }

        if (!current.isEmpty())
            blocks.add(current);

        unitOfWork.begin();
        boolean success = false;

        try
        {
            for (Map<String, String> block : blocks)
            {
                String nomeCategoria = block.get("categoria");

                if (nomeCategoria == null)
                {
                    report.add("SKIP proposta: campo 'categoria' mancante.");
                    continue;
                }

                try
                {
                    var proposta = propostaService.creaProposta(nomeCategoria);
                    Map<String, String> campi = new java.util.HashMap<>(block);
                    campi.remove("categoria");
                    proposta.putAllValoriCampi(campi);

                    List<String> errori = propostaService.validaProposta(proposta);

                    if (!errori.isEmpty())
                    {
                        report.add("ERRORE proposta [" + nomeCategoria + "]: " + errori);
                        continue;
                    }

                    propostaService.pubblicaPropostaSenzaSalvare(proposta);
                    String titolo = block.getOrDefault("Titolo", "senza titolo");
                    report.add("OK proposta pubblicata: \"" + titolo + "\"");
                }
                catch (Exception e)
                {
                    report.add("ERRORE proposta [" + nomeCategoria + "]: " + e.getMessage());
                }
            }

            success = true;
        }
        finally
        {
            if (success)
                unitOfWork.commit();
            else
                unitOfWork.rollback();
        }

        return report;
    }

    // ---------------------------------------------------------------
    // UTILITY
    // ---------------------------------------------------------------

    private List<String> leggiFile(Path file, List<String> report)
    {
        try
        {
            return Files.readAllLines(file);
        }
        catch (IOException e)
        {
            report.add("ERRORE lettura file \"" + file + "\": " + e.getMessage());
            return null;
        }
    }

    private TipoDato parseTipo(String tipoStr, String line, List<String> report)
    {
        try
        {
            return TipoDato.valueOf(tipoStr);
        }
        catch (IllegalArgumentException e)
        {
            report.add("SKIP (tipo sconosciuto \"" + tipoStr + "\"): " + line);
            return null;
        }
    }
}
