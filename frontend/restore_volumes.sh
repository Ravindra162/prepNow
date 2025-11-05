#!/bin/bash
set -e

# -----------------------------
# Restore all Docker volumes from .tar.gz files
# -----------------------------

echo "===================================="
echo "📦 Restoring Docker volumes from tar.gz files..."
echo "===================================="

for backup in *.tar.gz; do
  [ -e "$backup" ] || { echo "⚠️  No .tar.gz files found."; exit 1; }

  # Remove extension to get volume name
  volume_name=$(basename "$backup" .tar.gz)

  echo "🧩 Creating volume: $volume_name"
  docker volume create "$volume_name" >/dev/null

  echo "📂 Restoring data into: $volume_name"
  docker run --rm -v "$volume_name":/volume -v "$(pwd)":/backup alpine \
    tar xzf "/backup/$backup" -C /volume

  echo "✅ Successfully restored: $volume_name"
  echo
done

echo "===================================="
echo "🎉 All volumes restored successfully!"
echo "===================================="

# Show final list of volumes
echo
echo "📋 Available Docker volumes now:"
docker volume ls
