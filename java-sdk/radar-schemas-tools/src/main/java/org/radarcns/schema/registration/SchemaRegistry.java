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

import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import org.apache.avro.Schema;
import org.radarcns.config.ServerConfig;
import org.radarcns.producer.rest.ManagedConnectionPool;
import org.radarcns.producer.rest.ParsedSchemaMetadata;
import org.radarcns.producer.rest.RestClient;
import org.radarcns.producer.rest.SchemaRetriever;
import org.radarcns.schema.CommandLineApp;
import org.radarcns.schema.specification.DataProducer;
import org.radarcns.schema.specification.SourceCatalogue;
import org.radarcns.schema.util.SubCommand;
import org.radarcns.topic.AvroTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.stream.Stream;

public class SchemaRegistry implements Closeable {
    public enum Compatibility {
        NONE, FULL, BACKWARD, FORWARD, BACKWARD_TRANSITIVE, FORWARD_TRANSITIVE, FULL_TRANSITIVE
    }

    private static final Logger logger = LoggerFactory.getLogger(SchemaRegistry.class);
    private final SchemaRetriever schemaClient;
    private final RestClient httpClient;

    public SchemaRegistry(String baseUrl) throws MalformedURLException {
        ServerConfig config = new ServerConfig(baseUrl);
        config.setUnsafe(true);
        this.schemaClient = new SchemaRetriever(config, 10);
        this.httpClient = new RestClient(config, 10, ManagedConnectionPool.GLOBAL_POOL);
    }

    public boolean registerSchemas(SourceCatalogue catalogue) {
        return Stream.of(
                catalogue.getActiveSources(),
                catalogue.getPassiveSources(),
                catalogue.getMonitorSources())
                .flatMap(m -> m.values().stream())
                .flatMap(DataProducer::getTopics)
                .allMatch(this::registerSchema);
    }

    public boolean registerSchema(AvroTopic<?, ?> topic) {
        try {
            Schema schema = topic.getKeySchema();
            logger.info("Registering topic {} key schema: {}",
                    topic.getName(), schema.getFullName());
            ParsedSchemaMetadata metadata = new ParsedSchemaMetadata(null, null, schema);
            this.schemaClient.addSchemaMetadata(topic.getName(), false, metadata);

            schema = topic.getValueSchema();
            logger.info("Registering topic {} value schema: {}",
                    topic.getName(), schema.getFullName());
            metadata = new ParsedSchemaMetadata(null, null, schema);
            this.schemaClient.addSchemaMetadata(topic.getName(), true, metadata);
            return true;
        } catch (IOException ex) {
            logger.error("Failed to register schemas for topic {}", topic.getName(), ex);
            return false;
        }
    }

    public boolean setCompatibility(Compatibility compatibility) {
        logger.info("Setting compatibility to {}", compatibility);

        Request request;
        try {
            request = httpClient.requestBuilder("config")
                    .put(new RequestBody() {
                        @Override
                        public MediaType contentType() {
                            return MediaType.parse(
                                    "application/vnd.schemaregistry.v1+json; charset=utf-8");
                        }

                        @Override
                        public void writeTo(BufferedSink sink) throws IOException {
                            sink.writeUtf8("{\"compatibility\": \"");
                            sink.writeUtf8(compatibility.name());
                            sink.writeUtf8("\"}");
                        }
                    })
                    .build();
        } catch (MalformedURLException ex) {
            // should not occur with valid base URL
            return false;
        }

        try (Response response = httpClient.request(request)) {
            ResponseBody body = response.body();
            if (response.isSuccessful()) {
                logger.info("Compatibility set to {}", compatibility);
                return true;
            } else {
                String bodyString = body == null ? null : body.string();
                logger.info("Failed to set compatibility set to {}: {}", compatibility, bodyString);
                return false;
            }
        } catch (IOException ex) {
            logger.error("Error changing compatibility level to {}", compatibility, ex);
            return false;
        }
    }

    public void close() {
        schemaClient.close();
        httpClient.close();
    }

    public static SubCommand command() {
        return new SubCommand() {
            @Override
            public String getName() {
                return "register";
            }

            @Override
            public int execute(Namespace options, CommandLineApp app) {
                String url = options.get("schemaRegistry");
                try (SchemaRegistry registration = new SchemaRegistry(url)) {
                    boolean forced = options.getBoolean("force");
                    if (forced) {
                        forced = registration.setCompatibility(SchemaRegistry.Compatibility.NONE);
                    }
                    int result = registration.registerSchemas(app.getCatalogue()) ? 0 : 1;
                    if (forced) {
                        registration.setCompatibility(SchemaRegistry.Compatibility.FULL);
                    }
                    return result;
                } catch (MalformedURLException ex) {
                    logger.error("Schema registry URL {} is invalid: {}", url, ex.toString());
                    return 1;
                }
            }

            @Override
            public void addParser(ArgumentParser parser) {
                parser.description("Register schemas in the schema registry.");
                parser.addArgument("-f", "--force")
                        .help("force registering schema, even if it is incompatible")
                        .action(Arguments.storeTrue());
                parser.addArgument("schemaRegistry")
                        .help("schema registry URL");
                SubCommand.addRootArgument(parser);
            }
        };
    }

}
