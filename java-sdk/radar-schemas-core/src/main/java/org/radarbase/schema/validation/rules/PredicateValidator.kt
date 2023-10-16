package org.radarbase.schema.validation.rules

import org.radarbase.schema.validation.ValidationContext

/** Validator that checks given [predicate] and raises with [message] if it fails. */
class PredicateValidator<T>(
    private val predicate: (T) -> Boolean,
    private val message: (T) -> String,
) : Validator<T> {
    override fun ValidationContext.runValidation(value: T) {
        if (!predicate(value)) {
            raise(message(value))
        }
    }
}

/** Create a validator that checks given predicate and raises with message if it does not match. */
fun <T> validator(predicate: (T) -> Boolean, message: String): Validator<T> = PredicateValidator(predicate) { message }

fun <T> validator(predicate: (T) -> Boolean, message: (T) -> String): Validator<T> = PredicateValidator(predicate, message)
