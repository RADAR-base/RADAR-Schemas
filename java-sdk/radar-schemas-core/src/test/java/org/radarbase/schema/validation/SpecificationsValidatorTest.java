package org.radarbase.schema.validation;

import static org.junit.Assert.assertTrue;
import static org.radarbase.schema.specification.SourceCatalogue.BASE_PATH;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.radarbase.schema.Scope;
import org.radarbase.schema.validation.config.ExcludeConfig;

public class SpecificationsValidatorTest {
    private SpecificationsValidator validator;

    @Before
    public void setUp() throws IOException {
        this.validator = new SpecificationsValidator(BASE_PATH, ExcludeConfig.load(null));
    }

    @Test
    public void activeIsYml() throws IOException {
        assertTrue(validator.specificationsAreYmlFiles(Scope.ACTIVE));
    }

    @Test
    public void monitorIsYml() throws IOException {
        assertTrue(validator.specificationsAreYmlFiles(Scope.MONITOR));
    }

    @Test
    public void passiveIsYml() throws IOException {
        assertTrue(validator.specificationsAreYmlFiles(Scope.PASSIVE));
    }

    @Test
    public void connectorIsYml() throws IOException {
        assertTrue(validator.specificationsAreYmlFiles(Scope.CONNECTOR));
    }

    @Test
    public void streamIsYml() throws IOException {
        assertTrue(validator.specificationsAreYmlFiles(Scope.STREAM));
    }
}
