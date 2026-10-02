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
| `/` | GET | List the caller's habits with streak info |
| `/` | POST | Create a habit: `{ name, description? }` |
| `/{id}` | PUT | Update name/description of a habit the caller owns |
| `/{id}` | DELETE | Delete a habit the caller owns |
| `/{id}/toggle` | POST | Toggle completion for `?date=YYYY-MM-DD` (default: today) |
| `/{id}/logs` | GET | Completed dates in `[from, to]`, both required, ISO dates |

- Every operation on a habit ID must verify `habit.owner == caller` — a habit
  belonging to another user is treated as not found (404), not forbidden (403).
- Toggling a date twice removes the completion (idempotent on/off).

## Streak calculation (`StreakCalculator`, pure function)

Input: a `Set<LocalDate>` of completed dates + "today". Output: `(currentStreak,
longestStreak)`.

- `longestStreak`: length of the longest run of consecutive calendar dates in the set.
- `currentStreak`: counts backward from today. If today is not yet completed but
  yesterday was, the streak is still "alive" (today isn't over) and counts from
  yesterday. If neither today nor yesterday is completed, current streak is 0.
- Empty input → `(0, 0)`.

## Data model

- `User(id, email unique, passwordHash, createdAt)`
- `Habit(id, name, description?, active, createdAt, owner -> User)`
- `HabitLog(id, habit -> Habit, date, completedAt)` — unique on `(habit_id, date)`.

## Persistence

- Postgres in production/dev (`docker-compose.yml` for local).
- H2 in-memory for tests only (`spring-boot-starter-test` profile).

## Out of scope (see `[[INTENT]]`)

Password reset, email verification, habit sharing, reminders/notifications.
