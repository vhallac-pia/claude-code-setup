---
name: story-conventions
description: Story file format conventions for .stories.md files including structure, headings, anchors, metadata, and tags. Auto-activates when editing stories.md files, creating story breakdowns, or managing story format. Use when you see story headings like [DOING], [BACKLOG], child stories (#1, #2), or review items (#1.R1).
---

# Story Conventions Skill

This skill provides formatting conventions for .stories.md files.

## Reference Documents

- @temp/dnext-dev-support/templates/README.md - Template usage and conventions
- @temp/dnext-dev-support/templates/stories.template.md - Story file structure

## File Structure

- One `stories.md` per epic folder
- YAML front matter with epic context
- All stories at H1 level (JIRA, child, and review items)
- Logical hierarchy via placement (child after parent, review after child)

## Heading Levels

- `#` (H1): All stories (JIRA, child, review items)
- `##` (H2): Implementation Guide (subsection under JIRA story only)

## Status in Headings with Anchors

```markdown
<a id="PRND-27182"></a>
# [DOING] PRND-27182: Story Title :jira:
<!-- modules: DPOMS | complexity: medium -->

## Implementation Guide
...

<a id="PRND-27182.1"></a>
# [DONE] #1: Child Story Title
<!-- modules: DPOMS -->

<a id="PRND-27182.1.R1"></a>
# [BACKLOG] #1.R1: Review Item Title :review:
<!-- severity: medium -->
```

Anchors enable stable linking (status changes don't break links):

- Within file: `[#1](#PRND-27182.1)`, `[#1.R1](#PRND-27182.1.R1)`
- Cross-epic: `[PRND-27183](../PRND-{other-epic}-{slug}/stories.md#PRND-27183)`

## Story Hierarchy

| Level | Pattern | Synced to JIRA? |
|-------|---------|-----------------|
| JIRA Story | `# [STATUS] PRND-xxxxx: Title :jira:` | Yes |
| Child Story | `# [STATUS] #1: Title` | No (local only) |
| Review Item | `# [STATUS] #1.R1: Title :review:` | No (local only) |

## Placement Rules

- **Child stories**: Place immediately after parent JIRA story (after Implementation Guide)
- **Review items**: Place immediately after the child story they belong to

## Metadata Format

```markdown
<!-- key: value | key2: value2 -->
```

Common keys:

| Key | Usage |
|-----|-------|
| `modules` | Affected modules (DPOMS, common-core) |
| `complexity` | low, medium, high |
| `hours` | Estimate range (2-4) |
| `use_case` | UC reference from SRS |
| `depends_on` | Dependency reference |
| `held_by` | What's blocking (if HELD) |
| `blocks` | What this blocks |
| `severity` | For review items (critical, high, medium, low) |

## MODULES Naming

- **Services**: Use commercial name (e.g., `DPOMS`, `DPOM-OFS`, `DPCI`)
- **Libraries**: Use repository name (e.g., `common-core`, `dnext-commons`)

## Tags

Tags appear at end of headings:

| Tag | Purpose |
|-----|---------|
| `:jira:` | Story syncs to JIRA |
| `:review:` | Review item from code reviewer |
| `:foundation:` | Foundational/enabler work |
| `:urgent:` | High priority |
| `:backend:` | Backend-only change |

## Story States

| State | Meaning |
|-------|---------|
| BACKLOG | Not yet started, ready to pick |
| DOING | In progress (includes refinement + implementation) |
| HELD | Blocked by dependency |
| REVIEWING | Code review in progress |
| DONE | Complete |
