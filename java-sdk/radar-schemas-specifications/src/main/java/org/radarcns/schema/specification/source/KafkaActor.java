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

package org.radarcns.schema.specification.source;

import org.radarcns.catalogue.ProcessingState;

import java.util.Objects;

public class KafkaActor {

    private final String doc;

    private final double sampleRate;

    private final String unit;

    private final ProcessingState dataType;

    private final Topic topic;

    public KafkaActor(String doc, double sampleRate, String unit, ProcessingState dataType, Topic topic) {
        Objects.requireNonNull(dataType);
        Objects.requireNonNull(topic);
        Objects.requireNonNull(unit);

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

    public String getUnit() {
        return unit;
    }

    public ProcessingState getProcessingState() {
        return dataType;
    }

    public Topic getTopic() {
        return topic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KafkaActor that = (KafkaActor) o;
        return Double.compare(that.sampleRate, sampleRate) == 0 &&
                Objects.equals(doc, that.doc) &&
                Objects.equals(unit, that.unit) &&
                dataType == that.dataType &&
                Objects.equals(topic, that.topic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(doc, sampleRate, unit, dataType, topic);
    }
}
