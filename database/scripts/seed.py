# Copyright (c) 2025 Aaro Koinsaari
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import os
import requests
import subprocess
import psycopg2
from typing import List
from psycopg2.extras import Json
from dotenv import load_dotenv
from place import PlaceHandler, Place, TAGS

load_dotenv(override=True)

DATABASE_URL = os.getenv("DATABASE_URL")
PBF_FILENAME = "switzerland-latest.osm.pbf"
FILTERED_FILENAME = "switzerland-filtered.osm.pbf"
SWITZERLAND_PBF_URL="https://download.geofabrik.de/europe/switzerland-latest.osm.pbf"


def download_pbf():
    if os.path.exists(PBF_FILENAME):
        print(f"{PBF_FILENAME} already exists, skipping download.")
        return

    print("Downloading PBF file...")
    r = requests.get(SWITZERLAND_PBF_URL, stream=True)
    with open(PBF_FILENAME, 'wb') as f:
        for chunk in r.iter_content(chunk_size=8192):
            f.write(chunk)
    print("Download complete.")


def filter_pbf():
    if (os.path.exists(FILTERED_FILENAME)):
        print(f"{FILTERED_FILENAME} already exists, skipping filtering.")
        return
    
    tags_filter = []
    for tag_type, vals in TAGS.items():
        for val in vals:
            tags_filter.append(f"n/{tag_type}={val}")

    filter_cmd = ["osmium", "tags-filter", PBF_FILENAME] + tags_filter + ["-o", FILTERED_FILENAME]
    print("Running:", " ".join(filter_cmd))
    subprocess.run(filter_cmd, check=True)
    print("Filtering complete.")


def parse_filtered_pbf() -> List[Place]:
    handler = PlaceHandler()
    handler.apply_file(FILTERED_FILENAME)
    return handler.places


def seed_places(places: List[Place], limit: int = None):
    conn = psycopg2.connect(DATABASE_URL)
    cur = conn.cursor()

    places_to_seed = places[:limit] if limit else places

    for p in places_to_seed:
        accessibility_json = Json(p.accessibility_osm)
        contact_json = Json(p.contact)
        data_tuple = (
            p.osm_id,
            p.name,
            p.category,
            p.lat,
            p.lon,
            p.lon,  # longitude for MakePoint
            p.lat,  # latitude for MakePoint
            contact_json,
            accessibility_json
        )
        try:
            cur.execute("""
                INSERT INTO places (osm_id, name, category, lat, lon, geom, contact, accessibility_osm, last_osm_update, created_at)
                VALUES (%s, %s, %s, %s, %s, ST_SetSRID(ST_MakePoint(%s, %s), 4326)::GEOGRAPHY, %s, %s, NOW(), NOW())
                ON CONFLICT (osm_id) DO UPDATE
                SET name = EXCLUDED.name,
                    category = EXCLUDED.category,
                    lat = EXCLUDED.lat,
                    lon = EXCLUDED.lon,
                    geom = EXCLUDED.geom,
                    contact = EXCLUDED.contact,
                    accessibility_osm = EXCLUDED.accessibility_osm,
                    last_osm_update = NOW()
            """, data_tuple)

            print(f"Inserting place osm_id={p.osm_id}, name={p.name}, category={p.category}")
        except Exception as e:
            print(f"Error inserting osm_id={p.osm_id}: {e}")
    conn.commit()
    cur.close()
    conn.close()
    print(f"Seeding done. {len(places_to_seed)} places seeded.")


def main():
    download_pbf()
    filter_pbf()
    places = parse_filtered_pbf()
    seed_places(places)


if __name__ == "__main__":
    main()
