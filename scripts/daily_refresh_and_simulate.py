#!/usr/bin/env python3
"""
Automated Daily Pipeline: Full Sabermetric Data Fetch, 10k Monte Carlo Simulation, 
Outcome Propensity Sampling, Chart Visualization Rendering, and PocketHost Cloud Sync.
"""
import os
import sys
import time
import subprocess
import datetime

def get_utc_epoch_ms():
    return int(time.time() * 1000)

def get_utc_iso():
    return datetime.datetime.now(datetime.timezone.utc).isoformat()

def run_step(step_name, command, cwd=None):
    print(f"\n================================================================================")
    print(f" ▶ STEP: {step_name}")
    print(f"   Command: {command}")
    print(f"   Timestamp UTC: {get_utc_iso()} (Epoch MS: {get_utc_epoch_ms()})")
    print(f"================================================================================")
    start_t = time.time()
    res = subprocess.run(command, shell=True, cwd=cwd, text=True)
    elapsed = time.time() - start_t
    if res.returncode != 0:
        print(f"❌ ERROR: Step '{step_name}' failed with exit code {res.returncode} in {elapsed:.2f}s")
        return False
    print(f"✅ COMPLETED: Step '{step_name}' succeeded in {elapsed:.2f}s")
    return True

def main():
    proj_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
    print("================================================================================")
    print(" ⚾ MLB 2026 DAILY FULL DATA REFRESH, STOCHASTIC SIMULATION & SYNC PIPELINE")
    print(f"    Target Directory: {proj_dir}")
    print(f"    Start Timestamp UTC: {get_utc_iso()} ({get_utc_epoch_ms()} ms)")
    print("================================================================================")

    # 1. Clean and Run Kotlin Multiplatform Simulation & Chart Generation
    success = run_step(
        "10,000-Iteration Postseason Monte Carlo Simulation & High-Res Chart Generation",
        "./gradlew run",
        cwd=proj_dir
    )
    if not success:
        sys.exit(1)

    # 2. Run Unit Tests to Validate Probability Constraints & Calibration
    success = run_step(
        "KMP Unit Test Suite Verification (100% Probability Conservation)",
        "./gradlew test",
        cwd=proj_dir
    )
    if not success:
        sys.exit(1)

    # 3. Synchronize to PocketHost Cloud Hungarian Relational Schema
    sync_script = os.path.join(proj_dir, "scripts", "migrate_and_sync_hungarian.py")
    if os.path.exists(sync_script):
        success = run_step(
            "PocketHost Cloud Relational Sync (i_, m_, s_, o_, f_ Collections with UTC Epoch MS)",
            f"python3 -u {sync_script}",
            cwd=proj_dir
        )
        if not success:
            print("⚠️ Warning: PocketHost sync encountered a warning but continuing pipeline.")

    print("\n================================================================================")
    print(" 🏁 DAILY FULL REFRESH & SIMULATION PIPELINE COMPLETE")
    print(f"    Completion Timestamp UTC: {get_utc_iso()} ({get_utc_epoch_ms()} ms)")
    print("    Artifacts Updated:")
    print("      • 📁 Output Dataset: output_datasets/mlb_sabermetric_clean_dataset.csv")
    print("      • 🖼️  Win Probabilities: docs/charts/world_series_win_probabilities.png")
    print("      • 📈 Trend Checkpoints: docs/charts/team_probability_trends_over_time.png")
    print("      • 📊 Residual Luck: docs/charts/residual_luck_bias_decomposition.png")
    print("      • 🖼️  Roster Anchors: docs/charts/roster_anchors_leaderboard.png")
    print("      • ⚔️  Cross-League Matchup: docs/charts/cross_league_matchup_matrix.png")
    print("      • 🎲 Outcome Propensities: docs/charts/monte_carlo_outcome_propensities.png")
    print("      • ☁️  Cloud Sync Architecture: docs/charts/pockethost_cloud_sync_architecture.png")
    print("================================================================================\n")

if __name__ == "__main__":
    main()
