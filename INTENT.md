# Intent

## What this is

A minimal, self-hosted habit tracker: multiple users sign up, each manages their own
list of habits, and marks per-day completion. The app computes current and longest
streaks from that history.

## Why it exists

A single-binary alternative to SaaS habit trackers — no separate frontend deploy, no
third-party accounts beyond hosting. One Spring Boot app serves the API and the UI,
backed by Postgres.

## Goals

- Correct streak math (current + longest), independent of web/persistence concerns.
- Per-user data isolation enforced at the service layer, not just the UI.
- Deployable for free on a single small instance (Render free tier + free Postgres).
- Small enough surface area that one person can hold the whole system in their head.

## Non-goals

- No multi-tenant/team features, no habit sharing, no notifications/reminders.
- No mobile app, no separate frontend service — the React frontend is built by
  Gradle (via the node-gradle plugin) and still ships embedded in the same
  Spring Boot jar, so the one-deployable-unit goal above is unchanged.
- No pagination or caching layer — list/stats endpoints assume a single user's
  data is small enough to aggregate in memory on each request.

## Guiding principle

When extending this project, keep `StreakCalculator` pure (no JPA/web imports) and
keep ownership checks (`owner_id` match) in the service layer for every habit/log
operation — see `[[SPEC]]` for the exact contract and `[[PLAN]]` for current status.
