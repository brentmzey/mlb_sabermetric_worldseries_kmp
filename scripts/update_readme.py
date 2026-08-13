#!/usr/bin/env python3
"""
Updates README.md leaderboard table with the Calibrated Hot Streak Momentum (gamma = 0.12) simulation statistics.
"""

new_table = """| Rank | Movement | Team Name | League & Div | Record | Expected Wins | Playoff % | Pennant % | World Series Win Prob % | Visual Bar |
| :---: | :---: | :--- | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| 🥇 1 | ▲ +3 | **Los Angeles Dodgers** | NL West | 72 - 48 | 94.9 | **99.7%** | **38.5%** | **30.56%** | `████████████████` |
| 🥈 2 | ▲ +1 | **Atlanta Braves** | NL East | 73 - 48 | 98.6 | **100.0%** | **28.8%** | **20.56%** | `█████████████` |
| 🥉 3 | ▲ +3 | **New York Yankees** | AL East | 67 - 52 | 91.7 | **99.9%** | **36.2%** | **15.57%** | `██████████` |
| 4 | ▼ -3 | **Milwaukee Brewers** | NL Central | 74 - 47 | 98.2 | **100.0%** | **13.4%** | **8.02%** | `█████` |
| 5 | — | **Chicago Cubs** | NL Central | 71 - 50 | 96.8 | **99.9%** | **12.8%** | **7.83%** | `█████` |
| 6 | ▼ -4 | **Tampa Bay Rays** | AL East | 74 - 46 | 101.6 | **100.0%** | **23.4%** | **5.89%** | `████` |
| 7 | ▲ +5 | **Houston Astros** | AL West | 62 - 60 | 81.9 | **69.4%** | **14.2%** | **3.90%** | `██` |
| 8 | ▼ -1 | **San Diego Padres** | NL West | 65 - 57 | 87.3 | **70.1%** | **3.5%** | **1.82%** | `█` |
| 9 | ▼ -1 | **Boston Red Sox** | AL East | 64 - 56 | 86.6 | **95.2%** | **6.8%** | **1.31%** | `█` |
| 10 | ▲ +7 | **Detroit Tigers** | AL Central | 59 - 61 | 82.4 | **61.2%** | **7.6%** | **1.24%** | `█` |
| 11 | ▼ -1 | **Philadelphia Phillies** | NL East | 64 - 58 | 84.5 | **41.8%** | **2.1%** | **1.13%** | `█` |
| 12 | ▲ +3 | **Texas Rangers** | AL West | 60 - 60 | 80.7 | **43.7%** | **3.8%** | **0.48%** | `▏` |
| 13 | ▼ -4 | **Arizona Diamondbacks** | NL West | 64 - 58 | 84.7 | **33.3%** | **0.8%** | **0.32%** | `▏` |
| 14 | ▲ +2 | **Minnesota Twins** | AL Central | 60 - 62 | 78.8 | **18.2%** | **1.6%** | **0.30%** | `▏` |
| 15 | ▼ -2 | **Chicago White Sox** | AL Central | 61 - 57 | 82.9 | **64.2%** | **2.7%** | **0.30%** | `▏` |
| 16 | ▲ +4 | **Toronto Blue Jays** | AL East | 59 - 63 | 79.7 | **28.2%** | **1.5%** | **0.21%** | `▏` |
| 17 | ▲ +2 | **Cleveland Guardians** | AL Central | 59 - 62 | 77.3 | **9.8%** | **1.1%** | **0.21%** | `▏` |
| 18 | ▲ +4 | **Baltimore Orioles** | AL East | 58 - 63 | 76.9 | **8.1%** | **0.8%** | **0.12%** | `▏` |
| 19 | ▼ -5 | **St. Louis Cardinals** | NL Central | 61 - 60 | 83.2 | **22.0%** | **0.3%** | **0.11%** | `▏` |
| 20 | ▲ +4 | **Seattle Mariners** | AL West | 56 - 64 | 74.4 | **2.2%** | **0.3%** | **0.09%** | `▏` |
| 21 | ▼ -10 | **Miami Marlins** | NL East | 62 - 59 | 83.9 | **30.2%** | **0.1%** | **0.03%** | `▏` |
| 22 | ▲ +5 | **Kansas City Royals** | AL Central | 49 - 72 | 64.9 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 23 | ▲ +6 | **Oakland Athletics** | AL West | 47 - 74 | 61.1 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 24 | ▲ +6 | **Los Angeles Angels** | AL West | 46 - 74 | 62.7 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 25 | — | **New York Mets** | NL East | 53 - 69 | 72.0 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 26 | ▼ -8 | **Washington Nationals** | NL East | 59 - 63 | 78.0 | **1.0%** | **0.0%** | **0.00%** | `▏` |
| 27 | ▼ -4 | **Cincinnati Reds** | NL Central | 57 - 61 | 78.8 | **1.9%** | **0.0%** | **0.00%** | `▏` |
| 28 | ▼ -7 | **Pittsburgh Pirates** | NL Central | 58 - 64 | 75.1 | **0.1%** | **0.0%** | **0.00%** | `▏` |
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

print("Successfully updated README.md table with calibrated gamma = 0.12 results!")
