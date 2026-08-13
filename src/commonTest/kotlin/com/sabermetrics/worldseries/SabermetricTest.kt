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
        assertTrue(nyy.runDifferential > 50.0)

        val lad = SabermetricDataService.findTeamByCode("LAD")
        assertNotNull(lad)
        assertEquals(MlbTeamId.LAD, lad.teamId)
    }

    @Test
    fun testRecencyWeightingAndSeasonConsistency() {
        val teams = SabermetricDataService.loadCleanedMlbDataset()
        for (t in teams) {
            assertTrue(t.recencyWeightedWinPct in 0.20..0.80, "Recency weighted win % for ${t.id} out of bounds")
            assertTrue(t.seasonConsistencyIndex in 0.85..1.15, "Season consistency index for ${t.id} out of bounds")
            assertTrue(t.last10WinPct in 0.0..1.0, "Last 10 win % for ${t.id} out of bounds")
        }

        // Test team with strong recent form vs cold team
        val tbd = SabermetricDataService.getTeam(MlbTeamId.TBD) // Rays 9-1 in last 10
        assertTrue(tbd.last10WinPct == 0.90)
        assertTrue(tbd.recencyWeightedWinPct > tbd.winPct)

        val chc = SabermetricDataService.getTeam(MlbTeamId.CHC) // Cubs 8-2 in last 10
        assertEquals(0.80, chc.last10WinPct, 1e-4)
        assertTrue(chc.fourPillarConsistencyIndex in 0.85..1.15)
        assertTrue(chc.compositeExpertMediaIndex in 0.85..1.25)
        assertTrue(chc.defensiveEfficiencyRating in 0.90..1.15)
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

        // Verify top contender has significant championship odds
        val topTeam = result.leaderboard.first()
        assertTrue(topTeam.worldSeriesWinProb > 0.10)
    }

    @Test
    fun testExportCleanCsvDataset() {
        val teams = SabermetricDataService.loadCleanedMlbDataset()
        val result = WorldSeriesSimulator.runWorldSeriesSimulation(iterations = 100, seed = 42L)
        val csv = SabermetricDataService.exportCleanCsvDataset(teams, result.leaderboard)
        assertTrue(csv.contains("Team_ID,Team_Name,League,Division"))
        assertTrue(csv.contains("Recency_Win_Pct,Season_Consistency_Index"))
        assertTrue(csv.contains("Regular_Season_Rank,Sim_Rank,Rank_Movement"))
        assertTrue(csv.contains("NYY,\"New York Yankees\""))
        assertTrue(csv.contains("LAD,\"Los Angeles Dodgers\""))
    }

    @Test
    fun testStandingsMovementTracking() {
        val result = WorldSeriesSimulator.runWorldSeriesSimulation(iterations = 500, seed = 999L)
        assertTrue(result.leaderboard.all { it.regularSeasonRank in 1..30 })
        assertTrue(result.leaderboard.all { it.simRank in 1..30 })

        // Check Dodgers rank tracking
        val lad = result.leaderboard.first { it.team.teamId == MlbTeamId.LAD }
        assertTrue(lad.simRank in 1..5)
    }

    @Test
    fun testPocketHostDataTrackerPayloads() {
        val result = WorldSeriesSimulator.runWorldSeriesSimulation(iterations = 100, seed = 42L)
        val runPayload = com.sabermetrics.worldseries.data.PocketHostDataTracker.buildSimulationRunJsonPayload("RUN-TEST-001", result, 42L)
        assertTrue(runPayload.contains("\"str_run_id\": \"RUN-TEST-001\""))
        assertTrue(runPayload.contains("\"int_total_iterations\": 100"))

        val movePayload = com.sabermetrics.worldseries.data.PocketHostDataTracker.buildRankMovementsJsonPayload("RUN-TEST-001", result.leaderboard)
        assertTrue(movePayload.contains("\"str_team_code\": \"NYY\""))
    }
}



