package com.sabermetrics.worldseries.sync

import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Resilient Exponential Back-Off Policy with Randomized Jitter.
 * Designed for fault-tolerant read/write operations against PocketHost / PocketBase cloud endpoints.
 *
 * Backoff Formula:
 * Delay(attempt) = min(maxDelayMs, initialDelayMs * factor^(attempt - 1)) * (1 + jitter)
 * where jitter in [-jitterRatio, +jitterRatio].
 */
data class ExponentialBackoffPolicy(
    val initialDelayMs: Long = 500L,
    val maxDelayMs: Long = 8000L,
    val factor: Double = 2.0,
    val maxAttempts: Int = 4,
    val jitterRatio: Double = 0.15
) {
    init {
        require(initialDelayMs > 0) { "initialDelayMs must be positive" }
        require(maxDelayMs >= initialDelayMs) { "maxDelayMs must be >= initialDelayMs" }
        require(factor >= 1.0) { "factor must be >= 1.0" }
        require(maxAttempts >= 1) { "maxAttempts must be >= 1" }
        require(jitterRatio in 0.0..0.5) { "jitterRatio must be in [0.0, 0.5]" }
    }

    /**
     * Calculates deterministic base delay without jitter for a given 1-indexed attempt.
     */
    fun calculateBaseDelayMs(attempt: Int): Long {
        if (attempt <= 1) return initialDelayMs
        var delay = initialDelayMs.toDouble()
        for (i in 2..attempt) {
            delay *= factor
            if (delay >= maxDelayMs) {
                return maxDelayMs
            }
        }
        return delay.toLong().coerceAtMost(maxDelayMs)
    }

    /**
     * Calculates final delay with randomized jitter in range [1 - jitterRatio, 1 + jitterRatio].
     */
    fun calculateDelayWithJitterMs(attempt: Int, random: Random = Random.Default): Long {
        val baseDelay = calculateBaseDelayMs(attempt)
        val jitterMultiplier = 1.0 + (random.nextDouble(-jitterRatio, jitterRatio))
        return (baseDelay * jitterMultiplier).toLong().coerceIn(0L, maxDelayMs + (maxDelayMs * jitterRatio).toLong())
    }

    /**
     * Executes a suspending block with automated exponential backoff retry.
     */
    suspend fun <T> executeSuspend(
        onRetry: ((attempt: Int, exception: Throwable, delayMs: Long) -> Unit)? = null,
        block: suspend (attempt: Int) -> T
    ): Result<T> {
        var lastException: Throwable? = null
        for (attempt in 1..maxAttempts) {
            try {
                val result = block(attempt)
                return Result.success(result)
            } catch (e: Throwable) {
                lastException = e
                if (attempt < maxAttempts) {
                    val delayMs = calculateDelayWithJitterMs(attempt)
                    onRetry?.invoke(attempt, e, delayMs)
                    delay(delayMs)
                }
            }
        }
        return Result.failure(lastException ?: IllegalStateException("Execution failed after $maxAttempts attempts."))
    }

    /**
     * Synchronous / non-suspending execution with exponential backoff (using thread sleep on platforms that support it).
     */
    fun <T> executeSync(
        sleeper: (Long) -> Unit = { /* no-op or platform sleep */ },
        onRetry: ((attempt: Int, exception: Throwable, delayMs: Long) -> Unit)? = null,
        block: (attempt: Int) -> T
    ): Result<T> {
        var lastException: Throwable? = null
        for (attempt in 1..maxAttempts) {
            try {
                val result = block(attempt)
                return Result.success(result)
            } catch (e: Throwable) {
                lastException = e
                if (attempt < maxAttempts) {
                    val delayMs = calculateDelayWithJitterMs(attempt)
                    onRetry?.invoke(attempt, e, delayMs)
                    sleeper(delayMs)
                }
            }
        }
        return Result.failure(lastException ?: IllegalStateException("Execution failed after $maxAttempts attempts."))
    }
}
