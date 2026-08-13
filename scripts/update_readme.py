#!/usr/bin/env python3
"""
Updates README.md leaderboard table with the freshest Rest-of-Season Anchored simulation statistics.
"""

new_table = """| Rank | Movement | Team Name | League & Div | Record | Expected Wins | Playoff % | Pennant % | World Series Win Prob % | Visual Bar |
| :---: | :---: | :--- | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| 🥇 1 | ▲ +3 | **Los Angeles Dodgers** | NL West | 72 - 48 | 95.0 | **99.7%** | **29.6%** | **21.72%** | `████████████████` |
| 🥈 2 | ▲ +1 | **Atlanta Braves** | NL East | 73 - 48 | 98.6 | **100.0%** | **25.7%** | **16.49%** | `████████████` |
| 🥉 3 | ▼ -2 | **Milwaukee Brewers** | NL Central | 74 - 47 | 98.1 | **100.0%** | **18.7%** | **11.56%** | `████████` |
| 4 | ▲ +2 | **New York Yankees** | AL East | 67 - 52 | 91.9 | **99.9%** | **25.3%** | **11.50%** | `████████` |
| 5 | — | **Chicago Cubs** | NL Central | 71 - 50 | 96.9 | **100.0%** | **15.3%** | **9.27%** | `███████` |
| 6 | ▼ -4 | **Tampa Bay Rays** | AL East | 74 - 46 | 101.1 | **100.0%** | **23.5%** | **7.80%** | `██████` |
| 7 | ▲ +5 | **Houston Astros** | AL West | 62 - 60 | 81.8 | **69.8%** | **13.2%** | **4.53%** | `███` |
| 8 | ▲ +9 | **Detroit Tigers** | AL Central | 59 - 61 | 83.1 | **68.6%** | **11.4%** | **3.68%** | `██` |
| 9 | ▼ -1 | **Boston Red Sox** | AL East | 64 - 56 | 86.9 | **95.7%** | **10.4%** | **3.52%** | `██` |
| 10 | ▼ -3 | **San Diego Padres** | NL West | 65 - 57 | 87.1 | **69.0%** | **5.2%** | **2.88%** | `██` |
| 11 | ▼ -1 | **Philadelphia Phillies** | NL East | 64 - 58 | 84.2 | **39.2%** | **2.5%** | **1.39%** | `█` |
| 12 | ▲ +3 | **Texas Rangers** | AL West | 60 - 60 | 80.4 | **40.6%** | **4.5%** | **1.12%** | `█` |
| 13 | ▼ -4 | **Arizona Diamondbacks** | NL West | 64 - 58 | 84.5 | **32.9%** | **1.8%** | **1.00%** | `█` |
| 14 | ▲ +6 | **Toronto Blue Jays** | AL East | 59 - 63 | 79.5 | **25.7%** | **2.1%** | **0.77%** | `▏` |
| 15 | ▲ +1 | **Minnesota Twins** | AL Central | 60 - 62 | 78.6 | **16.8%** | **2.5%** | **0.74%** | `▏` |
| 16 | ▼ -3 | **Chicago White Sox** | AL Central | 61 - 57 | 83.0 | **64.3%** | **4.6%** | **0.71%** | `▏` |
| 17 | ▲ +2 | **Cleveland Guardians** | AL Central | 59 - 62 | 77.2 | **8.6%** | **1.5%** | **0.50%** | `▏` |
| 18 | ▼ -4 | **St. Louis Cardinals** | NL Central | 61 - 60 | 83.3 | **24.2%** | **0.8%** | **0.32%** | `▏` |
| 19 | ▲ +3 | **Baltimore Orioles** | AL East | 58 - 63 | 76.9 | **7.5%** | **0.8%** | **0.26%** | `▏` |
| 20 | ▼ -9 | **Miami Marlins** | NL East | 62 - 59 | 83.9 | **32.1%** | **0.4%** | **0.18%** | `▏` |
| 21 | ▲ +3 | **Seattle Mariners** | AL West | 56 - 64 | 74.4 | **2.4%** | **0.4%** | **0.05%** | `▏` |
| 22 | ▼ -4 | **Washington Nationals** | NL East | 59 - 63 | 78.2 | **1.5%** | **0.0%** | **0.01%** | `▏` |
| 23 | ▲ +4 | **Kansas City Royals** | AL Central | 49 - 72 | 64.8 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 24 | ▲ +5 | **Oakland Athletics** | AL West | 47 - 74 | 61.0 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 25 | ▲ +5 | **Los Angeles Angels** | AL West | 46 - 74 | 62.9 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 26 | ▼ -1 | **New York Mets** | NL East | 53 - 69 | 72.1 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 27 | ▼ -4 | **Cincinnati Reds** | NL Central | 57 - 61 | 78.3 | **1.4%** | **0.0%** | **0.00%** | `▏` |
| 28 | ▼ -7 | **Pittsburgh Pirates** | NL Central | 58 - 64 | 75.4 | **0.1%** | **0.0%** | **0.00%** | `▏` |
| 29 | ▼ -3 | **San Francisco Giants** | NL West | 50 - 71 | 66.5 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 30 | ▼ -2 | **Colorado Rockies** | NL West | 48 - 73 | 64.7 | **0.0%** | **0.0%** | **0.00%** | `▏`"""

readme_path = "README.md"
with open(readme_path, "r") as f:
    content = f.read()

import re
pattern = re.compile(r"\| Rank \| Movement \| Team Name \|.*?\| 30 \|.*?\n", re.DOTALL)
updated_content = pattern.sub(new_table + "\n", content)

with open(readme_path, "w") as f:
    f.write(updated_content)

print("Successfully updated README.md table!")
