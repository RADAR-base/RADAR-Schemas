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

import org.junit.Before;
import org.junit.Test;
import org.radarcns.schema.Scope;
import org.radarcns.schema.validation.config.ExcludeConfig;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.junit.Assert.fail;

public class AvroValidatorTest {
    private AvroValidator validator;

    @Before
    public void setUp() {
        ExcludeConfig config = ExcludeConfig.load();
        validator = new AvroValidator(config);
    }

    @Test
    public void active() throws IOException {
        testScope(Scope.ACTIVE);
    }

    @Test
    public void monitor() throws IOException {
        testScope(Scope.MONITOR);
    }

    @Test
    public void passive() throws IOException {
        testScope(Scope.PASSIVE);
    }

    @Test
    public void kafka() throws IOException {
        testScope(Scope.KAFKA);
    }

    @Test
    public void catalogue() throws IOException {
        testScope(Scope.CATALOGUE);
    }

    private void testScope(Scope scope) throws IOException {
        Collection<ValidationException> results = validator.analyseFiles(scope);
        if (!results.isEmpty()) {
            String resultString = results.stream()
                .map(r -> "\nValidation FAILED:\n" + r.getMessage() + "\n")
                .collect(Collectors.joining());

            fail(resultString);
        }
    }
}
