package com.sabermetrics.worldseries.data

import com.sabermetrics.worldseries.model.Division
import com.sabermetrics.worldseries.model.League
import com.sabermetrics.worldseries.model.MlbTeam

/**
 * Ingests, normalizes, and cleans open-source sabermetric data for all 30 MLB teams.
 */
object SabermetricDataService {

    fun loadCleanedMlbDataset(): List<MlbTeam> {
        return listOf(
            // --- AL EAST ---
            MlbTeam("NYY", "New York Yankees", League.AL, Division.EAST, 94, 68, 815.0, 668.0, 48.5, 0.334, 117.0, 3.74, 3.82, 3.8, 3.25, 2.5, 1.08), // Thumbs Down Hype!
            MlbTeam("BAL", "Baltimore Orioles", League.AL, Division.EAST, 91, 71, 786.0, 695.0, 44.2, 0.325, 111.0, 3.92, 3.98, 2.9, 3.45, 1.8, 1.04),
            MlbTeam("BOS", "Boston Red Sox", League.AL, Division.EAST, 81, 81, 752.0, 740.0, 32.1, 0.318, 104.0, 4.15, 4.20, 0.5, 4.10, 0.8, 1.00),
            MlbTeam("TBD", "Tampa Bay Rays", League.AL, Division.EAST, 80, 82, 630.0, 672.0, 33.4, 0.298, 95.0, 3.78, 3.85, 1.8, 3.60, 1.2, 1.00),
            MlbTeam("TOR", "Toronto Blue Jays", League.AL, Division.EAST, 74, 88, 671.0, 756.0, 26.8, 0.310, 100.0, 4.38, 4.32, -1.2, 4.25, 0.5, 0.98),

            // --- AL CENTRAL ---
            MlbTeam("CLE", "Cleveland Guardians", League.AL, Division.CENTRAL, 92, 69, 708.0, 621.0, 41.0, 0.308, 98.0, 3.65, 3.75, 4.8, 3.30, 1.5, 1.03),
            MlbTeam("KC",  "Kansas City Royals", League.AL, Division.CENTRAL, 86, 76, 735.0, 642.0, 38.6, 0.312, 102.0, 3.85, 3.90, 1.2, 3.40, 2.0, 1.02),
            MlbTeam("DET", "Detroit Tigers", League.AL, Division.CENTRAL, 86, 76, 682.0, 642.0, 37.2, 0.305, 96.0, 3.72, 3.80, 2.4, 2.85, 1.4, 1.02),
            MlbTeam("MIN", "Minnesota Twins", League.AL, Division.CENTRAL, 82, 80, 748.0, 720.0, 34.5, 0.320, 106.0, 4.08, 4.12, 0.8, 3.90, 0.6, 1.00),
            MlbTeam("CWS", "Chicago White Sox", League.AL, Division.CENTRAL, 41, 121, 507.0, 896.0, 8.2, 0.279, 76.0, 4.88, 4.95, -5.5, 5.10, -1.0, 0.85),

            // --- AL WEST ---
            MlbTeam("HOU", "Houston Astros", League.AL, Division.WEST, 88, 73, 740.0, 654.0, 40.5, 0.322, 108.0, 3.80, 3.88, 2.1, 3.35, 1.6, 1.04),
            MlbTeam("SEA", "Seattle Mariners", League.AL, Division.WEST, 85, 77, 676.0, 623.0, 39.0, 0.302, 97.0, 3.52, 3.60, 2.8, 2.95, 1.0, 1.02),
            MlbTeam("TEX", "Texas Rangers", League.AL, Division.WEST, 78, 84, 683.0, 714.0, 29.5, 0.306, 99.0, 4.22, 4.28, -0.8, 3.95, 0.4, 0.98),
            MlbTeam("OAK", "Oakland Athletics", League.AL, Division.WEST, 69, 93, 642.0, 758.0, 20.4, 0.300, 94.0, 4.45, 4.50, -1.8, 4.50, 0.2, 0.95),
            MlbTeam("LAA", "Los Angeles Angels", League.AL, Division.WEST, 63, 99, 635.0, 797.0, 16.8, 0.296, 91.0, 4.62, 4.68, -2.9, 4.75, -0.2, 0.92),

            // --- NL EAST ---
            MlbTeam("PHI", "Philadelphia Phillies", League.NL, Division.EAST, 95, 67, 784.0, 671.0, 46.8, 0.328, 112.0, 3.70, 3.76, 2.9, 3.15, 2.1, 1.05),
            MlbTeam("ATL", "Atlanta Braves", League.NL, Division.EAST, 89, 73, 704.0, 635.0, 42.1, 0.315, 102.0, 3.62, 3.68, 3.2, 3.10, 1.5, 1.04),
            MlbTeam("NYM", "New York Mets", League.NL, Division.EAST, 89, 73, 768.0, 697.0, 41.8, 0.324, 109.0, 3.95, 4.02, 1.6, 3.50, 1.7, 1.03),
            MlbTeam("WSH", "Washington Nationals", League.NL, Division.EAST, 71, 91, 660.0, 777.0, 22.1, 0.303, 93.0, 4.48, 4.52, -1.5, 4.40, 0.3, 0.95),
            MlbTeam("MIA", "Miami Marlins", League.NL, Division.EAST, 62, 100, 626.0, 818.0, 15.5, 0.292, 88.0, 4.70, 4.75, -3.2, 4.85, -0.5, 0.90),

            // --- NL CENTRAL ---
            MlbTeam("MIL", "Milwaukee Brewers", League.NL, Division.CENTRAL, 93, 69, 775.0, 648.0, 44.0, 0.321, 105.0, 3.68, 3.74, 4.2, 3.40, 1.9, 1.04),
            MlbTeam("CHC", "Chicago Cubs", League.NL, Division.CENTRAL, 83, 79, 736.0, 678.0, 36.2, 0.316, 101.0, 3.90, 3.95, 1.1, 3.65, 1.1, 1.01),
            MlbTeam("STL", "St. Louis Cardinals", League.NL, Division.CENTRAL, 83, 79, 672.0, 727.0, 32.5, 0.309, 98.0, 4.12, 4.18, 0.4, 4.00, 0.5, 1.00),
            MlbTeam("CIN", "Cincinnati Reds", League.NL, Division.CENTRAL, 77, 85, 702.0, 706.0, 28.0, 0.307, 95.0, 4.10, 4.15, -0.2, 3.90, 0.6, 0.95),
            MlbTeam("PIT", "Pittsburgh Pirates", League.NL, Division.CENTRAL, 76, 86, 665.0, 742.0, 27.2, 0.301, 92.0, 4.18, 4.22, 0.1, 3.55, 0.8, 0.95),

            // --- NL WEST ---
            MlbTeam("LAD", "Los Angeles Dodgers", League.NL, Division.WEST, 98, 64, 842.0, 686.0, 52.0, 0.337, 119.0, 3.76, 3.80, 3.5, 3.05, 3.0, 1.30),
            MlbTeam("SD",  "San Diego Padres", League.NL, Division.WEST, 93, 69, 760.0, 669.0, 45.1, 0.326, 110.0, 3.75, 3.82, 3.1, 3.30, 2.2, 1.20),
            MlbTeam("ARI", "Arizona Diamondbacks", League.NL, Division.WEST, 89, 73, 886.0, 788.0, 41.5, 0.332, 114.0, 4.25, 4.18, 0.8, 3.80, 1.3, 1.10),
            MlbTeam("SF",  "San Francisco Giants", League.NL, Division.WEST, 80, 82, 693.0, 710.0, 31.8, 0.308, 99.0, 3.98, 4.05, 0.2, 3.70, 0.7, 1.00),
            MlbTeam("COL", "Colorado Rockies", League.NL, Division.WEST, 61, 101, 683.0, 935.0, 12.0, 0.310, 86.0, 5.25, 5.15, -4.5, 5.40, -0.8, 0.75)
        )
    }

    /**
     * Generates a clean, CSV-formatted string of the open-source dataset ready for export.
     */
    fun exportCleanCsvDataset(teams: List<MlbTeam>): String {
        val sb = StringBuilder()
        sb.append("Team_ID,Team_Name,League,Division,Wins,Losses,Win_Pct,Runs_Scored,Runs_Allowed,Run_Differential,Pythagorean_Win_Pct,Team_WAR,wOBA,wRC_Plus,FIP,xFIP,Bullpen_WPA,Top3_Ace_ERA,Trade_Deadline_WAR,ThumbsDown_Hype_Index\n")
        for (t in teams) {
            sb.append("${t.id},\"${t.name}\",${t.league},${t.division},${t.wins},${t.losses},${"%.3f".format(t.winPct)},${t.runsScored},${t.runsAllowed},${t.runDifferential},${"%.3f".format(t.pythagoreanWinPct)},${t.teamWar},${t.wOBA},${t.wRCPlus},${t.fip},${t.xFip},${t.bullpenWpa},${t.top3AceEra},${t.tradeDeadlineWarAdded},${t.thumbsDownHypeIndex}\n")
        }
        return sb.toString()
    }
}
