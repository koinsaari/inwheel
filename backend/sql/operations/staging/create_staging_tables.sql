-- Copyright © 2025 Aaro Koinsaari
-- Licensed under the Apache License, Version 2.0
-- http://www.apache.org/licenses/LICENSE-2.0

CREATE TEMP TABLE staging_places (
    osm_id BIGINT,
    name VARCHAR(255),
    category VARCHAR(50),
    lat DOUBLE PRECISION,
    lon DOUBLE PRECISION,
    region VARCHAR(50)
);

CREATE TEMP TABLE staging_ga (
    osm_id BIGINT,
    accessibility VARCHAR(20),
    indoor_accessibility VARCHAR(20),
    additional_info TEXT
);

CREATE TEMP TABLE staging_ea (
    osm_id BIGINT,
    accessibility VARCHAR(20),
    step_count VARCHAR(20),
    step_height VARCHAR(20),
    ramp VARCHAR(20),
    lift VARCHAR(20),
    entrance_width VARCHAR(20),
    door_type VARCHAR(50)
);

CREATE TEMP TABLE staging_ra (
    osm_id BIGINT,
    accessibility VARCHAR(20),
    door_width VARCHAR(20),
    room_maneuver VARCHAR(20),
    grab_rails VARCHAR(20),
    sink VARCHAR(20),
    toilet_seat VARCHAR(20),
    emergency_alarm VARCHAR(20),
    euro_key BOOLEAN
);

CREATE TEMP TABLE staging_contact (
    osm_id BIGINT,
    phone VARCHAR(100),
    website VARCHAR(255),
    email VARCHAR(255),
    address VARCHAR(255)
);