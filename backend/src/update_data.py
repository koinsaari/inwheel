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
import argparse
from typing import List, Dict
from dotenv import load_dotenv
from place import PlaceHandler, Place, TAGS

load_dotenv(override=True)

DATABASE_URL = os.getenv("DATABASE_URL")
DATA_DIR = os.getenv("DATA_DIR", "data")
os.makedirs(DATA_DIR, exist_ok=True)

REGIONS = [
    {
        "name": "switzerland",
        "url": "https://download.geofabrik.de/europe/switzerland-latest.osm.pbf"
    },
    {
        "name": "finland",
        "url": "https://download.geofabrik.de/europe/finland-latest.osm.pbf"
    }
]


def download_pbf(region: Dict):
    """Download OSM PBF file for a region if not already present"""
    pbf_filename = os.path.join(DATA_DIR, f"{region['name']}-latest.osm.pbf")
    
    if os.path.exists(pbf_filename):
        print(f"{pbf_filename} already exists, skipping download.")
        return pbf_filename

    print(f"Downloading {region['name']} PBF file...")
    r = requests.get(region['url'], stream=True)
    with open(pbf_filename, 'wb') as f:
        for chunk in r.iter_content(chunk_size=8192):
            f.write(chunk)
    print(f"Download complete for {region['name']}.")
    return pbf_filename


def filter_pbf(pbf_filename: str):
    """Filter PBF file to extract only places with relevant tags"""
    filtered_filename = pbf_filename.replace('.osm.pbf', '-filtered.osm.pbf')
    
    if os.path.exists(filtered_filename):
        print(f"{filtered_filename} already exists, skipping filtering.")
        return filtered_filename
    
    tags_filter = []
    for tag_type, vals in TAGS.items():
        for val in vals:
            tags_filter.append(f"n/{tag_type}={val}")

    filter_cmd = ["osmium", "tags-filter", pbf_filename] + tags_filter + ["-o", filtered_filename]
    print("Running:", " ".join(filter_cmd))
    subprocess.run(filter_cmd, check=True)
    print(f"Filtering complete for {pbf_filename}.")
    return filtered_filename


def parse_filtered_pbf(filtered_filename: str, region_name: str) -> List[Place]:
    """Parse filtered PBF file and extract places with additional region info"""
    handler = PlaceHandler()
    handler.apply_file(filtered_filename)
    print(f"Parsing {filtered_filename}...")

    for place in handler.places:
        place.region = region_name
        
    return handler.places


def seed_places(places: List[Place], limit: int = None, update_all: bool = False):
    """
    Seed places to database.
    If update_all is False, places with user_modified flag will not be updated.
    """
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
            p.lon, p.lat,  # Geometry column
            p.region,
        )
        for p in places
    ]

    print(f"Inserting {len(places)} places for region {places[0].region if places else 'unknown'}...")
    cur.executemany(
        """
        INSERT INTO public.places (
        osm_id, name, category, lat, lon, geom, region, last_osm_update
        )
        VALUES (
        %s, %s, %s, %s, %s, ST_SetSRID(ST_MakePoint(%s, %s),4326), %s, now()
        )
        ON CONFLICT (osm_id) DO UPDATE SET
          name = EXCLUDED.name,
          category = EXCLUDED.category,
          lat = EXCLUDED.lat,
          lon = EXCLUDED.lon,
          geom = EXCLUDED.geom,
          region = EXCLUDED.region,
          last_osm_update = now()
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
              accessibility = CASE WHEN user_modified AND NOT %s THEN general_accessibility.accessibility ELSE EXCLUDED.accessibility END,
              indoor_accessibility = CASE WHEN user_modified AND NOT %s THEN general_accessibility.indoor_accessibility ELSE EXCLUDED.indoor_accessibility END,
              additional_info = CASE WHEN user_modified AND NOT %s THEN general_accessibility.additional_info ELSE EXCLUDED.additional_info END
            """,
            (
                ga.get("accessibility"),
                ga.get("indoor_accessibility"),
                ga.get("additional_info")[:1000] if ga.get("additional_info") else None,
                p.osm_id,
                update_all,
                update_all,
                update_all
            )
        )
        
        ea = p.entrance_accessibility
        cur.execute(
            """
            INSERT INTO public.entrance_accessibility (
              place_id, accessibility, step_count, step_height, ramp, lift, entrance_width, door_type
            )
            SELECT id, %s, %s, %s, %s, %s, %s, %s FROM public.places WHERE osm_id = %s
            ON CONFLICT (place_id) DO UPDATE SET
              accessibility = CASE WHEN user_modified AND NOT %s THEN entrance_accessibility.accessibility ELSE EXCLUDED.accessibility END,
              step_count = CASE WHEN user_modified AND NOT %s THEN entrance_accessibility.step_count ELSE EXCLUDED.step_count END,
              step_height = CASE WHEN user_modified AND NOT %s THEN entrance_accessibility.step_height ELSE EXCLUDED.step_height END,
              ramp = CASE WHEN user_modified AND NOT %s THEN entrance_accessibility.ramp ELSE EXCLUDED.ramp END,
              lift = CASE WHEN user_modified AND NOT %s THEN entrance_accessibility.lift ELSE EXCLUDED.lift END,
              entrance_width = CASE WHEN user_modified AND NOT %s THEN entrance_accessibility.entrance_width ELSE EXCLUDED.entrance_width END,
              door_type = CASE WHEN user_modified AND NOT %s THEN entrance_accessibility.door_type ELSE EXCLUDED.door_type END
            """,
            (
                ea.get("accessibility"),
                ea.get("step_count"),
                ea.get("step_height"),
                ea.get("ramp"),
                ea.get("lift"),
                ea.get("entrance_width"),
                ea.get("door_type"),
                p.osm_id,
                update_all,
                update_all,
                update_all,
                update_all,
                update_all,
                update_all,
                update_all
            )
        )
        
        ra = p.restroom_accessibility
        cur.execute(
            """
            INSERT INTO public.restroom_accessibility (
              place_id, accessibility, door_width, room_maneuver, grab_rails,
              sink, toilet_seat, emergency_alarm, euro_key
            )
            SELECT id, %s, %s, %s, %s, %s, %s, %s, %s FROM public.places WHERE osm_id = %s
            ON CONFLICT (place_id) DO UPDATE SET
              accessibility = CASE WHEN user_modified AND NOT %s THEN restroom_accessibility.accessibility ELSE EXCLUDED.accessibility END,
              door_width = CASE WHEN user_modified AND NOT %s THEN restroom_accessibility.door_width ELSE EXCLUDED.door_width END,
              room_maneuver = CASE WHEN user_modified AND NOT %s THEN restroom_accessibility.room_maneuver ELSE EXCLUDED.room_maneuver END,
              grab_rails = CASE WHEN user_modified AND NOT %s THEN restroom_accessibility.grab_rails ELSE EXCLUDED.grab_rails END,
              sink = CASE WHEN user_modified AND NOT %s THEN restroom_accessibility.sink ELSE EXCLUDED.sink END,
              toilet_seat = CASE WHEN user_modified AND NOT %s THEN restroom_accessibility.toilet_seat ELSE EXCLUDED.toilet_seat END,
              emergency_alarm = CASE WHEN user_modified AND NOT %s THEN restroom_accessibility.emergency_alarm ELSE EXCLUDED.emergency_alarm END,
              euro_key = CASE WHEN user_modified AND NOT %s THEN restroom_accessibility.euro_key ELSE EXCLUDED.euro_key END
            """,
            (
                ra.get("accessibility"),
                ra.get("door_width"),
                ra.get("room_maneuver"),
                ra.get("grab_rails"),
                ra.get("sink"),
                ra.get("toilet_seat"),
                ra.get("emergency_alarm"),
                ra.get("euro_key"),
                p.osm_id,
                update_all,
                update_all,
                update_all,
                update_all,
                update_all,
                update_all,
                update_all,
                update_all
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
    print(f"Seeding done. {len(places)} places seeded for region {places[0].region if places else 'unknown'}.")


def process_region(region: Dict, update_all: bool = False):
    """Process a single region from download to database insert"""
    pbf_filename = download_pbf(region)
    filtered_filename = filter_pbf(pbf_filename)
    places = parse_filtered_pbf(filtered_filename, region['name'])
    seed_places(places, update_all=update_all)
    return len(places)


def main():
    parser = argparse.ArgumentParser(description='Seed accessibility data from OSM')
    parser.add_argument('--region', help='Process only specific region')
    parser.add_argument('--update-all', action='store_true', help='Update all places, even user-modified ones')
    args = parser.parse_args()
    
    total_places = 0
    
    if args.region:
        # Process single region
        for region in REGIONS:
            if region['name'] == args.region:
                print(f"Processing region: {region['name']}")
                total_places += process_region(region, args.update_all)
                break
        else:
            print(f"Region {args.region} not found")
    else:
        # Process all regions
        for region in REGIONS:
            print(f"Processing region: {region['name']}")
            total_places += process_region(region, args.update_all)
    
    print(f"Total places processed: {total_places}")


if __name__ == "__main__":
    main()
