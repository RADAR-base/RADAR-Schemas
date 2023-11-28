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
import org.radarbase.schema.util.SchemaUtils.listRecursive
import org.radarbase.schema.validation.rules.Validator
import org.radarbase.schema.validation.rules.hasExtension
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Path
import java.nio.file.PathMatcher

/**
 * Validates RADAR-Schemas specifications.
 *
 * @param root RADAR-Schemas specifications directory.
 * @param config configuration to exclude certain schemas or fields from validation.
 *
 */
class SpecificationsValidator(
    private val root: Path,
    private val config: SchemaConfig,
) {
    private val mapper: ObjectMapper = ObjectMapper(YAMLFactory())
    private val pathMatcher: PathMatcher = config.pathMatcher(root)

    fun ofScope(scope: Scope): SpecificationsValidator? {
        val baseFolder = scope.getPath(root)
        return if (baseFolder == null) {
            logger.info(
                "{} sources folder not present at {}",
                scope,
                root.resolve(scope.lower),
            )
            null
        } else {
            SpecificationsValidator(baseFolder, config)
        }
    }

    suspend fun <T> isValidSpecification(clazz: Class<T>?): List<ValidationException> {
        val paths = root.listRecursive { pathMatcher.matches(it) }
        return validationContext {
            isYmlFile.validateAll(paths)
            isYmlFileParseable(clazz).validateAll(paths)
        }
    }

    private fun <T> isYmlFileParseable(clazz: Class<T>?) = Validator<Path> { path ->
        try {
            mapper.readerFor(clazz).readValue<T>(path.toFile())
        } catch (ex: IOException) {
            raise("Failed to load configuration $path", ex)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SpecificationsValidator::class.java)

        private val isYmlFile: Validator<Path> = hasExtension("yml")
    }
}
