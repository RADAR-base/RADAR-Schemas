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
package org.radarbase.schema.util

import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.Properties
import java.util.function.Function
import java.util.stream.Stream

/**
 * TODO.
 */
object SchemaUtils {
    private val logger = LoggerFactory.getLogger(SchemaUtils::class.java)
    private const val GRADLE_PROPERTIES = "exchange.properties"
    private const val GROUP_PROPERTY = "project.group"

    @JvmStatic
    @get:Synchronized
    var projectGroup: String? = null
        /**
         * TODO.
         * @return TODO
         */
        get() {
            if (field == null) {
                val prop = Properties()
                val loader = ClassLoader.getSystemClassLoader()
                try {
                    loader.getResourceAsStream(GRADLE_PROPERTIES).use { `in` ->
                        if (`in` == null) {
                            field = "org.radarcns"
                            logger.debug("Project group not specified. Using \"{}\".", field)
                        } else {
                            prop.load(`in`)
                            field = prop.getProperty(GROUP_PROPERTY)
                            if (field == null) {
                                field = "org.radarcns"
                                logger.debug("Project group not specified. Using \"{}\".", field)
                            }
                        }
                    }
                } catch (exc: IOException) {
                    throw IllegalStateException(
                        GROUP_PROPERTY +
                            " cannot be extracted from " + GRADLE_PROPERTIES,
                        exc,
                    )
                }
            }
            return field
        }
        private set

    /**
     * Expand a class name with the group name if it starts with a dot.
     * @param classShorthand class name, possibly starting with a dot as a shorthand.
     * @return class name or `null` if null or empty.
     */
    fun expandClass(classShorthand: String?): String? {
        return when {
            classShorthand.isNullOrEmpty() -> null
            classShorthand[0] == '.' -> projectGroup + classShorthand
            else -> classShorthand
        }
    }

    /**
     * Converts given file name from snake_case to CamelCase. This will cause underscores to be
     * removed, and the next character to be uppercase. This only converts the value up to the
     * first dot encountered.
     * @param value file name in snake_case
     * @return main part of file name in CamelCase.
     */
    @JvmStatic
    fun snakeToCamelCase(value: String): String {
        val fileName = value.toCharArray()
        val builder = StringBuilder(fileName.size)
        var nextIsUpperCase = true
        for (c in fileName) {
            when (c) {
                '_' -> nextIsUpperCase = true
                '.' -> return builder.toString()
                else -> if (nextIsUpperCase) {
                    builder.append(c.toString().uppercase())
                    nextIsUpperCase = false
                } else {
                    builder.append(c)
                }
            }
        }
        return builder.toString()
    }

    /** Apply a throwing function, and if it throws, log it and let it return an empty Stream.  */
    fun <T, R> applyOrEmpty(func: ThrowingFunction<T, Stream<R>?>): Function<T, Stream<R>?> {
        return Function { t: T ->
            try {
                return@Function func.apply(t)
            } catch (ex: Exception) {
                logger.error("Failed to apply function, returning empty.", ex)
                return@Function Stream.empty<R>()
            }
        }
    }

    /** Apply a throwing function, and if it throws, log it and let it return an empty Stream.  */
    fun <T, R> applyOrIllegalException(
        func: ThrowingFunction<T, Stream<R>?>,
    ): Function<T, Stream<R>?> {
        return Function { t: T ->
            try {
                return@Function func.apply(t)
            } catch (ex: Exception) {
                throw IllegalStateException(ex.message, ex)
            }
        }
    }

    /**
     * Function that may throw an exception.
     * @param <T> type of value taken.
     * @param <R> type of value returned.
     </R></T> */
    fun interface ThrowingFunction<T, R> {
        /**
         * Apply containing function.
         * @param value value to apply function to.
         * @return result of the function.
         * @throws Exception if the function fails.
         */
        @Throws(Exception::class)
        fun apply(value: T): R
    }
}
