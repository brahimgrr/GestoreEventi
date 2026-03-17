package it.unibs.ingsoft.v3.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V3 – Proposta")
class PropostaTest
{
    private Categoria makeCategoria() { return new Categoria("Sport"); }

    @Test @DisplayName("Constructor → BOZZA state, empty iscrizioni and storiaStati")
    void constructor_valid()
    {
        Proposta p = new Proposta(makeCategoria());
        assertEquals(StatoProposta.BOZZA, p.getStato());
        assertTrue(p.getIscrizioni().isEmpty());
        assertTrue(p.getStoriaStati().isEmpty());
    }

    @Test @DisplayName("Constructor null category → exception")
    void constructor_null_throws() { assertThrows(IllegalArgumentException.class, () -> new Proposta(null)); }

    @Test @DisplayName("setStato records in storiaStati")
    void setStato_recordsHistory()
    {
        Proposta p = new Proposta(makeCategoria());
        p.setStato(StatoProposta.VALIDA, LocalDate.now());
        assertTrue(p.getStoriaStati().containsKey(StatoProposta.VALIDA));
    }

    @Test @DisplayName("Invalid transition BOZZA → APERTA throws")
    void setStato_invalidTransition_throws()
    {
        Proposta p = new Proposta(makeCategoria());
        assertThrows(IllegalStateException.class, () -> p.setStato(StatoProposta.APERTA, LocalDate.now()));
    }

    @Test @DisplayName("setStato with null date throws")
    void setStato_nullDate_throws()
    {
        Proposta p = new Proposta(makeCategoria());
        assertThrows(IllegalArgumentException.class, () -> p.setStato(StatoProposta.VALIDA, null));
    }

    @Test @DisplayName("addIscrizione on APERTA → success")
    void addIscrizione_onAperta()
    {
        Proposta p = makeApertaProposta();
        Fruitore f = new Fruitore("user1");
        p.addIscrizione(new Iscrizione(f, LocalDate.now()));
        assertEquals(1, p.getNumeroIscritti());
    }

    @Test @DisplayName("addIscrizione on BOZZA → exception")
    void addIscrizione_onBozza_throws()
    {
        Proposta p = new Proposta(makeCategoria());
        assertThrows(IllegalStateException.class,
                () -> p.addIscrizione(new Iscrizione(new Fruitore("u"), LocalDate.now())));
    }

    @Test @DisplayName("removeIscrizione existing → true")
    void removeIscrizione_existing()
    {
        Proposta p = makeApertaProposta();
        p.addIscrizione(new Iscrizione(new Fruitore("user1"), LocalDate.now()));
        assertTrue(p.removeIscrizione("user1"));
        assertEquals(0, p.getNumeroIscritti());
    }

    @Test @DisplayName("removeIscrizione non-existing → false")
    void removeIscrizione_nonExisting() { assertFalse(makeApertaProposta().removeIscrizione("unknown")); }

    @Test @DisplayName("isIscrittoFruitore positive and negative")
    void isIscrittoFruitore()
    {
        Proposta p = makeApertaProposta();
        p.addIscrizione(new Iscrizione(new Fruitore("user1"), LocalDate.now()));
        assertTrue(p.isIscrittoFruitore("user1"));
        assertFalse(p.isIscrittoFruitore("user2"));
    }

    @Test @DisplayName("getNumeroIscritti tracks count")
    void getNumeroIscritti()
    {
        Proposta p = makeApertaProposta();
        assertEquals(0, p.getNumeroIscritti());
        p.addIscrizione(new Iscrizione(new Fruitore("a"), LocalDate.now()));
        p.addIscrizione(new Iscrizione(new Fruitore("b"), LocalDate.now()));
        assertEquals(2, p.getNumeroIscritti());
    }

    private Proposta makeApertaProposta()
    {
        Proposta p = new Proposta(makeCategoria());
        p.setStato(StatoProposta.VALIDA, LocalDate.now());
        p.setStato(StatoProposta.APERTA, LocalDate.now());
        return p;
    }
}
