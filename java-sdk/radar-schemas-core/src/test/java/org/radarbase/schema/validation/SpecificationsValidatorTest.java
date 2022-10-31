package org.radarbase.schema.validation;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.radarbase.schema.validation.SourceCatalogueValidationTest.BASE_PATH;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.radarbase.schema.Scope;
import org.radarbase.schema.specification.active.ActiveSource;
import org.radarbase.schema.specification.config.SchemaConfig;
import org.radarbase.schema.specification.connector.ConnectorSource;
import org.radarbase.schema.specification.monitor.MonitorSource;
import org.radarbase.schema.specification.passive.PassiveSource;
import org.radarbase.schema.specification.push.PushSource;
import org.radarbase.schema.specification.stream.StreamGroup;

public class SpecificationsValidatorTest {
    private SpecificationsValidator validator;

    @BeforeEach
    public void setUp() {
        this.validator = new SpecificationsValidator(BASE_PATH, new SchemaConfig());
    }

    @Test
    public void activeIsYml() throws IOException {
        assertTrue(validator.specificationsAreYmlFiles(Scope.ACTIVE));
        assertTrue(validator.checkSpecificationParsing(Scope.ACTIVE, ActiveSource.class));
    }

    @Test
    public void monitorIsYml() throws IOException {
        assertTrue(validator.specificationsAreYmlFiles(Scope.MONITOR));
        assertTrue(validator.checkSpecificationParsing(Scope.MONITOR, MonitorSource.class));
    }

    @Test
    public void passiveIsYml() throws IOException {
        assertTrue(validator.specificationsAreYmlFiles(Scope.PASSIVE));
        assertTrue(validator.checkSpecificationParsing(Scope.PASSIVE, PassiveSource.class));
    }

    @Test
    public void connectorIsYml() throws IOException {
        assertTrue(validator.specificationsAreYmlFiles(Scope.CONNECTOR));
        assertTrue(validator.checkSpecificationParsing(Scope.CONNECTOR, ConnectorSource.class));
    }

    @Test
    public void pushIsYml() throws IOException {
        assertTrue(validator.specificationsAreYmlFiles(Scope.PUSH));
        assertTrue(validator.checkSpecificationParsing(Scope.PUSH, PushSource.class));
    }

    @Test
    public void streamIsYml() throws IOException {
        assertTrue(validator.specificationsAreYmlFiles(Scope.STREAM));
        assertTrue(validator.checkSpecificationParsing(Scope.STREAM, StreamGroup.class));
    }
}
