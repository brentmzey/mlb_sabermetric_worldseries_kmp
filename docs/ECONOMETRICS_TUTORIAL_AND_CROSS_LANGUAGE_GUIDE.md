# ⚾ Approachable Econometrics: 2SLS, Causal Inference & Cross-Language Examples

Welcome to the **Interactive Econometrics & Cross-Language Guide**! This document breaks down the deep mathematics, statistics, and causal modeling used in our MLB Sabermetric Simulator into accessible, approachable concepts.

We will cover *why* we use these models, provide interactive Python code you can run on free datasets, and compare how the core logic translates from Kotlin into Python, TypeScript, Rust, and Java.

---

## 🎲 1. The Core Problem: The "Luck Factor" (Endogeneity)

In baseball, a team's win-loss record is heavily influenced by **luck** (sequencing of hits, 1-run game variance, extra-inning randomness). In econometrics, this is called **Endogeneity**. 

If we run a naive regression (OLS) to predict a team's true quality based purely on their actual wins, our model will be biased because the "Wins" variable is contaminated by the unobserved "Luck" variable.

**The Math (Simplified):**
$$ Wins_i = TrueQuality_i + Luck_i $$

If $Luck_i$ is positive (a team wins many 1-run games despite poor run differential), their $TrueQuality_i$ is mathematically overestimated.

---

## 🛠️ 2. The Solution: Two-Stage Least Squares (2SLS) / Instrumental Variables

To find a team's true, underlying structural skill (Causal Quality), we need a proxy for Wins that is **correlated with skill** but **completely independent of luck**. This is called an **Instrumental Variable (IV)**.

We use **Pythagorean Expectation** (Run Differential) and **BaseRuns** as our instruments.
1. **First Stage:** We regress actual Wins against our instruments to create "Fitted Wins" (Wins stripped of luck).
2. **Second Stage:** We regress our final Quality metrics against these "Fitted Wins" to find the true, causal championship probability.

---

## 🐍 3. Interactive Tutorial: Run 2SLS in Python (Free Data)

You can run this exact econometric model interactively using Python, `pandas`, and `statsmodels` with free data from FanGraphs or Baseball-Reference.

### Prerequisites (Python)
```bash
pip install pandas statsmodels linearmodels
```

### 💻 Python Interactive Snippet
```python
import pandas as pd
import numpy as np
from linearmodels.iv import IV2SLS

# 1. Create a dummy dataset mimicking free FanGraphs/Baseball-Reference Data
data = {
    'Team': ['Dodgers', 'Braves', 'Yankees', 'Marlins', 'Pirates'],
    'Actual_Wins': [95, 93, 88, 84, 65],  # Contaminated by luck
    'Runs_Scored': [800, 780, 750, 600, 550],
    'Runs_Allowed': [600, 620, 650, 700, 750],
    'Team_WAR': [45, 42, 40, 25, 15] # The ultimate outcome we are measuring against
}
df = pd.DataFrame(data)

# 2. Calculate the Instrument (Pythagorean Expectation)
# Pyth% = R^1.83 / (R^1.83 + RA^1.83)
df['Pyth_Win_Pct'] = (df['Runs_Scored']**1.83) / (df['Runs_Scored']**1.83 + df['Runs_Allowed']**1.83)
df['Expected_Wins'] = df['Pyth_Win_Pct'] * 162

# 3. Add a constant for the intercept
df['Intercept'] = 1

# 4. Run the 2SLS Causal Regression
# Dependent: Team_WAR
# Exogenous: Intercept
# Endogenous (Contaminated): Actual_Wins
# Instrument (The Purifier): Expected_Wins
iv_model = IV2SLS(
    dependent=df['Team_WAR'],
    exog=df['Intercept'],
    endog=df['Actual_Wins'],
    instruments=df['Expected_Wins']
).fit()

print(iv_model.summary)
```
**What happens here?** The `linearmodels` library automatically strips out the luck variance from `Actual_Wins` using `Expected_Wins` before calculating the true coefficient for Team WAR.

---

## 🌍 4. Cross-Language Implementations & Analogies

Our main project uses **Kotlin Multiplatform (KMP)** because it allows us to share the exact same causal mathematics across JVM Servers, iOS Apps, and Web Browsers. 

Here is how the core mathematical function—calculating the Log5 Pythagenpat matchup probability—looks across different languages.

### The Math (Bill James Log5)
$$ P(A \text{ beats } B) = \frac{Q_A^{1.20}}{Q_A^{1.20} + Q_B^{1.20}} $$

### 🅺 Kotlin (Our Codebase)
**Analogy:** The "Swiss Army Knife". Concise, safe, and multi-functional.
```kotlin
fun calculateLog5Probability(qualityA: Double, qualityB: Double, gamma: Double = 1.20): Double {
    val qA = qualityA.pow(gamma)
    val qB = qualityB.pow(gamma)
    return qA / (qA + qB)
}
```
*Why we use it:* `Double.pow()` is built into the Kotlin Standard Library, allowing it to compile cleanly to Java Bytecode, JavaScript, and native C binaries.

### 🐍 Python
**Analogy:** The "Scientist's Notebook". Highly readable, perfect for rapid econometric prototyping.
```python
def calculate_log5_probability(quality_a: float, quality_b: float, gamma: float = 1.20) -> float:
    q_a = quality_a ** gamma
    q_b = quality_b ** gamma
    return q_a / (q_a + q_b)
```

### 🟦 TypeScript
**Analogy:** The "Web Developer's Armor". Type-safe JavaScript for interactive charts.
```typescript
function calculateLog5Probability(qualityA: number, qualityB: number, gamma: number = 1.20): number {
    const qA = Math.pow(qualityA, gamma);
    const qB = Math.pow(qualityB, gamma);
    return qA / (qA + qB);
}
```

### 🦀 Rust
**Analogy:** The "F1 Racecar". Unparalleled speed and memory safety, used if we needed 10-billion iterations instead of 10-thousand.
```rust
fn calculate_log5_probability(quality_a: f64, quality_b: f64, gamma: f64) -> f64 {
    let q_a = quality_a.powf(gamma);
    let q_b = quality_b.powf(gamma);
    q_a / (q_a + q_b)
}
// Note: Rust enforces strict f64 (64-bit float) typing and uses .powf() for floats.
```

### ☕ Java
**Analogy:** The "Corporate Workhorse". Verbose, rock-solid, and historically standardized.
```java
public class Sabermetrics {
    public static double calculateLog5Probability(double qualityA, double qualityB) {
        double gamma = 1.20;
        double qA = Math.pow(qualityA, gamma);
        double qB = Math.pow(qualityB, gamma);
        return qA / (qA + qB);
    }
}
```

---

## 📈 5. Why the "Gamma" ($\gamma = 1.20$) Matters in Postseason Baseball

You'll notice the exponent `1.20` in the code above. In regular season baseball, a dominant team might win 65% of their games against a terrible team. 

However, in the **Postseason**, teams shorten their starting rotations (using only their Top 3 Aces) and rely heavily on their elite bullpen arms. This creates a **compression effect**. The parity in October baseball is significantly higher than in August.

By applying an empirical parity scaling factor of $\gamma = 1.20$, we compress the probability bounds to mirror historical MLB October outcomes, ensuring that a powerhouse like the Dodgers is favored to win a 7-game series, but still vulnerable to a Wild Card upset!
