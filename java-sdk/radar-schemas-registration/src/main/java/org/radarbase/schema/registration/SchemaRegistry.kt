/*
 * Copyright 2017 King's College London and The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.radarbase.schema.registration

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.request.basicAuth
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.take
import org.apache.avro.specific.SpecificRecord
import org.radarbase.kotlin.coroutines.forkJoin
import org.radarbase.producer.io.timeout
import org.radarbase.producer.rest.RestException
import org.radarbase.producer.schema.SchemaRetriever
import org.radarbase.producer.schema.SchemaRetriever.Companion.schemaRetriever
import org.radarbase.schema.registration.KafkaTopics.Companion.retryFlow
import org.radarbase.schema.specification.SourceCatalogue
import org.radarbase.schema.specification.config.TopicConfig
import org.radarbase.topic.AvroTopic
import org.radarcns.kafka.ObservationKey
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.MalformedURLException
import java.time.Duration
import kotlin.streams.asSequence
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toKotlinDuration

/**
 * Schema registry interface.
 *
 * @param baseUrl URL of the schema registry
 * @throws MalformedURLException if given URL is invalid.
 */
class SchemaRegistry(
    private val baseUrl: String,
    private val apiKey: String? = null,
    private val apiSecret: String? = null,
    private val topicConfiguration: Map<String, TopicConfig> = emptyMap(),
) {
    private val schemaClient: SchemaRetriever = schemaRetriever(baseUrl) {
        httpClient = HttpClient(CIO) {
            timeout(10.seconds)
            if (apiKey != null && apiSecret != null) {
                install(Auth) {
                    basic {
                        sendWithoutRequest { true }
                        credentials {
                            BasicAuthCredentials(username = apiKey, password = apiSecret)
                        }
                        realm = "Access to the '/' path"
                    }
                }
            }
        }
    }
    private val httpClient = schemaClient.restClient

    /**
     * Wait for schema registry to become available. This uses a polling mechanism, waiting for at
     * most 200 seconds.
     *
     * @throws InterruptedException when waiting for the brokers is interrupted.
     * @throws IllegalStateException if the schema registry is not ready after wait is finished.
     */
    @Throws(InterruptedException::class)
    suspend fun initialize() {
        checkNotNull(
            retryFlow(startSleep = 2.seconds, maxSleep = MAX_SLEEP.toKotlinDuration())
                .take(20)
                .mapNotNull {
                    try {
                        httpClient.request<List<String>> {
                            url("subjects")
                            if (apiKey != null && apiSecret != null) {
                                basicAuth(apiKey, apiSecret)
                            }
                        }
                    } catch (ex: RestException) {
                        logger.error(
                            "Schema registry {} not ready, responded with HTTP {}: {}",
                            baseUrl,
                            ex.status,
                            ex.message,
                        )
                        null
                    } catch (e: IOException) {
                        logger.error("Failed to connect to schema registry {}", e.toString())
                        null
                    }
                }
                .firstOrNull(),
        ) { "Schema registry $baseUrl not available" }
    }

    /**
     * Register all schemas in a source catalogue. Stream and connector sources are ignored.
     *
     * @param catalogue schema catalogue to read schemas from
     * @return whether all schemas were successfully registered.
     */
    suspend fun registerSchemas(catalogue: SourceCatalogue): Boolean {
        val sourceTopics = catalogue.sources.asSequence()
            .filter { it.registerSchema }
            .flatMap { it.topics(catalogue.schemaCatalogue).asSequence() }
            .distinctBy { it.name }
            .mapNotNull { topic ->
                val topicConfig = topicConfiguration[topic.name] ?: return@mapNotNull topic
                loadAvroTopic(topic.name, topicConfig, topic)
            }
            .toList()

        val remainingTopics = buildMap(topicConfiguration.size) {
            putAll(topicConfiguration)
            sourceTopics.forEach {
                remove(it.name)
            }
        }
        val configuredTopics = remainingTopics
            .mapNotNull { (name, topicConfig) -> loadAvroTopic(name, topicConfig) }

        return (sourceTopics + configuredTopics)
            .sortedBy(AvroTopic<*, *>::name)
            .forkJoin { topic ->
                logger.info(
                    "Registering topic {} schemas: {} - {}",
                    topic.name,
                    topic.keySchema.fullName,
                    topic.valueSchema.fullName,
                )
                registerSchema(topic)
            }
            .all { it }
    }

    private fun loadAvroTopic(
        name: String,
        topicConfig: TopicConfig,
        defaultTopic: AvroTopic<*, *>? = null,
    ): AvroTopic<*, *>? {
        if (!topicConfig.enabled || !topicConfig.registerSchema) return null
        val topicKeySchema = topicConfig.keySchema
        val topicValueSchema = topicConfig.valueSchema

        if (topicKeySchema == null && topicValueSchema == null) return defaultTopic
        val (keyClass, keySchema) = when {
            topicKeySchema != null -> {
                val record: SpecificRecord = AvroTopic.parseSpecificRecord(topicKeySchema)
                record.javaClass to record.schema
            }

            defaultTopic != null -> defaultTopic.keyClass to defaultTopic.keySchema
            else -> ObservationKey::class.java to ObservationKey.`SCHEMA$`
        }
        val (valueClass, valueSchema) = when {
            topicValueSchema != null -> {
                val record: SpecificRecord = AvroTopic.parseSpecificRecord(topicValueSchema)
                record.javaClass to record.schema
            }

            defaultTopic != null -> defaultTopic.valueClass to defaultTopic.valueSchema
            else -> {
                logger.warn(
                    "For topic {} the key schema is specified but the value schema is not",
                    name,
                )
                return null
            }
        }

        return AvroTopic(name, keySchema, valueSchema, keyClass, valueClass)
    }

    /**
     * Register the schema of a single topic.
     */
    suspend fun registerSchema(topic: AvroTopic<*, *>): Boolean = coroutineScope {
        try {
            listOf(
                async {
                    schemaClient.addSchema(topic.name, false, topic.keySchema)
                },
                async {
                    schemaClient.addSchema(topic.name, true, topic.valueSchema)
                },
            ).awaitAll()
            true
        } catch (ex: IOException) {
            logger.error("Failed to register schemas for topic {}", topic.name, ex)
            false
        }
    }

    /**
     * Set the compatibility level of the schema registry.
     *
     * @param compatibility target compatibility level.
     * @return whether the request was successful.
     */
    suspend fun putCompatibility(compatibility: Compatibility): Boolean {
        logger.info("Setting compatibility to {}", compatibility)
        return try {
            httpClient.requestEmpty {
                url("config")
                method = HttpMethod.Put
                contentType(ContentType("application", "vnd.schemaregistry.v1+json"))
                setBody("{\"compatibility\": \"${compatibility.name}\"}")
            }
            logger.info("Compatibility set to {}", compatibility)
            true
        } catch (ex: RestException) {
            logger.info(
                "Failed to set compatibility set to {}: {}",
                compatibility,
                ex.message,
            )
            false
        } catch (ex: IOException) {
            logger.error("Error changing compatibility level to {}", compatibility, ex)
            false
        }
    }

    enum class Compatibility {
        NONE, FULL, BACKWARD, FORWARD, BACKWARD_TRANSITIVE, FORWARD_TRANSITIVE, FULL_TRANSITIVE
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SchemaRegistry::class.java)
        private val MAX_SLEEP = Duration.ofSeconds(32)
    }
}
