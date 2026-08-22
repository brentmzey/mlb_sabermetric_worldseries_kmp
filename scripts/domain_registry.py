#!/usr/bin/env python3
"""
Canonical Python Domain Registry & Cross-Language Enum Definitions for MLB Sabermetrics.
Provides strict enum types, franchise registries, statistical pillar specifications,
postseason playoff round rules, and Hungarian collection schema mappings synchronized
with Kotlin Multiplatform (KMP) and docs/schema/mlb_domain_registry.json.
"""
from __future__ import annotations

import os
import json
from enum import Enum
from dataclasses import dataclass
from typing import (
    Any,
    Dict,
    Final,
    List,
    Mapping,
    Optional,
    Sequence,
    TextIO,
    TypedDict,
    Union,
    cast
)


# ==============================================================================
# Cross-Language Strongly Typed Enums (Kotlin <-> JSON <-> Python Parity)
# ==============================================================================

class League(str, Enum):
    """Major League Baseball League designations."""
    AL = "AL"
    NL = "NL"

    def __str__(self) -> str:
        return self.value


class Division(str, Enum):
    """Major League Baseball Division geographic alignments."""
    EAST = "EAST"
    CENTRAL = "CENTRAL"
    WEST = "WEST"

    def __str__(self) -> str:
        return self.value


class MlbTeamCode(str, Enum):
    """Strongly-typed enum of all 30 Major League Baseball franchise abbreviation codes."""
    # AL East
    NYY = "NYY"
    BAL = "BAL"
    BOS = "BOS"
    TBD = "TBD"
    TOR = "TOR"
    # AL Central
    CLE = "CLE"
    KC = "KC"
    DET = "DET"
    MIN = "MIN"
    CWS = "CWS"
    # AL West
    HOU = "HOU"
    SEA = "SEA"
    TEX = "TEX"
    OAK = "OAK"
    LAA = "LAA"
    # NL East
    PHI = "PHI"
    ATL = "ATL"
    NYM = "NYM"
    WSH = "WSH"
    MIA = "MIA"
    # NL Central
    MIL = "MIL"
    CHC = "CHC"
    STL = "STL"
    CIN = "CIN"
    PIT = "PIT"
    # NL West
    LAD = "LAD"
    SD = "SD"
    ARI = "ARI"
    SF = "SF"
    COL = "COL"

    def __str__(self) -> str:
        return self.value


class StatPillarType(str, Enum):
    """The 4 Core Empirical Dimensions of October Postseason Quality."""
    OFFENSE = "OFFENSE"
    DEFENSE = "DEFENSE"
    STARTING_PITCHING = "STARTING_PITCHING"
    BULLPEN_LEVERAGE = "BULLPEN_LEVERAGE"

    def __str__(self) -> str:
        return self.value


class PostseasonRound(str, Enum):
    """Postseason playoff series rounds in Major League Baseball."""
    WILD_CARD = "WILD_CARD"
    DIVISION_SERIES = "DIVISION_SERIES"
    LEAGUE_CHAMPIONSHIP = "LEAGUE_CHAMPIONSHIP"
    WORLD_SERIES = "WORLD_SERIES"

    def __str__(self) -> str:
        return self.value


class HungarianCollectionPrefix(str, Enum):
    """Hungarian database relational collection tier prefixes."""
    INPUT = "i_"
    MODEL = "m_"
    SUMMARY = "s_"
    OUTPUT = "o_"
    FINAL = "f_"

    def __str__(self) -> str:
        return self.value


class RecordStatusCode(str, Enum):
    """Lifecycle status codes for non-destructive time-series records."""
    ACTIVE = "ACTIVE"
    INACTIVE = "INACTIVE"
    SUPERSEDED = "SUPERSEDED"
    ARCHIVED = "ARCHIVED"

    def __str__(self) -> str:
        return self.value


# ==============================================================================
# Domain Dataclasses
# ==============================================================================

@dataclass(frozen=True)
class TeamFranchiseMetadata:
    """Immutable domain model for an MLB franchise."""
    code: MlbTeamCode
    full_name: str
    league: League
    division: Division
    city: str
    ballpark: str
    founded_year: int
    mlb_api_id: int


@dataclass(frozen=True)
class StatPillarDefinition:
    """Definition and empirical weighting for a sabermetric pillar."""
    code: StatPillarType
    name: str
    weight: float
    primary_metric: str
    secondary_metric: str
    description: str


@dataclass(frozen=True)
class PostseasonRoundDefinition:
    """Playoff series tournament round structure and rules."""
    code: PostseasonRound
    name: str
    best_of: int
    wins_to_advance: int
    home_field_format: str


@dataclass(frozen=True)
class HungarianPrefixDefinition:
    """Hungarian database collection tier specification."""
    prefix: str
    category: str
    description: str


# ==============================================================================
# Domain Registry Engine
# ==============================================================================

class DomainRegistry:
    """
    Centralized, thread-safe domain registry mapping teams, leagues, divisions,
    pillars, rounds, and database prefixes.
    """
    def __init__(self, registry_file_path: Optional[str] = None) -> None:
        if not registry_file_path:
            script_dir: str = os.path.dirname(__file__)
            registry_file_path = os.path.abspath(
                os.path.join(script_dir, "..", "docs", "schema", "mlb_domain_registry.json")
            )
        self.registry_file_path: str = registry_file_path
        self._teams_by_code: Dict[str, TeamFranchiseMetadata] = {}
        self._teams_by_mlb_id: Dict[int, TeamFranchiseMetadata] = {}
        self._pillars_by_code: Dict[str, StatPillarDefinition] = {}
        self._rounds_by_code: Dict[str, PostseasonRoundDefinition] = {}
        self._prefixes_by_code: Dict[str, HungarianPrefixDefinition] = {}
        self._load_from_json()

    def _load_from_json(self) -> None:
        """Loads canonical registry data from JSON schema file."""
        if not os.path.exists(self.registry_file_path):
            raise FileNotFoundError(f"Domain registry JSON not found: {self.registry_file_path}")

        f: TextIO
        with open(self.registry_file_path, "r", encoding="utf-8") as f:
            data: Dict[str, Any] = json.load(f)

        # Parse Teams
        t_raw: Dict[str, Any]
        for t_raw in data.get("teams", []):
            code_enum: MlbTeamCode = MlbTeamCode(t_raw["code"])
            team_meta: TeamFranchiseMetadata = TeamFranchiseMetadata(
                code=code_enum,
                full_name=str(t_raw["fullName"]),
                league=League(t_raw["league"]),
                division=Division(t_raw["division"]),
                city=str(t_raw["city"]),
                ballpark=str(t_raw["ballpark"]),
                founded_year=int(t_raw["foundedYear"]),
                mlb_api_id=int(t_raw["mlbApiId"])
            )
            self._teams_by_code[team_meta.code.value] = team_meta
            self._teams_by_mlb_id[team_meta.mlb_api_id] = team_meta

        # Parse Pillars
        p_raw: Dict[str, Any]
        for p_raw in data.get("stat_pillars", []):
            pillar_enum: StatPillarType = StatPillarType(p_raw["code"])
            pillar_def: StatPillarDefinition = StatPillarDefinition(
                code=pillar_enum,
                name=str(p_raw["name"]),
                weight=float(p_raw["weight"]),
                primary_metric=str(p_raw["primaryMetric"]),
                secondary_metric=str(p_raw["secondaryMetric"]),
                description=str(p_raw["description"])
            )
            self._pillars_by_code[pillar_def.code.value] = pillar_def

        # Parse Rounds
        r_raw: Dict[str, Any]
        for r_raw in data.get("postseason_rounds", []):
            round_enum: PostseasonRound = PostseasonRound(r_raw["code"])
            round_def: PostseasonRoundDefinition = PostseasonRoundDefinition(
                code=round_enum,
                name=str(r_raw["name"]),
                best_of=int(r_raw["bestOf"]),
                wins_to_advance=int(r_raw["winsToAdvance"]),
                home_field_format=str(r_raw["homeFieldFormat"])
            )
            self._rounds_by_code[round_def.code.value] = round_def

        # Parse Prefixes
        pre_raw: Dict[str, Any]
        for pre_raw in data.get("hungarian_prefixes", []):
            prefix_def: HungarianPrefixDefinition = HungarianPrefixDefinition(
                prefix=str(pre_raw["prefix"]),
                category=str(pre_raw["category"]),
                description=str(pre_raw["description"])
            )
            self._prefixes_by_code[prefix_def.prefix] = prefix_def

    def get_team(self, code: Union[str, MlbTeamCode]) -> TeamFranchiseMetadata:
        """Retrieves franchise metadata by team code."""
        key: str = code.value if isinstance(code, MlbTeamCode) else str(code).strip().upper()
        if key not in self._teams_by_code:
            raise KeyError(f"Invalid or unregistered MLB team code: '{code}'")
        return self._teams_by_code[key]

    def get_team_by_mlb_id(self, mlb_id: int) -> Optional[TeamFranchiseMetadata]:
        """Retrieves franchise metadata by official MLB Stats API ID."""
        return self._teams_by_mlb_id.get(mlb_id)

    def get_all_teams(self) -> Sequence[TeamFranchiseMetadata]:
        """Returns all 30 MLB franchises."""
        return list(self._teams_by_code.values())

    def get_teams_by_league(self, league: League) -> Sequence[TeamFranchiseMetadata]:
        """Returns all 15 teams in the specified League (AL or NL)."""
        return [t for t in self._teams_by_code.values() if t.league == league]

    def get_teams_by_division(self, league: League, division: Division) -> Sequence[TeamFranchiseMetadata]:
        """Returns all 5 teams in the specified League and Division."""
        return [t for t in self._teams_by_code.values() if t.league == league and t.division == division]

    def get_all_pillars(self) -> Sequence[StatPillarDefinition]:
        """Returns the 4 core postseason sabermetric pillar specifications."""
        return list(self._pillars_by_code.values())

    def get_pillar(self, pillar: StatPillarType) -> StatPillarDefinition:
        """Returns the specific pillar definition."""
        return self._pillars_by_code[pillar.value]

    def get_all_rounds(self) -> Sequence[PostseasonRoundDefinition]:
        """Returns the 4 playoff series tournament rounds."""
        return list(self._rounds_by_code.values())

    def get_round(self, round_type: PostseasonRound) -> PostseasonRoundDefinition:
        """Returns rules for a specific playoff tournament round."""
        return self._rounds_by_code[round_type.value]


# Global Domain Registry Singleton Instance
MLB_REGISTRY: Final[DomainRegistry] = DomainRegistry()
