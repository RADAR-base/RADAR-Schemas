package org.radarcns.schema.validation;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.radarcns.schema.specification.SourceCatalogue.BASE_PATH;
import static org.radarcns.schema.validation.ValidationSupport.isValidTopic;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.BeforeClass;
import org.junit.Test;
import org.radarcns.schema.specification.DataProducer;
import org.radarcns.schema.specification.SourceCatalogue;

/**
 * TODO.
 */
public class SourceCatalogueValidation {
    private static SourceCatalogue catalogue;

    @BeforeClass
    public static void setUp() throws IOException {
        catalogue = SourceCatalogue.load(BASE_PATH);
    }

    @Test
    public void validateTopicNames() {
        catalogue.getTopicNames().forEach(topic ->
                assertTrue(topic + " is invalid", isValidTopic(topic)));
    }

    @Test
    public void validateTopics() {
        List<String> expected = Stream.of(
                catalogue.getActiveSources(),
                catalogue.getMonitorSources(),
                catalogue.getPassiveSources(),
                catalogue.getStreamGroups())
                .flatMap(map -> map.values().stream())
                .flatMap(DataProducer::getTopicNames)
                .sorted()
                .collect(Collectors.toList());

        assertEquals(expected, catalogue.getTopicNames().sorted().collect(Collectors.toList()));
    }

    @Test
    public void validateTopicSchemas() {
        catalogue.getSources().stream()
                .flatMap(source -> source.getData().stream())
                .forEach(data -> {
                    try {
                        assertTrue(data.getTopics().count() > 0);
                    } catch (IOException ex) {
                        fail("Cannot create topic from specification: " + ex);
                    }
                });
    }
}
