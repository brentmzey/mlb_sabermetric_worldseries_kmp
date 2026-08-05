package com.sabermetrics.worldseries.data

import com.sabermetrics.worldseries.model.TeamProbability
import com.sabermetrics.worldseries.model.WorldSeriesSimulationResult

/**
 * PocketHost / PocketBase Database Sync Helper for tracking historical simulation runs
 * and team rank movements over time using Hungarian prefix notation schema payloads.
 */
object PocketHostDataTracker {

    /**
     * Prepares PocketHost JSON payload for `tbl_simulation_runs` collection.
     */
    fun buildSimulationRunJsonPayload(
        runId: String,
        result: WorldSeriesSimulationResult,
        seed: Long
    ): String {
        val topFav = result.leaderboard.first()
        val timestamp = "2026-08-05T16:00:00.000Z"
        return """
        {
          "str_run_id": "$runId",
          "dt_run_timestamp": "$timestamp",
          "int_total_iterations": ${result.totalSimulations},
          "int_random_seed": $seed,
          "str_top_favorite_code": "${topFav.team.id}",
          "dbl_top_favorite_prob": ${topFav.worldSeriesWinProb},
          "str_causal_engine_status": "${result.causalDiagnostics["Causal_2SLS_IV_Engine"] ?: "Active"}",
          "str_hype_multiplier_note": "${result.causalDiagnostics["ThumbsDown_Hype_Multiplier"] ?: "Applied"}"
        }
        """.trimIndent()
    }

    /**
     * Prepares PocketHost JSON array payload for `tbl_rank_movements` collection.
     */
    fun buildRankMovementsJsonPayload(
        runId: String,
        leaderboard: List<TeamProbability>
    ): String {
        val rows = leaderboard.joinToString(",\n") { tp ->
            """
            {
              "rel_run_id": "$runId",
              "str_team_code": "${tp.team.id}",
              "int_regular_season_rank": ${tp.regularSeasonRank},
              "int_sim_rank": ${tp.simRank},
              "int_rank_delta": ${tp.rankDelta},
              "str_movement_symbol": "${tp.movementSymbol}",
              "dbl_playoff_prob": ${tp.playoffProb},
              "dbl_pennant_prob": ${tp.pennantProb},
              "dbl_world_series_win_prob": ${tp.worldSeriesWinProb},
              "dbl_expected_season_wins": ${tp.expectedSeasonWins},
              "dbl_latent_quality_score": ${tp.latentQualityScore}
            }
            """.trimIndent()
        }
        return "[\n$rows\n]"
    }
}
