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
package org.radarbase.schema.validation

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.radarbase.schema.Scope
import org.radarbase.schema.specification.config.SchemaConfig
import org.radarbase.schema.validation.ValidationHelper.SPECIFICATIONS_PATH
import org.radarbase.schema.validation.ValidationHelper.matchesExtension
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.PathMatcher
import kotlin.io.path.walk

/**
 * Validates RADAR-Schemas specifications.
 */
class SpecificationsValidator(root: Path, config: SchemaConfig) {
    private val specificationsRoot: Path
    private val mapper: ObjectMapper
    private val pathMatcher: PathMatcher

    /**
     * Specifications validator for given RADAR-Schemas directory.
     * @param root RADAR-Schemas directory.
     * @param config configuration to exclude certain schemas or fields from validation.
     */
    init {
        specificationsRoot = root.resolve(SPECIFICATIONS_PATH)
        pathMatcher = config.pathMatcher(specificationsRoot)
        mapper = ObjectMapper(YAMLFactory())
    }

    /** Check that all files in the specifications directory are YAML files.  */
    @Throws(IOException::class)
    fun specificationsAreYmlFiles(scope: Scope): Boolean {
        val baseFolder = scope.getPath(specificationsRoot)
        if (baseFolder == null) {
            logger.info(
                "{} sources folder not present at {}",
                scope,
                specificationsRoot.resolve(scope.lower),
            )
            return false
        }
        Files.walk(baseFolder).use { walker ->
            return walker
                .filter { path: Path? -> pathMatcher.matches(path) }
                .allMatch { path: Path -> isYmlFile(path) }
        }
    }

    @Throws(IOException::class)
    fun <T> checkSpecificationParsing(scope: Scope, clazz: Class<T>?): Boolean {
        val baseFolder = scope.getPath(specificationsRoot)
        if (baseFolder == null) {
            logger.info(
                "{} sources folder not present at {}",
                scope,
                specificationsRoot.resolve(scope.lower),
            )
            return false
        }
        Files.walk(baseFolder).use { walker ->
            return walker
                .filter { path: Path? -> pathMatcher.matches(path) }
                .allMatch { f: Path ->
                    try {
                        mapper.readerFor(clazz).readValue<T>(f.toFile())
                        return@allMatch true
                    } catch (ex: IOException) {
                        logger.error(
                            "Failed to load configuration {}: {}",
                            f,
                            ex.toString(),
                        )
                        return@allMatch false
                    }
                }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(
            SpecificationsValidator::class.java,
        )
        const val YML_EXTENSION = "yml"
        private fun isYmlFile(path: Path): Boolean {
            return matchesExtension(path, YML_EXTENSION)
        }
    }
}
