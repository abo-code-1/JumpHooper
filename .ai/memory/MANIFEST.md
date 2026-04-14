# Memory — Manifest

Memory is sliced by topic so agents can load just the relevant file instead of all project knowledge at once.

| File | What's in it | Triggers |
|---|---|---|
| [`project.md`](project.md) | Project decisions: why libGDX, why hand-rolled physics, why not Box2D, why enum singleton | project, why, decision, history, rationale, architecture |
| [`team.md`](team.md) | Members, roles, task-split convention, who owns what area | Abror, Ivan, team, teammate, who owns, who's working on |
| [`clickup.md`](clickup.md) | Workspace/space/list IDs, API base URL, auth expectations | ClickUp, task, JumpHooper space, workspace id, api |

## Loading rules for agents

1. Scan frontmatter `triggers:` against the user's message.
2. Load at most 2 memory files per turn unless the user asks explicitly — memory is support, not ambient noise.
3. In-repo memory (`.ai/memory/`) **wins** over agent-specific memory (e.g. Claude Code's auto-memory). If they conflict, update both.
4. If a memory is stale (e.g. team changed, task IDs changed), **edit the file** — don't pile a correction on top.

## Frontmatter shape

```yaml
---
name: <short title>
description: <one-line purpose>
type: <project | team | reference | feedback | user>
triggers: [keyword, keyword, …]
---
```
