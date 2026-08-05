package com.sabermetrics.worldseries

import com.sabermetrics.worldseries.data.SabermetricDataService
import com.sabermetrics.worldseries.engine.WorldSeriesSimulator
import com.sabermetrics.worldseries.model.Division
import com.sabermetrics.worldseries.model.League
import com.sabermetrics.worldseries.model.MlbTeamId
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SabermetricTest {

    @Test
    fun testMlbTeamIdEnumAndParsing() {
        assertEquals(30, MlbTeamId.entries.size)

        // Parse code (case insensitive)
        assertEquals(MlbTeamId.NYY, MlbTeamId.parseCode("nyy"))
        assertEquals(MlbTeamId.LAD, MlbTeamId.parseCode("LAD"))
        assertNull(MlbTeamId.parseCode("INVALID"))

        // Strict parse
        assertEquals(MlbTeamId.BAL, MlbTeamId.fromCode("BAL"))

        // Parse by full name
        assertEquals(MlbTeamId.NYY, MlbTeamId.parseName("New York Yankees"))
        assertEquals(MlbTeamId.BOS, MlbTeamId.parseName("boston red sox"))

        // Filter by League and Division
        val alEast = MlbTeamId.byLeagueAndDivision(League.AL, Division.EAST)
        assertEquals(5, alEast.size)
        assertTrue(alEast.contains(MlbTeamId.NYY))

        val nlTeams = MlbTeamId.byLeague(League.NL)
        assertEquals(15, nlTeams.size)
    }

    @Test
    fun testDatasetLoadingAndPythagoreanWins() {
        val teams = SabermetricDataService.loadCleanedMlbDataset()
        assertEquals(30, teams.size)

        val nyy = SabermetricDataService.getTeam(MlbTeamId.NYY)
        assertEquals("NYY", nyy.id)
        assertEquals("New York Yankees", nyy.name)
        assertEquals(League.AL, nyy.league)
        assertEquals(Division.EAST, nyy.division)
        assertTrue(nyy.pythagoreanWinPct > 0.55)
        assertTrue(nyy.runDifferential > 100.0)

        val lad = SabermetricDataService.findTeamByCode("LAD")
        assertNotNull(lad)
        assertEquals(MlbTeamId.LAD, lad.teamId)
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

        // Verify NYY or LAD is at the top of the leaderboard
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

