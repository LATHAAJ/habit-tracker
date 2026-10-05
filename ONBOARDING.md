# Onboarding: Habit Tracker

A habit tracker with streak math, custom frequency (daily or Nx/week), categories,
trend stats, and a React UI — all served as **one deployable unit** by a single
Spring Boot application. No separate frontend host, no API gateway.

```
Browser → Spring Boot (Tomcat embedded) → serves React static build at "/"
                                        → serves JSON API at "/api/**"
                                        → Postgres (prod) / H2 (tests)
```

Read `INTENT.md` for the why, `SPEC.md` for the exact functional contract (kept in
sync with the code), `PLAN.md` for status (currently a bit stale — this doc is the
more current picture as of the React/stats/categories rewrite).

## Stack

- **Backend**: Java 17, Spring Boot 3 (Web, Security, Data JPA, Validation), `jjwt` for JWT, BCrypt for passwords.
- **Frontend**: React + Vite, **no router** (one `tab` state switches views — deliberate, avoids needing Spring-side SPA-fallback routing), vanilla CSS with custom properties for theming (no Tailwind/CSS-in-JS).
- **DB**: Postgres in prod/dev, H2 in-memory for tests. **No Flyway/Liquibase** — schema is Hibernate `ddl-auto: update`. This is a real constraint: every new column must be nullable, since there's no migration tool to backfill a populated table.
- **Build**: Gradle builds the frontend too. The `com.github.node-gradle.node` plugin wires `npm install && npm run build` into `processResources`, so `./gradlew bootRun`/`bootJar`/`build` transparently produce a working app with zero manual npm step. The Vite output lands in `build/generated-resources/static`, which is added as an extra resources source dir.
- **Deploy**: 2-stage Dockerfile (Alpine JDK build stage → slim JRE Alpine runtime), `render.yaml` Blueprint provisions the web service + a free Postgres together on Render.

## Running locally

```bash
docker compose up -d     # local Postgres on localhost:5432
./gradlew bootRun        # http://localhost:8080 — builds frontend automatically
./gradlew test           # StreakCalculatorTest + HabitControllerIntegrationTest + StatsControllerIntegrationTest
```

## Backend package layout (`src/main/java/com/habittracker/`)

| Package | Responsibility |
|---|---|
| `auth` | Signup/login, JWT issue/validate, `JwtAuthFilter` |
| `user` | `User` entity: id, email (unique), passwordHash, name (nullable), createdAt |
| `habit` | `Habit` entity + CRUD, category (free string), frequency |
| `log` | `HabitLog` — one row per `(habit_id, date)`, unique constraint |
| `streak` | `StreakCalculator` — **pure static class, zero Spring/JPA imports, by design**. Keep it that way. |
| `stats` | Trend + best/worst-habit aggregation, read-only, no pagination/caching (small-scale app, deliberate) |
| `config` | Security filter chain, `GlobalExceptionHandler` (one `@ExceptionHandler` per custom exception → HTTP status) |

**Auth model**: stateless JWT. `JwtAuthFilter` decodes the token once per request and sets the loaded `User` entity **directly** as the Spring Security principal — no `UserDetails` wrapper, no roles (every authenticated user has identical permissions; there is no admin/role concept in this app). Controllers inject the caller via `@AuthenticationPrincipal User owner`.

**Ownership enforcement**: every habit lookup is scoped by `owner_id` at the repository level (`findByIdAndOwnerId`). A habit you don't own returns `404`, not `403` — so you can't even detect it exists. Enforced in the service layer, not just filtered in the UI — follow this pattern for anything new.

**Streak math** (`StreakCalculator`): input is a `Set<LocalDate>` + "today" (+ frequency + target for the weekly overload).
- `DAILY`: counts backward from today; if today isn't marked but yesterday was, the streak is still "alive" (today isn't over yet).
- `WEEKLY`: buckets completions by Monday-start week; a week "counts" if it hit `targetPerPeriod`; same alive-rule at week granularity.

## API surface

See `SPEC.md` for the full contract. Summary:

| Area | Endpoints |
|---|---|
| Auth | `POST /api/auth/signup` `{email,password,name}`, `POST /api/auth/login` → both return `{token, email, name}` |
| Habits | `GET/POST /api/habits` (optional `?category=`), `PUT/DELETE /api/habits/{id}`, `POST /api/habits/{id}/toggle?date=`, `GET /api/habits/{id}/logs?from=&to=` |
| Stats | `GET /api/stats/trend?granularity=WEEK\|MONTH&periods=`, `GET /api/stats/habits?from=&to=` |
| Health | `GET /api/health` (unauthenticated) |

A ready-to-import **Postman collection + environment** live in `postman/` — imports clean, auto-saves the JWT from signup/login into a collection variable so every other request just works.

## Frontend (`frontend/src/`)

- `App.jsx` — top-level `tab` state (`'dashboard' | 'stats'`), no React Router.
- `AuthContext.jsx` + `api.js` — the only "state management": a `fetch` wrapper that attaches the Bearer token and clears session on `401`, plus `localStorage`-backed auth state. No Redux/Zustand.
- `categories.js` — the fixed 6 categories (Health/Learning/Career/Mind/Personal/Finance), each mapped to one of the app's categorical hues, used consistently across tags/chips/stats.
- `theme.js` + `ThemeToggle.jsx` — light/dark via CSS custom properties, overridable via `data-theme` attribute, persisted to `localStorage`.
- `components/HabitDetailModal.jsx` — click a habit tile for a week-by-week consistency breakdown + rating. **Known fragile spot**: it's rendered as a plain nested `position: fixed` div, not via a React portal. If `.habit-card:hover`'s `transform` is active at click time, that transform makes the card the containing block for the fixed modal (CSS spec behavior), which can misposition/flicker it. Worth fixing via `createPortal(..., document.body)` if it recurs.

## Testing conventions

`@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")`, real MockMvc + H2 (`create-drop`), AssertJ assertions, a private `signup(email)` helper per test class, `UUID.randomUUID()` for unique emails. No Testcontainers, no shared fixtures.

## Things that were tried and reverted

An AI-powered "generate a habit plan from a goal" feature (via Google Gemini's free tier) was built, then fully reverted at the user's request — see commits `68e1a8e`/`bb64aa5` if you're curious, but there's no trace of it in the current code or config.
