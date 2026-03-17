package it.unibs.ingsoft.v3.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.format.DateTimeFormatter;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V3 – AppConstants")
class AppConstantsTest
{
    @Test @DisplayName("DATE_FMT parses dd/MM/yyyy")
    void dateFmt()
    {
        String formatted = java.time.LocalDate.of(2026, 3, 18).format(AppConstants.DATE_FMT);
        assertEquals("18/03/2026", formatted);
    }
}
