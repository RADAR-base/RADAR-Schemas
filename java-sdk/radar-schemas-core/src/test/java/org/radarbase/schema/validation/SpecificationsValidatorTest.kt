package org.radarbase.schema.validation

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
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
import org.radarbase.schema.validation.ValidationHelper.SPECIFICATIONS_PATH
import java.io.IOException

class SpecificationsValidatorTest {
    private lateinit var validator: SpecificationsValidator

    @BeforeEach
    fun setUp() {
        validator = SpecificationsValidator(SourceCatalogueValidationTest.BASE_PATH.resolve(SPECIFICATIONS_PATH), SchemaConfig())
    }

    @Test
    @Throws(IOException::class)
    fun activeIsYml() = runBlocking {
        val validator = validator.ofScope(ACTIVE) ?: return@runBlocking
        val result = validator.isValidSpecification(ActiveSource::class.java)
        assertEquals("", result.toFormattedString())
    }

    @Test
    @Throws(IOException::class)
    fun monitorIsYml() = runBlocking {
        val validator = validator.ofScope(MONITOR) ?: return@runBlocking
        val result = validator.isValidSpecification(MonitorSource::class.java)
        assertEquals("", result.toFormattedString())
    }

    @Test
    @Throws(IOException::class)
    fun passiveIsYml() = runBlocking {
        val validator = validator.ofScope(PASSIVE) ?: return@runBlocking
        val result = validator.isValidSpecification(PassiveSource::class.java)
        assertEquals("", result.toFormattedString())
    }

    @Test
    @Throws(IOException::class)
    fun connectorIsYml() = runBlocking {
        val validator = validator.ofScope(CONNECTOR) ?: return@runBlocking
        val result = validator.isValidSpecification(ConnectorSource::class.java)
        assertEquals("", result.toFormattedString())
    }

    @Test
    @Throws(IOException::class)
    fun pushIsYml() = runBlocking {
        val validator = validator.ofScope(PUSH) ?: return@runBlocking
        val result = validator.isValidSpecification(PushSource::class.java)
        assertEquals("", result.toFormattedString())
    }

    @Test
    @Throws(IOException::class)
    fun streamIsYml() = runBlocking {
        val validator = validator.ofScope(STREAM) ?: return@runBlocking
        val result = validator.isValidSpecification(StreamGroup::class.java)
        assertEquals("", result.toFormattedString())
    }
}
