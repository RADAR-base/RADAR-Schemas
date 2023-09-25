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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.radarbase.schema.Scope
import org.radarbase.schema.specification.config.SchemaConfig
import org.radarbase.schema.validation.ValidationHelper.SPECIFICATIONS_PATH
import org.radarbase.schema.validation.rules.Validator
import org.radarbase.schema.validation.rules.pathExtensionValidator
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.util.stream.Collectors

/**
 * Validates RADAR-Schemas specifications.
 *
 * @param root RADAR-Schemas directory.
 * @param config configuration to exclude certain schemas or fields from validation.
 *
 */
class SpecificationsValidator(root: Path, config: SchemaConfig) {
    private val specificationsRoot: Path = root.resolve(SPECIFICATIONS_PATH)
    private val mapper: ObjectMapper = ObjectMapper(YAMLFactory())
    private val pathMatcher: PathMatcher = config.pathMatcher(specificationsRoot)

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
        return runBlocking {
            val paths = baseFolder.fetchChildren()
            val exceptions = validationContext {
                paths.forEach { isYmlFile.launchValidation(it) }
            }
            if (exceptions.isEmpty()) {
                true
            } else {
                logger.error("Not all specification files have the right extension: {}", exceptions.joinToString())
                false
            }
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
        val validator = isValidYmlFile(clazz)

        return runBlocking {
            val paths = baseFolder.fetchChildren()
            val exceptions = validationContext {
                paths.forEach { validator.launchValidation(it) }
            }
            if (exceptions.isEmpty()) {
                true
            } else {
                logger.error("Not all specification files have the right format: {}", exceptions.joinToString())
                false
            }
        }
    }

    private suspend fun Path.fetchChildren(): List<Path> = withContext(Dispatchers.IO) {
        Files.walk(this@fetchChildren).use { walker ->
            walker
                .filter { pathMatcher.matches(it) }
                .collect(Collectors.toList())
        }
    }

    private fun <T> isValidYmlFile(clazz: Class<T>?) = Validator<Path> { path ->
        try {
            mapper.readerFor(clazz).readValue<T>(path.toFile())
        } catch (ex: IOException) {
            raise("Failed to load configuration $path", ex)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SpecificationsValidator::class.java)

        private val isYmlFile: Validator<Path> = pathExtensionValidator("yml")
    }
}
