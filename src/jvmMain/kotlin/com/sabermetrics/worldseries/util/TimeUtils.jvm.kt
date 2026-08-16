package com.sabermetrics.worldseries.util

/**
 * JVM actual implementation for UTC epoch milliseconds.
 */
actual fun currentTimeMillisUtc(): Long = System.currentTimeMillis()
