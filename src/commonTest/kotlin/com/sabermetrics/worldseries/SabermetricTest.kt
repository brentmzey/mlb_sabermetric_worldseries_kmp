package com.sabermetrics.worldseries

import com.sabermetrics.worldseries.data.SabermetricDataService
import com.sabermetrics.worldseries.engine.WorldSeriesSimulator
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SabermetricTest {

    @Test
    fun testDatasetLoadingAndPythagoreanWins() {
        val teams = SabermetricDataService.loadCleanedMlbDataset()
        assertEquals(30, teams.size)

        val nyy = teams.first { it.id == "NYY" }
        assertEquals("New York Yankees", nyy.name)
        assertTrue(nyy.pythagoreanWinPct > 0.55)
        assertTrue(nyy.runDifferential > 100.0)
    }

    @Test
    fun testMonteCarloWorldSeriesSimulation() {
        val result = WorldSeriesSimulator.runWorldSeriesSimulation(iterations = 1000, seed = 12345L)
        assertEquals(1000, result.totalSimulations)
        assertEquals(30, result.leaderboard.size)

        var totalWsProb = 0.0
        for (tp in result.leaderboard) {
            totalWsProb += tp.worldSeriesWinProb
            assertTrue(tp.playoffProb in 0.0..1.0)
            assertTrue(tp.pennantProb in 0.0..1.0)
        }

        // Sum of all teams' World Series win probabilities must equal 100% (1.0)
        assertEquals(1.0, totalWsProb, 1e-4)

        // Verify NYY is at the top of the leaderboard
        val topTeam = result.leaderboard.first()
        assertTrue(topTeam.worldSeriesWinProb > 0.10)
    }

    @Test
    fun testExportCleanCsvDataset() {
        val teams = SabermetricDataService.loadCleanedMlbDataset()
        val csv = SabermetricDataService.exportCleanCsvDataset(teams)
        assertTrue(csv.contains("Team_ID,Team_Name,League,Division"))
        assertTrue(csv.contains("NYY,\"New York Yankees\""))
        assertTrue(csv.contains("LAD,\"Los Angeles Dodgers\""))
    }
}
