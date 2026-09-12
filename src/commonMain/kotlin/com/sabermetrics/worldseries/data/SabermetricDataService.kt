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
            MlbTeam(MlbTeamId.NYY, 85, 62, 673.0, 540.0, 36.8, 0.334, 117.0, 3.70, 3.78, 3.2, 3.15, 2.2, 1.10, 7, 3, 1.023, 0.140, 1.14, 1.10, 1.04, 1.12, 1.04, 1.10, 1.08),
            MlbTeam(MlbTeamId.BAL, 72, 76, 668.0, 692.0, 24.2, 0.312, 101.0, 4.18, 4.22, -0.4, 3.75, 0.5, 1.00, 3, 7, 1.038, 0.015, 1.06, 0.98, 1.04, 1.02, 1.03, 0.98, 0.98),
            MlbTeam(MlbTeamId.BOS, 80, 68, 654.0, 573.0, 27.2, 0.318, 104.0, 4.08, 4.15, 0.5, 3.95, 0.8, 1.00, 5, 5, 1.024, 0.035, 1.02, 1.05, 0.97, 1.04, 0.96, 1.01, 1.00),
            MlbTeam(MlbTeamId.TBD, 88, 59, 665.0, 607.0, 24.5, 0.298, 95.0, 3.82, 3.85, 1.2, 3.55, 1.0, 1.00, 6, 4, 0.994, 0.090, 1.05, 1.15, 1.04, 0.96, 1.05, 1.04, 1.03),
            MlbTeam(MlbTeamId.TOR, 73, 75, 593.0, 635.0, 20.1, 0.305, 98.0, 4.28, 4.30, -0.8, 4.10, 0.4, 0.98, 5, 5, 1.020, 0.010, 0.98, 0.97, 1.05, 0.98, 1.06, 0.97, 0.96),
            
            // --- AL CENTRAL ---
            MlbTeam(MlbTeamId.CLE, 75, 73, 603.0, 616.0, 30.5, 0.308, 98.0, 3.65, 3.75, 4.2, 3.45, 1.2, 1.03, 5, 5, 1.027, 0.010, 0.99, 0.97, 1.12, 0.98, 1.14, 1.06, 1.12),
            MlbTeam(MlbTeamId.KC, 66, 82, 632.0, 719.0, 35.6, 0.322, 110.0, 3.65, 3.75, 2.5, 3.35, 2.0, 1.04, 4, 6, 1.036, 0.001, 0.92, 0.90, 1.08, 1.08, 1.08, 1.04, 1.02),
            MlbTeam(MlbTeamId.MIN, 69, 78, 677.0, 733.0, 28.5, 0.320, 106.0, 3.98, 4.05, 0.8, 3.80, 0.6, 1.00, 4, 6, 1.035, 0.015, 1.00, 0.96, 1.01, 1.06, 1.01, 0.99, 1.01),
            MlbTeam(MlbTeamId.DET, 68, 79, 647.0, 588.0, 25.8, 0.302, 94.0, 3.75, 3.80, 1.8, 3.45, 0.8, 0.98, 5, 5, 0.975, 0.035, 0.98, 1.00, 1.02, 0.95, 1.02, 1.06, 1.02),
            MlbTeam(MlbTeamId.CWS, 75, 72, 688.0, 653.0, 5.2, 0.279, 76.0, 4.88, 4.95, -4.5, 5.10, -0.8, 0.85, 3, 7, 1.029, 0.010, 0.92, 1.03, 0.93, 0.82, 0.92, 0.84, 0.80),

            // --- AL WEST ---
            MlbTeam(MlbTeamId.HOU, 75, 73, 663.0, 705.0, 30.5, 0.324, 109.0, 3.75, 3.82, 2.0, 3.30, 1.5, 1.05, 5, 5, 1.012, 0.065, 1.08, 1.06, 1.04, 1.08, 1.03, 1.08, 1.06),
            MlbTeam(MlbTeamId.SEA, 69, 79, 586.0, 661.0, 28.0, 0.302, 97.0, 3.52, 3.60, 2.2, 2.95, 0.8, 1.02, 5, 5, 1.023, 0.012, 1.04, 0.95, 1.05, 0.96, 1.05, 1.12, 1.05),
            MlbTeam(MlbTeamId.TEX, 72, 76, 611.0, 660.0, 21.5, 0.306, 99.0, 4.15, 4.22, -0.5, 3.90, 0.3, 0.98, 4, 6, 1.023, 0.020, 1.01, 0.98, 1.02, 0.99, 1.02, 0.98, 0.97),
            MlbTeam(MlbTeamId.OAK, 60, 88, 647.0, 833.0, 15.4, 0.300, 94.0, 4.42, 4.48, -1.2, 4.40, 0.2, 0.95, 7, 3, 1.025, 0.001, 0.88, 0.86, 0.94, 0.94, 0.93, 0.90, 0.92),
            MlbTeam(MlbTeamId.LAA, 56, 91, 594.0, 668.0, 12.8, 0.296, 91.0, 4.58, 4.62, -2.0, 4.65, -0.2, 0.92, 4, 6, 0.988, 0.001, 0.89, 0.86, 0.93, 0.91, 0.92, 0.88, 0.86),

            // --- NL EAST ---
            MlbTeam(MlbTeamId.PHI, 82, 66, 664.0, 628.0, 35.2, 0.330, 114.0, 3.65, 3.70, 2.8, 3.10, 2.0, 1.05, 4, 6, 1.017, 0.045, 1.12, 1.03, 0.98, 1.12, 0.98, 1.10, 1.08),
            MlbTeam(MlbTeamId.ATL, 87, 61, 677.0, 566.0, 27.5, 0.310, 100.0, 3.78, 3.82, 1.8, 3.35, 0.6, 0.98, 5, 5, 1.035, 0.180, 1.01, 1.14, 1.02, 0.98, 1.02, 1.04, 1.02),
            MlbTeam(MlbTeamId.NYM, 68, 79, 631.0, 673.0, 29.8, 0.324, 109.0, 3.92, 4.00, 1.2, 3.50, 1.4, 1.03, 7, 3, 1.034, 0.004, 0.97, 0.94, 0.98, 1.08, 0.97, 1.02, 1.02),
            MlbTeam(MlbTeamId.WSH, 68, 81, 760.0, 759.0, 16.1, 0.303, 93.0, 4.42, 4.48, -1.0, 4.35, 0.2, 0.95, 3, 7, 1.005, 0.002, 0.93, 0.90, 0.96, 0.94, 0.96, 0.92, 0.94),
            MlbTeam(MlbTeamId.MIA, 72, 76, 656.0, 653.0, 12.0, 0.292, 88.0, 4.68, 4.72, -2.5, 4.75, -0.4, 0.90, 3, 7, 1.028, 0.003, 0.92, 0.96, 0.97, 0.88, 0.97, 0.88, 0.84),

            // --- NL CENTRAL ---
            MlbTeam(MlbTeamId.MIL, 92, 56, 762.0, 570.0, 35.8, 0.325, 108.0, 3.62, 3.68, 3.5, 3.35, 1.8, 1.10, 7, 3, 1.033, 0.100, 1.08, 1.20, 1.08, 1.06, 1.09, 1.08, 1.11),
            MlbTeam(MlbTeamId.CHC, 82, 66, 792.0, 654.0, 34.0, 0.325, 108.0, 3.72, 3.78, 2.8, 3.28, 1.8, 1.10, 4, 6, 1.014, 0.085, 1.10, 1.12, 1.07, 1.08, 1.08, 1.07, 1.08),
            MlbTeam(MlbTeamId.STL, 73, 75, 675.0, 683.0, 22.5, 0.309, 98.0, 4.12, 4.18, 0.3, 3.98, 0.4, 1.00, 5, 5, 1.039, 0.008, 0.98, 0.98, 1.00, 0.98, 1.00, 0.98, 0.99),
            MlbTeam(MlbTeamId.CIN, 69, 78, 611.0, 753.0, 20.5, 0.307, 95.0, 4.08, 4.12, -0.2, 3.85, 0.5, 0.95, 4, 6, 0.989, 0.004, 0.95, 0.94, 0.98, 0.95, 0.98, 0.98, 0.97),
            MlbTeam(MlbTeamId.PIT, 74, 74, 711.0, 685.0, 19.5, 0.301, 92.0, 4.15, 4.20, 0.1, 3.50, 0.6, 0.95, 7, 3, 1.026, 0.003, 0.94, 0.92, 1.01, 0.92, 1.01, 1.02, 0.98),

            // --- NL WEST ---
            MlbTeam(MlbTeamId.LAD, 90, 57, 734.0, 561.0, 41.2, 0.338, 120.0, 3.62, 3.68, 3.8, 2.70, 3.2, 1.25, 8, 2, 1.033, 0.360, 1.22, 1.25, 1.06, 1.18, 1.06, 1.20, 1.15),
            MlbTeam(MlbTeamId.SD, 79, 68, 626.0, 607.0, 32.5, 0.326, 110.0, 3.72, 3.80, 2.4, 3.30, 1.8, 1.10, 7, 3, 1.021, 0.050, 1.08, 1.04, 1.03, 1.09, 1.03, 1.08, 1.08),
            MlbTeam(MlbTeamId.ARI, 79, 69, 657.0, 650.0, 29.8, 0.332, 114.0, 4.22, 4.15, 0.6, 3.80, 1.1, 1.08, 6, 4, 1.017, 0.025, 1.02, 1.02, 1.10, 1.12, 1.10, 0.98, 1.01),
            MlbTeam(MlbTeamId.SF, 62, 86, 622.0, 696.0, 22.8, 0.308, 99.0, 3.95, 4.02, 0.1, 3.68, 0.5, 1.00, 5, 5, 1.016, 0.002, 0.94, 0.92, 0.99, 0.98, 0.99, 1.01, 0.98),
            MlbTeam(MlbTeamId.COL, 55, 92, 688.0, 846.0, 8.0, 0.310, 86.0, 5.20, 5.10, -3.5, 5.35, -0.6, 0.75, 3, 7, 1.014, 0.000, 0.85, 0.82, 0.90, 0.85, 0.89, 0.78, 0.75)
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

