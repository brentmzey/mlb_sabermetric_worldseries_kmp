# 🗄️ PocketHost / PocketBase Querying, Backup, & Local Deployment Guide

This comprehensive guide documents how to **query the latest multi-dimensional sabermetric data** stored in PocketHost / PocketBase, how to **automate complete database backups**, and how to **run the database locally as standalone SQLite or a local PocketBase server instance**.

---

## 1. Architectural Overview & Collection Namespace

The database uses the **Hungarian Relational Notation** paired with **64-bit UTC Epoch Milliseconds** (`int_created_epoch_ms_utc`, `int_updated_epoch_ms_utc`) and non-destructive status flags (`bool_is_active`, `str_status_code`):

```
┌─────────────────────────────────────────────────────────────────────────────────────────────┐
│                           POCKETHOST / POCKETBASE CLOUD INSTANCE                            │
│                        https://mlb-sabermetric-worldseries.pockethost.io                    │
├─────────────────────────────────────────────────────────────────────────────────────────────┤
│  [i_] INPUTS   │ i_mlb_teams, i_team_season_inputs, i_market_odds_inputs, i_expert_media   │
│  [m_] MODELS   │ m_simulation_runs, m_latent_quality_estimates, m_four_pillar_metrics        │
│  [s_] SUMMARY  │ s_divisional_standings_aggregates, s_league_aggregates, s_head_to_head     │
│  [o_] OUTPUTS  │ o_playoff_series_simulations, o_rank_movements                              │
│  [f_] FINALS   │ f_world_series_leaderboard, f_cubs_scenario_analysis                        │
│  [tbl_] PANEL  │ tbl_mlb_teams, tbl_simulation_runs, tbl_team_snapshots, tbl_rank_movements   │
└─────────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Querying PocketHost / PocketBase Data

### A. PocketBase REST Filter Dialect & Query Rules

All collections support structured query filtering, sorting, pagination, and relational expansions:

* **Query Latest Active Record for a Franchise**:
  ```
  filter=(str_team_code='CHC' && bool_is_active=true)&sort=-int_updated_epoch_ms_utc&limit=1
  ```
* **Query Top 10 World Series Contenders for the Current Simulation Run**:
  ```
  filter=(bool_is_active=true)&sort=int_sim_rank&perPage=10
  ```
* **Query All Teams in a Specific Division with High Starting Pitching Quality**:
  ```
  filter=(str_league='NL' && str_division='CENTRAL' && dbl_pitching_score>1.05 && bool_is_active=true)
  ```

---

### B. Polyglot Query Examples

#### 1. 🌐 cURL / HTTP REST (CLI)

```bash
# Query top 5 World Series contenders
curl -s "https://mlb-sabermetric-worldseries.pockethost.io/api/collections/f_world_series_leaderboard/records?filter=(bool_is_active=true)&sort=int_sim_rank&perPage=5" | jq .

# Query Chicago Cubs 4-Pillar Sabermetric Metrics
curl -s "https://mlb-sabermetric-worldseries.pockethost.io/api/collections/m_four_pillar_metrics/records?filter=(str_team_code='CHC'%26%26bool_is_active=true)&sort=-int_updated_epoch_ms_utc&limit=1" | jq .
```

#### 2. 🐍 Python 3.10+ (Programmatic Query Utility)

Run the included standalone query utility:
```bash
python3 scripts/query_latest_pockethost_data.py
```

Or execute custom inline Python queries:
```python
import urllib.request, urllib.parse, json

base_url = "https://mlb-sabermetric-worldseries.pockethost.io"
params = urllib.parse.urlencode({
    "filter": "(bool_is_active=true)",
    "sort": "int_sim_rank",
    "perPage": 5
})

req = urllib.request.Request(f"{base_url}/api/collections/f_world_series_leaderboard/records?{params}")
with urllib.request.urlopen(req) as resp:
    data = json.loads(resp.read().decode())
    for item in data.get("items", []):
        print(f"#{item['int_sim_rank']} {item['str_team_name']} ({item['str_team_code']}): {item['dbl_world_series_win_prob']*100:.2f}% WS Win Prob")
```

#### 3. 🅺 Kotlin Multiplatform (Ktor / PocketHost Client)

```kotlin
import com.sabermetrics.worldseries.repository.HungarianQueryBuilder
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

suspend fun fetchLatestCubsLeaderboard(client: HttpClient): String {
    val filterQuery = HungarianQueryBuilder.buildLatestActiveTeamFilter("CHC")
    val response: HttpResponse = client.get("https://mlb-sabermetric-worldseries.pockethost.io/api/collections/f_world_series_leaderboard/records?$filterQuery")
    return response.bodyAsText()
}
```

#### 4. 🟨 JavaScript / TypeScript (Official PocketBase SDK)

```typescript
import PocketBase from "pocketbase";

const pb = new PocketBase("https://mlb-sabermetric-worldseries.pockethost.io");

// Fetch active World Series leaderboard sorted by rank
const leaderboard = await pb.collection("f_world_series_leaderboard").getFullList({
    filter: "bool_is_active = true",
    sort: "int_sim_rank"
});

leaderboard.forEach(row => {
    console.log(`${row.int_sim_rank}. ${row.str_team_name}: ${(row.dbl_world_series_win_prob * 100).toFixed(2)}%`);
});
```

---

## 3. Database Backup & JSON Export

### A. Automated One-Command Backup Tool

Run the automated backup script to generate both a **timestamped JSON snapshot archive** and a **local standalone SQLite database**:

```bash
python3 scripts/backup_and_export_pockethost.py
```

**Artifacts Generated**:
* `output_datasets/pockethost_backup_latest.json`: Complete dump of all Hungarian and panel collections.
* `output_datasets/pockethost_backup_YYYYMMDD_HHMMSS.json`: Timestamped snapshot archive.
* `output_datasets/mlb_sabermetric_local.db`: Standalone zero-dependency SQLite database with applied schemas, check constraints, and analytical views.

---

## 4. Running the Database Locally as SQLite

The replicated SQLite database ([`output_datasets/mlb_sabermetric_local.db`](file:///Users/brentzey/personal/mlb_sabermetric_worldseries_kmp/output_datasets/mlb_sabermetric_local.db)) requires zero servers or daemons and can be queried directly via standard SQL:

### A. SQLite Interactive CLI

```bash
# Query the pre-materialized active championship leaderboard view
sqlite3 output_datasets/mlb_sabermetric_local.db "
SELECT sim_rank, team_code, team_name, league, division, expected_wins, world_series_win_prob, visual_bar
FROM vw_latest_active_world_series_leaderboard
LIMIT 10;
"
```

### B. Advanced Relational SQL Analytical Queries

```sql
-- Query 1: Pennant and World Series Probabilities by Division
SELECT 
    str_league,
    str_division,
    COUNT(*) AS total_teams,
    ROUND(SUM(dbl_world_series_win_prob) * 100, 2) AS total_division_ws_prob_pct,
    ROUND(AVG(dbl_expected_season_wins), 1) AS avg_expected_wins
FROM f_world_series_leaderboard
WHERE bool_is_active = 1
GROUP BY str_league, str_division
ORDER BY total_division_ws_prob_pct DESC;

-- Query 2: Cross-Join Franchises with Starting Pitching and Bullpen Quality
SELECT 
    t.str_team_code,
    t.str_team_name,
    t.str_ballpark,
    i.dbl_top3_ace_era,
    i.dbl_bullpen_wpa,
    l.dbl_world_series_win_prob
FROM i_mlb_teams t
JOIN i_team_season_inputs i ON t.str_team_code = i.str_team_code
JOIN f_world_series_leaderboard l ON t.str_team_code = l.str_team_code
WHERE t.bool_is_active = 1 AND i.bool_is_active = 1 AND l.bool_is_active = 1
ORDER BY l.int_sim_rank ASC
LIMIT 10;
```

---

## 5. Running Locally as a Standalone PocketBase Server

To run a fully functional local clone of PocketHost on your development machine:

### Step 1: Download PocketBase Binary
```bash
# macOS (Apple Silicon / ARM64)
curl -L -o pocketbase.zip https://github.com/pocketbase/pocketbase/releases/download/v0.22.20/pocketbase_0.22.20_darwin_arm64.zip
unzip pocketbase.zip -d ./local_pb && rm pocketbase.zip

# Linux (x86_64)
# curl -L -o pocketbase.zip https://github.com/pocketbase/pocketbase/releases/download/v0.22.20/pocketbase_0.22.20_linux_amd64.zip
```

### Step 2: Start Local PocketBase Server
```bash
./local_pb/pocketbase serve --dir=./local_pb/pb_data --http="127.0.0.1:8090"
```
* **Local Web Admin Dashboard**: `http://127.0.0.1:8090/_/`
* **Local REST API**: `http://127.0.0.1:8090/api/`

### Step 3: Import Schema into Local Instance
Use the provided Hungarian schema definition ([`docs/schema/pockethost_hungarian_schema.json`](file:///Users/brentzey/personal/mlb_sabermetric_worldseries_kmp/docs/schema/pockethost_hungarian_schema.json)) or panel schema ([`docs/schema/pockethost_collections.json`](file:///Users/brentzey/personal/mlb_sabermetric_worldseries_kmp/docs/schema/pockethost_collections.json)) via the Web Admin UI:
1. Navigate to **Settings** > **Import collections**.
2. Upload `docs/schema/pockethost_hungarian_schema.json` and click **Confirm**.

### Step 4: Populate Local PocketBase with Data
Run the sync script targeted at your local instance:
```bash
POCKETHOST_URL="http://127.0.0.1:8090" \
POCKETHOST_ADMIN_EMAIL="admin@local.test" \
POCKETHOST_ADMIN_PASSWORD="local_password_123456" \
python3 scripts/migrate_and_sync_hungarian.py
```

---

## 6. Automated Validation & Verification

* **Python Query Suite**: [`scripts/query_latest_pockethost_data.py`](file:///Users/brentzey/personal/mlb_sabermetric_worldseries_kmp/scripts/query_latest_pockethost_data.py)
* **Backup & Replicator**: [`scripts/backup_and_export_pockethost.py`](file:///Users/brentzey/personal/mlb_sabermetric_worldseries_kmp/scripts/backup_and_export_pockethost.py)
* **SQL DDL & Views**: [`docs/schema/mlb_domain_schema.sql`](file:///Users/brentzey/personal/mlb_sabermetric_worldseries_kmp/docs/schema/mlb_domain_schema.sql)
