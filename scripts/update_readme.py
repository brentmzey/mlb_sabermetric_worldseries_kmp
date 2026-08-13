#!/usr/bin/env python3
"""
Updates README.md leaderboard table with the Rich 4-Pillar Consistency, Media Consensus, and Vegas Ensemble simulation statistics.
"""

new_table = """| Rank | Movement | Team Name | League & Div | Record | Expected Wins | Playoff % | Pennant % | World Series Win Prob % | Visual Bar |
| :---: | :---: | :--- | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| 🥇 1 | ▲ +3 | **Los Angeles Dodgers** | NL West | 72 - 48 | 94.9 | **99.8%** | **48.9%** | **41.95%** | `████████████████` |
| 🥈 2 | ▲ +1 | **Atlanta Braves** | NL East | 73 - 48 | 98.6 | **100.0%** | **25.0%** | **17.81%** | `██████████` |
| 🥉 3 | ▲ +3 | **New York Yankees** | AL East | 67 - 52 | 91.7 | **99.9%** | **40.4%** | **14.62%** | `████████` |
| 4 | ▼ -3 | **Milwaukee Brewers** | NL Central | 74 - 47 | 98.1 | **100.0%** | **12.0%** | **7.59%** | `████` |
| 5 | — | **Chicago Cubs** | NL Central | 71 - 50 | 96.8 | **99.9%** | **9.3%** | **5.77%** | `███` |
| 6 | ▼ -4 | **Tampa Bay Rays** | AL East | 74 - 46 | 101.6 | **100.0%** | **19.5%** | **3.30%** | `██` |
| 7 | ▲ +5 | **Houston Astros** | AL West | 62 - 60 | 82.0 | **69.5%** | **15.3%** | **3.20%** | `██` |
| 8 | ▼ -1 | **San Diego Padres** | NL West | 65 - 57 | 87.3 | **70.8%** | **2.7%** | **1.55%** | `█` |
| 9 | ▲ +8 | **Detroit Tigers** | AL Central | 59 - 61 | 82.4 | **60.5%** | **7.3%** | **1.05%** | `█` |
| 10 | ▼ -2 | **Boston Red Sox** | AL East | 64 - 56 | 86.6 | **95.2%** | **6.6%** | **0.98%** | `█` |
| 11 | ▼ -1 | **Philadelphia Phillies** | NL East | 64 - 58 | 84.5 | **40.8%** | **1.5%** | **0.75%** | `▏` |
| 12 | ▲ +3 | **Texas Rangers** | AL West | 60 - 60 | 80.7 | **44.0%** | **3.6%** | **0.41%** | `▏` |
| 13 | ▲ +3 | **Minnesota Twins** | AL Central | 60 - 62 | 78.8 | **18.7%** | **1.7%** | **0.24%** | `▏` |
| 14 | ▲ +5 | **Cleveland Guardians** | AL Central | 59 - 62 | 77.4 | **10.0%** | **1.4%** | **0.21%** | `▏` |
| 15 | ▼ -6 | **Arizona Diamondbacks** | NL West | 64 - 58 | 84.7 | **32.4%** | **0.4%** | **0.16%** | `▏` |
| 16 | ▲ +4 | **Toronto Blue Jays** | AL East | 59 - 63 | 79.7 | **28.2%** | **1.4%** | **0.14%** | `▏` |
| 17 | ▼ -4 | **Chicago White Sox** | AL Central | 61 - 57 | 82.9 | **64.0%** | **2.1%** | **0.11%** | `▏` |
| 18 | ▲ +4 | **Baltimore Orioles** | AL East | 58 - 63 | 76.9 | **7.7%** | **0.4%** | **0.09%** | `▏` |
| 19 | ▼ -5 | **St. Louis Cardinals** | NL Central | 61 - 60 | 83.2 | **22.2%** | **0.1%** | **0.06%** | `▏` |
| 20 | ▲ +4 | **Seattle Mariners** | AL West | 56 - 64 | 74.4 | **2.3%** | **0.3%** | **0.01%** | `▏` |
| 21 | ▲ +6 | **Kansas City Royals** | AL Central | 49 - 72 | 64.8 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 22 | ▲ +7 | **Oakland Athletics** | AL West | 47 - 74 | 61.2 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 23 | ▲ +7 | **Los Angeles Angels** | AL West | 46 - 74 | 62.6 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 24 | ▲ +1 | **New York Mets** | NL East | 53 - 69 | 72.0 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 25 | ▼ -7 | **Washington Nationals** | NL East | 59 - 63 | 78.0 | **1.1%** | **0.0%** | **0.00%** | `▏` |
| 26 | ▼ -15 | **Miami Marlins** | NL East | 62 - 59 | 83.8 | **31.0%** | **0.0%** | **0.00%** | `▏` |
| 27 | ▼ -4 | **Cincinnati Reds** | NL Central | 57 - 61 | 78.8 | **1.9%** | **0.0%** | **0.00%** | `▏` |
| 28 | ▼ -7 | **Pittsburgh Pirates** | NL Central | 58 - 64 | 75.2 | **0.1%** | **0.0%** | **0.00%** | `▏` |
| 29 | ▼ -3 | **San Francisco Giants** | NL West | 50 - 71 | 66.4 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 30 | ▼ -2 | **Colorado Rockies** | NL West | 48 - 73 | 64.5 | **0.0%** | **0.0%** | **0.00%** | `▏`"""

readme_path = "README.md"
with open(readme_path, "r") as f:
    content = f.read()

import re
pattern = re.compile(r"\| Rank \| Movement \| Team Name \|.*?\| 30 \|.*?\n", re.DOTALL)
updated_content = pattern.sub(new_table + "\n", content)

with open(readme_path, "w") as f:
    f.write(updated_content)

print("Successfully updated README.md table with rich 4-pillar and media consensus results!")
