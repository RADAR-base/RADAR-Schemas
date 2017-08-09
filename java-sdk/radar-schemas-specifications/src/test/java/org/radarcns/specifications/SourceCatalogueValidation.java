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
import static org.radarcns.specifications.SourceCatalogue.getActiveSource;
import static org.radarcns.specifications.SourceCatalogue.getMonitorSource;
import static org.radarcns.specifications.SourceCatalogue.getPassiveSource;
import static org.radarcns.specifications.validator.ValidationSupport.isValidTopic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.junit.Test;
import org.radarcns.active.questionnaire.QuestionnaireType;
import org.radarcns.catalogue.MonitorSourceType;
import org.radarcns.catalogue.PassiveSourceType;
import org.radarcns.specifications.source.active.questionnaire.QuestionnaireSource;
import org.radarcns.specifications.source.passive.MonitorSource;
import org.radarcns.specifications.source.passive.PassiveSource;

/**
 * TODO.
 */
public class SourceCatalogueValidation {

    @Test
    public void checkActiveSourceType() {
        assertTrue("Not all " + QuestionnaireType.class.getName() + " has a specification",
                Arrays.stream(QuestionnaireType.values())
                        .filter(type -> !type.name().equals(QuestionnaireType.UNKNOWN.name()))
                        .allMatch(type -> getActiveSource(type) != null));
    }

    @Test
    public void checkMonitorSourceType() {
        assertTrue("Not all " + MonitorSourceType.class.getName() + " has a specification",
            Arrays.stream(MonitorSourceType.values())
                .filter(type -> !type.name().equals(MonitorSourceType.UNKNOWN.name()))
                .allMatch(type -> getMonitorSource(type) != null));
    }

    @Test
    public void checkPassiveSourceType() {
        assertTrue("Not all " + PassiveSourceType.class.getName() + " has a specification",
                Arrays.stream(PassiveSourceType.values())
                        .filter(type -> !type.name().equals(PassiveSourceType.UNKNOWN.name()))
                        .allMatch(type -> getPassiveSource(type) != null));
    }

    //TODO
    /*@Test
    public void validateTopicNamesVerbose() {
        for (Entry<String, Map<String, Set<String>>> source
                : SourceCatalogue.getTopicsVerbose().entrySet()) {
            for (Entry<String, Set<String>> details : source.getValue().entrySet()) {
                details.getValue().forEach(topic -> assertTrue(
                    topic + " in " + source.getKey() + "-" + details.getKey()
                            + " is invalid", isValidTopic(topic)));
            }
        }
    }*/

    @Test
    public void validateTopicNames() {
        SourceCatalogue.getTopics().forEach(topic ->
                assertTrue(topic + " is invalid", isValidTopic(topic)));
    }

    @Test
    public void validateTopics() {
        Set<String> expected = new HashSet<>();

        for (QuestionnaireSource source : SourceCatalogue.getActiveSources().values()) {
            expected.addAll(source.getTopics());
        }
        for (MonitorSource source : SourceCatalogue.getMonitorSources().values()) {
            expected.addAll(source.getTopics());
        }
        for (PassiveSource source : SourceCatalogue.getPassiveSources().values()) {
            expected.addAll(source.getTopics());
        }

        expected.removeAll(SourceCatalogue.getTopics());
        assertTrue(expected.isEmpty());
    }
}
