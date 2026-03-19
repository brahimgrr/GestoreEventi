package it.unibs.ingsoft.v3.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V3 – App (controller)")
class ApplicationTest
{
    @Test @DisplayName("App can be instantiated")
    void canInstantiate() { assertNotNull(new App()); }
}
