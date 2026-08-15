package com.sabermetrics.worldseries

import com.sabermetrics.worldseries.data.SabermetricDataService
import com.sabermetrics.worldseries.engine.WorldSeriesSimulator
import java.io.File
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MainJvmTest {

    @Test
    fun testMainCliExecutionAndArtifactGeneration() {
        // Run main CLI entrypoint
        main()

        // Verify CSV export was generated and has valid content
        val csvFile = File("output_datasets/mlb_sabermetric_clean_dataset.csv")
        assertTrue(csvFile.exists(), "Clean CSV dataset must exist")
        val csvLines = csvFile.readLines()
        assertTrue(csvLines.size >= 31, "CSV must contain header + 30 teams")
        assertTrue(csvLines[0].contains("Team_ID"), "CSV header must contain Team_ID")
        assertTrue(csvLines[0].contains("Pythagorean_Win_Pct"), "CSV header must contain Pythagorean_Win_Pct")

        // Verify all 5 chart images exist and are non-empty
        val chartFiles = listOf(
            File("docs/charts/world_series_win_probabilities.png"),
            File("docs/charts/team_probability_trends_over_time.png"),
            File("docs/charts/residual_luck_bias_decomposition.png"),
            File("docs/charts/roster_anchors_leaderboard.png"),
            File("docs/charts/cross_league_matchup_matrix.png")
        )

        for (cf in chartFiles) {
            assertTrue(cf.exists(), "Chart file ${cf.name} must exist")
            assertTrue(cf.length() > 1000L, "Chart file ${cf.name} must be a valid PNG image (> 1KB)")
        }
    }

    @Test
    fun testIndividualChartGenerators() {
        val result = WorldSeriesSimulator.runWorldSeriesSimulation(iterations = 500, seed = 42L)
        assertNotNull(result)
        assertEquals(30, result.leaderboard.size)

        // Test each visual chart generation function
        generateChartImage(result.leaderboard.take(8))
        generateLineChartImage(result.leaderboard.take(8))
        generateLuckResidualChartImage(result.leaderboard)
        generateRosterAnchorsChartImage(result.leaderboard.take(10))
        generateCrossLeagueMatchupChartImage()

        val barChart = File("docs/charts/world_series_win_probabilities.png")
        assertTrue(barChart.exists() && barChart.length() > 0)
    }
}
