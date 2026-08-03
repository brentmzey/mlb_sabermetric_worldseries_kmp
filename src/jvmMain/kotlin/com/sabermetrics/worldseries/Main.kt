package com.sabermetrics.worldseries

import com.sabermetrics.worldseries.engine.WorldSeriesSimulator
import java.io.File

fun main() {
    println("=================================================================================================================")
    println(" ⚾ MLB SABERMETRIC WORLD SERIES PREDICTION & CAUSAL ESTIMATION SUITE (KMP Multiplatform)")
    println("    Clean Open-Source Datasets + 10,000-Iteration Postseason Monte Carlo Simulator")
    println("    Dedicated to Brian, Patrick, and Matthew (Inspired by Yankees Thumbs-Down Rally)")
    println("=================================================================================================================\n")

    println("🧠 ECONOMETRIC THEORY & SABERMETRIC REASONING BEHIND PREDICTIONS:")
    println("   1. Pythagorean Run Differential: Filters 1-run game noise; true quality is driven by R vs RA exponent (1.83).")
    println("   2. BaseRuns (BSR) Component Model: Eliminates sequence-dependent luck by isolating raw baserunner creation.")
    println("   3. 2SLS IV Causal Model: Instruments win totals with Pythagorean expectation & SOS to remove endogeneity.")
    println("   4. Postseason Compression: Short series leverage Top-3 Ace ERAs and high-leverage bullpen WPA over roster depth.")
    println("   5. Thumbs-Down Hype Index: Clubhouse momentum & trade additions boost non-linear October performance.\n")

    println("⏳ Running 10,000-iteration Monte Carlo playoff simulation...")
    val result = WorldSeriesSimulator.runWorldSeriesSimulation(iterations = 10000, seed = 20260803L)

    println("\n🏆 MLB WORLD SERIES WIN PROBABILITY LEADERBOARD (10,000 SIMULATIONS):")
    println("-----------------------------------------------------------------------------------------------------------------")
    println("%-4s | %-24s | %-6s | %-8s | %-12s | %-12s | %-14s | %-12s".format(
        "Rank", "Team Name", "Lg/Div", "W-L", "Sim Wins", "Playoff %", "Pennant %", "World Series %"
    ))
    println("-----------------------------------------------------------------------------------------------------------------")

    var rank = 1
    for (tp in result.leaderboard) {
        val t = tp.team
        val lgDiv = "${t.league}-${t.division.name.take(1)}"
        val wl = "${t.wins}-${t.losses}"
        val playoffStr = "%.1f%%".format(tp.playoffProb * 100)
        val pennantStr = "%.1f%%".format(tp.pennantProb * 100)
        val wsStr = "%.2f%%".format(tp.worldSeriesWinProb * 100)

        val star = if (t.id == "NYY") " 👎 (Thumbs Down Rally)" else ""
        println("%-4d | %-24s | %-6s | %-8s | %-12.1f | %-12s | %-14s | %-12s%s".format(
            rank, t.name, lgDiv, wl, tp.expectedSeasonWins, playoffStr, pennantStr, wsStr, star
        ))
        rank++
    }
    println("-----------------------------------------------------------------------------------------------------------------")

    println("\n📊 CAUSAL MODEL & SIMULATION DIAGNOSTICS:")
    for ((key, value) in result.causalDiagnostics) {
        println("   • $key: $value")
    }

    // Export clean CSV dataset
    val outputDir = File("output_datasets")
    if (!outputDir.exists()) outputDir.mkdirs()

    val csvFile = File(outputDir, "mlb_sabermetric_clean_dataset.csv")
    csvFile.writeText(result.generatedCsvExport)

    println("\n📁 Open-Source Cleaned Sabermetric Dataset exported to:")
    println("   file://${csvFile.absolutePath}")
    println("=================================================================================================================\n")
}
