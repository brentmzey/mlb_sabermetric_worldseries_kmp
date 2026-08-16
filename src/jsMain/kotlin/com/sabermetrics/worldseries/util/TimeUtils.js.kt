package com.sabermetrics.worldseries.util

import kotlin.js.Date

/**
 * JS actual implementation for UTC epoch milliseconds.
 */
actual fun currentTimeMillisUtc(): Long = Date.now().toLong()
