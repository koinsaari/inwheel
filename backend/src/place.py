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

import osmium
from parsers import parse_accessibility_info, format_address

TAGS = {
    "amenity": [
        "restaurant", "cafe", "bar", "pub", "pharmacy", "hospital",
        "fuel", "toilets", "library", "bank", "cinema", "university",
        "school", "kindergarten", "college", "clinic", "nightclub", "courthouse"
    ],
    "tourism": [
        "hotel", "hostel", "museum"
    ],
    "shop": [
        "supermarket", "bakery", "clothes",
        "electronics", "convenience", "cosmetics",
        "car", "bicycle", "motorcycle", "furniture",
        "jewelry", "shoes", "sports", "books",
    ]
}


class Place:
    def __init__(
        self,
        osm_id: int,
        name: str,
        category: str,
        lat: float,
        lon: float,
        contact: dict,
        general_accessibility: dict,
        entrance_accessibility: dict,
        restroom_accessibility: dict,
        region: str = None
    ):
        self.osm_id = osm_id
        self.name = name
        self.category = category
        self.lat = lat
        self.lon = lon
        self.contact = contact
        self.general_accessibility = general_accessibility
        self.entrance_accessibility = entrance_accessibility
        self.restroom_accessibility = restroom_accessibility
        self.region = region


class PlaceHandler(osmium.SimpleHandler):
    def __init__(self, region=None):
        super().__init__()
        self.places = []
        self.region = region

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
            general_acc, entrance_acc, restroom_acc = parse_accessibility_info(tags)
            contact = {
                "address": format_address(tags),
                "phone": tags.get("phone")[:100] if tags.get("phone") else None,
                "email": tags.get("email")[:255] if tags.get("email") else None,
                "website": tags.get("website")[:255] if tags.get("website") else None,
            }

            place = Place(
                osm_id=n.id,
                name=tags.get("name", "Unknown")[:255],
                category=cat[:50],
                lat=n.location.lat,
                lon=n.location.lon,
                contact=contact,
                general_accessibility=general_acc,
                entrance_accessibility=entrance_acc,
                restroom_accessibility=restroom_acc,
                region=self.region[:50]
            )
            self.places.append(place)
