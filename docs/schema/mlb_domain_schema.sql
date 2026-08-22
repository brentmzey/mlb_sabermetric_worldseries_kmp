-- =================================================================================================
-- ⚾ MLB SABERMETRIC WORLD SERIES DOMAIN REGISTRY SQL DDL SCHEMA
--    Enforces strict relational constraints, check constraints, foreign keys, and indexes
--    synchronized with docs/schema/mlb_domain_registry.json, Kotlin KMP, and Python.
-- =================================================================================================

PRAGMA foreign_keys = ON;

-- -------------------------------------------------------------------------------------------------
-- 1. Master Franchises Registry: i_mlb_teams
-- -------------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS i_mlb_teams (
    id VARCHAR(36) PRIMARY KEY,
    str_team_code VARCHAR(3) NOT NULL UNIQUE,
    str_team_name VARCHAR(60) NOT NULL,
    str_league VARCHAR(2) NOT NULL CHECK (UPPER(str_league) IN ('AL', 'NL')),
    str_division VARCHAR(10) NOT NULL CHECK (UPPER(str_division) IN ('EAST', 'CENTRAL', 'WEST')),
    str_city VARCHAR(50) NOT NULL,
    str_ballpark VARCHAR(80) NOT NULL,
    int_founded_year INTEGER NOT NULL CHECK (int_founded_year >= 1850 AND int_founded_year <= 2100),
    int_mlb_api_id INTEGER NOT NULL UNIQUE,
    bool_is_active INTEGER NOT NULL DEFAULT 1 CHECK (bool_is_active IN (0, 1)),
    str_status_code VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (UPPER(str_status_code) IN ('ACTIVE', 'INACTIVE', 'SUPERSEDED', 'ARCHIVED')),
    int_created_epoch_ms_utc BIGINT NOT NULL,
    int_updated_epoch_ms_utc BIGINT NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_i_mlb_teams_code ON i_mlb_teams(str_team_code);
CREATE INDEX IF NOT EXISTS idx_i_mlb_teams_league_div ON i_mlb_teams(str_league, str_division);
CREATE INDEX IF NOT EXISTS idx_i_mlb_teams_active ON i_mlb_teams(str_team_code, bool_is_active);

-- -------------------------------------------------------------------------------------------------
-- 2. Raw Team Season Sabermetric Inputs: i_team_season_inputs
-- -------------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS i_team_season_inputs (
    id VARCHAR(36) PRIMARY KEY,
    str_team_code VARCHAR(3) NOT NULL REFERENCES i_mlb_teams(str_team_code) ON DELETE RESTRICT,
    int_season_year INTEGER NOT NULL DEFAULT 2026,
    int_season_week INTEGER NOT NULL DEFAULT 18,
    int_wins INTEGER NOT NULL CHECK (int_wins >= 0 AND int_wins <= 162),
    int_losses INTEGER NOT NULL CHECK (int_losses >= 0 AND int_losses <= 162),
    dbl_runs_scored DOUBLE PRECISION NOT NULL CHECK (dbl_runs_scored >= 0),
    dbl_runs_allowed DOUBLE PRECISION NOT NULL CHECK (dbl_runs_allowed >= 0),
    dbl_team_war DOUBLE PRECISION NOT NULL,
    dbl_woba DOUBLE PRECISION NOT NULL CHECK (dbl_woba >= 0.150 AND dbl_woba <= 0.600),
    dbl_wrc_plus DOUBLE PRECISION NOT NULL CHECK (dbl_wrc_plus >= 30.0 AND dbl_wrc_plus <= 200.0),
    dbl_fip DOUBLE PRECISION NOT NULL CHECK (dbl_fip >= 1.0 AND dbl_fip <= 10.0),
    dbl_xfip DOUBLE PRECISION NOT NULL CHECK (dbl_xfip >= 1.0 AND dbl_xfip <= 10.0),
    dbl_bullpen_wpa DOUBLE PRECISION NOT NULL,
    dbl_top3_ace_era DOUBLE PRECISION NOT NULL CHECK (dbl_top3_ace_era >= 0.50 AND dbl_top3_ace_era <= 9.00),
    int_last10_wins INTEGER NOT NULL DEFAULT 5 CHECK (int_last10_wins >= 0 AND int_last10_wins <= 10),
    int_last10_losses INTEGER NOT NULL DEFAULT 5 CHECK (int_last10_losses >= 0 AND int_last10_losses <= 10),
    bool_is_active INTEGER NOT NULL DEFAULT 1 CHECK (bool_is_active IN (0, 1)),
    str_status_code VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (str_status_code IN ('ACTIVE', 'INACTIVE', 'SUPERSEDED', 'ARCHIVED')),
    int_created_epoch_ms_utc BIGINT NOT NULL,
    int_updated_epoch_ms_utc BIGINT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_i_season_inputs_team_year ON i_team_season_inputs(str_team_code, int_season_year);
CREATE INDEX IF NOT EXISTS idx_i_season_inputs_active ON i_team_season_inputs(str_team_code, bool_is_active, int_updated_epoch_ms_utc);

-- -------------------------------------------------------------------------------------------------
-- 3. Simulation Runs Registry: m_simulation_runs
-- -------------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS m_simulation_runs (
    id VARCHAR(36) PRIMARY KEY,
    str_run_id VARCHAR(60) NOT NULL UNIQUE,
    dt_run_timestamp TIMESTAMP NOT NULL,
    int_season_year INTEGER NOT NULL DEFAULT 2026,
    int_total_iterations INTEGER NOT NULL DEFAULT 10000 CHECK (int_total_iterations >= 100),
    int_random_seed BIGINT NOT NULL,
    str_engine_version VARCHAR(40) NOT NULL,
    str_top_favorite_code VARCHAR(3) NOT NULL REFERENCES i_mlb_teams(str_team_code) ON DELETE RESTRICT,
    dbl_top_favorite_prob DOUBLE PRECISION NOT NULL CHECK (dbl_top_favorite_prob >= 0.0 AND dbl_top_favorite_prob <= 1.0),
    str_causal_iv_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE_2SLS_PYTHAGOREAN_LOG5',
    bool_is_active INTEGER NOT NULL DEFAULT 1 CHECK (bool_is_active IN (0, 1)),
    str_status_code VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (str_status_code IN ('ACTIVE', 'INACTIVE', 'SUPERSEDED', 'ARCHIVED')),
    int_created_epoch_ms_utc BIGINT NOT NULL,
    int_updated_epoch_ms_utc BIGINT NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_m_sim_runs_run_id ON m_simulation_runs(str_run_id);
CREATE INDEX IF NOT EXISTS idx_m_sim_runs_year ON m_simulation_runs(int_season_year, int_updated_epoch_ms_utc);

-- -------------------------------------------------------------------------------------------------
-- 4. Latent Quality Estimates: m_latent_quality_estimates
-- -------------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS m_latent_quality_estimates (
    id VARCHAR(36) PRIMARY KEY,
    str_run_id VARCHAR(60) NOT NULL REFERENCES m_simulation_runs(str_run_id) ON DELETE CASCADE,
    str_team_code VARCHAR(3) NOT NULL REFERENCES i_mlb_teams(str_team_code) ON DELETE RESTRICT,
    int_season_year INTEGER NOT NULL DEFAULT 2026,
    dbl_latent_quality_score DOUBLE PRECISION NOT NULL,
    dbl_bayes_adjusted_win_pct DOUBLE PRECISION NOT NULL CHECK (dbl_bayes_adjusted_win_pct >= 0.0 AND dbl_bayes_adjusted_win_pct <= 1.0),
    dbl_recency_win_pct DOUBLE PRECISION NOT NULL CHECK (dbl_recency_win_pct >= 0.0 AND dbl_recency_win_pct <= 1.0),
    dbl_momentum_multiplier DOUBLE PRECISION NOT NULL CHECK (dbl_momentum_multiplier >= 0.5 AND dbl_momentum_multiplier <= 2.0),
    dbl_hype_multiplier DOUBLE PRECISION NOT NULL CHECK (dbl_hype_multiplier >= 0.5 AND dbl_hype_multiplier <= 2.0),
    bool_is_active INTEGER NOT NULL DEFAULT 1 CHECK (bool_is_active IN (0, 1)),
    str_status_code VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (str_status_code IN ('ACTIVE', 'INACTIVE', 'SUPERSEDED', 'ARCHIVED')),
    int_created_epoch_ms_utc BIGINT NOT NULL,
    int_updated_epoch_ms_utc BIGINT NOT NULL,
    CONSTRAINT uq_latent_run_team UNIQUE (str_run_id, str_team_code)
);

CREATE INDEX IF NOT EXISTS idx_m_latent_run_team ON m_latent_quality_estimates(str_run_id, str_team_code);
CREATE INDEX IF NOT EXISTS idx_m_latent_active ON m_latent_quality_estimates(str_team_code, bool_is_active);

-- -------------------------------------------------------------------------------------------------
-- 5. Playoff Series Simulations: o_playoff_series_simulations
-- -------------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS o_playoff_series_simulations (
    id VARCHAR(36) PRIMARY KEY,
    str_run_id VARCHAR(60) NOT NULL REFERENCES m_simulation_runs(str_run_id) ON DELETE CASCADE,
    str_round_name VARCHAR(25) NOT NULL CHECK (str_round_name IN ('WILD_CARD', 'DIVISION_SERIES', 'LEAGUE_CHAMPIONSHIP', 'WORLD_SERIES')),
    str_team_a_code VARCHAR(3) NOT NULL REFERENCES i_mlb_teams(str_team_code) ON DELETE RESTRICT,
    str_team_b_code VARCHAR(3) NOT NULL REFERENCES i_mlb_teams(str_team_code) ON DELETE RESTRICT,
    dbl_team_a_win_prob DOUBLE PRECISION NOT NULL CHECK (dbl_team_a_win_prob >= 0.0 AND dbl_team_a_win_prob <= 1.0),
    dbl_expected_games DOUBLE PRECISION NOT NULL CHECK (dbl_expected_games >= 2.0 AND dbl_expected_games <= 7.0),
    bool_is_active INTEGER NOT NULL DEFAULT 1 CHECK (bool_is_active IN (0, 1)),
    str_status_code VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (str_status_code IN ('ACTIVE', 'INACTIVE', 'SUPERSEDED', 'ARCHIVED')),
    int_created_epoch_ms_utc BIGINT NOT NULL,
    int_updated_epoch_ms_utc BIGINT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_o_series_run_round ON o_playoff_series_simulations(str_run_id, str_round_name);

-- -------------------------------------------------------------------------------------------------
-- 6. World Series Championship Leaderboard: f_world_series_leaderboard
-- -------------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS f_world_series_leaderboard (
    id VARCHAR(36) PRIMARY KEY,
    str_run_id VARCHAR(60) NOT NULL REFERENCES m_simulation_runs(str_run_id) ON DELETE CASCADE,
    str_team_code VARCHAR(3) NOT NULL REFERENCES i_mlb_teams(str_team_code) ON DELETE RESTRICT,
    str_team_name VARCHAR(60) NOT NULL,
    str_league VARCHAR(2) NOT NULL CHECK (UPPER(str_league) IN ('AL', 'NL')),
    str_division VARCHAR(10) NOT NULL CHECK (UPPER(str_division) IN ('EAST', 'CENTRAL', 'WEST')),
    int_sim_rank INTEGER NOT NULL CHECK (int_sim_rank >= 1 AND int_sim_rank <= 30),
    dbl_expected_season_wins DOUBLE PRECISION NOT NULL CHECK (dbl_expected_season_wins >= 30.0 AND dbl_expected_season_wins <= 130.0),
    dbl_playoff_prob DOUBLE PRECISION NOT NULL CHECK (dbl_playoff_prob >= 0.0 AND dbl_playoff_prob <= 1.0),
    dbl_pennant_prob DOUBLE PRECISION NOT NULL CHECK (dbl_pennant_prob >= 0.0 AND dbl_pennant_prob <= 1.0),
    dbl_world_series_win_prob DOUBLE PRECISION NOT NULL CHECK (dbl_world_series_win_prob >= 0.0 AND dbl_world_series_win_prob <= 1.0),
    str_visual_bar VARCHAR(20),
    bool_is_active INTEGER NOT NULL DEFAULT 1 CHECK (bool_is_active IN (0, 1)),
    str_status_code VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (UPPER(str_status_code) IN ('ACTIVE', 'INACTIVE', 'SUPERSEDED', 'ARCHIVED')),
    int_created_epoch_ms_utc BIGINT NOT NULL,
    int_updated_epoch_ms_utc BIGINT NOT NULL,
    CONSTRAINT uq_leaderboard_run_team UNIQUE (str_run_id, str_team_code)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_f_leaderboard_run_rank ON f_world_series_leaderboard(str_run_id, int_sim_rank);
CREATE INDEX IF NOT EXISTS idx_f_leaderboard_active ON f_world_series_leaderboard(str_team_code, bool_is_active, int_sim_rank);

-- -------------------------------------------------------------------------------------------------
-- 7. High-Performance SQL View: vw_latest_active_world_series_leaderboard
-- -------------------------------------------------------------------------------------------------
CREATE VIEW IF NOT EXISTS vw_latest_active_world_series_leaderboard AS
SELECT 
    l.int_sim_rank AS sim_rank,
    l.str_team_code AS team_code,
    l.str_team_name AS team_name,
    l.str_league AS league,
    l.str_division AS division,
    t.str_ballpark AS ballpark,
    t.str_city AS city,
    l.dbl_expected_season_wins AS expected_wins,
    l.dbl_playoff_prob AS playoff_prob,
    l.dbl_pennant_prob AS pennant_prob,
    l.dbl_world_series_win_prob AS world_series_win_prob,
    l.str_visual_bar AS visual_bar,
    l.str_run_id AS run_id,
    l.int_updated_epoch_ms_utc AS updated_epoch_ms_utc
FROM f_world_series_leaderboard l
JOIN i_mlb_teams t ON l.str_team_code = t.str_team_code
WHERE l.bool_is_active = 1
  AND (
    l.str_run_id = (
      SELECT m.str_run_id 
      FROM m_simulation_runs m 
      WHERE m.bool_is_active = 1 
      ORDER BY m.int_updated_epoch_ms_utc DESC 
      LIMIT 1
    )
    OR NOT EXISTS (SELECT 1 FROM m_simulation_runs)
  )
ORDER BY l.int_sim_rank ASC;
