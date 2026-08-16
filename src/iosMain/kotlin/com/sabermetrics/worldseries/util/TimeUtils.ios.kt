package com.sabermetrics.worldseries.util

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

/**
 * iOS / Apple Native actual implementation for UTC epoch milliseconds.
 */
actual fun currentTimeMillisUtc(): Long = (NSDate().timeIntervalSince1970 * 1000.0).toLong()
