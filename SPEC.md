# Spec

Functional contract for the current implementation. Update this file when behavior
changes; it should always match `src/main/java` exactly.

## Auth (`/api/auth`)

| Endpoint | Method | Body | Response | Notes |
|---|---|---|---|---|
| `/signup` | POST | `{ email, password }` | `201` + `{ token, email }` | Fails if email already in use |
| `/login` | POST | `{ email, password }` | `200` + `{ token, email }` | Fails on bad credentials |

- Passwords are BCrypt-hashed, never stored or returned in plaintext.
- `token` is a JWT; `JWT_EXPIRATION_DAYS` env var controls lifetime (default 7).
- All `/api/habits/**` endpoints require `Authorization: Bearer <token>`.

## Habits (`/api/habits`, all require auth)

| Endpoint | Method | Notes |
|---|---|---|
| `/` | GET | List the caller's habits with streak info. Optional `?category=` filters by exact category match |
| `/` | POST | Create a habit: `{ name, description?, active?, category?, frequencyType?, targetPerPeriod? }` |
| `/{id}` | PUT | Update a habit the caller owns (full overwrite, same body as POST) |
| `/{id}` | DELETE | Delete a habit the caller owns |
| `/{id}/toggle` | POST | Toggle completion for `?date=YYYY-MM-DD` (default: today) |
| `/{id}/logs` | GET | Completed dates in `[from, to]`, both required, ISO dates |

- Every operation on a habit ID must verify `habit.owner == caller` — a habit
  belonging to another user is treated as not found (404), not forbidden (403).
- Toggling a date twice removes the completion (idempotent on/off).
- `category` is a free-text label (max 60 chars), optional, no fixed enum.
- `frequencyType` is `DAILY` (default when omitted) or `WEEKLY`. When `WEEKLY`,
  `targetPerPeriod` must be `1..7` (times per week) or the request is rejected
  with `400`; when `DAILY`, `targetPerPeriod` is ignored and reported as `1`.

## Stats (`/api/stats`, all require auth)

| Endpoint | Method | Notes |
|---|---|---|
| `/trend` | GET | `?granularity=WEEK\|MONTH` (default `WEEK`), `?periods=` (default `12`, must be `1..52`). Returns oldest-first completion-rate points across all the caller's active habits |
| `/habits` | GET | `?from=&to=` (both required, ISO dates). Returns every habit ranked by completion rate, best first |

- `possibleCount` in a trend point = active habit count × days in that period
  (a habit created mid-period isn't excluded from the denominator — accepted
  simplification for a personal-scale app).
- In the habit ranking, `expectedCount` accounts for frequency: `DAILY` expects
  one completion per day in range; `WEEKLY` expects `ceil(days / 7) * targetPerPeriod`,
  so habits of different frequencies compare fairly.
- Invalid `granularity`, out-of-range `periods`, or `from > to` → `400`.

## AI habit plan (`/api/ai`, requires auth)

| Endpoint | Method | Body | Notes |
|---|---|---|---|
| `/habit-plan` | POST | `{ goal }` (1..300 chars) | Returns `{ goalSummary, habits: [{name, description?, category, frequencyType, targetPerPeriod?}] }` |

- Calls Google's Gemini API (`GEMINI_API_KEY` env var; model configurable via
  `GEMINI_MODEL`, default `gemini-2.0-flash-lite`) — not Anthropic — chosen specifically
  for its no-cost free tier.
- `category` in the response is one of `HEALTH|LEARNING|CAREER|MIND|PERSONAL|FINANCE`
  (maps to the frontend's fixed category list).
- Returns `503` if `GEMINI_API_KEY` is unset; `502` if the upstream call fails or
  returns an unusable response.
- This endpoint only *suggests* habits — nothing is persisted. The client creates
  habits the user selects via the normal `POST /api/habits`.

## Streak calculation (`StreakCalculator`, pure function)

Input: a `Set<LocalDate>` of completed dates + "today" (+ frequency + target for
the frequency-aware overload). Output: `(currentStreak, longestStreak)`.

**DAILY** (default):
- `longestStreak`: length of the longest run of consecutive calendar dates in the set.
- `currentStreak`: counts backward from today. If today is not yet completed but
  yesterday was, the streak is still "alive" (today isn't over) and counts from
  yesterday. If neither today nor yesterday is completed, current streak is 0.
- Empty input → `(0, 0)`.

**WEEKLY** (habit's `targetPerPeriod` completions per calendar week, bucketed
Monday-start):
- A week "counts" if it has at least `targetPerPeriod` completions in it.
- `longestStreak`: longest run of consecutive counting weeks.
- `currentStreak`: counts backward from the current week if it already meets
  target; otherwise from last week if last week met target (this week isn't
  over yet, same "alive" idea as the daily rule); otherwise 0.
- Empty input → `(0, 0)`.

## Data model

- `User(id, email unique, passwordHash, createdAt)`
- `Habit(id, name, description?, active, createdAt, owner -> User, category?,
  frequencyType?, targetPerPeriod?)` — `category`/`frequencyType`/`targetPerPeriod`
  are nullable columns; `null` frequencyType means `DAILY`, `null` targetPerPeriod
  means `1`.
- `HabitLog(id, habit -> Habit, date, completedAt)` — unique on `(habit_id, date)`.

## Persistence

- Postgres in production/dev (`docker-compose.yml` for local).
- H2 in-memory for tests only (`spring-boot-starter-test` profile).

## Out of scope (see `[[INTENT]]`)

Password reset, email verification, habit sharing, reminders/notifications.
