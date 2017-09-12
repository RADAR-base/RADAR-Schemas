package org.radarcns.validator;

import org.junit.Before;
import org.junit.Test;
import org.radarcns.validator.config.ExcludeConfig;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class SpecificationsValidatorTest {
    private SpecificationsValidator validator;

    @Before
    public void setUp() {
        this.validator = new SpecificationsValidator(ExcludeConfig.load());
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