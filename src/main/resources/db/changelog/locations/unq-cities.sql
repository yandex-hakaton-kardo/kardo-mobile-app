ALTER TABLE cities
ADD CONSTRAINT unq_cities UNIQUE NULLS NOT DISTINCT (name, country_id, region_id);