package it.unibs.ingsoft.v5.model;
import org.junit.jupiter.api.Test; import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
@DisplayName("V5 – AppConstants") class AppConstantsTest { @Test @DisplayName("DATE_FMT") void d() { assertEquals("18/03/2026", java.time.LocalDate.of(2026,3,18).format(AppConstants.DATE_FMT)); } }
