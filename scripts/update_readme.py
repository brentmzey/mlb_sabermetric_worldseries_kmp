#!/usr/bin/env python3
"""
Updates README.md leaderboard table with the Hot Streak Momentum & Accelerated Recency simulation statistics.
"""

new_table = """| Rank | Movement | Team Name | League & Div | Record | Expected Wins | Playoff % | Pennant % | World Series Win Prob % | Visual Bar |
| :---: | :---: | :--- | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| 🥇 1 | ▲ +2 | **Atlanta Braves** | NL East | 73 - 48 | 99.1 | **100.0%** | **34.1%** | **24.67%** | `████████████████` |
| 🥈 2 | ▲ +2 | **Los Angeles Dodgers** | NL West | 72 - 48 | 93.0 | **98.2%** | **30.1%** | **23.20%** | `███████████████` |
| 🥉 3 | ▲ +3 | **New York Yankees** | AL East | 67 - 52 | 91.9 | **99.9%** | **35.2%** | **15.51%** | `█████████` |
| 4 | ▲ +1 | **Chicago Cubs** | NL Central | 71 - 50 | 98.1 | **100.0%** | **17.0%** | **10.89%** | `███████` |
| 5 | ▼ -3 | **Tampa Bay Rays** | AL East | 74 - 46 | 103.5 | **100.0%** | **25.0%** | **6.90%** | `████` |
| 6 | ▼ -5 | **Milwaukee Brewers** | NL Central | 74 - 47 | 97.5 | **99.9%** | **11.9%** | **6.70%** | `████` |
| 7 | ▲ +5 | **Houston Astros** | AL West | 62 - 60 | 82.0 | **69.8%** | **13.2%** | **3.62%** | `██` |
| 8 | ▼ -1 | **San Diego Padres** | NL West | 65 - 57 | 88.4 | **77.2%** | **4.5%** | **2.65%** | `█` |
| 9 | ▲ +8 | **Detroit Tigers** | AL Central | 59 - 61 | 83.4 | **72.6%** | **9.8%** | **1.98%** | `█` |
| 10 | ▼ -2 | **Boston Red Sox** | AL East | 64 - 56 | 86.2 | **93.6%** | **6.8%** | **1.28%** | `█` |
| 11 | ▼ -1 | **Philadelphia Phillies** | NL East | 64 - 58 | 84.5 | **34.6%** | **1.5%** | **0.79%** | `▏` |
| 12 | ▲ +8 | **Toronto Blue Jays** | AL East | 59 - 63 | 81.1 | **43.4%** | **2.5%** | **0.52%** | `▏` |
| 13 | ▲ +2 | **Texas Rangers** | AL West | 60 - 60 | 80.8 | **43.4%** | **3.4%** | **0.36%** | `▏` |
| 14 | ▲ +2 | **Minnesota Twins** | AL Central | 60 - 62 | 78.3 | **14.1%** | **1.2%** | **0.25%** | `▏` |
| 15 | ▼ -6 | **Arizona Diamondbacks** | NL West | 64 - 58 | 84.6 | **26.1%** | **0.4%** | **0.16%** | `▏` |
| 16 | ▲ +3 | **Cleveland Guardians** | AL Central | 59 - 62 | 76.2 | **4.8%** | **0.6%** | **0.14%** | `▏` |
| 17 | ▼ -3 | **St. Louis Cardinals** | NL Central | 61 - 60 | 84.4 | **29.7%** | **0.4%** | **0.13%** | `▏` |
| 18 | ▼ -5 | **Chicago White Sox** | AL Central | 61 - 57 | 82.0 | **51.8%** | **1.7%** | **0.11%** | `▏` |
| 19 | ▲ +3 | **Baltimore Orioles** | AL East | 58 - 63 | 76.5 | **5.5%** | **0.4%** | **0.07%** | `▏` |
| 20 | ▼ -9 | **Miami Marlins** | NL East | 62 - 59 | 84.3 | **30.8%** | **0.2%** | **0.06%** | `▏` |
| 21 | ▲ +3 | **Seattle Mariners** | AL West | 56 - 64 | 73.3 | **1.1%** | **0.1%** | **0.01%** | `▏` |
| 22 | ▲ +5 | **Kansas City Royals** | AL Central | 49 - 72 | 64.1 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 23 | ▲ +6 | **Oakland Athletics** | AL West | 47 - 74 | 60.1 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 24 | ▲ +6 | **Los Angeles Angels** | AL West | 46 - 74 | 62.6 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 25 | — | **New York Mets** | NL East | 53 - 69 | 72.9 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 26 | ▼ -8 | **Washington Nationals** | NL East | 59 - 63 | 77.4 | **0.5%** | **0.0%** | **0.00%** | `▏` |
| 27 | ▼ -4 | **Cincinnati Reds** | NL Central | 57 - 61 | 79.7 | **2.9%** | **0.0%** | **0.00%** | `▏` |
| 28 | ▼ -7 | **Pittsburgh Pirates** | NL Central | 58 - 64 | 73.5 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 29 | ▼ -3 | **San Francisco Giants** | NL West | 50 - 71 | 65.6 | **0.0%** | **0.0%** | **0.00%** | `▏` |
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
