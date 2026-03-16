package it.unibs.ingsoft.v5.service;

import it.unibs.ingsoft.v5.model.TipoDato;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BatchImportService
{
    private final CategoriaService  categoriaService;
    private final PropostaService   propostaService;

    public BatchImportService(CategoriaService categoriaService, PropostaService propostaService)
    {
        this.categoriaService = categoriaService;
        this.propostaService  = propostaService;
    }

    // ---------------------------------------------------------------
    // PUBLIC ENTRY POINTS
    // ---------------------------------------------------------------

    /**
     * Imports common fields from a CSV file.
     * Format per line: nome,tipo,obbligatorio
     * Returns a report of what happened (successes and errors).
     */
    public List<String> importaCampiComuni(Path file)
    {
        List<String> report = new ArrayList<>();

        List<String> lines = leggiFile(file, report);
        if (lines == null) return report;

        for (String line : lines)
        {
            if (line.isBlank() || line.startsWith("#")) continue;

            String[] parts = line.split(",", -1);

            if (parts.length < 3)
            {
                report.add("SKIP (formato errato): " + line);
                continue;
            }

            String  nome       = parts[0].trim();
            String  tipoStr    = parts[1].trim().toUpperCase();
            boolean obbligat   = Boolean.parseBoolean(parts[2].trim());

            TipoDato tipo = parseTipo(tipoStr, line, report);
            if (tipo == null) continue;

            try
            {
                categoriaService.aggiungiCampoComune(nome, tipo, obbligat);
                report.add("OK campo comune: " + nome);
            }
            catch (Exception e)
            {
                report.add("ERRORE campo comune \"" + nome + "\": " + e.getMessage());
            }
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
     */
    public List<String> importaCategorie(Path file)
    {
        List<String> report = new ArrayList<>();

        List<String> lines = leggiFile(file, report);
        if (lines == null) return report;

        String        nomeCategoria  = null;
        boolean       aspettaCategoria = false;
        boolean       aspettaCampi   = false;

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
                nomeCategoria = line.trim();
                aspettaCategoria = false;

                try
                {
                    categoriaService.creaCategoria(nomeCategoria);
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
                    categoriaService.aggiungiCampoSpecifico(nomeCategoria, nome, tipo, obbligat);
                    report.add("  OK campo specifico \"" + nome + "\" -> " + nomeCategoria);
                }
                catch (Exception e)
                {
                    report.add("  ERRORE campo specifico \"" + nome + "\": " + e.getMessage());
                }
            }
        }

        return report;
    }

    /**
     * Imports proposals from a CSV file.
     * Format: key,value pairs per line, blank line between proposals.
     * Returns a report of what happened.
     */
    public List<String> importaProposte(Path file)
    {
        List<String> report = new ArrayList<>();

        List<String> lines = leggiFile(file, report);
        if (lines == null) return report;

        // Split lines into blocks separated by blank lines
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

        // Don't forget the last block if file doesn't end with blank line
        if (!current.isEmpty())
            blocks.add(current);

        // Process each block as a proposal
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
                proposta.getValoriCampi().putAll(block);
                proposta.getValoriCampi().remove("categoria");

                List<String> errori = propostaService.validaProposta(proposta);

                if (!errori.isEmpty())
                {
                    report.add("ERRORE proposta [" + nomeCategoria + "]: " + errori);
                    continue;
                }

                propostaService.pubblicaProposta(proposta);
                String titolo = block.getOrDefault("Titolo", "senza titolo");
                report.add("OK proposta pubblicata: \"" + titolo + "\"");
            }
            catch (Exception e)
            {
                report.add("ERRORE proposta [" + nomeCategoria + "]: " + e.getMessage());
            }
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