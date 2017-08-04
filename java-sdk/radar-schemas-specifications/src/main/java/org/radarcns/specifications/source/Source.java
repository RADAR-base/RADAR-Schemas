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

import java.util.Set;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * TODO.
 */
public abstract class Source {

    private final String name;

    private final String doc;

    public Source(String name, String doc) {
        this.name = name;
        this.doc = doc;
    }

    public String getName() {
        return name;
    }

    public String getDoc() {
        return doc;
    }

    /**
     * TODO.
     * @return TODO.
     */
    public abstract Set<String> getTopics();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Source)) {
            return false;
        }

        Source source = (Source) o;

        return new EqualsBuilder()
            .append(name, source.name)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(name)
            .toHashCode();
    }
}
