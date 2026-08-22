#!/usr/bin/env python3
"""
Unit tests for Python Domain Registry and cross-language JSON enum parity.
"""
import os
import sys
import unittest

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "scripts")))
from domain_registry import (
    MLB_REGISTRY,
    League,
    Division,
    MlbTeamCode,
    StatPillarType,
    PostseasonRound,
    HungarianCollectionPrefix,
    RecordStatusCode
)


class TestDomainRegistry(unittest.TestCase):

    def test_franchise_counts_and_lookups(self):
        teams = MLB_REGISTRY.get_all_teams()
        self.assertEqual(len(teams), 30)

        # 15 AL, 15 NL
        al_teams = MLB_REGISTRY.get_teams_by_league(League.AL)
        nl_teams = MLB_REGISTRY.get_teams_by_league(League.NL)
        self.assertEqual(len(al_teams), 15)
        self.assertEqual(len(nl_teams), 15)

        # 5 per division
        for lg in [League.AL, League.NL]:
            for div in [Division.EAST, Division.CENTRAL, Division.WEST]:
                div_teams = MLB_REGISTRY.get_teams_by_division(lg, div)
                self.assertEqual(len(div_teams), 5, f"Division {lg} {div} does not have 5 teams")

        # Lookup by code
        nyy = MLB_REGISTRY.get_team(MlbTeamCode.NYY)
        self.assertEqual(nyy.full_name, "New York Yankees")
        self.assertEqual(nyy.league, League.AL)
        self.assertEqual(nyy.division, Division.EAST)
        self.assertEqual(nyy.mlb_api_id, 147)

        # Lookup by MLB API ID
        lad = MLB_REGISTRY.get_team_by_mlb_id(119)
        self.assertIsNotNone(lad)
        self.assertEqual(lad.code, MlbTeamCode.LAD)
        self.assertEqual(lad.full_name, "Los Angeles Dodgers")

    def test_stat_pillar_weights_conservation(self):
        pillars = MLB_REGISTRY.get_all_pillars()
        self.assertEqual(len(pillars), 4)
        total_weight = sum(p.weight for p in pillars)
        self.assertAlmostEqual(total_weight, 1.00, places=4)

        offense = MLB_REGISTRY.get_pillar(StatPillarType.OFFENSE)
        self.assertEqual(offense.primary_metric, "wRC+")
        self.assertEqual(offense.weight, 0.25)

        sp = MLB_REGISTRY.get_pillar(StatPillarType.STARTING_PITCHING)
        self.assertEqual(sp.primary_metric, "Top3_Ace_ERA")
        self.assertEqual(sp.weight, 0.35)

    def test_postseason_rounds(self):
        rounds = MLB_REGISTRY.get_all_rounds()
        self.assertEqual(len(rounds), 4)

        ws = MLB_REGISTRY.get_round(PostseasonRound.WORLD_SERIES)
        self.assertEqual(ws.best_of, 7)
        self.assertEqual(ws.wins_to_advance, 4)
        self.assertEqual(ws.home_field_format, "2-3-2")

        wc = MLB_REGISTRY.get_round(PostseasonRound.WILD_CARD)
        self.assertEqual(wc.best_of, 3)
        self.assertEqual(wc.wins_to_advance, 2)


if __name__ == "__main__":
    unittest.main()
