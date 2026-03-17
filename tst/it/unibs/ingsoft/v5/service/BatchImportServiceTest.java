package it.unibs.ingsoft.v5.service;

import it.unibs.ingsoft.v5.model.*;
import it.unibs.ingsoft.v5.persistence.AppData;
import it.unibs.ingsoft.v5.persistence.IPersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V5 – BatchImportService")
class BatchImportServiceTest
{
    @TempDir Path tempDir;

    private CategoriaService cs;
    private PropostaService ps;
    private BatchImportService bis;

    @BeforeEach
    void setUp()
    {
        AppData data = new AppData();
        IPersistenceService db = new IPersistenceService()
        {
            @Override public AppData loadOrCreate() { return data; }
            @Override public void save(AppData d) {}
        };
        cs = new CategoriaService(db, data);
        ps = new PropostaService(db, data);
        bis = new BatchImportService(cs, ps);
    }

    @Test @DisplayName("importaCampiComuni with valid CSV → OK report")
    void importaCampiComuni_valid() throws IOException
    {
        Path f = tempDir.resolve("campi.csv");
        Files.writeString(f, "Note,STRINGA,false\nCosto extra,DECIMALE,true\n");

        List<String> report = bis.importaCampiComuni(f);
        assertTrue(report.stream().anyMatch(r -> r.contains("OK campo comune: Note")));
        assertTrue(report.stream().anyMatch(r -> r.contains("OK campo comune: Costo extra")));
        assertEquals(2, cs.getCampiComuni().size());
    }

    @Test @DisplayName("importaCampiComuni with malformed line → SKIP")
    void importaCampiComuni_malformed() throws IOException
    {
        Path f = tempDir.resolve("bad.csv");
        Files.writeString(f, "OnlyOnePart\n");

        List<String> report = bis.importaCampiComuni(f);
        assertTrue(report.stream().anyMatch(r -> r.contains("SKIP")));
    }

    @Test @DisplayName("importaCampiComuni with unknown type → SKIP")
    void importaCampiComuni_unknownType() throws IOException
    {
        Path f = tempDir.resolve("unk.csv");
        Files.writeString(f, "Foo,UNKNOWN,true\n");

        List<String> report = bis.importaCampiComuni(f);
        assertTrue(report.stream().anyMatch(r -> r.contains("SKIP") && r.contains("UNKNOWN")));
    }

    @Test @DisplayName("importaCampiComuni with non-existent file → ERRORE")
    void importaCampiComuni_noFile()
    {
        List<String> report = bis.importaCampiComuni(tempDir.resolve("missing.csv"));
        assertTrue(report.stream().anyMatch(r -> r.contains("ERRORE")));
    }

    @Test @DisplayName("importaCampiComuni skips comments and blank lines")
    void importaCampiComuni_commentsAndBlanks() throws IOException
    {
        Path f = tempDir.resolve("c.csv");
        Files.writeString(f, "# comment\n\nNote,STRINGA,false\n");

        List<String> report = bis.importaCampiComuni(f);
        assertEquals(1, report.size());
        assertTrue(report.get(0).contains("OK"));
    }

    @Test @DisplayName("importaCategorie creates category with specific fields")
    void importaCategorie_valid() throws IOException
    {
        Path f = tempDir.resolve("cat.csv");
        Files.writeString(f, """
                # categoria
                Sport
                # campi specifici
                Certificato,BOOLEANO,true
                Livello,STRINGA,false
                """);

        List<String> report = bis.importaCategorie(f);
        assertTrue(report.stream().anyMatch(r -> r.contains("OK categoria: Sport")));
        assertTrue(report.stream().anyMatch(r -> r.contains("OK campo specifico \"Certificato\"")));
    }

    @Test @DisplayName("importaCategorie with duplicate category → ERRORE")
    void importaCategorie_duplicate() throws IOException
    {
        cs.createCategoria("Sport");
        Path f = tempDir.resolve("dup.csv");
        Files.writeString(f, "# categoria\nSport\n");

        List<String> report = bis.importaCategorie(f);
        assertTrue(report.stream().anyMatch(r -> r.contains("ERRORE")));
    }

    @Test @DisplayName("importaProposte creates and publishes proposals")
    void importaProposte_valid() throws IOException
    {
        cs.createCategoria("Sport");

        LocalDate dl = LocalDate.now().plusDays(10);
        LocalDate ev = dl.plusDays(3);

        Path f = tempDir.resolve("prop.csv");
        Files.writeString(f,
                "categoria,Sport\n" +
                "Titolo,Gita al lago\n" +
                "Numero di partecipanti,10\n" +
                "Termine ultimo di iscrizione," + dl.format(AppConstants.DATE_FMT) + "\n" +
                "Luogo,Brescia\n" +
                "Data," + ev.format(AppConstants.DATE_FMT) + "\n" +
                "Ora,09:00\n" +
                "Quota individuale,0\n" +
                "Data conclusiva," + ev.format(AppConstants.DATE_FMT) + "\n");

        List<String> report = bis.importaProposte(f);
        assertTrue(report.stream().anyMatch(r -> r.contains("OK proposta pubblicata")));
    }

    @Test @DisplayName("importaProposte with missing categoria → SKIP")
    void importaProposte_missingCategoria() throws IOException
    {
        Path f = tempDir.resolve("nocat.csv");
        Files.writeString(f, "Titolo,Test\nLuogo,Brescia\n");

        List<String> report = bis.importaProposte(f);
        assertTrue(report.stream().anyMatch(r -> r.contains("SKIP") && r.contains("categoria")));
    }

    @Test @DisplayName("importaProposte with validation error → ERRORE")
    void importaProposte_validationError() throws IOException
    {
        cs.createCategoria("Sport");
        Path f = tempDir.resolve("bad.csv");
        Files.writeString(f, "categoria,Sport\nTitolo,Bad proposal\n");

        List<String> report = bis.importaProposte(f);
        assertTrue(report.stream().anyMatch(r -> r.contains("ERRORE")));
    }

    @Test @DisplayName("importaProposte with multiple blocks separated by blank lines")
    void importaProposte_multipleBlocks() throws IOException
    {
        cs.createCategoria("Sport");

        LocalDate dl = LocalDate.now().plusDays(10);
        LocalDate ev = dl.plusDays(3);
        String dlStr = dl.format(AppConstants.DATE_FMT);
        String evStr = ev.format(AppConstants.DATE_FMT);

        Path f = tempDir.resolve("multi.csv");
        Files.writeString(f,
                "categoria,Sport\nTitolo,A\nNumero di partecipanti,10\nTermine ultimo di iscrizione," + dlStr +
                "\nLuogo,B\nData," + evStr + "\nOra,09:00\nQuota individuale,0\nData conclusiva," + evStr +
                "\n\ncategoria,Sport\nTitolo,B\nNumero di partecipanti,5\nTermine ultimo di iscrizione," + dlStr +
                "\nLuogo,C\nData," + evStr + "\nOra,10:00\nQuota individuale,0\nData conclusiva," + evStr + "\n");

        List<String> report = bis.importaProposte(f);
        long okCount = report.stream().filter(r -> r.contains("OK proposta pubblicata")).count();
        assertEquals(2, okCount);
    }
}
