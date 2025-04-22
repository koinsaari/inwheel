#!/bin/bash
# Copyright © 2025 Aaro Koinsaari
# Licensed under the Apache License, Version 2.0
# http://www.apache.org/licenses/LICENSE-2.0

set -e
mkdir -p ./backend/data

TIMESTAMP=$(date +"%Y-%m-%d_%H-%M-%S")
LOG_FILE="./backend/data/update_${TIMESTAMP}.log"

echo "===== Starting Map Data Update: $(date) =====" > "$LOG_FILE"
echo "Running update with:" >> "$LOG_FILE"

REGIONS=${REGIONS:-""}  # which regions to update
OVERWRITE=${OVERWRITE:-"false"}  # overwrite user-modified entries
TEST_MODE=${TEST_MODE:-"false"}  # test mode, only insert 100 places

echo "- Selected regions: ${REGIONS:-all regions}" >> "$LOG_FILE"
echo "- Overwrite user-modified: ${OVERWRITE}" >> "$LOG_FILE"
echo "- Test mode: ${TEST_MODE}" >> "$LOG_FILE"

# Build docker arguments explicitly based on flags
docker_args=""
[ "$OVERWRITE" == "true" ] && docker_args="$docker_args --overwrite"
[ "$TEST_MODE" == "true" ] && docker_args="$docker_args --test"

if [ -z "$REGIONS" ]; then
  echo "No specific regions selected, processing all regions" | tee -a "$LOG_FILE"
  
  echo "Starting Docker container for all regions..." >> "$LOG_FILE"
  if ! docker run --rm \
    -v $(pwd)/backend/data:/app/data \
    -e DATABASE_URL \
    map-data-updater $docker_args 2>&1 | tee -a "$LOG_FILE"; then
    echo "::error::Data update failed! Check logs for details."
    echo "===== Update FAILED: $(date) =====" >> "$LOG_FILE"
    exit 1
  fi
else
  echo "Processing selected regions: $REGIONS" | tee -a "$LOG_FILE"
  IFS=',' read -ra REGION_ARRAY <<< "$REGIONS"
  
  for region in "${REGION_ARRAY[@]}"; do
    if [ -n "$region" ]; then
      echo "Processing region: $region" | tee -a "$LOG_FILE"
      
      # Start with the region-specific arg, then add common args
      region_args="--region $region $docker_args"
      
      echo "Starting Docker container for region $region..." >> "$LOG_FILE"
      if ! docker run --rm \
        -v $(pwd)/backend/data:/app/data \
        -e DATABASE_URL \
        map-data-updater $region_args 2>&1 | tee -a "$LOG_FILE"; then
        echo "::error::Data update for region $region failed! Check logs for details."
        echo "===== Update for $region FAILED: $(date) =====" >> "$LOG_FILE"
        exit 1
      fi
      
      echo "===== Region $region update COMPLETED: $(date) =====" >> "$LOG_FILE"
    fi
  done
fi

echo "===== All Updates SUCCEEDED: $(date) =====" >> "$LOG_FILE"