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
import static org.radarcns.specifications.SourceCatalogue.BASE_PATH;
import static org.radarcns.specifications.SourceCatalogue.YAML_EXTENSION;
import static org.radarcns.specifications.validator.ValidationSupport.Package;
import static org.radarcns.specifications.validator.ValidationSupport.getMessage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;
import org.radarcns.catalogue.PassiveSourceType;
import org.radarcns.config.YamlConfigLoader;
import org.radarcns.specifications.SourceCatalogue.NameFolder;
import org.radarcns.specifications.source.passive.PassiveSource;
import org.radarcns.specifications.source.passive.Processor;
import org.radarcns.specifications.source.passive.Sensor;
import org.radarcns.specifications.validator.ValidationResult;
import org.radarcns.specifications.validator.Validator;

/**
 * TODO.
 */
public class PassiveValidation {

    private static Map<PassiveSourceType, Package> converter;

    @BeforeClass
    public static void initConverter() {
        converter = new HashMap<>();
        converter.put(PassiveSourceType.BIOVOTION_VSM1, Package.BIOVOTION);
        converter.put(PassiveSourceType.EMPATICA_E4, Package.EMPATICA);
        converter.put(PassiveSourceType.PEBBLE_2, Package.PEBBLE);
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
                NameFolder.PASSIVE.getName()).resolve(
                type.name().toLowerCase().concat(YAML_EXTENSION)).toUri());

            PassiveSource source = new YamlConfigLoader().load(file, PassiveSource.class);

            ValidationResult result = Validator.validatePassive(source, file);
            assertTrue(getMessage(file, result), result.isValid());

            for (Sensor sensor : source.getSensors()) {
                result = Validator.validateSensor(converter.get(type), sensor);
                assertTrue(getMessage(file, result), result.isValid());

            }

            for (Processor processor : source.getProcessors()) {
                result = Validator.validateProcessor(converter.get(type), processor);
                assertTrue(getMessage(file, result), result.isValid());

            }
        }*/
    }

}
