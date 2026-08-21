#!/usr/bin/env python3
"""
Automated Daily Pipeline: Full Sabermetric Data Fetch, 10k Monte Carlo Simulation, 
Outcome Propensity Sampling, Chart Visualization Rendering, and PocketHost Cloud Sync.
Strongly typed using Python 3.10+ dataclasses, type annotations, and structured execution results.
"""
from __future__ import annotations

import os
import sys
import time
import subprocess
import datetime
from dataclasses import dataclass, field
from typing import Final, List, Optional, Sequence


DEFAULT_TIMEOUT_SECONDS: Final[int] = 300


@dataclass(frozen=True)
class PipelineStep:
    """Represents an atomic step in the daily automated sabermetric pipeline."""
    name: str
    command: str
    cwd: Optional[str] = None
    is_critical: bool = True
    timeout_seconds: int = DEFAULT_TIMEOUT_SECONDS


@dataclass
class StepExecutionResult:
    """Structured result of executing a pipeline step."""
    step: PipelineStep
    return_code: int
    elapsed_seconds: float
    is_success: bool
    stdout: Optional[str] = None
    stderr: Optional[str] = None


@dataclass
class PipelineRunSummary:
    """Comprehensive summary of the complete daily pipeline run."""
    start_utc: str
    start_epoch_ms: int
    completion_utc: Optional[str] = None
    completion_epoch_ms: Optional[int] = None
    step_results: List[StepExecutionResult] = field(default_factory=list)
    artifacts: List[str] = field(default_factory=list)

    @property
    def total_elapsed_seconds(self) -> float:
        return sum(r.elapsed_seconds for r in self.step_results)

    @property
    def is_all_successful(self) -> bool:
        return all(r.is_success for r in self.step_results if r.step.is_critical)


def get_utc_epoch_ms() -> int:
    """Returns the current timestamp in UTC Epoch Milliseconds as an integer."""
    return int(time.time() * 1000)


def get_utc_iso() -> str:
    """Returns the current UTC ISO 8601 formatted timestamp string."""
    return datetime.datetime.now(datetime.timezone.utc).isoformat()


def run_pipeline_step(step: PipelineStep) -> StepExecutionResult:
    """
    Executes a typed pipeline step as a subprocess, measures wall-clock execution time,
    and returns a strongly typed StepExecutionResult.
    """
    print("\n================================================================================")
    print(f" ▶ STEP: {step.name}")
    print(f"   Command: {step.command}")
    print(f"   Timestamp UTC: {get_utc_iso()} (Epoch MS: {get_utc_epoch_ms()})")
    print("================================================================================")

    start_time: float = time.time()
    try:
        process: subprocess.CompletedProcess[str] = subprocess.run(
            step.command,
            shell=True,
            cwd=step.cwd,
            text=True,
            timeout=step.timeout_seconds
        )
        elapsed: float = time.time() - start_time
        is_success: bool = (process.returncode == 0)

        if not is_success:
            print(f"❌ ERROR: Step '{step.name}' failed with exit code {process.returncode} in {elapsed:.2f}s")
        else:
            print(f"✅ COMPLETED: Step '{step.name}' succeeded in {elapsed:.2f}s")

        return StepExecutionResult(
            step=step,
            return_code=process.returncode,
            elapsed_seconds=elapsed,
            is_success=is_success
        )
    except subprocess.TimeoutExpired as err:
        elapsed = time.time() - start_time
        print(f"❌ TIMEOUT: Step '{step.name}' timed out after {step.timeout_seconds}s")
        return StepExecutionResult(
            step=step,
            return_code=-1,
            elapsed_seconds=elapsed,
            is_success=False,
            stderr=str(err)
        )
    except Exception as err:
        elapsed = time.time() - start_time
        print(f"❌ UNEXPECTED ERROR in step '{step.name}': {err}")
        return StepExecutionResult(
            step=step,
            return_code=-2,
            elapsed_seconds=elapsed,
            is_success=False,
            stderr=str(err)
        )


def build_pipeline_steps(proj_dir: str) -> Sequence[PipelineStep]:
    """Constructs the sequence of pipeline steps for the daily refresh process."""
    sync_script_path: str = os.path.join(proj_dir, "scripts", "migrate_and_sync_hungarian.py")
    
    steps: List[PipelineStep] = [
        PipelineStep(
            name="10,000-Iteration Postseason Monte Carlo Simulation & High-Res Chart Generation",
            command="./gradlew run",
            cwd=proj_dir,
            is_critical=True,
            timeout_seconds=180
        ),
        PipelineStep(
            name="KMP Unit Test Suite Verification (100% Probability Conservation)",
            command="./gradlew test",
            cwd=proj_dir,
            is_critical=True,
            timeout_seconds=120
        ),
    ]

    if os.path.exists(sync_script_path):
        steps.append(
            PipelineStep(
                name="PocketHost Cloud Relational Sync (i_, m_, s_, o_, f_ Collections with UTC Epoch MS)",
                command=f"python3 -u {sync_script_path}",
                cwd=proj_dir,
                is_critical=False,
                timeout_seconds=240
            )
        )

    return steps


def main() -> None:
    """Main entrypoint for the daily automated sabermetric pipeline."""
    proj_dir: str = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
    start_iso: str = get_utc_iso()
    start_epoch: int = get_utc_epoch_ms()

    summary: PipelineRunSummary = PipelineRunSummary(
        start_utc=start_iso,
        start_epoch_ms=start_epoch,
        artifacts=[
            "output_datasets/mlb_sabermetric_clean_dataset.csv",
            "docs/charts/world_series_win_probabilities.png",
            "docs/charts/team_probability_trends_over_time.png",
            "docs/charts/residual_luck_bias_decomposition.png",
            "docs/charts/roster_anchors_leaderboard.png",
            "docs/charts/cross_league_matchup_matrix.png",
            "docs/charts/monte_carlo_outcome_propensities.png",
            "docs/charts/pockethost_cloud_sync_architecture.png"
        ]
    )

    print("================================================================================")
    print(" ⚾ MLB 2026 DAILY FULL DATA REFRESH, STOCHASTIC SIMULATION & SYNC PIPELINE")
    print(f"    Target Directory: {proj_dir}")
    print(f"    Start Timestamp UTC: {summary.start_utc} ({summary.start_epoch_ms} ms)")
    print("================================================================================")

    steps: Sequence[PipelineStep] = build_pipeline_steps(proj_dir)

    for step in steps:
        result: StepExecutionResult = run_pipeline_step(step)
        summary.step_results.append(result)
        if not result.is_success and step.is_critical:
            print(f"\n🛑 Pipeline aborted due to failure in critical step: '{step.name}'")
            sys.exit(result.return_code)

    summary.completion_utc = get_utc_iso()
    summary.completion_epoch_ms = get_utc_epoch_ms()

    print("\n================================================================================")
    print(" 🏁 DAILY FULL REFRESH & SIMULATION PIPELINE COMPLETE")
    print(f"    Completion Timestamp UTC: {summary.completion_utc} ({summary.completion_epoch_ms} ms)")
    print(f"    Total Pipeline Execution Time: {summary.total_elapsed_seconds:.2f}s")
    print("    Artifacts Updated:")
    for artifact in summary.artifacts:
        print(f"      • 📁 {artifact}")
    print("================================================================================\n")


if __name__ == "__main__":
    main()
