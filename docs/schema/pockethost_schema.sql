-- =================================================================================================
-- ⚾ POCKETHOST / POCKETBASE DATABASE SCHEMA (HUNGARIAN PREFIX NOTATION)
--    MLB Sabermetric World Series Prediction & Historical Standings Tracking Engine
-- =================================================================================================

PRAGMA foreign_keys = ON;

-- -------------------------------------------------------------------------------------------------
-- 1. Master Teams Table: tbl_mlb_teams
-- -------------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tbl_mlb_teams (
    id_team VARCHAR(36) PRIMARY KEY,
    str_team_code VARCHAR(10) NOT NULL UNIQUE,
    str_team_name VARCHAR(100) NOT NULL,
    str_league VARCHAR(10) NOT NULL,
    str_division VARCHAR(10) NOT NULL,
    dt_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_mlb_teams_code ON tbl_mlb_teams(str_team_code);
CREATE INDEX IF NOT EXISTS idx_mlb_teams_league_div ON tbl_mlb_teams(str_league, str_division);

-- -------------------------------------------------------------------------------------------------
-- 2. Simulation Runs Table: tbl_simulation_runs
-- -------------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tbl_simulation_runs (
    id_run VARCHAR(36) PRIMARY KEY,
    str_run_id VARCHAR(50) NOT NULL UNIQUE,
    dt_run_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    int_total_iterations INTEGER NOT NULL DEFAULT 10000,
    int_random_seed BIGINT NOT NULL,
    str_top_favorite_code VARCHAR(10) NOT NULL,
    dbl_top_favorite_prob DOUBLE PRECISION NOT NULL,
    str_causal_engine_status VARCHAR(50) NOT NULL DEFAULT 'Active',
    str_hype_multiplier_note TEXT
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_sim_runs_run_id ON tbl_simulation_runs(str_run_id);
CREATE INDEX IF NOT EXISTS idx_sim_runs_timestamp ON tbl_simulation_runs(dt_run_timestamp);

-- -------------------------------------------------------------------------------------------------
-- 3. Team Snapshots Table: tbl_team_snapshots
-- -------------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tbl_team_snapshots (
    id_snapshot VARCHAR(36) PRIMARY KEY,
    rel_run_id VARCHAR(36) NOT NULL REFERENCES tbl_simulation_runs(id_run) ON DELETE CASCADE,
    rel_team_id VARCHAR(36) NOT NULL REFERENCES tbl_mlb_teams(id_team) ON DELETE CASCADE,
    str_team_code VARCHAR(10) NOT NULL,
    int_wins INTEGER NOT NULL,
    int_losses INTEGER NOT NULL,
    dbl_runs_scored DOUBLE PRECISION NOT NULL,
    dbl_runs_allowed DOUBLE PRECISION NOT NULL,
    dbl_team_war DOUBLE PRECISION NOT NULL,
    dbl_woba DOUBLE PRECISION NOT NULL,
    dbl_wrc_plus DOUBLE PRECISION NOT NULL,
    dbl_fip DOUBLE PRECISION NOT NULL,
    dbl_xfip DOUBLE PRECISION NOT NULL,
    dbl_bullpen_wpa DOUBLE PRECISION NOT NULL,
    dbl_top3_ace_era DOUBLE PRECISION NOT NULL,
    dbl_thumbs_down_hype_index DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    dt_snapshot_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_snapshot_run_team UNIQUE (rel_run_id, rel_team_id)
);

CREATE INDEX IF NOT EXISTS idx_snapshots_run_id ON tbl_team_snapshots(rel_run_id);
CREATE INDEX IF NOT EXISTS idx_snapshots_team_id ON tbl_team_snapshots(rel_team_id);
CREATE INDEX IF NOT EXISTS idx_snapshots_code ON tbl_team_snapshots(str_team_code);

-- -------------------------------------------------------------------------------------------------
-- 4. Rank Movements Table: tbl_rank_movements
-- -------------------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tbl_rank_movements (
    id_movement VARCHAR(36) PRIMARY KEY,
    rel_run_id VARCHAR(36) NOT NULL REFERENCES tbl_simulation_runs(id_run) ON DELETE CASCADE,
    rel_team_id VARCHAR(36) NOT NULL REFERENCES tbl_mlb_teams(id_team) ON DELETE CASCADE,
    str_team_code VARCHAR(10) NOT NULL,
    int_regular_season_rank INTEGER NOT NULL,
    int_sim_rank INTEGER NOT NULL,
    int_rank_delta INTEGER NOT NULL, -- (regular_season_rank - sim_rank)
    str_movement_symbol VARCHAR(10) NOT NULL, -- '▲ +k', '▼ -k', '—'
    dbl_playoff_prob DOUBLE PRECISION NOT NULL,
    dbl_pennant_prob DOUBLE PRECISION NOT NULL,
    dbl_world_series_win_prob DOUBLE PRECISION NOT NULL,
    dbl_expected_season_wins DOUBLE PRECISION NOT NULL,
    dbl_latent_quality_score DOUBLE PRECISION NOT NULL,
    dt_recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_movement_run_team UNIQUE (rel_run_id, rel_team_id)
);

CREATE INDEX IF NOT EXISTS idx_movements_run_id ON tbl_rank_movements(rel_run_id);
CREATE INDEX IF NOT EXISTS idx_movements_team_id ON tbl_rank_movements(rel_team_id);
CREATE INDEX IF NOT EXISTS idx_movements_sim_rank ON tbl_rank_movements(int_sim_rank);
CREATE INDEX IF NOT EXISTS idx_movements_rank_delta ON tbl_rank_movements(int_rank_delta);
