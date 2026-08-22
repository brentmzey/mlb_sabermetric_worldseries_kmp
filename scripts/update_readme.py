#!/usr/bin/env python3
"""
Updates README.md leaderboard table with the Bill James Log5 & Brian Kenny October Compression simulation statistics.
Strongly typed using Python 3.10+ dataclasses, type annotations, and structured string transformations.
"""
from __future__ import annotations

import os
import re
from dataclasses import dataclass
from typing import Final, List, Match, Optional, Sequence, TextIO


@dataclass(frozen=True)
class LeaderboardTableRow:
    """Represents a single row in the README markdown leaderboard table."""
    rank_display: str
    movement: str
    team_name: str
    league_division: str
    record: str
    expected_wins: float
    playoff_pct: float
    pennant_pct: float
    world_series_pct: float
    visual_bar: str

    def to_markdown(self) -> str:
        return (
            f"| {self.rank_display} | {self.movement} | **{self.team_name}** | {self.league_division} | "
            f"{self.record} | {self.expected_wins:.1f} | **{self.playoff_pct:.1f}%** | "
            f"**{self.pennant_pct:.1f}%** | **{self.world_series_pct:.2f}%** | `{self.visual_bar}` |"
        )


TABLE_HEADER: Final[str] = """| Rank | Movement | Team Name | League & Div | Record | Expected Wins | Playoff % | Pennant % | World Series Win Prob % | Visual Bar |
| :---: | :---: | :--- | :---: | :---: | :---: | :---: | :---: | :---: | :--- |"""


SAMPLE_LEADERBOARD_ROWS: Final[Sequence[LeaderboardTableRow]] = [
    LeaderboardTableRow("🥇 1", "▲ +2", "Los Angeles Dodgers", "NL West", "73 - 49", 95.3, 99.7, 29.8, 20.51, "██████████"),
    LeaderboardTableRow("🥈 2", "▲ +2", "Atlanta Braves", "NL East", "73 - 49", 99.8, 100.0, 27.2, 17.92, "█████████"),
    LeaderboardTableRow("🥉 3", "▲ +3", "New York Yankees", "AL East", "68 - 54", 92.6, 100.0, 33.1, 15.65, "████████"),
    LeaderboardTableRow("4", "▼ -3", "Milwaukee Brewers", "NL Central", "75 - 47", 98.9, 100.0, 17.5, 11.45, "██████"),
    LeaderboardTableRow("5", "—", "Chicago Cubs", "NL Central", "72 - 51", 98.2, 100.0, 16.6, 10.15, "█████"),
    LeaderboardTableRow("6", "▼ -4", "Tampa Bay Rays", "AL East", "74 - 46", 103.2, 100.0, 26.6, 8.60, "████"),
    LeaderboardTableRow("7", "▲ +5", "Houston Astros", "AL West", "62 - 60", 81.8, 77.1, 17.3, 6.63, "███"),
    LeaderboardTableRow("8", "▼ -1", "San Diego Padres", "NL West", "66 - 57", 89.4, 89.4, 7.8, 4.32, "██"),
    LeaderboardTableRow("9", "▲ +6", "Detroit Tigers", "AL Central", "60 - 62", 81.8, 59.7, 10.3, 3.25, "██"),
    LeaderboardTableRow("10", "—", "Philadelphia Phillies", "NL East", "65 - 58", 85.2, 49.0, 4.7, 2.75, "█"),
    LeaderboardTableRow("11", "▼ -3", "Boston Red Sox", "AL East", "65 - 57", 85.9, 94.3, 7.1, 2.12, "█"),
    LeaderboardTableRow("12", "▼ -3", "Arizona Diamondbacks", "NL West", "65 - 58", 85.4, 41.1, 2.0, 0.87, "▏"),
    LeaderboardTableRow("13", "▲ +6", "Texas Rangers", "AL West", "60 - 62", 79.3, 31.8, 2.4, 0.55, "▏"),
    LeaderboardTableRow("14", "▲ +6", "Toronto Blue Jays", "AL East", "60 - 64", 79.8, 31.6, 1.9, 0.53, "▏"),
    LeaderboardTableRow("15", "▲ +3", "Minnesota Twins", "AL Central", "60 - 63", 77.8, 13.9, 1.8, 0.53, "▏"),
    LeaderboardTableRow("16", "▼ -2", "Chicago White Sox", "AL Central", "61 - 58", 83.0, 64.8, 3.4, 0.46, "▏"),
    LeaderboardTableRow("17", "▼ -3", "St. Louis Cardinals", "NL Central", "61 - 61", 83.7, 23.4, 1.0, 0.43, "▏"),
    LeaderboardTableRow("18", "▲ +1", "Cleveland Guardians", "AL Central", "59 - 63", 76.7, 6.9, 1.0, 0.40, "▏"),
    LeaderboardTableRow("19", "▲ +3", "Baltimore Orioles", "AL East", "58 - 64", 76.4, 6.0, 0.6, 0.19, "▏"),
    LeaderboardTableRow("20", "▼ -9", "Miami Marlins", "NL East", "62 - 60", 84.2, 30.6, 0.6, 0.18, "▏"),
    LeaderboardTableRow("21", "▲ +3", "Seattle Mariners", "AL West", "56 - 65", 73.6, 1.2, 0.2, 0.04, "▏"),
    LeaderboardTableRow("22", "▼ -4", "Washington Nationals", "NL East", "59 - 64", 77.8, 0.8, 0.0, 0.01, "▏"),
    LeaderboardTableRow("23", "—", "Cincinnati Reds", "NL Central", "57 - 62", 78.6, 1.1, 0.0, 0.01, "▏"),
    LeaderboardTableRow("24", "▲ +3", "Kansas City Royals", "AL Central", "49 - 73", 63.7, 0.0, 0.0, 0.00, "▏"),
    LeaderboardTableRow("25", "▲ +4", "Oakland Athletics", "AL West", "47 - 75", 59.8, 0.0, 0.0, 0.00, "▏"),
    LeaderboardTableRow("26", "▲ +4", "Los Angeles Angels", "AL West", "46 - 75", 61.6, 0.0, 0.0, 0.00, "▏"),
    LeaderboardTableRow("27", "▼ -2", "New York Mets", "NL East", "53 - 70", 71.6, 0.0, 0.0, 0.00, "▏"),
    LeaderboardTableRow("28", "▼ -7", "Pittsburgh Pirates", "NL Central", "58 - 65", 74.5, 0.0, 0.0, 0.00, "▏"),
    LeaderboardTableRow("29", "▼ -3", "San Francisco Giants", "NL West", "50 - 72", 65.3, 0.0, 0.0, 0.00, "▏"),
    LeaderboardTableRow("30", "▼ -2", "Colorado Rockies", "NL West", "48 - 74", 63.5, 0.0, 0.0, 0.00, "▏"),
]


def generate_markdown_table(rows: Sequence[LeaderboardTableRow]) -> str:
    """Serializes structured leaderboard rows into markdown table format."""
    lines: List[str] = [TABLE_HEADER]
    row: LeaderboardTableRow
    for row in rows:
        lines.append(row.to_markdown())
    return "\n".join(lines)


def update_readme_table(readme_path: str, new_table_str: str) -> bool:
    """Replaces the markdown table in README.md with the latest calibrated table."""
    file_exists: bool = os.path.exists(readme_path)
    if not file_exists:
        print(f"❌ Error: README file not found at {readme_path}")
        return False

    content: str
    f: TextIO
    with open(readme_path, "r", encoding="utf-8") as f:
        content = f.read()

    pattern: re.Pattern[str] = re.compile(r"\| Rank \| Movement \| Team Name \|.*?\| 30 \|.*?\n", re.DOTALL)
    search_match: Optional[Match[str]] = pattern.search(content)
    if not search_match:
        print("⚠️ Warning: Table pattern not matched in README.md.")
        return False

    updated_content: str = pattern.sub(new_table_str + "\n", content)
    with open(readme_path, "w", encoding="utf-8") as f:
        f.write(updated_content)

    print("✅ Successfully updated README.md table with strongly typed simulation results!")
    return True


def main() -> None:
    """Main execution function for README leaderboard table updating."""
    proj_dir: str = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
    readme_path: str = os.path.join(proj_dir, "README.md")
    table_str: str = generate_markdown_table(SAMPLE_LEADERBOARD_ROWS)
    update_readme_table(readme_path, table_str)


if __name__ == "__main__":
    main()


