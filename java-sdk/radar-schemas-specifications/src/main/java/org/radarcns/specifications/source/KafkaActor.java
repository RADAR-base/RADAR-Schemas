package org.radarcns.specifications.source;

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

import static org.radarcns.specifications.util.Labels.DATA_TYPE;
import static org.radarcns.specifications.util.Labels.SAMPLE_RATE;
import static org.radarcns.specifications.util.Labels.TOPIC;
import static org.radarcns.specifications.util.Labels.UNIT;

import java.util.Objects;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.radarcns.catalogue.DataType;
import org.radarcns.catalogue.Unit;
import org.radarcns.specifications.source.passive.Sensor;

public class KafkaActor {

    private final String doc;

    private final double sampleRate;

    private final Unit unit;

    private final DataType dataType;

    private final Topic topic;

    private static final String NULL_MESSAGE = " in ".concat(
        Sensor.class.getName()).concat(" cannot be null.");

    public KafkaActor(String doc, double sampleRate, Unit unit, DataType dataType, Topic topic) {

        Objects.requireNonNull(dataType, DATA_TYPE.concat(NULL_MESSAGE));
        Objects.requireNonNull(sampleRate, SAMPLE_RATE.concat(NULL_MESSAGE));
        Objects.requireNonNull(topic, TOPIC.concat(NULL_MESSAGE));
        Objects.requireNonNull(unit, UNIT.concat(NULL_MESSAGE));

        this.doc = doc;
        this.sampleRate = sampleRate;
        this.unit = unit;
        this.dataType = dataType;
        this.topic = topic;
    }

    public String getDoc() {
        return doc;
    }

    public double getSampleRate() {
        return sampleRate;
    }

    public Unit getUnit() {
        return unit;
    }

    public DataType getDataType() {
        return dataType;
    }

    public Topic getTopic() {
        return topic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof KafkaActor)) {
            return false;
        }

        KafkaActor that = (KafkaActor) o;

        return new EqualsBuilder()
            .append(sampleRate, that.sampleRate)
            .append(doc, that.doc)
            .append(unit, that.unit)
            .append(dataType, that.dataType)
            .append(topic, that.topic)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(doc)
            .append(sampleRate)
            .append(unit)
            .append(dataType)
            .append(topic)
            .toHashCode();
    }
}
