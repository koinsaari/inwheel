-- Copyright © 2025 Aaro Koinsaari
-- Licensed under the Apache License, Version 2.0
-- http://www.apache.org/licenses/LICENSE-2.0

INSERT INTO public.general_accessibility (place_id, accessibility, indoor_accessibility, additional_info)
SELECT 
    p.id, 
    sg.accessibility::ACCESSIBILITY_STATUS,
    sg.indoor_accessibility::ACCESSIBILITY_STATUS,
    sg.additional_info
FROM staging_ga sg
JOIN public.places p ON p.osm_id = sg.osm_id
ON CONFLICT (place_id) DO UPDATE SET
    accessibility = CASE WHEN general_accessibility.user_modified AND NOT {overwrite} 
        THEN general_accessibility.accessibility 
        ELSE EXCLUDED.accessibility END,
    indoor_accessibility = CASE WHEN general_accessibility.user_modified AND NOT {overwrite} 
        THEN general_accessibility.indoor_accessibility 
        ELSE EXCLUDED.indoor_accessibility END,
    additional_info = CASE WHEN general_accessibility.user_modified AND NOT {overwrite} 
        THEN general_accessibility.additional_info 
        ELSE EXCLUDED.additional_info END