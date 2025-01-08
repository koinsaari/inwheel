CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE public.places (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL,
  osm_id BIGINT UNIQUE NOT NULL,
  name TEXT,
  category TEXT NOT NULL,
  lat DOUBLE PRECISION NOT NULL,
  lon DOUBLE PRECISION NOT NULL,
  geom GEOGRAPHY(Point, 4326),
  accessibility JSONB,
  last_osm_update TIMESTAMP WITH TIME ZONE,
  last_user_update TIMESTAMP WITH TIME ZONE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE INDEX idx_places_geom ON places USING GIST (geom);
CREATE INDEX idx_places_osm_id ON places (osm_id);
CREATE INDEX idx_places_accessibility ON places USING GIN (accessibility);