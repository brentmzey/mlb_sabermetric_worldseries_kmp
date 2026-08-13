#!/usr/bin/env python3
"""
Updates README.md leaderboard table with the Calibrated Rich 4-Pillar Consistency, Media Consensus, and Vegas Ensemble simulation statistics.
"""

new_table = """| Rank | Movement | Team Name | League & Div | Record | Expected Wins | Playoff % | Pennant % | World Series Win Prob % | Visual Bar |
| :---: | :---: | :--- | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| 🥇 1 | ▲ +3 | **Los Angeles Dodgers** | NL West | 72 - 48 | 94.9 | **99.6%** | **26.4%** | **18.35%** | `██████████` |
| 🥈 2 | ▲ +1 | **Atlanta Braves** | NL East | 73 - 48 | 98.6 | **100.0%** | **26.7%** | **16.85%** | `█████████` |
| 🥉 3 | ▲ +3 | **New York Yankees** | AL East | 67 - 52 | 91.7 | **99.9%** | **23.0%** | **11.26%** | `██████` |
| 4 | ▼ -3 | **Milwaukee Brewers** | NL Central | 74 - 47 | 98.1 | **100.0%** | **18.4%** | **10.81%** | `██████` |
| 5 | ▼ -3 | **Tampa Bay Rays** | AL East | 74 - 46 | 101.6 | **100.0%** | **25.3%** | **9.58%** | `█████` |
| 6 | ▼ -1 | **Chicago Cubs** | NL Central | 71 - 50 | 96.9 | **100.0%** | **15.5%** | **8.97%** | `█████` |
| 7 | ▲ +5 | **Houston Astros** | AL West | 62 - 60 | 81.9 | **69.0%** | **13.7%** | **5.60%** | `███` |
| 8 | — | **Boston Red Sox** | AL East | 64 - 56 | 86.6 | **95.2%** | **9.3%** | **3.48%** | `██` |
| 9 | ▲ +8 | **Detroit Tigers** | AL Central | 59 - 61 | 82.4 | **60.8%** | **10.7%** | **3.33%** | `██` |
| 10 | ▼ -3 | **San Diego Padres** | NL West | 65 - 57 | 87.3 | **70.8%** | **5.9%** | **3.11%** | `██` |
| 11 | ▲ +4 | **Texas Rangers** | AL West | 60 - 60 | 80.7 | **44.3%** | **5.3%** | **1.74%** | `█` |
| 12 | ▼ -2 | **Philadelphia Phillies** | NL East | 64 - 58 | 84.5 | **40.1%** | **3.4%** | **1.70%** | `█` |
| 13 | — | **Chicago White Sox** | AL Central | 61 - 57 | 82.9 | **64.4%** | **5.4%** | **1.16%** | `█` |
| 14 | ▼ -5 | **Arizona Diamondbacks** | NL West | 64 - 58 | 84.7 | **33.5%** | **2.0%** | **0.88%** | `▏` |
| 15 | ▲ +1 | **Minnesota Twins** | AL Central | 60 - 62 | 78.7 | **18.2%** | **2.4%** | **0.84%** | `▏` |
| 16 | ▲ +4 | **Toronto Blue Jays** | AL East | 59 - 63 | 79.8 | **28.5%** | **2.5%** | **0.73%** | `▏` |
| 17 | ▲ +2 | **Cleveland Guardians** | AL Central | 59 - 62 | 77.3 | **10.0%** | **1.4%** | **0.48%** | `▏` |
| 18 | ▼ -4 | **St. Louis Cardinals** | NL Central | 61 - 60 | 83.2 | **22.7%** | **1.0%** | **0.43%** | `▏` |
| 19 | ▼ -8 | **Miami Marlins** | NL East | 62 - 59 | 83.8 | **29.9%** | **0.7%** | **0.26%** | `▏` |
| 20 | ▲ +2 | **Baltimore Orioles** | AL East | 58 - 63 | 76.9 | **7.5%** | **0.7%** | **0.23%** | `▏` |
| 21 | ▲ +3 | **Seattle Mariners** | AL West | 56 - 64 | 74.4 | **2.2%** | **0.3%** | **0.14%** | `▏` |
| 22 | ▲ +1 | **Cincinnati Reds** | NL Central | 57 - 61 | 78.8 | **2.3%** | **0.1%** | **0.05%** | `▏` |
| 23 | ▼ -5 | **Washington Nationals** | NL East | 59 - 63 | 78.0 | **1.2%** | **0.0%** | **0.02%** | `▏` |
| 24 | ▲ +3 | **Kansas City Royals** | AL Central | 49 - 72 | 64.8 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 25 | ▲ +4 | **Oakland Athletics** | AL West | 47 - 74 | 61.2 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 26 | ▲ +4 | **Los Angeles Angels** | AL West | 46 - 74 | 62.8 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 27 | ▼ -2 | **New York Mets** | NL East | 53 - 69 | 72.0 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 28 | ▼ -7 | **Pittsburgh Pirates** | NL Central | 58 - 64 | 75.2 | **0.1%** | **0.0%** | **0.00%** | `▏` |
| 29 | ▼ -3 | **San Francisco Giants** | NL West | 50 - 71 | 66.3 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 30 | ▼ -2 | **Colorado Rockies** | NL West | 48 - 73 | 64.5 | **0.0%** | **0.0%** | **0.00%** | `▏`"""

readme_path = "README.md"
with open(readme_path, "r") as f:
    content = f.read()

import re
pattern = re.compile(r"\| Rank \| Movement \| Team Name \|.*?\| 30 \|.*?\n", re.DOTALL)
updated_content = pattern.sub(new_table + "\n", content)

with open(readme_path, "w") as f:
    f.write(updated_content)

print("Successfully updated README.md table with calibrated 4-pillar and media consensus results!")
