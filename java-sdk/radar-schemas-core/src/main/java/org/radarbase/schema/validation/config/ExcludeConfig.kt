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
package org.radarbase.schema.validation.config

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.radarbase.schema.validation.rules.SchemaField
import org.radarbase.schema.validation.rules.Validator
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.util.*
import java.util.regex.Pattern

/**
 * TODO.
 */
class ExcludeConfig {
    @JsonIgnore
    private val matchers: MutableCollection<PathMatcher> = ArrayList()
    private val validation: MutableMap<String, ConfigItem> = HashMap()

    var root: Path? = null
        set(value) {
            field = value?.normalize()
        }

    /**
     * TODO.
     * @return TODO
     */
    fun isSkipped(field: SchemaField): Boolean {
        val schema = field.schema
        val item = validation[schema.fullName]
            ?: validation[schema.namespace + WILD_CARD_PACKAGE]
            ?: return false

        return item.fields.contains(field.field.name())
    }

    /**
     * TODO.
     * @param checkPath TODO
     * @return TODO
     */
    fun skipFile(checkPath: Path?): Boolean {
        checkPath ?: return false
        val relativePath = relativize(checkPath)
        return matchers.any { it.matches(relativePath) || it.matches(checkPath.fileName) }
    }

    private fun relativize(path: Path): Path {
        val localRoot = root
        if (path.isAbsolute && localRoot != null) {
            try {
                return localRoot.relativize(path.normalize())
            } catch (ex: IllegalArgumentException) {
                // relativePath cannot be relativized
            }
        }
        return path
    }

    fun setFiles(vararg files: String) {
        setFiles(listOf(*files))
    }

    /** Set the files to be excluded.  */
    @JsonSetter("files")  // File system should not be closed
    fun setFiles(files: Collection<String>) {
        val fs = FileSystems.getDefault()
        val pathMatchers = files
            .mapNotNull { path ->
                try {
                    fs.getPathMatcher("glob:$path")
                } catch (ex: IllegalArgumentException) {
                    logger.error(
                        """
                            |Exclude pattern {} is invalid.
                            | Please use the glob syntax described in
                            | https://docs.oracle.com/javase/7/docs/api/
                            |java/nio/file/FileSystem.html#getPathMatcher(java.lang.String)
                            |"""
                            .trimMargin()
                            .replace("\n", ""),
                        path,
                        ex,
                    )
                    null
                }
            }
        require(files.size == pathMatchers.size) { "Invalid exclude config." }
        if (!files.isEmpty()) {
            matchers.clear()
        }
        matchers.addAll(pathMatchers)
    }

    /** Set the validation to be excluded.  */
    @JsonSetter("validation")
    fun setValidation(validation: Map<String, ConfigItem>) {
        //Validate validation key map
        require(validClass(validation.keys.asSequence())) { "Validation map keys are invalid" }
        require(validClass(validation.values.asSequence()
            .flatMap { it.fields.asSequence() })
        ) { "Validation map values are not valid." }
        if (this.validation.isNotEmpty()) {
            this.validation.clear()
        }
        this.validation.putAll(validation)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ExcludeConfig::class.java)

        /** Repository name.  */
        const val REPOSITORY_NAME = "/RADAR-Schemas/"

        /** File path location.  */
        private const val FILE_NAME = "schema.yml"

        /** Wild card to suppress check for entire package.  */
        private const val WILD_CARD_PACKAGE = ".*"

        /** Regex for validating the yml file.  */
        @JvmField
        val VALID_INPUT_PATTERN: Pattern = Pattern.compile("[a-z][a-zA-Z0-9.*]*")

        /** Load the ExcludeConfig from file.  */
        @JvmStatic
        @Throws(IOException::class)
        fun load(path: Path?): ExcludeConfig {
            val factory = YAMLFactory()
            val reader = ObjectMapper(factory)
                .readerFor(ExcludeConfig::class.java)
            if (path == null) {
                val loader = Thread.currentThread().contextClassLoader
                loader.getResourceAsStream(FILE_NAME).use { `in` ->
                    return if (`in` == null) {
                        logger.debug("Not loading any configuration")
                        ExcludeConfig()
                    } else {
                        reader.readValue(`in`)
                    }
                }
            } else {
                return reader.readValue(path.toFile())
            }
        }

        /**
         * TODO.
         * @param stream TODO
         * @return TODO
         */
        private fun validClass(stream: Sequence<String>): Boolean =
            stream.all(Validator.matches(VALID_INPUT_PATTERN)::test)
    }
}
