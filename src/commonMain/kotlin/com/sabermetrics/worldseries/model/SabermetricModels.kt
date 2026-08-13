package com.sabermetrics.worldseries.model

import kotlin.math.pow

enum class League { AL, NL }
enum class Division { EAST, CENTRAL, WEST }

/**
 * Strongly-typed enumeration of all 30 Major League Baseball teams.
 * Provides central, immutable mapping for team codes (IDs), full names, league, and division.
 */
enum class MlbTeamId(
    val code: String,
    val fullName: String,
    val league: League,
    val division: Division
) {
    // --- AL EAST ---
    NYY("NYY", "New York Yankees", League.AL, Division.EAST),
    BAL("BAL", "Baltimore Orioles", League.AL, Division.EAST),
    BOS("BOS", "Boston Red Sox", League.AL, Division.EAST),
    TBD("TBD", "Tampa Bay Rays", League.AL, Division.EAST),
    TOR("TOR", "Toronto Blue Jays", League.AL, Division.EAST),

    // --- AL CENTRAL ---
    CLE("CLE", "Cleveland Guardians", League.AL, Division.CENTRAL),
    KC("KC",  "Kansas City Royals", League.AL, Division.CENTRAL),
    DET("DET", "Detroit Tigers", League.AL, Division.CENTRAL),
    MIN("MIN", "Minnesota Twins", League.AL, Division.CENTRAL),
    CWS("CWS", "Chicago White Sox", League.AL, Division.CENTRAL),

    // --- AL WEST ---
    HOU("HOU", "Houston Astros", League.AL, Division.WEST),
    SEA("SEA", "Seattle Mariners", League.AL, Division.WEST),
    TEX("TEX", "Texas Rangers", League.AL, Division.WEST),
    OAK("OAK", "Oakland Athletics", League.AL, Division.WEST),
    LAA("LAA", "Los Angeles Angels", League.AL, Division.WEST),

    // --- NL EAST ---
    PHI("PHI", "Philadelphia Phillies", League.NL, Division.EAST),
    ATL("ATL", "Atlanta Braves", League.NL, Division.EAST),
    NYM("NYM", "New York Mets", League.NL, Division.EAST),
    WSH("WSH", "Washington Nationals", League.NL, Division.EAST),
    MIA("MIA", "Miami Marlins", League.NL, Division.EAST),

    // --- NL CENTRAL ---
    MIL("MIL", "Milwaukee Brewers", League.NL, Division.CENTRAL),
    CHC("CHC", "Chicago Cubs", League.NL, Division.CENTRAL),
    STL("STL", "St. Louis Cardinals", League.NL, Division.CENTRAL),
    CIN("CIN", "Cincinnati Reds", League.NL, Division.CENTRAL),
    PIT("PIT", "Pittsburgh Pirates", League.NL, Division.CENTRAL),

    // --- NL WEST ---
    LAD("LAD", "Los Angeles Dodgers", League.NL, Division.WEST),
    SD("SD",  "San Diego Padres", League.NL, Division.WEST),
    ARI("ARI", "Arizona Diamondbacks", League.NL, Division.WEST),
    SF("SF",  "San Francisco Giants", League.NL, Division.WEST),
    COL("COL", "Colorado Rockies", League.NL, Division.WEST);

    companion object {
        private val codeMap: Map<String, MlbTeamId> by lazy { entries.associateBy { it.code } }
        private val nameMap: Map<String, MlbTeamId> by lazy { entries.associateBy { it.fullName.lowercase() } }

        /**
         * Safely parse team by 2 or 3-letter abbreviation code (case-insensitive).
         */
        fun parseCode(code: String): MlbTeamId? = codeMap[code.trim().uppercase()]

        /**
         * Strict parse team by code, throwing IllegalArgumentException if invalid.
         */
        fun fromCode(code: String): MlbTeamId =
            parseCode(code) ?: throw IllegalArgumentException("Invalid or unknown MLB team code: '$code'")

        /**
         * Safely parse team by full name (case-insensitive).
         */
        fun parseName(name: String): MlbTeamId? = nameMap[name.trim().lowercase()]

        /**
         * Retrieve all teams belonging to a specific League.
         */
        fun byLeague(league: League): List<MlbTeamId> = entries.filter { it.league == league }

        /**
         * Retrieve all teams belonging to a specific League and Division.
         */
        fun byLeagueAndDivision(league: League, division: Division): List<MlbTeamId> =
            entries.filter { it.league == league && it.division == division }
    }
}

/**
 * Clean Open-Source Sabermetric Team Record & Advanced Analytical Metrics.
 */
data class MlbTeam(
    val teamId: MlbTeamId,
    val wins: Int,
    val losses: Int,
    val runsScored: Double,
    val runsAllowed: Double,
    val teamWar: Double,
    val wOBA: Double,
    val wRCPlus: Double,
    val fip: Double,
    val xFip: Double,
    val bullpenWpa: Double,
    val top3AceEra: Double,
    val tradeDeadlineWarAdded: Double = 0.0,
    val clubhouseHypeIndex: Double = 1.0, // Team Chemistry / Hype Index (0.5 to 2.0)
    val last10Wins: Int = 5,
    val last10Losses: Int = 5,
    val seasonConsistencyScore: Double = 1.0 // Season Consistency Metric (0.85 to 1.15)
) {
    val id: String get() = teamId.code
    val name: String get() = teamId.fullName
    val league: League get() = teamId.league
    val division: Division get() = teamId.division

    /**
     * Backward-compatible secondary constructor using string ID and parameters.
     */
    constructor(
        id: String,
        name: String,
        league: League,
        division: Division,
        wins: Int,
        losses: Int,
        runsScored: Double,
        runsAllowed: Double,
        teamWar: Double,
        wOBA: Double,
        wRCPlus: Double,
        fip: Double,
        xFip: Double,
        bullpenWpa: Double,
        top3AceEra: Double,
        tradeDeadlineWarAdded: Double = 0.0,
        clubhouseHypeIndex: Double = 1.0,
        last10Wins: Int = 5,
        last10Losses: Int = 5,
        seasonConsistencyScore: Double = 1.0
    ) : this(
        teamId = MlbTeamId.fromCode(id),
        wins = wins,
        losses = losses,
        runsScored = runsScored,
        runsAllowed = runsAllowed,
        teamWar = teamWar,
        wOBA = wOBA,
        wRCPlus = wRCPlus,
        fip = fip,
        xFip = xFip,
        bullpenWpa = bullpenWpa,
        top3AceEra = top3AceEra,
        tradeDeadlineWarAdded = tradeDeadlineWarAdded,
        clubhouseHypeIndex = clubhouseHypeIndex,
        last10Wins = last10Wins,
        last10Losses = last10Losses,
        seasonConsistencyScore = seasonConsistencyScore
    )

    val gamesPlayed: Int get() = wins + losses
    val winPct: Double get() = if (gamesPlayed > 0) wins.toDouble() / gamesPlayed else 0.500
    val runDifferential: Double get() = runsScored - runsAllowed

    val last10WinPct: Double get() = if (last10Wins + last10Losses > 0) last10Wins.toDouble() / (last10Wins + last10Losses) else 0.500

    /**
     * Bayesian-Adjusted Win Expectancy.
     * Shrinks Pythagorean run-differential expectation towards actual season win percentage by factor 0.35.
     * Prevents extreme luck residuals (blown 1-run games or hit clustering noise) from over-distorting quality.
     */
    val bayesianAdjustedWinPct: Double get() {
        val luckResidual = pythagoreanWinPct - winPct
        return winPct + (0.65 * luckResidual)
    }

    /**
     * Exponentially Recency-Weighted Win Expectancy with Bayesian backoff smoothing.
     * Combines Bayesian-adjusted expectation (45%), full-season win % (35%), and recent hot/cold form (20%).
     */
    val recencyWeightedWinPct: Double get() {
        return 0.45 * bayesianAdjustedWinPct + 0.35 * winPct + 0.20 * last10WinPct
    }

    /**
     * Bounded Season Consistency Index (0.85 to 1.15).
     */
    val seasonConsistencyIndex: Double get() = seasonConsistencyScore.coerceIn(0.85, 1.15)

    /**
     * Bill James Pythagorean Win Expectancy with Pythagenpat exponent (1.83).
     */
    val pythagoreanWinPct: Double get() {
        val rExp = runsScored.pow(1.83)
        val raExp = runsAllowed.pow(1.83)
        return if (rExp + raExp > 0) rExp / (rExp + raExp) else 0.500
    }

    val pythagoreanWinsExpected: Double get() = pythagoreanWinPct * 162.0

    /**
     * BaseRuns (BSR) Expected Runs Scored Approximation.
     * BSR = A * B / (B + C) + D
     */
    val baseRunsEstimate: Double get() {
        val A = runsScored * 0.85 // On-base factor
        val B = runsScored * 0.55 // Score factor
        val C = runsScored * 0.40 // Out factor
        val D = runsScored * 0.05 // Home runs
        return (A * B / (B + C + 1e-5)) + D
    }
}

/**
 * Probability summary for each MLB team's postseason and World Series outcomes,
 * including rank tracking and standings movement.
 */
data class TeamProbability(
    val team: MlbTeam,
    val playoffProb: Double,        // Probability of reaching postseason
    val pennantProb: Double,        // Probability of winning AL/NL Pennant
    val worldSeriesWinProb: Double,   // Probability of winning World Series
    val expectedSeasonWins: Double,
    val latentQualityScore: Double,
    val regularSeasonRank: Int = 0, // Baseline W-L rank (1..30)
    val simRank: Int = 0,           // Causal World Series simulation rank (1..30)
    val rankDelta: Int = 0          // Rank movement delta (regularSeasonRank - simRank)
) {
    val movementSymbol: String get() = when {
        rankDelta > 0 -> "▲ +$rankDelta"
        rankDelta < 0 -> "▼ $rankDelta"
        else -> "—"
    }
}

/**
 * Complete Output of 10,000-Iteration Sabermetric Monte Carlo Simulation.
 */
data class WorldSeriesSimulationResult(
    val totalSimulations: Int,
    val leaderboard: List<TeamProbability>,
    val causalDiagnostics: Map<String, String>,
    val generatedCsvExport: String
)

