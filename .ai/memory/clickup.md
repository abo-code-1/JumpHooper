---
name: ClickUp coordinates
description: Workspace / space / list IDs and API usage for the DuddleJump project.
type: reference
triggers: [ClickUp, task, JumpHooper space, workspace id, api, curl, click up]
---

# ClickUp coordinates

## IDs

- **Workspace (team):** `90182544099` — "Abror's Workspace"
- **Space:** `901810382820` — "JumpHooper – Task management" (rich 8-status workflow: to do → planning → in progress → at risk → update required → on hold → complete → cancelled)
- **List:** `901816969291` — the active MVP list (20 tasks)

## API

- Base: `https://api.clickup.com/api/v2`
- Auth: `Authorization: <personal-token>` header (personal tokens start with `pk_…`)
- **Token is user-supplied per session. Never persist it, never commit it, never log it.**

## Common calls

```bash
# List all tasks (including closed)
curl -H "Authorization: $TOKEN" \
  "https://api.clickup.com/api/v2/list/901816969291/task?include_closed=true"

# Get one task by id
curl -H "Authorization: $TOKEN" \
  "https://api.clickup.com/api/v2/task/<task_id>"

# Update status
curl -X PUT -H "Authorization: $TOKEN" -H "Content-Type: application/json" \
  "https://api.clickup.com/api/v2/task/<task_id>" \
  -d '{"status":"in progress"}'

# Create a task
curl -X POST -H "Authorization: $TOKEN" -H "Content-Type: application/json" \
  "https://api.clickup.com/api/v2/list/901816969291/task" \
  -d '{"name":"…","description":"…","assignees":[290416131],"priority":2}'
```

## Priority values

| Priority | Meaning |
|---|---|
| `1` | urgent |
| `2` | high |
| `3` | normal |
| `4` | low |

## Task name convention

- `[I01]` to `[I09]` = Ivan, `[A01]` to `[A11]` = Abror. Numbering matches `docs/MVP_TASKS.md`.
- When the user says "A05" without context, it means the ClickUp task prefixed `[A05]`.
