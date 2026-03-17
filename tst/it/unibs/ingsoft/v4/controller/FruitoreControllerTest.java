package it.unibs.ingsoft.v4.controller;
import it.unibs.ingsoft.v4.model.*; import it.unibs.ingsoft.v4.persistence.AppData; import it.unibs.ingsoft.v4.persistence.IPersistenceService;
import it.unibs.ingsoft.v4.service.*;
import org.junit.jupiter.api.BeforeEach; import org.junit.jupiter.api.Test; import org.junit.jupiter.api.DisplayName;
import java.time.LocalDate; import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
@DisplayName("V4 – FruitoreController") class FruitoreControllerTest { private FruitoreService frs; private PropostaService ps; private CategoriaService cs; private IscrizioneService is;
@BeforeEach void setUp() { AppData d = new AppData(); IPersistenceService db = new IPersistenceService() { @Override public AppData loadOrCreate() { return d; } @Override public void save(AppData x) {} }; NotificaService ns = new NotificaService(db, d); frs = new FruitoreService(db, d, ns); cs = new CategoriaService(db, d); ps = new PropostaService(db, d); is = new IscrizioneService(db, d, frs); }
@Test @DisplayName("Register + subscribe + unsubscribe") void workflow() { cs.createCategoria("S"); Fruitore f = frs.registraFruitore("user1", "pass1234"); Proposta p = makeOpen(); is.iscrivi(f, p); assertTrue(p.isIscrittoFruitore("user1")); is.disdici(f, p); assertFalse(p.isIscrittoFruitore("user1")); }
private Proposta makeOpen() { Proposta p = ps.creaProposta("S"); LocalDate dl = LocalDate.now().plusDays(30); LocalDate ev = dl.plusDays(3); p.putAllValoriCampi(Map.of("Titolo","T"+System.nanoTime(),"Numero di partecipanti","10","Termine ultimo di iscrizione",dl.format(AppConstants.DATE_FMT),"Luogo","B","Data",ev.format(AppConstants.DATE_FMT),"Ora","09:00","Quota individuale","0","Data conclusiva",ev.format(AppConstants.DATE_FMT))); ps.validaProposta(p); ps.pubblicaProposta(p); return p; } }
