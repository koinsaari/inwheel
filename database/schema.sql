CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE public.places (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL,
  osm_id BIGINT UNIQUE NOT NULL,
  name TEXT,
  category TEXT NOT NULL,
  lat DOUBLE PRECISION NOT NULL,
  lon DOUBLE PRECISION NOT NULL,
  geom GEOGRAPHY(Point, 4326),
  contact JSONB,
  accessibility_osm JSONB,
  accessibility_user JSONB,
  last_osm_update TIMESTAMP WITH TIME ZONE,
  last_user_update TIMESTAMP WITH TIME ZONE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE INDEX idx_places_geom ON places USING GIST (geom);
CREATE INDEX idx_places_osm_id ON places (osm_id);
CREATE INDEX idx_places_contact ON places USING GIN (contact);
CREATE INDEX idx_places_accessibility_osm ON places USING GIN (accessibility_osm);
CREATE INDEX idx_places_accessibility_user ON places USING GIN (accessibility_user);