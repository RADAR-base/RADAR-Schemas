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

package org.radarcns.schema.validation;

import org.junit.BeforeClass;
import org.junit.Test;
import org.radarcns.schema.specification.passive.PassiveSource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO.
 */
public class PassiveValidation {

    private static Map<PassiveSource.RadarSourceTypes, ValidationSupport.Package> converter;

    @BeforeClass
    public static void initConverter() {
        converter = new HashMap<>();
        converter.put(PassiveSource.RadarSourceTypes.BIOVOTION_VSM1,
                ValidationSupport.Package.BIOVOTION);
        converter.put(PassiveSource.RadarSourceTypes.EMPATICA_E4,
                ValidationSupport.Package.EMPATICA);
        converter.put(PassiveSource.RadarSourceTypes.PEBBLE_2, ValidationSupport.Package.PEBBLE);
    }

    @Test
    public void validate() throws IOException {
        /*for (PassiveSourceType type : PassiveSourceType.values()) {

            if (type.name().equals(PassiveSourceType.UNKNOWN.name())) {
                continue;
            }

            if (type.name().equals(PassiveSourceType.ANDROID_PHONE.name())) {
                continue;
            }

            File file = new File(BASE_PATH.resolve(
                NameFolder.PASSIVE.getLiteral()).resolve(
                type.name().toLowerCase() + YAML_EXTENSION)).toUri());

            PassiveSource source = new YamlConfigLoader().load(file, PassiveSource.class);

            Stream<ValidationException> result =Validator.validatePassive(source, file);
            assertTrue(getMessage(file, result), result.isEmpty());

            for (PassiveData sensor : source.getSensors()) {
                result = Validator.validateSensor(converter.get(type), sensor);
                assertTrue(getMessage(file, result), result.isEmpty());

            }

            for (Processor processor : source.getProcessors()) {
                result = Validator.validateProcessor(converter.get(type), processor);
                assertTrue(getMessage(file, result), result.isEmpty());

            }
        }*/
    }

}
