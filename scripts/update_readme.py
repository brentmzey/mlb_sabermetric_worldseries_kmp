#!/usr/bin/env python3
"""
Updates README.md leaderboard table with the freshest Bayesian-Adjusted Rest-of-Season simulation statistics.
"""

new_table = """| Rank | Movement | Team Name | League & Div | Record | Expected Wins | Playoff % | Pennant % | World Series Win Prob % | Visual Bar |
| :---: | :---: | :--- | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| 🥇 1 | ▲ +3 | **Los Angeles Dodgers** | NL West | 72 - 48 | 95.0 | **99.8%** | **30.0%** | **22.37%** | `████████████████` |
| 🥈 2 | ▲ +1 | **Atlanta Braves** | NL East | 73 - 48 | 98.5 | **100.0%** | **25.6%** | **16.45%** | `████████████` |
| 🥉 3 | ▲ +3 | **New York Yankees** | AL East | 67 - 52 | 91.8 | **99.9%** | **26.6%** | **12.27%** | `████████` |
| 4 | ▼ -3 | **Milwaukee Brewers** | NL Central | 74 - 47 | 98.1 | **100.0%** | **18.9%** | **12.27%** | `████████` |
| 5 | — | **Chicago Cubs** | NL Central | 71 - 50 | 96.9 | **100.0%** | **15.4%** | **9.37%** | `███████` |
| 6 | ▼ -4 | **Tampa Bay Rays** | AL East | 74 - 46 | 101.5 | **100.0%** | **23.7%** | **7.41%** | `██████` |
| 7 | ▲ +5 | **Houston Astros** | AL West | 62 - 60 | 82.0 | **69.1%** | **13.8%** | **4.80%** | `███` |
| 8 | — | **Boston Red Sox** | AL East | 64 - 56 | 86.6 | **95.3%** | **9.9%** | **2.96%** | `██` |
| 9 | ▲ +8 | **Detroit Tigers** | AL Central | 59 - 61 | 82.4 | **61.1%** | **9.6%** | **2.70%** | `██` |
| 10 | ▼ -3 | **San Diego Padres** | NL West | 65 - 57 | 87.3 | **70.6%** | **4.8%** | **2.59%** | `██` |
| 11 | ▼ -1 | **Philadelphia Phillies** | NL East | 64 - 58 | 84.5 | **41.5%** | **2.6%** | **1.46%** | `█` |
| 12 | ▲ +3 | **Texas Rangers** | AL West | 60 - 60 | 80.8 | **44.6%** | **4.8%** | **1.33%** | `█` |
| 13 | ▼ -4 | **Arizona Diamondbacks** | NL West | 64 - 58 | 84.6 | **32.7%** | **1.7%** | **0.81%** | `█` |
| 14 | ▲ +2 | **Minnesota Twins** | AL Central | 60 - 62 | 78.8 | **18.8%** | **2.6%** | **0.71%** | `▏` |
| 15 | ▼ -2 | **Chicago White Sox** | AL Central | 61 - 57 | 82.9 | **64.7%** | **4.5%** | **0.63%** | `▏` |
| 16 | ▲ +4 | **Toronto Blue Jays** | AL East | 59 - 63 | 79.7 | **27.0%** | **2.0%** | **0.57%** | `▏` |
| 17 | ▲ +2 | **Cleveland Guardians** | AL Central | 59 - 62 | 77.4 | **9.2%** | **1.5%** | **0.48%** | `▏` |
| 18 | ▼ -4 | **St. Louis Cardinals** | NL Central | 61 - 60 | 83.2 | **22.3%** | **0.7%** | **0.34%** | `▏` |
| 19 | ▲ +3 | **Baltimore Orioles** | AL East | 58 - 63 | 77.0 | **8.2%** | **0.8%** | **0.24%** | `▏` |
| 20 | ▼ -9 | **Miami Marlins** | NL East | 62 - 59 | 83.8 | **30.1%** | **0.4%** | **0.14%** | `▏` |
| 21 | ▲ +3 | **Seattle Mariners** | AL West | 56 - 64 | 74.4 | **2.1%** | **0.3%** | **0.07%** | `▏` |
| 22 | ▼ -4 | **Washington Nationals** | NL East | 59 - 63 | 78.0 | **1.2%** | **0.0%** | **0.01%** | `▏` |
| 23 | — | **Cincinnati Reds** | NL Central | 57 - 61 | 78.7 | **1.8%** | **0.0%** | **0.01%** | `▏` |
| 24 | ▼ -3 | **Pittsburgh Pirates** | NL Central | 58 - 64 | 75.2 | **0.1%** | **0.0%** | **0.01%** | `▏` |
| 25 | ▲ +2 | **Kansas City Royals** | AL Central | 49 - 72 | 64.8 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 26 | ▲ +3 | **Oakland Athletics** | AL West | 47 - 74 | 61.1 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 27 | ▲ +3 | **Los Angeles Angels** | AL West | 46 - 74 | 62.7 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 28 | ▼ -3 | **New York Mets** | NL East | 53 - 69 | 71.9 | **0.0%** | **0.0%** | **0.00%** | `▏` |
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
