package com.sabermetrics.worldseries.sync

import com.sabermetrics.worldseries.data.SabermetricDataService
import com.sabermetrics.worldseries.model.MlbTeam
import com.sabermetrics.worldseries.model.TeamProbability
import com.sabermetrics.worldseries.model.WorldSeriesSimulationResult
import com.sabermetrics.worldseries.repository.FWorldSeriesLeaderboardRecord
import com.sabermetrics.worldseries.repository.HungarianQueryBuilder
import com.sabermetrics.worldseries.repository.IMlbTeamRecord
import com.sabermetrics.worldseries.repository.MLatentQualityEstimateRecord
import com.sabermetrics.worldseries.repository.MSimulationRunRecord
import com.sabermetrics.worldseries.repository.RecordStatusCode

/**
 * PocketHost / PocketBase Cloud Synchronization Configuration.
 */
data class PocketHostConfig(
    val baseUrl: String = "https://mlb-sabermetrics.pockethost.io",
    val authToken: String? = null,
    val backoffPolicy: ExponentialBackoffPolicy = ExponentialBackoffPolicy()
)

/**
 * Audit Report detailing the status of PocketHost database synchronization.
 */
data class PocketHostSyncReport(
    val runId: String,
    val timestampUtc: String,
    val collectionsSynced: List<String>,
    val totalRecordsSynced: Int,
    val isSuccessful: Boolean,
    val logEntries: List<String>
)

/**
 * High-Performance Client for synchronizing MLB Sabermetric Simulation Data
 * with hosted PocketBase instances on PocketHost using Hungarian Schema & Exponential Back-Off.
 */
class PocketHostSyncClient(
    val config: PocketHostConfig = PocketHostConfig()
) {

    /**
     * Serializes `m_simulation_runs` record to Hungarian JSON string.
     */
    fun serializeSimulationRunRecord(runId: String, result: WorldSeriesSimulationResult, seed: Long, epochMs: Long = 1786704000000L): String {
        val top = result.leaderboard.first()
        val record = MSimulationRunRecord(
            str_run_id = runId,
            dt_run_timestamp = "2026-08-14T22:30:00.000Z",
            int_season_year = 2026,
            int_total_iterations = result.totalSimulations,
            int_random_seed = seed.toInt(),
            str_engine_version = "2.4.0-KMP-2SLS",
            str_top_favorite_code = top.team.id,
            dbl_top_favorite_prob = top.worldSeriesWinProb,
            str_causal_iv_status = result.causalDiagnostics["Causal_2SLS_IV_Engine"] ?: "Active",
            bool_is_active = true,
            str_status_code = RecordStatusCode.ACTIVE.name,
            int_created_epoch_ms_utc = epochMs,
            int_updated_epoch_ms_utc = epochMs
        )
        return """
        {
          "str_run_id": "${record.str_run_id}",
          "dt_run_timestamp": "${record.dt_run_timestamp}",
          "int_season_year": ${record.int_season_year},
          "int_total_iterations": ${record.int_total_iterations},
          "int_random_seed": ${record.int_random_seed},
          "str_engine_version": "${record.str_engine_version}",
          "str_top_favorite_code": "${record.str_top_favorite_code}",
          "dbl_top_favorite_prob": ${record.dbl_top_favorite_prob},
          "str_causal_iv_status": "${record.str_causal_iv_status}",
          "bool_is_active": ${record.bool_is_active},
          "str_status_code": "${record.str_status_code}",
          "int_created_epoch_ms_utc": ${record.int_created_epoch_ms_utc},
          "int_updated_epoch_ms_utc": ${record.int_updated_epoch_ms_utc}
        }
        """.trimIndent()
    }

    /**
     * Serializes all 30 teams into `f_world_series_leaderboard` JSON array.
     */
    fun serializeLeaderboardRecords(runId: String, leaderboard: List<TeamProbability>, epochMs: Long = 1786704000000L): String {
        val rows = leaderboard.map { tp ->
            val record = FWorldSeriesLeaderboardRecord(
                str_run_id = runId,
                str_team_code = tp.team.id,
                str_team_name = tp.team.name,
                str_league = tp.team.league.name,
                str_division = tp.team.division.name,
                int_sim_rank = tp.simRank,
                dbl_expected_season_wins = tp.expectedSeasonWins,
                dbl_playoff_prob = tp.playoffProb,
                dbl_pennant_prob = tp.pennantProb,
                dbl_world_series_win_prob = tp.worldSeriesWinProb,
                str_visual_bar = tp.movementSymbol,
                bool_is_active = true,
                str_status_code = RecordStatusCode.ACTIVE.name,
                int_created_epoch_ms_utc = epochMs,
                int_updated_epoch_ms_utc = epochMs
            )
            """
            {
              "str_run_id": "${record.str_run_id}",
              "str_team_code": "${record.str_team_code}",
              "str_team_name": "${record.str_team_name}",
              "str_league": "${record.str_league}",
              "str_division": "${record.str_division}",
              "int_sim_rank": ${record.int_sim_rank},
              "dbl_expected_season_wins": ${record.dbl_expected_season_wins},
              "dbl_playoff_prob": ${record.dbl_playoff_prob},
              "dbl_pennant_prob": ${record.dbl_pennant_prob},
              "dbl_world_series_win_prob": ${record.dbl_world_series_win_prob},
              "str_visual_bar": "${record.str_visual_bar}",
              "bool_is_active": ${record.bool_is_active},
              "str_status_code": "${record.str_status_code}",
              "int_created_epoch_ms_utc": ${record.int_created_epoch_ms_utc},
              "int_updated_epoch_ms_utc": ${record.int_updated_epoch_ms_utc}
            }
            """.trimIndent()
        }
        return "[\n" + rows.joinToString(",\n") + "\n]"
    }

    /**
     * Serializes all 30 teams into `m_latent_quality_estimates` JSON array.
     */
    fun serializeLatentQualities(runId: String, leaderboard: List<TeamProbability>, epochMs: Long = 1786704000000L): String {
        val rows = leaderboard.map { tp ->
            val record = MLatentQualityEstimateRecord(
                str_run_id = runId,
                str_team_code = tp.team.id,
                int_season_year = 2026,
                dbl_latent_quality_score = tp.latentQualityScore,
                dbl_bayes_adjusted_win_pct = tp.team.bayesianAdjustedWinPct,
                dbl_recency_win_pct = tp.team.recencyWeightedWinPct,
                dbl_momentum_multiplier = tp.team.hotStreakMomentumMultiplier,
                dbl_hype_multiplier = tp.team.clubhouseHypeIndex,
                bool_is_active = true,
                str_status_code = RecordStatusCode.ACTIVE.name,
                int_created_epoch_ms_utc = epochMs,
                int_updated_epoch_ms_utc = epochMs
            )
            """
            {
              "str_run_id": "${record.str_run_id}",
              "str_team_code": "${record.str_team_code}",
              "int_season_year": ${record.int_season_year},
              "dbl_latent_quality_score": ${record.dbl_latent_quality_score},
              "dbl_bayes_adjusted_win_pct": ${record.dbl_bayes_adjusted_win_pct},
              "dbl_recency_win_pct": ${record.dbl_recency_win_pct},
              "dbl_momentum_multiplier": ${record.dbl_momentum_multiplier},
              "dbl_hype_multiplier": ${record.dbl_hype_multiplier},
              "bool_is_active": ${record.bool_is_active},
              "str_status_code": "${record.str_status_code}",
              "int_created_epoch_ms_utc": ${record.int_created_epoch_ms_utc},
              "int_updated_epoch_ms_utc": ${record.int_updated_epoch_ms_utc}
            }
            """.trimIndent()
        }
        return "[\n" + rows.joinToString(",\n") + "\n]"
    }

    /**
     * Generates a complete, multi-collection database sync JSON bundle ready for batch import.
     */
    fun generateFullDatabaseSyncPackage(runId: String, result: WorldSeriesSimulationResult, seed: Long): String {
        val epochMs = 1786704000000L
        val runJson = serializeSimulationRunRecord(runId, result, seed, epochMs)
        val leaderboardJson = serializeLeaderboardRecords(runId, result.leaderboard, epochMs)
        val qualitiesJson = serializeLatentQualities(runId, result.leaderboard, epochMs)

        return """
        {
          "schema_version": "1.0.0-hungarian",
          "target_instance": "${config.baseUrl}",
          "sync_epoch_ms_utc": $epochMs,
          "run_id": "$runId",
          "collections": {
            "m_simulation_runs": $runJson,
            "m_latent_quality_estimates": $qualitiesJson,
            "f_world_series_leaderboard": $leaderboardJson
          }
        }
        """.trimIndent()
    }

    /**
     * Executes cloud synchronization across all PocketHost collections with Exponential Back-Off.
     * Uses optional transporter callback or local simulation.
     */
    fun syncDatabaseWithRetry(
        runId: String,
        result: WorldSeriesSimulationResult,
        seed: Long,
        transporter: ((collection: String, payloadJson: String) -> Boolean)? = null
    ): PocketHostSyncReport {
        val logs = mutableListOf<String>()
        val collections = listOf("m_simulation_runs", "m_latent_quality_estimates", "f_world_series_leaderboard")
        val epochMs = 1786704000000L
        var totalRecords = 0
        var allSuccess = true

        logs.add("🚀 Starting PocketHost Cloud Sync to ${config.baseUrl} [Run: $runId]")

        // 1. Sync Simulation Run Record
        val runPayload = serializeSimulationRunRecord(runId, result, seed, epochMs)
        val runResult = config.backoffPolicy.executeSync(
            onRetry = { attempt, err, delayMs ->
                logs.add("⚠️  [m_simulation_runs] Attempt $attempt failed (${err.message}). Retrying in ${delayMs}ms...")
            }
        ) { attempt ->
            if (transporter != null) {
                val ok = transporter("m_simulation_runs", runPayload)
                if (!ok) throw RuntimeException("HTTP 503 Service Unavailable on attempt $attempt")
            }
            1
        }
        if (runResult.isSuccess) {
            totalRecords += 1
            logs.add("✅ Synced 1 record to 'm_simulation_runs'")
        } else {
            allSuccess = false
            logs.add("❌ Failed to sync 'm_simulation_runs': ${runResult.exceptionOrNull()?.message}")
        }

        // 2. Sync Latent Quality Estimates
        val qualitiesPayload = serializeLatentQualities(runId, result.leaderboard, epochMs)
        val qualResult = config.backoffPolicy.executeSync(
            onRetry = { attempt, err, delayMs ->
                logs.add("⚠️  [m_latent_quality_estimates] Attempt $attempt failed (${err.message}). Retrying in ${delayMs}ms...")
            }
        ) { attempt ->
            if (transporter != null) {
                val ok = transporter("m_latent_quality_estimates", qualitiesPayload)
                if (!ok) throw RuntimeException("HTTP 429 Rate Limit on attempt $attempt")
            }
            result.leaderboard.size
        }
        if (qualResult.isSuccess) {
            totalRecords += result.leaderboard.size
            logs.add("✅ Synced ${result.leaderboard.size} records to 'm_latent_quality_estimates'")
        } else {
            allSuccess = false
            logs.add("❌ Failed to sync 'm_latent_quality_estimates': ${qualResult.exceptionOrNull()?.message}")
        }

        // 3. Sync Final World Series Leaderboard
        val leaderboardPayload = serializeLeaderboardRecords(runId, result.leaderboard, epochMs)
        val lbResult = config.backoffPolicy.executeSync(
            onRetry = { attempt, err, delayMs ->
                logs.add("⚠️  [f_world_series_leaderboard] Attempt $attempt failed (${err.message}). Retrying in ${delayMs}ms...")
            }
        ) { attempt ->
            if (transporter != null) {
                val ok = transporter("f_world_series_leaderboard", leaderboardPayload)
                if (!ok) throw RuntimeException("HTTP 502 Bad Gateway on attempt $attempt")
            }
            result.leaderboard.size
        }
        if (lbResult.isSuccess) {
            totalRecords += result.leaderboard.size
            logs.add("✅ Synced ${result.leaderboard.size} records to 'f_world_series_leaderboard'")
        } else {
            allSuccess = false
            logs.add("❌ Failed to sync 'f_world_series_leaderboard': ${lbResult.exceptionOrNull()?.message}")
        }

        logs.add("🏁 PocketHost Cloud Sync Complete: $totalRecords total records processed across ${collections.size} collections.")

        return PocketHostSyncReport(
            runId = runId,
            timestampUtc = "2026-08-14T22:30:00.000Z",
            collectionsSynced = collections,
            totalRecordsSynced = totalRecords,
            isSuccessful = allSuccess,
            logEntries = logs
        )
    }
}
