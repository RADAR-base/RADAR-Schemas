package org.radarbase.schema.registration

import io.ktor.http.HttpHeaders
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.radarbase.schema.registration.SchemaRegistry.Compatibility.FORWARD
import org.radarbase.topic.AvroTopic
import org.radarcns.kafka.ObservationKey
import org.radarcns.passive.phone.PhoneAcceleration

class SchemaRegistryTest {
    private var server = MockWebServer()
    private val expectedAuthHeader = "Basic dXNlcm5hbWU6cGFzc3dvcmQ="
    lateinit var schemaRegistry: SchemaRegistry

    @BeforeEach
    fun setUpClass() {
        schemaRegistry = SchemaRegistry(
            server.url("/").toString(),
            "username",
            "password",
        )

        val mockresponse =
            MockResponse()
                .setHeader(HttpHeaders.ContentType, "application/json")
                .setBody(
                    """
                    {
                        "id": 1
                    }
                    """.trimIndent(),
                )

        server.enqueue(mockresponse)
        server.enqueue(mockresponse)
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun registerSchema() {
        // Create an instance of AvroTopic
        val avroTopic = AvroTopic(
            "test",
            ObservationKey.getClassSchema(),
            PhoneAcceleration.getClassSchema(),
            ObservationKey::class.java,
            PhoneAcceleration::class.java,
        )

        runBlocking {
            // Register the schema
            schemaRegistry.registerSchema(avroTopic)

            // Get the request that was received by the MockWebServer
            val request = server.takeRequest()

            // Verify the Basic Auth credentials
            val authHeader = request.getHeader("Authorization")
            assertEquals(expectedAuthHeader, authHeader)
        }
    }

    @Test
    fun putCompatibility() {
        runBlocking {
            // Register the schema
            schemaRegistry.putCompatibility(compatibility = FORWARD)

            // Get the request that was received by the MockWebServer
            val request = server.takeRequest()

            // Verify the Basic Auth credentials
            val authHeader = request.getHeader("Authorization")
            assertEquals(expectedAuthHeader, authHeader)
        }
    }
}
