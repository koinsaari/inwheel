-- Copyright © 2025 Aaro Koinsaari
-- Licensed under the Apache License, Version 2.0
-- http://www.apache.org/licenses/LICENSE-2.0

CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TYPE ACCESSIBILITY_STATUS AS ENUM ('FULLY_ACCESSIBLE', 'PARTIALLY_ACCESSIBLE', 'NOT_ACCESSIBLE');

CREATE TABLE public.places (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL,
  osm_id BIGINT UNIQUE NOT NULL,
  name VARCHAR(255),
  category VARCHAR(50) NOT NULL,
  lat DOUBLE PRECISION NOT NULL,
  lon DOUBLE PRECISION NOT NULL,
  geom GEOGRAPHY(Point, 4326),
  region VARCHAR(50) NOT NULL,
  last_osm_update TIMESTAMP WITH TIME ZONE DEFAULT now(),
  last_user_update TIMESTAMP WITH TIME ZONE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE public.general_accessibility (
  place_id UUID PRIMARY KEY REFERENCES public.places(id) ON DELETE CASCADE,
  accessibility ACCESSIBILITY_STATUS,
  indoor_accessibility ACCESSIBILITY_STATUS,
  additional_info TEXT CHECK (char_length(additional_info) <= 1000),
  user_modified BOOLEAN DEFAULT FALSE
);

CREATE TABLE public.entrance_accessibility (
  place_id UUID PRIMARY KEY REFERENCES public.places(id) ON DELETE CASCADE,
  accessibility ACCESSIBILITY_STATUS,
  step_count ACCESSIBILITY_STATUS,
  step_height ACCESSIBILITY_STATUS,
  ramp ACCESSIBILITY_STATUS,
  lift ACCESSIBILITY_STATUS,
  entrance_width ACCESSIBILITY_STATUS,
  door_type VARCHAR(50),
  user_modified BOOLEAN DEFAULT FALSE
);

CREATE TABLE public.restroom_accessibility (
  place_id UUID PRIMARY KEY REFERENCES public.places(id) ON DELETE CASCADE,
  accessibility ACCESSIBILITY_STATUS,
  door_width ACCESSIBILITY_STATUS,
  room_maneuver ACCESSIBILITY_STATUS,
  grab_rails ACCESSIBILITY_STATUS,
  sink ACCESSIBILITY_STATUS,
  toilet_seat ACCESSIBILITY_STATUS,
  emergency_alarm ACCESSIBILITY_STATUS,
  euro_key BOOLEAN,
  user_modified BOOLEAN DEFAULT FALSE
);

CREATE TABLE public.contact (
  place_id UUID PRIMARY KEY REFERENCES public.places(id) ON DELETE CASCADE,
  phone VARCHAR(100),
  website VARCHAR(255),
  email VARCHAR(255),
  address VARCHAR(255)
);

CREATE INDEX idx_places_geom ON public.places USING GIST (geom);
CREATE INDEX idx_places_osm_id ON public.places (osm_id);
CREATE INDEX idx_places_category ON public.places (category);
CREATE INDEX idx_places_region ON public.places (region);