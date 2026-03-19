package it.unibs.ingsoft.v4.controller;

import org.junit.jupiter.api.Test; import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
@DisplayName("V4 – App") class ApplicationTest { @Test @DisplayName("Instantiate") void i() { assertNotNull(new App()); } }
