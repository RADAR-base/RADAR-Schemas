package org.radarbase.schema.validation.rules

import org.radarbase.schema.validation.ValidationContext

/** Validator that checks given predicate. */
class DirectValidator<T>(
    private val validation: ValidationContext.(T) -> Unit,
) : Validator<T> {
    override fun ValidationContext.runValidation(value: T) {
        validation(value)
    }
}

/** Implementation of validator that passes given function as in a new Validator object. */
fun <T> Validator(validation: ValidationContext.(T) -> Unit): Validator<T> = DirectValidator(validation)
