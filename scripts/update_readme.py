#!/usr/bin/env python3
"""
Updates README.md leaderboard table with the Bayesian Market & Expert Ensemble simulation statistics.
"""

new_table = """| Rank | Movement | Team Name | League & Div | Record | Expected Wins | Playoff % | Pennant % | World Series Win Prob % | Visual Bar |
| :---: | :---: | :--- | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| 🥇 1 | ▲ +3 | **Los Angeles Dodgers** | NL West | 72 - 48 | 95.0 | **99.7%** | **42.6%** | **35.23%** | `████████████████` |
| 🥈 2 | ▲ +1 | **Atlanta Braves** | NL East | 73 - 48 | 98.5 | **100.0%** | **27.3%** | **19.18%** | `████████████` |
| 🥉 3 | ▲ +3 | **New York Yankees** | AL East | 67 - 52 | 91.7 | **99.9%** | **36.7%** | **14.92%** | `████████` |
| 4 | ▼ -3 | **Milwaukee Brewers** | NL Central | 74 - 47 | 98.2 | **100.0%** | **13.3%** | **7.98%** | `████████` |
| 5 | — | **Chicago Cubs** | NL Central | 71 - 50 | 96.9 | **100.0%** | **10.7%** | **6.53%** | `███████` |
| 6 | ▼ -4 | **Tampa Bay Rays** | AL East | 74 - 46 | 101.5 | **100.0%** | **20.6%** | **4.72%** | `██████` |
| 7 | ▲ +5 | **Houston Astros** | AL West | 62 - 60 | 82.0 | **69.6%** | **14.9%** | **3.70%** | `███` |
| 8 | ▼ -1 | **San Diego Padres** | NL West | 65 - 57 | 87.3 | **70.1%** | **2.9%** | **1.65%** | `██` |
| 9 | ▲ +8 | **Detroit Tigers** | AL Central | 59 - 61 | 82.5 | **62.2%** | **7.8%** | **1.47%** | `██` |
| 10 | ▼ -2 | **Boston Red Sox** | AL East | 64 - 56 | 86.6 | **95.4%** | **7.6%** | **1.38%** | `██` |
| 11 | ▼ -1 | **Philadelphia Phillies** | NL East | 64 - 58 | 84.4 | **40.6%** | **1.9%** | **1.02%** | `█` |
| 12 | ▲ +3 | **Texas Rangers** | AL West | 60 - 60 | 80.7 | **43.6%** | **3.9%** | **0.64%** | `█` |
| 13 | ▼ -4 | **Arizona Diamondbacks** | NL West | 64 - 58 | 84.6 | **33.1%** | **0.9%** | **0.39%** | `█` |
| 14 | ▲ +5 | **Cleveland Guardians** | AL Central | 59 - 62 | 77.3 | **9.6%** | **1.4%** | **0.32%** | `▏` |
| 15 | ▲ +1 | **Minnesota Twins** | AL Central | 60 - 62 | 78.8 | **18.6%** | **2.0%** | **0.30%** | `▏` |
| 16 | ▲ +4 | **Toronto Blue Jays** | AL East | 59 - 63 | 79.7 | **27.4%** | **1.6%** | **0.17%** | `▏` |
| 17 | ▼ -4 | **Chicago White Sox** | AL Central | 61 - 57 | 82.9 | **63.7%** | **2.6%** | **0.17%** | `▏` |
| 18 | ▲ +4 | **Baltimore Orioles** | AL East | 58 - 63 | 76.9 | **7.9%** | **0.7%** | **0.10%** | `▏` |
| 19 | ▲ +5 | **Seattle Mariners** | AL West | 56 - 64 | 74.4 | **2.3%** | **0.3%** | **0.06%** | `▏` |
| 20 | ▼ -6 | **St. Louis Cardinals** | NL Central | 61 - 60 | 83.3 | **22.6%** | **0.2%** | **0.04%** | `▏` |
| 21 | ▼ -10 | **Miami Marlins** | NL East | 62 - 59 | 83.8 | **30.6%** | **0.1%** | **0.02%** | `▏` |
| 22 | ▲ +1 | **Cincinnati Reds** | NL Central | 57 - 61 | 78.7 | **2.1%** | **0.0%** | **0.01%** | `▏` |
| 23 | ▲ +4 | **Kansas City Royals** | AL Central | 49 - 72 | 64.8 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 24 | ▲ +5 | **Oakland Athletics** | AL West | 47 - 74 | 61.1 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 25 | ▲ +5 | **Los Angeles Angels** | AL West | 46 - 74 | 62.7 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 26 | ▼ -1 | **New York Mets** | NL East | 53 - 69 | 72.0 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 27 | ▼ -9 | **Washington Nationals** | NL East | 59 - 63 | 78.0 | **1.1%** | **0.0%** | **0.00%** | `▏` |
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

print("Successfully updated README.md table!")
