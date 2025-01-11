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

import osmium
from parsers import parse_toilets_info, parse_parking_info, parse_general_accessibility_info

TAGS = {
    "amenity": [
        "restaurant", "cafe", "bar", "pub" "fast_food", "pharmacy", "hospital",
        "parking", "fuel", "toilets", "library", "bank", "post_office",
        "cinema", "university", "school", "kindergarten", "college", "clinic", "casino",
        "nightclub", "courthouse"
    ],
    "tourism": [
        "hotel", "hostel", "motel", "guest-house", "chalet", "museum"
    ],
    "shop": [
        "supermarket", "bakery", "clothes",
        "electronics", "convenience", "cosmetics",
        "car", "bicycle", "motorcycle", "furniture",
        "jewelry", "shoes", "sports", "books",
    ]
}

def initialize_accessibility(tags: dict, category: str) -> dict:
    if category == "toilets":
        return parse_toilets_info(tags)
    elif category == "parking":
        return parse_parking_info(tags)
    else:
        return parse_general_accessibility_info(tags)


def initialize_user_accessibility(osm_accessibility: dict) -> dict:
    def recursive_default(structure):
        if isinstance(structure, dict):
            return {k: recursive_default(v) for k, v in structure.items()}
        return None
    
    return recursive_default(osm_accessibility)


def format_address(tags: dict) -> str:
    if "addr:full" in tags:
        return tags["addr:full"]

    address_parts = [
        tags.get("addr:street", ""),
        tags.get("addr:housenumber", ""),
        tags.get("addr:city", ""),
        tags.get("addr:postcode", ""),
    ]

    # delete empty parts and join them together with a comma
    formatted_address = ", ".join(part for part in address_parts if part)
    return formatted_address or None



def format_address(tags: dict) -> str:
    """
    Formats an address from tags.

    If "addr:full" exists, it is returned as-is. Otherwise, constructs the address as:
    "street housenumber, city postcode". Skips missing components.

    Args:
        tags (dict): Dictionary with address keys like "addr:full", "addr:street",
                    "addr:housenumber", "addr:city", "addr:postcode".

    Returns:
        str: Formatted address or None if no components are available.
    """
    if "addr:full" in tags:
        return tags["addr:full"]

    street_and_number = " ".join(
        part for part in [tags.get("addr:street", ""), tags.get("addr:housenumber", "")] if part
    )
    city_and_postcode = " ".join(
        part for part in [tags.get("addr:city", ""), tags.get("addr:postcode", "")] if part
    )
    address_parts = [street_and_number, city_and_postcode]
    formatted_address = ", ".join(part for part in address_parts if part)

    return formatted_address or None


class Place:
    def __init__(
            self,
            osm_id: int,
            name: str,
            category: str,
            lat: float,
            lon: float,
            contact: dict,
            accessibility_osm: dict,
            accessibility_user: dict
        ):
        self.osm_id = osm_id
        self.name = name
        self.category = category
        self.lat = lat
        self.lon = lon
        self.contact = contact
        self.accessibility_osm = accessibility_osm
        self.accessibility_user = accessibility_user


class PlaceHandler(osmium.SimpleHandler):
    def __init__(self):
        super().__init__()
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
            osm_accessibility = initialize_accessibility(tags, cat)
            user_accessibility = initialize_user_accessibility(osm_accessibility)

            contact = {
                "address": format_address(tags),
                "phone": tags.get("phone"),
                "email": tags.get("email"),
                "website": tags.get("website"),
            }

            place = Place(
                osm_id=n.id,
                name=tags.get("name", "Unknown"),
                category=cat,
                lat=n.location.lat,
                lon=n.location.lon,
                contact=contact,
                accessibility_osm=osm_accessibility,
                accessibility_user=user_accessibility
            )
            self.places.append(place)
