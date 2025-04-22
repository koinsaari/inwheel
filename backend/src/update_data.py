# Copyright © 2025 Aaro Koinsaari
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


def load_sql(filepath, **kwargs):
    """Load SQL from file and format with provided parameters"""
    sql_dir = os.path.join(os.path.dirname(os.path.dirname(__file__)), 'sql')
    with open(os.path.join(sql_dir, filepath), 'r') as f:
        sql = f.read()
    return sql.format(**kwargs)


def seed_places(places: List[Place], limit: int = None, overwrite: bool = False):
    """
    Seed places to database using staging tables.
    If overwrite is False, places with user_modified flag will not be updated.
    Uses a single transaction for all operations.
    """
    if limit:
        places = places[:limit]
    
    if not places:
        print("No places to insert.")
        return
    
    total_places = len(places)
    start_time = time.time()
    
    print(f"Preparing to insert {total_places} places for region {places[0].region}...")
    
    conn = psycopg.connect(DATABASE_URL)
    try:
        with conn.cursor() as cur:
            print("Creating staging tables...")
            cur.execute(load_sql("operations/staging/create_staging_tables.sql"))
            
            # Prepare data for bulk insert
            print("Preparing data for bulk insert...")
            places_data = []
            general_data = []
            entrance_data = []
            restroom_data = []
            contact_data = []
            
            for p in places:
                places_data.append((
                    p.osm_id,
                    p.name,
                    p.category,
                    p.lat,
                    p.lon,
                    p.region
                ))
                
                ga = p.general_accessibility
                general_data.append((
                    p.osm_id,
                    ga.get("accessibility"),
                    ga.get("indoor_accessibility"),
                    ga.get("additional_info")[:1000] if ga.get("additional_info") else None
                ))
                
                ea = p.entrance_accessibility
                entrance_data.append((
                    p.osm_id,
                    ea.get("accessibility"),
                    ea.get("step_count"),
                    ea.get("step_height"),
                    ea.get("ramp"),
                    ea.get("lift"),
                    ea.get("entrance_width"),
                    ea.get("door_type")
                ))
                
                ra = p.restroom_accessibility
                restroom_data.append((
                    p.osm_id,
                    ra.get("accessibility"),
                    ra.get("door_width"),
                    ra.get("room_maneuver"),
                    ra.get("grab_rails"),
                    ra.get("sink"),
                    ra.get("toilet_seat"),
                    ra.get("emergency_alarm"),
                    ra.get("euro_key")
                ))
                
                contact_data.append((
                    p.osm_id,
                    p.contact.get("phone"),
                    p.contact.get("website"),
                    p.contact.get("email"),
                    p.contact.get("address")
                ))
            
            print("Bulk inserting into staging tables...")
            stage_start = time.time()
            
            cur.executemany(load_sql("operations/staging/insert_staging_places.sql"), places_data)
            cur.executemany(load_sql("operations/staging/insert_staging_ga.sql"), general_data)
            cur.executemany(load_sql("operations/staging/insert_staging_ea.sql"), entrance_data)
            cur.executemany(load_sql("operations/staging/insert_staging_ra.sql"), restroom_data)
            cur.executemany(load_sql("operations/staging/insert_staging_contact.sql"), contact_data)
            
            stage_time = time.time() - stage_start
            print(f"Staging tables populated in {stage_time:.2f}s")
            
            # Merge from staging tables to main tables
            print("Merging data from staging tables to main tables...")
            merge_start = time.time()
            
            print("Updating places...")
            cur.execute(load_sql("operations/insert_places.sql"))
            overwrite_clause = "TRUE" if overwrite else "FALSE"
            
            print("Updating general accessibility...")
            cur.execute(load_sql("operations/insert_ga.sql", overwrite=overwrite_clause))
            
            print("Updating entrance accessibility...")
            cur.execute(load_sql("operations/insert_ea.sql", overwrite=overwrite_clause))
            
            print("Updating restroom accessibility...")
            cur.execute(load_sql("operations/insert_ra.sql", overwrite=overwrite_clause))
            
            print("Updating contact information...")
            cur.execute(load_sql("operations/insert_contact.sql"))
            
            merge_time = time.time() - merge_start
            print(f"Merge completed in {merge_time:.2f}s")
            
            conn.commit()
            print("Committing all changes.")
            
    except Exception as e:
        conn.rollback()
        print(f"Error occurred, rolling back all changes: {str(e)}")
        raise
    finally:
        conn.close()
    
    total_time = time.time() - start_time
    print(f"Seeding done. {total_places} places seeded for region {places[0].region} in {total_time:.2f}s ({total_places/total_time:.1f} places/s)")


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
