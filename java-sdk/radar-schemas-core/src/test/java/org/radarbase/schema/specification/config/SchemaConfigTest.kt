package org.radarbase.schema.specification.config

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.radarbase.schema.SchemaCatalogue
import org.radarbase.schema.Scope
import org.radarbase.schema.validation.ValidationHelper.COMMONS_PATH
import java.nio.file.Paths
import kotlin.io.path.absolute

internal class SchemaConfigTest {

    @Test
    fun getMonitor() {
        val config = SchemaConfig(
            exclude = listOf("**"),
            monitor = mapOf(
                "application/test.avsc" to """
                {
                  "namespace": "org.radarcns.monitor.application",
                  "type": "record",
                  "name": "ApplicationUptime2",
                  "doc": "Length of application uptime.",
                  "fields": [
                    { "name": "time", "type": "double", "doc": "Device timestamp in UTC (s)." },
                    { "name": "uptime", "type": "double", "doc": "Time since last app start (s)." }
                  ]
                }
                """.trimIndent(),
            ),
        )
        val commonsRoot = Paths.get("../..").resolve(COMMONS_PATH)
            .absolute()
            .normalize()
        val schemaCatalogue = runBlocking {
            SchemaCatalogue(commonsRoot, config)
        }
        assertEquals(1, schemaCatalogue.schemas.size)
        val (fullName, schemaMetadata) = schemaCatalogue.schemas.entries.first()
        assertEquals("org.radarcns.monitor.application.ApplicationUptime2", fullName)
        assertEquals("org.radarcns.monitor.application.ApplicationUptime2", schemaMetadata.schema.fullName)
        assertEquals(commonsRoot.resolve("monitor/application/test.avsc"), schemaMetadata.path)
        assertEquals(Scope.MONITOR, schemaMetadata.scope)
    }
}
