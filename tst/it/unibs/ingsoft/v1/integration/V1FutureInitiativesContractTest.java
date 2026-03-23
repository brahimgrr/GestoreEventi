package it.unibs.ingsoft.v1.integration;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("Le iniziative saranno introdotte dalla V2; questi test formalizzano il vincolo richiesto per le versioni successive.")
class V1FutureInitiativesContractTest {

    @Test
    void shouldNotRetroactivelyAffectExistingInitiativesWhenCommonFieldsChange() {
    }

    @Test
    void shouldNotRetroactivelyAffectExistingInitiativesWhenCategoryIsRemoved() {
    }
}
