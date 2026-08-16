package com.sabermetrics.worldseries.util

/**
 * Cross-platform expect declaration for fetching current UTC epoch milliseconds.
 */
expect fun currentTimeMillisUtc(): Long

/**
 * Comprehensive, Pure-Kotlin Sabermetric Time & Datetime Utility Suite.
 * Provides dynamically calculated UTC epoch milliseconds, ISO-8601 formatting,
 * civil calendar translations, season year detection, and run identifier generators.
 */
object TimeUtils {

    /**
     * Returns the current timestamp in UTC Epoch Milliseconds.
     */
    fun currentTimeMillisUtc(): Long = com.sabermetrics.worldseries.util.currentTimeMillisUtc()

    /**
     * Returns the current timestamp in UTC Epoch Milliseconds.
     */
    fun nowEpochMs(): Long = currentTimeMillisUtc()

    /**
     * Alias for `nowEpochMs()`.
     */
    fun currentTimeMillis(): Long = currentTimeMillisUtc()

    /**
     * Returns the current UTC timestamp formatted as ISO-8601 string (e.g. "2026-08-15T07:57:35.000Z").
     */
    fun currentIsoTimestampUtc(): String = formatIsoTimestampUtc(currentTimeMillisUtc())

    /**
     * Returns the current MLB season year dynamically calculated from current system clock.
     */
    fun currentSeasonYear(): Int = getSeasonYear(currentTimeMillisUtc())

    /**
     * Extracts the 4-digit Gregorian calendar year (season year) from UTC Epoch Milliseconds.
     */
    fun getSeasonYear(epochMs: Long = currentTimeMillisUtc()): Int {
        val (year, _, _) = epochMsToCivilDate(epochMs)
        return year
    }

    /**
     * Formats UTC Epoch Milliseconds to standard ISO-8601 UTC timestamp string:
     * `YYYY-MM-DDTHH:mm:ss.sssZ`
     */
    fun formatIsoTimestampUtc(epochMs: Long): String {
        val (year, month, day) = epochMsToCivilDate(epochMs)
        val (hour, minute, second, millis) = epochMsToTimeComponents(epochMs)
        return "${year.toString().padStart(4, '0')}-${month.pad2()}-${day.pad2()}T${hour.pad2()}:${minute.pad2()}:${second.pad2()}.${millis.pad3()}Z"
    }

    /**
     * Formats UTC Epoch Milliseconds to date string: `YYYY-MM-DD`.
     */
    fun formatDateUtc(epochMs: Long = currentTimeMillisUtc()): String {
        val (year, month, day) = epochMsToCivilDate(epochMs)
        return "${year.toString().padStart(4, '0')}-${month.pad2()}-${day.pad2()}"
    }

    /**
     * Formats UTC Epoch Milliseconds to compact date string: `YYYYMMDD`.
     */
    fun formatCompactDateUtc(epochMs: Long = currentTimeMillisUtc()): String {
        val (year, month, day) = epochMsToCivilDate(epochMs)
        return "${year.toString().padStart(4, '0')}${month.pad2()}${day.pad2()}"
    }

    /**
     * Formats UTC Epoch Milliseconds to compact timestamp string: `YYYYMMDD-HHmmss`.
     */
    fun formatCompactDateTimeUtc(epochMs: Long = currentTimeMillisUtc()): String {
        val (year, month, day) = epochMsToCivilDate(epochMs)
        val (hour, minute, second, _) = epochMsToTimeComponents(epochMs)
        return "${year.toString().padStart(4, '0')}${month.pad2()}${day.pad2()}-${hour.pad2()}${minute.pad2()}${second.pad2()}"
    }

    /**
     * Dynamically generates a standard Monte Carlo Simulation Run ID with timestamp:
     * e.g. `RUN-20260815-123000` or `RUN-2026-POSTSEASON-MC10K`
     */
    fun generateRunId(prefix: String = "RUN", epochMs: Long = currentTimeMillisUtc()): String {
        val compact = formatCompactDateTimeUtc(epochMs)
        return "$prefix-$compact"
    }

    /**
     * Converts civil date and time components into UTC Epoch Milliseconds.
     */
    fun createEpochMsUtc(
        year: Int,
        month: Int,
        day: Int,
        hour: Int = 0,
        minute: Int = 0,
        second: Int = 0,
        millis: Int = 0
    ): Long {
        require(month in 1..12) { "Month must be between 1 and 12: $month" }
        require(day in 1..31) { "Day must be between 1 and 31: $day" }
        require(hour in 0..23) { "Hour must be between 0 and 23: $hour" }
        require(minute in 0..59) { "Minute must be between 0 and 59: $minute" }
        require(second in 0..59) { "Second must be between 0 and 59: $second" }
        require(millis in 0..999) { "Millis must be between 0 and 999: $millis" }

        val y = year - (if (month <= 2) 1 else 0)
        val era = (if (y >= 0) y else y - 399) / 400
        val yoe = y - era * 400
        val doy = (153 * (month + (if (month > 2) -3 else 9)) + 2) / 5 + day - 1
        val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
        val totalDays = era * 146097L + doe - 719468L
        val totalSeconds = totalDays * 86400L + hour * 3600L + minute * 60L + second
        return totalSeconds * 1000L + millis
    }

    /**
     * Parses standard ISO-8601 UTC timestamp or date strings:
     * - `YYYY-MM-DDTHH:mm:ss.sssZ`
     * - `YYYY-MM-DDTHH:mm:ssZ`
     * - `YYYY-MM-DD`
     */
    fun parseIsoTimestampUtc(isoString: String): Long {
        val trimmed = isoString.trim()
        val isoRegex = Regex("""^(\d{4})-(\d{2})-(\d{2})(?:T(\d{2}):(\d{2}):(\d{2})(?:\.(\d{1,3}))?Z?)?$""")
        val match = isoRegex.matchEntire(trimmed)
            ?: throw IllegalArgumentException("Invalid ISO-8601 timestamp format: '$isoString'")

        val year = match.groupValues[1].toInt()
        val month = match.groupValues[2].toInt()
        val day = match.groupValues[3].toInt()
        val hour = match.groupValues.getOrNull(4)?.takeIf { it.isNotEmpty() }?.toInt() ?: 0
        val minute = match.groupValues.getOrNull(5)?.takeIf { it.isNotEmpty() }?.toInt() ?: 0
        val second = match.groupValues.getOrNull(6)?.takeIf { it.isNotEmpty() }?.toInt() ?: 0
        val millisRaw = match.groupValues.getOrNull(7)?.takeIf { it.isNotEmpty() } ?: "0"
        val millis = millisRaw.padEnd(3, '0').take(3).toInt()

        return createEpochMsUtc(year, month, day, hour, minute, second, millis)
    }

    // --- Private mathematical civil date conversion helpers ---

    private fun epochMsToCivilDate(epochMs: Long): Triple<Int, Int, Int> {
        val totalSeconds = if (epochMs >= 0) epochMs / 1000L else (epochMs - 999L) / 1000L
        val totalDays = if (totalSeconds >= 0) totalSeconds / 86400L else (totalSeconds - 86399L) / 86400L

        val z = totalDays + 719468L
        val era = (if (z >= 0) z else z - 146096L) / 146097L
        val doe = (z - era * 146097L).toInt()
        val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
        val y = yoe + era * 400
        val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
        val mp = (5 * doy + 2) / 153
        val day = doy - (153 * mp + 2) / 5 + 1
        val month = mp + (if (mp < 10) 3 else -9)
        val year = (y + (if (month <= 2) 1 else 0)).toInt()

        return Triple(year, month, day)
    }

    private fun epochMsToTimeComponents(epochMs: Long): IntArray {
        var remMs = (epochMs % 86400000L)
        if (remMs < 0) remMs += 86400000L

        val millis = (remMs % 1000L).toInt()
        val totalSeconds = (remMs / 1000L).toInt()
        val second = totalSeconds % 60
        val totalMinutes = totalSeconds / 60
        val minute = totalMinutes % 60
        val hour = totalMinutes / 60

        return intArrayOf(hour, minute, second, millis)
    }

    private fun Int.pad2(): String = if (this in 0..9) "0$this" else this.toString()
    private fun Int.pad3(): String = when {
        this in 0..9 -> "00$this"
        this in 10..99 -> "0$this"
        else -> this.toString()
    }
}
