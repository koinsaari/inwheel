CREATE OR REPLACE FUNCTION places_in_bbox(
  min_lon float8, min_lat float8,
  max_lon float8, max_lat float8
) 
RETURNS JSONB
LANGUAGE sql STABLE AS
$$
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
    ST_MakeEnvelope(min_lon, min_lat, max_lon, max_lat, 4326)::geography
  );
$$;

GRANT EXECUTE ON FUNCTION places_in_bbox(float8, float8, float8, float8) TO anon;