package it.unibs.ingsoft.v4.controller;
import it.unibs.ingsoft.v4.model.*; import it.unibs.ingsoft.v4.persistence.AppData; import it.unibs.ingsoft.v4.persistence.IPersistenceService;
import it.unibs.ingsoft.v4.service.*;
import org.junit.jupiter.api.BeforeEach; import org.junit.jupiter.api.Test; import org.junit.jupiter.api.DisplayName;
import java.time.LocalDate; import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
@DisplayName("V4 – ConfiguratoreController") class ConfiguratoreControllerTest { private CategoriaService cs; private PropostaService ps;
@BeforeEach void setUp() { AppData d = new AppData(); IPersistenceService db = new IPersistenceService() { @Override public AppData loadOrCreate() { return d; } @Override public void save(AppData x) {} }; cs = new CategoriaService(db, d); ps = new PropostaService(db, d); }
@Test @DisplayName("Full workflow") void fw() { cs.createCategoria("S"); Proposta p = ps.creaProposta("S"); LocalDate dl = LocalDate.now().plusDays(5); LocalDate ev = dl.plusDays(3); p.putAllValoriCampi(Map.of("Titolo","G","Numero di partecipanti","10","Termine ultimo di iscrizione",dl.format(AppConstants.DATE_FMT),"Luogo","B","Data",ev.format(AppConstants.DATE_FMT),"Ora","09:00","Quota individuale","0","Data conclusiva",ev.format(AppConstants.DATE_FMT))); assertTrue(ps.validaProposta(p).isEmpty()); ps.pubblicaProposta(p); assertEquals(StatoProposta.APERTA, p.getStato()); } }
