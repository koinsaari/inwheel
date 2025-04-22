-- Copyright © 2025 Aaro Koinsaari
-- Licensed under the Apache License, Version 2.0
-- http://www.apache.org/licenses/LICENSE-2.0

INSERT INTO staging_ra (osm_id, accessibility, door_width, room_maneuver, grab_rails, sink, toilet_seat, emergency_alarm, euro_key)
VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)