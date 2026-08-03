# ⚾ MLB Sabermetric World Series Predictions & Open Source Data Engine

[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-purple.svg)](https://kotlinlang.org/docs/multiplatform.html)
[![Targets](https://img.shields.io/badge/Targets-iOS_|_Android_|_Web_|_Desktop_|_Server-blue.svg)]()
[![JVM](https://img.shields.io/badge/JVM-17%2B-red.svg)](https://www.oracle.com/java/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> **Dedicated to Brian, Patrick, and Matthew — inspired by the iconic Yankees "Thumbs Down" Fan Rally! 👎**

Welcome to the **MLB Sabermetric World Series Prediction Suite**. This open-source repository combines advanced sabermetrics (wOBA, wRC+, FIP, BaseRuns, Pythagorean Win %) with **2SLS Instrumental Variable Causal Modeling** and a **10,000-iteration Monte Carlo postseason simulator** to predict the exact World Series win probabilities for all 30 MLB teams.

---

## 📊 Live MLB World Series Winning Probability Predictions

> **Updated Predictions (10,000-Iteration Postseason Monte Carlo Simulation)**

| Rank | Team Name | League & Division | Regular Season Record | Expected Season Wins | Playoff Prob % | Pennant Prob % | World Series Win Prob % |
| :---: | :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| 1 👎 | **New York Yankees** | AL East | 94 - 68 | 95.5 | **100.0%** | **99.1%** | **97.13%** |
| 2 | **Los Angeles Dodgers** | NL West | 98 - 64 | 96.0 | **100.0%** | **62.5%** | **2.30%** |
| 3 | **Philadelphia Phillies** | NL East | 95 - 67 | 92.5 | **95.1%** | **19.2%** | **0.26%** |
| 4 | **San Diego Padres** | NL West | 93 - 69 | 90.4 | **80.1%** | **6.8%** | **0.08%** |
| 5 | **Baltimore Orioles** | AL East | 91 - 71 | 90.1 | **90.6%** | **0.4%** | **0.06%** |
| 6 | **Atlanta Braves** | NL East | 89 - 73 | 88.6 | **59.6%** | **2.9%** | **0.04%** |
| 7 | **Milwaukee Brewers** | NL Central | 93 - 69 | 94.1 | **99.4%** | **6.0%** | **0.04%** |
| 8 | **Cleveland Guardians** | AL Central | 92 - 69 | 90.7 | **94.3%** | **0.2%** | **0.03%** |
| 9 | **New York Mets** | NL East | 89 - 73 | 88.2 | **53.8%** | **1.6%** | **0.03%** |
| 10 | **Houston Astros** | AL West | 88 - 73 | 90.1 | **92.1%** | **0.2%** | **0.02%** |
| 11 | **Detroit Tigers** | AL Central | 86 - 76 | 85.4 | **39.6%** | **0.0%** | **0.01%** |
| 12 | **Kansas City Royals** | AL Central | 86 - 76 | 91.0 | **95.6%** | **0.1%** | **0.00%** |
| 13 | **Seattle Mariners** | AL West | 85 - 77 | 87.0 | **60.0%** | **0.0%** | **0.00%** |
| 14 | **Arizona Diamondbacks** | NL West | 89 - 73 | 89.6 | **71.5%** | **0.9%** | **0.00%** |
| 15 | **Chicago Cubs** | NL Central | 83 - 79 | 87.1 | **40.4%** | **0.1%** | **0.00%** |
| 16 | **Minnesota Twins** | AL Central | 82 - 80 | 83.8 | **20.8%** | **0.0%** | **0.00%** |

*Download full 30-team clean dataset below.*

---

## 📂 Download Open-Source Cleaned Sabermetric Datasets

We provide free, ready-to-use CSV datasets for researchers, sports analysts, and fans:

* 📥 **[Download Clean MLB Sabermetric CSV Dataset](output_datasets/mlb_sabermetric_clean_dataset.csv)** (Includes wOBA, wRC+, FIP, xFIP, Pythagorean Win %, BaseRuns, Bullpen WPA, & Ace ERA).

---

## 🛠️ Engineers & Developers: How to Pull, Build, & Contribute

Whether you're building Kotlin Multiplatform applications or adding new sabermetric models, here is how to set up the project locally.

### **1. Install Prerequisites via Package Manager**

#### 🍏 macOS & 🐧 Linux (Homebrew)
```bash
brew update
brew install openjdk@17 gradle
```

#### 🪟 Windows (Chocolatey)
```cmd
:: Run Command Prompt or PowerShell as Administrator
choco install openjdk17 gradle
```

---

### **2. Clone Repository & Run Build**

```bash
# Clone repository
git clone https://github.com/brentmzey/mlb_sabermetric_worldseries_kmp.git
cd mlb_sabermetric_worldseries_kmp

# Run Unit Tests (100% Passing)
gradle test

# Build Runnable Fat JAR & KMP Targets (JVM, JS, iOS)
gradle build && gradle fatJar

# Run 10,000-Iteration Monte Carlo World Series Simulator locally
gradle run
```

---

## 🏗️ Codebase Architecture & Contribution Guide

The codebase is organized as a **Kotlin Multiplatform (KMP)** project:

```
mlb_sabermetric_worldseries_kmp/
├── build.gradle.kts                   (Multiplatform configuration for JVM, JS, iOS)
├── output_datasets/
│   └── mlb_sabermetric_clean_dataset.csv (Generated open-source dataset)
└── src/
    ├── commonMain/kotlin/com/sabermetrics/worldseries/
    │   ├── model/SabermetricModels.kt  (MlbTeam, TeamProbability, Simulation Result models)
    │   ├── data/SabermetricDataService.kt (Data ingestion & clean CSV exporter)
    │   └── engine/WorldSeriesSimulator.kt (2SLS IV Causal Engine & 10k Monte Carlo Simulator)
    ├── commonTest/kotlin/com/sabermetrics/worldseries/
    │   └── SabermetricTest.kt          (Unit tests for probability constraints & formulas)
    └── jvmMain/kotlin/com/sabermetrics/worldseries/
        └── Main.kt                    (Desktop CLI runner & table formatter)
```

### 🤝 How to Contribute:
1. **Fork & Branch**: Create a feature branch (e.g., `git checkout -b feature/add-statcast-exit-velocity`).
2. **Add Estimators or Data**: Update `SabermetricDataService.kt` or `WorldSeriesSimulator.kt`.
3. **Verify Tests**: Ensure `gradle test` passes cleanly.
4. **Submit PR**: Open a Pull Request detailing your sabermetric additions!

---

## 🔗 Companion Repositories

* 📱 **Archetype KMP Engine**: [`econometric_archetype_kmp`](file:///Users/brentzey/personal/econometric_archetype_kmp)
* 🌐 **Full-Stack KMP Engine**: [`econometric_fullstack_kmp`](file:///Users/brentzey/personal/econometric_fullstack_kmp)
