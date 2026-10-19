# Delivery Demo API

A Spring Boot REST API that calculates delivery fees based on the delivery **city**, the **vehicle type**, and the
current **weather conditions**.

The fee is composed of a **regional base fee** plus optional **weather-based extra fees** (air temperature, wind speed,
and weather phenomenon). Certain weather conditions can also make a vehicle type **forbidden** for delivery.

## Tech Stack

- **Java 25**
- **Spring Boot 4.1.1** (Web MVC, Data JPA, Flyway, Scheduling)
- **H2** file-based database
- **Flyway** for database migrations
- **springdoc-openapi** for Swagger UI / OpenAPI docs
- **Lombok**
- **Gradle** build

## Getting Started

### Prerequisites

- JDK 25
- No external database required (H2 is embedded)

### Run the application

```bash
./gradlew bootRun
```

The application starts on `http://localhost:8080`.

### Run the tests

```bash
./gradlew test
```

## Weather Data

Weather observations are fetched automatically from the Estonian weather service (`https://www.ilmateenistus.ee/`) on a
schedule (default: every hour at 15 minutes past)
and stored in the database. The following stations are tracked:

- `Tallinn-Harku` → Tallinn
- `Tartu-Tõravere` → Tartu
- `Pärnu` → Pärnu

## API Documentation

Interactive Swagger UI is available at:

```
http://localhost:8080/swagger
```

The raw OpenAPI document is available at:

```
http://localhost:8080/v3/api-docs
```

## Configuration

Key settings in `src/main/resources/application.yaml`:

| Property                | Description                                     |
|-------------------------|-------------------------------------------------|
| `spring.datasource.url` | H2 database location (`jdbc:h2:file:./db/demo`) |
| `weather.api.base-url`  | Weather API base URL                            |
| `weather.api.endpoint`  | Weather observations endpoint                   |
| `weather.stations`      | Comma-separated list of tracked stations        |
| `weather.schedule`      | Cron expression for the weather fetch job       |
