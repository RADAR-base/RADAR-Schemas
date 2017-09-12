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

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Checks the schema catalog.
 * Checks that the tree folder structure respects the following structure:
 * <ul>
 *   <li>commons
 *    <ul>
 *      <li>active</li>
 *      <li>kafka</li>
 *      <li>monitor</li>
 *      <li>passive</li>
 *    </ul>
 *    </li>
 *   <li>rest</li>
 *   <li>specifications
 *      <ul>
 *        <li>active</li>
 *        <li>monitor</li>
 *        <li>passive</li>
 *      </ul>
 *    </li>
 * </ul>
 * At moment, the {@code restApi} does not have a well defined structure.
 */
public final class SchemaRepository {

    public static final Path PROJECT_ROOT = Paths.get("../..").toAbsolutePath();
    public static final Path COMMONS_PATH = PROJECT_ROOT.resolve("commons");
    public static final Path REST_API_PATH = PROJECT_ROOT.resolve("restapi");
    public static final Path SPECIFICATIONS_PATH = PROJECT_ROOT.resolve("specifications");

    private SchemaRepository() {
        // utility class
    }
}
