package org.radarbase.schema.validation

import org.junit.jupiter.api.Assertions.assertTrue
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
        assertTrue(validator.specificationsAreYmlFiles(ACTIVE))
        assertTrue(
            validator.checkSpecificationParsing(
                ACTIVE,
                ActiveSource::class.java,
            ),
        )
    }

    @Test
    @Throws(IOException::class)
    fun monitorIsYml() {
        assertTrue(validator.specificationsAreYmlFiles(MONITOR))
        assertTrue(
            validator.checkSpecificationParsing(
                MONITOR,
                MonitorSource::class.java,
            ),
        )
    }

    @Test
    @Throws(IOException::class)
    fun passiveIsYml() {
        assertTrue(validator.specificationsAreYmlFiles(PASSIVE))
        assertTrue(
            validator.checkSpecificationParsing(
                PASSIVE,
                PassiveSource::class.java,
            ),
        )
    }

    @Test
    @Throws(IOException::class)
    fun connectorIsYml() {
        assertTrue(validator.specificationsAreYmlFiles(CONNECTOR))
        assertTrue(
            validator.checkSpecificationParsing(
                CONNECTOR,
                ConnectorSource::class.java,
            ),
        )
    }

    @Test
    @Throws(IOException::class)
    fun pushIsYml() {
        assertTrue(validator.specificationsAreYmlFiles(PUSH))
        assertTrue(validator.checkSpecificationParsing(PUSH, PushSource::class.java))
    }

    @Test
    @Throws(IOException::class)
    fun streamIsYml() {
        assertTrue(validator.specificationsAreYmlFiles(STREAM))
        assertTrue(
            validator.checkSpecificationParsing(
                STREAM,
                StreamGroup::class.java,
            ),
        )
    }
}
