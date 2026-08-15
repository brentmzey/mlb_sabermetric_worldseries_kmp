#!/usr/bin/env python3
"""
Updates README.md leaderboard table with the Bill James Log5 & Brian Kenny October Compression simulation statistics.
"""

new_table = """| Rank | Movement | Team Name | League & Div | Record | Expected Wins | Playoff % | Pennant % | World Series Win Prob % | Visual Bar |
| :---: | :---: | :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| 🥇 1 | ▲ +3 | **Los Angeles Dodgers** | NL West | 72 - 48 | 99.4 | **100.0%** | **35.1%** | **23.53%** | `████████████` |
| 🥈 2 | — | **Tampa Bay Rays** | AL East | 74 - 46 | 103.2 | **100.0%** | **29.6%** | **12.56%** | `██████` |
| 🥉 3 | ▼ -2 | **Milwaukee Brewers** | NL Central | 74 - 47 | 99.0 | **100.0%** | **20.1%** | **12.14%** | `██████` |
| 4 | ▲ +1 | **Chicago Cubs** | NL Central | 71 - 50 | 98.2 | **100.0%** | **18.9%** | **11.31%** | `██████` |
| 5 | ▲ +1 | **New York Yankees** | AL East | 67 - 52 | 92.5 | **100.0%** | **22.0%** | **10.65%** | `█████` |
| 6 | ▼ -3 | **Atlanta Braves** | NL East | 73 - 48 | 96.9 | **100.0%** | **11.7%** | **5.46%** | `███` |
| 7 | ▲ +10 | **Detroit Tigers** | AL Central | 59 - 61 | 83.0 | **68.3%** | **13.5%** | **5.39%** | `███` |
| 8 | ▲ +4 | **Houston Astros** | AL West | 62 - 60 | 81.9 | **70.0%** | **12.8%** | **5.05%** | `███` |
| 9 | ▼ -2 | **San Diego Padres** | NL West | 65 - 57 | 87.9 | **73.8%** | **6.8%** | **3.63%** | `██` |
| 10 | ▼ -2 | **Boston Red Sox** | AL East | 64 - 56 | 87.1 | **96.1%** | **9.0%** | **3.17%** | `██` |
| 11 | ▼ -1 | **Philadelphia Phillies** | NL East | 64 - 58 | 84.6 | **38.6%** | **3.8%** | **1.93%** | `█` |
| 12 | ▲ +3 | **Texas Rangers** | AL West | 60 - 60 | 80.5 | **42.5%** | **4.3%** | **1.12%** | `▏` |
| 13 | ▼ -4 | **Arizona Diamondbacks** | NL West | 64 - 58 | 84.8 | **31.6%** | **2.1%** | **0.95%** | `▏` |
| 14 | ▼ -1 | **Chicago White Sox** | AL Central | 61 - 57 | 83.0 | **64.8%** | **3.4%** | **0.76%** | `▏` |
| 15 | ▲ +1 | **Minnesota Twins** | AL Central | 60 - 62 | 78.4 | **15.2%** | **1.8%** | **0.55%** | `▏` |
| 16 | ▲ +4 | **Toronto Blue Jays** | AL East | 59 - 63 | 79.8 | **29.0%** | **1.9%** | **0.54%** | `▏` |
| 17 | ▼ -3 | **St. Louis Cardinals** | NL Central | 61 - 60 | 83.7 | **23.4%** | **1.0%** | **0.43%** | `▏` |
| 18 | ▲ +1 | **Cleveland Guardians** | AL Central | 59 - 62 | 76.7 | **6.9%** | **1.0%** | **0.40%** | `▏` |
| 19 | ▲ +3 | **Baltimore Orioles** | AL East | 58 - 63 | 76.4 | **6.0%** | **0.6%** | **0.19%** | `▏` |
| 20 | ▼ -9 | **Miami Marlins** | NL East | 62 - 59 | 84.2 | **30.6%** | **0.6%** | **0.18%** | `▏` |
| 21 | ▲ +3 | **Seattle Mariners** | AL West | 56 - 64 | 73.6 | **1.2%** | **0.2%** | **0.04%** | `▏` |
| 22 | ▼ -4 | **Washington Nationals** | NL East | 59 - 63 | 77.8 | **0.8%** | **0.0%** | **0.01%** | `▏` |
| 23 | — | **Cincinnati Reds** | NL Central | 57 - 61 | 78.6 | **1.1%** | **0.0%** | **0.01%** | `▏` |
| 24 | ▲ +3 | **Kansas City Royals** | AL Central | 49 - 72 | 63.7 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 25 | ▲ +4 | **Oakland Athletics** | AL West | 47 - 74 | 59.8 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 26 | ▲ +4 | **Los Angeles Angels** | AL West | 46 - 74 | 61.6 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 27 | ▼ -2 | **New York Mets** | NL East | 53 - 69 | 71.6 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 28 | ▼ -7 | **Pittsburgh Pirates** | NL Central | 58 - 64 | 74.5 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 29 | ▼ -3 | **San Francisco Giants** | NL West | 50 - 71 | 65.3 | **0.0%** | **0.0%** | **0.00%** | `▏` |
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
