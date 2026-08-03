package com.sabermetrics.worldseries

import com.sabermetrics.worldseries.engine.WorldSeriesSimulator
import com.sabermetrics.worldseries.model.TeamProbability
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

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

    // Generate high-resolution chart graphic
    generateChartImage(result.leaderboard.take(8))
    println("=================================================================================================================\n")
}

fun generateChartImage(topTeams: List<TeamProbability>) {
    val width = 1200
    val height = 750
    val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val g = img.createGraphics()

    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

    // Dark sleek background
    g.color = Color(15, 23, 42)
    g.fillRect(0, 0, width, height)

    // Card background
    g.color = Color(30, 41, 59)
    g.fillRoundRect(40, 40, width - 80, height - 80, 24, 24)

    // Title & Subtitle
    g.color = Color(248, 250, 252)
    g.font = Font("SansSerif", Font.BOLD, 32)
    g.drawString("⚾ 2026 MLB World Series Winning Probabilities", 70, 95)

    g.color = Color(148, 163, 184)
    g.font = Font("SansSerif", Font.PLAIN, 18)
    g.drawString("10,000-Iteration Sabermetric & Causal Monte Carlo Simulation Engine", 70, 130)

    val maxProb = topTeams.first().worldSeriesWinProb.coerceAtLeast(0.01)
    val barMaxPx = 620
    var yPos = 180

    val barColors = listOf(
        Color(14, 165, 233), // Sky Blue
        Color(99, 102, 241), // Indigo
        Color(168, 85, 247), // Purple
        Color(236, 72, 153), // Pink
        Color(245, 158, 11), // Amber
        Color(16, 185, 129), // Emerald
        Color(20, 184, 166), // Teal
        Color(100, 116, 139) // Slate
    )

    for ((idx, tp) in topTeams.withIndex()) {
        val t = tp.team
        val pct = tp.worldSeriesWinProb * 100
        val barW = ((tp.worldSeriesWinProb / maxProb) * barMaxPx).toInt().coerceAtLeast(12)

        // Rank & Team Name
        g.color = Color(226, 232, 240)
        g.font = Font("SansSerif", Font.BOLD, 18)
        val nameLabel = "#${idx + 1} ${t.name}"
        g.drawString(nameLabel, 70, yPos + 25)

        // Bar Graphic
        g.color = barColors[idx % barColors.size]
        g.fillRoundRect(330, yPos + 4, barW, 30, 12, 12)

        // Percentage Text
        g.color = Color(255, 255, 255)
        g.font = Font("SansSerif", Font.BOLD, 18)
        val pctStr = "%.2f%%".format(pct) + if (t.id == "NYY") " 👎" else ""
        g.drawString(pctStr, 345 + barW, yPos + 26)

        yPos += 58
    }

    // Footer Watermark
    g.color = Color(100, 116, 139)
    g.font = Font("SansSerif", Font.ITALIC, 15)
    g.drawString("Dedicated to Brian, Patrick, & Matthew | Inspired by Yankees Thumbs-Down Rally 👎 | Open Source KMP Engine", 70, height - 65)

    g.dispose()

    val chartDir = File("docs/charts")
    if (!chartDir.exists()) chartDir.mkdirs()

    val outFile = File(chartDir, "world_series_win_probabilities.png")
    ImageIO.write(img, "PNG", outFile)
    println("🖼️  Visual Chart Image generated at:")
    println("   file://${outFile.absolutePath}")
}
