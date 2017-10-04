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

package org.radarcns.schema.registration;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.avro.Schema;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.radarcns.schema.specification.DataProducer;
import org.radarcns.schema.specification.SourceCatalogue;
import org.radarcns.topic.AvroTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;

public final class SchemaRegistration {
    private static final Logger logger = LoggerFactory.getLogger(SchemaRegistration.class);

    private SchemaRegistration() {
    }

    public static boolean registerSchemas(String url, SourceCatalogue catalogue) {
        return setCompatibility(url, "NONE")
                && Stream.of(
                catalogue.getActiveSources(),
                catalogue.getPassiveSources(),
                catalogue.getMonitorSources())
                .flatMap(m -> m.values().stream())
                .flatMap(DataProducer::getTopics)
                .allMatch(topic -> registerSchemasForTopic(topic, url))
                && setCompatibility(url, "FULL");
    }

    private static boolean registerSchemasForTopic(AvroTopic<?, ?> topic, String url) {
        return registerSchema(topic.getKeySchema(),
                topic.getName() + "-key", url)
                && registerSchema(topic.getValueSchema(),
                topic.getName() + "-value", url);
    }

    private static boolean registerSchema(Schema schema, String subject, String url) {
        logger.info("Registering {}", subject);
        HttpPost request = new HttpPost(url + "/subjects/" + subject + "/versions");
        try {
            request.addHeader("Content-Type", "application/vnd.schemaregistry.v1+json");
            request.setEntity(new StringEntity(schemaEntity(schema)));

            HttpResponse response = createHttpClient().execute(request);
            boolean ok = response.getStatusLine().getStatusCode() == 200;
            if (ok) {
                logger.info("OK");
                return true;
            } else {
                logger.error(response.getStatusLine().toString());
            }
        } catch (Exception e) {
            logger.error("Error registering a schema for subject " + subject, e);
        } finally {
            request.releaseConnection();
        }
        return false;
    }

    private static String schemaEntity(Schema schema) throws IOException {
        StringWriter writer = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(writer);
        gen.writeStartObject();
        gen.writeStringField("schema", schema.toString());
        gen.writeEndObject();
        gen.flush();
        return writer.toString();
    }

    private static boolean setCompatibility(String url, String compatibility) {
        logger.info("Setting compatibility to {}", compatibility);
        HttpPut request = new HttpPut(url + "/config");
        try {
            request.addHeader("Content-Type", "application/vnd.schemaregistry.v1+json");
            request.setEntity(new StringEntity("{\"compatibility\": \"" + compatibility + "\"}"));

            HttpResponse response = createHttpClient().execute(request);
            boolean ok = response.getStatusLine().getStatusCode() == 200;
            if (ok) {
                logger.info("OK");
                return true;
            } else {
                logger.error(response.getStatusLine().toString());
            }
        } catch (Exception e) {
            logger.error("Error changing compatibility level", e);
        } finally {
            request.releaseConnection();
        }
        return false;
    }

    private static HttpClient createHttpClient()
            throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial((chain, authType) -> true)
                .build();

        SSLConnectionSocketFactory sslSocketFactory =
                new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

        Registry<ConnectionSocketFactory> socketFactoryRegistry =
                RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .register("https", sslSocketFactory)
                        .build();

        return HttpClientBuilder.create()
                .setSSLContext(sslContext)
                .setConnectionManager(new PoolingHttpClientConnectionManager(socketFactoryRegistry))
                .build();
    }
}
