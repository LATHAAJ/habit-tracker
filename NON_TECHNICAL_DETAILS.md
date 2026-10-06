# How the Habit Tracker Works (No Tech Jargon)

This explains what actually happens behind the scenes when you use the app —
written so you could explain it to a friend without needing to know any coding.

## Signing up

When you create an account, three things happen:

1. The app double-checks your email isn't already taken.
2. Your password gets **scrambled into an unreadable code** before it's saved —
   nobody, not even someone looking directly at the database, can see your
   actual password. Only a scrambled version is ever stored. When you log in
   later, the app scrambles what you type the same way and checks if the
   scrambled versions match — it never needs to "unscramble" anything.
3. You're handed a **digital wristband** (more on this below) so you're
   automatically logged in right after signing up — no separate login step.

## Logging in

Same idea as a festival or concert check-in:

- You show your ID (email + password).
- If it checks out, you get a **wristband** — in tech terms this is called a
  "token." It's a piece of text with an invisible stamp on it that only the
  app knows how to verify.
- From then on, every time you do anything in the app (see your habits, mark
  one done, edit something), your browser just shows this wristband instead
  of asking for your password again. The app glances at the stamp, confirms
  it's genuine, and lets you through — instantly.
- The wristband **expires after 7 days**. After that you'd need to log in
  again to get a new one.
- Here's the interesting part: the app doesn't keep a guest list anywhere. It
  doesn't write down "this wristband belongs to you" in a database. It just
  trusts any wristband with the right stamp. "Logging out" simply means you
  stop showing the wristband — the app didn't actually invalidate it, you just
  put it away.

## Adding a new habit

You type a name (like "Exercise"), optionally add a description, pick a
category (Health, Learning, Career, Mind, Personal, or Finance), and choose
how often you want to do it — every day, or a certain number of times per
week (like "Gym, 3x a week").

The app saves this as brand new, with zero history — so it always starts at
"0 current streak, 0 best streak." There's nothing to calculate yet because
you haven't marked it done even once.

## Marking a habit done

Click the button, and the app flips a switch for "did I do this today?" —
literally a toggle: click once to mark it done, click again to undo it if you
clicked by mistake.

Every time you do this, the app **recalculates your streak from scratch** by
looking at your *entire* history of completed days for that habit, not just
today. So your "current streak" and "best streak" numbers are always freshly
computed, not just a running counter that could drift out of sync.

The streak logic itself has a small kindness built in: if you haven't marked
today done yet, but you *did* do it yesterday, your streak doesn't reset to
zero immediately — it stays "alive" until the day is actually over. Same idea
applies to weekly habits, just counted in whole weeks instead of days.

## Editing a habit

Click the pencil icon on any habit, and a form pops up already filled in with
everything that habit currently has — its name, description, category, and
frequency. Change whatever you want and save.

One detail worth knowing: when you save, the app replaces **the whole thing**,
not just the one field you changed. That's why the edit form always shows
every field pre-filled rather than a blank box — if it only sent the one
changed field, the app would think you meant to erase everything else.

## Filtering by category

This one's simpler than it looks: when you click a category chip (like
"Learning"), the app isn't going back to ask the server anything. It already
has your full list of habits sitting in front of it — clicking the filter
just temporarily hides the habits that don't match, instantly, with nothing
loading in the background.

## Viewing a habit's details

Tap any habit tile, and you get a closer look: a week-by-week breakdown of
how consistent you've been, plus a rating (Excellent, Great, Good, Fair, or
Needs work) based on how many times per week you actually did it compared to
your target.

## Deleting a habit

A confirmation pop-up checks you're sure, then the habit and **all of its
history** are gone for good — there's no undo, no trash bin to recover it
from.

## Who can see or touch your habits

Every single habit belongs to exactly one account. If you tried to peek at
or edit someone else's habit (say, by guessing its ID), the app would act
like it doesn't exist at all — not even telling you it belongs to someone
else. It just says "not found," the same message you'd get for an ID that
was never real in the first place. That way, nobody can even confirm another
person's habit exists, let alone see or change it.
