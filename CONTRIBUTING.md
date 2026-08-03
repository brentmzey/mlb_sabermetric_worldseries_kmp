# 🛠️ Contributing to `mlb_sabermetric_worldseries_kmp`

Thank you for your interest in contributing to the **MLB Sabermetric World Series Prediction Suite**! We welcome contributions from sports analytics researchers, Kotlin engineers, data scientists, and MLB fans.

---

## 🚀 Quickstart for Engineers

### 1. Prerequisites
- **Java JDK 17+**
- **Gradle 8.0+**
- **Git**

### 2. Setup Project
```bash
git clone https://github.com/brentmzey/mlb_sabermetric_worldseries_kmp.git
cd mlb_sabermetric_worldseries_kmp

# Run full unit test suite
gradle test

# Run the 10,000-iteration Monte Carlo simulation locally
gradle run
```

---

## 💡 Areas for Contribution

1. **New Sabermetric Metrics**: Integrating Statcast metrics (Hard-Hit %, Barrel %, Whiff %, Stuff+, Pitching Location Quality).
2. **Causal Econometric Models**: Adding Difference-in-Differences (DiD) trade deadline impact estimations or Synthetic Control Methods for team injuries.
3. **Multiplatform UI Wrappers**: Adding Compose Multiplatform (Desktop/Android) or SwiftUI (iOS) dashboard views.

---

## 🧪 Code Style & Testing Requirements

- All math and estimation logic must reside in `src/commonMain/kotlin` to ensure cross-platform compatibility across JVM, JS, and iOS.
- New estimators must include unit tests in `src/commonTest/kotlin` verifying mathematical constraints ($\sum P = 1.0$, non-negative variance).
- Ensure `gradle test` passes before opening a Pull Request.

---

## 📬 Submitting Pull Requests

1. Fork the repo and create your feature branch: `git checkout -b feature/my-sabermetric-feature`
2. Commit your changes: `git commit -m "Add Statcast Barrel% to latent quality model"`
3. Push to your branch: `git push origin feature/my-sabermetric-feature`
4. Create a new Pull Request on GitHub.

Thank you for helping keep this MLB sabermetric prediction engine open-source and clean for everyone!
