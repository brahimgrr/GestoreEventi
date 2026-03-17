package it.unibs.ingsoft.v5.integration;

import it.unibs.ingsoft.v5.model.*;
import it.unibs.ingsoft.v5.persistence.AppData;
import it.unibs.ingsoft.v5.persistence.IPersistenceService;
import it.unibs.ingsoft.v5.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for V5 batch import lifecycle.
 * Validates end-to-end: CSV file → BatchImportService → AppData → subscribe user.
 * Uses @TempDir to isolate all file system state.
 */
@DisplayName("V5 Integration – Batch Import Lifecycle")
class BatchImportLifecycleIT
{
    @TempDir Path tempDir;

    private AppData data;
    private CategoriaService cs;
    private PropostaService ps;
    private IscrizioneService is;
    private NotificaService ns;
    private BatchImportService bis;

    @BeforeEach
    void setUp()
    {
        data = new AppData();
        IPersistenceService mockDb = new IPersistenceService()
        {
            @Override public AppData loadOrCreate() { return data; }
            @Override public void save(AppData d) {}
        };
        cs  = new CategoriaService(mockDb, data);
        ps  = new PropostaService(mockDb, data);
        ns  = new NotificaService(mockDb, data);
        NotificaListener listener = (user, msg) -> ns.aggiungiNotifica(user, msg);
        is  = new IscrizioneService(mockDb, data, listener);
        bis = new BatchImportService(cs, ps);
    }

    @Test
    @DisplayName("Import common fields from CSV → fields available in CategoriaService")
    void importCampiComuni_thenAvailableInService() throws IOException
    {
        // Arrange
        Path file = tempDir.resolve("campi.csv");
        Files.writeString(file, "Livello di difficoltà,STRINGA,false\nCosto attrezzatura,DECIMALE,true\n");

        // Act
        List<String> report = bis.importaCampiComuni(file);

        // Assert — both fields imported, service exposes them
        assertTrue(report.stream().allMatch(r -> r.startsWith("OK")), "All lines should succeed: " + report);
        assertEquals(2, cs.getCampiComuni().size());
    }

    @Test
    @DisplayName("Import category from CSV then create a proposal for it → proposal published")
    void importCategoria_thenCreateProposal() throws IOException
    {
        // Arrange — import category with one specific field
        Path catFile = tempDir.resolve("categorie.csv");
        Files.writeString(catFile, """
                # categoria
                Escursionismo
                # campi specifici
                Difficoltà,STRINGA,false
                """);
        bis.importaCategorie(catFile);

        // Verify category is present
        assertTrue(cs.getCategorie().stream().anyMatch(c -> c.getNome().equalsIgnoreCase("Escursionismo")));

        // Arrange — create and publish a proposal for that category
        LocalDate dl = LocalDate.now().plusDays(10);
        LocalDate ev = dl.plusDays(3);

        Proposta p = ps.creaProposta("Escursionismo");
        p.putAllValoriCampi(Map.of(
                "Titolo",                       "Sentiero delle Orobie",
                "Numero di partecipanti",       "15",
                "Termine ultimo di iscrizione", dl.format(AppConstants.DATE_FMT),
                "Luogo",                        "Bergamo",
                "Data",                         ev.format(AppConstants.DATE_FMT),
                "Ora",                          "07:00",
                "Quota individuale",            "5",
                "Data conclusiva",              ev.format(AppConstants.DATE_FMT)
        ));
        List<String> errori = ps.validaProposta(p);
        ps.pubblicaProposta(p);

        // Assert
        assertTrue(errori.isEmpty(), "Validation errors: " + errori);
        assertEquals(StatoProposta.APERTA, p.getStato());
        assertEquals(1, ps.getBacheca().size());
    }

    @Test
    @DisplayName("Import proposals from CSV → proposals appear in bacheca and can be subscribed")
    void importProposte_thenSubscribeUser() throws IOException
    {
        // Arrange — category must exist before importing proposals
        cs.createCategoria("Sport");

        LocalDate dl = LocalDate.now().plusDays(10);
        LocalDate ev = dl.plusDays(3);
        String dlStr = dl.format(AppConstants.DATE_FMT);
        String evStr = ev.format(AppConstants.DATE_FMT);

        Path propFile = tempDir.resolve("proposte.csv");
        Files.writeString(propFile,
                "categoria,Sport\n" +
                "Titolo,Partita di calcio\n" +
                "Numero di partecipanti,10\n" +
                "Termine ultimo di iscrizione," + dlStr + "\n" +
                "Luogo,Milano\n" +
                "Data," + evStr + "\n" +
                "Ora,15:00\n" +
                "Quota individuale,0\n" +
                "Data conclusiva," + evStr + "\n");

        // Act
        List<String> report = bis.importaProposte(propFile);

        // Assert — proposal was imported and published
        assertTrue(report.stream().anyMatch(r -> r.contains("OK proposta pubblicata")), "Report: " + report);
        assertEquals(1, ps.getBacheca().size());

        // Now subscribe a user to the imported proposal
        Proposta imported = ps.getBacheca().get(0);
        Fruitore user = new Fruitore("gianni");
        is.iscrivi(user, imported);
        assertTrue(imported.isIscrittoFruitore("gianni"));
    }

    @Test
    @DisplayName("End-to-end: import category + common fields + proposals → all state consistent")
    void endToEnd_importAll_stateConsistent() throws IOException
    {
        // Arrange — category CSV
        Path catFile = tempDir.resolve("cat.csv");
        Files.writeString(catFile, "# categoria\nCiclismo\n# campi specifici\nTipo bici,STRINGA,false\n");

        // Common fields CSV
        Path campiFile = tempDir.resolve("campi.csv");
        Files.writeString(campiFile, "Dotazione casco,BOOLEANO,false\n");

        LocalDate dl = LocalDate.now().plusDays(15);
        LocalDate ev = dl.plusDays(3);
        String dlStr = dl.format(AppConstants.DATE_FMT);
        String evStr = ev.format(AppConstants.DATE_FMT);

        // Proposals CSV (two proposals separated by blank line)
        Path propFile = tempDir.resolve("props.csv");
        Files.writeString(propFile,
                "categoria,Ciclismo\nTitolo,Giro del lago\nNumero di partecipanti,20\n" +
                "Termine ultimo di iscrizione," + dlStr + "\nLuogo,Como\nData," + evStr +
                "\nOra,08:00\nQuota individuale,10\nData conclusiva," + evStr + "\n\n" +
                "categoria,Ciclismo\nTitolo,Salita al Ghisallo\nNumero di partecipanti,10\n" +
                "Termine ultimo di iscrizione," + dlStr + "\nLuogo,Bellagio\nData," + evStr +
                "\nOra,09:00\nQuota individuale,5\nData conclusiva," + evStr + "\n");

        // Act — import in order: categories, common fields, proposals
        bis.importaCategorie(catFile);
        bis.importaCampiComuni(campiFile);
        List<String> propReport = bis.importaProposte(propFile);

        // Assert
        assertEquals(1, cs.getCategorie().size());
        assertEquals(1, cs.getCampiComuni().size());
        long okProposals = propReport.stream().filter(r -> r.contains("OK proposta pubblicata")).count();
        assertEquals(2L, okProposals);
        assertEquals(2, ps.getBacheca().size());
    }
}
