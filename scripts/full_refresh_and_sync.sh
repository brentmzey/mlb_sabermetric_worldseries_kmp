#!/usr/bin/env bash
# ==============================================================================
# ⚾ MLB Sabermetric World Series Suite - Full Refresh & Multi-DB Sync
# 1. Ingests clean 30-team open-source sabermetric matrix
# 2. Runs 10,000-iteration Monte Carlo World Series Simulation
# 3. Synchronizes PocketHost / PocketBase Cloud instance with exponential back-off
# 4. Exports local SQLite database file & standard SQL dump
# 5. Generates 8 high-resolution analytical PNG charts in docs/charts/
# 6. Populates local-db-stack SQLite, PostgreSQL (port 15432), and MySQL (port 13306)
# ==============================================================================

set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOCAL_STACK_DIR="${HOME}/personal/local-db-stack"

echo "=============================================================================="
echo " 🚀 MLB Sabermetrics: Starting Full Refresh & Multi-DB Sync Pipeline"
echo " Time: $(date)"
echo " Project Directory: $PROJECT_DIR"
echo "=============================================================================="

cd "$PROJECT_DIR"

# 1. Compile and execute simulation
echo ""
echo "⚾ [1/3] Running Monte Carlo Simulation, PocketHost Sync & Local DB Exports..."
./gradlew run

# 2. Run Test Suite & Generate JaCoCo Coverage Report
echo ""
echo "🧪 [2/3] Running Unit Test Suite & JaCoCo Coverage Report..."
./gradlew test jacocoTestReport

# 3. Populate Local DB Stack (SQLite, PostgreSQL, MySQL)
if [ -d "$LOCAL_STACK_DIR" ] && [ -f "$LOCAL_STACK_DIR/load_mlb_sabermetrics.sh" ]; then
    echo ""
    echo "🗄️  [3/3] Populating Local DB Stack (SQLite, PostgreSQL, MySQL)..."
    "$LOCAL_STACK_DIR/load_mlb_sabermetrics.sh" --all
else
    echo "ℹ️  Local DB Stack directory not found at $LOCAL_STACK_DIR, skipping docker populator."
fi

echo ""
echo "=============================================================================="
echo " ✅ Full Refresh, Simulation Run & Multi-DB Synchronization Complete!"
echo " • PocketHost Cloud: https://mlb-sabermetrics.pockethost.io"
echo " • Local SQLite DB:  $PROJECT_DIR/output_datasets/mlb_sabermetrics_local.sqlite"
echo " • Local PostgreSQL: localhost:15432 (local_database)"
echo " • Local MySQL:      localhost:13306 (local_database)"
echo " • Generated Charts: $PROJECT_DIR/docs/charts/"
echo "=============================================================================="
