package org.radarbase.schema.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.radarbase.schema.specification.SourceCatalogue
import org.radarbase.schema.specification.config.SchemaConfig
import org.radarbase.schema.specification.config.SourceConfig
import java.nio.file.Paths
import kotlin.time.Duration.Companion.milliseconds

internal class SourceCatalogueServerTest {
    private lateinit var server: SourceCatalogueServer
    private lateinit var serverThread: Thread
    private var exception: Exception? = null

    @BeforeEach
    fun setUp() {
        exception = null
        server = SourceCatalogueServer(9876)
        serverThread = Thread {
            try {
                val sourceCatalog = runBlocking {
                    SourceCatalogue(Paths.get("../.."), SchemaConfig(), SourceConfig())
                }
                server.start(sourceCatalog)
            } catch (e: IllegalStateException) {
                // this is acceptable
            } catch (e: Exception) {
                exception = e
            }
        }
        serverThread.start()
    }

    @AfterEach
    @Throws(Exception::class)
    fun tearDown() {
        serverThread.interrupt()
        server.close()
        serverThread.join()
        exception?.let { throw it }
    }

    @Test
    fun sourceTypesTest(): Unit = runBlocking {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    },
                )
            }
        }

        val response = (0 until 5000).asFlow()
            .mapNotNull {
                try {
                    client.get("http://localhost:9876/source-types")
                        .takeIf { it.status.isSuccess() }
                } catch (ex: Exception) {
                    null
                }.also {
                    if (it == null) delay(10.milliseconds)
                }
            }
            .first()

        val body = response.body<JsonElement>()
        val obj = body.jsonObject
        assertTrue(obj.containsKey("passive-source-types"))
        obj["passive-source-types"]!!.jsonArray
    }
}
