# Copyright © 2025 Aaro Koinsaari
# Licensed under the Apache License, Version 2.0
# http://www.apache.org/licenses/LICENSE-2.0

FROM python:3.13-slim
RUN apt-get update && \
    apt-get install -y osmium-tool postgresql-client && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY sql/ ./sql/
COPY src/ ./src/
RUN mkdir -p /app/data

ENV PYTHONUNBUFFERED=1
ENV DATA_DIR=/app/data
ENTRYPOINT ["python", "src/update_data.py"]