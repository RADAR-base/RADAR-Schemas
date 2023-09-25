package org.radarbase.schema.validation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.radarbase.schema.validation.rules.Validator

interface ValidationContext {
    fun raise(message: String, ex: Exception? = null)

    fun <T> Validator<T>.launchValidation(value: T)
}

private class ValidationContextImpl(
    private val coroutineScope: CoroutineScope,
) : ValidationContext {
    private val channel = Channel<ValidationException>(Channel.UNLIMITED)
    private lateinit var producerCoroutineScope: CoroutineScope

    suspend fun runValidation(block: ValidationContext.() -> Unit): List<ValidationException> {
        coroutineScope.launch {
            coroutineScope {
                producerCoroutineScope = this
                block()
            }
            channel.close()
        }
        return channel.toList().distinct()
    }

    override fun raise(message: String, ex: Exception?) {
        channel.trySend(ValidationException(message, ex))
    }

    override fun <T> Validator<T>.launchValidation(value: T) {
        producerCoroutineScope.launch {
            runValidation(value)
        }
    }
}

suspend fun validationContext(block: ValidationContext.() -> Unit) =
    coroutineScope {
        val context = ValidationContextImpl(this)
        context.runValidation(block)
    }

suspend fun <T> Validator<T>.validate(value: T) = validationContext {
    launchValidation(value)
}
