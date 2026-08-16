# 🎲 2026 Daily Monte Carlo Outcome Propensities & Stochastic Sampling Pipeline

## 🎯 Executive Overview
This document describes the **Daily Stochastic Sampling, Outcome Propensity Distribution, and Automated Model Re-run Pipeline** for the 2026 MLB Sabermetric World Series Prediction Suite.

Whenever new daily games, bullpen usage, or betting market odds are recorded, the pipeline executes a full **10,000-iteration Monte Carlo simulation** using **Bill James' Pythagenpat Log5 Matchup Theorem** and **Brian Kenny's October Ace & Bullpen Leverage Compression**.

```
                               🎲 DAILY STOCHASTIC SIMULATION & OUTCOME PIPELINE
 ┌────────────────────┐     ┌──────────────────────┐     ┌───────────────────────┐     ┌────────────────────────┐
 │ 1. Ingest Daily    │ ──> │ 2. 2SLS IV Causal    │ ──> │ 3. 10,000 Monte Carlo │ ──> │ 4. PocketHost Sync &   │
 │    Clean Inputs    │     │    Quality Model (q) │     │    Log5 Resampling    │     │    High-Res Chart Gen  │
 └────────────────────┘     └──────────────────────┘     └───────────────────────┘     └────────────────────────┘
```

---

## 📊 1. Outcome Propensities & 95% Bootstrap Confidence Intervals

Across 10,000 simulated postseason brackets, the Central Limit Theorem guarantees that empirical simulation win probabilities converge asymptotically to the true posterior distribution with a standard error of:

$$\text{SE}(\hat{p}_i) = \sqrt{\frac{\hat{p}_i (1 - \hat{p}_i)}{N}} \le \sqrt{\frac{0.25}{10,000}} = \pm 0.50\% \quad (\text{For Dodgers at } 20.95\%: \text{SE} = \pm 0.407\%)$$

$$\text{95\% Bootstrap Confidence Interval}: \quad \hat{p}_i \pm 1.96 \cdot \text{SE}(\hat{p}_i)$$

| Contender Rank | Team Name | Mean WS Win % ($\hat{p}_i$) | Standard Error (SE) | 95% Bootstrap Confidence Interval | Outcome Propensity Density Spread | Primary Postseason Drivers |
| :---: | :--- | :---: | :---: | :---: | :---: | :--- |
| 🥇 1 | **Los Angeles Dodgers** | **21.05%** | $\pm 0.408\%$ | **[20.25% – 21.85%]** | `████████████████████` | 120 wRC+, 2.70 Ace ERA, 36% Polymarket, First-Round Bye |
| 🥈 2 | **Atlanta Braves** | **17.80%** | $\pm 0.383\%$ | **[17.05% – 18.55%]** | `█████████████████` | 3.52 FIP, 3.35 Ace ERA, First-Round Bye |
| 🥉 3 | **New York Yankees** | **15.20%** | $\pm 0.359\%$ | **[14.50% – 15.90%]** | `███████████████` | 117 wRC+, 3.15 Ace ERA, AL Bye path |
| 4 | **Milwaukee Brewers** | **11.20%** | $\pm 0.315\%$ | **[10.58% – 11.82%]** | `███████████` | #1 NL Defense (1.10), +3.5 WPA Bullpen |
| 5 | **Chicago Cubs** | **10.12%** | $\pm 0.302\%$ | **[9.53% – 10.71%]** | `██████████` | +111 Run Diff, 8–2 Hot Streak (+3.7%), 1.10 Defense |
| 6 | **Tampa Bay Rays** | **8.49%** | $\pm 0.279\%$ | **[7.94% – 9.04%]** | `████████` | 74–46 Win Leader, +62 Run Diff |
| 7 | **Houston Astros** | **7.02%** | $\pm 0.256\%$ | **[6.52% – 7.52%]** | `███████` | October Pedigree, +2.0 Bullpen WPA, 3.30 Ace ERA |
| 8 | **San Diego Padres** | **4.44%** | $\pm 0.206\%$ | **[4.04% – 4.84%]** | `████` | Tatis/Machado/Cease/King Wild Card Contender |
| 9 | **Detroit Tigers** | **3.48%** | $\pm 0.183\%$ | **[3.12% – 3.84%]** | `███` | Tarik Skubal Cy Young Ace Compression |

---

## 🖼️ 2. Visual Outcome Propensity Chart

![Monte Carlo Outcome Propensity and Sampling Distribution](docs/charts/monte_carlo_outcome_propensities.png)

---

## ⚡ 3. How to Run the Automated Daily Refresh Pipeline

To perform a daily data fetch, re-run all models, re-render all 8 visual chart PNGs, and sync the results to PocketHost:

```bash
# Execute the unified daily pipeline
python3 scripts/daily_refresh_and_simulate.py
```

### Pipeline Execution Lifecycle:
1. **Model Execution**: `./gradlew run` executes 10,000 Monte Carlo iterations in pure Kotlin Multiplatform.
2. **Dataset Export**: Exports clean 30-team dataset with predictive rank deltas to `output_datasets/mlb_sabermetric_clean_dataset.csv`.
3. **High-Resolution Graphics**: Generates 8 presentation-ready chart PNGs in `docs/charts/`.
4. **Cloud Synchronization**: Posts latest active records to PocketHost (`i_`, `m_`, `s_`, `o_`, `f_` collections) indexed by 64-bit UTC Epoch Milliseconds.

---

## 🔗 Related Documentation & Visual Artifacts
- ⚔️ **[docs/MLB_CROSS_LEAGUE_HEAD_TO_HEAD_AND_SEED_ANALYSIS.md](file:///Users/brentzey/personal/mlb_sabermetric_worldseries_kmp/docs/MLB_CROSS_LEAGUE_HEAD_TO_HEAD_AND_SEED_ANALYSIS.md)**: Cross-League Head-to-Head Analysis
- 📖 **[docs/BREWERS_CHAMPIONSHIP_ODDS_AND_NL_CENTRAL_ANALYSIS.md](file:///Users/brentzey/personal/mlb_sabermetric_worldseries_kmp/docs/BREWERS_CHAMPIONSHIP_ODDS_AND_NL_CENTRAL_ANALYSIS.md)**: Milwaukee Brewers Analysis
- 📖 **[docs/CUBS_CHAMPIONSHIP_ODDS_AND_NL_CENTRAL_ANALYSIS.md](file:///Users/brentzey/personal/mlb_sabermetric_worldseries_kmp/docs/CUBS_CHAMPIONSHIP_ODDS_AND_NL_CENTRAL_ANALYSIS.md)**: Chicago Cubs Analysis
- 📖 **[docs/MODEL_BIAS_AND_ECONOMETRIC_RESIDUALS.md](file:///Users/brentzey/personal/mlb_sabermetric_worldseries_kmp/docs/MODEL_BIAS_AND_ECONOMETRIC_RESIDUALS.md)**: 30-Team Econometric Residual & Bias Mitigation Matrix
