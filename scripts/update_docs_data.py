import os
import re
import csv
import json

def get_medal(rank):
    if rank == 1: return "🥇 1"
    if rank == 2: return "🥈 2"
    if rank == 3: return "🥉 3"
    return str(rank)

def get_visual_bar(prob, max_prob):
    # max blocks is 10
    if max_prob <= 0: max_prob = 1
    blocks = int((prob / max_prob) * 10)
    if blocks > 10: blocks = 10
    if blocks < 1 and prob > 0: return "▏"
    if blocks < 1: return " "
    return "█" * blocks

def update_all():
    # Read CSV
    df = []
    with open("output_datasets/mlb_sabermetric_clean_dataset.csv", "r", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            df.append(row)
    
    with open("output_datasets/pockethost_sync_payload.json", "r") as f:
        ph_data = json.load(f)
    
    leaderboard = {row["str_team_code"]: row for row in ph_data["collections"]["f_world_series_leaderboard"]}
    
    team_data = {}
    max_ws_prob = 0
    for row in ph_data["collections"]["f_world_series_leaderboard"]:
        p = row.get("dbl_world_series_win_prob", 0.0) * 100
        if p > max_ws_prob: max_ws_prob = p

    for row in df:
        tc = row["Team_ID"]
        lb = leaderboard.get(tc, {})
        ws_prob = lb.get("dbl_world_series_win_prob", 0.0) * 100
        team_data[row["Team_Name"]] = {
            "wins": int(float(row["Wins"])),
            "losses": int(float(row["Losses"])),
            "record": f"{int(float(row['Wins']))} - {int(float(row['Losses']))}",
            "record_dash": f"{int(float(row['Wins']))}–{int(float(row['Losses']))}",
            "ws_prob": ws_prob,
            "rank": lb.get("int_sim_rank", 99),
            "movement": lb.get("str_visual_bar", "—"),
            "league": row["League"],
            "division": row["Division"].capitalize(),
            "playoff_pct": lb.get("dbl_playoff_prob", 0.0) * 100,
            "pennant_pct": lb.get("dbl_pennant_prob", 0.0) * 100,
            "expected_wins": lb.get("dbl_expected_season_wins", float(row["Wins"]))
        }
    
    # Generate new table
    sorted_teams = sorted(team_data.items(), key=lambda x: x[1]["rank"])
    table_lines = [
        "| Rank | Movement | Team Name | League & Div | Record | Expected Wins | Playoff % | Pennant % | World Series Win Prob % | Visual Bar |",
        "| :---: | :---: | :--- | :---: | :---: | :---: | :---: | :---: | :---: | :--- |"
    ]
    for team, data in sorted_teams:
        rank_str = get_medal(data["rank"])
        bar = get_visual_bar(data["ws_prob"], max_ws_prob)
        line = f"| {rank_str} | {data['movement']} | **{team}** | {data['league']} {data['division']} | {data['record']} | {data['expected_wins']:.1f} | **{data['playoff_pct']:.1f}%** | **{data['pennant_pct']:.1f}%** | **{data['ws_prob']:.2f}%** | `{bar}` |"
        table_lines.append(line)
        
    table_str = "\n".join(table_lines) + "\n"
    
    # Regex to replace markdown tables
    pattern = re.compile(r"\| Rank \| Movement \| Team Name \|.*?\| 30 \|.*?\n", re.DOTALL)
    
    md_files = ["README.md"]
    for root, _, files in os.walk("docs"):
        for file in files:
            if file.endswith(".md"):
                md_files.append(os.path.join(root, file))
                
    old_data = {
        "Los Angeles Dodgers": {"record": "73 - 49", "record2": "73–49", "ws": "16.63", "ws2": "20.51"},
        "Atlanta Braves": {"record": "73 - 49", "record2": "73–49", "ws": "17.92", "ws2": "8.55", "ws3": "17.80"},
        "New York Yankees": {"record": "68 - 54", "record2": "68–54", "ws": "15.65", "ws2": "10.63", "ws3": "15.20"},
        "Milwaukee Brewers": {"record": "75 - 47", "record2": "75–47", "ws": "11.45", "ws2": "14.18", "ws3": "11.20"},
        "Chicago Cubs": {"record": "72 - 51", "record2": "72–51", "ws": "10.15", "ws2": "10.12"},
        "Tampa Bay Rays": {"record": "74 - 46", "record2": "74–46", "ws": "8.60", "ws2": "14.16"},
        "Houston Astros": {"record": "62 - 60", "record2": "62–60", "ws": "6.63", "ws2": "6.38"},
    }

    for filepath in md_files:
        with open(filepath, "r", encoding="utf-8") as f:
            content = f.read()
            
        new_content = pattern.sub(table_str, content)
        
        for team, d in team_data.items():
            if team in old_data:
                # Replace old record with new record
                new_content = new_content.replace(old_data[team]["record"], d["record"])
                new_content = new_content.replace(old_data[team]["record2"], d["record_dash"])
                
                # Replace old WS prob with new
                new_content = new_content.replace(f"{old_data[team]['ws']}%", f"{d['ws_prob']:.2f}%")
                if "ws2" in old_data[team]:
                    new_content = new_content.replace(f"{old_data[team]['ws2']}%", f"{d['ws_prob']:.2f}%")
                if "ws3" in old_data[team]:
                    new_content = new_content.replace(f"{old_data[team]['ws3']}%", f"{d['ws_prob']:.2f}%")
        
        if new_content != content:
            with open(filepath, "w", encoding="utf-8") as f:
                f.write(new_content)
            print(f"Updated {filepath}")

if __name__ == "__main__":
    update_all()
