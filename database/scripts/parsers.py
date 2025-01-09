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
    LIMITED_ACCESSIBILITY = "LIMITED_ACCESSIBILITY"
    NOT_ACCESSIBLE = "NOT_ACCESSIBLE"


def parse_yes_no(value: str) -> Optional[str]:
    if not value:
        return None
    val = value.strip().lower()
    if val in {"yes", "wheelchair", "designated"}:
        return AccessibilityStatus.FULLY_ACCESSIBLE.value
    elif val == "limited":
        return AccessibilityStatus.LIMITED_ACCESSIBILITY.value
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
            return AccessibilityStatus.LIMITED_ACCESSIBILITY.value
        elif v > 0.7:
            return AccessibilityStatus.NOT_ACCESSIBLE.value
    return None
    

def parse_restroom_manuever(front: Optional[str], side: Optional[str]) -> Optional[str]:
    if front is None or side is None:
        return None
    
    front_val = parse_meters(front)
    side_val = parse_meters(side)

    if front_val is None or side_val is None:
        return None
    
    if front_val and side_val >= 1.5:
        return AccessibilityStatus.FULLY_ACCESSIBLE.value
    else:
        return AccessibilityStatus.NOT_ACCESSIBLE.value
    

def parse_general_accessibility_info(tags: Dict[str, str]) -> Dict[str, Optional[float]]:
    def parse_count(value: Optional[str]) -> Optional[int]:
        if not value:
            return None
        try:
            return int(value)
        except ValueError:
            return None

    def parse_entrance_steps(tags: Dict[str, str]) -> Dict[str, Optional[float]]:
        return {
            "step_count": parse_count(tags.get("entrance:step_count") or tags.get("entrance:steps")),
            "step_height": parse_meters(tags.get("entrance:kerb:height")),
            "ramp": parse_yes_no(
                tags.get("entrance:ramp") or tags.get("ramp") or tags.get("wheelchair:ramp")
            ),
            "lift": None
        }

    def parse_entrance_door(tags: Dict[str, str]) -> Dict[str, Optional[str]]:
        door_type = "automatic" if tags.get("entrance:automatic_door", "").strip().lower() == "yes" else tags.get("entrance:door")
        return {
            "width": parse_width(tags.get("door:width") or tags.get("entrance:width")),
            "type": door_type
        }

    def parse_restroom(tags: Dict[str, str]) -> Dict[str, Optional[float]]:
        return {
            "door_width": parse_width(tags.get("toilets:wheelchair:door_width")),
            "room_manuever": parse_restroom_manuever("toilets:wheelchair:space_front", "toilets:wheelchair:space_side"),
            "toilet_seat": None,
            "grab_rails": None,
            "sink": None,
            "emergency_alarm": None,
            "euro_key": tags.get("toilets:centralkey") == "eurokey",
            "accessible_via": tags.get("toilets:wheelchair:accessible_via"),
            "additional_info": None
        }
    return {
        "accessibility_status": parse_yes_no(tags.get("wheelchair")),
        "indoor_accessibility": parse_yes_no(tags.get("wheelchair:turning_circle")),
        "entrance": {
            "steps": parse_entrance_steps(tags),
            "door": parse_entrance_door(tags),
            "additional_info": None
        },
        "restroom": parse_restroom(tags),
        "additional_info": tags.get("wheelchair:description")
    }
    

def parse_toilets_info(tags: Dict[str, str]) -> Dict[str, Optional[float]]:
    return {
        "door_width": parse_width(tags.get("door:width") or tags.get("entrance:width")),
        "room_manuever": parse_restroom_manuever(tags.get("toilets:wheelchair:space_front"), tags.get("toilets:wheelchair:space_side")),
        "grab_rails": parse_yes_no(tags.get("toilets:wheelchair:grab_rails")),
        "toilet_seat": None,
        "sink": None,
        "emergency_alarm": None,
        "euro_key": tags.get("centralkey") == "eurokey",
        "additional_info": tags.get("wheelchair:description")
    }


def parse_parking_info(tags: Dict[str, str]) -> Dict[str, Optional[float]]:
    return {
        "accessible_spot_count": tags.get("capacity:disabled") if tags.get("capacity:disabled") else None,
        "surface": tags.get("surface") or None,
        "parking_type": tags.get("parking_type"),
        "has_elevator": None,
        "additional_info": tags.get("description")
    }