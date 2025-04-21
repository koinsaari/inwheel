ALTER TABLE public.places ENABLE ROW LEVEL SECURITY;
CREATE POLICY select_places ON public.places
  FOR SELECT
  USING (true);

ALTER TABLE public.contact ENABLE ROW LEVEL SECURITY;
CREATE POLICY select_contact ON public.contact
  FOR SELECT
  USING (true);

ALTER TABLE public.general_accessibility ENABLE ROW LEVEL SECURITY;
CREATE POLICY select_general_accessibility ON public.general_accessibility
  FOR SELECT
  USING (true);
CREATE POLICY update_general_accessibility ON public.general_accessibility
  FOR UPDATE
  USING (true)
  WITH CHECK (true);

ALTER TABLE public.entrance_accessibility ENABLE ROW LEVEL SECURITY;
CREATE POLICY select_entrance_accessibility ON public.entrance_accessibility
  FOR SELECT
  USING (true);
CREATE POLICY update_entrance_accessibility ON public.entrance_accessibility
  FOR UPDATE
  USING (true)
  WITH CHECK (true);

ALTER TABLE public.restroom_accessibility ENABLE ROW LEVEL SECURITY;
CREATE POLICY select_restroom_accessibility ON public.restroom_accessibility
  FOR SELECT
  USING (true);
CREATE POLICY update_restroom_accessibility ON public.restroom_accessibility
  FOR UPDATE
  USING (true)
  WITH CHECK (true);