package org.radarbase.schema.validation.rules

import org.radarbase.schema.validation.ValidationContext

/** Validator that checks all validator in its list. */
class AllValidator<T>(
    private val subValidators: List<Validator<T>>,
) : Validator<T> {
    override fun ValidationContext.runValidation(value: T) {
        subValidators.forEach {
            it.launchValidation(value)
        }
    }
}

/** Create a new validator that combines the validation of underlying validators. */
fun <T> all(vararg validators: Validator<T>) = AllValidator(validators.toList())
