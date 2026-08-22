package com.sabermetrics.worldseries

import com.sabermetrics.worldseries.data.PocketHostDataTracker
import com.sabermetrics.worldseries.data.SabermetricDataService
import com.sabermetrics.worldseries.engine.WorldSeriesSimulator
import com.sabermetrics.worldseries.model.Division
import com.sabermetrics.worldseries.model.HungarianCollectionPrefix
import com.sabermetrics.worldseries.model.League
import com.sabermetrics.worldseries.model.MlbTeam
import com.sabermetrics.worldseries.model.MlbTeamId
import com.sabermetrics.worldseries.model.PostseasonRound
import com.sabermetrics.worldseries.model.StatPillarType
import com.sabermetrics.worldseries.model.TeamProbability
import com.sabermetrics.worldseries.repository.FWorldSeriesLeaderboardRecord
import com.sabermetrics.worldseries.repository.HungarianQueryBuilder
import com.sabermetrics.worldseries.repository.IMlbTeamRecord
import com.sabermetrics.worldseries.repository.ITeamSeasonInputRecord
import com.sabermetrics.worldseries.repository.MLatentQualityEstimateRecord
import com.sabermetrics.worldseries.repository.MSimulationRunRecord
import com.sabermetrics.worldseries.repository.RecordStatusCode
import com.sabermetrics.worldseries.sync.ExponentialBackoffPolicy
import com.sabermetrics.worldseries.sync.PocketHostConfig
import com.sabermetrics.worldseries.sync.PocketHostSyncClient
import com.sabermetrics.worldseries.util.TimeUtils
import com.sabermetrics.worldseries.util.format
import com.sabermetrics.worldseries.util.formatDecimals
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class SabermetricTest {

    @Test
    fun testMlbTeamIdEnumAndParsing() {
        assertEquals(30, MlbTeamId.entries.size)

        // Parse code (case insensitive)
        assertEquals(MlbTeamId.NYY, MlbTeamId.parseCode("nyy"))
        assertEquals(MlbTeamId.LAD, MlbTeamId.parseCode("LAD"))
        assertEquals(MlbTeamId.CHC, MlbTeamId.parseCode("  chc  "))
        assertNull(MlbTeamId.parseCode("INVALID_CODE"))

        // Strict parse
        assertEquals(MlbTeamId.BAL, MlbTeamId.fromCode("BAL"))
        assertFailsWith<IllegalArgumentException> {
            MlbTeamId.fromCode("NOT_REAL")
        }

        // Parse by full name
        assertEquals(MlbTeamId.NYY, MlbTeamId.parseName("New York Yankees"))
        assertEquals(MlbTeamId.BOS, MlbTeamId.parseName("boston red sox"))
        assertEquals(MlbTeamId.CHC, MlbTeamId.parseName("Chicago Cubs"))
        assertNull(MlbTeamId.parseName("Atlantis Whales"))

        // Parse by MLB API ID
        assertEquals(MlbTeamId.NYY, MlbTeamId.fromMlbApiId(147))
        assertEquals(MlbTeamId.CHC, MlbTeamId.fromMlbApiId(112))
        assertEquals(MlbTeamId.LAD, MlbTeamId.fromMlbApiId(119))
        assertNull(MlbTeamId.fromMlbApiId(99999))

        // Filter by League and Division
        val alEast = MlbTeamId.byLeagueAndDivision(League.AL, Division.EAST)
        assertEquals(5, alEast.size)
        assertTrue(alEast.contains(MlbTeamId.NYY))

        val nlTeams = MlbTeamId.byLeague(League.NL)
        assertEquals(15, nlTeams.size)

        val alTeams = MlbTeamId.byLeague(League.AL)
        assertEquals(15, alTeams.size)
    }

    @Test
    fun testCrossLanguageDomainRegistryEnums() {
        // 1. StatPillarType Weights Conservation
        assertEquals(4, StatPillarType.entries.size)
        val totalPillarWeight = StatPillarType.entries.sumOf { it.weight }
        assertEquals(1.00, totalPillarWeight, 0.0001)

        assertEquals(StatPillarType.OFFENSE, StatPillarType.fromCode("OFFENSE"))
        assertEquals(StatPillarType.DEFENSE, StatPillarType.fromCode("defense"))
        assertEquals(StatPillarType.STARTING_PITCHING, StatPillarType.fromCode("STARTING_PITCHING"))
        assertEquals(StatPillarType.BULLPEN_LEVERAGE, StatPillarType.fromCode("bullpen_leverage"))
        assertFailsWith<IllegalArgumentException> { StatPillarType.fromCode("UNKNOWN_PILLAR") }

        // 2. PostseasonRound Structural Integrity
        assertEquals(4, PostseasonRound.entries.size)
        assertEquals(PostseasonRound.WILD_CARD, PostseasonRound.fromCode("WILD_CARD"))
        assertEquals(PostseasonRound.DIVISION_SERIES, PostseasonRound.fromCode("division_series"))
        assertEquals(PostseasonRound.LEAGUE_CHAMPIONSHIP, PostseasonRound.fromCode("LEAGUE_CHAMPIONSHIP"))
        assertEquals(PostseasonRound.WORLD_SERIES, PostseasonRound.fromCode("WORLD_SERIES"))
        assertEquals(7, PostseasonRound.WORLD_SERIES.bestOf)
        assertEquals(4, PostseasonRound.WORLD_SERIES.winsToAdvance)
        assertEquals("2-3-2", PostseasonRound.WORLD_SERIES.homeFieldFormat)

        // 3. HungarianCollectionPrefix Relational Tiers
        assertEquals(5, HungarianCollectionPrefix.entries.size)
        assertEquals(HungarianCollectionPrefix.INPUT, HungarianCollectionPrefix.fromPrefix("i_"))
        assertEquals(HungarianCollectionPrefix.MODEL, HungarianCollectionPrefix.fromPrefix("m_"))
        assertEquals(HungarianCollectionPrefix.SUMMARY, HungarianCollectionPrefix.fromPrefix("s_"))
        assertEquals(HungarianCollectionPrefix.OUTPUT, HungarianCollectionPrefix.fromPrefix("o_"))
        assertEquals(HungarianCollectionPrefix.FINAL, HungarianCollectionPrefix.fromPrefix("f_"))
        assertFailsWith<IllegalArgumentException> { HungarianCollectionPrefix.fromPrefix("x_") }
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
        assertTrue(nyy.pythagoreanWinPct > 0.50)
        assertTrue(nyy.runDifferential > 50.0)

        val lad = SabermetricDataService.findTeamByCode("LAD")
        assertNotNull(lad)
        assertEquals(MlbTeamId.LAD, lad.teamId)

        val chc = SabermetricDataService.findTeamByCode("chc")
        assertNotNull(chc)
        assertEquals(MlbTeamId.CHC, chc.teamId)

        assertNull(SabermetricDataService.findTeamByCode("XYZ"))

        // Throws on missing team in custom subset
        assertFailsWith<NoSuchElementException> {
            val emptyService = object {
                fun getMissing() = listOf<MlbTeam>().first { it.teamId == MlbTeamId.LAD }
            }
            emptyService.getMissing()
        }
    }

    @Test
    fun testMlbTeamCalculationsAndFormulas() {
        val team = MlbTeam(
            teamId = MlbTeamId.CHC,
            wins = 71,
            losses = 50,
            runsScored = 634.0,
            runsAllowed = 519.0,
            teamWar = 33.5,
            wOBA = 0.325,
            wRCPlus = 108.0,
            fip = 3.72,
            xFip = 3.78,
            bullpenWpa = 2.8,
            top3AceEra = 3.28,
            tradeDeadlineWarAdded = 1.8,
            clubhouseHypeIndex = 1.10,
            last10Wins = 8,
            last10Losses = 2,
            seasonConsistencyScore = 1.037,
            marketImpliedWsProb = 0.075,
            expertConsensusRating = 1.10,
            mediaPowerRankRating = 1.12,
            defensiveEfficiencyRating = 1.07,
            pillarOffenseConsistency = 1.08,
            pillarDefenseConsistency = 1.08,
            pillarPitchingConsistency = 1.07,
            pillarBullpenConsistency = 1.08
        )

        assertEquals(121, team.gamesPlayed)
        assertEquals(71.0 / 121.0, team.winPct, 1e-4)
        assertEquals(115.0, team.runDifferential, 1e-4)
        assertEquals(0.80, team.last10WinPct, 1e-4)
        assertTrue(team.compositeRelativeFormScore > 0.60)
        assertTrue(team.hotStreakMomentumMultiplier in 0.92..1.08)
        assertTrue(team.pythagoreanWinPct in 0.50..0.90)
        assertTrue(team.pythagoreanWinsExpected in 80.0..140.0)
        assertTrue(team.bayesianAdjustedWinPct in 0.50..0.85)
        assertTrue(team.recencyWeightedWinPct in 0.50..0.85)
        assertTrue(team.fourPillarConsistencyIndex in 0.85..1.15)
        assertTrue(team.compositeExpertMediaIndex in 0.85..1.25)
        assertTrue(team.seasonConsistencyIndex in 0.85..1.15)
        assertTrue(team.baseRunsEstimate > 0.0)

        // Secondary constructor verification
        val teamSec = MlbTeam(
            id = "CHC",
            name = "Chicago Cubs",
            league = League.NL,
            division = Division.CENTRAL,
            wins = 71,
            losses = 50,
            runsScored = 634.0,
            runsAllowed = 519.0,
            teamWar = 33.5,
            wOBA = 0.325,
            wRCPlus = 108.0,
            fip = 3.72,
            xFip = 3.78,
            bullpenWpa = 2.8,
            top3AceEra = 3.28
        )
        assertEquals(MlbTeamId.CHC, teamSec.teamId)
        assertEquals("Chicago Cubs", teamSec.name)
        assertEquals(League.NL, teamSec.league)
    }

    @Test
    fun testMlbTeamEdgeCases() {
        val zeroTeam = MlbTeam(
            teamId = MlbTeamId.OAK,
            wins = 0,
            losses = 0,
            runsScored = 0.0,
            runsAllowed = 0.0,
            teamWar = 0.0,
            wOBA = 0.0,
            wRCPlus = 0.0,
            fip = 0.0,
            xFip = 0.0,
            bullpenWpa = 0.0,
            top3AceEra = 4.50,
            last10Wins = 0,
            last10Losses = 0
        )
        assertEquals(0, zeroTeam.gamesPlayed)
        assertEquals(0.500, zeroTeam.winPct)
        assertEquals(0.500, zeroTeam.last10WinPct)
        assertEquals(0.500, zeroTeam.pythagoreanWinPct)
        assertTrue(zeroTeam.hotStreakMomentumMultiplier in 0.92..1.08)

        // Latent quality with zero games played
        val zeroQuality = WorldSeriesSimulator.computeLatentTeamQuality(zeroTeam)
        assertTrue(zeroQuality > 0.0)

        val pZero = WorldSeriesSimulator.predictGameWinProb(zeroTeam, zeroTeam)
        assertEquals(0.50, pZero, 1e-4)

        val nullRecord = IMlbTeamRecord(
            str_team_code = "XYZ",
            str_team_name = "Null Team",
            str_league = "AL",
            str_division = "WEST",
            str_city = null,
            str_ballpark = null,
            int_founded_year = null,
            int_created_epoch_ms_utc = 1000L,
            int_updated_epoch_ms_utc = 1000L
        )
        assertNull(nullRecord.str_city)
        assertNull(nullRecord.str_ballpark)
        assertNull(nullRecord.int_founded_year)
    }

    @Test
    fun testTeamProbabilityAndMovementSymbol() {
        val team = SabermetricDataService.getTeam(MlbTeamId.LAD)
        val tpUp = TeamProbability(team, 1.0, 0.35, 0.235, 99.4, 1.042, regularSeasonRank = 4, simRank = 1, rankDelta = 3)
        assertEquals("▲ +3", tpUp.movementSymbol)

        val tpDown = TeamProbability(team, 1.0, 0.11, 0.054, 96.9, 0.892, regularSeasonRank = 3, simRank = 6, rankDelta = -3)
        assertEquals("▼ -3", tpDown.movementSymbol)

        val tpSame = TeamProbability(team, 1.0, 0.29, 0.125, 103.2, 0.957, regularSeasonRank = 2, simRank = 2, rankDelta = 0)
        assertEquals("—", tpSame.movementSymbol)
    }

    @Test
    fun testWorldSeriesSimulatorGamePredictionAndSeries() {
        val lad = SabermetricDataService.getTeam(MlbTeamId.LAD)
        val nyy = SabermetricDataService.getTeam(MlbTeamId.NYY)

        val pLadBeatsNyy = WorldSeriesSimulator.predictGameWinProb(lad, nyy)
        val pNyyBeatsLad = WorldSeriesSimulator.predictGameWinProb(nyy, lad)

        // Mathematical symmetry: P(A beats B) + P(B beats A) == 1.0
        assertEquals(1.0, pLadBeatsNyy + pNyyBeatsLad, 1e-4)
        assertTrue(pLadBeatsNyy in 0.45..0.60)

        // Custom momentum map test
        val customMomentum = mapOf(MlbTeamId.NYY to 1.10)
        val pWithBoost = WorldSeriesSimulator.predictGameWinProb(nyy, lad, customMomentum)
        assertTrue(pWithBoost > pNyyBeatsLad)

        // Series simulation test
        val rng = Random(12345)
        val winnerBestOf3 = WorldSeriesSimulator.simulateSeries(lad, nyy, bestOf = 3, random = rng)
        assertTrue(winnerBestOf3.teamId == MlbTeamId.LAD || winnerBestOf3.teamId == MlbTeamId.NYY)

        val winnerBestOf5 = WorldSeriesSimulator.simulateSeries(lad, nyy, bestOf = 5, random = rng)
        assertTrue(winnerBestOf5.teamId == MlbTeamId.LAD || winnerBestOf5.teamId == MlbTeamId.NYY)

        val winnerBestOf7 = WorldSeriesSimulator.simulateSeries(lad, nyy, bestOf = 7, random = rng)
        assertTrue(winnerBestOf7.teamId == MlbTeamId.LAD || winnerBestOf7.teamId == MlbTeamId.NYY)
    }

    @Test
    fun testMonteCarloWorldSeriesSimulationDeterminism() {
        val res1 = WorldSeriesSimulator.runWorldSeriesSimulation(iterations = 500, seed = 42L)
        val res2 = WorldSeriesSimulator.runWorldSeriesSimulation(iterations = 500, seed = 42L)

        assertEquals(res1.totalSimulations, res2.totalSimulations)
        assertEquals(res1.leaderboard.size, res2.leaderboard.size)

        for (i in res1.leaderboard.indices) {
            val t1 = res1.leaderboard[i]
            val t2 = res2.leaderboard[i]
            assertEquals(t1.team.teamId, t2.team.teamId)
            assertEquals(t1.worldSeriesWinProb, t2.worldSeriesWinProb, 1e-6)
            assertEquals(t1.pennantProb, t2.pennantProb, 1e-6)
        }

        // Sum of all teams' World Series win probabilities must equal 100% (1.0)
        val totalWsProb = res1.leaderboard.sumOf { it.worldSeriesWinProb }
        assertEquals(1.0, totalWsProb, 1e-4)
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
        assertTrue(csv.lines().size >= 31)
    }

    @Test
    fun testPocketHostDataTrackerPayloads() {
        val result = WorldSeriesSimulator.runWorldSeriesSimulation(iterations = 100, seed = 42L)
        val runPayload = PocketHostDataTracker.buildSimulationRunJsonPayload("RUN-TEST-001", result, 42L)
        assertTrue(runPayload.contains("\"str_run_id\": \"RUN-TEST-001\""))
        assertTrue(runPayload.contains("\"int_total_iterations\": 100"))

        val movePayload = PocketHostDataTracker.buildRankMovementsJsonPayload("RUN-TEST-001", result.leaderboard)
        assertTrue(movePayload.contains("\"str_team_code\": \"NYY\""))
        assertTrue(movePayload.contains("\"rel_run_id\": \"RUN-TEST-001\""))
    }

    @Test
    fun testPocketBaseHungarianModelsAndQueryBuilder() {
        val teamRecord = IMlbTeamRecord(
            id = "rec123",
            str_team_code = "CHC",
            str_team_name = "Chicago Cubs",
            str_league = "NL",
            str_division = "CENTRAL",
            str_city = "Chicago",
            str_ballpark = "Wrigley Field",
            int_founded_year = 1876,
            bool_is_active = true,
            str_status_code = RecordStatusCode.ACTIVE.name,
            int_created_epoch_ms_utc = 1723650000000L,
            int_updated_epoch_ms_utc = 1723650000000L
        )
        assertEquals("CHC", teamRecord.str_team_code)
        assertEquals("ACTIVE", teamRecord.str_status_code)
        assertTrue(teamRecord.bool_is_active)

        val inputRecord = ITeamSeasonInputRecord(
            id = "input123",
            str_team_code = "LAD",
            int_season_year = 2026,
            int_season_week = 20,
            int_wins = 72,
            int_losses = 48,
            dbl_runs_scored = 602.0,
            dbl_runs_allowed = 462.0,
            dbl_team_war = 40.0,
            dbl_woba = 0.338,
            dbl_wrc_plus = 120.0,
            dbl_fip = 3.62,
            dbl_xfip = 3.68,
            dbl_bullpen_wpa = 3.8,
            dbl_top3_ace_era = 2.70,
            int_last10_wins = 7,
            int_last10_losses = 3,
            int_created_epoch_ms_utc = 1723650000000L,
            int_updated_epoch_ms_utc = 1723650000000L
        )
        assertEquals(2026, inputRecord.int_season_year)
        assertEquals(72, inputRecord.int_wins)

        val runRecord = MSimulationRunRecord(
            id = "run123",
            str_run_id = "RUN-20260814",
            dt_run_timestamp = "2026-08-14T21:00:00Z",
            int_season_year = 2026,
            int_total_iterations = 10000,
            int_random_seed = 42,
            str_engine_version = "1.0.0",
            str_top_favorite_code = "LAD",
            dbl_top_favorite_prob = 0.2353,
            str_causal_iv_status = "ACTIVE",
            int_created_epoch_ms_utc = 1723650000000L,
            int_updated_epoch_ms_utc = 1723650000000L
        )
        assertEquals("RUN-20260814", runRecord.str_run_id)

        val latentRecord = MLatentQualityEstimateRecord(
            id = "lat123",
            str_run_id = "RUN-20260814",
            str_team_code = "NYY",
            int_season_year = 2026,
            dbl_latent_quality_score = 0.985,
            dbl_bayes_adjusted_win_pct = 0.560,
            dbl_recency_win_pct = 0.580,
            dbl_momentum_multiplier = 1.024,
            dbl_hype_multiplier = 1.10,
            int_created_epoch_ms_utc = 1723650000000L,
            int_updated_epoch_ms_utc = 1723650000000L
        )
        assertEquals(0.985, latentRecord.dbl_latent_quality_score)

        val boardRecord = FWorldSeriesLeaderboardRecord(
            id = "board123",
            str_run_id = "RUN-20260814",
            str_team_code = "MIL",
            str_team_name = "Milwaukee Brewers",
            str_league = "NL",
            str_division = "CENTRAL",
            int_sim_rank = 3,
            dbl_expected_season_wins = 99.0,
            dbl_playoff_prob = 1.0,
            dbl_pennant_prob = 0.201,
            dbl_world_series_win_prob = 0.1214,
            str_visual_bar = "██████",
            int_created_epoch_ms_utc = 1723650000000L,
            int_updated_epoch_ms_utc = 1723650000000L
        )
        assertEquals(3, boardRecord.int_sim_rank)

        // RecordStatusCode enum
        assertEquals(4, RecordStatusCode.entries.size)
        assertEquals(RecordStatusCode.ACTIVE, RecordStatusCode.valueOf("ACTIVE"))
        assertEquals(RecordStatusCode.INACTIVE, RecordStatusCode.valueOf("INACTIVE"))
        assertEquals(RecordStatusCode.SUPERSEDED, RecordStatusCode.valueOf("SUPERSEDED"))
        assertEquals(RecordStatusCode.ARCHIVED, RecordStatusCode.valueOf("ARCHIVED"))

        // Query Builder
        val teamFilter = HungarianQueryBuilder.buildLatestActiveTeamFilter("CHC")
        assertTrue(teamFilter.contains("str_team_code='CHC'"))
        assertTrue(teamFilter.contains("bool_is_active=true"))

        val runFilter = HungarianQueryBuilder.buildActiveRunFilter("RUN-999")
        assertTrue(runFilter.contains("str_run_id='RUN-999'"))
    }

    @Test
    fun testFormatUtils() {
        assertEquals("12.35", 12.3456.formatDecimals(2))
        assertEquals("12.3", 12.3456.formatDecimals(1))
        assertEquals("12", 12.3456.formatDecimals(0))
        assertEquals("-5.50", (-5.5).formatDecimals(2))
        assertEquals("0.00", 0.0.formatDecimals(2))
        assertEquals("NaN", Double.NaN.formatDecimals(2))
        assertEquals("Infinity", Double.POSITIVE_INFINITY.formatDecimals(2))
        assertEquals("-Infinity", Double.NEGATIVE_INFINITY.formatDecimals(2))

        val templ = "Team %s has %.2f WS Prob"
        val formatted = templ.format("LAD", 23.53)
        assertEquals("Team LAD has 23.53 WS Prob", formatted)

        val singleArg = "Hello %s".format("World")
        assertEquals("Hello World", singleArg)
    }

    @Test
    fun testExponentialBackoffPolicyAndRetries() {
        val policy = ExponentialBackoffPolicy(
            initialDelayMs = 200L,
            maxDelayMs = 2000L,
            factor = 2.0,
            maxAttempts = 4,
            jitterRatio = 0.10
        )

        assertEquals(200L, policy.calculateBaseDelayMs(1))
        assertEquals(400L, policy.calculateBaseDelayMs(2))
        assertEquals(800L, policy.calculateBaseDelayMs(3))
        assertEquals(1600L, policy.calculateBaseDelayMs(4))
        assertEquals(2000L, policy.calculateBaseDelayMs(5)) // Capped at maxDelayMs

        val jittered = policy.calculateDelayWithJitterMs(2, Random(42))
        assertTrue(jittered in 360L..440L)

        // Validation bounds
        assertFailsWith<IllegalArgumentException> {
            ExponentialBackoffPolicy(initialDelayMs = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            ExponentialBackoffPolicy(initialDelayMs = 500, maxDelayMs = 100)
        }
        assertFailsWith<IllegalArgumentException> {
            ExponentialBackoffPolicy(maxAttempts = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            ExponentialBackoffPolicy(jitterRatio = 0.6)
        }

        // Test sync retry with eventual success
        var attemptsRan = 0
        val successResult = policy.executeSync { attempt ->
            attemptsRan = attempt
            if (attempt < 3) throw RuntimeException("Simulated Transient Failure $attempt")
            "SUCCESS_ON_$attempt"
        }
        assertTrue(successResult.isSuccess)
        assertEquals("SUCCESS_ON_3", successResult.getOrNull())
        assertEquals(3, attemptsRan)

        // Test sync retry with ultimate failure after max attempts
        val failResult = policy.executeSync { attempt ->
            throw RuntimeException("Persistent Failure $attempt")
        }
        assertTrue(failResult.isFailure)
        assertTrue(failResult.exceptionOrNull()?.message?.contains("Persistent Failure 4") == true)
    }

    @Test
    fun testPocketHostSyncClientAndPayloads() {
        val client = PocketHostSyncClient()
        val result = WorldSeriesSimulator.runWorldSeriesSimulation(iterations = 100, seed = 42L)

        val runJson = client.serializeSimulationRunRecord("RUN-TEST-100", result, 42L)
        assertTrue(runJson.contains("\"str_run_id\": \"RUN-TEST-100\""))
        assertTrue(runJson.contains("\"int_total_iterations\": 100"))
        assertTrue(runJson.contains("\"bool_is_active\": true"))

        val lbJson = client.serializeLeaderboardRecords("RUN-TEST-100", result.leaderboard)
        assertTrue(lbJson.contains("\"str_team_code\": \"LAD\""))
        assertTrue(lbJson.contains("\"int_sim_rank\": 1"))

        val qualJson = client.serializeLatentQualities("RUN-TEST-100", result.leaderboard)
        assertTrue(qualJson.contains("\"str_team_code\": \"LAD\""))
        assertTrue(qualJson.contains("\"dbl_latent_quality_score\""))

        val fullBundle = client.generateFullDatabaseSyncPackage("RUN-TEST-100", result, 42L)
        assertTrue(fullBundle.contains("\"schema_version\": \"1.0.0-hungarian\""))
        assertTrue(fullBundle.contains("\"m_simulation_runs\""))
        assertTrue(fullBundle.contains("\"f_world_series_leaderboard\""))

        // Test simulated sync with successful transporter
        val report = client.syncDatabaseWithRetry("RUN-TEST-100", result, 42L) { col, payload ->
            assertTrue(payload.isNotEmpty())
            true
        }
        assertTrue(report.isSuccessful)
        assertEquals(61, report.totalRecordsSynced) // 1 run + 30 qualities + 30 leaderboard
        assertEquals(3, report.collectionsSynced.size)

        // Test sync with transporter failures to verify fallback and log branches
        var callCount = 0
        val fastPolicy = ExponentialBackoffPolicy(initialDelayMs = 1L, maxDelayMs = 5L, factor = 1.5, maxAttempts = 2)
        val fastClient = PocketHostSyncClient(PocketHostConfig(backoffPolicy = fastPolicy))
        val failingReport = fastClient.syncDatabaseWithRetry("RUN-FAIL-01", result, 42L) { col, _ ->
            callCount++
            false // Simulate failed HTTP network requests
        }
        assertFalse(failingReport.isSuccessful)
        assertEquals(0, failingReport.totalRecordsSynced)
        assertTrue(failingReport.logEntries.any { it.contains("❌") })

        // Test default sync without transporter
        val defaultReport = fastClient.syncDatabaseWithRetry("RUN-DEFAULT-01", result, 42L)
        assertTrue(defaultReport.isSuccessful)
        assertEquals(61, defaultReport.totalRecordsSynced)

        // Test suspend retry execution
        kotlinx.coroutines.runBlocking {
            var sCount = 0
            val sRes = fastPolicy.executeSuspend(
                onRetry = { att, err, delay ->
                    sCount++
                }
            ) { attempt ->
                if (attempt == 1) throw RuntimeException("Suspend retry test")
                "SUSPEND_OK"
            }
            assertTrue(sRes.isSuccess)
            assertEquals("SUSPEND_OK", sRes.getOrNull())
            assertEquals(1, sCount)

            // Failing suspend retry
            val sFail = fastPolicy.executeSuspend { attempt ->
                throw RuntimeException("Permanent suspend error $attempt")
            }
            assertTrue(sFail.isFailure)
        }
    }

    @Test
    fun testTimeUtilsCalculations() {
        val nowMs = TimeUtils.currentTimeMillisUtc()
        assertTrue(nowMs > 1700000000000L, "Epoch millis must be valid contemporary timestamp")
        assertEquals(nowMs, TimeUtils.nowEpochMs())
        assertEquals(nowMs, TimeUtils.currentTimeMillis())

        val isoNow = TimeUtils.currentIsoTimestampUtc()
        assertTrue(isoNow.endsWith("Z"))
        assertTrue(isoNow.contains("T"))

        val currentYear = TimeUtils.currentSeasonYear()
        assertTrue(currentYear >= 2024)
        assertEquals(currentYear, TimeUtils.getSeasonYear(nowMs))

        // Epoch 0 test (1970-01-01T00:00:00.000Z)
        assertEquals("1970-01-01T00:00:00.000Z", TimeUtils.formatIsoTimestampUtc(0L))
        assertEquals("1970-01-01", TimeUtils.formatDateUtc(0L))
        assertEquals("19700101", TimeUtils.formatCompactDateUtc(0L))
        assertEquals("19700101-000000", TimeUtils.formatCompactDateTimeUtc(0L))
        assertEquals(1970, TimeUtils.getSeasonYear(0L))

        // Known arbitrary date test
        val customEpochMs = TimeUtils.createEpochMsUtc(2026, 8, 15, 7, 57, 35, 123)
        assertEquals("2026-08-15T07:57:35.123Z", TimeUtils.formatIsoTimestampUtc(customEpochMs))
        assertEquals("2026-08-15", TimeUtils.formatDateUtc(customEpochMs))
        assertEquals("20260815", TimeUtils.formatCompactDateUtc(customEpochMs))
        assertEquals("20260815-075735", TimeUtils.formatCompactDateTimeUtc(customEpochMs))
        assertEquals(2026, TimeUtils.getSeasonYear(customEpochMs))

        // Bidirectional parse test
        val parsedMs = TimeUtils.parseIsoTimestampUtc("2026-08-15T07:57:35.123Z")
        assertEquals(customEpochMs, parsedMs)

        val parsedNoMillis = TimeUtils.parseIsoTimestampUtc("2026-08-15T07:57:35Z")
        assertEquals("2026-08-15T07:57:35.000Z", TimeUtils.formatIsoTimestampUtc(parsedNoMillis))

        val parsedDateOnly = TimeUtils.parseIsoTimestampUtc("2026-08-15")
        assertEquals("2026-08-15T00:00:00.000Z", TimeUtils.formatIsoTimestampUtc(parsedDateOnly))

        // Run ID generation
        val runId = TimeUtils.generateRunId("RUN-MC10K", customEpochMs)
        assertEquals("RUN-MC10K-20260815-075735", runId)

        // Error handling on invalid ISO strings and components
        assertFailsWith<IllegalArgumentException> {
            TimeUtils.parseIsoTimestampUtc("invalid-date")
        }
        assertFailsWith<IllegalArgumentException> {
            TimeUtils.createEpochMsUtc(2026, 13, 1)
        }
        assertFailsWith<IllegalArgumentException> {
            TimeUtils.createEpochMsUtc(2026, 8, 32)
        }
        assertFailsWith<IllegalArgumentException> {
            TimeUtils.createEpochMsUtc(2026, 8, 1, 24, 0, 0)
        }
    }

    @Test
    fun testDynamicHungarianDefaultsAndSerialization() {
        // Records created without specifying epochMs should dynamically default to current time
        val teamRecord = IMlbTeamRecord(
            str_team_code = "BOS",
            str_team_name = "Boston Red Sox",
            str_league = "AL",
            str_division = "EAST"
        )
        assertTrue(teamRecord.int_created_epoch_ms_utc > 1700000000000L)
        assertTrue(teamRecord.int_updated_epoch_ms_utc > 1700000000000L)

        val runRecord = MSimulationRunRecord(
            str_run_id = "RUN-DYNAMIC-01",
            int_total_iterations = 10000,
            int_random_seed = 42,
            str_engine_version = "2.4.0",
            str_top_favorite_code = "LAD",
            dbl_top_favorite_prob = 0.235,
            str_causal_iv_status = "Active"
        )
        assertTrue(runRecord.dt_run_timestamp.endsWith("Z"))
        assertTrue(runRecord.int_season_year >= 2024)
        assertTrue(runRecord.int_created_epoch_ms_utc > 1700000000000L)

        val result = WorldSeriesSimulator.runWorldSeriesSimulation(iterations = 100, seed = 42L)
        val client = PocketHostSyncClient()

        // Test dynamic serialization default
        val runJsonDynamic = client.serializeSimulationRunRecord("RUN-DYN-01", result, 42L)
        assertTrue(runJsonDynamic.contains("\"dt_run_timestamp\""))
        assertTrue(runJsonDynamic.contains("\"int_created_epoch_ms_utc\""))

        // Test explicit epoch override
        val customEpoch = TimeUtils.createEpochMsUtc(2026, 8, 14, 12, 0, 0, 0)
        val runJsonOverride = client.serializeSimulationRunRecord("RUN-OVR-01", result, 42L, customEpoch)
        assertTrue(runJsonOverride.contains("\"int_created_epoch_ms_utc\": $customEpoch"))
        assertTrue(runJsonOverride.contains("\"dt_run_timestamp\": \"2026-08-14T12:00:00.000Z\""))

        // Test PocketHostDataTracker with dynamic timestamp vs explicit
        val dataTrackerJsonDynamic = PocketHostDataTracker.buildSimulationRunJsonPayload("RUN-DYN-02", result, 42L)
        assertTrue(dataTrackerJsonDynamic.contains("\"dt_run_timestamp\""))

        val dataTrackerJsonOverride = PocketHostDataTracker.buildSimulationRunJsonPayload("RUN-DYN-02", result, 42L, customEpoch)
        assertTrue(dataTrackerJsonOverride.contains("\"dt_run_timestamp\": \"2026-08-14T12:00:00.000Z\""))
    }
}
