package org.radarbase.schema.validation

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.radarbase.schema.Scope.ACTIVE
import org.radarbase.schema.Scope.CONNECTOR
import org.radarbase.schema.Scope.MONITOR
import org.radarbase.schema.Scope.PASSIVE
import org.radarbase.schema.Scope.PUSH
import org.radarbase.schema.Scope.STREAM
import org.radarbase.schema.specification.active.ActiveSource
import org.radarbase.schema.specification.config.SchemaConfig
import org.radarbase.schema.specification.connector.ConnectorSource
import org.radarbase.schema.specification.monitor.MonitorSource
import org.radarbase.schema.specification.passive.PassiveSource
import org.radarbase.schema.specification.push.PushSource
import org.radarbase.schema.specification.stream.StreamGroup
import java.io.IOException

class SpecificationsValidatorTest {
    private lateinit var validator: SpecificationsValidator

    @BeforeEach
    fun setUp() {
        validator = SpecificationsValidator(SourceCatalogueValidationTest.BASE_PATH, SchemaConfig())
    }

    @Test
    @Throws(IOException::class)
    fun activeIsYml() {
        Assertions.assertTrue(validator.specificationsAreYmlFiles(ACTIVE))
        Assertions.assertTrue(
            validator.checkSpecificationParsing(
                ACTIVE,
                ActiveSource::class.java,
            ),
        )
    }

    @Test
    @Throws(IOException::class)
    fun monitorIsYml() {
        Assertions.assertTrue(validator.specificationsAreYmlFiles(MONITOR))
        Assertions.assertTrue(
            validator.checkSpecificationParsing(
                MONITOR,
                MonitorSource::class.java,
            ),
        )
    }

    @Test
    @Throws(IOException::class)
    fun passiveIsYml() {
        Assertions.assertTrue(validator.specificationsAreYmlFiles(PASSIVE))
        Assertions.assertTrue(
            validator.checkSpecificationParsing(
                PASSIVE,
                PassiveSource::class.java,
            ),
        )
    }

    @Test
    @Throws(IOException::class)
    fun connectorIsYml() {
        Assertions.assertTrue(validator.specificationsAreYmlFiles(CONNECTOR))
        Assertions.assertTrue(
            validator.checkSpecificationParsing(
                CONNECTOR,
                ConnectorSource::class.java,
            ),
        )
    }

    @Test
    @Throws(IOException::class)
    fun pushIsYml() {
        Assertions.assertTrue(validator.specificationsAreYmlFiles(PUSH))
        Assertions.assertTrue(validator.checkSpecificationParsing(PUSH, PushSource::class.java))
    }

    @Test
    @Throws(IOException::class)
    fun streamIsYml() {
        Assertions.assertTrue(validator.specificationsAreYmlFiles(STREAM))
        Assertions.assertTrue(
            validator.checkSpecificationParsing(
                STREAM,
                StreamGroup::class.java,
            ),
        )
    }
}
