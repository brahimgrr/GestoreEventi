package it.unibs.ingsoft.v2.controller;

import it.unibs.ingsoft.v2.composition.Application;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V2 – Application (composition root)")
class ApplicationTest
{
    @Test @DisplayName("Application can be instantiated")
    void canInstantiate() { assertNotNull(new Application()); }
}
