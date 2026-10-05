# Habit Tracker

A habit tracker with per-day completion tracking, streak math (current + longest streak,
daily or custom weekly frequency), categories, weekly/monthly stats, multi-user accounts
(signup/login via JWT), and a React web UI — all served by a single Spring Boot application.

## Stack

- Java 17, Spring Boot 3 (Web, Security, Data JPA, Validation)
- PostgreSQL (production), H2 in-memory (tests only)
- JWT auth (`jjwt`), BCrypt password hashing
- React + Vite frontend (`frontend/`), built by Gradle (`com.github.node-gradle.node`
  plugin) into the Spring Boot jar's static resources — still one deployable unit, no
  separate frontend server/deploy, no manual `npm` step for `./gradlew bootRun`/`bootJar`
- Gradle, multi-stage Dockerfile

## Running locally

Requires Docker (for a local Postgres) and a JDK 17+.

```bash
docker compose up -d          # starts a local Postgres on localhost:5432
./gradlew bootRun             # starts the app on http://localhost:8080
```

Open http://localhost:8080, sign up, and start tracking habits.

To run against a different local Postgres, override the datasource properties, e.g.:

```bash
./gradlew bootRun \
  --args="--spring.datasource.url=jdbc:postgresql://localhost:5432/habit_tracker --spring.datasource.username=habit --spring.datasource.password=habit"
```

## Running tests

```bash
./gradlew test
```

- `StreakCalculatorTest` — unit tests for daily and weekly-frequency streak math (no
  completions, consecutive runs, broken streaks, "today/this week not yet marked but
  the prior period was", month/year boundaries).
- `JwtServiceTest` — token generation/validation, tampering, expiry.
- `HabitControllerIntegrationTest` — full HTTP + security + JPA stack via MockMvc and an
  in-memory H2 database (`test` profile): signup → login → create → toggle → streak reflected
  in the list, category filtering, weekly-frequency validation, ownership isolation between
  users.
- `StatsControllerIntegrationTest` — trend and best/worst-habit ranking endpoints, invalid
  range handling, per-user scoping.

## API

All `/api/habits/**` and `/api/stats/**` routes require `Authorization: Bearer <token>`
from signup/login.

| Method | Path                          | Description                                  |
|--------|-------------------------------|-----------------------------------------------|
| POST   | `/api/auth/signup`            | Create an account, returns a JWT             |
| POST   | `/api/auth/login`              | Log in, returns a JWT                        |
| GET    | `/api/habits?category=`        | List your habits with streak info, optionally filtered by category |
| POST   | `/api/habits`                  | Create a habit `{name, description?, category?, frequencyType?, targetPerPeriod?}` |
| PUT    | `/api/habits/{id}`             | Update a habit                               |
| DELETE | `/api/habits/{id}`             | Delete a habit (and its history)             |
| POST   | `/api/habits/{id}/toggle?date=`| Toggle completion for a date (default today) |
| GET    | `/api/habits/{id}/logs?from=&to=` | Completed dates in a range (ISO strings) |
| GET    | `/api/stats/trend?granularity=&periods=` | Weekly/monthly completion-rate trend |
| GET    | `/api/stats/habits?from=&to=`  | Habits ranked by completion rate, best first |
| GET    | `/api/health`                  | Unauthenticated health check                 |

## Deploying to Render

This repo includes a `render.yaml` Blueprint that provisions a free web service (built from the
`Dockerfile`) and a free Postgres database, wired together automatically.

1. Push this repository to GitHub.
2. In the Render dashboard: **New** → **Blueprint**, and point it at the repo. Render reads
   `render.yaml` and creates both the web service and the database.
3. Render auto-generates `JWT_SECRET` and wires the database's host/port/credentials into the
   web service's environment — no manual configuration needed.
4. Once deployed, open the service's public URL — the UI and API are both served from it.

### Deploying manually (without Blueprints)

1. Create a Postgres instance on Render (free tier), note its host/port/database/user/password.
2. Create a Web Service from this repo with **Docker** as the runtime.
3. Set these environment variables on the web service:
   - `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` — from the Postgres instance
   - `JWT_SECRET` — any long random string (used to sign tokens; keep it secret)
4. Deploy. Render sets `PORT` automatically; the app reads it via `server.port=${PORT:8080}`.
