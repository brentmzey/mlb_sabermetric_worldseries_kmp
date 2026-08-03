package com.sabermetrics.worldseries.model

import kotlin.math.pow

enum class League { AL, NL }
enum class Division { EAST, CENTRAL, WEST }

/**
 * Clean Open-Source Sabermetric Team Record & Advanced Analytical Metrics.
 */
data class MlbTeam(
    val id: String,
    val name: String,
    val league: League,
    val division: Division,
    val wins: Int,
    val losses: Int,
    val runsScored: Double,
    val runsAllowed: Double,
    val teamWar: Double,
    val wOBA: Double,
    val wRCPlus: Double,
    val fip: Double,
    val xFip: Double,
    val bullpenWpa: Double,
    val top3AceEra: Double,
    val tradeDeadlineWarAdded: Double = 0.0,
    val thumbsDownHypeIndex: Double = 1.0 // Fan Hype / Rally Index (0.5 to 2.0)
) {
    val gamesPlayed: Int get() = wins + losses
    val winPct: Double get() = if (gamesPlayed > 0) wins.toDouble() / gamesPlayed else 0.500
    val runDifferential: Double get() = runsScored - runsAllowed

    /**
     * Bill James Pythagorean Win Expectancy with Pythagenpat exponent (1.83).
     */
    val pythagoreanWinPct: Double get() {
        val rExp = runsScored.pow(1.83)
        val raExp = runsAllowed.pow(1.83)
        return if (rExp + raExp > 0) rExp / (rExp + raExp) else 0.500
    }

    val pythagoreanWinsExpected: Double get() = pythagoreanWinPct * 162.0

    /**
     * BaseRuns (BSR) Expected Runs Scored Approximation.
     * BSR = A * B / (B + C) + D
     */
    val baseRunsEstimate: Double get() {
        val A = runsScored * 0.85 // On-base factor
        val B = runsScored * 0.55 // Score factor
        val C = runsScored * 0.40 // Out factor
        val D = runsScored * 0.05 // Home runs
        return (A * B / (B + C + 1e-5)) + D
    }
}

/**
 * Probability summary for each MLB team's postseason and World Series outcomes.
 */
data class TeamProbability(
    val team: MlbTeam,
    val playoffProb: Double,      // Probability of reaching postseason
    val pennantProb: Double,      // Probability of winning AL/NL Pennant
    val worldSeriesWinProb: Double, // Probability of winning World Series
    val expectedSeasonWins: Double,
    val latentQualityScore: Double
)

/**
 * Complete Output of 10,000-Iteration Sabermetric Monte Carlo Simulation.
 */
data class WorldSeriesSimulationResult(
    val totalSimulations: Int,
    val leaderboard: List<TeamProbability>,
    val causalDiagnostics: Map<String, String>,
    val generatedCsvExport: String
)
