package com.sabermetrics.worldseries.sync

import com.sabermetrics.worldseries.data.SabermetricDataService
import com.sabermetrics.worldseries.engine.WorldSeriesSimulator
import com.sabermetrics.worldseries.model.MlbTeam
import com.sabermetrics.worldseries.model.TeamProbability
import com.sabermetrics.worldseries.model.WorldSeriesSimulationResult
import com.sabermetrics.worldseries.util.TimeUtils
import java.io.File
import java.sql.DriverManager

/**
 * High-performance Local SQLite Database & Local PocketBase stack exporter.
 * Mirrors the exact PocketHost cloud schema locally to provide offline relational SQL querying,
 * SQLite file generation (`output_datasets/mlb_sabermetrics_local.sqlite`),
 * and standalone SQL dumps (`output_datasets/mlb_sabermetrics_local_dump.sql`).
 */
object LocalSqliteDatabaseService {

    data class LocalDatabaseReport(
        val sqliteFile: File,
        val sqlDumpFile: File,
        val totalTablesCreated: Int,
        val totalRowsInserted: Int,
        val isSuccessful: Boolean,
        val executionTimeMs: Long
    )

    fun exportToLocalSqliteDatabase(
        runId: String,
        result: WorldSeriesSimulationResult,
        epochTimestampMs: Long = TimeUtils.currentTimeMillisUtc(),
        outputDir: File = File("output_datasets")
    ): LocalDatabaseReport {
        val startTime = System.currentTimeMillis()
        if (!outputDir.exists()) outputDir.mkdirs()

        val sqliteFile = File(outputDir, "mlb_sabermetrics_local.sqlite")
        if (sqliteFile.exists()) sqliteFile.delete()

        val sqlDumpFile = File(outputDir, "mlb_sabermetrics_local_dump.sql")
        val sqlBuilder = StringBuilder()

        val teams: List<MlbTeam> = SabermetricDataService.loadCleanedMlbDataset()
        val momentumMap = WorldSeriesSimulator.computeMultiDimensionalMomentumMultipliers(teams)

        var rowsCount = 0

        // 1. Establish SQLite JDBC connection
        val jdbcUrl = "jdbc:sqlite:${sqliteFile.absolutePath}"
        DriverManager.getConnection(jdbcUrl).use { conn ->
            conn.autoCommit = false

            // DDL: Create Tables matching PocketHost Hungarian Schema
            val ddlStatements = listOf(
                """
                CREATE TABLE IF NOT EXISTS m_simulation_runs (
                    id TEXT PRIMARY KEY,
                    run_id TEXT NOT NULL UNIQUE,
                    season_year INTEGER NOT NULL,
                    iterations_count INTEGER NOT NULL,
                    simulation_seed INTEGER NOT NULL,
                    champion_team_id TEXT NOT NULL,
                    champion_team_name TEXT NOT NULL,
                    champion_win_probability REAL NOT NULL,
                    engine_version TEXT NOT NULL,
                    schema_version TEXT NOT NULL,
                    created_at_utc TEXT NOT NULL,
                    created_at_epoch_ms INTEGER NOT NULL
                );
                """.trimIndent(),
                """
                CREATE TABLE IF NOT EXISTS i_mlb_teams (
                    team_id TEXT PRIMARY KEY,
                    team_name TEXT NOT NULL,
                    abbreviation TEXT NOT NULL,
                    league TEXT NOT NULL,
                    division TEXT NOT NULL,
                    ballpark TEXT NOT NULL,
                    founded_year INTEGER NOT NULL
                );
                """.trimIndent(),
                """
                CREATE TABLE IF NOT EXISTS i_team_season_inputs (
                    id TEXT PRIMARY KEY,
                    team_id TEXT NOT NULL,
                    run_id TEXT NOT NULL,
                    wins_count INTEGER NOT NULL,
                    losses_count INTEGER NOT NULL,
                    runs_scored REAL NOT NULL,
                    runs_allowed REAL NOT NULL,
                    pythagorean_win_pct REAL NOT NULL,
                    baseruns_estimate REAL NOT NULL,
                    woba_offense REAL NOT NULL,
                    wrc_plus_offense REAL NOT NULL,
                    fip_pitching REAL NOT NULL,
                    top3_ace_era REAL NOT NULL,
                    bullpen_wpa REAL NOT NULL,
                    polymarket_consensus_pct REAL NOT NULL,
                    last10_win_pct REAL NOT NULL,
                    season_consistency_index REAL NOT NULL,
                    created_at_epoch_ms INTEGER NOT NULL,
                    FOREIGN KEY (team_id) REFERENCES i_mlb_teams(team_id)
                );
                """.trimIndent(),
                """
                CREATE TABLE IF NOT EXISTS m_latent_quality_estimates (
                    id TEXT PRIMARY KEY,
                    team_id TEXT NOT NULL,
                    run_id TEXT NOT NULL,
                    latent_quality_score REAL NOT NULL,
                    bayesian_adjusted_win_pct REAL NOT NULL,
                    recency_weighted_win_pct REAL NOT NULL,
                    war_normalized_pace REAL NOT NULL,
                    clubhouse_momentum_multiplier REAL NOT NULL,
                    created_at_epoch_ms INTEGER NOT NULL,
                    FOREIGN KEY (team_id) REFERENCES i_mlb_teams(team_id)
                );
                """.trimIndent(),
                """
                CREATE TABLE IF NOT EXISTS f_world_series_leaderboard (
                    id TEXT PRIMARY KEY,
                    team_id TEXT NOT NULL,
                    run_id TEXT NOT NULL,
                    simulated_rank INTEGER NOT NULL,
                    regular_season_rank INTEGER NOT NULL,
                    rank_movement_indicator TEXT NOT NULL,
                    projected_season_wins REAL NOT NULL,
                    playoff_make_probability REAL NOT NULL,
                    pennant_win_probability REAL NOT NULL,
                    world_series_win_probability REAL NOT NULL,
                    polymarket_consensus_probability REAL NOT NULL,
                    created_at_epoch_ms INTEGER NOT NULL,
                    FOREIGN KEY (team_id) REFERENCES i_mlb_teams(team_id)
                );
                """.trimIndent()
            )

            val ddlIndexStatements = listOf(
                "CREATE INDEX IF NOT EXISTS idx_f_leaderboard_ws ON f_world_series_leaderboard(world_series_win_probability DESC);",
                "CREATE INDEX IF NOT EXISTS idx_m_latent_quality ON m_latent_quality_estimates(latent_quality_score DESC);",
                "CREATE INDEX IF NOT EXISTS idx_i_team_inputs_run ON i_team_season_inputs(run_id, team_id);"
            )

            conn.createStatement().use { stmt ->
                for (ddl in ddlStatements) {
                    stmt.execute(ddl)
                    sqlBuilder.append(ddl).append("\n\n")
                }
                for (idx in ddlIndexStatements) {
                    stmt.execute(idx)
                    sqlBuilder.append(idx).append("\n")
                }
            }

            // 2. Insert m_simulation_runs
            val topTeam: TeamProbability = result.leaderboard.first()
            val runSql = """
                INSERT INTO m_simulation_runs (
                    id, run_id, season_year, iterations_count, simulation_seed,
                    champion_team_id, champion_team_name, champion_win_probability,
                    engine_version, schema_version, created_at_utc, created_at_epoch_ms
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

            conn.prepareStatement(runSql).use { ps ->
                ps.setString(1, "sim_$runId")
                ps.setString(2, runId)
                ps.setInt(3, TimeUtils.getSeasonYear(epochTimestampMs))
                ps.setInt(4, result.totalSimulations)
                ps.setLong(5, 42L)
                ps.setString(6, topTeam.team.teamId.name)
                ps.setString(7, topTeam.team.name)
                ps.setDouble(8, topTeam.worldSeriesWinProb)
                ps.setString(9, "2.4.0-KMP-2SLS")
                ps.setString(10, "1.0.0-hungarian")
                ps.setString(11, TimeUtils.formatIsoTimestampUtc(epochTimestampMs))
                ps.setLong(12, epochTimestampMs)
                ps.executeUpdate()
                rowsCount++
            }
            sqlBuilder.append("\n-- Simulation Runs\n")
            sqlBuilder.append("INSERT INTO m_simulation_runs VALUES ('sim_$runId', '$runId', ${TimeUtils.getSeasonYear(epochTimestampMs)}, ${result.totalSimulations}, 42, '${topTeam.team.teamId.name}', '${topTeam.team.name.replace("'", "''")}', ${topTeam.worldSeriesWinProb}, '2.4.0-KMP-2SLS', '1.0.0-hungarian', '${TimeUtils.formatIsoTimestampUtc(epochTimestampMs)}', $epochTimestampMs);\n\n")

            // 3. Insert i_mlb_teams
            val teamSql = """
                INSERT INTO i_mlb_teams (
                    team_id, team_name, abbreviation, league, division, ballpark, founded_year
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

            conn.prepareStatement(teamSql).use { ps ->
                for (t: MlbTeam in teams) {
                    ps.setString(1, t.teamId.name)
                    ps.setString(2, t.name)
                    ps.setString(3, t.teamId.name)
                    ps.setString(4, t.league.name)
                    ps.setString(5, t.division.name)
                    ps.setString(6, t.teamId.ballpark)
                    ps.setInt(7, t.teamId.foundedYear)
                    ps.executeUpdate()
                    rowsCount++

                    sqlBuilder.append("INSERT OR REPLACE INTO i_mlb_teams VALUES ('${t.teamId.name}', '${t.name.replace("'", "''")}', '${t.teamId.name}', '${t.league.name}', '${t.division.name}', '${t.teamId.ballpark.replace("'", "''")}', ${t.teamId.foundedYear});\n")
                }
            }

            // 4. Insert i_team_season_inputs
            val inputSql = """
                INSERT INTO i_team_season_inputs (
                    id, team_id, run_id, wins_count, losses_count, runs_scored, runs_allowed,
                    pythagorean_win_pct, baseruns_estimate, woba_offense, wrc_plus_offense,
                    fip_pitching, top3_ace_era, bullpen_wpa, polymarket_consensus_pct,
                    last10_win_pct, season_consistency_index, created_at_epoch_ms
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

            conn.prepareStatement(inputSql).use { ps ->
                for (t: MlbTeam in teams) {
                    val inputId = "inp_${runId}_${t.teamId.name.lowercase()}"
                    ps.setString(1, inputId)
                    ps.setString(2, t.teamId.name)
                    ps.setString(3, runId)
                    ps.setInt(4, t.wins)
                    ps.setInt(5, t.losses)
                    ps.setDouble(6, t.runsScored)
                    ps.setDouble(7, t.runsAllowed)
                    ps.setDouble(8, t.pythagoreanWinPct)
                    ps.setDouble(9, t.baseRunsEstimate)
                    ps.setDouble(10, t.wOBA)
                    ps.setDouble(11, t.wRCPlus)
                    ps.setDouble(12, t.fip)
                    ps.setDouble(13, t.top3AceEra)
                    ps.setDouble(14, t.bullpenWpa)
                    ps.setDouble(15, t.marketImpliedWsProb)
                    ps.setDouble(16, t.last10WinPct)
                    ps.setDouble(17, t.seasonConsistencyIndex)
                    ps.setLong(18, epochTimestampMs)
                    ps.executeUpdate()
                    rowsCount++

                    sqlBuilder.append("INSERT INTO i_team_season_inputs VALUES ('$inputId', '${t.teamId.name}', '$runId', ${t.wins}, ${t.losses}, ${t.runsScored}, ${t.runsAllowed}, ${t.pythagoreanWinPct}, ${t.baseRunsEstimate}, ${t.wOBA}, ${t.wRCPlus}, ${t.fip}, ${t.top3AceEra}, ${t.bullpenWpa}, ${t.marketImpliedWsProb}, ${t.last10WinPct}, ${t.seasonConsistencyIndex}, $epochTimestampMs);\n")
                }
            }

            // 5. Insert m_latent_quality_estimates
            val qualitySql = """
                INSERT INTO m_latent_quality_estimates (
                    id, team_id, run_id, latent_quality_score, bayesian_adjusted_win_pct,
                    recency_weighted_win_pct, war_normalized_pace, clubhouse_momentum_multiplier,
                    created_at_epoch_ms
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

            conn.prepareStatement(qualitySql).use { ps ->
                for (t: MlbTeam in teams) {
                    val qId = "qual_${runId}_${t.teamId.name.lowercase()}"
                    val qScore = WorldSeriesSimulator.computeLatentTeamQuality(t, momentumMap)
                    val warNorm = if (t.gamesPlayed > 0) (t.teamWar / t.gamesPlayed * 162.0) / 45.0 else 0.50
                    val mom = momentumMap[t.teamId] ?: 1.0

                    ps.setString(1, qId)
                    ps.setString(2, t.teamId.name)
                    ps.setString(3, runId)
                    ps.setDouble(4, qScore)
                    ps.setDouble(5, t.bayesianAdjustedWinPct)
                    ps.setDouble(6, t.recencyWeightedWinPct)
                    ps.setDouble(7, warNorm)
                    ps.setDouble(8, mom)
                    ps.setLong(9, epochTimestampMs)
                    ps.executeUpdate()
                    rowsCount++

                    sqlBuilder.append("INSERT INTO m_latent_quality_estimates VALUES ('$qId', '${t.teamId.name}', '$runId', $qScore, ${t.bayesianAdjustedWinPct}, ${t.recencyWeightedWinPct}, $warNorm, $mom, $epochTimestampMs);\n")
                }
            }

            // 6. Insert f_world_series_leaderboard
            val leaderSql = """
                INSERT INTO f_world_series_leaderboard (
                    id, team_id, run_id, simulated_rank, regular_season_rank, rank_movement_indicator,
                    projected_season_wins, playoff_make_probability, pennant_win_probability,
                    world_series_win_probability, polymarket_consensus_probability,
                    created_at_epoch_ms
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

            conn.prepareStatement(leaderSql).use { ps ->
                val leaderboardList: List<TeamProbability> = result.leaderboard
                for (idx in 0 until leaderboardList.size) {
                    val tp: TeamProbability = leaderboardList[idx]
                    val lId = "lead_${runId}_${tp.team.teamId.name.lowercase()}"

                    ps.setString(1, lId)
                    ps.setString(2, tp.team.teamId.name)
                    ps.setString(3, runId)
                    ps.setInt(4, tp.simRank)
                    ps.setInt(5, tp.regularSeasonRank)
                    ps.setString(6, tp.movementSymbol)
                    ps.setDouble(7, tp.expectedSeasonWins)
                    ps.setDouble(8, tp.playoffProb)
                    ps.setDouble(9, tp.pennantProb)
                    ps.setDouble(10, tp.worldSeriesWinProb)
                    ps.setDouble(11, tp.team.marketImpliedWsProb)
                    ps.setLong(12, epochTimestampMs)
                    ps.executeUpdate()
                    rowsCount++

                    sqlBuilder.append("INSERT INTO f_world_series_leaderboard VALUES ('$lId', '${tp.team.teamId.name}', '$runId', ${tp.simRank}, ${tp.regularSeasonRank}, '${tp.movementSymbol}', ${tp.expectedSeasonWins}, ${tp.playoffProb}, ${tp.pennantProb}, ${tp.worldSeriesWinProb}, ${tp.team.marketImpliedWsProb}, $epochTimestampMs);\n")
                }
            }

            conn.commit()
        }

        // Write SQLite SQL dump script
        sqlDumpFile.writeText(sqlBuilder.toString())

        // 7. Generate Idempotent PostgreSQL Seed Script
        val pgDumpFile = File(outputDir, "local_db_stack_postgres_seed.sql")
        val pgBuilder = StringBuilder()
        pgBuilder.append("""
            -- Auto-generated Idempotent PostgreSQL Seed Script for ~/personal/local-db-stack
            -- Target: local_postgres (port 15432, user: local_user, db: local_database)
            
            CREATE TABLE IF NOT EXISTS m_simulation_runs (
                id VARCHAR(60) PRIMARY KEY,
                run_id VARCHAR(60) NOT NULL UNIQUE,
                season_year INTEGER NOT NULL,
                iterations_count INTEGER NOT NULL,
                simulation_seed INTEGER NOT NULL,
                champion_team_id VARCHAR(10) NOT NULL,
                champion_team_name VARCHAR(60) NOT NULL,
                champion_win_probability DOUBLE PRECISION NOT NULL,
                engine_version VARCHAR(30) NOT NULL,
                schema_version VARCHAR(30) NOT NULL,
                created_at_utc VARCHAR(30) NOT NULL,
                created_at_epoch_ms BIGINT NOT NULL
            );

            CREATE TABLE IF NOT EXISTS i_mlb_teams (
                id VARCHAR(36) PRIMARY KEY,
                str_team_code VARCHAR(3) NOT NULL UNIQUE,
                str_team_name VARCHAR(60) NOT NULL,
                str_league VARCHAR(2) NOT NULL,
                str_division VARCHAR(10) NOT NULL,
                str_city VARCHAR(50) NOT NULL,
                str_ballpark VARCHAR(80) NOT NULL,
                int_founded_year INTEGER NOT NULL,
                int_mlb_api_id INTEGER NOT NULL UNIQUE,
                bool_is_active BOOLEAN NOT NULL DEFAULT TRUE,
                str_status_code VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                int_created_epoch_ms_utc BIGINT NOT NULL,
                int_updated_epoch_ms_utc BIGINT NOT NULL
            );

            CREATE TABLE IF NOT EXISTS i_team_season_inputs (
                id VARCHAR(60) PRIMARY KEY,
                team_id VARCHAR(3) NOT NULL REFERENCES i_mlb_teams(str_team_code),
                run_id VARCHAR(60) NOT NULL,
                wins_count INTEGER NOT NULL,
                losses_count INTEGER NOT NULL,
                runs_scored DOUBLE PRECISION NOT NULL,
                runs_allowed DOUBLE PRECISION NOT NULL,
                pythagorean_win_pct DOUBLE PRECISION NOT NULL,
                baseruns_estimate DOUBLE PRECISION NOT NULL,
                woba_offense DOUBLE PRECISION NOT NULL,
                wrc_plus_offense DOUBLE PRECISION NOT NULL,
                fip_pitching DOUBLE PRECISION NOT NULL,
                top3_ace_era DOUBLE PRECISION NOT NULL,
                bullpen_wpa DOUBLE PRECISION NOT NULL,
                polymarket_consensus_pct DOUBLE PRECISION NOT NULL,
                last10_win_pct DOUBLE PRECISION NOT NULL,
                season_consistency_index DOUBLE PRECISION NOT NULL,
                created_at_epoch_ms BIGINT NOT NULL
            );

            CREATE TABLE IF NOT EXISTS m_latent_quality_estimates (
                id VARCHAR(60) PRIMARY KEY,
                team_id VARCHAR(3) NOT NULL REFERENCES i_mlb_teams(str_team_code),
                run_id VARCHAR(60) NOT NULL,
                latent_quality_score DOUBLE PRECISION NOT NULL,
                bayesian_adjusted_win_pct DOUBLE PRECISION NOT NULL,
                recency_weighted_win_pct DOUBLE PRECISION NOT NULL,
                war_normalized_pace DOUBLE PRECISION NOT NULL,
                clubhouse_momentum_multiplier DOUBLE PRECISION NOT NULL,
                created_at_epoch_ms BIGINT NOT NULL
            );

            CREATE TABLE IF NOT EXISTS f_world_series_leaderboard (
                id VARCHAR(80) PRIMARY KEY,
                str_run_id VARCHAR(60) NOT NULL,
                str_team_code VARCHAR(3) NOT NULL REFERENCES i_mlb_teams(str_team_code),
                str_team_name VARCHAR(60) NOT NULL,
                str_league VARCHAR(2) NOT NULL,
                str_division VARCHAR(10) NOT NULL,
                int_sim_rank INTEGER NOT NULL,
                dbl_expected_season_wins DOUBLE PRECISION NOT NULL,
                dbl_playoff_prob DOUBLE PRECISION NOT NULL,
                dbl_pennant_prob DOUBLE PRECISION NOT NULL,
                dbl_world_series_win_prob DOUBLE PRECISION NOT NULL,
                str_visual_bar VARCHAR(20),
                bool_is_active BOOLEAN NOT NULL DEFAULT TRUE,
                str_status_code VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                int_created_epoch_ms_utc BIGINT NOT NULL,
                int_updated_epoch_ms_utc BIGINT NOT NULL
            );

            TRUNCATE TABLE f_world_series_leaderboard CASCADE;
            TRUNCATE TABLE m_latent_quality_estimates CASCADE;
            TRUNCATE TABLE i_team_season_inputs CASCADE;

        """.trimIndent()).append("\n\n")

        val topTeam = result.leaderboard.first()
        pgBuilder.append("INSERT INTO m_simulation_runs VALUES ('sim_$runId', '$runId', ${TimeUtils.getSeasonYear(epochTimestampMs)}, ${result.totalSimulations}, 42, '${topTeam.team.teamId.name}', '${topTeam.team.name.replace("'", "''")}', ${topTeam.worldSeriesWinProb}, '2.4.0-KMP-2SLS', '1.0.0-hungarian', '${TimeUtils.formatIsoTimestampUtc(epochTimestampMs)}', $epochTimestampMs) ON CONFLICT (run_id) DO UPDATE SET champion_win_probability = EXCLUDED.champion_win_probability;\n\n")

        for (t in teams) {
            pgBuilder.append("INSERT INTO i_mlb_teams (id, str_team_code, str_team_name, str_league, str_division, str_city, str_ballpark, int_founded_year, int_mlb_api_id, bool_is_active, str_status_code, int_created_epoch_ms_utc, int_updated_epoch_ms_utc) VALUES ('${t.teamId.name}', '${t.teamId.name}', '${t.name.replace("'", "''")}', '${t.league.name}', '${t.division.name}', '${t.teamId.city.replace("'", "''")}', '${t.teamId.ballpark.replace("'", "''")}', ${t.teamId.foundedYear}, ${t.teamId.mlbApiId}, TRUE, 'ACTIVE', $epochTimestampMs, $epochTimestampMs) ON CONFLICT (str_team_code) DO UPDATE SET str_team_name = EXCLUDED.str_team_name, int_updated_epoch_ms_utc = EXCLUDED.int_updated_epoch_ms_utc;\n")
        }
        pgBuilder.append("\n")

        for (t in teams) {
            val inputId = "inp_${runId}_${t.teamId.name.lowercase()}"
            pgBuilder.append("INSERT INTO i_team_season_inputs VALUES ('$inputId', '${t.teamId.name}', '$runId', ${t.wins}, ${t.losses}, ${t.runsScored}, ${t.runsAllowed}, ${t.pythagoreanWinPct}, ${t.baseRunsEstimate}, ${t.wOBA}, ${t.wRCPlus}, ${t.fip}, ${t.top3AceEra}, ${t.bullpenWpa}, ${t.marketImpliedWsProb}, ${t.last10WinPct}, ${t.seasonConsistencyIndex}, $epochTimestampMs) ON CONFLICT (id) DO NOTHING;\n")
        }
        pgBuilder.append("\n")

        for (t in teams) {
            val qId = "qual_${runId}_${t.teamId.name.lowercase()}"
            val qScore = WorldSeriesSimulator.computeLatentTeamQuality(t, momentumMap)
            val warNorm = if (t.gamesPlayed > 0) (t.teamWar / t.gamesPlayed * 162.0) / 45.0 else 0.50
            val mom = momentumMap[t.teamId] ?: 1.0
            pgBuilder.append("INSERT INTO m_latent_quality_estimates VALUES ('$qId', '${t.teamId.name}', '$runId', $qScore, ${t.bayesianAdjustedWinPct}, ${t.recencyWeightedWinPct}, $warNorm, $mom, $epochTimestampMs) ON CONFLICT (id) DO NOTHING;\n")
        }
        pgBuilder.append("\n")

        for (tp in result.leaderboard) {
            val lId = "lead_${runId}_${tp.team.teamId.name.lowercase()}"
            pgBuilder.append("INSERT INTO f_world_series_leaderboard (id, str_run_id, str_team_code, str_team_name, str_league, str_division, int_sim_rank, dbl_expected_season_wins, dbl_playoff_prob, dbl_pennant_prob, dbl_world_series_win_prob, str_visual_bar, bool_is_active, str_status_code, int_created_epoch_ms_utc, int_updated_epoch_ms_utc) VALUES ('$lId', '$runId', '${tp.team.teamId.name}', '${tp.team.name.replace("'", "''")}', '${tp.team.league.name}', '${tp.team.division.name}', ${tp.simRank}, ${tp.expectedSeasonWins}, ${tp.playoffProb}, ${tp.pennantProb}, ${tp.worldSeriesWinProb}, '${tp.movementSymbol}', TRUE, 'ACTIVE', $epochTimestampMs, $epochTimestampMs) ON CONFLICT (id) DO UPDATE SET dbl_world_series_win_prob = EXCLUDED.dbl_world_series_win_prob, int_updated_epoch_ms_utc = EXCLUDED.int_updated_epoch_ms_utc;\n")
        }

        pgBuilder.append("""

            CREATE OR REPLACE VIEW vw_latest_active_world_series_leaderboard AS
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
                to_timestamp(l.int_created_epoch_ms_utc / 1000.0) AT TIME ZONE 'America/Chicago' AS updated_local
            FROM f_world_series_leaderboard l
            JOIN i_mlb_teams t ON l.str_team_code = t.str_team_code
            WHERE l.bool_is_active = TRUE
            ORDER BY l.int_sim_rank ASC;
        """.trimIndent())
        pgDumpFile.writeText(pgBuilder.toString())

        // 8. Generate Idempotent MySQL Seed Script
        val mysqlDumpFile = File(outputDir, "local_db_stack_mysql_seed.sql")
        val mysqlBuilder = StringBuilder()
        mysqlBuilder.append("""
            -- Auto-generated Idempotent MySQL Seed Script for ~/personal/local-db-stack
            -- Target: local_mysql (port 13306, user: local_user, db: local_database)
            
            USE local_database;

            CREATE TABLE IF NOT EXISTS m_simulation_runs (
                id VARCHAR(60) PRIMARY KEY,
                run_id VARCHAR(60) NOT NULL UNIQUE,
                season_year INT NOT NULL,
                iterations_count INT NOT NULL,
                simulation_seed INT NOT NULL,
                champion_team_id VARCHAR(10) NOT NULL,
                champion_team_name VARCHAR(60) NOT NULL,
                champion_win_probability DOUBLE NOT NULL,
                engine_version VARCHAR(30) NOT NULL,
                schema_version VARCHAR(30) NOT NULL,
                created_at_utc VARCHAR(30) NOT NULL,
                created_at_epoch_ms BIGINT NOT NULL
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

            CREATE TABLE IF NOT EXISTS i_mlb_teams (
                id VARCHAR(36) PRIMARY KEY,
                str_team_code VARCHAR(3) NOT NULL UNIQUE,
                str_team_name VARCHAR(60) NOT NULL,
                str_league VARCHAR(2) NOT NULL,
                str_division VARCHAR(10) NOT NULL,
                str_city VARCHAR(50) NOT NULL,
                str_ballpark VARCHAR(80) NOT NULL,
                int_founded_year INT NOT NULL,
                int_mlb_api_id INT NOT NULL UNIQUE,
                bool_is_active BOOLEAN NOT NULL DEFAULT TRUE,
                str_status_code VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                int_created_epoch_ms_utc BIGINT NOT NULL,
                int_updated_epoch_ms_utc BIGINT NOT NULL
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

            CREATE TABLE IF NOT EXISTS i_team_season_inputs (
                id VARCHAR(60) PRIMARY KEY,
                team_id VARCHAR(3) NOT NULL,
                run_id VARCHAR(60) NOT NULL,
                wins_count INT NOT NULL,
                losses_count INT NOT NULL,
                runs_scored DOUBLE NOT NULL,
                runs_allowed DOUBLE NOT NULL,
                pythagorean_win_pct DOUBLE NOT NULL,
                baseruns_estimate DOUBLE NOT NULL,
                woba_offense DOUBLE NOT NULL,
                wrc_plus_offense DOUBLE NOT NULL,
                fip_pitching DOUBLE NOT NULL,
                top3_ace_era DOUBLE NOT NULL,
                bullpen_wpa DOUBLE NOT NULL,
                polymarket_consensus_pct DOUBLE NOT NULL,
                last10_win_pct DOUBLE NOT NULL,
                season_consistency_index DOUBLE NOT NULL,
                created_at_epoch_ms BIGINT NOT NULL,
                FOREIGN KEY (team_id) REFERENCES i_mlb_teams(str_team_code)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

            CREATE TABLE IF NOT EXISTS m_latent_quality_estimates (
                id VARCHAR(60) PRIMARY KEY,
                team_id VARCHAR(3) NOT NULL,
                run_id VARCHAR(60) NOT NULL,
                latent_quality_score DOUBLE NOT NULL,
                bayesian_adjusted_win_pct DOUBLE NOT NULL,
                recency_weighted_win_pct DOUBLE NOT NULL,
                war_normalized_pace DOUBLE NOT NULL,
                clubhouse_momentum_multiplier DOUBLE NOT NULL,
                created_at_epoch_ms BIGINT NOT NULL,
                FOREIGN KEY (team_id) REFERENCES i_mlb_teams(str_team_code)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

            CREATE TABLE IF NOT EXISTS f_world_series_leaderboard (
                id VARCHAR(80) PRIMARY KEY,
                str_run_id VARCHAR(60) NOT NULL,
                str_team_code VARCHAR(3) NOT NULL,
                str_team_name VARCHAR(60) NOT NULL,
                str_league VARCHAR(2) NOT NULL,
                str_division VARCHAR(10) NOT NULL,
                int_sim_rank INT NOT NULL,
                dbl_expected_season_wins DOUBLE NOT NULL,
                dbl_playoff_prob DOUBLE NOT NULL,
                dbl_pennant_prob DOUBLE NOT NULL,
                dbl_world_series_win_prob DOUBLE NOT NULL,
                str_visual_bar VARCHAR(20),
                bool_is_active BOOLEAN NOT NULL DEFAULT TRUE,
                str_status_code VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                int_created_epoch_ms_utc BIGINT NOT NULL,
                int_updated_epoch_ms_utc BIGINT NOT NULL,
                FOREIGN KEY (str_team_code) REFERENCES i_mlb_teams(str_team_code)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

            DELETE FROM f_world_series_leaderboard;
            DELETE FROM m_latent_quality_estimates;
            DELETE FROM i_team_season_inputs;

        """.trimIndent()).append("\n\n")

        mysqlBuilder.append("INSERT INTO m_simulation_runs VALUES ('sim_$runId', '$runId', ${TimeUtils.getSeasonYear(epochTimestampMs)}, ${result.totalSimulations}, 42, '${topTeam.team.teamId.name}', '${topTeam.team.name.replace("'", "''")}', ${topTeam.worldSeriesWinProb}, '2.4.0-KMP-2SLS', '1.0.0-hungarian', '${TimeUtils.formatIsoTimestampUtc(epochTimestampMs)}', $epochTimestampMs) ON DUPLICATE KEY UPDATE champion_win_probability = VALUES(champion_win_probability);\n\n")

        for (t in teams) {
            mysqlBuilder.append("INSERT INTO i_mlb_teams (id, str_team_code, str_team_name, str_league, str_division, str_city, str_ballpark, int_founded_year, int_mlb_api_id, bool_is_active, str_status_code, int_created_epoch_ms_utc, int_updated_epoch_ms_utc) VALUES ('${t.teamId.name}', '${t.teamId.name}', '${t.name.replace("'", "''")}', '${t.league.name}', '${t.division.name}', '${t.teamId.city.replace("'", "''")}', '${t.teamId.ballpark.replace("'", "''")}', ${t.teamId.foundedYear}, ${t.teamId.mlbApiId}, TRUE, 'ACTIVE', $epochTimestampMs, $epochTimestampMs) ON DUPLICATE KEY UPDATE str_team_name = VALUES(str_team_name), int_updated_epoch_ms_utc = VALUES(int_updated_epoch_ms_utc);\n")
        }
        mysqlBuilder.append("\n")

        for (t in teams) {
            val inputId = "inp_${runId}_${t.teamId.name.lowercase()}"
            mysqlBuilder.append("INSERT INTO i_team_season_inputs VALUES ('$inputId', '${t.teamId.name}', '$runId', ${t.wins}, ${t.losses}, ${t.runsScored}, ${t.runsAllowed}, ${t.pythagoreanWinPct}, ${t.baseRunsEstimate}, ${t.wOBA}, ${t.wRCPlus}, ${t.fip}, ${t.top3AceEra}, ${t.bullpenWpa}, ${t.marketImpliedWsProb}, ${t.last10WinPct}, ${t.seasonConsistencyIndex}, $epochTimestampMs) ON DUPLICATE KEY UPDATE wrc_plus_offense = VALUES(wrc_plus_offense);\n")
        }
        mysqlBuilder.append("\n")

        for (t in teams) {
            val qId = "qual_${runId}_${t.teamId.name.lowercase()}"
            val qScore = WorldSeriesSimulator.computeLatentTeamQuality(t, momentumMap)
            val warNorm = if (t.gamesPlayed > 0) (t.teamWar / t.gamesPlayed * 162.0) / 45.0 else 0.50
            val mom = momentumMap[t.teamId] ?: 1.0
            mysqlBuilder.append("INSERT INTO m_latent_quality_estimates VALUES ('$qId', '${t.teamId.name}', '$runId', $qScore, ${t.bayesianAdjustedWinPct}, ${t.recencyWeightedWinPct}, $warNorm, $mom, $epochTimestampMs) ON DUPLICATE KEY UPDATE latent_quality_score = VALUES(latent_quality_score);\n")
        }
        mysqlBuilder.append("\n")

        for (tp in result.leaderboard) {
            val lId = "lead_${runId}_${tp.team.teamId.name.lowercase()}"
            mysqlBuilder.append("INSERT INTO f_world_series_leaderboard (id, str_run_id, str_team_code, str_team_name, str_league, str_division, int_sim_rank, dbl_expected_season_wins, dbl_playoff_prob, dbl_pennant_prob, dbl_world_series_win_prob, str_visual_bar, bool_is_active, str_status_code, int_created_epoch_ms_utc, int_updated_epoch_ms_utc) VALUES ('$lId', '$runId', '${tp.team.teamId.name}', '${tp.team.name.replace("'", "''")}', '${tp.team.league.name}', '${tp.team.division.name}', ${tp.simRank}, ${tp.expectedSeasonWins}, ${tp.playoffProb}, ${tp.pennantProb}, ${tp.worldSeriesWinProb}, '${tp.movementSymbol}', TRUE, 'ACTIVE', $epochTimestampMs, $epochTimestampMs) ON DUPLICATE KEY UPDATE dbl_world_series_win_prob = VALUES(dbl_world_series_win_prob), int_updated_epoch_ms_utc = VALUES(int_updated_epoch_ms_utc);\n")
        }

        mysqlBuilder.append("""

            CREATE OR REPLACE VIEW vw_latest_active_world_series_leaderboard AS
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
                FROM_UNIXTIME(l.int_created_epoch_ms_utc / 1000) AS updated_local
            FROM f_world_series_leaderboard l
            JOIN i_mlb_teams t ON l.str_team_code = t.str_team_code
            WHERE l.bool_is_active = TRUE
            ORDER BY l.int_sim_rank ASC;
        """.trimIndent())
        mysqlDumpFile.writeText(mysqlBuilder.toString())

        // Automatically sync artifacts to ~/personal/local-db-stack/sqlite and ~/.local-db-stack/data/sqlite if present
        val userHome = System.getProperty("user.home") ?: ""
        val localStackTargets = listOf(
            File("$userHome/personal/local-db-stack/sqlite"),
            File("$userHome/.local-db-stack/data/sqlite")
        )

        for (targetDir in localStackTargets) {
            try {
                if (!targetDir.exists()) targetDir.mkdirs()
                sqliteFile.copyTo(File(targetDir, sqliteFile.name), overwrite = true)
                sqlDumpFile.copyTo(File(targetDir, sqlDumpFile.name), overwrite = true)
                pgDumpFile.copyTo(File(targetDir, pgDumpFile.name), overwrite = true)
                mysqlDumpFile.copyTo(File(targetDir, mysqlDumpFile.name), overwrite = true)
                val payloadFile = File(outputDir, "pockethost_sync_payload.json")
                if (payloadFile.exists()) {
                    payloadFile.copyTo(File(targetDir, payloadFile.name), overwrite = true)
                }
            } catch (e: Exception) {
                // Log and continue gracefully
            }
        }

        val duration = System.currentTimeMillis() - startTime
        return LocalDatabaseReport(
            sqliteFile = sqliteFile,
            sqlDumpFile = sqlDumpFile,
            totalTablesCreated = 5,
            totalRowsInserted = rowsCount,
            isSuccessful = true,
            executionTimeMs = duration
        )
    }
}
