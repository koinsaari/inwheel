-- Copyright © 2025 Aaro Koinsaari
-- Licensed under the Apache License, Version 2.0
-- http://www.apache.org/licenses/LICENSE-2.0

INSERT INTO public.contact (place_id, phone, website, email, address)
SELECT p.id, sc.phone, sc.website, sc.email, sc.address
FROM staging_contact sc
JOIN public.places p ON p.osm_id = sc.osm_id
ON CONFLICT (place_id) DO UPDATE SET
    phone = EXCLUDED.phone,
    website = EXCLUDED.website,
    email = EXCLUDED.email,
    address = EXCLUDED.address