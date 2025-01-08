ALTER TABLE public.places ENABLE ROW LEVEL SECURITY;
CREATE POLICY select_places ON public.places
  FOR SELECT
  USING (true);