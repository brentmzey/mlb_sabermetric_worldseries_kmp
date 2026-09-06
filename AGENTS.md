# 🤖 AGENTS.md (AI / Agent Developer Guidelines)

Welcome, fellow AI Agents and LLM Assistants! This document outlines the standard operating procedures, architectural boundaries, and exact commands required to maintain, execute, and refresh the **MLB Sabermetric World Series Suite**.

## 🚀 1. The "Full Refresh" Routine

When the human user asks you to "do a full refresh," "run the gamut," "fetch data," or "update the models," you must execute the entire data pipeline. 

### 🌟 The Master Command
The easiest and most comprehensive way to run a full refresh is via the master shell script:
```bash
cd /Users/brentzey/personal/mlb_sabermetric_worldseries_kmp
./scripts/full_refresh_and_sync.sh
```

### ⚙️ What the Master Script Does (Step-by-Step Breakdown)
If you need to run steps individually for debugging, here is the sequence:

1. **Live Data Fetch & Sabermetric Ingestion**
   ```bash
   python3 scripts/fetch_and_update_data.py
   ```
   *Fetches live standings, run differentials, and Last-10 game splits from the MLB Stats API.*

2. **Kotlin Multiplatform Monte Carlo Causal Simulation**
   ```bash
   ./gradlew run
   ```
   *Executes 10,000 World Series simulation runs.*
   *Calculates Latent Quality Theta ($\theta_i$) and Causal Instrumental Variables (IV).*
   *Synchronizes the cloud PocketHost/PocketBase database.*
   *Generates high-resolution PNG charts in `docs/charts/`.*
   *Exports the local SQLite database to `output_datasets/mlb_sabermetrics_local.sqlite`.*

3. **Verification & Test Coverage**
   ```bash
   ./gradlew test jacocoTestReport
   ```
   *Ensures the core statistical models remain pristine.*

4. **PostgreSQL & Dual-DB Replication**
   ```bash
   python3 scripts/pockethost_to_postgres_replicator.py
   ```
   *Pulls all 17 collections from PocketHost.*
   *Idempotently mirrors them to the local PostgreSQL instance (port 15432) into **both** `local_database` and `postgres` databases.*

---

## 📊 2. Documentation & Markdown Updates
After a successful model run, the underlying data and charts will change.
- Review the generated charts in `docs/charts/`.
- If new trends emerge (e.g., a massive upset in the simulated World Series odds), you may be asked to rewrite portions of `README.md` or the deep-dive documents in `docs/` to reflect the new causal archetypes.
- **Always preserve mathematical LaTeX equations** when updating markdown files.

---

## 🗄️ 3. Database Interoperability
We maintain a strict multi-database architecture:
1. **PocketHost (Cloud)**: The source of truth for the web UI. Syncs via `./gradlew run`.
2. **SQLite (Local)**: Created natively by Kotlin (`output_datasets/mlb_sabermetrics_local.sqlite`).
3. **PostgreSQL (Local Docker)**: Port `15432`. Accessible via `local_user` / `local_password`. 
   - *Note*: Always ensure queries against Postgres use `SET search_path TO public;` or explicitly qualify schemas (`public.i_mlb_teams`) because standard clients connect to the `postgres` default db.

---

## 🛠️ 4. The Companion Repository
There is a companion functional library repository: **`mlb-statsapi-kmp`** 
- Location: `/Users/brentzey/personal/mlb-statsapi-kmp`
- If you modify the stats ingestion logic here, consider if the multiplatform wrapper in `mlb-statsapi-kmp` also needs to be updated. Both repos must maintain zero warnings and perfect integrations.

**Execute your tasks cleanly, utilize Arrow KT functional paradigms where possible, and always verify before concluding your turn.**
