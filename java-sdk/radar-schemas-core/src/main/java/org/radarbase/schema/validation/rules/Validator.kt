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

import org.radarbase.schema.validation.ValidationException
import java.util.stream.Stream

class Validator<T>(
    private val validation: (T) -> Stream<ValidationException>,
) {
    fun and(other: Validator<T>): Validator<T> = Validator { obj ->
        Stream.concat(
            this.validate(obj),
            other.validate(obj),
        )
    }

    fun validate(value: T): Stream<ValidationException> = this.validation.invoke(value)

    companion object {
        fun check(test: Boolean, message: String): Stream<ValidationException> =
            if (test) valid() else raise(message)

        inline fun check(test: Boolean, message: () -> String): Stream<ValidationException> {
            return if (test) valid() else raise(message())
        }

        fun <T> validate(predicate: (T) -> Boolean, message: String): Validator<T> =
            Validator { obj ->
                check(predicate(obj), message)
            }

        fun <T> validate(predicate: (T) -> Boolean, message: (T) -> String): Validator<T> =
            Validator { obj: T ->
                check(predicate(obj), message(obj))
            }

        fun raise(message: String, ex: Exception? = null): Stream<ValidationException> =
            Stream.of(ValidationException(message, ex))

        fun valid(): Stream<ValidationException> = Stream.empty()
    }
}
