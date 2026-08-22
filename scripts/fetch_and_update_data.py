#!/usr/bin/env python3
"""
Fetch freshest live 2026 MLB standings, run differentials, recency trends,
and season consistency metrics from official MLB Stats API and update SabermetricDataService.kt.
Strongly typed using Python 3.10+ dataclasses, TypedDict schemas, and explicit type annotations for every variable.
"""
from __future__ import annotations

import os
import sys
import re
import json
import urllib.request
import datetime
from dataclasses import dataclass
from typing import (
    Any,
    Dict,
    Final,
    List,
    Mapping,
    Match,
    Optional,
    Sequence,
    TextIO,
    TypedDict,
    Union,
    cast
)


# ==============================================================================
# TypedDict Definitions for MLB Stats API Schema
# ==============================================================================

class MlbTeamReferenceJson(TypedDict, total=False):
    id: int
    name: str
    link: str


class MlbSplitRecordJson(TypedDict, total=False):
    type: str
    wins: int
    losses: int
    pct: str


class MlbSplitRecordsContainerJson(TypedDict, total=False):
    splitRecords: List[MlbSplitRecordJson]


class MlbTeamRecordEntryJson(TypedDict, total=False):
    team: MlbTeamReferenceJson
    season: str
    wins: int
    losses: int
    runsScored: Union[int, float]
    runsAllowed: Union[int, float]
    records: MlbSplitRecordsContainerJson


class MlbDivisionStandingsRecordJson(TypedDict, total=False):
    standingsType: str
    league: Dict[str, Any]
    division: Dict[str, Any]
    teamRecords: List[MlbTeamRecordEntryJson]


class MlbStandingsApiResponseJson(TypedDict, total=False):
    copyright: str
    records: List[MlbDivisionStandingsRecordJson]


# ==============================================================================
# Domain Model Dataclasses
# ==============================================================================

@dataclass(frozen=True)
class LiveTeamRecord:
    """Strongly-typed live regular season statistical record from MLB Stats API."""
    code: str
    wins: int
    losses: int
    runs_scored: float
    runs_allowed: float
    last10_wins: int
    last10_losses: int
    consistency_score: float

    @property
    def win_pct(self) -> float:
        total: int = self.wins + self.losses
        return self.wins / total if total > 0 else 0.500

    @property
    def pythagorean_pct(self) -> float:
        rs_exp: float = self.runs_scored ** 1.83
        ra_exp: float = self.runs_allowed ** 1.83
        denom: float = rs_exp + ra_exp
        return rs_exp / denom if denom > 0 else 0.500


TEAM_ID_TO_CODE: Final[Mapping[int, str]] = {
    108: "LAA", 109: "ARI", 110: "BAL", 111: "BOS", 112: "CHC", 113: "CIN",
    114: "CLE", 115: "COL", 116: "DET", 117: "HOU", 118: "KC", 119: "LAD",
    120: "WSH", 121: "NYM", 133: "OAK", 134: "PIT", 135: "SD", 136: "SEA",
    137: "SF", 138: "STL", 139: "TBD", 140: "TEX", 141: "TOR", 142: "MIN",
    143: "PHI", 144: "ATL", 145: "CWS", 146: "MIA", 147: "NYY", 158: "MIL"
}


def _calculate_season_consistency(wins: int, losses: int, runs_scored: float, runs_allowed: float) -> float:
    """Computes a team's season consistency score based on actual vs Pythagorean win percentage variance."""
    pyth_denom: float = (runs_scored ** 1.83) + (runs_allowed ** 1.83)
    pyth_pct: float = ((runs_scored ** 1.83) / pyth_denom) if pyth_denom > 0 else 0.500
    total_games: int = wins + losses
    actual_pct: float = (wins / total_games) if total_games > 0 else 0.500
    luck_diff: float = abs(actual_pct - pyth_pct)
    consistency_adj: float = max(-0.08, min(0.08, 0.04 - luck_diff * 0.8))
    return round(1.0 + consistency_adj, 3)


def _extract_last10_record(splits_container: Optional[MlbSplitRecordsContainerJson]) -> tuple[int, int]:
    """Extracts (wins, losses) for the last 10 games from an MLB split records container."""
    splits: List[MlbSplitRecordJson] = splits_container.get("splitRecords", []) if splits_container else []
    last10: Optional[MlbSplitRecordJson] = next((s for s in splits if s.get("type") == "lastTen"), None)
    l10_w: int = int(last10.get("wins", 5)) if last10 else 5
    l10_l: int = int(last10.get("losses", 5)) if last10 else 5
    return l10_w, l10_l


def _parse_single_team_record(tr: MlbTeamRecordEntryJson) -> Optional[LiveTeamRecord]:
    """Parses an individual team record entry from the MLB Stats API JSON."""
    team_ref: Optional[MlbTeamReferenceJson] = tr.get("team")
    t_id: int = int(team_ref.get("id", 0)) if team_ref else 0
    code: Optional[str] = TEAM_ID_TO_CODE.get(t_id)
    if not code:
        return None

    wins: int = int(tr.get("wins", 0))
    losses: int = int(tr.get("losses", 0))
    rs: float = float(tr.get("runsScored", 0.0))
    ra: float = float(tr.get("runsAllowed", 0.0))
    
    splits_container: Optional[MlbSplitRecordsContainerJson] = tr.get("records")
    l10_w, l10_l = _extract_last10_record(splits_container)
    consistency: float = _calculate_season_consistency(wins, losses, rs, ra)

    return LiveTeamRecord(
        code=code,
        wins=wins,
        losses=losses,
        runs_scored=rs,
        runs_allowed=ra,
        last10_wins=l10_w,
        last10_losses=l10_l,
        consistency_score=consistency
    )


def _parse_all_standings_records(records: Sequence[MlbDivisionStandingsRecordJson]) -> Dict[str, LiveTeamRecord]:
    """Parses a sequence of division standings into a map of team codes to LiveTeamRecord objects."""
    records_map: Dict[str, LiveTeamRecord] = {}
    record: MlbDivisionStandingsRecordJson
    for record in records:
        team_records: List[MlbTeamRecordEntryJson] = record.get("teamRecords", [])
        tr: MlbTeamRecordEntryJson
        for tr in team_records:
            parsed_team: Optional[LiveTeamRecord] = _parse_single_team_record(tr)
            if parsed_team:
                records_map[parsed_team.code] = parsed_team
    return records_map


def fetch_live_standings(season_year: int) -> Dict[str, LiveTeamRecord]:
    """Fetches live regular season standings and recency splits from MLB Stats API."""
    url: str = f"https://statsapi.mlb.com/api/v1/standings?leagueId=103,104&season={season_year}"
    headers_dict: Dict[str, str] = {"User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)"}
    req: urllib.request.Request = urllib.request.Request(url, headers=headers_dict)
    
    data: MlbStandingsApiResponseJson
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            raw_bytes: bytes = resp.read()
            raw_text: str = raw_bytes.decode("utf-8")
            data = cast(MlbStandingsApiResponseJson, json.loads(raw_text))
    except Exception as err:
        err_msg: str = str(err)
        print(f"⚠️ Warning: Could not fetch from MLB Stats API: {err_msg}")
        return {}

    records: List[MlbDivisionStandingsRecordJson] = data.get("records", [])
    return _parse_all_standings_records(records)


def _reconstruct_team_args(code: str, raw_args: str, live_data: Mapping[str, LiveTeamRecord]) -> str:
    """Reconstructs the Kotlin MlbTeam(...) arguments string preserving analytical parameters."""
    if code not in live_data:
        return f"MlbTeam(MlbTeamId.{code}, {raw_args})"

    ld: LiveTeamRecord = live_data[code]
    parts: List[str] = [p.strip() for p in raw_args.split(",") if p.strip()]

    # parts structure in MlbTeam(...):
    # 0: wins, 1: losses, 2: rs, 3: ra,
    # 4..12: teamWar, wOBA, wRCPlus, fip, xFip, bullpenWpa, top3AceEra, tradeDeadlineWarAdded, clubhouseHypeIndex (9 params)
    # 13: last10Wins, 14: last10Losses, 15: seasonConsistencyScore
    # 16+: remaining analytical pillar params (marketImpliedWsProb, expertConsensusRating, etc.)
    if len(parts) >= 16:
        middle_params: List[str] = parts[4:13]
        trailing_params: List[str] = parts[16:]
        reconstructed_parts: List[str] = (
            [str(ld.wins), str(ld.losses), f"{ld.runs_scored:.1f}", f"{ld.runs_allowed:.1f}"]
            + middle_params
            + [str(ld.last10_wins), str(ld.last10_losses), f"{ld.consistency_score:.3f}"]
            + trailing_params
        )
        joined_params: str = ", ".join(reconstructed_parts)
        return f"MlbTeam(MlbTeamId.{code}, {joined_params})"
    elif len(parts) >= 4:
        middle_params_fallback: List[str] = parts[4:]
        joined_fallback: str = ", ".join(middle_params_fallback)
        return f"MlbTeam(MlbTeamId.{code}, {ld.wins}, {ld.losses}, {ld.runs_scored:.1f}, {ld.runs_allowed:.1f}, {joined_fallback})"

    return f"MlbTeam(MlbTeamId.{code}, {raw_args})"


def update_sabermetric_data_service(kt_file_path: str, live_data: Mapping[str, LiveTeamRecord]) -> bool:
    """Updates Kotlin source code in SabermetricDataService.kt with freshly fetched live stats while preserving analytical parameters."""
    file_exists: bool = os.path.exists(kt_file_path)
    if not file_exists:
        print(f"❌ Error: File not found: {kt_file_path}")
        return False

    content: str
    f: TextIO
    with open(kt_file_path, "r", encoding="utf-8") as f:
        content = f.read()

    def replace_team_entry(match: Match[str]) -> str:
        code: str = match.group(1)
        raw_args: str = match.group(2).strip()
        return _reconstruct_team_args(code, raw_args, live_data)

    pattern: re.Pattern[str] = re.compile(r'MlbTeam\(MlbTeamId\.([A-Z]+),\s*(.*?)\)')
    new_content: str = pattern.sub(replace_team_entry, content)

    with open(kt_file_path, "w", encoding="utf-8") as f:
        f.write(new_content)

    return True


def main() -> None:
    """Main execution function for fetching and updating live MLB standings data."""
    now_utc: datetime.datetime = datetime.datetime.now(datetime.timezone.utc)
    current_year: int = now_utc.year
    script_dir: str = os.path.dirname(__file__)
    proj_dir: str = os.path.abspath(os.path.join(script_dir, ".."))
    kt_file: str = os.path.join(
        proj_dir,
        "src",
        "commonMain",
        "kotlin",
        "com",
        "sabermetrics",
        "worldseries",
        "data",
        "SabermetricDataService.kt"
    )

    live_data: Dict[str, LiveTeamRecord] = fetch_live_standings(current_year)
    team_count: int = len(live_data)
    if team_count > 0:
        print(f"✅ Fetched live data (with last 10 games recency & season consistency) for {team_count} teams from MLB Stats API.")
        success: bool = update_sabermetric_data_service(kt_file, live_data)
        if success:
            print(f"✅ Successfully updated {kt_file} with live {current_year} standings, last 10 games recency, and season consistency scores!")
    else:
        print("ℹ️ No live API updates applied (offline or off-season fallback preserved).")


if __name__ == "__main__":
    main()



