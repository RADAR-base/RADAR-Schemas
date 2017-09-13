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

import org.junit.BeforeClass;
import org.junit.Test;
import org.radarcns.schema.specification.SourceCatalogue;
import org.radarcns.schema.specification.source.MonitorSource;
import org.radarcns.schema.specification.source.active.ActiveSource;
import org.radarcns.schema.specification.source.active.questionnaire.QuestionnaireSource;
import org.radarcns.schema.specification.source.passive.PassiveSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.radarcns.schema.specification.SourceCatalogue.BASE_PATH;
import static org.radarcns.schema.validation.ValidationSupport.isValidTopic;

/**
 * TODO.
 */
public class SourceCatalogueValidation {
    private static final Logger logger = LoggerFactory.getLogger(SourceCatalogueValidation.class);
    private static SourceCatalogue catalogue;

    @BeforeClass
    public static void setUp() throws IOException {
        catalogue = SourceCatalogue.load(BASE_PATH);
    }

    @Test
    public void checkActiveSourceType() {
        assertTrue("Not all " + QuestionnaireSource.RadarSourceTypes.class.getName()
                        + " have a specification",
                Arrays.stream(QuestionnaireSource.RadarSourceTypes.values())
                    .allMatch(type -> catalogue.getActiveSource(type.name()) != null));
    }

    @Test
    public void checkMonitorSourceType() {
        assertTrue("Not all " + MonitorSource.RadarSourceTypes.class.getName()
                        + " have a specification",
                Arrays.stream(MonitorSource.RadarSourceTypes.values())
                    .allMatch(type -> catalogue.getMonitorSource(type.name()) != null));
    }

    @Test
    public void checkPassiveSourceType() {
        assertEquals("Not all " + PassiveSource.RadarSourceTypes.class.getName()
                        + " have a specification", 0,
                Arrays.stream(PassiveSource.RadarSourceTypes.values())
                        .filter(type -> catalogue.getPassiveSource(type.name()) == null)
                        .peek(t -> logger.error("Passive source {} unknown", t))
                        .count());
    }

    @Test
    public void validateTopicNames() {
        catalogue.getTopics().forEach(topic ->
                assertTrue(topic + " is invalid", isValidTopic(topic)));
    }

    @Test
    public void validateTopics() {
        Set<String> expected = new HashSet<>();

        for (ActiveSource source : catalogue.getActiveSources().values()) {
            expected.addAll(source.getTopics());
        }
        for (MonitorSource source : catalogue.getMonitorSources().values()) {
            expected.addAll(source.getTopics());
        }
        for (PassiveSource source : catalogue.getPassiveSources().values()) {
            expected.addAll(source.getTopics());
        }

        expected.removeAll(catalogue.getTopics());
        assertTrue(expected.isEmpty());
    }
}
