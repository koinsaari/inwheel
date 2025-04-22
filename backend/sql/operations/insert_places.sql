-- Copyright © 2025 Aaro Koinsaari
-- Licensed under the Apache License, Version 2.0
-- http://www.apache.org/licenses/LICENSE-2.0

INSERT INTO public.places (osm_id, name, category, lat, lon, geom, region, last_osm_update)
SELECT sp.osm_id, sp.name, sp.category, sp.lat, sp.lon, 
        ST_SetSRID(ST_MakePoint(sp.lon, sp.lat), 4326), sp.region, now()
FROM staging_places sp
ON CONFLICT (osm_id) DO UPDATE SET
    name = EXCLUDED.name,
    category = EXCLUDED.category,
    lat = EXCLUDED.lat,
    lon = EXCLUDED.lon,
    geom = EXCLUDED.geom,
    region = EXCLUDED.region,
    last_osm_update = now()