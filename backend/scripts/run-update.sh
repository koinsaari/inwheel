#!/bin/bash
# Copyright (c) 2025 Aaro Koinsaari
# Licensed under the Apache License, Version 2.0

set -e
mkdir -p ./backend/data

echo "===== Starting Map Data Update: $(date) =====" > ./backend/data/update.log
echo "Running update with:" >> ./backend/data/update.log
echo "- Region: ${REGION:-all regions}" >> ./backend/data/update.log
echo "- Update user-modified: ${UPDATE_ALL:-false}" >> ./backend/data/update.log
echo "- Test mode: ${TEST_MODE:-false}" >> ./backend/data/update.log

ARGS=""
if [ "$REGION" != "" ]; then
  ARGS="--region $REGION"
fi

if [ "$UPDATE_ALL" == "true" ]; then
  ARGS="$ARGS --update-all"
fi

if [ "$TEST_MODE" == "true" ]; then
  ARGS="$ARGS --test"
fi

echo "Starting Docker container..." >> ./backend/data/update.log
if ! docker run --rm \
  -v $(pwd)/backend/data:/app/data \
  -e DATABASE_URL \
  map-data-updater $ARGS 2>&1 | tee -a ./backend/data/update.log; then
  echo "::error::Data update failed! Check logs for details."
  echo "===== Update FAILED: $(date) =====" >> ./backend/data/update.log
  exit 1
else
  echo "===== Update SUCCEEDED: $(date) =====" >> ./backend/data/update.log
fi