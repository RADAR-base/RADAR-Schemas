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

import org.radarbase.schema.Scope
import org.radarbase.schema.util.SchemaUtils.projectGroup
import org.radarbase.schema.util.SchemaUtils.snakeToCamelCase
import java.nio.file.Path
import java.util.Objects
import java.util.function.Predicate
import java.util.regex.Pattern

/**
 * TODO.
 */
object ValidationHelper {
    const val COMMONS_PATH = "commons"
    const val SPECIFICATIONS_PATH = "specifications"

    // snake case
    private val TOPIC_PATTERN = Pattern.compile(
        "[A-Za-z][a-z0-9-]*(_[A-Za-z0-9-]+)*",
    )

    /**
     * TODO.
     * @param scope TODO
     * @return TODO
     */
    fun getNamespace(schemaRoot: Path?, schemaPath: Path?, scope: Scope): String {
        // add subfolder of root to namespace
        val rootPath = scope.getPath(schemaRoot)
            ?: throw IllegalArgumentException("Scope $scope does not have a commons path")
        val relativePath = rootPath.relativize(schemaPath)
        val builder = StringBuilder(50)
        builder.append(projectGroup).append('.').append(scope.lower)
        for (i in 0 until relativePath.nameCount - 1) {
            builder.append('.').append(relativePath.getName(i))
        }
        return builder.toString()
    }

    /**
     * TODO.
     * @param path TODO
     * @return TODO
     */
    @JvmStatic
    fun getRecordName(path: Path): String {
        Objects.requireNonNull(path)
        return snakeToCamelCase(path.fileName.toString())
    }

    /**
     * TODO.
     * @param topicName TODO
     * @return TODO
     */
    @JvmStatic
    fun isValidTopic(topicName: String?): Boolean {
        return topicName != null && TOPIC_PATTERN.matcher(topicName).matches()
    }

    /**
     * TODO.
     * @param file TODO.
     * @return TODO.
     */
    @JvmStatic
    fun matchesExtension(file: Path, extension: String): Boolean {
        return file.toString().lowercase()
            .endsWith("." + extension.lowercase())
    }

    /**
     * TODO.
     * @param file TODO
     * @param extension TODO
     * @return TODO
     */
    fun equalsFileName(file: Path, extension: String): Predicate<String> {
        return Predicate { str: String ->
            var fileName = file.fileName.toString()
            if (fileName.endsWith(extension)) {
                fileName = fileName.substring(0, fileName.length - extension.length)
            }
            str.equals(fileName, ignoreCase = true)
        }
    }
}
