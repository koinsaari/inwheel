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
import osmium
import psycopg2
from typing import List
from psycopg2.extras import Json
from parsers import parse_general_accessibility_info, parse_parking_info, parse_toilets_info
from dotenv import load_dotenv

load_dotenv()

DATABASE_URL = os.getenv("DATABASE_URL")
DATABASE_API_KEY = os.getenv("DATABASE_API_KEY")
PBF_FILENAME = "switzerland-latest.osm.pbf"
FILTERED_FILENAME = "switzerland-filtered.osm.pbf"
SWITZERLAND_PBF_URL="https://download.geofabrik.de/europe/switzerland-latest.osm.pbf"

TAGS = {
    "amenity": [
        "restaurant", "cafe", "bar", "fast_food", "pharmacy", "hospital",
        "parking", "fuel", "toilets", "library", "bank", "post_office",
        "cinema"
    ],
    "tourism": [
        "hotel", "hostel", "motel", "guest-house", "chalet", "museum"
    ],
    "shop": [
        "supermarket", "bakery", "clothes",
        "electronics", "convenience", "cosmetics"
    ]
}

class Place:
    def __init__(self, osm_id: int, name: str, category: str, lat: float, lon: float, accessibility: dict):
        self.osm_id = osm_id
        self.name = name
        self.category = category
        self.lat = lat
        self.lon = lon
        self.accessibility = accessibility


class PlaceHandler(osmium.SimpleHandler):
    def __init__(self):
        super().__init__
        self.places = []

    def node(self, n):
        tags = dict(n.tags)
        cat = None
        if "amenity" in tags and tags["amenity"] in TAGS["amenity"]:
            cat = tags["amenity"]
        elif "shop" in tags and tags["shop"] in TAGS["shop"]:
            cat = tags["shop"]
        elif "tourism" in tags and tags["tourism"] in TAGS["tourism"]:
            cat = tags["tourism"]

        if cat:
            place = Place(
            osm_id=n.id,
            name=tags.get("name", "Unknown"),
            category=cat,
            lat=n.location.lat,
            lon=n.location.lon
            )
            if cat == "toilets":
                place.accessibility = parse_toilets_info(tags)
            elif cat == "parking":
                place.accessibility = parse_parking_info(tags)
            else:
                place.accessibility = parse_general_accessibility_info(tags)
            self.places.append(place)


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


def seed_places(places: List[Place]):
    conn = psycopg2.connect(DATABASE_URL)
    cur = conn.cursor()
    for p in places:
        cur.execute("""
            INSERT INTO places (osm_id, name, category, lat, lon, accessibility, last_osm_update, created_at)
            VALUES (%s, %s, %s, %s, %s, %s, NOW(), NOW())
        """, (
            p.osm_id,
            p.name,
            p.category,
            p.lat,
            p.lon,
            Json(p.accessibility)
        ))
        print(f"Inserted new place osm_id={p.osm_id}")
    conn.commit()
    cur.close()
    conn.close()
    print("Seeding done.")


def main():
    download_pbf()
    filter_pbf()
    places = parse_filtered_pbf()
    seed_places(places)


if __name__ == "__main__":
    main()