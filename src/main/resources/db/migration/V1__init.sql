-- Cities reference
CREATE TABLE cities (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    region      VARCHAR(100)
);

-- Routes between cities
CREATE TABLE routes (
    id              BIGSERIAL PRIMARY KEY,
    departure_city_id BIGINT NOT NULL REFERENCES cities(id),
    arrival_city_id   BIGINT NOT NULL REFERENCES cities(id),
    departure_time    TIME NOT NULL,
    duration_minutes  INTEGER NOT NULL CHECK (duration_minutes > 0),
    CONSTRAINT routes_different_cities CHECK (departure_city_id <> arrival_city_id)
);

CREATE INDEX idx_routes_departure ON routes(departure_city_id);
CREATE INDEX idx_routes_arrival ON routes(arrival_city_id);

-- Prices per route
CREATE TABLE prices (
    id          BIGSERIAL PRIMARY KEY,
    route_id    BIGINT NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
    amount      NUMERIC(10, 2) NOT NULL CHECK (amount > 0),
    valid_from  DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to    DATE,
    CONSTRAINT prices_valid_period CHECK (valid_to IS NULL OR valid_to >= valid_from)
);

CREATE INDEX idx_prices_route ON prices(route_id);

-- Sold tickets
CREATE TABLE tickets (
    id              BIGSERIAL PRIMARY KEY,
    route_id        BIGINT NOT NULL REFERENCES routes(id),
    price_id        BIGINT NOT NULL REFERENCES prices(id),
    passenger_name  VARCHAR(150) NOT NULL,
    seat_number     INTEGER NOT NULL CHECK (seat_number > 0),
    travel_date     DATE NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    sold_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT tickets_status_check CHECK (status IN ('ACTIVE', 'CANCELLED'))
);

CREATE INDEX idx_tickets_route ON tickets(route_id);
CREATE INDEX idx_tickets_travel_date ON tickets(travel_date);

-- Seed data
INSERT INTO cities (name, region) VALUES
    ('Москва', 'Москва'),
    ('Санкт-Петербург', 'Ленинградская область'),
    ('Казань', 'Татарстан'),
    ('Нижний Новгород', 'Нижегородская область'),
    ('Самара', 'Самарская область');

INSERT INTO routes (departure_city_id, arrival_city_id, departure_time, duration_minutes) VALUES
    (1, 2, '08:00', 720),
    (1, 2, '20:00', 720),
    (1, 3, '09:30', 660),
    (2, 1, '07:00', 720),
    (1, 4, '10:00', 240),
    (4, 5, '14:00', 360);

INSERT INTO prices (route_id, amount, valid_from) VALUES
    (1, 2500.00, '2026-01-01'),
    (2, 2200.00, '2026-01-01'),
    (3, 1800.00, '2026-01-01'),
    (4, 2500.00, '2026-01-01'),
    (5, 900.00, '2026-01-01'),
    (6, 1200.00, '2026-01-01');
