package it.unibs.ingsoft.v2.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V2 – Proposta")
class PropostaTest
{
    private Categoria makeCategoria()
    {
        return new Categoria("Sport");
    }

    // ───────────────────── Construction ─────────────────────

    @Test
    @DisplayName("Constructor with valid category → BOZZA state")
    void constructor_validCategory_bozzaState()
    {
        Proposta p = new Proposta(makeCategoria());
        assertEquals(StatoProposta.BOZZA, p.getStato());
        assertEquals("Sport", p.getCategoria().getNome());
        assertTrue(p.getValoriCampi().isEmpty());
    }

    @Test
    @DisplayName("Constructor with null category → exception")
    void constructor_nullCategory_throwsException()
    {
        assertThrows(IllegalArgumentException.class, () -> new Proposta(null));
    }

    // ───────────────────── Field values ─────────────────────

    @Test
    @DisplayName("putAllValoriCampi stores values")
    void putAllValoriCampi_storesValues()
    {
        Proposta p = new Proposta(makeCategoria());
        p.putAllValoriCampi(java.util.Map.of("Titolo", "Gita", "Luogo", "Brescia"));
        assertEquals("Gita", p.getValoriCampi().get("Titolo"));
        assertEquals("Brescia", p.getValoriCampi().get("Luogo"));
    }

    @Test
    @DisplayName("getValoriCampi returns unmodifiable map")
    void getValoriCampi_unmodifiable()
    {
        Proposta p = new Proposta(makeCategoria());
        assertThrows(UnsupportedOperationException.class,
                () -> p.getValoriCampi().put("x", "y"));
    }

    // ───────────────────── State transitions ─────────────────────

    @Test
    @DisplayName("Valid transition BOZZA → VALIDA succeeds")
    void setStato_bozzaToValida_succeeds()
    {
        Proposta p = new Proposta(makeCategoria());
        p.setStato(StatoProposta.VALIDA);
        assertEquals(StatoProposta.VALIDA, p.getStato());
    }

    @Test
    @DisplayName("Valid transition VALIDA → APERTA succeeds")
    void setStato_validaToAperta_succeeds()
    {
        Proposta p = new Proposta(makeCategoria());
        p.setStato(StatoProposta.VALIDA);
        p.setStato(StatoProposta.APERTA);
        assertEquals(StatoProposta.APERTA, p.getStato());
    }

    @Test
    @DisplayName("Invalid transition BOZZA → APERTA throws exception")
    void setStato_bozzaToAperta_throwsException()
    {
        Proposta p = new Proposta(makeCategoria());
        assertThrows(IllegalStateException.class, () -> p.setStato(StatoProposta.APERTA));
    }

    @Test
    @DisplayName("setStato with null throws exception")
    void setStato_null_throwsException()
    {
        Proposta p = new Proposta(makeCategoria());
        assertThrows(IllegalArgumentException.class, () -> p.setStato(null));
    }

    // ───────────────────── Date setters ─────────────────────

    @Test
    @DisplayName("Date setters/getters work correctly")
    void dateSettersGetters()
    {
        Proposta p = new Proposta(makeCategoria());
        java.time.LocalDate date = java.time.LocalDate.of(2026, 6, 15);

        p.setDataPubblicazione(date);
        assertEquals(date, p.getDataPubblicazione());

        p.setTermineIscrizione(date);
        assertEquals(date, p.getTermineIscrizione());

        p.setDataEvento(date);
        assertEquals(date, p.getDataEvento());
    }
}
