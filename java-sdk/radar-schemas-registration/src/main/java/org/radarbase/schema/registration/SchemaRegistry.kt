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

import okhttp3.Credentials.basic
import okhttp3.Headers.Companion.headersOf
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.radarbase.producer.rest.SchemaRetriever
import org.radarbase.producer.rest.RestClient
import org.radarcns.kafka.ObservationKey
import kotlin.Throws
import org.radarbase.schema.specification.SourceCatalogue
import org.radarbase.topic.AvroTopic
import okio.BufferedSink
import org.apache.avro.specific.SpecificRecord
import org.radarbase.config.ServerConfig
import org.radarbase.schema.registration.KafkaTopics.Companion.retrySequence
import org.slf4j.LoggerFactory
import java.io.IOException
import java.lang.IllegalStateException
import java.net.MalformedURLException
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.streams.asSequence

/**
 * Schema registry interface.
 *
 * @param baseUrl URL of the schema registry
 * @throws MalformedURLException if given URL is invalid.
 */
class SchemaRegistry @JvmOverloads constructor(
    baseUrl: String,
    apiKey: String? = null,
    apiSecret: String? = null,
    private val topicConfiguration: Map<String, TopicConfig> = emptyMap(),
) {
    private val httpClient: RestClient = RestClient.global().apply {
        timeout(10, TimeUnit.SECONDS)
        server(ServerConfig(baseUrl).apply {
            isUnsafe = false
        })
        if (apiKey != null && apiSecret != null) {
            headers(headersOf("Authorization", basic(apiKey, apiSecret)))
        }
    }.build()
    private val schemaClient: SchemaRetriever = SchemaRetriever(httpClient)

    /**
     * Wait for schema registry to become available. This uses a polling mechanism, waiting for at
     * most 200 seconds.
     *
     * @throws InterruptedException when waiting for the brokers is interrupted.
     * @throws IllegalStateException if the schema registry is not ready after wait is finished.
     */
    @Throws(InterruptedException::class)
    fun initialize() {
        check(
            retrySequence(startSleep = Duration.ofSeconds(2), maxSleep = MAX_SLEEP)
                .take(20)
                .any {
                    try {
                        httpClient.request("subjects").use { response ->
                            if (response.isSuccessful) {
                                true
                            } else {
                                logger.error("Schema registry {} not ready, responded with HTTP {}: {}",
                                    httpClient.server, response.code,
                                    RestClient.responseBody(response))
                                false
                            }
                        }
                    } catch (e: IOException) {
                        logger.error("Failed to connect to schema registry {}",
                            httpClient.server)
                        false
                    }
                }
        ) { "Schema registry ${httpClient.server} not available" }
    }

    /**
     * Register all schemas in a source catalogue. Stream and connector sources are ignored.
     *
     * @param catalogue schema catalogue to read schemas from
     * @return whether all schemas were successfully registered.
     */
    fun registerSchemas(catalogue: SourceCatalogue): Boolean {
        val sourceTopics = catalogue.sources.asSequence()
            .filter { it.doRegisterSchema() }
            .flatMap { it.getTopics(catalogue.schemaCatalogue).asSequence() }
            .distinctBy { it.name }
            .mapNotNull { topic ->
                val topicConfig = topicConfiguration[topic.name] ?: return@mapNotNull topic
                loadAvroTopic(topic.name, topicConfig, topic)
            }
            .toList()

        val remainingTopics = topicConfiguration.toMutableMap()
        sourceTopics.forEach { remainingTopics -= it.name }

        val configuredTopics = remainingTopics
            .mapNotNull { (name, topicConfig) -> loadAvroTopic(name, topicConfig) }

        return (sourceTopics.asSequence() + configuredTopics.asSequence())
            .sortedBy(AvroTopic<*, *>::getName)
            .onEach { t -> logger.info(
                "Registering topic {} schemas: {} - {}",
                t.name,
                t.keySchema.fullName,
                t.valueSchema.fullName,
            ) }
            .map(::registerSchema)
            .reduceOrNull { a, b -> a && b }
            ?: true
    }

    private fun loadAvroTopic(
        name: String,
        topicConfig: TopicConfig,
        defaultTopic: AvroTopic<*, *>? = null,
    ): AvroTopic<*, *>? {
        if (!topicConfig.enabled || !topicConfig.registerSchema) return null
        if (topicConfig.keySchema == null && topicConfig.valueSchema == null) return defaultTopic
        val (keyClass, keySchema) = when {
            topicConfig.keySchema != null -> {
                val record: SpecificRecord = AvroTopic.parseSpecificRecord(topicConfig.keySchema)
                record.javaClass to record.schema
            }
            defaultTopic != null -> defaultTopic.keyClass to defaultTopic.keySchema
            else -> ObservationKey::class.java to ObservationKey.`SCHEMA$`
        }
        val (valueClass, valueSchema) = when {
            topicConfig.valueSchema != null -> {
                val record: SpecificRecord = AvroTopic.parseSpecificRecord(topicConfig.valueSchema)
                record.javaClass to record.schema
            }
            defaultTopic != null -> defaultTopic.valueClass to defaultTopic.valueSchema
            else -> {
                logger.warn("For topic {} the key schema is specified but the value schema is not",
                    name)
                return null
            }
        }

        return AvroTopic(name, keySchema, valueSchema, keyClass, valueClass)
    }

    /**
     * Register the schema of a single topic.
     */
    fun registerSchema(topic: AvroTopic<*, *>): Boolean {
        return try {
            schemaClient.addSchema(topic.name, false, topic.keySchema)
            schemaClient.addSchema(topic.name, true, topic.valueSchema)
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
    fun putCompatibility(compatibility: Compatibility): Boolean {
        logger.info("Setting compatibility to {}", compatibility)
        val request = try {
            httpClient.requestBuilder("config")
                .put(object : RequestBody() {
                    override fun contentType(): MediaType? =
                        "application/vnd.schemaregistry.v1+json; charset=utf-8"
                            .toMediaTypeOrNull()

                    @Throws(IOException::class)
                    override fun writeTo(sink: BufferedSink) {
                        sink.writeUtf8("{\"compatibility\": \"")
                        sink.writeUtf8(compatibility.name)
                        sink.writeUtf8("\"}")
                    }
                })
                .build()
        } catch (ex: MalformedURLException) {
            // should not occur with valid base URL
            return false
        }
        return try {
            httpClient.request(request).use { response ->
                response.body.use { body ->
                    if (response.isSuccessful) {
                        logger.info("Compatibility set to {}", compatibility)
                        true
                    } else {
                        val bodyString = body?.string()
                        logger.info("Failed to set compatibility set to {}: {}",
                            compatibility,
                            bodyString)
                        false
                    }
                }
            }
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
