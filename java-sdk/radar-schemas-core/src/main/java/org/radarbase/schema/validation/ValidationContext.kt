package org.radarbase.schema.validation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.radarbase.schema.validation.rules.Validator
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Context that validators run in. As part of the context, they can raise errors and launch
 * validations in additional coroutines.
 */
class ValidationContext(
    /** Channel that will receive validation exceptions. */
    private val channel: SendChannel<ValidationException>,
    /** Scope that the validation will run in. */
    private val coroutineScope: CoroutineScope,
) {
    /** Raise a validation exception. */
    fun raise(message: String, ex: Exception? = null) {
        channel.trySend(ValidationException(message, ex))
    }

    /** Launch a validation by a validator in a new coroutine. */
    fun <T> Validator<T>.launchValidation(value: T) {
        coroutineScope.launch {
            runValidation(value)
        }
    }

    /** Launch a validation by a validator in the same coroutine. */
    fun <T> Validator<T>.validate(value: T) {
        runValidation(value)
    }

    /** Validate all given values. */
    fun <T> Validator<T>.validateAll(values: Iterable<T>) {
        values.forEach { launchValidation(it) }
    }

    /**
     * Launch an inline validation in a new coroutine. By passing [context], the validation is run
     * in a different sub-context.
     */
    fun launch(context: CoroutineContext = EmptyCoroutineContext, block: suspend CoroutineScope.() -> Unit) {
        coroutineScope.launch(context, block = block)
    }
}

/**
 * Create a ValidationContext to launch validations in.
 *
 * @return validation exceptions that were raised as within the validation context.
 */
suspend fun validationContext(block: ValidationContext.() -> Unit): List<ValidationException> {
    val channel = Channel<ValidationException>(UNLIMITED)
    coroutineScope {
        val producerJob = launch {
            with(ValidationContext(channel, this@launch)) {
                block()
            }
        }
        producerJob.join()
        channel.close()
    }

    return buildSet {
        channel.consumeEach { add(it) }
    }.toList()
}

/**
 * Run a validation inside its own context. This can be used for one-off validations. Otherwise, a
 * separate validationContext should be created.
 */
suspend fun <T> Validator<T>.validate(value: T) = validationContext {
    launchValidation(value = value)
}

/**
 * Run a validation inside its own context. This can be used for one-off validations. Otherwise, a
 * separate validationContext should be created.
 */
suspend fun <T> Validator<T>.validateAll(values: Iterable<T>) = validationContext {
    validateAll(values = values)
}
