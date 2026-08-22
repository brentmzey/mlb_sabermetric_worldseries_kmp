package com.sabermetrics.worldseries.data

import com.sabermetrics.worldseries.model.MlbTeam
import com.sabermetrics.worldseries.model.MlbTeamId
import com.sabermetrics.worldseries.model.TeamProbability
import com.sabermetrics.worldseries.util.formatDecimals

/**
 * Ingests, normalizes, and cleans open-source sabermetric data for all 30 MLB teams.
 */
object SabermetricDataService {

    fun loadCleanedMlbDataset(): List<MlbTeam> {
        val dataset = listOf(
            // --- AL EAST ---
            MlbTeam(MlbTeamId.NYY, 72, 55, 565.0, 471.0, 36.8, 0.334, 117.0, 3.70, 3.78, 3.2, 3.15, 2.2, 1.10, 6, 4, 1.028, 0.140, 1.14, 1.10, 1.04, 1.12, 1.04, 1.10, 1.08),
            MlbTeam(MlbTeamId.BAL, 61, 67, 574.0, 610.0, 24.2, 0.312, 101.0, 4.18, 4.22, -0.4, 3.75, 0.5, 1.00, 4, 6, 1.037, 0.015, 1.06, 0.98, 1.04, 1.02, 1.03, 0.98, 0.98),
            MlbTeam(MlbTeamId.BOS, 68, 59, 575.0, 480.0, 27.2, 0.318, 104.0, 4.08, 4.15, 0.5, 3.95, 0.8, 1.00, 4, 6, 1.003, 0.035, 1.02, 1.05, 0.97, 1.04, 0.96, 1.01, 1.00),
            MlbTeam(MlbTeamId.TBD, 76, 51, 579.0, 534.0, 24.5, 0.298, 95.0, 3.82, 3.85, 1.2, 3.55, 1.0, 1.00, 5, 5, 0.991, 0.090, 1.05, 1.15, 1.04, 0.96, 1.05, 1.04, 1.03),
            MlbTeam(MlbTeamId.TOR, 63, 66, 513.0, 562.0, 20.1, 0.305, 98.0, 4.28, 4.30, -0.8, 4.10, 0.4, 0.98, 7, 3, 1.016, 0.010, 0.98, 0.97, 1.05, 0.98, 1.06, 0.97, 0.96),
            
            // --- AL CENTRAL ---
            MlbTeam(MlbTeamId.CLE, 62, 66, 507.0, 530.0, 30.5, 0.308, 98.0, 3.65, 3.75, 4.2, 3.45, 1.2, 1.03, 4, 6, 1.036, 0.010, 0.99, 0.97, 1.12, 0.98, 1.14, 1.06, 1.12),
            MlbTeam(MlbTeamId.KC, 55, 74, 541.0, 641.0, 35.6, 0.322, 110.0, 3.65, 3.75, 2.5, 3.35, 2.0, 1.04, 6, 4, 1.037, 0.001, 0.92, 0.90, 1.08, 1.08, 1.08, 1.04, 1.02),
            MlbTeam(MlbTeamId.MIN, 63, 65, 592.0, 633.0, 28.5, 0.320, 106.0, 3.98, 4.05, 0.8, 3.80, 0.6, 1.00, 5, 5, 1.022, 0.015, 1.00, 0.96, 1.01, 1.06, 1.01, 0.99, 1.01),
            MlbTeam(MlbTeamId.DET, 61, 66, 577.0, 495.0, 25.8, 0.302, 94.0, 3.75, 3.80, 1.8, 3.45, 0.8, 0.98, 4, 6, 0.969, 0.035, 0.98, 1.00, 1.02, 0.95, 1.02, 1.06, 1.02),
            MlbTeam(MlbTeamId.CWS, 66, 61, 605.0, 560.0, 5.2, 0.279, 76.0, 4.88, 4.95, -4.5, 5.10, -0.8, 0.85, 5, 5, 1.028, 0.010, 0.92, 1.03, 0.93, 0.82, 0.92, 0.84, 0.80),

            // --- AL WEST ---
            MlbTeam(MlbTeamId.HOU, 64, 64, 583.0, 629.0, 30.5, 0.324, 109.0, 3.75, 3.82, 2.0, 3.30, 1.5, 1.05, 4, 6, 1.012, 0.065, 1.08, 1.06, 1.04, 1.08, 1.03, 1.08, 1.06),
            MlbTeam(MlbTeamId.SEA, 60, 68, 500.0, 546.0, 28.0, 0.302, 97.0, 3.52, 3.60, 2.2, 2.95, 0.8, 1.02, 4, 6, 1.033, 0.012, 1.04, 0.95, 1.05, 0.96, 1.05, 1.12, 1.05),
            MlbTeam(MlbTeamId.TEX, 63, 65, 514.0, 560.0, 21.5, 0.306, 99.0, 4.15, 4.22, -0.5, 3.90, 0.3, 0.98, 4, 6, 1.015, 0.020, 1.01, 0.98, 1.02, 0.99, 1.02, 0.98, 0.97),
            MlbTeam(MlbTeamId.OAK, 49, 79, 557.0, 742.0, 15.4, 0.300, 94.0, 4.42, 4.48, -1.2, 4.40, 0.2, 0.95, 2, 8, 1.031, 0.001, 0.88, 0.86, 0.94, 0.94, 0.93, 0.90, 0.92),
            MlbTeam(MlbTeamId.LAA, 51, 77, 530.0, 600.0, 12.8, 0.296, 91.0, 4.58, 4.62, -2.0, 4.65, -0.2, 0.92, 6, 4, 1.004, 0.001, 0.89, 0.86, 0.93, 0.91, 0.92, 0.88, 0.86),

            // --- NL EAST ---
            MlbTeam(MlbTeamId.PHI, 70, 58, 574.0, 559.0, 35.2, 0.330, 114.0, 3.65, 3.70, 2.8, 3.10, 2.0, 1.05, 8, 2, 1.012, 0.045, 1.12, 1.03, 0.98, 1.12, 0.98, 1.10, 1.08),
            MlbTeam(MlbTeamId.ATL, 75, 54, 609.0, 498.0, 27.5, 0.310, 100.0, 3.78, 3.82, 1.8, 3.35, 0.6, 0.98, 4, 6, 1.032, 0.180, 1.01, 1.14, 1.02, 0.98, 1.02, 1.04, 1.02),
            MlbTeam(MlbTeamId.NYM, 58, 70, 531.0, 573.0, 29.8, 0.324, 109.0, 3.92, 4.00, 1.2, 3.50, 1.4, 1.03, 7, 3, 1.030, 0.004, 0.97, 0.94, 0.98, 1.08, 0.97, 1.02, 1.02),
            MlbTeam(MlbTeamId.WSH, 61, 68, 676.0, 659.0, 16.1, 0.303, 93.0, 4.42, 4.48, -1.0, 4.35, 0.2, 0.95, 3, 7, 1.009, 0.002, 0.93, 0.90, 0.96, 0.94, 0.96, 0.92, 0.94),
            MlbTeam(MlbTeamId.MIA, 64, 64, 560.0, 546.0, 12.0, 0.292, 88.0, 4.68, 4.72, -2.5, 4.75, -0.4, 0.90, 5, 5, 1.031, 0.003, 0.92, 0.96, 0.97, 0.88, 0.97, 0.88, 0.84),

            // --- NL CENTRAL ---
            MlbTeam(MlbTeamId.MIL, 80, 49, 644.0, 484.0, 35.8, 0.325, 108.0, 3.62, 3.68, 3.5, 3.35, 1.8, 1.10, 6, 4, 1.034, 0.100, 1.08, 1.20, 1.08, 1.06, 1.09, 1.08, 1.11),
            MlbTeam(MlbTeamId.CHC, 74, 54, 656.0, 556.0, 34.0, 0.325, 108.0, 3.72, 3.78, 2.8, 3.28, 1.8, 1.10, 6, 4, 1.038, 0.085, 1.10, 1.12, 1.07, 1.08, 1.08, 1.07, 1.08),
            MlbTeam(MlbTeamId.STL, 66, 63, 572.0, 562.0, 22.5, 0.309, 98.0, 4.12, 4.18, 0.3, 3.98, 0.4, 1.00, 7, 3, 1.037, 0.008, 0.98, 0.98, 1.00, 0.98, 1.00, 0.98, 0.99),
            MlbTeam(MlbTeamId.CIN, 61, 67, 523.0, 612.0, 20.5, 0.307, 95.0, 4.08, 4.12, -0.2, 3.85, 0.5, 0.95, 4, 6, 1.002, 0.004, 0.95, 0.94, 0.98, 0.95, 0.98, 0.98, 0.97),
            MlbTeam(MlbTeamId.PIT, 63, 66, 646.0, 615.0, 19.5, 0.301, 92.0, 4.15, 4.20, 0.1, 3.50, 0.6, 0.95, 5, 5, 1.013, 0.003, 0.94, 0.92, 1.01, 0.92, 1.01, 1.02, 0.98),

            // --- NL WEST ---
            MlbTeam(MlbTeamId.LAD, 77, 51, 640.0, 495.0, 41.2, 0.338, 120.0, 3.62, 3.68, 3.8, 2.70, 3.2, 1.25, 7, 3, 1.029, 0.360, 1.22, 1.25, 1.06, 1.18, 1.06, 1.20, 1.15),
            MlbTeam(MlbTeamId.SD, 68, 60, 543.0, 540.0, 32.5, 0.326, 110.0, 3.72, 3.80, 2.4, 3.30, 1.8, 1.10, 7, 3, 1.017, 0.050, 1.08, 1.04, 1.03, 1.09, 1.03, 1.08, 1.08),
            MlbTeam(MlbTeamId.ARI, 67, 61, 579.0, 575.0, 29.8, 0.332, 114.0, 4.22, 4.15, 0.6, 3.80, 1.1, 1.08, 5, 5, 1.024, 0.025, 1.02, 1.02, 1.10, 1.12, 1.10, 0.98, 1.01),
            MlbTeam(MlbTeamId.SF, 52, 75, 513.0, 589.0, 22.8, 0.308, 99.0, 3.95, 4.02, 0.1, 3.68, 0.5, 1.00, 3, 7, 1.018, 0.002, 0.94, 0.92, 0.99, 0.98, 0.99, 1.01, 0.98),
            MlbTeam(MlbTeamId.COL, 50, 77, 611.0, 733.0, 8.0, 0.310, 86.0, 5.20, 5.10, -3.5, 5.35, -0.6, 0.75, 4, 6, 1.021, 0.000, 0.85, 0.82, 0.90, 0.85, 0.89, 0.78, 0.75)
        )
        check(dataset.size == MlbTeamId.entries.size) {
            "Dataset does not contain all ${MlbTeamId.entries.size} MLB teams!"
        }
        return dataset
    }

    /**
     * Type-safe lookup for an MLB team by strong enum ID.
     */
    fun getTeam(teamId: MlbTeamId): MlbTeam {
        return loadCleanedMlbDataset().first { it.teamId == teamId }
    }

    /**
     * Look up team by code or return null if code is invalid or missing.
     */
    fun findTeamByCode(code: String): MlbTeam? {
        val teamId = MlbTeamId.parseCode(code) ?: return null
        return getTeam(teamId)
    }

    /**
     * Generates a clean, CSV-formatted string of the open-source dataset ready for export,
     * including predictive standings rank movement metrics.
     */
    fun exportCleanCsvDataset(teams: List<MlbTeam>, leaderboard: List<TeamProbability>? = null): String {
        val probMap = leaderboard?.associateBy { it.team.teamId }
        val sb = StringBuilder()
        sb.append("Team_ID,Team_Name,League,Division,Wins,Losses,Win_Pct,Runs_Scored,Runs_Allowed,Run_Differential,Pythagorean_Win_Pct,Last10_Wins,Last10_Losses,Recency_Win_Pct,Season_Consistency_Index,Four_Pillar_Consistency,Team_WAR,wOBA,wRC_Plus,FIP,xFIP,Bullpen_WPA,Top3_Ace_ERA,Defensive_Efficiency,Media_Power_Rank_Index,Market_Futures_Prob,Expert_Consensus_Rating,Trade_Deadline_WAR,Clubhouse_Hype_Index,Regular_Season_Rank,Sim_Rank,Rank_Movement\n")
        for (t in teams) {
            val tp = probMap?.get(t.teamId)
            val regRank = tp?.regularSeasonRank?.toString() ?: ""
            val simRank = tp?.simRank?.toString() ?: ""
            val movement = tp?.movementSymbol ?: ""
            sb.append("${t.id},\"${t.name}\",${t.league},${t.division},${t.wins},${t.losses},${t.winPct.formatDecimals(3)},${t.runsScored},${t.runsAllowed},${t.runDifferential},${t.pythagoreanWinPct.formatDecimals(3)},${t.last10Wins},${t.last10Losses},${t.recencyWeightedWinPct.formatDecimals(3)},${t.seasonConsistencyIndex.formatDecimals(3)},${t.fourPillarConsistencyIndex.formatDecimals(3)},${t.teamWar},${t.wOBA},${t.wRCPlus},${t.fip},${t.xFip},${t.bullpenWpa},${t.top3AceEra},${t.defensiveEfficiencyRating.formatDecimals(2)},${t.mediaPowerRankRating.formatDecimals(2)},${t.marketImpliedWsProb.formatDecimals(3)},${t.expertConsensusRating.formatDecimals(2)},${t.tradeDeadlineWarAdded},${t.clubhouseHypeIndex},$regRank,$simRank,$movement\n")
        }
        return sb.toString()
    }
}

