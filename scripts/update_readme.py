#!/usr/bin/env python3
"""
Updates README.md leaderboard table with the Bill James Log5 & Brian Kenny October Compression simulation statistics.
"""

new_table = """| Rank | Movement | Team Name | League & Div | Record | Expected Wins | Playoff % | Pennant % | World Series Win Prob % | Visual Bar |
| :---: | :---: | :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| 🥇 1 | ▲ +3 | **Los Angeles Dodgers** | NL West | 72 - 48 | 99.4 | **100.0%** | **48.3%** | **36.35%** | `██████████████████` |
| 🥈 2 | ▲ +4 | **New York Yankees** | AL East | 67 - 52 | 92.6 | **100.0%** | **32.4%** | **15.42%** | `████████` |
| 🥉 3 | ▼ -2 | **Milwaukee Brewers** | NL Central | 74 - 47 | 98.9 | **100.0%** | **17.6%** | **10.51%** | `██████` |
| 4 | ▲ +1 | **Chicago Cubs** | NL Central | 71 - 50 | 98.2 | **100.0%** | **17.3%** | **10.51%** | `██████` |
| 5 | ▼ -3 | **Tampa Bay Rays** | AL East | 74 - 46 | 103.3 | **100.0%** | **25.3%** | **7.79%** | `████` |
| 6 | ▲ +6 | **Houston Astros** | AL West | 62 - 60 | 81.9 | **69.5%** | **15.1%** | **4.94%** | `██` |
| 7 | ▲ +10 | **Detroit Tigers** | AL Central | 59 - 61 | 83.0 | **68.8%** | **11.1%** | **3.00%** | `█` |
| 8 | ▼ -1 | **San Diego Padres** | NL West | 65 - 57 | 87.9 | **74.4%** | **5.7%** | **2.99%** | `█` |
| 9 | ▼ -6 | **Atlanta Braves** | NL East | 73 - 48 | 96.9 | **99.9%** | **6.3%** | **2.69%** | `█` |
| 10 | ▼ -2 | **Boston Red Sox** | AL East | 64 - 56 | 87.0 | **96.1%** | **7.3%** | **1.88%** | `█` |
| 11 | ▼ -1 | **Philadelphia Phillies** | NL East | 64 - 58 | 84.5 | **37.6%** | **3.0%** | **1.68%** | `█` |
| 12 | ▼ -3 | **Arizona Diamondbacks** | NL West | 64 - 58 | 84.8 | **31.0%** | **1.2%** | **0.56%** | `▏` |
| 13 | ▲ +2 | **Texas Rangers** | AL West | 60 - 60 | 80.6 | **43.0%** | **3.1%** | **0.51%** | `▏` |
| 14 | ▲ +2 | **Minnesota Twins** | AL Central | 60 - 62 | 78.4 | **14.9%** | **1.5%** | **0.36%** | `▏` |
| 15 | ▲ +5 | **Toronto Blue Jays** | AL East | 59 - 63 | 79.9 | **28.9%** | **1.5%** | **0.23%** | `▏` |
| 16 | ▲ +3 | **Cleveland Guardians** | AL Central | 59 - 62 | 76.8 | **7.2%** | **1.1%** | **0.19%** | `▏` |
| 17 | ▼ -3 | **St. Louis Cardinals** | NL Central | 61 - 60 | 83.6 | **23.3%** | **0.5%** | **0.17%** | `▏` |
| 18 | ▼ -5 | **Chicago White Sox** | AL Central | 61 - 57 | 83.0 | **65.2%** | **1.1%** | **0.11%** | `▏` |
| 19 | ▲ +3 | **Baltimore Orioles** | AL East | 58 - 63 | 76.4 | **5.2%** | **0.4%** | **0.06%** | `▏` |
| 20 | ▲ +4 | **Seattle Mariners** | AL West | 56 - 64 | 73.6 | **1.3%** | **0.2%** | **0.04%** | `▏` |
| 21 | ▼ -10 | **Miami Marlins** | NL East | 62 - 59 | 84.2 | **31.7%** | **0.0%** | **0.01%** | `▏` |
| 22 | ▲ +5 | **Kansas City Royals** | AL Central | 49 - 72 | 63.6 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 23 | ▲ +6 | **Oakland Athletics** | AL West | 47 - 74 | 59.8 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 24 | ▲ +6 | **Los Angeles Angels** | AL West | 46 - 74 | 61.6 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 25 | — | **New York Mets** | NL East | 53 - 69 | 71.7 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 26 | ▼ -8 | **Washington Nationals** | NL East | 59 - 63 | 77.8 | **0.7%** | **0.0%** | **0.00%** | `▏` |
| 27 | ▼ -4 | **Cincinnati Reds** | NL Central | 57 - 61 | 78.6 | **1.5%** | **0.0%** | **0.00%** | `▏` |
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
