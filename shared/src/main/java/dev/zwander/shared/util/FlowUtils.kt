package dev.zwander.shared.util

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

fun <T> Flow<T>.firstBlocking() = runBlocking {
    try {
        withTimeout(500) {
            first()
        }
    } catch (e: TimeoutCancellationException) {
        null
    }
}
