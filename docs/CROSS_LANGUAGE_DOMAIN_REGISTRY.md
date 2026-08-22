# 🌐 Cross-Language Domain Registry & Strongly-Typed Component Enumeration

## 1. Architectural Overview & Single Source of Truth

The **MLB Sabermetric World Series Simulation Engine** maintains strict, compile-time and runtime type safety across its polyglot stack (**Kotlin Multiplatform** and **Python 3.10+**) via a centralized, canonical JSON domain contract:

```
                                  ┌──────────────────────────────────────────────┐
                                  │   docs/schema/mlb_domain_registry.json       │
                                  │     (Canonical Domain Registry Schema)       │
                                  └───────────────┬──────────────────────────────┘
                                                  │
                         ┌────────────────────────┴────────────────────────┐
                         ▼                                                 ▼
      ┌────────────────────────────────────┐             ┌────────────────────────────────────┐
      │     Kotlin Multiplatform (KMP)     │             │            Python 3.10+            │
      │   com.sabermetrics.worldseries...  │             │      scripts/domain_registry.py    │
      ├────────────────────────────────────┤             ├────────────────────────────────────┤
      │ • enum class League                │             │ • class League(str, Enum)          │
      │ • enum class Division              │             │ • class Division(str, Enum)        │
      │ • enum class MlbTeamId             │             │ • class MlbTeamCode(str, Enum)     │
      │ • enum class StatPillarType        │             │ • class StatPillarType(str, Enum)  │
      │ • enum class PostseasonRound       │             │ • class PostseasonRound(str, Enum) │
      │ • enum class HungarianPrefix       │             │ • class HungarianPrefix(str, Enum) │
      │ • enum class RecordStatusCode      │             │ • class RecordStatusCode(str, Enum)│
      └────────────────────────────────────┘             └────────────────────────────────────┘
```

---

## 2. Canonical JSON Schema Registry (`mlb_domain_registry.json`)

The JSON registry ([`docs/schema/mlb_domain_registry.json`](file:///Users/brentzey/personal/mlb_sabermetric_worldseries_kmp/docs/schema/mlb_domain_registry.json)) defines authoritative metadata, empirical weights, tournament formats, and relational database tiers:

* **30 MLB Franchises**: Abbreviation code, full name, league, division, home city, ballpark, founding year, and official MLB Stats API ID.
* **4 Postseason Sabermetric Pillars**: Weights, primary/secondary metrics, and mathematical descriptions.
* **4 Postseason Tournament Rounds**: Series lengths, required win thresholds, and home-field advantage distribution formats.
* **5 Hungarian Collection Relational Tiers**: Relational database namespaces (`i_`, `m_`, `s_`, `o_`, `f_`).
* **4 Time-Series Record Status Codes**: Immutable row lifecycle status indicators.

---

## 3. Cross-Language Enumeration & Type Parity Matrix

### A. Major League Baseball Franchises (`MlbTeamId` / `MlbTeamCode`)

| Team Code | Full Franchise Name | League | Division | Home Ballpark | City | Founded | MLB API ID |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `NYY` | New York Yankees | `AL` | `EAST` | Yankee Stadium | New York, NY | 1901 | 147 |
| `BAL` | Baltimore Orioles | `AL` | `EAST` | Oriole Park at Camden Yards | Baltimore, MD | 1901 | 110 |
| `BOS` | Boston Red Sox | `AL` | `EAST` | Fenway Park | Boston, MA | 1901 | 111 |
| `TBD` | Tampa Bay Rays | `AL` | `EAST` | Tropicana Field | St. Petersburg, FL | 1998 | 139 |
| `TOR` | Toronto Blue Jays | `AL` | `EAST` | Rogers Centre | Toronto, ON | 1977 | 141 |
| `CLE` | Cleveland Guardians | `AL` | `CENTRAL` | Progressive Field | Cleveland, OH | 1901 | 114 |
| `KC` | Kansas City Royals | `AL` | `CENTRAL` | Kauffman Stadium | Kansas City, MO | 1969 | 118 |
| `DET` | Detroit Tigers | `AL` | `CENTRAL` | Comerica Park | Detroit, MI | 1901 | 116 |
| `MIN` | Minnesota Twins | `AL` | `CENTRAL` | Target Field | Minneapolis, MN | 1901 | 142 |
| `CWS` | Chicago White Sox | `AL` | `CENTRAL` | Guaranteed Rate Field | Chicago, IL | 1901 | 145 |
| `HOU` | Houston Astros | `AL` | `WEST` | Daikin Park | Houston, TX | 1962 | 117 |
| `SEA` | Seattle Mariners | `AL` | `WEST` | T-Mobile Park | Seattle, WA | 1977 | 136 |
| `TEX` | Texas Rangers | `AL` | `WEST` | Globe Life Field | Arlington, TX | 1961 | 140 |
| `OAK` | Oakland Athletics | `AL` | `WEST` | Sutter Health Park | Sacramento, CA | 1901 | 133 |
| `LAA` | Los Angeles Angels | `AL` | `WEST` | Angel Stadium | Anaheim, CA | 1961 | 108 |
| `PHI` | Philadelphia Phillies | `NL` | `EAST` | Citizens Bank Park | Philadelphia, PA | 1883 | 143 |
| `ATL` | Atlanta Braves | `NL` | `EAST` | Truist Park | Atlanta, GA | 1871 | 144 |
| `NYM` | New York Mets | `NL` | `EAST` | Citi Field | New York, NY | 1962 | 121 |
| `WSH` | Washington Nationals | `NL` | `EAST` | Nationals Park | Washington, D.C. | 1969 | 120 |
| `MIA` | Miami Marlins | `NL` | `EAST` | loanDepot park | Miami, FL | 1993 | 146 |
| `MIL` | Milwaukee Brewers | `NL` | `CENTRAL` | American Family Field | Milwaukee, WI | 1969 | 158 |
| `CHC` | Chicago Cubs | `NL` | `CENTRAL` | Wrigley Field | Chicago, IL | 1876 | 112 |
| `STL` | St. Louis Cardinals | `NL` | `CENTRAL` | Busch Stadium | St. Louis, MO | 1882 | 138 |
| `CIN` | Cincinnati Reds | `NL` | `CENTRAL` | Great American Ball Park | Cincinnati, OH | 1881 | 113 |
| `PIT` | Pittsburgh Pirates | `NL` | `CENTRAL` | PNC Park | Pittsburgh, PA | 1882 | 134 |
| `LAD` | Los Angeles Dodgers | `NL` | `WEST` | Dodger Stadium | Los Angeles, CA | 1883 | 119 |
| `SD` | San Diego Padres | `NL` | `WEST` | Petco Park | San Diego, CA | 1969 | 135 |
| `ARI` | Arizona Diamondbacks | `NL` | `WEST` | Chase Field | Phoenix, AZ | 1998 | 109 |
| `SF` | San Francisco Giants | `NL` | `WEST` | Oracle Park | San Francisco, CA | 1883 | 137 |
| `COL` | Colorado Rockies | `NL` | `WEST` | Coors Field | Denver, CO | 1993 | 115 |

---

### B. Sabermetric Statistical Pillars (`StatPillarType`)

| Pillar Enum | Display Name | Weight | Primary Metric | Secondary Metric | Postseason Role & Description |
| :--- | :--- | :---: | :--- | :--- | :--- |
| `OFFENSE` | Offensive Run Creation | `25%` | `wRC+` | `wOBA` | Park-adjusted offensive run generation and on-base weighted outcomes. |
| `DEFENSE` | Defensive Efficiency & Prevention | `15%` | `Defensive_Efficiency` | `OAA_DRS` | Run conversion suppression, Outs Above Average (OAA), and DRS. |
| `STARTING_PITCHING` | Top-3 Starting Rotation Ace Quality | `35%` | `Top3_Ace_ERA` | `FIP` | Postseason compressed rotation effectiveness of frontline starting aces. |
| `BULLPEN_LEVERAGE` | Bullpen High-Leverage Reliability | `25%` | `Bullpen_WPA` | `xFIP` | Late-inning Win Probability Added (WPA) and high-leverage strikeout sustainability. |

$$\sum_{p \in \text{Pillars}} \text{Weight}_p = 0.25 + 0.15 + 0.35 + 0.25 = 1.00 \quad (100\%)$$

---

### C. Postseason Playoff Tournament Rounds (`PostseasonRound`)

| Round Enum | Series Name | Best Of | Wins Needed | Home Field Format |
| :--- | :--- | :---: | :---: | :---: |
| `WILD_CARD` | Wild Card Series | `3` | `2` | `2-1` (Higher seed hosts all) |
| `DIVISION_SERIES` | Division Series (LDS) | `5` | `3` | `2-2-1` |
| `LEAGUE_CHAMPIONSHIP` | League Championship Series (LCS) | `7` | `4` | `2-3-2` |
| `WORLD_SERIES` | World Series (Fall Classic) | `7` | `4` | `2-3-2` |

---

### D. Hungarian Database Relational Collection Tiers (`HungarianCollectionPrefix`)

| Prefix | Category | Relational Tier Role | Example Collection |
| :---: | :--- | :--- | :--- |
| `i_` | `INPUT` | Raw ingested time-series data from external APIs and markets | `i_team_season_inputs`, `i_market_odds_inputs` |
| `m_` | `MODEL` | Econometric 2SLS causal estimates and Bayesian indices | `m_latent_quality_estimates`, `m_four_pillar_metrics` |
| `s_` | `SUMMARY` | Materialized aggregations across divisions and leagues | `s_divisional_standings_aggregates`, `s_league_aggregates` |
| `o_` | `OUTPUT` | Stochastic simulations and head-to-head matchup matrices | `o_playoff_series_simulations`, `o_rank_movements` |
| `f_` | `FINAL` | Authoritative customer-facing championship leaderboard | `f_world_series_leaderboard`, `f_cubs_scenario_analysis` |

---

## 4. Cross-Language Usage Examples

### Kotlin Multiplatform Usage

```kotlin
import com.sabermetrics.worldseries.model.MlbTeamId
import com.sabermetrics.worldseries.model.StatPillarType
import com.sabermetrics.worldseries.model.PostseasonRound

// Strict team parsing with rich metadata
val team: MlbTeamId = MlbTeamId.fromCode("CHC")
println("Team: ${team.fullName}, Ballpark: ${team.ballpark}, MLB API: ${team.mlbApiId}")

// Sabermetric pillar evaluation
val acePillar = StatPillarType.STARTING_PITCHING
println("Weight: ${acePillar.weight}, Metric: ${acePillar.primaryMetric}")

// Postseason round series structure
val wsRound = PostseasonRound.WORLD_SERIES
println("Series: ${wsRound.displayName}, Best of: ${wsRound.bestOf}")
```

### Python 3.10+ Usage

```python
from domain_registry import MLB_REGISTRY, MlbTeamCode, StatPillarType, PostseasonRound

# Centralized franchise lookup
cubs = MLB_REGISTRY.get_team(MlbTeamCode.CHC)
print(f"Franchise: {cubs.full_name}, Ballpark: {cubs.ballpark}, Founded: {cubs.founded_year}")

# Pillar inspection
sp_pillar = MLB_REGISTRY.get_pillar(StatPillarType.STARTING_PITCHING)
print(f"Pillar: {sp_pillar.name}, Weight: {sp_pillar.weight * 100}%")

# Postseason round rules
ws = MLB_REGISTRY.get_round(PostseasonRound.WORLD_SERIES)
print(f"Round: {ws.name}, Games: {ws.best_of}, Format: {ws.home_field_format}")
```

---

## 5. Automated Validation & Test Suite

The cross-language registry is verified continuously in the CI/CD pipeline across both test runners:

* **Kotlin Test Suite**: [`src/commonTest/kotlin/com/sabermetrics/worldseries/SabermetricTest.kt`](file:///Users/brentzey/personal/mlb_sabermetric_worldseries_kmp/src/commonTest/kotlin/com/sabermetrics/worldseries/SabermetricTest.kt) (`testMlbTeamIdEnumAndParsing`, `testCrossLanguageDomainRegistryEnums`).
* **Python Test Suite**: [`tests/test_domain_registry.py`](file:///Users/brentzey/personal/mlb_sabermetric_worldseries_kmp/tests/test_domain_registry.py) (`test_franchise_counts_and_lookups`, `test_stat_pillar_weights_conservation`, `test_postseason_rounds`).
