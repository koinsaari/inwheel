CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE public.places (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL,
  osm_id BIGINT UNIQUE NOT NULL,
  name TEXT NOT NULL,
  lat DOUBLE PRECISION NOT NULL,
  lon DOUBLE PRECISION NOT NULL,
  geom GEOGRAPHY(Point, 4326),
  accessibility JSONB,
  last_osm_update TIMESTAMP WITH TIME ZONE,
  last_modified TIMESTAMP WITH TIME ZONE, -- latest user modification
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE INDEX idx_places_geom ON places USING GIST (geom);
CREATE INDEX idx_places_osm_id ON places (osm_id);
CREATE INDEX idx_places_accessibility ON places USING GIN (accessibility);

CREATE TABLE public.reviews (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL,
  place_id UUID REFERENCES places(id) ON DELETE CASCADE NOT NULL,
  user_id UUID REFERENCES auth.users ON DELETE CASCADE NOT NULL,
  changes JSONB NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  approved BOOLEAN DEFAULT false
);