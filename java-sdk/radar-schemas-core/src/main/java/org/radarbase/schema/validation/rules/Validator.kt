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
package org.radarbase.schema.validation.rules

import org.radarbase.schema.validation.ValidationContext
import java.nio.file.Path
import kotlin.io.path.extension

open class Validator<T>(
    private val validation: ValidationContext.(T) -> Unit,
) {
    open fun ValidationContext.runValidation(value: T) {
        this.validation(value)
    }
}

fun <T> validator(predicate: (T) -> Boolean, message: String): Validator<T> =
    Validator { obj ->
        if (!predicate(obj)) raise(message)
    }

fun <T> validator(predicate: (T) -> Boolean, message: (T) -> String): Validator<T> =
    Validator { obj ->
        if (!predicate(obj)) raise(message(obj))
    }

fun <T> all(vararg validators: Validator<T>) = Validator<T> { obj ->
    validators.forEach {
        it.launchValidation(obj)
    }
}

fun pathExtensionValidator(extension: String) = Validator<Path> { path ->
    if (!path.extension.equals(extension, ignoreCase = true)) {
        raise("Path $path does not have extension $extension")
    }
}
