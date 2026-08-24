package com.sabermetrics.worldseries.util

/**
 * Log levels for structured multiplatform semantic logging.
 */
enum class LogLevel(val label: String) {
    DEBUG("DEBUG"),
    INFO("INFO "),
    STAGE("STAGE"),
    METRIC("METR "),
    SUCCESS("OK   "),
    WARN("WARN "),
    ERROR("ERROR")
}

/**
 * Enterprise semantic logger with ANSI color styling, timestamps, and structured metric badges.
 */
object SemanticLogger {

    private const val RESET = "\u001B[0m"
    private const val BOLD = "\u001B[1m"
    private const val DIM = "\u001B[2m"
    private const val CYAN = "\u001B[36m"
    private const val BRIGHT_GREEN = "\u001B[92m"
    private const val BRIGHT_YELLOW = "\u001B[93m"
    private const val BRIGHT_RED = "\u001B[91m"
    private const val BRIGHT_BLUE = "\u001B[94m"
    private const val BRIGHT_CYAN = "\u001B[96m"
    private const val MAGENTA = "\u001B[35m"

    var isColorEnabled: Boolean = true

    fun banner(title: String, subtitle: String? = null) {
        val width = 88
        val line = "═".repeat(width)
        println()
        if (isColorEnabled) {
            println("$BOLD$BRIGHT_BLUE╔$line╗$RESET")
            println("$BOLD$BRIGHT_BLUE║  ⚾ ${title.padEnd(width - 5)}║$RESET")
            if (subtitle != null) {
                println("$DIM$BRIGHT_CYAN║     ${subtitle.padEnd(width - 5)}║$RESET")
            }
            println("$BOLD$BRIGHT_BLUE╚$line╝$RESET")
        } else {
            println("╔$line╗")
            println("║  ⚾ ${title.padEnd(width - 5)}║")
            if (subtitle != null) {
                println("║     ${subtitle.padEnd(width - 5)}║")
            }
            println("╚$line╝")
        }
    }

    fun stage(stageNumber: Int, totalStages: Int, title: String) {
        val bar = "═".repeat(76)
        println()
        if (isColorEnabled) {
            println("$BOLD$MAGENTA┌$bar┐$RESET")
            println("$BOLD$MAGENTA│  🚀 STAGE [$stageNumber/$totalStages]: ${title.padEnd(58)}│$RESET")
            println("$BOLD$MAGENTA└$bar┘$RESET")
        } else {
            println("┌$bar┐")
            println("│  🚀 STAGE [$stageNumber/$totalStages]: ${title.padEnd(58)}│")
            println("└$bar┘")
        }
    }

    fun log(level: LogLevel, serviceName: String, message: String, attributes: Map<String, Any> = emptyMap()) {
        val ts = TimeUtils.formatIsoTimestampUtc(TimeUtils.currentTimeMillisUtc())
        val badge = when (level) {
            LogLevel.INFO -> if (isColorEnabled) "$CYAN[INFO ]$RESET" else "[INFO ]"
            LogLevel.STAGE -> if (isColorEnabled) "$BOLD$MAGENTA[STAGE]$RESET" else "[STAGE]"
            LogLevel.METRIC -> if (isColorEnabled) "$BRIGHT_BLUE[METR ]$RESET" else "[METR ]"
            LogLevel.SUCCESS -> if (isColorEnabled) "$BOLD$BRIGHT_GREEN[OK   ]$RESET" else "[OK   ]"
            LogLevel.WARN -> if (isColorEnabled) "$BOLD$BRIGHT_YELLOW[WARN ]$RESET" else "[WARN ]"
            LogLevel.ERROR -> if (isColorEnabled) "$BOLD$BRIGHT_RED[ERROR]$RESET" else "[ERROR]"
            LogLevel.DEBUG -> if (isColorEnabled) "$DIM[DEBUG]$RESET" else "[DEBUG]"
        }

        val dimTs = if (isColorEnabled) "$DIM$ts$RESET" else ts
        val dimSvc = if (isColorEnabled) "$DIM[$serviceName]$RESET" else "[$serviceName]"

        val attrStr = if (attributes.isNotEmpty()) {
            val pairs = attributes.entries.joinToString(", ") { "${it.key}=${it.value}" }
            if (isColorEnabled) " $DIM($pairs)$RESET" else " ($pairs)"
        } else ""

        println("$dimTs $badge $dimSvc $message$attrStr")
    }

    fun info(serviceName: String, message: String, attributes: Map<String, Any> = emptyMap()) =
        log(LogLevel.INFO, serviceName, message, attributes)

    fun success(serviceName: String, message: String, attributes: Map<String, Any> = emptyMap()) =
        log(LogLevel.SUCCESS, serviceName, message, attributes)

    fun warn(serviceName: String, message: String, attributes: Map<String, Any> = emptyMap()) =
        log(LogLevel.WARN, serviceName, message, attributes)

    fun error(serviceName: String, message: String, attributes: Map<String, Any> = emptyMap()) =
        log(LogLevel.ERROR, serviceName, message, attributes)

    fun metric(serviceName: String, key: String, value: Any, unit: String = "") {
        val valStr = "$value$unit"
        val msg = if (isColorEnabled) "$BOLD$key$RESET = $BRIGHT_CYAN$valStr$RESET" else "$key = $valStr"
        log(LogLevel.METRIC, serviceName, msg)
    }
}
