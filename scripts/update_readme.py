#!/usr/bin/env python3
"""
Updates README.md leaderboard table with the Multi-Dimensional Relative z-Score Form Estimator simulation statistics.
"""

new_table = """| Rank | Movement | Team Name | League & Div | Record | Expected Wins | Playoff % | Pennant % | World Series Win Prob % | Visual Bar |
| :---: | :---: | :--- | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| 🥇 1 | ▲ +3 | **Los Angeles Dodgers** | NL West | 72 - 48 | 94.9 | **99.8%** | **42.3%** | **35.07%** | `████████████████` |
| 🥈 2 | ▲ +1 | **Atlanta Braves** | NL East | 73 - 48 | 98.5 | **100.0%** | **27.6%** | **19.98%** | `███████████` |
| 🥉 3 | ▲ +3 | **New York Yankees** | AL East | 67 - 52 | 91.7 | **99.9%** | **37.6%** | **15.21%** | `████████` |
| 4 | ▼ -3 | **Milwaukee Brewers** | NL Central | 74 - 47 | 98.1 | **100.0%** | **13.2%** | **8.18%** | `████` |
| 5 | — | **Chicago Cubs** | NL Central | 71 - 50 | 96.8 | **100.0%** | **10.7%** | **6.54%** | `███` |
| 6 | ▼ -4 | **Tampa Bay Rays** | AL East | 74 - 46 | 101.6 | **100.0%** | **21.8%** | **4.42%** | `██` |
| 7 | ▲ +5 | **Houston Astros** | AL West | 62 - 60 | 82.0 | **70.1%** | **14.3%** | **3.45%** | `██` |
| 8 | ▼ -1 | **San Diego Padres** | NL West | 65 - 57 | 87.4 | **71.1%** | **3.1%** | **1.61%** | `█` |
| 9 | ▼ -1 | **Boston Red Sox** | AL East | 64 - 56 | 86.6 | **95.5%** | **7.1%** | **1.27%** | `█` |
| 10 | — | **Philadelphia Phillies** | NL East | 64 - 58 | 84.5 | **40.9%** | **2.2%** | **1.17%** | `█` |
| 11 | ▲ +6 | **Detroit Tigers** | AL Central | 59 - 61 | 82.3 | **60.6%** | **7.2%** | **1.10%** | `█` |
| 12 | ▲ +3 | **Texas Rangers** | AL West | 60 - 60 | 80.7 | **43.7%** | **3.9%** | **0.56%** | `▏` |
| 13 | ▲ +3 | **Minnesota Twins** | AL Central | 60 - 62 | 78.8 | **19.1%** | **2.1%** | **0.33%** | `▏` |
| 14 | ▼ -5 | **Arizona Diamondbacks** | NL West | 64 - 58 | 84.7 | **33.5%** | **0.7%** | **0.26%** | `▏` |
| 15 | ▲ +4 | **Cleveland Guardians** | AL Central | 59 - 62 | 77.3 | **9.4%** | **1.3%** | **0.24%** | `▏` |
| 16 | ▼ -3 | **Chicago White Sox** | AL Central | 61 - 57 | 82.8 | **63.5%** | **2.5%** | **0.23%** | `▏` |
| 17 | ▲ +3 | **Toronto Blue Jays** | AL East | 59 - 63 | 79.8 | **28.3%** | **1.6%** | **0.19%** | `▏` |
| 18 | ▼ -4 | **St. Louis Cardinals** | NL Central | 61 - 60 | 83.2 | **21.9%** | **0.3%** | **0.08%** | `▏` |
| 19 | ▲ +3 | **Baltimore Orioles** | AL East | 58 - 63 | 77.0 | **7.9%** | **0.6%** | **0.06%** | `▏` |
| 20 | ▲ +4 | **Seattle Mariners** | AL West | 56 - 64 | 74.3 | **2.0%** | **0.3%** | **0.04%** | `▏` |
| 21 | ▲ +2 | **Cincinnati Reds** | NL Central | 57 - 61 | 78.8 | **1.9%** | **0.0%** | **0.01%** | `▏` |
| 22 | ▲ +5 | **Kansas City Royals** | AL Central | 49 - 72 | 64.8 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 23 | ▲ +6 | **Oakland Athletics** | AL West | 47 - 74 | 61.2 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 24 | ▲ +6 | **Los Angeles Angels** | AL West | 46 - 74 | 62.7 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 25 | — | **New York Mets** | NL East | 53 - 69 | 72.0 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 26 | ▼ -8 | **Washington Nationals** | NL East | 59 - 63 | 78.0 | **1.0%** | **0.0%** | **0.00%** | `▏` |
| 27 | ▼ -16 | **Miami Marlins** | NL East | 62 - 59 | 83.8 | **29.8%** | **0.1%** | **0.00%** | `▏` |
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

print("Successfully updated README.md table with Multi-Dimensional Relative Form Estimator results!")
