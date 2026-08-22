#!/usr/bin/env python3
"""
PocketHost / PocketBase Programmatic Querying & Analytics Utility.
Demonstrates strongly-typed REST queries against Hungarian-prefixed collections (i_, m_, s_, o_, f_),
filtering for latest-active records, sorting by epoch timestamp, and extracting multi-dimensional
sabermetric indicators across the 30 MLB franchises.
"""
from __future__ import annotations

import os
import sys
import json
import urllib.request
import urllib.parse
from dataclasses import dataclass
from typing import Any, Dict, Final, List, Mapping, Optional, Sequence, TextIO, Tuple, cast

sys.path.insert(0, os.path.dirname(__file__))
from domain_registry import MLB_REGISTRY, League, Division, MlbTeamCode, StatPillarType

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


def http_get(endpoint: str, query_params: Optional[Mapping[str, str]] = None, token: Optional[str] = None) -> Dict[str, Any]:
    """Executes a typed GET request to PocketHost with query parameters."""
    url: str = f"{POCKETHOST_URL}{endpoint}"
    if query_params:
        url += "?" + urllib.parse.urlencode(query_params)
    headers: Dict[str, str] = {
        "User-Agent": "MLB-Sabermetric-KMP/2.6",
        "Accept": "application/json"
    }
    if token:
        headers["Authorization"] = token
    req = urllib.request.Request(url, headers=headers, method="GET")
    with urllib.request.urlopen(req, timeout=15) as resp:
        return cast(Dict[str, Any], json.loads(resp.read().decode("utf-8")))


def authenticate_admin() -> Optional[str]:
    """Authenticates admin credentials to access PocketHost collections."""
    if not ADMIN_EMAIL or not ADMIN_PASSWORD:
        return None
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
    return None


# ==============================================================================
# Query Demonstrations
# ==============================================================================

def query_latest_simulation_run(token: Optional[str]) -> Optional[Dict[str, Any]]:
    """1. Query the most recent 10k Monte Carlo simulation run record."""
    print("▶ 1. Querying Latest Authoritative Simulation Run (`m_simulation_runs`)...")
    params: Dict[str, str] = {
        "filter": "(bool_is_active=true)",
        "sort": "-int_updated_epoch_ms_utc",
        "limit": "1"
    }
    res: Dict[str, Any] = http_get("/api/collections/m_simulation_runs/records", params, token)
    items: List[Dict[str, Any]] = res.get("items", [])
    if items:
        run = items[0]
        print(f"   • Run ID: {run.get('str_run_id')}")
        print(f"   • Timestamp UTC: {run.get('dt_run_timestamp')}")
        print(f"   • Top Favorite: {run.get('str_top_favorite_code')} ({(float(run.get('dbl_top_favorite_prob', 0))*100):.2f}%)")
        print(f"   • Engine Version: {run.get('str_engine_version')}")
        return run
    print("   ⚠️ No active simulation runs found.")
    return None


def query_championship_leaderboard_top(token: Optional[str], run_id: Optional[str] = None, top_n: int = 10) -> Sequence[Dict[str, Any]]:
    """2. Query top N championship contenders from `f_world_series_leaderboard`."""
    print(f"\n▶ 2. Querying Top {top_n} World Series Contenders (`f_world_series_leaderboard`)...")
    flt: str = f"(str_run_id='{run_id}' && bool_is_active=true)" if run_id else "(bool_is_active=true)"
    params: Dict[str, str] = {
        "filter": flt,
        "sort": "int_sim_rank",
        "perPage": str(top_n)
    }
    res: Dict[str, Any] = http_get("/api/collections/f_world_series_leaderboard/records", params, token)
    items: List[Dict[str, Any]] = res.get("items", [])
    print("   " + "-" * 80)
    print(f"   {'Rank':<5} | {'Team':<24} | {'Lg/Div':<8} | {'Exp Wins':<9} | {'Pennant%':<9} | {'WS Win%':<8} | {'Visual'}")
    print("   " + "-" * 80)
    for it in items:
        rank = it.get("int_sim_rank", "-")
        name = it.get("str_team_name", "-")
        lg_div = f"{it.get('str_league', '')}-{it.get('str_division', '')[:1]}"
        wins = f"{float(it.get('dbl_expected_season_wins', 0)):.1f}"
        pennant = f"{(float(it.get('dbl_pennant_prob', 0))*100):.1f}%"
        ws_win = f"{(float(it.get('dbl_world_series_win_prob', 0))*100):.2f}%"
        bar = it.get("str_visual_bar", "")
        print(f"   {rank:<5} | {name:<24} | {lg_div:<8} | {wins:<9} | {pennant:<9} | {ws_win:<8} | {bar}")
    print("   " + "-" * 80)
    return items


def query_team_four_pillar_metrics(token: Optional[str], team_code: str = "CHC") -> Optional[Dict[str, Any]]:
    """3. Query multi-dimensional four-pillar sabermetric metrics for a franchise."""
    print(f"\n▶ 3. Querying 4-Pillar Sabermetric Metrics for `{team_code}` (`m_four_pillar_metrics`)...")
    params: Dict[str, str] = {
        "filter": f"(str_team_code='{team_code}' && bool_is_active=true)",
        "sort": "-int_updated_epoch_ms_utc",
        "limit": "1"
    }
    res: Dict[str, Any] = http_get("/api/collections/m_four_pillar_metrics/records", params, token)
    items: List[Dict[str, Any]] = res.get("items", [])
    if items:
        m = items[0]
        meta = MLB_REGISTRY.get_team(team_code)
        print(f"   • Franchise: {meta.full_name} ({meta.city}, {meta.ballpark})")
        print(f"   • Offense Consistency: {m.get('dbl_offense_consistency')}")
        print(f"   • Defensive Efficiency: {m.get('dbl_defense_efficiency')}")
        print(f"   • Starting Pitching Rotation Quality: {m.get('dbl_pitching_rotation_quality')}")
        print(f"   • Bullpen High-Leverage Reliability: {m.get('dbl_bullpen_leverage_reliability')}")
        print(f"   • Composite 4-Pillar Index: {m.get('dbl_composite_pillar_index')}")
        return m
    print(f"   ⚠️ No 4-pillar records found for {team_code}.")
    return None


def query_cubs_sensitivity_scenarios(token: Optional[str]) -> Sequence[Dict[str, Any]]:
    """4. Query Chicago Cubs NL Central title vs Wild Card sensitivity analysis."""
    print("\n▶ 4. Querying Cubs Championship Sensitivity Scenarios (`f_cubs_scenario_analysis`)...")
    params: Dict[str, str] = {
        "filter": "(bool_is_active=true)",
        "sort": "dbl_world_series_win_prob",
        "perPage": "10"
    }
    res: Dict[str, Any] = http_get("/api/collections/f_cubs_scenario_analysis/records", params, token)
    items: List[Dict[str, Any]] = res.get("items", [])
    for sc in items:
        name = sc.get("str_scenario_name")
        seed = sc.get("str_seed_designation")
        prob = f"{(float(sc.get('dbl_world_series_win_prob', 0))*100):.2f}%"
        takeaway = sc.get("str_strategic_takeaway")
        print(f"   • [{name}] {seed} -> WS Win Prob: {prob}")
        print(f"     Strategic Note: {takeaway}")
    return items


def main() -> None:
    """Main query demonstration runner."""
    print("================================================================================")
    print(" 🔍 POCKETHOST / POCKETBASE LIVE SABERMETRIC QUERY UTILITY")
    print(f"    Target URL: {POCKETHOST_URL}")
    print("================================================================================")
    
    token: Optional[str] = authenticate_admin()
    if token:
        print("🔐 Admin token authenticated successfully.")
    else:
        print("ℹ️ Running in public / unauthenticated read mode.")

    latest_run = query_latest_simulation_run(token)
    run_id = str(latest_run.get("str_run_id")) if latest_run else None
    query_championship_leaderboard_top(token, run_id=run_id, top_n=10)
    query_team_four_pillar_metrics(token, team_code="CHC")
    query_cubs_sensitivity_scenarios(token)
    print("\n✅ All PocketHost sabermetric queries executed successfully.\n")


if __name__ == "__main__":
    main()
