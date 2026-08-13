package com.sabermetrics.worldseries.repository

/**
 * Enumerable record lifecycle status for non-destructive row-level time-series management.
 */
enum class RecordStatusCode {
    ACTIVE,      // Currently authoritative active record for model selection
    INACTIVE,    // Temporarily disabled record
    SUPERSEDED,  // Historical record replaced by a newer update in time series
    ARCHIVED     // Cold-storage historical snapshot
}

/**
 * Base interface for all Hungarian-prefixed PocketBase records with explicit Epoch Millis in UTC.
 */
interface HungarianRecord {
    val bool_is_active: Boolean
    val str_status_code: String
    val int_created_epoch_ms_utc: Long
    val int_updated_epoch_ms_utc: Long
}

/**
 * Team Master Registry Record (`i_mlb_teams`).
 */
data class IMlbTeamRecord(
    val id: String? = null,
    val str_team_code: String,
    val str_team_name: String,
    val str_league: String,
    val str_division: String,
    val str_city: String? = null,
    val str_ballpark: String? = null,
    val int_founded_year: Int? = null,
    override val bool_is_active: Boolean = true,
    override val str_status_code: String = RecordStatusCode.ACTIVE.name,
    override val int_created_epoch_ms_utc: Long,
    override val int_updated_epoch_ms_utc: Long
) : HungarianRecord

/**
 * Raw Team Season Sabermetric Input Record (`i_team_season_inputs`).
 */
data class ITeamSeasonInputRecord(
    val id: String? = null,
    val str_team_code: String,
    val int_season_year: Int,
    val int_season_week: Int,
    val int_wins: Int,
    val int_losses: Int,
    val dbl_runs_scored: Double,
    val dbl_runs_allowed: Double,
    val dbl_team_war: Double,
    val dbl_woba: Double,
    val dbl_wrc_plus: Double,
    val dbl_fip: Double,
    val dbl_xfip: Double,
    val dbl_bullpen_wpa: Double,
    val dbl_top3_ace_era: Double,
    val int_last10_wins: Int,
    val int_last10_losses: Int,
    override val bool_is_active: Boolean = true,
    override val str_status_code: String = RecordStatusCode.ACTIVE.name,
    override val int_created_epoch_ms_utc: Long,
    override val int_updated_epoch_ms_utc: Long
) : HungarianRecord

/**
 * Monte Carlo Simulation Run Execution Record (`m_simulation_runs`).
 */
data class MSimulationRunRecord(
    val id: String? = null,
    val str_run_id: String,
    val dt_run_timestamp: String,
    val int_season_year: Int,
    val int_total_iterations: Int,
    val int_random_seed: Int,
    val str_engine_version: String,
    val str_top_favorite_code: String,
    val dbl_top_favorite_prob: Double,
    val str_causal_iv_status: String,
    override val bool_is_active: Boolean = true,
    override val str_status_code: String = RecordStatusCode.ACTIVE.name,
    override val int_created_epoch_ms_utc: Long,
    override val int_updated_epoch_ms_utc: Long
) : HungarianRecord

/**
 * Econometric Latent Quality Estimates Record (`m_latent_quality_estimates`).
 */
data class MLatentQualityEstimateRecord(
    val id: String? = null,
    val str_run_id: String,
    val str_team_code: String,
    val int_season_year: Int,
    val dbl_latent_quality_score: Double,
    val dbl_bayes_adjusted_win_pct: Double,
    val dbl_recency_win_pct: Double,
    val dbl_momentum_multiplier: Double,
    val dbl_hype_multiplier: Double,
    override val bool_is_active: Boolean = true,
    override val str_status_code: String = RecordStatusCode.ACTIVE.name,
    override val int_created_epoch_ms_utc: Long,
    override val int_updated_epoch_ms_utc: Long
) : HungarianRecord

/**
 * Final World Series Championship Leaderboard Record (`f_world_series_leaderboard`).
 */
data class FWorldSeriesLeaderboardRecord(
    val id: String? = null,
    val str_run_id: String,
    val str_team_code: String,
    val str_team_name: String,
    val str_league: String,
    val str_division: String,
    val int_sim_rank: Int,
    val dbl_expected_season_wins: Double,
    val dbl_playoff_prob: Double,
    val dbl_pennant_prob: Double,
    val dbl_world_series_win_prob: Double,
    val str_visual_bar: String? = null,
    override val bool_is_active: Boolean = true,
    override val str_status_code: String = RecordStatusCode.ACTIVE.name,
    override val int_created_epoch_ms_utc: Long,
    override val int_updated_epoch_ms_utc: Long
) : HungarianRecord

/**
 * High-Performance Query Builder for Latest-Active PocketBase Record Retrieval.
 */
object HungarianQueryBuilder {

    /**
     * Builds URL filter string for querying the latest ACTIVE record for a team.
     * PocketBase Filter: (str_team_code='CHC' && bool_is_active=true)
     * Sort: -int_updated_epoch_ms_utc
     */
    fun buildLatestActiveTeamFilter(teamCode: String): String {
        return "filter=(str_team_code='${teamCode}' && bool_is_active=true)&sort=-int_updated_epoch_ms_utc&limit=1"
    }

    /**
     * Builds URL filter string for querying all current ACTIVE records for a simulation run.
     */
    fun buildActiveRunFilter(runId: String): String {
        return "filter=(str_run_id='${runId}' && bool_is_active=true)&sort=int_sim_rank"
    }
}
