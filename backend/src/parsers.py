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

import re
from enum import Enum
from typing import Optional, Dict, Tuple


class AccessibilityStatus(Enum):
    """Enum representing accessibility status levels for places."""
    FULLY_ACCESSIBLE = "FULLY_ACCESSIBLE"
    PARTIALLY_ACCESSIBLE = "PARTIALLY_ACCESSIBLE"
    NOT_ACCESSIBLE = "NOT_ACCESSIBLE"


# word -> number dictionary for some common languages in OSM
_NUMBER_WORDS: Dict[str, int] = {
    **dict.fromkeys(("zero", "nul", "null", "zéro"), 0),
    **dict.fromkeys(("one", "ein", "une", "un", "uno", "uma", "um"), 1),
    **dict.fromkeys(("two", "zwei", "deux", "due", "dois", "duas"), 2),
    **dict.fromkeys(("three", "drei", "trois", "tre", "três"), 3),
}


def normalize(val: Optional[str]) -> Optional[str]:
    """Trim, lowercase, remove whitespace."""
    if not val:
        return None
    return re.sub(r'\s+', ' ', val.strip().lower())


def is_simple_token(val: str) -> bool:
    """Allow only single token or token+unit, reject full sentences."""
    parts = val.split()
    return len(parts) <= 2


def parse_number_word(val: str) -> Optional[int]:
    """Map a single number-word to its integer, else None."""
    return _NUMBER_WORDS.get(val)


def extract_integer(val: str) -> Optional[int]:
    """
    Extracts a plain integer from:
      - a lone digit or digits ("2", "42")
      - a number-word ("two", "zwei")
      - numeric+unit pair ("2 steps", "3m")
    Reject anything with decimal separators or multi-token sentences.
    """
    # must be simple
    if not is_simple_token(val):
        return None

    # reject decimals
    if re.search(r'\d+[.,]\d+', val):
        return None

    # try word→number
    num = parse_number_word(val)
    if num is not None:
        return num

    # try pulling digits
    try:
        m = re.match(r'^\D*(\d+)\D*$', val)
        if m:
            return int(m.group(1))
    except (ValueError, TypeError):
        pass

    return None


def parse_meters(value: Optional[str]) -> Optional[float]:
    """Convert various text measurements to meters."""
    v = normalize(value)
    if not v:
        return None

    # strip to digits, '.', ','
    clean = "".join(ch for ch in v if ch.isdigit() or ch in ".,")
    clean = clean.replace(",", ".")
    try:
        num = float(clean)
    except ValueError:
        return None

    # interpret units
    if "cm" in v:
        return num / 100.0
    if num > 10 and "m" not in v:
        # for example someone typed "150" meaning cm
        return num / 100.0
    return num


def parse_step_count(count: int) -> str:
    """Map count to an AccessibilityStatus value."""
    if count == 0:
        return AccessibilityStatus.FULLY_ACCESSIBLE.value
    if count == 1:
        return AccessibilityStatus.PARTIALLY_ACCESSIBLE.value
    return AccessibilityStatus.NOT_ACCESSIBLE.value


def parse_yes_no(value: Optional[str]) -> Optional[str]:
    """Parse common yes/no/limited accessibility indicators."""
    v = normalize(value)
    if not v:
        return None
    if v in {"yes", "wheelchair", "designated"}:
        return AccessibilityStatus.FULLY_ACCESSIBLE.value
    if v == "limited":
        return AccessibilityStatus.PARTIALLY_ACCESSIBLE.value
    if v == "no":
        return AccessibilityStatus.NOT_ACCESSIBLE.value
    return None


def parse_width(value: Optional[str]) -> Optional[str]:
    """Convert width measurements to accessibility status."""
    m = parse_meters(value)
    if m is None:
        return None
    if m == 0:
        return AccessibilityStatus.FULLY_ACCESSIBLE.value
    if m <= 0.7:
        return AccessibilityStatus.PARTIALLY_ACCESSIBLE.value
    return AccessibilityStatus.NOT_ACCESSIBLE.value


def parse_count(value: Optional[str]) -> Optional[str]:
    """Parse step counts into accessibility rating."""
    v = normalize(value)
    if not v:
        return None
    count = extract_integer(v)
    if count is None:
        return None
    return parse_step_count(count)


def parse_step_height(value: Optional[str]) -> Optional[str]:
    """Convert step height measurements to accessibility status."""
    height = parse_meters(value)
    if height is None:
        return None
    if height == 0:
        return AccessibilityStatus.FULLY_ACCESSIBLE.value
    elif height <= 0.03:  # 3 cm threshold for "small step"
        return AccessibilityStatus.PARTIALLY_ACCESSIBLE.value
    else:
        return AccessibilityStatus.NOT_ACCESSIBLE.value


def parse_restroom_maneuver(front: Optional[str], side: Optional[str]) -> Optional[str]:
    """Evaluate restroom maneuverability from front and side space."""
    f = parse_meters(front)
    s = parse_meters(side)
    if f is None or s is None:
        return None
    return (AccessibilityStatus.FULLY_ACCESSIBLE.value
            if f >= 1.5 and s >= 1.5
            else AccessibilityStatus.NOT_ACCESSIBLE.value)


def format_address(tags: Dict[str, str]) -> Optional[str]:
    """Format OSM address tags into "street, housenumber, city postcode" form."""
    if tags.get("addr:full"):
        return tags["addr:full"][:255]
    s = " ".join(filter(None, (tags.get("addr:street", ""), tags.get("addr:housenumber", ""))))
    c = " ".join(filter(None, (tags.get("addr:city", ""), tags.get("addr:postcode", ""))))
    out = ", ".join(filter(None, (s, c)))
    return out[:255] if out else None


def parse_accessibility_info(tags: Dict[str, str]) -> Tuple[Dict, Dict, Dict]:
    """
    Parse OSM tags into structured accessibility information. Some are set directly to None,
    because they are not available in OSM data.
    
    Returns:
        Tuple of (general_accessibility, entrance_accessibility, restroom_accessibility)
    """
    general = {
        "accessibility": parse_yes_no(tags.get("wheelchair")),
        "indoor_accessibility": parse_yes_no(tags.get("wheelchair:turning_circle")),
        "additional_info": (tags.get("wheelchair:description") or "")[:1000] or None,
    }
    
    entrance = {
        "accessibility": None,
        "step_count": parse_count(tags.get("entrance:step_count") or tags.get("entrance:steps")),
        "step_height": parse_step_height(tags.get("entrance:kerb:height")),  # Now returns enum value
        "ramp": parse_yes_no(tags.get("entrance:ramp") or tags.get("ramp") or tags.get("wheelchair:ramp")),
        "lift": None,
        "entrance_width": parse_width(tags.get("door:width") or tags.get("entrance:width")),
        "door_type": ("automatic" if normalize(tags.get("entrance:automatic_door")) == "yes"
                      else tags.get("entrance:door", "")[:50] or None),
    }
    
    restroom = {
        "accessibility": None,
        "door_width": parse_width(tags.get("toilets:wheelchair:door_width")),
        "room_maneuver": parse_restroom_maneuver(tags.get("toilets:wheelchair:space_front"),
                                               tags.get("toilets:wheelchair:space_side")),
        "toilet_seat": None,
        "grab_rails": None,
        "sink": None,
        "emergency_alarm": None,
        "euro_key": tags.get("centralkey") == "eurokey" if "centralkey" in tags else None,
    }
    
    return general, entrance, restroom
