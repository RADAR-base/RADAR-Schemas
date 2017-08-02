package org.radarcns.specifications;

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

import static org.junit.Assert.assertTrue;
import static org.radarcns.specifications.SourceCatalogue.getActiveSources;
import static org.radarcns.specifications.SourceCatalogue.getMonitorSources;
import static org.radarcns.specifications.SourceCatalogue.getPassiveSources;

import java.util.List;
import org.apache.kafka.common.errors.InvalidTopicException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.MatchError;

/**
 * TODO.
 */
public class SourceCatalogueTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SourceCatalogueTest.class);

    @Test
    public void validateTopicName() {
        assertTrue(getActiveSources().values().stream().allMatch(source ->
                validateTopicName(source.getName(), source.getTopicName())));

        assertTrue(getMonitorSources().values().stream().allMatch(source ->
                validateTopicName(source.getName(), source.getTopicName())));

        assertTrue(getPassiveSources().values().stream()
                    .map(passiveSource -> passiveSource.getSensors())
                    .flatMap(List::stream).allMatch(sensor ->
                validateTopicName(sensor.getName().name(), sensor.getTopicName())));
    }

    private static boolean validateTopicName(String origin, String topicName) {
        try {
            kafka.common.Topic.validate(topicName);
            return true;
        } catch (InvalidTopicException | MatchError exc) {
            LOGGER.error("Topic {} in {} is invalid", topicName, origin);
            return false;
        }
    }
}
