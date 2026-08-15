#!/usr/bin/env python3
"""
Updates README.md leaderboard table with the Bill James Log5 & Brian Kenny October Compression simulation statistics.
"""

new_table = """| Rank | Movement | Team Name | League & Div | Record | Expected Wins | Playoff % | Pennant % | World Series Win Prob % | Visual Bar |
| :---: | :---: | :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| 🥇 1 | ▲ +2 | **Atlanta Braves** | NL East | 73 - 48 | 99.8 | **100.0%** | **31.1%** | **21.19%** | `███████████` |
| 🥈 2 | ▲ +2 | **Los Angeles Dodgers** | NL West | 72 - 48 | 95.3 | **99.7%** | **26.3%** | **18.65%** | `██████████` |
| 🥉 3 | ▲ +3 | **New York Yankees** | AL East | 67 - 52 | 92.6 | **100.0%** | **32.3%** | **15.20%** | `████████` |
| 4 | ▼ -3 | **Milwaukee Brewers** | NL Central | 74 - 47 | 98.9 | **100.0%** | **16.9%** | **10.26%** | `██████` |
| 5 | — | **Chicago Cubs** | NL Central | 71 - 50 | 98.2 | **100.0%** | **16.5%** | **10.12%** | `█████` |
| 6 | ▼ -4 | **Tampa Bay Rays** | AL East | 74 - 46 | 103.2 | **100.0%** | **26.4%** | **8.49%** | `████` |
| 7 | ▲ +5 | **Houston Astros** | AL West | 62 - 60 | 81.9 | **70.1%** | **14.7%** | **5.04%** | `██` |
| 8 | ▲ +9 | **Detroit Tigers** | AL Central | 59 - 61 | 83.1 | **68.7%** | **11.2%** | **2.89%** | `█` |
| 9 | ▼ -2 | **San Diego Padres** | NL West | 65 - 57 | 87.9 | **73.7%** | **5.1%** | **2.75%** | `█` |
| 10 | ▼ -2 | **Boston Red Sox** | AL East | 64 - 56 | 87.0 | **96.4%** | **6.7%** | **1.77%** | `█` |
| 11 | ▼ -1 | **Philadelphia Phillies** | NL East | 64 - 58 | 84.5 | **37.8%** | **2.5%** | **1.42%** | `█` |
| 12 | ▲ +3 | **Texas Rangers** | AL West | 60 - 60 | 80.6 | **42.6%** | **3.0%** | **0.47%** | `▏` |
| 13 | ▼ -4 | **Arizona Diamondbacks** | NL West | 64 - 58 | 84.8 | **30.9%** | **1.1%** | **0.46%** | `▏` |
| 14 | ▲ +2 | **Minnesota Twins** | AL Central | 60 - 62 | 78.4 | **15.1%** | **1.6%** | **0.37%** | `▏` |
| 15 | ▲ +4 | **Cleveland Guardians** | AL Central | 59 - 62 | 76.7 | **6.7%** | **0.9%** | **0.30%** | `▏` |
| 16 | ▲ +4 | **Toronto Blue Jays** | AL East | 59 - 63 | 79.8 | **29.3%** | **1.4%** | **0.25%** | `▏` |
| 17 | ▼ -3 | **St. Louis Cardinals** | NL Central | 61 - 60 | 83.7 | **23.9%** | **0.4%** | **0.15%** | `▏` |
| 18 | ▼ -5 | **Chicago White Sox** | AL Central | 61 - 57 | 83.0 | **64.1%** | **1.2%** | **0.07%** | `▏` |
| 19 | ▲ +3 | **Baltimore Orioles** | AL East | 58 - 63 | 76.5 | **5.9%** | **0.4%** | **0.06%** | `▏` |
| 20 | ▲ +4 | **Seattle Mariners** | AL West | 56 - 64 | 73.6 | **1.1%** | **0.2%** | **0.05%** | `▏` |
| 21 | ▼ -10 | **Miami Marlins** | NL East | 62 - 59 | 84.2 | **31.8%** | **0.1%** | **0.04%** | `▏` |
| 22 | ▲ +5 | **Kansas City Royals** | AL Central | 49 - 72 | 63.6 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 23 | ▲ +6 | **Oakland Athletics** | AL West | 47 - 74 | 59.8 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 24 | ▲ +6 | **Los Angeles Angels** | AL West | 46 - 74 | 61.6 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 25 | — | **New York Mets** | NL East | 53 - 69 | 71.7 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 26 | ▼ -8 | **Washington Nationals** | NL East | 59 - 63 | 77.8 | **0.8%** | **0.0%** | **0.00%** | `▏` |
| 27 | ▼ -4 | **Cincinnati Reds** | NL Central | 57 - 61 | 78.6 | **1.4%** | **0.0%** | **0.00%** | `▏` |
| 28 | ▼ -7 | **Pittsburgh Pirates** | NL Central | 58 - 64 | 74.6 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 29 | ▼ -3 | **San Francisco Giants** | NL West | 50 - 71 | 65.2 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 30 | ▼ -2 | **Colorado Rockies** | NL West | 48 - 73 | 63.5 | **0.0%** | **0.0%** | **0.00%** | `▏`"""

readme_path = "README.md"
with open(readme_path, "r") as f:
    content = f.read()

import re
pattern = re.compile(r"\| Rank \| Movement \| Team Name \|.*?\| 30 \|.*?\n", re.DOTALL)
updated_content = pattern.sub(new_table + "\n", content)

with open(readme_path, "w") as f:
    f.write(updated_content)

print("Successfully updated README.md table with Bill James Log5 results!")
