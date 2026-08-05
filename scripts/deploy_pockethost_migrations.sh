#!/usr/bin/env bash
# =================================================================================================
# 🚀 POCKETHOST MIGRATION & SCHEMA DEPLOYMENT SCRIPT
#    Uploads PocketBase JavaScript migrations & executes database schema setup.
# =================================================================================================
set -e

POCKETHOST_URL="https://mlb-sabermetric-worldseries.pockethost.io"
SFTP_USER="brentmzey4795@gmail.com@ftp.pockethost.io"
SFTP_PORT="2222"
SSH_KEY_PATH="$HOME/.ssh/pockethost_ed25519"

echo "================================================================================================="
echo " ⚾ POCKETHOST MIGRATION DEPLOYMENT"
echo "    Base URL: $POCKETHOST_URL"
echo "================================================================================================="

# Check health of PocketHost instance
echo "🔍 Checking PocketHost API health..."
HEALTH_STATUS=$(curl -s "$POCKETHOST_URL/api/health" | grep -o '"code":200' || true)
if [ -z "$HEALTH_STATUS" ]; then
    echo "❌ Error: Could not reach PocketHost API at $POCKETHOST_URL"
    exit 1
fi
echo "✅ PocketHost API is online and healthy."

# Check if SSH private key exists for SFTP upload
if [ -f "$SSH_KEY_PATH" ]; then
    echo "🚀 Uploading migration files via SFTP..."
    sftp -i "$SSH_KEY_PATH" -P "$SFTP_PORT" "$SFTP_USER" <<EOF
mkdir pb_migrations
put -r pb_migrations/* pb_migrations/
quit
EOF
    echo "✅ Migrations uploaded via SFTP."
else
    echo "⚠️ Note: SSH key '$SSH_KEY_PATH' not found."
    echo "   To deploy migrations via SFTP, save your private key to '$SSH_KEY_PATH' and re-run:"
    echo "   $ sftp -i $SSH_KEY_PATH -P $SFTP_PORT $SFTP_USER"
fi

echo "================================================================================================="
echo " ✅ PocketHost schema files prepared at docs/schema/ & pb_migrations/"
echo "================================================================================================="
