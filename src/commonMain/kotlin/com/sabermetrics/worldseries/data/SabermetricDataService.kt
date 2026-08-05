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
            MlbTeam(MlbTeamId.NYY, 94, 68, 815.0, 668.0, 48.5, 0.334, 117.0, 3.74, 3.82, 3.8, 3.25, 2.5, 1.05),
            MlbTeam(MlbTeamId.BAL, 91, 71, 786.0, 695.0, 44.2, 0.325, 111.0, 3.92, 3.98, 2.9, 3.45, 1.8, 1.04),
            MlbTeam(MlbTeamId.BOS, 81, 81, 752.0, 740.0, 32.1, 0.318, 104.0, 4.15, 4.20, 0.5, 4.10, 0.8, 1.00),
            MlbTeam(MlbTeamId.TBD, 80, 82, 630.0, 672.0, 33.4, 0.298, 95.0, 3.78, 3.85, 1.8, 3.60, 1.2, 1.00),
            MlbTeam(MlbTeamId.TOR, 74, 88, 671.0, 756.0, 26.8, 0.310, 100.0, 4.38, 4.32, -1.2, 4.25, 0.5, 0.98),
            
            // --- AL CENTRAL ---
            MlbTeam(MlbTeamId.CLE, 92, 69, 708.0, 621.0, 41.0, 0.308, 98.0, 3.65, 3.75, 4.8, 3.30, 1.5, 1.03),
            MlbTeam(MlbTeamId.KC,  86, 76, 735.0, 642.0, 38.6, 0.312, 102.0, 3.85, 3.90, 1.2, 3.40, 2.0, 1.02),
            MlbTeam(MlbTeamId.DET, 86, 76, 682.0, 642.0, 37.2, 0.305, 96.0, 3.72, 3.80, 2.4, 2.85, 1.4, 1.02),
            MlbTeam(MlbTeamId.MIN, 82, 80, 748.0, 720.0, 34.5, 0.320, 106.0, 4.08, 4.12, 0.8, 3.90, 0.6, 1.00),
            MlbTeam(MlbTeamId.CWS, 41, 121, 507.0, 896.0, 8.2, 0.279, 76.0, 4.88, 4.95, -5.5, 5.10, -1.0, 0.85),

            // --- AL WEST ---
            MlbTeam(MlbTeamId.HOU, 88, 73, 740.0, 654.0, 40.5, 0.322, 108.0, 3.80, 3.88, 2.1, 3.35, 1.6, 1.04),
            MlbTeam(MlbTeamId.SEA, 85, 77, 676.0, 623.0, 39.0, 0.302, 97.0, 3.52, 3.60, 2.8, 2.95, 1.0, 1.02),
            MlbTeam(MlbTeamId.TEX, 78, 84, 683.0, 714.0, 29.5, 0.306, 99.0, 4.22, 4.28, -0.8, 3.95, 0.4, 0.98),
            MlbTeam(MlbTeamId.OAK, 69, 93, 642.0, 758.0, 20.4, 0.300, 94.0, 4.45, 4.50, -1.8, 4.50, 0.2, 0.95),
            MlbTeam(MlbTeamId.LAA, 63, 99, 635.0, 797.0, 16.8, 0.296, 91.0, 4.62, 4.68, -2.9, 4.75, -0.2, 0.92),

            // --- NL EAST ---
            MlbTeam(MlbTeamId.PHI, 95, 67, 784.0, 671.0, 46.8, 0.328, 112.0, 3.70, 3.76, 2.9, 3.15, 2.1, 1.05),
            MlbTeam(MlbTeamId.ATL, 89, 73, 704.0, 635.0, 42.1, 0.315, 102.0, 3.62, 3.68, 3.2, 3.10, 1.5, 1.04),
            MlbTeam(MlbTeamId.NYM, 89, 73, 768.0, 697.0, 41.8, 0.324, 109.0, 3.95, 4.02, 1.6, 3.50, 1.7, 1.03),
            MlbTeam(MlbTeamId.WSH, 71, 91, 660.0, 777.0, 22.1, 0.303, 93.0, 4.48, 4.52, -1.5, 4.40, 0.3, 0.95),
            MlbTeam(MlbTeamId.MIA, 62, 100, 626.0, 818.0, 15.5, 0.292, 88.0, 4.70, 4.75, -3.2, 4.85, -0.5, 0.90),

            // --- NL CENTRAL ---
            MlbTeam(MlbTeamId.MIL, 93, 69, 775.0, 648.0, 44.0, 0.321, 105.0, 3.68, 3.74, 4.2, 3.40, 1.9, 1.04),
            MlbTeam(MlbTeamId.CHC, 83, 79, 736.0, 678.0, 36.2, 0.316, 101.0, 3.90, 3.95, 1.1, 3.65, 1.1, 1.01),
            MlbTeam(MlbTeamId.STL, 83, 79, 672.0, 727.0, 32.5, 0.309, 98.0, 4.12, 4.18, 0.4, 4.00, 0.5, 1.00),
            MlbTeam(MlbTeamId.CIN, 77, 85, 702.0, 706.0, 28.0, 0.307, 95.0, 4.10, 4.15, -0.2, 3.90, 0.6, 0.95),
            MlbTeam(MlbTeamId.PIT, 76, 86, 665.0, 742.0, 27.2, 0.301, 92.0, 4.18, 4.22, 0.1, 3.55, 0.8, 0.95),

            // --- NL WEST ---
            MlbTeam(MlbTeamId.LAD, 98, 64, 842.0, 686.0, 52.0, 0.337, 119.0, 3.76, 3.80, 3.5, 3.05, 3.0, 1.30),
            MlbTeam(MlbTeamId.SD,  93, 69, 760.0, 669.0, 45.1, 0.326, 110.0, 3.75, 3.82, 3.1, 3.30, 2.2, 1.20),
            MlbTeam(MlbTeamId.ARI, 89, 73, 886.0, 788.0, 41.5, 0.332, 114.0, 4.25, 4.18, 0.8, 3.80, 1.3, 1.10),
            MlbTeam(MlbTeamId.SF,  80, 82, 693.0, 710.0, 31.8, 0.308, 99.0, 4.70, 4.05, 0.2, 3.70, 0.7, 1.00),
            MlbTeam(MlbTeamId.COL, 61, 101, 683.0, 935.0, 12.0, 0.310, 86.0, 5.25, 5.15, -4.5, 5.40, -0.8, 0.75)
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
        sb.append("Team_ID,Team_Name,League,Division,Wins,Losses,Win_Pct,Runs_Scored,Runs_Allowed,Run_Differential,Pythagorean_Win_Pct,Team_WAR,wOBA,wRC_Plus,FIP,xFIP,Bullpen_WPA,Top3_Ace_ERA,Trade_Deadline_WAR,Clubhouse_Hype_Index,Regular_Season_Rank,Sim_Rank,Rank_Movement\n")
        for (t in teams) {
            val tp = probMap?.get(t.teamId)
            val regRank = tp?.regularSeasonRank?.toString() ?: ""
            val simRank = tp?.simRank?.toString() ?: ""
            val movement = tp?.movementSymbol ?: ""
            sb.append("${t.id},\"${t.name}\",${t.league},${t.division},${t.wins},${t.losses},${t.winPct.formatDecimals(3)},${t.runsScored},${t.runsAllowed},${t.runDifferential},${t.pythagoreanWinPct.formatDecimals(3)},${t.teamWar},${t.wOBA},${t.wRCPlus},${t.fip},${t.xFip},${t.bullpenWpa},${t.top3AceEra},${t.tradeDeadlineWarAdded},${t.clubhouseHypeIndex},$regRank,$simRank,$movement\n")
        }
        return sb.toString()
    }
}

