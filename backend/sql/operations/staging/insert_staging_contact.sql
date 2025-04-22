-- Copyright © 2025 Aaro Koinsaari
-- Licensed under the Apache License, Version 2.0
-- http://www.apache.org/licenses/LICENSE-2.0

INSERT INTO staging_contact (osm_id, phone, website, email, address)
VALUES (%s, %s, %s, %s, %s)