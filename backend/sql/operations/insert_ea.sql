-- Copyright © 2025 Aaro Koinsaari
-- Licensed under the Apache License, Version 2.0
-- http://www.apache.org/licenses/LICENSE-2.0

INSERT INTO public.entrance_accessibility (
    place_id, accessibility, step_count, step_height, ramp, lift, entrance_width, door_type
)
SELECT 
    p.id, 
    se.accessibility::ACCESSIBILITY_STATUS,
    se.step_count::ACCESSIBILITY_STATUS,
    se.step_height::ACCESSIBILITY_STATUS,
    se.ramp::ACCESSIBILITY_STATUS,
    se.lift::ACCESSIBILITY_STATUS,
    se.entrance_width::ACCESSIBILITY_STATUS,
    se.door_type
FROM staging_ea se
JOIN public.places p ON p.osm_id = se.osm_id
ON CONFLICT (place_id) DO UPDATE SET
    accessibility = CASE WHEN entrance_accessibility.user_modified AND NOT {overwrite} 
        THEN entrance_accessibility.accessibility 
        ELSE EXCLUDED.accessibility END,
    step_count = CASE WHEN entrance_accessibility.user_modified AND NOT {overwrite} 
        THEN entrance_accessibility.step_count 
        ELSE EXCLUDED.step_count END,
    step_height = CASE WHEN entrance_accessibility.user_modified AND NOT {overwrite} 
        THEN entrance_accessibility.step_height 
        ELSE EXCLUDED.step_height END,
    ramp = CASE WHEN entrance_accessibility.user_modified AND NOT {overwrite} 
        THEN entrance_accessibility.ramp 
        ELSE EXCLUDED.ramp END,
    lift = CASE WHEN entrance_accessibility.user_modified AND NOT {overwrite} 
        THEN entrance_accessibility.lift 
        ELSE EXCLUDED.lift END,
    entrance_width = CASE WHEN entrance_accessibility.user_modified AND NOT {overwrite} 
        THEN entrance_accessibility.entrance_width 
        ELSE EXCLUDED.entrance_width END,
    door_type = CASE WHEN entrance_accessibility.user_modified AND NOT {overwrite} 
        THEN entrance_accessibility.door_type 
        ELSE EXCLUDED.door_type END