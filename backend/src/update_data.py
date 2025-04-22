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
import psycopg
import argparse
import time
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


def seed_places(places: List[Place], limit: int = None, overwrite: bool = False):
    """
    Seed places to database with batching.
    If overwrite is False, places with user_modified flag will not be updated.
    Uses a single transaction for all operations.
    """
    if limit:
        places = places[:limit]
    
    if not places:
        print("No places to insert.")
        return
        
    BATCH_SIZE = 2000
    total_places = len(places)
    total_batches = (total_places + BATCH_SIZE - 1) // BATCH_SIZE
    start_time = time.time()
    
    print(f"Inserting {total_places} places for region {places[0].region} in batches of {BATCH_SIZE}...")
    
    conn = psycopg.connect(DATABASE_URL)
    try:
        with conn.cursor() as cur:
            for i in range(0, total_places, BATCH_SIZE):
                batch = places[i:i+BATCH_SIZE]
                batch_start = time.time()
                
                print(f"Processing batch {i//BATCH_SIZE + 1}/{total_batches} ({len(batch)} places)")
                
                places_sql = """
                INSERT INTO public.places (
                osm_id, name, category, lat, lon, geom, region, last_osm_update
                )
                VALUES (%s, %s, %s, %s, %s, ST_SetSRID(ST_MakePoint(%s, %s),4326), %s, now())
                ON CONFLICT (osm_id) DO UPDATE SET
                  name = EXCLUDED.name,
                  category = EXCLUDED.category,
                  lat = EXCLUDED.lat,
                  lon = EXCLUDED.lon,
                  geom = EXCLUDED.geom,
                  region = EXCLUDED.region,
                  last_osm_update = now()
                """
                
                place_values = [
                    (
                        p.osm_id,
                        p.name,
                        p.category,
                        p.lat, p.lon,
                        p.lon, p.lat,
                        p.region,
                    )
                    for p in batch
                ]
                cur.executemany(places_sql, place_values)
                
                ga_sql = """
                INSERT INTO public.general_accessibility (place_id, accessibility, indoor_accessibility, additional_info)
                SELECT id, %s, %s, %s FROM public.places WHERE osm_id = %s
                ON CONFLICT (place_id) DO UPDATE SET 
                  accessibility = CASE WHEN general_accessibility.user_modified AND NOT %s THEN general_accessibility.accessibility ELSE EXCLUDED.accessibility END,
                  indoor_accessibility = CASE WHEN general_accessibility.user_modified AND NOT %s THEN general_accessibility.indoor_accessibility ELSE EXCLUDED.indoor_accessibility END,
                  additional_info = CASE WHEN general_accessibility.user_modified AND NOT %s THEN general_accessibility.additional_info ELSE EXCLUDED.additional_info END
                """
                
                ea_sql = """
                INSERT INTO public.entrance_accessibility (
                  place_id, accessibility, step_count, step_height, ramp, lift, entrance_width, door_type
                )
                SELECT id, %s, %s, %s, %s, %s, %s, %s FROM public.places WHERE osm_id = %s
                ON CONFLICT (place_id) DO UPDATE SET
                  accessibility = CASE WHEN entrance_accessibility.user_modified AND NOT %s THEN entrance_accessibility.accessibility ELSE EXCLUDED.accessibility END,
                  step_count = CASE WHEN entrance_accessibility.user_modified AND NOT %s THEN entrance_accessibility.step_count ELSE EXCLUDED.step_count END,
                  step_height = CASE WHEN entrance_accessibility.user_modified AND NOT %s THEN entrance_accessibility.step_height ELSE EXCLUDED.step_height END,
                  ramp = CASE WHEN entrance_accessibility.user_modified AND NOT %s THEN entrance_accessibility.ramp ELSE EXCLUDED.ramp END,
                  lift = CASE WHEN entrance_accessibility.user_modified AND NOT %s THEN entrance_accessibility.lift ELSE EXCLUDED.lift END,
                  entrance_width = CASE WHEN entrance_accessibility.user_modified AND NOT %s THEN entrance_accessibility.entrance_width ELSE EXCLUDED.entrance_width END,
                  door_type = CASE WHEN entrance_accessibility.user_modified AND NOT %s THEN entrance_accessibility.door_type ELSE EXCLUDED.door_type END
                """
                
                ra_sql = """
                INSERT INTO public.restroom_accessibility (
                  place_id, accessibility, door_width, room_maneuver, grab_rails,
                  sink, toilet_seat, emergency_alarm, euro_key
                )
                SELECT id, %s, %s, %s, %s, %s, %s, %s, %s FROM public.places WHERE osm_id = %s
                ON CONFLICT (place_id) DO UPDATE SET
                  accessibility = CASE WHEN restroom_accessibility.user_modified AND NOT %s THEN restroom_accessibility.accessibility ELSE EXCLUDED.accessibility END,
                  door_width = CASE WHEN restroom_accessibility.user_modified AND NOT %s THEN restroom_accessibility.door_width ELSE EXCLUDED.door_width END,
                  room_maneuver = CASE WHEN restroom_accessibility.user_modified AND NOT %s THEN restroom_accessibility.room_maneuver ELSE EXCLUDED.room_maneuver END,
                  grab_rails = CASE WHEN restroom_accessibility.user_modified AND NOT %s THEN restroom_accessibility.grab_rails ELSE EXCLUDED.grab_rails END,
                  sink = CASE WHEN restroom_accessibility.user_modified AND NOT %s THEN restroom_accessibility.sink ELSE EXCLUDED.sink END,
                  toilet_seat = CASE WHEN restroom_accessibility.user_modified AND NOT %s THEN restroom_accessibility.toilet_seat ELSE EXCLUDED.toilet_seat END,
                  emergency_alarm = CASE WHEN restroom_accessibility.user_modified AND NOT %s THEN restroom_accessibility.emergency_alarm ELSE EXCLUDED.emergency_alarm END,
                  euro_key = CASE WHEN restroom_accessibility.user_modified AND NOT %s THEN restroom_accessibility.euro_key ELSE EXCLUDED.euro_key END
                """
                
                contact_sql = """
                INSERT INTO public.contact (place_id, phone, website, email, address)
                SELECT id, %s, %s, %s, %s FROM public.places WHERE osm_id = %s
                ON CONFLICT (place_id) DO UPDATE SET 
                  phone = EXCLUDED.phone,
                  website = EXCLUDED.website,
                  email = EXCLUDED.email,
                  address = EXCLUDED.address
                """
                
                for p in batch:
                    ga = p.general_accessibility
                    cur.execute(
                        ga_sql,
                        (
                            ga.get("accessibility"),
                            ga.get("indoor_accessibility"),
                            ga.get("additional_info")[:1000] if ga.get("additional_info") else None,
                            p.osm_id,
                            overwrite,
                            overwrite,
                            overwrite
                        )
                    )
                    
                    ea = p.entrance_accessibility
                    cur.execute(
                        ea_sql,
                        (
                            ea.get("accessibility"),
                            ea.get("step_count"),
                            ea.get("step_height"),
                            ea.get("ramp"),
                            ea.get("lift"),
                            ea.get("entrance_width"),
                            ea.get("door_type"),
                            p.osm_id,
                            overwrite,
                            overwrite,
                            overwrite,
                            overwrite,
                            overwrite,
                            overwrite,
                            overwrite
                        )
                    )
                    
                    ra = p.restroom_accessibility
                    cur.execute(
                        ra_sql,
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
                            overwrite,
                            overwrite,
                            overwrite,
                            overwrite,
                            overwrite,
                            overwrite,
                            overwrite,
                            overwrite
                        )
                    )

                    cur.execute(
                        contact_sql,
                        (
                            p.contact.get("phone"),
                            p.contact.get("website"),
                            p.contact.get("email"),
                            p.contact.get("address"),
                            p.osm_id
                        )
                    )

                batch_time = time.time() - batch_start
                print(f"Batch {i//BATCH_SIZE + 1} complete: {len(batch)} places inserted in {batch_time:.2f}s ({len(batch)/batch_time:.1f} places/s)")
            
            conn.commit()
            print(f"Committing all changes.")
            
    except Exception as e:
        conn.rollback()
        print(f"Error occurred, rolling back all changes: {str(e)}")
        raise
    finally:
        conn.close()
    
    total_time = time.time() - start_time
    print(f"Seeding done. {total_places} places seeded for region {places[0].region} in {total_time:.2f}s ({total_places/total_time:.1f} places/s)")


def process_region(region: Dict, overwrite: bool = False):
    """Process a single region from download to database insert"""
    pbf_filename = download_pbf(region)
    filtered_filename = filter_pbf(pbf_filename)
    places = parse_filtered_pbf(filtered_filename, region['name'])
    seed_places(places, overwrite=overwrite)
    return len(places)


def main():
    parser = argparse.ArgumentParser(description='Seed accessibility data from OSM')
    parser.add_argument('--region', help='Process only specific region')
    parser.add_argument('--overwrite', action='store_true', help='Overwrite all places, even user-modified ones')
    parser.add_argument('--test', action='store_true', help='Test mode: only process 100 places')
    args = parser.parse_args()
    
    total_places = 0
    
    if args.region:
        # Process single region
        for region in REGIONS:
            if region['name'] == args.region:
                print(f"Processing region: {region['name']}")
                places = parse_filtered_pbf(filter_pbf(download_pbf(region)), region['name'])
                if args.test:
                    print("TEST MODE: Only processing 100 places")
                    seed_places(places, limit=100, overwrite=args.overwrite)
                    total_places += min(100, len(places))
                else:
                    seed_places(places, overwrite=args.overwrite)
                    total_places += len(places)
                break
        else:
            print(f"Region {args.region} not found")
    else:
        # Process all regions
        for region in REGIONS:
            print(f"Processing region: {region['name']}")
            places = parse_filtered_pbf(filter_pbf(download_pbf(region)), region['name'])
            if args.test:
                print("TEST MODE: Only processing 100 places")
                seed_places(places, limit=100, overwrite=args.overwrite)
                total_places += min(100, len(places))
            else:
                seed_places(places, overwrite=args.overwrite)
                total_places += len(places)
    
    print(f"Total places processed: {total_places}")


if __name__ == "__main__":
    main()
