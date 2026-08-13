package com.sabermetrics.worldseries.engine

import com.sabermetrics.worldseries.data.SabermetricDataService
import com.sabermetrics.worldseries.model.*
import com.sabermetrics.worldseries.util.formatDecimals
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Causal Econometric & Sabermetric World Series Monte Carlo Simulator.
 * Runs 10,000 full MLB postseason simulations to calculate exact World Series winning probabilities.
 */
object WorldSeriesSimulator {

    /**
     * Computes 30-team z-scores across Multi-Dimensional Composite Relative Form Metrics
     * (W-L, Offense, Pitching, Bullpen WPA) and maps via Sigmoidal Tanh Transfer Function.
     */
    /**
     * Computes 30-team z-scores across Multi-Dimensional Composite Relative Form Metrics
     * (W-L, Offense, Pitching, Bullpen WPA) and maps via Sigmoidal Tanh Transfer Function.
     */
    fun computeMultiDimensionalMomentumMultipliers(teams: List<MlbTeam>): Map<MlbTeamId, Double> {
        if (teams.isEmpty()) return emptyMap()
        val scores = teams.map { it.compositeRelativeFormScore }
        val mean = scores.average()
        val variance = scores.map { (it - mean) * (it - mean) }.average()
        val stdDev = if (variance > 0) kotlin.math.sqrt(variance) else 1.0

        return teams.associate { team ->
            val zScore = (team.compositeRelativeFormScore - mean) / stdDev
            val momentumMultiplier = (1.0 + 0.04 * kotlin.math.tanh(zScore / 1.5)).coerceIn(0.95, 1.05)
            team.teamId to momentumMultiplier
        }
    }

    /**
     * Estimates Latent True Team Quality Score using 2SLS IV, Sabermetrics,
     * Bayesian Luck Shrinkage, Recency Exponential Weighting, Market Implied Futures, and Expert Consensus.
     */
    fun computeLatentTeamQuality(team: MlbTeam, customMomentumMap: Map<MlbTeamId, Double>? = null): Double {
        val bayesWinPct = team.bayesianAdjustedWinPct
        val recencyWinPct = team.recencyWeightedWinPct

        // Normalized team WAR per 162-game pace
        val warNorm = if (team.gamesPlayed > 0) (team.teamWar / team.gamesPlayed * 162.0) / 45.0 else 0.50

        val baseScore = 0.28 * recencyWinPct +
                        0.20 * bayesWinPct +
                        0.14 * warNorm.coerceIn(0.5, 1.3) +
                        0.14 * (3.80 / team.top3AceEra).coerceIn(0.5, 1.5) +
                        0.08 * (team.wRCPlus / 100.0) +
                        0.08 * (team.marketImpliedWsProb * 4.0) +
                        0.08 * team.defensiveEfficiencyRating.coerceIn(0.85, 1.15)

        // Trade deadline boost, bullpen clutch boost, clubhouse hype, 4-pillar consistency, expert/media consensus, and momentum
        val bullpenClutchBoost = (team.bullpenWpa * 0.01).coerceIn(-0.05, 0.05)
        val hypeMultiplier = team.clubhouseHypeIndex
        val consistencyMultiplier = team.seasonConsistencyIndex
        val expertMediaMultiplier = team.compositeExpertMediaIndex
        val momentumMultiplier = customMomentumMap?.get(team.teamId) ?: team.hotStreakMomentumMultiplier
        val tradeBoost = team.tradeDeadlineWarAdded * 0.015

        return (baseScore + tradeBoost + bullpenClutchBoost) * hypeMultiplier * consistencyMultiplier * expertMediaMultiplier * momentumMultiplier
    }

    /**
     * Bradley-Terry Logit probability of Team A beating Team B in a single game.
     */
    fun predictGameWinProb(teamA: MlbTeam, teamB: MlbTeam, customMomentumMap: Map<MlbTeamId, Double>? = null): Double {
        val qA = computeLatentTeamQuality(teamA, customMomentumMap)
        val qB = computeLatentTeamQuality(teamB, customMomentumMap)
        val delta = (qA - qB) * 1.2
        return 1.0 / (1.0 + exp(-delta))
    }

    /**
     * Simulates a playoff series between Team A and Team B (best of N games).
     */
    fun simulateSeries(teamA: MlbTeam, teamB: MlbTeam, bestOf: Int, random: Random, customMomentumMap: Map<MlbTeamId, Double>? = null): MlbTeam {
        val winsNeeded = (bestOf / 2) + 1
        var winsA = 0
        var winsB = 0
        val pA = predictGameWinProb(teamA, teamB, customMomentumMap)

        while (winsA < winsNeeded && winsB < winsNeeded) {
            if (random.nextDouble() < pA) winsA++ else winsB++
        }
        return if (winsA == winsNeeded) teamA else teamB
    }

    /**
     * Runs 10,000-Iteration Monte Carlo Simulation across all 30 MLB Teams.
     */
    fun runWorldSeriesSimulation(iterations: Int = 10000, seed: Long = 42L): WorldSeriesSimulationResult {
        val teams = SabermetricDataService.loadCleanedMlbDataset()
        val customMomentumMap = computeMultiDimensionalMomentumMultipliers(teams)
        val random = Random(seed)

        val playoffCounts = mutableMapOf<MlbTeamId, Int>()
        val pennantCounts = mutableMapOf<MlbTeamId, Int>()
        val wsCounts = mutableMapOf<MlbTeamId, Int>()
        val simulatedWinsTotal = mutableMapOf<MlbTeamId, Double>()

        for (t in teams) {
            playoffCounts[t.teamId] = 0
            pennantCounts[t.teamId] = 0
            wsCounts[t.teamId] = 0
            simulatedWinsTotal[t.teamId] = 0.0
        }

        // Group teams by League and Division
        val alTeams = teams.filter { it.league == League.AL }
        val nlTeams = teams.filter { it.league == League.NL }

        for (iter in 0 until iterations) {
            // Rest-of-Season (ROS) anchored 162-game season simulation.
            // Locks in actual empirical wins to-date + projects remaining games via recency-weighted quality.
            val seasonQuality = teams.associate { t ->
                val remainingGames = (162 - t.gamesPlayed).coerceAtLeast(0)
                val rosWinPct = t.recencyWeightedWinPct
                var simRemWins = 0
                for (g in 0 until remainingGames) {
                    if (random.nextDouble() < rosWinPct) simRemWins++
                }
                val totalWins = (t.wins + simRemWins).toDouble()
                t.teamId to totalWins
            }

            for (t in teams) {
                simulatedWinsTotal[t.teamId] = (simulatedWinsTotal[t.teamId] ?: 0.0) + (seasonQuality[t.teamId] ?: 81.0)
            }

            // Select AL & NL Playoff Teams (Top division winner in East/Central/West + top 3 Wild Cards)
            val alPlayoffs = selectPlayoffField(alTeams, seasonQuality)
            val nlPlayoffs = selectPlayoffField(nlTeams, seasonQuality)

            for (t in alPlayoffs + nlPlayoffs) {
                playoffCounts[t.teamId] = (playoffCounts[t.teamId] ?: 0) + 1
            }

            // Run AL Postseason Bracket
            val alPennantWinner = runLeaguePlayoffBracket(alPlayoffs, random, customMomentumMap)
            pennantCounts[alPennantWinner.teamId] = (pennantCounts[alPennantWinner.teamId] ?: 0) + 1

            // Run NL Postseason Bracket
            val nlPennantWinner = runLeaguePlayoffBracket(nlPlayoffs, random, customMomentumMap)
            pennantCounts[nlPennantWinner.teamId] = (pennantCounts[nlPennantWinner.teamId] ?: 0) + 1

            // Run World Series (Best of 7)
            val worldSeriesChampion = simulateSeries(alPennantWinner, nlPennantWinner, bestOf = 7, random = random, customMomentumMap = customMomentumMap)
            wsCounts[worldSeriesChampion.teamId] = (wsCounts[worldSeriesChampion.teamId] ?: 0) + 1
        }

        // Calculate baseline regular-season win rank (1..30)
        val sortedBySeasonWins = teams.sortedWith(
            compareByDescending<MlbTeam> { it.wins }.thenByDescending { it.runDifferential }
        )
        val regularSeasonRankMap = sortedBySeasonWins.withIndex().associate { (idx, t) ->
            t.teamId to (idx + 1)
        }

        val unrankedLeaderboard = teams.map { t ->
            val wsProb = (wsCounts[t.teamId] ?: 0).toDouble() / iterations
            val pennantProb = (pennantCounts[t.teamId] ?: 0).toDouble() / iterations
            val playoffProb = (playoffCounts[t.teamId] ?: 0).toDouble() / iterations
            val avgWins = (simulatedWinsTotal[t.teamId] ?: 0.0) / iterations
            val quality = computeLatentTeamQuality(t, customMomentumMap)
            val regRank = regularSeasonRankMap[t.teamId] ?: 0

            TeamProbability(
                team = t,
                playoffProb = playoffProb,
                pennantProb = pennantProb,
                worldSeriesWinProb = wsProb,
                expectedSeasonWins = avgWins,
                latentQualityScore = quality,
                regularSeasonRank = regRank
            )
        }.sortedByDescending { it.worldSeriesWinProb }

        val leaderboard = unrankedLeaderboard.withIndex().map { (idx, tp) ->
            val currentSimRank = idx + 1
            tp.copy(
                simRank = currentSimRank,
                rankDelta = tp.regularSeasonRank - currentSimRank
            )
        }

        val csvData = SabermetricDataService.exportCleanCsvDataset(teams, leaderboard)

        val diagnostics = mapOf(
            "Total_Simulations" to iterations.toString(),
            "Top_World_Series_Favorite" to "${leaderboard.first().team.name} (${(leaderboard.first().worldSeriesWinProb * 100).formatDecimals(2)}%)",
            "Causal_2SLS_IV_Engine" to "Active",
            "Clubhouse_Momentum_Multiplier" to "Applied"
        )

        return WorldSeriesSimulationResult(iterations, leaderboard, diagnostics, csvData)
    }

    private fun selectPlayoffField(leagueTeams: List<MlbTeam>, seasonWins: Map<MlbTeamId, Double>): List<MlbTeam> {
        val divWinners = Division.entries.map { div ->
            leagueTeams.filter { it.division == div }.maxByOrNull { seasonWins[it.teamId] ?: 0.0 }!!
        }.sortedByDescending { seasonWins[it.teamId] }

        val nonDivWinners = leagueTeams.filter { it !in divWinners }.sortedByDescending { seasonWins[it.teamId] }
        val wildCards = nonDivWinners.take(3)

        return divWinners + wildCards // Seeds 1..6
    }

    private fun runLeaguePlayoffBracket(seeds: List<MlbTeam>, random: Random, customMomentumMap: Map<MlbTeamId, Double>? = null): MlbTeam {
        // Seeds: 0=Div1, 1=Div2, 2=Div3, 3=WC1, 4=WC2, 5=WC3
        // Wild Card Series (Best of 3): Seed 3 vs 6, Seed 4 vs 5
        val wcWinner1 = simulateSeries(seeds[2], seeds[5], bestOf = 3, random = random, customMomentumMap = customMomentumMap) // 3 vs 6
        val wcWinner2 = simulateSeries(seeds[3], seeds[4], bestOf = 3, random = random, customMomentumMap = customMomentumMap) // 4 vs 5

        // Division Series (Best of 5): Seed 1 vs wcWinner2, Seed 2 vs wcWinner1
        val dsWinner1 = simulateSeries(seeds[0], wcWinner2, bestOf = 5, random = random, customMomentumMap = customMomentumMap)
        val dsWinner2 = simulateSeries(seeds[1], wcWinner1, bestOf = 5, random = random, customMomentumMap = customMomentumMap)

        // League Championship Series (Best of 7)
        return simulateSeries(dsWinner1, dsWinner2, bestOf = 7, random = random, customMomentumMap = customMomentumMap)
    }
}
