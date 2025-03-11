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
    if limit:
        places = places[:limit]

    conn = psycopg2.connect(DATABASE_URL)
    cur = conn.cursor()

    place_values = [
        (
            p.osm_id,
            p.name,
            p.category,
            p.lat, p.lon,  # Regular columns
            p.lon, p.lat  # Geometry column
        )
        for p in places
    ]

    print("Inserting places. This might take a while...")
    cur.executemany(
        """
        INSERT INTO public.places (
        osm_id, name, category, lat, lon, geom, last_osm_update
        )
        VALUES (
        %s, %s, %s, %s, %s, ST_SetSRID(ST_MakePoint(%s, %s),4326), now()
        )
        ON CONFLICT (osm_id) DO NOTHING
        """,
        place_values
    )

    for p in places:
        ga = p.general_accessibility
        cur.execute(
            """
            INSERT INTO public.general_accessibility (place_id, accessibility, indoor_accessibility, additional_info)
            SELECT id, %s, %s, %s FROM public.places WHERE osm_id = %s
            ON CONFLICT (place_id) DO UPDATE SET 
              accessibility = EXCLUDED.accessibility,
              indoor_accessibility = EXCLUDED.indoor_accessibility,
              additional_info = EXCLUDED.additional_info
            """,
            (
                ga.get("accessibility"),
                ga.get("indoor_accessibility"),
                ga.get("additional_info")[:1000] if ga.get("additional_info") else None,
                p.osm_id
            )
        )
        
        ea = p.entrance_accessibility
        cur.execute(
            """
            INSERT INTO public.entrance_accessibility (
              place_id, accessibility, step_count, step_height, ramp, lift, width, type, additional_info
            )
            SELECT id, %s, %s, %s, %s, %s, %s, %s, %s FROM public.places WHERE osm_id = %s
            ON CONFLICT (place_id) DO UPDATE SET
              accessibility = EXCLUDED.accessibility,
              step_count = EXCLUDED.step_count,
              step_height = EXCLUDED.step_height,
              ramp = EXCLUDED.ramp,
              lift = EXCLUDED.lift,
              width = EXCLUDED.width,
              type = EXCLUDED.type,
              additional_info = EXCLUDED.additional_info
            """,
            (
                ea.get("accessibility"),
                ea.get("step_count"),
                ea.get("step_height"),
                ea.get("ramp"),
                ea.get("lift"),
                ea.get("width"),
                ea.get("type"),
                ea.get("additional_info")[:1000] if ea.get("additional_info") else None,
                p.osm_id
            )
        )
        
        ra = p.restroom_accessibility
        cur.execute(
            """
            INSERT INTO public.restroom_accessibility (
              place_id, accessibility, door_width, room_maneuver, grab_rails,
              sink, toilet_seat, emergency_alarm, accessible_via, euro_key, additional_info
            )
            SELECT id, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s FROM public.places WHERE osm_id = %s
            ON CONFLICT (place_id) DO UPDATE SET
              accessibility = EXCLUDED.accessibility,
              door_width = EXCLUDED.door_width,
              room_maneuver = EXCLUDED.room_maneuver,
              grab_rails = EXCLUDED.grab_rails,
              sink = EXCLUDED.sink,
              toilet_seat = EXCLUDED.toilet_seat,
              emergency_alarm = EXCLUDED.emergency_alarm,
              accessible_via = EXCLUDED.accessible_via,
              euro_key = EXCLUDED.euro_key,
              additional_info = EXCLUDED.additional_info
            """,
            (
                ra.get("accessibility"),
                ra.get("door_width"),
                ra.get("room_maneuver"),
                ra.get("grab_rails"),
                ra.get("sink"),
                ra.get("toilet_seat"),
                ra.get("emergency_alarm"),
                ra.get("accessible_via"),
                ra.get("euro_key"),
                ra.get("additional_info")[:1000] if ra.get("additional_info") else None,
                p.osm_id
            )
        )

        cur.execute(
            """
            INSERT INTO public.contact (place_id, phone, website, email, address)
            SELECT id, %s, %s, %s, %s FROM public.places WHERE osm_id = %s
            ON CONFLICT (place_id) DO UPDATE SET 
              phone = EXCLUDED.phone,
              website = EXCLUDED.website,
              email = EXCLUDED.email,
              address = EXCLUDED.address
            """,
            (
                p.contact.get("phone"),
                p.contact.get("website"),
                p.contact.get("email"),
                p.contact.get("address"),
                p.osm_id
            )
        )

    conn.commit()
    cur.close()
    conn.close()
    print(f"Seeding done. {len(places)} places seeded.")


def main():
    download_pbf()
    filter_pbf()
    places = parse_filtered_pbf()
    seed_places(places)


if __name__ == "__main__":
    main()
