---
name: story-workflow
description: D-NEXT story workflow patterns including states, lifecycle, parent-child relationships, branching rules, and resume behavior. Auto-activates when working with JIRA stories, story breakdown, or managing story status transitions. Use when you see PRND-xxxxx, child stories (#1, #2), or story state changes.
---

# Story Workflow Skill

This skill provides workflow patterns for D-NEXT story management.

## Story States

```text
BACKLOG --> DOING --> REVIEWING --> DONE
               |
               v
             HELD (blocked by dependency)
               |
               v
            BACKLOG (when unblocked)
```

| State | Meaning |
|-------|---------|
| `BACKLOG` | Not yet started, ready to pick |
| `DOING` | In progress (includes refinement and implementation) |
| `HELD` | Blocked by dependency (another story or external) |
| `REVIEWING` | Code review in progress |
| `DONE` | Complete |

## Story Lifecycle

1. **Pick JIRA story** from BACKLOG, move to DOING
2. **Create branch** in module repo: `feature/PRND-xxxxx` or `bugfix/PRND-xxxxx`
3. **Generate implementation guide** using context sources
4. **Break down into child stories** (#1, #2, #3) if too complex
5. **Implement each child story** on the same branch (test-first per child)
6. **Review loop** for each child (may create review items #1.R1, #2.R1)
7. **Final PR review** when all child stories DONE
8. **Merge PR**, parent JIRA story and all children marked DONE
9. **Update module design.md** with implementation summary

## Parent Story as Tracker

When a JIRA story is broken into child stories:

- **Parent stays in DOING**: The JIRA story (PRND-xxxxx) remains active
- **Parent becomes tracker**: No direct implementation on parent; it coordinates children
- **For reporting**: Parent story is the "active" story (what you report working on)
- **Single branch**: All children implemented on `feature/PRND-xxxxx`
- **Auto-completion**: Parent transitions to DONE when all children complete

```text
PRND-27182 (JIRA, DOING)   <-- Report this as active
├── #1: Controller [DONE]
├── #2: Service [DOING]    <-- Currently implementing
└── #3: Events [BACKLOG]
```

## Resume Behavior

If developer pauses mid-story:

- Child story states preserved in .stories.md
- Running `/start-story PRND-xxxxx` again detects existing children
- Prompts to continue with next BACKLOG child

## Branch Rules

- MUST create branch only for JIRA stories (PRND-xxxxx), NOT for epics
- Branch format: `feature/PRND-xxxxx` or `bugfix/PRND-xxxxx`
- MUST NOT create separate branches for child stories (#1, #2) or review items (#1.R1)
- All child story work happens on the parent JIRA story branch
- MUST NOT create branches in dnext-dev-support repo (documentation only)

## Story Discovery During Development

When discovering beneficial work during implementation:

- **Technical work** (refactoring, tech debt): Inform user, add to BACKLOG in current .stories.md
- **Cross-module dependency**: Inform user, create JIRA enabler story, add HELD status to current story
- Add only sufficient detail when discovered. Full grooming happens at pickup.

## Vertical Slice Stories

Each JIRA story must be a **vertical slice** - delivering end-to-end functionality for a use case:

- Based on use cases, not technical layers
- Includes all FRs and BVRs for that use case
- Independently deployable and testable
- Split large use cases progressively: happy path first, validation/exceptions later
