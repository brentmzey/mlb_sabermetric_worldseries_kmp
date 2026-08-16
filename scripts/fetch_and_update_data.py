#!/usr/bin/env python3
"""
Fetch freshest live 2026 MLB standings, run differentials, recency trends,
and season consistency metrics from official MLB Stats API and update SabermetricDataService.kt.
"""
import urllib.request
import json
import re
import datetime

mapping = {
    108: "LAA", 109: "ARI", 110: "BAL", 111: "BOS", 112: "CHC", 113: "CIN",
    114: "CLE", 115: "COL", 116: "DET", 117: "HOU", 118: "KC", 119: "LAD",
    120: "WSH", 121: "NYM", 133: "OAK", 134: "PIT", 135: "SD", 136: "SEA",
    137: "SF", 138: "STL", 139: "TBD", 140: "TEX", 141: "TOR", 142: "MIN",
    143: "PHI", 144: "ATL", 145: "CWS", 146: "MIA", 147: "NYY", 158: "MIL"
}

current_year = datetime.datetime.now(datetime.timezone.utc).year
url = f"https://statsapi.mlb.com/api/v1/standings?leagueId=103,104&season={current_year}"
req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
with urllib.request.urlopen(req) as resp:
    data = json.loads(resp.read().decode("utf-8"))

live_data = {}
for record in data.get("records", []):
    for tr in record.get("teamRecords", []):
        t_id = tr["team"]["id"]
        code = mapping.get(t_id)
        if code:
            wins = tr["wins"]
            losses = tr["losses"]
            rs = float(tr["runsScored"])
            ra = float(tr["runsAllowed"])
            splits = tr.get("records", {}).get("splitRecords", [])
            last10 = next((s for s in splits if s.get("type") == "lastTen"), {})
            l10_w = int(last10.get("wins", 5))
            l10_l = int(last10.get("losses", 5))

            # Season consistency calculation: pythagorean alignment & variance stability
            pyth_pct = (rs**1.83) / (rs**1.83 + ra**1.83)
            actual_pct = wins / (wins + losses) if wins + losses > 0 else 0.500
            luck_diff = abs(actual_pct - pyth_pct)
            consistency = round(1.0 + max(-0.08, min(0.08, 0.04 - luck_diff * 0.8)), 3)

            live_data[code] = {
                "wins": wins,
                "losses": losses,
                "rs": rs,
                "ra": ra,
                "l10_w": l10_w,
                "l10_l": l10_l,
                "consistency": consistency
            }

print(f"Fetched live data (with last 10 games recency & season consistency) for {len(live_data)} teams from MLB Stats API.")

# Read SabermetricDataService.kt
kt_file = "src/commonMain/kotlin/com/sabermetrics/worldseries/data/SabermetricDataService.kt"
with open(kt_file, "r") as f:
    content = f.read()

# Replace MlbTeam lines with updated parameters including last10Wins, last10Losses, seasonConsistencyScore
def replace_team(match):
    code = match.group(1)
    if code in live_data:
        ld = live_data[code]
        w = ld["wins"]
        l = ld["losses"]
        rs = ld["rs"]
        ra = ld["ra"]
        l10_w = ld["l10_w"]
        l10_l = ld["l10_l"]
        consistency = ld["consistency"]

        # Parse existing rest of args: teamWar, wOBA, wRCPlus, fip, xFip, bullpenWpa, top3AceEra, tradeBoost, hype
        args_str = match.group(2).strip()
        # Remove leading comma
        if args_str.startswith(","):
            args_str = args_str[1:].strip()
        parts = [p.strip() for p in args_str.split(",")]
        # Keep first 9 parameters (teamWar, wOBA, wRCPlus, fip, xFip, bullpenWpa, top3AceEra, tradeBoost, hype)
        param_parts = parts[:9]
        rest_joined = ", ".join(param_parts)
        return f"MlbTeam(MlbTeamId.{code}, {w}, {l}, {rs}, {ra}, {rest_joined}, {l10_w}, {l10_l}, {consistency})"
    return match.group(0)

pattern = re.compile(r'MlbTeam\(MlbTeamId\.([A-Z]+),\s*\d+,\s*\d+,\s*[\d\.]+,\s*[\d\.]+\s*,\s*(.*?)\)')
new_content = pattern.sub(replace_team, content)

with open(kt_file, "w") as f:
    f.write(new_content)

print(f"Successfully updated {kt_file} with live 2026 standings, last 10 games recency, and season consistency scores!")
