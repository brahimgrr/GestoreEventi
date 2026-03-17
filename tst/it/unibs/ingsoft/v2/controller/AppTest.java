package it.unibs.ingsoft.v2.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V2 – App (controller)")
class AppTest
{
    @Test @DisplayName("App can be instantiated")
    void canInstantiate() { assertNotNull(new App()); }
}
