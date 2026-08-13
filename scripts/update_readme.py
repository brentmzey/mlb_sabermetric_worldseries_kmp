#!/usr/bin/env python3
"""
Updates README.md leaderboard table with the Bill James Log5 & Brian Kenny October Compression simulation statistics.
"""

new_table = """| Rank | Movement | Team Name | League & Div | Record | Expected Wins | Playoff % | Pennant % | World Series Win Prob % | Visual Bar |
| :---: | :---: | :--- | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| 🥇 1 | ▲ +3 | **Los Angeles Dodgers** | NL West | 72 - 48 | 95.2 | **99.7%** | **28.6%** | **20.95%** | `███████████` |
| 🥈 2 | ▲ +1 | **Atlanta Braves** | NL East | 73 - 48 | 99.7 | **100.0%** | **29.0%** | **19.52%** | `██████████` |
| 🥉 3 | ▲ +3 | **New York Yankees** | AL East | 67 - 52 | 92.5 | **99.9%** | **32.4%** | **15.29%** | `████████` |
| 4 | ▼ -3 | **Milwaukee Brewers** | NL Central | 74 - 47 | 99.0 | **100.0%** | **17.5%** | **10.85%** | `██████` |
| 5 | — | **Chicago Cubs** | NL Central | 71 - 50 | 98.2 | **100.0%** | **16.1%** | **9.69%** | `█████` |
| 6 | ▼ -4 | **Tampa Bay Rays** | AL East | 74 - 46 | 103.3 | **100.0%** | **25.6%** | **8.17%** | `████` |
| 7 | ▲ +5 | **Houston Astros** | AL West | 62 - 60 | 81.9 | **70.3%** | **15.1%** | **4.68%** | `██` |
| 8 | ▲ +9 | **Detroit Tigers** | AL Central | 59 - 61 | 83.1 | **69.1%** | **11.2%** | **2.68%** | `█` |
| 9 | ▼ -2 | **San Diego Padres** | NL West | 65 - 57 | 87.8 | **73.6%** | **4.5%** | **2.46%** | `█` |
| 10 | ▼ -2 | **Boston Red Sox** | AL East | 64 - 56 | 87.1 | **96.3%** | **6.8%** | **1.72%** | `█` |
| 11 | ▼ -1 | **Philadelphia Phillies** | NL East | 64 - 58 | 84.6 | **38.6%** | **2.9%** | **1.60%** | `█` |
| 12 | ▲ +3 | **Texas Rangers** | AL West | 60 - 60 | 80.5 | **42.2%** | **3.1%** | **0.64%** | `▏` |
| 13 | ▼ -4 | **Arizona Diamondbacks** | NL West | 64 - 58 | 84.8 | **31.0%** | **1.0%** | **0.52%** | `▏` |
| 14 | ▲ +2 | **Minnesota Twins** | AL Central | 60 - 62 | 78.4 | **14.5%** | **1.5%** | **0.36%** | `▏` |
| 15 | ▲ +4 | **Cleveland Guardians** | AL Central | 59 - 62 | 76.7 | **6.9%** | **1.1%** | **0.29%** | `▏` |
| 16 | ▲ +4 | **Toronto Blue Jays** | AL East | 59 - 63 | 79.9 | **29.0%** | **1.6%** | **0.26%** | `▏` |
| 17 | ▼ -3 | **St. Louis Cardinals** | NL Central | 61 - 60 | 83.7 | **23.5%** | **0.3%** | **0.14%** | `▏` |
| 18 | ▲ +4 | **Baltimore Orioles** | AL East | 58 - 63 | 76.5 | **6.0%** | **0.4%** | **0.08%** | `▏` |
| 19 | ▼ -6 | **Chicago White Sox** | AL Central | 61 - 57 | 82.9 | **64.4%** | **1.1%** | **0.08%** | `▏` |
| 20 | ▲ +4 | **Seattle Mariners** | AL West | 56 - 64 | 73.6 | **1.4%** | **0.1%** | **0.02%** | `▏` |
| 21 | ▲ +6 | **Kansas City Royals** | AL Central | 49 - 72 | 63.6 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 22 | ▲ +7 | **Oakland Athletics** | AL West | 47 - 74 | 59.8 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 23 | ▲ +7 | **Los Angeles Angels** | AL West | 46 - 74 | 61.5 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 24 | ▲ +1 | **New York Mets** | NL East | 53 - 69 | 71.7 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 25 | ▼ -7 | **Washington Nationals** | NL East | 59 - 63 | 77.8 | **0.8%** | **0.0%** | **0.00%** | `▏` |
| 26 | ▼ -15 | **Miami Marlins** | NL East | 62 - 59 | 84.2 | **31.4%** | **0.1%** | **0.00%** | `▏` |
| 27 | ▼ -4 | **Cincinnati Reds** | NL Central | 57 - 61 | 78.6 | **1.4%** | **0.0%** | **0.00%** | `▏` |
| 28 | ▼ -7 | **Pittsburgh Pirates** | NL Central | 58 - 64 | 74.6 | **0.0%** | **0.0%** | **0.00%** | `▏` |
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
