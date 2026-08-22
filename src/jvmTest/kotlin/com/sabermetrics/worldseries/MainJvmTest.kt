package com.sabermetrics.worldseries

import com.sabermetrics.worldseries.data.SabermetricDataService
import com.sabermetrics.worldseries.engine.WorldSeriesSimulator
import com.sabermetrics.worldseries.util.TimeUtils
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

        // Verify PocketHost cloud sync payload exists
        val syncPayloadFile = File("output_datasets/pockethost_sync_payload.json")
        assertTrue(syncPayloadFile.exists(), "PocketHost sync payload must exist")
        assertTrue(syncPayloadFile.length() > 500L, "PocketHost sync payload must contain serialized JSON")

        // Verify Local SQLite Database and SQL Dump exist
        val sqliteDbFile = File("output_datasets/mlb_sabermetrics_local.sqlite")
        assertTrue(sqliteDbFile.exists(), "Local SQLite Database file must exist")
        assertTrue(sqliteDbFile.length() > 1000L, "Local SQLite Database file must be non-empty (> 1KB)")

        val sqlDumpFile = File("output_datasets/mlb_sabermetrics_local_dump.sql")
        assertTrue(sqlDumpFile.exists(), "Local SQL dump script must exist")
        assertTrue(sqlDumpFile.length() > 1000L, "Local SQL dump script must be non-empty (> 1KB)")

        // Verify all 8 chart images exist and are non-empty
        val chartFiles = listOf(
            File("docs/charts/world_series_win_probabilities.png"),
            File("docs/charts/team_probability_trends_over_time.png"),
            File("docs/charts/residual_luck_bias_decomposition.png"),
            File("docs/charts/roster_anchors_leaderboard.png"),
            File("docs/charts/cross_league_matchup_matrix.png"),
            File("docs/charts/causal_vs_correlational_survival_framework.png"),
            File("docs/charts/pockethost_cloud_sync_architecture.png"),
            File("docs/charts/monte_carlo_outcome_propensities.png")
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

        val year = TimeUtils.currentSeasonYear()

        // Test each visual chart generation function with dynamic season year
        generateChartImage(result.leaderboard.take(8), year)
        generateLineChartImage(result.leaderboard.take(8), year)
        generateLuckResidualChartImage(result.leaderboard, year)
        generateRosterAnchorsChartImage(result.leaderboard.take(10), year)
        generateCrossLeagueMatchupChartImage(year)
        generateCausalSurvivalFrameworkImage(year)
        generatePocketHostCloudSyncArchitectureImage(42L)
        generateOutcomePropensitySamplingChartImage(result.leaderboard.take(8), year)

        val barChart = File("docs/charts/world_series_win_probabilities.png")
        assertTrue(barChart.exists() && barChart.length() > 0)
    }

    @Test
    fun testLocalSqliteDatabaseExport() {
        val result = WorldSeriesSimulator.runWorldSeriesSimulation(iterations = 500, seed = 42L)
        val tempDir = File("build/tmp/test_local_db")
        if (tempDir.exists()) tempDir.deleteRecursively()
        tempDir.mkdirs()

        val report = com.sabermetrics.worldseries.sync.LocalSqliteDatabaseService.exportToLocalSqliteDatabase(
            runId = "test_run_sqlite_001",
            result = result,
            epochTimestampMs = 1787390000000L,
            outputDir = tempDir
        )

        assertTrue(report.isSuccessful, "Local database export must succeed")
        assertEquals(5, report.totalTablesCreated, "5 tables must be created")
        assertTrue(report.totalRowsInserted >= 90, "At least 90 rows must be inserted")
        assertTrue(report.sqliteFile.exists() && report.sqliteFile.length() > 0, "SQLite file must exist")
        assertTrue(report.sqlDumpFile.exists() && report.sqlDumpFile.length() > 0, "SQL dump file must exist")
    }
}
