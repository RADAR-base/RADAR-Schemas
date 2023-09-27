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

interface ValidationContext {
    fun raise(message: String, ex: Exception? = null)

    fun <T> Validator<T>.launchValidation(value: T)

    fun launchValidation(context: CoroutineContext = EmptyCoroutineContext, block: suspend CoroutineScope.() -> Unit)
}

private class ValidationContextImpl(
    private val channel: SendChannel<ValidationException>,
    private val coroutineScope: CoroutineScope,
) : ValidationContext {

    override fun raise(message: String, ex: Exception?) {
        channel.trySend(ValidationException(message, ex))
    }

    override fun <T> Validator<T>.launchValidation(value: T) {
        coroutineScope.launch {
            runValidation(value)
        }
    }

    override fun launchValidation(context: CoroutineContext, block: suspend CoroutineScope.() -> Unit) {
        coroutineScope.launch(context, block = block)
    }
}

suspend fun validationContext(block: ValidationContext.() -> Unit): List<ValidationException> {
    val channel = Channel<ValidationException>(UNLIMITED)
    coroutineScope {
        val producerJob = launch {
            with(ValidationContextImpl(channel, this@launch)) {
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

suspend fun <T> Validator<T>.validate(value: T) = validationContext {
    launchValidation(value = value)
}
