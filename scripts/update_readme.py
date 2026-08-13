#!/usr/bin/env python3
"""
Updates README.md leaderboard table with the freshest 2026 recency & consistency simulation statistics.
"""

new_table = """| Rank | Movement | Team Name | League & Div | Record | Expected Wins | Playoff % | Pennant % | World Series Win Prob % | Visual Bar |
| :---: | :---: | :--- | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| 🥇 1 | ▲ +3 | **Los Angeles Dodgers** | NL West | 72 - 48 | 100.3 | **100.0%** | **39.7%** | **28.94%** | `████████████████` |
| 🥈 2 | ▲ +4 | **New York Yankees** | AL East | 67 - 52 | 93.7 | **100.0%** | **34.0%** | **15.18%** | `████████` |
| 🥉 3 | — | **Atlanta Braves** | NL East | 73 - 48 | 98.2 | **100.0%** | **21.4%** | **13.50%** | `███████` |
| 4 | ▼ -3 | **Milwaukee Brewers** | NL Central | 74 - 47 | 99.0 | **100.0%** | **18.9%** | **11.70%** | `██████` |
| 5 | — | **Chicago Cubs** | NL Central | 71 - 50 | 95.7 | **100.0%** | **13.5%** | **8.03%** | `████` |
| 6 | ▲ +11 | **Detroit Tigers** | AL Central | 59 - 61 | 93.7 | **100.0%** | **21.3%** | **6.01%** | `███` |
| 7 | ▲ +1 | **Boston Red Sox** | AL East | 64 - 56 | 92.9 | **100.0%** | **15.0%** | **4.37%** | `██` |
| 8 | ▼ -6 | **Tampa Bay Rays** | AL East | 74 - 46 | 89.8 | **100.0%** | **12.5%** | **3.95%** | `██` |
| 9 | ▲ +3 | **Houston Astros** | AL West | 62 - 60 | 77.5 | **37.4%** | **6.1%** | **2.18%** | `█` |
| 10 | ▲ +14 | **Seattle Mariners** | AL West | 56 - 64 | 77.5 | **38.2%** | **4.7%** | **1.79%** | `█` |
| 11 | ▼ -2 | **Arizona Diamondbacks** | NL West | 64 - 58 | 82.5 | **33.1%** | **1.8%** | **1.02%** | `█` |
| 12 | ▲ +1 | **Chicago White Sox** | AL Central | 61 - 57 | 85.9 | **98.6%** | **4.0%** | **0.67%** | `▏` |
| 13 | ▲ +2 | **Texas Rangers** | AL West | 60 - 60 | 76.5 | **24.6%** | **2.3%** | **0.67%** | `▏` |
| 14 | ▼ -7 | **San Diego Padres** | NL West | 65 - 57 | 81.1 | **17.5%** | **1.2%** | **0.44%** | `▏` |
| 15 | ▲ +6 | **Pittsburgh Pirates** | NL Central | 58 - 64 | 82.6 | **33.6%** | **0.8%** | **0.39%** | `▏` |
| 16 | ▼ -5 | **Miami Marlins** | NL East | 62 - 59 | 84.3 | **54.8%** | **1.0%** | **0.31%** | `▏` |
| 17 | ▼ -7 | **Philadelphia Phillies** | NL East | 64 - 58 | 80.1 | **8.8%** | **0.5%** | **0.28%** | `▏` |
| 18 | — | **Washington Nationals** | NL East | 59 - 63 | 82.8 | **36.9%** | **0.8%** | **0.28%** | `▏` |
| 19 | ▼ -5 | **St. Louis Cardinals** | NL Central | 61 - 60 | 80.9 | **15.4%** | **0.5%** | **0.25%** | `▏` |
| 20 | ▼ -1 | **Cleveland Guardians** | AL Central | 59 - 62 | 77.0 | **0.6%** | **0.1%** | **0.02%** | `▏` |
| 21 | ▲ +1 | **Baltimore Orioles** | AL East | 58 - 63 | 76.2 | **0.0%** | **0.0%** | **0.01%** | `▏` |
| 22 | ▼ -6 | **Minnesota Twins** | AL Central | 60 - 62 | 77.0 | **0.5%** | **0.0%** | **0.01%** | `▏` |
| 23 | ▼ -3 | **Toronto Blue Jays** | AL East | 59 - 63 | 73.2 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 24 | ▲ +3 | **Kansas City Royals** | AL Central | 49 - 72 | 66.2 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 25 | ▲ +4 | **Oakland Athletics** | AL West | 47 - 74 | 59.6 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 26 | ▲ +4 | **Los Angeles Angels** | AL West | 46 - 74 | 68.1 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 27 | ▼ -2 | **New York Mets** | NL East | 53 - 69 | 74.5 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 28 | ▼ -5 | **Cincinnati Reds** | NL Central | 57 - 61 | 70.7 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 29 | ▼ -3 | **San Francisco Giants** | NL West | 50 - 71 | 71.9 | **0.0%** | **0.0%** | **0.00%** | `▏` |
| 30 | ▼ -2 | **Colorado Rockies** | NL West | 48 - 73 | 67.6 | **0.0%** | **0.0%** | **0.00%** | `▏`"""

readme_path = "README.md"
with open(readme_path, "r") as f:
    content = f.read()

import re
pattern = re.compile(r"\| Rank \| Movement \| Team Name \|.*?\| 30 \|.*?\n", re.DOTALL)
updated_content = pattern.sub(new_table + "\n", content)

with open(readme_path, "w") as f:
    f.write(updated_content)

print("Successfully updated README.md table!")
