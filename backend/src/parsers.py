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

from enum import Enum
from typing import Optional, Dict

class AccessibilityStatus(Enum):
    FULLY_ACCESSIBLE = "FULLY_ACCESSIBLE"
    PARTIALLY_ACCESSIBLE = "PARTIALLY_ACCESSIBLE"
    NOT_ACCESSIBLE = "NOT_ACCESSIBLE"


def parse_yes_no(value: str) -> Optional[str]:
    if not value:
        return None
    val = value.strip().lower()
    if val in {"yes", "wheelchair", "designated"}:
        return AccessibilityStatus.FULLY_ACCESSIBLE.value
    elif val == "limited":
        return AccessibilityStatus.PARTIALLY_ACCESSIBLE.value
    elif val == "no":
        return AccessibilityStatus.NOT_ACCESSIBLE.value
    return None


def parse_meters(value: str) -> Optional[float]:
    if not value:
        return None
    v = value.strip().lower()
    v_clean = "".join(ch for ch in v if ch.isdigit() or ch in [".", ","])
    v_clean = v_clean.replace(",", ".")
    try:
        val = float(v_clean)
        if "cm" in v:
            return val / 100
        if val > 10 and "m" not in v:
            return val / 100
        return val
    except ValueError:
        return None
    

def parse_width(value: Optional[str]) -> Optional[str]:
    v = parse_meters(value)
    if v is not None:
        if v == 0:
            return AccessibilityStatus.FULLY_ACCESSIBLE.value
        elif v <= 0.7:
            return AccessibilityStatus.PARTIALLY_ACCESSIBLE.value
        elif v > 0.7:
            return AccessibilityStatus.NOT_ACCESSIBLE.value
    return None


def parse_count(value: Optional[str]) -> Optional[str]:
    if not value:
        return None
    try:
        step_count = int(value)
        if step_count == 0:
            return AccessibilityStatus.FULLY_ACCESSIBLE.value
        elif step_count == 1:
            return AccessibilityStatus.PARTIALLY_ACCESSIBLE.value
        elif step_count > 1:
            return AccessibilityStatus.NOT_ACCESSIBLE.value
        return None
    except ValueError:
        return None


def parse_restroom_maneuver(front: Optional[str], side: Optional[str]) -> Optional[str]:
    if front is None or side is None:
        return None
    
    front_val = parse_meters(front)
    side_val = parse_meters(side)

    if front_val is None or side_val is None:
        return None
    
    if front_val >= 1.5 and side_val >= 1.5:
        return AccessibilityStatus.FULLY_ACCESSIBLE.value
    else:
        return AccessibilityStatus.NOT_ACCESSIBLE.value


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

    return formatted_address[:255] or None


def parse_accessibility_info(tags: Dict[str, str]):
    general = {
        "accessibility": parse_yes_no(tags.get("wheelchair")),
        "indoor_accessibility": parse_yes_no(tags.get("wheelchair:turning_circle")),
        "additional_info": (tags.get("wheelchair:description")[:1000] if tags.get("wheelchair:description") else None)
    }
    entrance = {
        "accessibility": None,  
        "step_count": parse_count(tags.get("entrance:step_count") or tags.get("entrance:steps")),
        "step_height": parse_meters(tags.get("entrance:kerb:height")),
        "ramp": parse_yes_no(tags.get("entrance:ramp") or tags.get("ramp") or tags.get("wheelchair:ramp")),
        "lift": None,
        "entrance_width": parse_width(tags.get("door:width") or tags.get("entrance:width")),
        "door_type": "automatic" if tags.get("entrance:automatic_door", "").strip().lower() == "yes" else (tags.get("entrance:door")[:50] if tags.get("entrance:door") else None)
    }
    restroom = {
        "accessibility": None,
        "door_width": parse_width(tags.get("toilets:wheelchair:door_width")),
        "room_maneuver": parse_restroom_maneuver(tags.get("toilets:wheelchair:space_front"), tags.get("toilets:wheelchair:space_side")),
        "toilet_seat": None,
        "grab_rails": None,
        "sink": None,
        "emergency_alarm": None,
        "euro_key": tags.get("centralkey") == "eurokey" if "centralkey" in tags else None
    }
    
    return general, entrance, restroom
