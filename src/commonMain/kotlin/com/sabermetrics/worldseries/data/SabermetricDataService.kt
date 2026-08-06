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
            MlbTeam(MlbTeamId.NYY, 66, 48, 582.0, 478.0, 32.5, 0.332, 116.0, 3.74, 3.82, 2.8, 3.25, 1.8, 1.05),
            MlbTeam(MlbTeamId.BAL, 55, 58, 542.0, 558.0, 24.2, 0.312, 101.0, 4.18, 4.22, -0.4, 4.15, 0.5, 1.00),
            MlbTeam(MlbTeamId.BOS, 59, 53, 550.0, 525.0, 26.8, 0.318, 104.0, 4.08, 4.15, 0.5, 3.95, 0.8, 1.00),
            MlbTeam(MlbTeamId.TBD, 56, 56, 475.0, 490.0, 24.5, 0.298, 95.0, 3.82, 3.85, 1.2, 3.55, 1.0, 1.00),
            MlbTeam(MlbTeamId.TOR, 52, 62, 482.0, 545.0, 19.8, 0.305, 98.0, 4.28, 4.30, -0.8, 4.10, 0.4, 0.98),
            
            // --- AL CENTRAL ---
            MlbTeam(MlbTeamId.CLE, 67, 46, 525.0, 442.0, 31.0, 0.308, 98.0, 3.65, 3.75, 4.2, 3.28, 1.2, 1.03),
            MlbTeam(MlbTeamId.KC,  64, 52, 568.0, 482.0, 35.6, 0.322, 110.0, 3.65, 3.75, 2.5, 3.35, 2.0, 1.04),
            MlbTeam(MlbTeamId.MIN, 63, 50, 548.0, 502.0, 28.5, 0.320, 106.0, 3.98, 4.05, 0.8, 3.80, 0.6, 1.00),
            MlbTeam(MlbTeamId.DET, 55, 59, 472.0, 488.0, 25.2, 0.302, 94.0, 3.75, 3.80, 1.8, 2.85, 1.2, 1.02),
            MlbTeam(MlbTeamId.CWS, 28, 86, 352.0, 622.0, 5.2, 0.279, 76.0, 4.88, 4.95, -4.5, 5.10, -0.8, 0.85),

            // --- AL WEST ---
            MlbTeam(MlbTeamId.HOU, 62, 52, 535.0, 468.0, 29.5, 0.322, 108.0, 3.78, 3.85, 1.8, 3.35, 1.4, 1.04),
            MlbTeam(MlbTeamId.SEA, 60, 54, 476.0, 438.0, 28.0, 0.302, 97.0, 3.52, 3.60, 2.2, 2.95, 0.8, 1.02),
            MlbTeam(MlbTeamId.TEX, 54, 60, 478.0, 502.0, 21.5, 0.306, 99.0, 4.15, 4.22, -0.5, 3.90, 0.3, 0.98),
            MlbTeam(MlbTeamId.OAK, 48, 67, 452.0, 538.0, 15.4, 0.300, 94.0, 4.42, 4.48, -1.2, 4.40, 0.2, 0.95),
            MlbTeam(MlbTeamId.LAA, 44, 69, 445.0, 558.0, 12.8, 0.296, 91.0, 4.58, 4.62, -2.0, 4.65, -0.2, 0.92),

            // --- NL EAST ---
            MlbTeam(MlbTeamId.PHI, 67, 46, 548.0, 462.0, 33.8, 0.328, 112.0, 3.68, 3.74, 2.2, 3.15, 1.8, 1.05),
            MlbTeam(MlbTeamId.ATL, 60, 52, 488.0, 435.0, 29.1, 0.315, 102.0, 3.62, 3.68, 2.8, 3.10, 1.2, 1.04),
            MlbTeam(MlbTeamId.NYM, 61, 53, 542.0, 488.0, 29.8, 0.324, 109.0, 3.92, 4.00, 1.2, 3.50, 1.4, 1.03),
            MlbTeam(MlbTeamId.WSH, 51, 63, 458.0, 538.0, 16.1, 0.303, 93.0, 4.42, 4.48, -1.0, 4.35, 0.2, 0.95),
            MlbTeam(MlbTeamId.MIA, 43, 71, 432.0, 562.0, 11.5, 0.292, 88.0, 4.68, 4.72, -2.5, 4.75, -0.4, 0.90),

            // --- NL CENTRAL ---
            MlbTeam(MlbTeamId.MIL, 65, 49, 542.0, 452.0, 31.0, 0.321, 105.0, 3.68, 3.74, 3.2, 3.40, 1.5, 1.04),
            MlbTeam(MlbTeamId.CHC, 66, 48, 562.0, 478.0, 33.5, 0.325, 108.0, 3.72, 3.78, 2.8, 3.20, 1.8, 1.15),
            MlbTeam(MlbTeamId.STL, 58, 56, 468.0, 502.0, 22.5, 0.309, 98.0, 4.12, 4.18, 0.3, 3.98, 0.4, 1.00),
            MlbTeam(MlbTeamId.CIN, 54, 60, 488.0, 492.0, 20.0, 0.307, 95.0, 4.08, 4.12, -0.2, 3.85, 0.5, 0.95),
            MlbTeam(MlbTeamId.PIT, 53, 60, 462.0, 512.0, 19.2, 0.301, 92.0, 4.15, 4.20, 0.1, 3.50, 0.6, 0.95),

            // --- NL WEST ---
            MlbTeam(MlbTeamId.LAD, 68, 47, 592.0, 482.0, 37.0, 0.337, 119.0, 3.76, 3.80, 2.8, 3.05, 2.5, 1.20),
            MlbTeam(MlbTeamId.SD,  66, 49, 532.0, 468.0, 32.1, 0.326, 110.0, 3.72, 3.80, 2.4, 3.30, 1.8, 1.15),
            MlbTeam(MlbTeamId.ARI, 61, 53, 612.0, 548.0, 29.5, 0.332, 114.0, 4.22, 4.15, 0.6, 3.80, 1.1, 1.08),
            MlbTeam(MlbTeamId.SF,  56, 58, 482.0, 495.0, 22.8, 0.308, 99.0, 3.95, 4.02, 0.1, 3.68, 0.5, 1.00),
            MlbTeam(MlbTeamId.COL, 42, 73, 475.0, 652.0, 8.0, 0.310, 86.0, 5.20, 5.10, -3.5, 5.35, -0.6, 0.75)
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

