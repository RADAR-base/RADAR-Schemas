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

package org.radarcns.schema.specification;

import org.radarcns.schema.util.Utils;

import java.util.Objects;
import java.util.Set;

/**
 * TODO.
 */
public abstract class Source {
    private final String name;
    private final String doc;

    /**
     * TODO.
     * @param name TODO
     * @param doc TODO
     */
    public Source(String name, String doc) {
        Objects.requireNonNull(name);
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
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Source source = (Source) o;
        return Objects.equals(name, source.name)
                && Objects.equals(doc, source.doc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, doc);
    }

    public static String expandClass(String classShorthand) {
        if (classShorthand == null || classShorthand.isEmpty()) {
            return null;
        } else if (classShorthand.charAt(0) == '.') {
            return Utils.getProjectGroup() + classShorthand;
        } else {
            return classShorthand;
        }
    }
}