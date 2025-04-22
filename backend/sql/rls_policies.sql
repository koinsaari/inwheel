-- Copyright © 2025 Aaro Koinsaari
-- Licensed under the Apache License, Version 2.0
-- http://www.apache.org/licenses/LICENSE-2.0

ALTER TABLE public.places ENABLE ROW LEVEL SECURITY;
CREATE POLICY select_places ON public.places FOR SELECT USING (true);
CREATE POLICY update_places ON public.places FOR UPDATE 
  USING (current_setting('app.is_service', true) = 'true')
  WITH CHECK (current_setting('app.is_service', true) = 'true');

ALTER TABLE public.contact ENABLE ROW LEVEL SECURITY;
CREATE POLICY select_contact ON public.contact FOR SELECT USING (true);
CREATE POLICY update_contact ON public.contact FOR ALL 
  USING (current_setting('app.is_service', true) = 'true')
  WITH CHECK (current_setting('app.is_service', true) = 'true');

ALTER TABLE public.general_accessibility ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.entrance_accessibility ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.restroom_accessibility ENABLE ROW LEVEL SECURITY;

CREATE POLICY select_accessibility ON public.general_accessibility FOR SELECT USING (true);
CREATE POLICY select_accessibility ON public.entrance_accessibility FOR SELECT USING (true);
CREATE POLICY select_accessibility ON public.restroom_accessibility FOR SELECT USING (true);

CREATE POLICY update_general_accessibility ON public.general_accessibility FOR ALL
  USING (true)
  WITH CHECK (true);
  
CREATE POLICY update_entrance_accessibility ON public.entrance_accessibility FOR ALL
  USING (true)
  WITH CHECK (true);
  
CREATE POLICY update_restroom_accessibility ON public.restroom_accessibility FOR ALL
  USING (true)
  WITH CHECK (true);