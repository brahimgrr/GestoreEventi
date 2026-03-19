package it.unibs.ingsoft.v2.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V2 – AppConstants")
class ApplicationConstantsTest
{
    @Test
    @DisplayName("DATE_FMT parses dd/MM/yyyy format")
    void dateFmt_parsesCorrectFormat()
    {
        DateTimeFormatter fmt = AppConstants.DATE_FMT;
        assertNotNull(fmt);
        String formatted = java.time.LocalDate.of(2026, 3, 18).format(fmt);
        assertEquals("18/03/2026", formatted);
    }

    @Test
    @DisplayName("DATE_FMT round-trip parse/format")
    void dateFmt_roundTrip()
    {
        String dateStr = "25/12/2026";
        java.time.LocalDate parsed = java.time.LocalDate.parse(dateStr, AppConstants.DATE_FMT);
        assertEquals(dateStr, parsed.format(AppConstants.DATE_FMT));
    }
}
