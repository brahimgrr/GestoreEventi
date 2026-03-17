package it.unibs.ingsoft.v5.model;
import org.junit.jupiter.api.Test; import org.junit.jupiter.api.DisplayName; import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
@DisplayName("V5 – Proposta") class PropostaTest
{
    @Test @DisplayName("Constructor → BOZZA") void c() { assertEquals(StatoProposta.BOZZA, new Proposta(new Categoria("S")).getStato()); }
    @Test @DisplayName("addIscrizione on APERTA") void add()
    {
        Proposta p = new Proposta(new Categoria("S"));
        p.setStato(StatoProposta.VALIDA, LocalDate.now());
        p.setStato(StatoProposta.APERTA, LocalDate.now());
        p.addIscrizione(new Iscrizione(new Fruitore("u"), LocalDate.now()));
        assertEquals(1, p.getNumeroIscritti());
    }
}
