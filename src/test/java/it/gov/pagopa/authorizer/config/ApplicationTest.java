package it.gov.pagopa.authorizer.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationTest {

    @Test
    void applicationContextLoaded() {
        assertTrue(true); // it just tests that an error has not occurred
    }

    @Test
    void applicationContextTest() {
        Application.main(new String[] {});
        assertTrue(true); // it just tests that an error has not occurred
    }
}
