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

import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import org.apache.avro.specific.SpecificRecord
import org.radarbase.kotlin.coroutines.forkJoin
import org.radarbase.producer.io.timeout
import org.radarbase.producer.rest.RestException
import org.radarbase.producer.schema.SchemaRetriever
import org.radarbase.producer.schema.SchemaRetriever.Companion.schemaRetriever
import org.radarbase.schema.registration.KafkaTopics.Companion.retryDelayFlow
import org.radarbase.schema.specification.SourceCatalogue
import org.radarbase.schema.specification.config.TopicConfig
import org.radarbase.topic.AvroTopic
import org.radarcns.kafka.ObservationKey
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.MalformedURLException
import kotlin.streams.asSequence
import kotlin.time.Duration.Companion.seconds

/**
 * Schema registry interface.
 *
 * @param baseUrl URL of the schema registry
 * @throws MalformedURLException if given URL is invalid.
 */
class SchemaRegistry @JvmOverloads constructor(
    private val baseUrl: String,
    apiKey: String? = null,
    apiSecret: String? = null,
    private val topicConfiguration: Map<String, TopicConfig> = emptyMap(),
) {
    private val schemaClient: SchemaRetriever = schemaRetriever(baseUrl) {
        httpClient {
            timeout(10.seconds)
            if (apiKey != null && apiSecret != null) {
                install(Auth) {
                    basic {
                        credentials {
                            BasicAuthCredentials(username = apiKey, password = apiSecret)
                        }
                        sendWithoutRequest {
                            it.url.toString().startsWith(baseUrl)
                        }
                    }
                }
            }
        }
    }

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
            retryDelayFlow(2.seconds, MAX_SLEEP)
                .take(20)
                .map {
                    try {
                        schemaClient.restClient
                            .request<List<String>> {
                                url("subjects")
                            }
                            .isNotEmpty()
                    } catch (ex: RestException) {
                        logger.error(
                            "Schema registry {} not ready, responded with HTTP {}: {}",
                            baseUrl,
                            ex.status,
                            ex.message,
                        )
                        false
                    } catch (e: IOException) {
                        logger.error(
                            "Failed to connect to schema registry {}",
                            baseUrl,
                        )
                        false
                    }
                }
                .firstOrNull { it },
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
            .filter { it.doRegisterSchema() }
            .flatMap { it.getTopics(catalogue.schemaCatalogue).asSequence() }
            .distinctBy { it.name }
            .mapNotNull { topic ->
                val topicConfig = topicConfiguration[topic.name] ?: return@mapNotNull topic
                loadAvroTopic(topic.name, topicConfig, topic)
            }
            .toList()

        val sourceTopicNames = sourceTopics.mapTo(HashSet()) { it.name }

        val configuredTopics = topicConfiguration
            .filterKeys { it !in sourceTopicNames }
            .mapNotNull { (name, topicConfig) -> loadAvroTopic(name, topicConfig) }

        return (sourceTopics + configuredTopics)
            .sortedBy(AvroTopic<*, *>::name)
            .forkJoin { t ->
                logger.info(
                    "Registering topic {} schemas: {} - {}",
                    t.name,
                    t.keySchema.fullName,
                    t.valueSchema.fullName,
                )
                registerSchema(t)
            }
            .all { it }
    }

    private fun loadAvroTopic(
        name: String,
        topicConfig: TopicConfig,
        defaultTopic: AvroTopic<*, *>? = null,
    ): AvroTopic<*, *>? {
        if (!topicConfig.enabled || !topicConfig.registerSchema) return null

        val keySchemaString = topicConfig.keySchema
        val valueSchemaString = topicConfig.valueSchema
        if (keySchemaString == null && valueSchemaString == null) return defaultTopic

        val (keyClass, keySchema) = when {
            keySchemaString != null -> {
                val record: SpecificRecord = AvroTopic.parseSpecificRecord(keySchemaString)
                record.javaClass to record.schema
            }
            defaultTopic != null -> defaultTopic.keyClass to defaultTopic.keySchema
            else -> ObservationKey::class.java to ObservationKey.`SCHEMA$`
        }
        val (valueClass, valueSchema) = when {
            valueSchemaString != null -> {
                val record: SpecificRecord = AvroTopic.parseSpecificRecord(valueSchemaString)
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
            val addKey = async {
                schemaClient.addSchema(topic.name, false, topic.keySchema)
            }
            val addValue = async {
                schemaClient.addSchema(topic.name, true, topic.valueSchema)
            }
            addKey.await()
            addValue.await()
            true
        } catch (ex: Exception) {
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
            schemaClient.restClient.requestEmpty {
                url("config")
                method = HttpMethod.Put
                setBody("""{"compatibility": "${compatibility.name}"}""")
                contentType(ContentType.parse("application/vnd.schemaregistry.v1+json; charset=utf-8"))
            }
            true
        } catch (ex: RestException) {
            logger.error(
                "Failed to change compatibility level to {} (status code {}): {}",
                compatibility,
                ex.status,
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
        private val MAX_SLEEP = 32.seconds
    }
}
