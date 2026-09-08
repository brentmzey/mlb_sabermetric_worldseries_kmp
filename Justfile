# Justfile for MLB Sabermetric World Series Suite

default:
    @just --list

# Runs the complete data refresh, commits changes, aligns main/dev branches, and pushes
publish:
    @echo "⚾ Starting Full Refresh Pipeline..."
    ./scripts/full_refresh_and_sync.sh
    @echo "📊 Data refresh complete. Staging artifacts and charts..."
    git add .
    git commit -m "🤖 auto: Full data refresh, simulation re-run, and chart updates" || echo "No changes to commit"
    @echo "🔄 Aligning main and development branches..."
    # Ensure we are on main
    git checkout main
    # Push main to remote
    git push origin main
    # Checkout development (create if it doesn't exist)
    git checkout development 2>/dev/null || git checkout -b development
    # Merge main into development to ensure alignment
    git merge main -m "Merge main into development"
    # Push development to remote
    git push origin development
    # Return to main
    git checkout main
    @echo "✅ Pipeline Complete: Data synced, committed, branches aligned, and pushed to remote!"

# Runs the core data refresh script only (without git operations)
refresh:
    ./scripts/full_refresh_and_sync.sh

# Runs the Kotlin test suite and generates Jacoco coverage reports
test:
    ./gradlew test jacocoTestReport
