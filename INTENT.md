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
- No SPA framework or separate frontend build — static HTML/CSS/vanilla JS only.
- No mobile app.

## Guiding principle

When extending this project, keep `StreakCalculator` pure (no JPA/web imports) and
keep ownership checks (`owner_id` match) in the service layer for every habit/log
operation — see `[[SPEC]]` for the exact contract and `[[PLAN]]` for current status.
