---
name: Project decisions & rationale
description: Why the codebase looks the way it does — architectural decisions, scope cuts, and "why not X" explanations.
type: project
triggers: [project, why, decision, history, rationale, architecture, scope, MVP]
---

# Project decisions

## Why Java + libGDX

Chosen because (1) both team members are comfortable with Java, (2) libGDX makes the 6 design patterns show up naturally (Screen, AssetManager, SpriteBatch are textbook OO), and (3) it grades cleanly for a Design Pattern course.

Alternatives considered: Tiny.js (JS). **Rejected** — too lightweight, hides patterns behind closures. The project briefly existed as a Tiny.js scaffold before being wiped and restarted.

## Why hand-rolled physics (no Box2D)

Box2D would dominate the codebase and bury our patterns under its own abstractions (World, Body, Fixture, etc.). Our physics is two `Vector2`s and a constant — that's enough for MVP and keeps the Strategy/Observer work visible.

**If scope grows** (springs, monsters, rope swings), revisit.

## Why enum singleton for ScoreManager

Effective Java Item 3. Enum singletons are:
- thread-safe by JVM guarantee
- serialization-safe
- reflection-safe
- zero boilerplate compared to `private static final INSTANCE` + `private constructor` + `readResolve`

The one downside (can't subclass) doesn't matter here — we don't want subclasses of a score manager.

## Why the 20-task split (not 10)

The first pass had 10 large tasks. Each felt intimidating and mixed multiple concerns (e.g., "Platform system" = 4 subclasses + factory + strategy all in one).

**Rewritten into 20 smaller tasks** so each is a 1–2 hour chunk with a single clear goal. Pattern work is still labeled on the specific tasks that carry it (6 out of 20). Ivan owns 9, Abror owns 11 — imbalance reflects that gameplay has more discrete subsystems than flow/UI.

## Why we pivoted from Tiny.js to libGDX

Originally scaffolded from a TinyJS DoodleJump tutorial. Re-evaluated mid-project and switched to Java/libGDX for pattern visibility (see above) and because Java is the team's shared strong language.

**Lesson:** the first README, architecture doc, and task descriptions were all written against Tiny.js and had to be rewritten. Double-check the stack *before* writing detailed docs next time.

## Why we lost the source once

The doodle-jump subfolder was a nested git repo, not a proper submodule — no `.gitmodules` pointing to a remote. When the parent directory was wiped, the nested `.git` went with it and there was no recovery path.

**Invariant going forward:** always commit + push to a real remote before any destructive operation. If a folder looks like a submodule, verify `.gitmodules` exists and contains the URL.
