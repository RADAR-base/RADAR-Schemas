package org.radarcns.validator;

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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;

public class Validator {

    public static final String COMMONS_FOLDER_NAME = "commons";
    public static final String RESTAPI_FOLDER_NAME = "restapi";
    public static final String SPECIFICATION_FOLDER_NAME = "specification";

    public static final Path COMMONS_PATH = Paths.get(
            new File(".").toURI()).getParent().getParent().getParent().resolve(
                  COMMONS_FOLDER_NAME);

    public static final Path REST_API_PATH = Paths.get(
            new File(".").toURI()).getParent().getParent().getParent().resolve(
                  RESTAPI_FOLDER_NAME);

    public static final Path SPECIFICATION_PATH = Paths.get(
            new File(".").toURI()).getParent().getParent().getParent().resolve(
                  SPECIFICATION_FOLDER_NAME);

    //private static final Logger LOGGER = LoggerFactory.getLogger(Validator.class);

    @Test
    public void commons() {
        File commonsFolder = new File(COMMONS_PATH.toUri());
        assertEquals(true, commonsFolder.exists());
        assertEquals(true, commonsFolder.isDirectory());
    }

    @Test
    public void restapi() {
        File commonsFolder = new File(REST_API_PATH.toUri());
        assertEquals(true, commonsFolder.exists());
        assertEquals(true, commonsFolder.isDirectory());
    }

    @Test
    public void specification() {
        File commonsFolder = new File(SPECIFICATION_PATH.toUri());
        assertEquals(true, commonsFolder.exists());
        assertEquals(true, commonsFolder.isDirectory());
    }
}
