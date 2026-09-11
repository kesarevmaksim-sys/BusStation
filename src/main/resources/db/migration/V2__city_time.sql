ALTER TABLE cities
    ADD COLUMN timezone_id VARCHAR(100),
    ADD COLUMN timezone_abbreviation VARCHAR(10),
    ADD COLUMN utc_offset VARCHAR(6),
    ADD COLUMN city_current_time TIMESTAMP WITH TIME ZONE,
    ADD COLUMN time_updated_at TIMESTAMP WITH TIME ZONE;

UPDATE cities SET timezone_id = 'Europe/Moscow'
WHERE name IN ('Москва', 'Санкт-Петербург', 'Казань', 'Нижний Новгород');

UPDATE cities SET timezone_id = 'Europe/Samara'
WHERE name = 'Самара';
