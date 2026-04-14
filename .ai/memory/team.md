---
name: Team — Abror & Ivan
description: Two-person team, task-split conventions, area ownership.
type: team
triggers: [Abror, Ivan, team, teammate, who owns, who's working on, split, assignee]
---

# Team

## Members

| Name | Role | ClickUp id | Email |
|---|---|---|---|
| **Abror** | Owner, gameplay engine | `290416131` | abocorp4@gmail.com |
| **Ivan Kanevskii** | Flow / UI / persistence | `113423894` | ivan.kanevskii@narxoz.kz |

## Area ownership

| Area | Owner |
|---|---|
| `core/.../entities/` (Doodle, Platform*, spawner) | Abror |
| `core/.../factories/` | Abror |
| `core/.../events/` (EventBus, ContactListener) | Abror |
| `core/.../state/` (GameState + impls) | Abror |
| Camera + world scroll in GameScreen | Abror |
| `core/.../managers/Assets.java` | Ivan |
| `core/.../managers/ScoreManager.java` | Ivan |
| `core/.../screens/` (Menu, Loading, GameOver) | Ivan |
| `core/.../input/` | Ivan |
| Gradle setup, build config, desktop launcher | Ivan |

## Task-split conventions

- Tasks on ClickUp are named with zero-padded prefixes: `[A01]` to `[A11]` for Abror and `[I01]` to `[I09]` for Ivan.
- PR titles mirror the task prefix: `[A05] Green + Red platforms`.
- When the user says "A05" or "I03", they mean that specific ClickUp task — look it up in `docs/MVP_TASKS.md`.
- Parallelizable work: Ivan and Abror can always pick up independent tasks simultaneously. First sprint: `[I01]` + `[A01]`.
