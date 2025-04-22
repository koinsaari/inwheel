-- Copyright © 2025 Aaro Koinsaari
-- Licensed under the Apache License, Version 2.0
-- http://www.apache.org/licenses/LICENSE-2.0

CREATE OR REPLACE FUNCTION places_in_bbox(
  min_lon float8, min_lat float8,
  max_lon float8, max_lat float8
) 
RETURNS JSONB
LANGUAGE plpgsql STABLE
SET search_path = public
AS
$$
DECLARE
  safe_min_lon float8;
  safe_min_lat float8;
  safe_max_lon float8;
  safe_max_lat float8;
  bbox_area float8;
  result_limit integer;
  result jsonb;
BEGIN
  safe_min_lon := GREATEST(-180, LEAST(180, min_lon));
  safe_min_lat := GREATEST(-90, LEAST(90, min_lat));
  safe_max_lon := GREATEST(-180, LEAST(180, max_lon));
  safe_max_lat := GREATEST(-90, LEAST(90, max_lat));
  
  -- Calculate the viewing area size in square degrees
  bbox_area := (safe_max_lon - safe_min_lon) * (safe_max_lat - safe_min_lat);
  
  -- Determine appropriate result limit based on area size
  IF bbox_area <= 25 THEN
    result_limit := 5000;
  ELSIF bbox_area <= 100 THEN
    result_limit := 2500;
  ELSE
    result_limit := 1000;
  END IF;
  

  SELECT jsonb_agg(
    jsonb_build_object(
      'id', p.id,
      'osm_id', p.osm_id,
      'name', p.name,
      'category', p.category,
      'lat', p.lat,
      'lon', p.lon,
      'last_osm_update', p.last_osm_update,
      'last_user_update', p.last_user_update,
      'created_at', p.created_at,
      'contact', to_jsonb(c) - 'place_id',
      'generalAccessibility', to_jsonb(g) - 'place_id',
      'entranceAccessibility', to_jsonb(e) - 'place_id',
      'restroomAccessibility', to_jsonb(r) - 'place_id'
    )
  )
  INTO result
  FROM public.places p
    LEFT JOIN public.contact c
      ON c.place_id = p.id
    LEFT JOIN public.general_accessibility g
      ON g.place_id = p.id
    LEFT JOIN public.entrance_accessibility e
      ON e.place_id = p.id
    LEFT JOIN public.restroom_accessibility r
      ON r.place_id = p.id
  WHERE ST_Intersects(
    p.geom,
    ST_MakeEnvelope(safe_min_lon, safe_min_lat, safe_max_lon, safe_max_lat, 4326)::geography
  )
  LIMIT result_limit;
  
  RETURN result;
END;
$$;

GRANT EXECUTE ON FUNCTION places_in_bbox(float8, float8, float8, float8) TO anon;