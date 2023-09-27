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

object ValidationHelper {
    const val COMMONS_PATH = "commons"
    const val SPECIFICATIONS_PATH = "specifications"

    // snake case
    private val TOPIC_PATTERN = "[A-Za-z][a-z0-9-]*(_[A-Za-z0-9-]+)*".toRegex()

    fun getNamespace(schemaRoot: Path?, schemaPath: Path?, scope: Scope): String {
        // add subfolder of root to namespace
        val rootPath = requireNotNull(scope.getPath(schemaRoot)) { "Scope $scope does not have a commons path" }
        requireNotNull(schemaPath) { "Missing schema path" }
        val relativePath = rootPath.relativize(schemaPath)
        return buildString(50) {
            append(projectGroup)
            append('.')
            append(scope.lower)
            for (i in 0 until relativePath.nameCount - 1) {
                append('.')
                append(relativePath.getName(i))
            }
        }
    }

    fun Path.toRecordName(): String = snakeToCamelCase(fileName.toString())

    fun isValidTopic(topicName: String?): Boolean = topicName?.matches(TOPIC_PATTERN) == true
}
