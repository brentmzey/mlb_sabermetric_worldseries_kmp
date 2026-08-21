#!/usr/bin/env python3
"""
Fetch freshest live 2026 MLB standings, run differentials, recency trends,
and season consistency metrics from official MLB Stats API and update SabermetricDataService.kt.
Strongly typed using Python 3.10+ dataclasses, type annotations, and structured API payload parsing.
"""
from __future__ import annotations

import os
import sys
import re
import json
import urllib.request
import datetime
from dataclasses import dataclass
from typing import Any, Dict, Final, List, Mapping, Match, Optional, Sequence


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


def fetch_live_standings(season_year: int) -> Dict[str, LiveTeamRecord]:
    """Fetches live regular season standings and recency splits from MLB Stats API."""
    url: str = f"https://statsapi.mlb.com/api/v1/standings?leagueId=103,104&season={season_year}"
    req: urllib.request.Request = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)"})
    
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            data: Dict[str, Any] = json.loads(resp.read().decode("utf-8"))
    except Exception as err:
        print(f"⚠️ Warning: Could not fetch from MLB Stats API: {err}")
        return {}

    records_map: Dict[str, LiveTeamRecord] = {}
    records: Sequence[Dict[str, Any]] = data.get("records", [])

    for record in records:
        team_records: Sequence[Dict[str, Any]] = record.get("teamRecords", [])
        for tr in team_records:
            t_id: int = tr.get("team", {}).get("id", 0)
            code: Optional[str] = TEAM_ID_TO_CODE.get(t_id)
            if not code:
                continue

            wins: int = int(tr.get("wins", 0))
            losses: int = int(tr.get("losses", 0))
            rs: float = float(tr.get("runsScored", 0.0))
            ra: float = float(tr.get("runsAllowed", 0.0))
            
            splits: Sequence[Dict[str, Any]] = tr.get("records", {}).get("splitRecords", [])
            last10: Dict[str, Any] = next((s for s in splits if s.get("type") == "lastTen"), {})
            l10_w: int = int(last10.get("wins", 5))
            l10_l: int = int(last10.get("losses", 5))

            pyth_pct: float = (rs ** 1.83) / (rs ** 1.83 + ra ** 1.83) if (rs > 0 or ra > 0) else 0.500
            actual_pct: float = wins / (wins + losses) if (wins + losses) > 0 else 0.500
            luck_diff: float = abs(actual_pct - pyth_pct)
            consistency: float = round(1.0 + max(-0.08, min(0.08, 0.04 - luck_diff * 0.8)), 3)

            records_map[code] = LiveTeamRecord(
                code=code,
                wins=wins,
                losses=losses,
                runs_scored=rs,
                runs_allowed=ra,
                last10_wins=l10_w,
                last10_losses=l10_l,
                consistency_score=consistency
            )

    return records_map


def update_sabermetric_data_service(kt_file_path: str, live_data: Mapping[str, LiveTeamRecord]) -> bool:
    """Updates Kotlin source code in SabermetricDataService.kt with freshly fetched live stats while preserving analytical parameters."""
    if not os.path.exists(kt_file_path):
        print(f"❌ Error: File not found: {kt_file_path}")
        return False

    with open(kt_file_path, "r") as f:
        content: str = f.read()

    def replace_team_entry(match: Match[str]) -> str:
        code: str = match.group(1)
        if code in live_data:
            ld: LiveTeamRecord = live_data[code]
            raw_args: str = match.group(2).strip()
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
                return f"MlbTeam(MlbTeamId.{code}, {', '.join(reconstructed_parts)})"
            elif len(parts) >= 4:
                middle_params = parts[4:]
                return f"MlbTeam(MlbTeamId.{code}, {ld.wins}, {ld.losses}, {ld.runs_scored:.1f}, {ld.runs_allowed:.1f}, {', '.join(middle_params)})"
        return match.group(0)

    pattern: re.Pattern[str] = re.compile(r'MlbTeam\(MlbTeamId\.([A-Z]+),\s*(.*?)\)')
    new_content: str = pattern.sub(replace_team_entry, content)

    with open(kt_file_path, "w") as f:
        f.write(new_content)

    return True


def main() -> None:
    """Main execution function for fetching and updating live MLB standings data."""
    current_year: int = datetime.datetime.now(datetime.timezone.utc).year
    proj_dir: str = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
    kt_file: str = os.path.join(proj_dir, "src", "commonMain", "kotlin", "com", "sabermetrics", "worldseries", "data", "SabermetricDataService.kt")

    live_data: Dict[str, LiveTeamRecord] = fetch_live_standings(current_year)
    if live_data:
        print(f"✅ Fetched live data (with last 10 games recency & season consistency) for {len(live_data)} teams from MLB Stats API.")
        success: bool = update_sabermetric_data_service(kt_file, live_data)
        if success:
            print(f"✅ Successfully updated {kt_file} with live {current_year} standings, last 10 games recency, and season consistency scores!")
    else:
        print("ℹ️ No live API updates applied (offline or off-season fallback preserved).")


if __name__ == "__main__":
    main()

