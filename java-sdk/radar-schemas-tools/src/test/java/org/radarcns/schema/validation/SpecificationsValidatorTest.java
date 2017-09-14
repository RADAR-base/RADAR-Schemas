package org.radarcns.schema.validation;

import org.junit.Before;
import org.junit.Test;
import org.radarcns.schema.Scope;
import org.radarcns.schema.validation.config.ExcludeConfig;

import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.radarcns.schema.specification.SourceCatalogue.BASE_PATH;

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
}
