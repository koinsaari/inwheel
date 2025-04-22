-- Copyright © 2025 Aaro Koinsaari
-- Licensed under the Apache License, Version 2.0
-- http://www.apache.org/licenses/LICENSE-2.0

INSERT INTO public.restroom_accessibility (
    place_id, accessibility, door_width, room_maneuver, grab_rails, 
    sink, toilet_seat, emergency_alarm, euro_key
)
SELECT 
    p.id, 
    sr.accessibility::ACCESSIBILITY_STATUS,
    sr.door_width::ACCESSIBILITY_STATUS,
    sr.room_maneuver::ACCESSIBILITY_STATUS,
    sr.grab_rails::ACCESSIBILITY_STATUS,
    sr.sink::ACCESSIBILITY_STATUS,
    sr.toilet_seat::ACCESSIBILITY_STATUS,
    sr.emergency_alarm::ACCESSIBILITY_STATUS,
    sr.euro_key
FROM staging_ra sr
JOIN public.places p ON p.osm_id = sr.osm_id
ON CONFLICT (place_id) DO UPDATE SET
    accessibility = CASE WHEN restroom_accessibility.user_modified AND NOT {overwrite} 
        THEN restroom_accessibility.accessibility 
        ELSE EXCLUDED.accessibility END,
    door_width = CASE WHEN restroom_accessibility.user_modified AND NOT {overwrite} 
        THEN restroom_accessibility.door_width 
        ELSE EXCLUDED.door_width END,
    room_maneuver = CASE WHEN restroom_accessibility.user_modified AND NOT {overwrite} 
        THEN restroom_accessibility.room_maneuver 
        ELSE EXCLUDED.room_maneuver END,
    grab_rails = CASE WHEN restroom_accessibility.user_modified AND NOT {overwrite} 
        THEN restroom_accessibility.grab_rails 
        ELSE EXCLUDED.grab_rails END,
    sink = CASE WHEN restroom_accessibility.user_modified AND NOT {overwrite} 
        THEN restroom_accessibility.sink 
        ELSE EXCLUDED.sink END,
    toilet_seat = CASE WHEN restroom_accessibility.user_modified AND NOT {overwrite} 
        THEN restroom_accessibility.toilet_seat 
        ELSE EXCLUDED.toilet_seat END,
    emergency_alarm = CASE WHEN restroom_accessibility.user_modified AND NOT {overwrite} 
        THEN restroom_accessibility.emergency_alarm 
        ELSE EXCLUDED.emergency_alarm END,
    euro_key = CASE WHEN restroom_accessibility.user_modified AND NOT {overwrite} 
        THEN restroom_accessibility.euro_key 
        ELSE EXCLUDED.euro_key END