# Автобусные билеты

Веб-приложение на Java (Spring Boot) для продажи автобусных билетов с управлением справочниками в PostgreSQL.

## Стек

- Java 21
- Spring Boot 3.3 (Web, JPA, Thymeleaf, Validation)
- PostgreSQL 16
- Flyway (миграции схемы и начальные данные)

## Модули

| Раздел | Описание |
|--------|----------|
| **Города** | CRUD справочника городов |
| **Маршруты** | CRUD маршрутов (откуда → куда, время, длительность) |
| **Цены** | CRUD тарифов на маршруты с периодом действия |
| **Билеты** | Продажа и отмена билетов |

## Быстрый запуск через Docker Compose

Для запуска приложения, PostgreSQL и pgAdmin одной командой:

```bash
docker compose up -d --build
```

- Приложение: http://localhost:8080
- pgAdmin: http://localhost:5050
- PostgreSQL: `localhost:5433`

Данные для входа в pgAdmin: `admin@example.com` / `admin`.
Для регистрации сервера PostgreSQL в pgAdmin используйте хост `postgres`, порт `5432`,
БД `bus_tickets`, пользователя `bustickets` и пароль `bustickets`.

Остановить сервисы:

```bash
docker compose down
```

Данные PostgreSQL и pgAdmin сохраняются в Docker volumes. Для локальной настройки
портов и учетных данных можно передать переменные `APP_PORT`, `POSTGRES_PORT`,
`POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `PGADMIN_PORT`,
`PGADMIN_EMAIL` и `PGADMIN_PASSWORD`.

## Запуск приложения из IDE

Поднимите только PostgreSQL:

```bash
docker compose up -d postgres
```

Затем запустите `com.bustickets.BusTicketsApplication`, задав URL базы данных:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/bus_tickets mvn spring-boot:run
```

## Структура БД

- `cities` — города
- `routes` — маршруты (FK на города)
- `prices` — цены (FK на маршруты)
- `tickets` — проданные билеты (FK на маршрут и цену)

Миграции и тестовые данные: `src/main/resources/db/migration/V1__init.sql`.
