package org.radarcns.specifications.source.passive;

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

import static org.radarcns.specifications.util.Labels.AGGREGATOR;
import static org.radarcns.specifications.util.Labels.APP_PROVIDER;
import static org.radarcns.specifications.util.Labels.DATA_TYPE;
import static org.radarcns.specifications.util.Labels.DOC;
import static org.radarcns.specifications.util.Labels.KEY;
import static org.radarcns.specifications.util.Labels.NAME;
import static org.radarcns.specifications.util.Labels.SAMPLE_RATE;
import static org.radarcns.specifications.util.Labels.TOPIC;
import static org.radarcns.specifications.util.Labels.UNIT;
import static org.radarcns.specifications.util.Labels.VALUE;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.Set;
import org.radarcns.catalogue.DataType;
import org.radarcns.catalogue.MonitorSourceType;
import org.radarcns.catalogue.Unit;
import org.radarcns.specifications.source.KafkaActor;
import org.radarcns.specifications.source.Source;
import org.radarcns.specifications.source.Topic;
import org.radarcns.specifications.util.Utils;

/**
 * TODO.
 */
public class MonitorSource extends Source {

    private final MonitorSourceType type;

    private final String appProvider;

    private final KafkaActor kafkaActor;

    private static final String NULL_MESSAGE = " in ".concat(
            MonitorSource.class.getName()).concat(" cannot be null.");

    /**
     * TODO.
     *
     * @param name TODO
     * @param appProvider TODO
     * @param doc TODO
     * @param sampleRate TODO
     * @param unit TODO
     * @param dataType TODO
     * @param topic TODO
     * @param key TODO
     * @param value TODO
     * @param aggregator TODO
     */
    @JsonCreator
    public MonitorSource(
            @JsonProperty(NAME) MonitorSourceType name,
            @JsonProperty(APP_PROVIDER) String appProvider,
            @JsonProperty(DOC) String doc,
            @JsonProperty(SAMPLE_RATE) double sampleRate,
            @JsonProperty(UNIT) Unit unit,
            @JsonProperty(DATA_TYPE) DataType dataType,
            @JsonProperty(TOPIC) String topic,
            @JsonProperty(KEY) String key,
            @JsonProperty(VALUE) String value,
            @JsonProperty(AGGREGATOR) String aggregator) {
        super(name.name(), doc);

        Objects.requireNonNull(appProvider, APP_PROVIDER.concat(NULL_MESSAGE));

        this.type = name;
        this.appProvider = Utils.getProjectGroup().concat(appProvider);

        this.kafkaActor = new KafkaActor(doc, sampleRate, unit, dataType,
                new Topic(topic, key, value, aggregator, null));
    }

    public MonitorSourceType getType() {
        return type;
    }

    public String getAppProvider() {
        return appProvider;
    }

    public KafkaActor getKafkaActor() {
        return kafkaActor;
    }

    @Override
    public Set<String> getTopics() {
        return kafkaActor.getTopic().getTopicNames();
    }
}
