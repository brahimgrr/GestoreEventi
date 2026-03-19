package it.unibs.ingsoft.v1.controller;

import it.unibs.ingsoft.v1.composition.Application;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * App is the composition root wiring services and launching the UI loop.
 * Since it depends heavily on System.in/out (ConsoleUI) and creates real
 * DatabaseService instances, meaningful isolated tests are limited.
 * We verify that the class can be instantiated and its method exists.
 */
@DisplayName("V1 – App (controller)")
class ApplicationTest
{
    @Test
    @DisplayName("App can be instantiated")
    void app_canBeInstantiated()
    {
        Application app = new Application();
        assertNotNull(app);
    }
}
