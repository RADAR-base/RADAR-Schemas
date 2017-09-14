package org.radarcns.schema.specification;

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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.Set;
import org.radarcns.catalogue.ProcessingState;

/**
 * TODO.
 */
public class MonitorSource extends Source {
    public enum RadarSourceTypes {
        EXTERNAL_TIME, RECORD_COUNTS, SERVER_STATUS, UPTIME
    }

    private final String type;
    private final String appProvider;
    private final KafkaActor kafkaActor;

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
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public MonitorSource(
            @JsonProperty(Labels.NAME) String name,
            @JsonProperty(Labels.APP_PROVIDER) String appProvider,
            @JsonProperty(Labels.DOC) String doc,
            @JsonProperty(Labels.SAMPLE_RATE) double sampleRate,
            @JsonProperty(Labels.UNIT) String unit,
            @JsonProperty(Labels.PROCESSING_STATE) ProcessingState dataType,
            @JsonProperty(Labels.TOPIC) String topic,
            @JsonProperty(Labels.KEY) String key,
            @JsonProperty(Labels.VALUE) String value,
            @JsonProperty(Labels.AGGREGATOR) String aggregator) {
        super(name, doc);

        Objects.requireNonNull(appProvider);

        this.type = name;
        this.appProvider = deduceProjectClass(appProvider);

        this.kafkaActor = new KafkaActor(doc, sampleRate, unit, dataType,
                new Topic(topic, key, value, aggregator, null));
    }

    public String getType() {
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
