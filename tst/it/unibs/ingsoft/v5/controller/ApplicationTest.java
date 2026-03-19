package it.unibs.ingsoft.v5.controller;
import org.junit.jupiter.api.Test; import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
@DisplayName("V5 – App") class ApplicationTest { @Test @DisplayName("Instantiate") void i() { assertNotNull(new App()); } }
