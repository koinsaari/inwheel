-- Copyright © 2025 Aaro Koinsaari
-- Licensed under the Apache License, Version 2.0
-- http://www.apache.org/licenses/LICENSE-2.0

INSERT INTO staging_ea (osm_id, accessibility, step_count, step_height, ramp, lift, entrance_width, door_type)
VALUES (%s, %s, %s, %s, %s, %s, %s, %s)