#!/usr/bin/env python3
"""
PocketHost / PocketBase Automated Database Backup, JSON Dump, & Local SQLite Replicator.
Exports all Hungarian-prefixed collections and master datasets from PocketHost, generates
a timestamped JSON backup archive, and populates a standalone local SQLite database (mlb_sabermetric_local.db)
for zero-dependency offline SQL analytics and local PocketBase deployment.
"""
from __future__ import annotations

import os
import sys
import json
import time
import sqlite3
import datetime
import urllib.request
import urllib.parse
from dataclasses import dataclass
from typing import Any, Dict, Final, List, Mapping, Optional, Sequence, TextIO, Tuple, cast

sys.path.insert(0, os.path.dirname(__file__))
from domain_registry import MLB_REGISTRY, League, Division, MlbTeamCode

POCKETHOST_URL: Final[str] = "https://mlb-sabermetric-worldseries.pockethost.io"
def _load_env_credentials() -> Tuple[Optional[str], Optional[str]]:
    """Loads POCKETHOST_ADMIN_EMAIL and POCKETHOST_ADMIN_PASSWORD from ~/.env or .env."""
    email: Optional[str] = os.getenv("POCKETHOST_ADMIN_EMAIL")
    password: Optional[str] = os.getenv("POCKETHOST_ADMIN_PASSWORD")
    
    env_paths: List[str] = [
        os.path.join(os.path.dirname(__file__), "..", ".env"),
        os.path.expanduser("~/.env")
    ]
    env_loc: str
    for env_loc in env_paths:
        if os.path.exists(env_loc):
            f_env: TextIO
            with open(env_loc, "r", encoding="utf-8") as f_env:
                line: str
                for line in f_env:
                    stripped_line: str = line.strip()
                    if stripped_line and not stripped_line.startswith("#") and "=" in stripped_line:
                        k: str
                        v: str
                        k, v = stripped_line.split("=", 1)
                        k_clean: str = k.strip()
                        v_clean: str = v.strip().strip('"').strip("'")
                        if k_clean == "POCKETHOST_ADMIN_EMAIL" and not email:
                            email = v_clean
                        elif k_clean == "POCKETHOST_ADMIN_PASSWORD" and not password:
                            password = v_clean
    return email, password


ADMIN_EMAIL, ADMIN_PASSWORD = _load_env_credentials()
OUTPUT_DIR: Final[str] = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "output_datasets"))
SQL_SCHEMA_FILE: Final[str] = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "docs", "schema", "mlb_domain_schema.sql"))
LOCAL_DB_FILE: Final[str] = os.path.join(OUTPUT_DIR, "mlb_sabermetric_local.db")


def authenticate_admin() -> str:
    """Authenticates admin credentials to access PocketHost."""
    if not ADMIN_EMAIL or not ADMIN_PASSWORD:
        print("❌ Error: Missing POCKETHOST_ADMIN_EMAIL or POCKETHOST_ADMIN_PASSWORD.")
        sys.exit(1)

    for attempt in range(5):
        for auth_ep in ["/api/collections/_superusers/auth-with-password", "/api/admins/auth-with-password"]:
            try:
                payload: bytes = json.dumps({"identity": ADMIN_EMAIL, "password": ADMIN_PASSWORD}).encode("utf-8")
                req = urllib.request.Request(
                    f"{POCKETHOST_URL}{auth_ep}",
                    data=payload,
                    headers={"Content-Type": "application/json", "User-Agent": "MLB-Sabermetric-KMP/2.6"},
                    method="POST"
                )
                with urllib.request.urlopen(req, timeout=10) as resp:
                    res = json.loads(resp.read().decode("utf-8"))
                    if isinstance(res, dict) and "token" in res:
                        return str(res["token"])
            except Exception:
                continue
        if attempt < 4:
            time.sleep(2.0)
    print("❌ Failed to authenticate with PocketHost.")
    sys.exit(1)


def fetch_all_collection_records(collection_name: str, token: str) -> List[Dict[str, Any]]:
    """Paginates and fetches all records from a specific PocketHost collection."""
    all_records: List[Dict[str, Any]] = []
    page: int = 1
    per_page: int = 100
    headers: Dict[str, str] = {
        "Authorization": token,
        "Accept": "application/json",
        "User-Agent": "MLB-Sabermetric-KMP/2.6"
    }
    while True:
        url: str = f"{POCKETHOST_URL}/api/collections/{collection_name}/records?page={page}&perPage={per_page}"
        req = urllib.request.Request(url, headers=headers, method="GET")
        try:
            with urllib.request.urlopen(req, timeout=15) as resp:
                res = json.loads(resp.read().decode("utf-8"))
                items = res.get("items", [])
                if not items:
                    break
                all_records.extend(items)
                if len(items) < per_page:
                    break
                page += 1
        except Exception:
            # Collection may not exist or be empty
            break
    return all_records


def export_full_database_json(token: str) -> Tuple[str, Dict[str, List[Dict[str, Any]]]]:
    """Exports all known collections to a unified JSON backup file."""
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    target_collections: List[str] = [
        "i_mlb_teams", "i_team_season_inputs", "i_market_odds_inputs", "i_expert_media_rankings",
        "m_simulation_runs", "m_latent_quality_estimates", "m_four_pillar_metrics",
        "s_divisional_standings_aggregates", "s_league_aggregates", "s_head_to_head_matrices",
        "o_playoff_series_simulations", "o_rank_movements",
        "f_world_series_leaderboard", "f_cubs_scenario_analysis",
        "tbl_mlb_teams", "tbl_simulation_runs", "tbl_team_snapshots", "tbl_rank_movements"
    ]

    backup_payload: Dict[str, Any] = {
        "metadata": {
            "source_instance": POCKETHOST_URL,
            "export_timestamp_utc": datetime.datetime.now(datetime.timezone.utc).isoformat(),
            "export_epoch_ms_utc": int(time.time() * 1000),
            "engine_version": "MLB-Sabermetric-KMP/2026.1"
        },
        "collections": {}
    }

    print("📦 Exporting PocketHost collections to JSON backup...")
    col_name: str
    for col_name in target_collections:
        recs = fetch_all_collection_records(col_name, token)
        if recs:
            backup_payload["collections"][col_name] = recs
            print(f"   • Fetched {len(recs):>3} records from `{col_name}`")

    timestamp_str: str = datetime.datetime.now(datetime.timezone.utc).strftime("%Y%m%d_%H%M%S")
    backup_file_path: str = os.path.join(OUTPUT_DIR, f"pockethost_backup_{timestamp_str}.json")
    latest_file_path: str = os.path.join(OUTPUT_DIR, "pockethost_backup_latest.json")

    with open(backup_file_path, "w", encoding="utf-8") as f:
        json.dump(backup_payload, f, indent=2)
    with open(latest_file_path, "w", encoding="utf-8") as f:
        json.dump(backup_payload, f, indent=2)

    print(f"✅ JSON Database Backup saved to:\n   • {backup_file_path}\n   • {latest_file_path}")
    return latest_file_path, backup_payload["collections"]


# --- Helper utilities to reduce duplication ---

def _get_str(r: Mapping[str, Any], key: str, default: str = "", upper: bool = False) -> str:
    v = r.get(key, default) or default
    s = str(v)
    return s.strip().upper() if upper else s.strip()


def _get_int(r: Mapping[str, Any], key: str, default: int = 0) -> int:
    try:
        return int(r.get(key, default))
    except Exception:
        return int(default)


def _get_float(r: Mapping[str, Any], key: str, default: float = 0.0) -> float:
    try:
        return float(r.get(key, default))
    except Exception:
        return float(default)


def _get_bool_int(r: Mapping[str, Any], key: str, default: bool = True) -> int:
    v = r.get(key, default)
    try:
        return 1 if bool(v) else 0
    except Exception:
        return 1 if default else 0


def _insert(cursor: sqlite3.Cursor, table: str, columns: Sequence[str], values: Sequence[Any]) -> None:
    placeholders = ", ".join(["?"] * len(columns))
    cols = ", ".join(columns)
    sql = f"INSERT OR REPLACE INTO {table} ({cols}) VALUES ({placeholders})"
    cursor.execute(sql, values)


def replicate_to_local_sqlite(collections: Mapping[str, Sequence[Mapping[str, Any]]]) -> str:
    """Builds and populates a standalone local SQLite database from exported PocketHost data."""
    print(f"\n🗄️  Replicating exported data to local SQLite database: `{LOCAL_DB_FILE}`...")
    if os.path.exists(LOCAL_DB_FILE):
        os.remove(LOCAL_DB_FILE)

    conn: sqlite3.Connection = sqlite3.connect(LOCAL_DB_FILE)
    cursor: sqlite3.Cursor = conn.cursor()

    # Apply DDL schema
    if os.path.exists(SQL_SCHEMA_FILE):
        with open(SQL_SCHEMA_FILE, "r", encoding="utf-8") as f:
            ddl: str = f.read()
            cursor.executescript(ddl)

    # Ingest i_mlb_teams
    teams_recs = collections.get("i_mlb_teams", [])
    for r in teams_recs:
        code = _get_str(r, "str_team_code", "", upper=True)
        if not code:
            continue
        try:
            meta = MLB_REGISTRY.get_team(code)
            mlb_id = _get_int(r, "int_mlb_api_id", meta.mlb_api_id)
            city = _get_str(r, "str_city", meta.city)
            ballpark = _get_str(r, "str_ballpark", meta.ballpark)
            founded = _get_int(r, "int_founded_year", meta.founded_year)
            lg = _get_str(r, "str_league", meta.league.value, upper=True)
            div = _get_str(r, "str_division", meta.division.value, upper=True)
            name = _get_str(r, "str_team_name", meta.full_name)
        except Exception:
            mlb_id = _get_int(r, "int_mlb_api_id", 0)
            city = _get_str(r, "str_city", "")
            ballpark = _get_str(r, "str_ballpark", "")
            founded = _get_int(r, "int_founded_year", 1901)
            lg = _get_str(r, "str_league", "AL", upper=True)
            div = _get_str(r, "str_division", "EAST", upper=True)
            name = _get_str(r, "str_team_name", "")

        _insert(cursor, "i_mlb_teams", [
            "id", "str_team_code", "str_team_name", "str_league", "str_division", "str_city",
            "str_ballpark", "int_founded_year", "int_mlb_api_id", "bool_is_active",
            "str_status_code", "int_created_epoch_ms_utc", "int_updated_epoch_ms_utc"
        ], [
            _get_str(r, "id", ""), code, name, lg, div, city, ballpark, founded, mlb_id,
            _get_bool_int(r, "bool_is_active", True), _get_str(r, "str_status_code", "ACTIVE", upper=True),
            _get_int(r, "int_created_epoch_ms_utc", 0), _get_int(r, "int_updated_epoch_ms_utc", 0)
        ])

    # Ingest m_simulation_runs
    sim_recs = collections.get("m_simulation_runs", [])
    for r in sim_recs:
        _insert(cursor, "m_simulation_runs", [
            "id", "str_run_id", "dt_run_timestamp", "int_season_year", "int_total_iterations",
            "int_random_seed", "str_engine_version", "str_top_favorite_code",
            "dbl_top_favorite_prob", "str_causal_iv_status", "bool_is_active",
            "str_status_code", "int_created_epoch_ms_utc", "int_updated_epoch_ms_utc"
        ], [
            _get_str(r, "id", ""), _get_str(r, "str_run_id", ""), _get_str(r, "dt_run_timestamp", ""),
            _get_int(r, "int_season_year", 2026), _get_int(r, "int_total_iterations", 10000),
            _get_int(r, "int_random_seed", 0), _get_str(r, "str_engine_version", ""),
            _get_str(r, "str_top_favorite_code", "LAD", upper=True), _get_float(r, "dbl_top_favorite_prob", 0.0),
            _get_str(r, "str_causal_iv_status", "ACTIVE"), _get_bool_int(r, "bool_is_active", True),
            _get_str(r, "str_status_code", "ACTIVE", upper=True), _get_int(r, "int_created_epoch_ms_utc", 0),
            _get_int(r, "int_updated_epoch_ms_utc", 0)
        ])

    # Ingest f_world_series_leaderboard
    lb_recs = collections.get("f_world_series_leaderboard", [])
    for r in lb_recs:
        _insert(cursor, "f_world_series_leaderboard", [
            "id", "str_run_id", "str_team_code", "str_team_name", "str_league", "str_division",
            "int_sim_rank", "dbl_expected_season_wins", "dbl_playoff_prob",
            "dbl_pennant_prob", "dbl_world_series_win_prob", "str_visual_bar",
            "bool_is_active", "str_status_code", "int_created_epoch_ms_utc", "int_updated_epoch_ms_utc"
        ], [
            _get_str(r, "id", ""), _get_str(r, "str_run_id", ""), _get_str(r, "str_team_code", "", upper=True),
            _get_str(r, "str_team_name", ""), _get_str(r, "str_league", "AL", upper=True),
            _get_str(r, "str_division", "EAST", upper=True), _get_int(r, "int_sim_rank", 1),
            _get_float(r, "dbl_expected_season_wins", 0.0), _get_float(r, "dbl_playoff_prob", 0.0),
            _get_float(r, "dbl_pennant_prob", 0.0), _get_float(r, "dbl_world_series_win_prob", 0.0),
            _get_str(r, "str_visual_bar", ""), _get_bool_int(r, "bool_is_active", True),
            _get_str(r, "str_status_code", "ACTIVE", upper=True), _get_int(r, "int_created_epoch_ms_utc", 0),
            _get_int(r, "int_updated_epoch_ms_utc", 0)
        ])

    conn.commit()

    # Query view to test replication
    cursor.execute("SELECT COUNT(*) FROM vw_latest_active_world_series_leaderboard")
    count = cursor.fetchone()[0]
    conn.close()

    print(f"✅ Local SQLite Database built successfully with {count} active leaderboard records.")
    return LOCAL_DB_FILE


def main() -> None:
    print("================================================================================")
    print(" 💾 POCKETHOST DATABASE BACKUP, EXPORT & LOCAL SQLITE REPLICATOR")
    print(f"    Target Source: {POCKETHOST_URL}")
    print("================================================================================")

    token: str = authenticate_admin()
    json_path, collections = export_full_database_json(token)
    db_path = replicate_to_local_sqlite(collections)

    print("\n================================================================================")
    print(" 🎉 BACKUP & REPLICATION COMPLETE!")
    print(f"    • Unified JSON Backup: {json_path}")
    print(f"    • Local SQLite Database: {db_path}")
    print("    • How to query locally with SQLite CLI:")
    print(f"        sqlite3 {db_path} 'SELECT * FROM vw_latest_active_world_series_leaderboard LIMIT 10;'")
    print("================================================================================\n")


if __name__ == "__main__":
    main()
