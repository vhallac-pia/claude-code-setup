# Start Story Command

Pick up a JIRA story for implementation. Creates branch, generates implementation guide, breaks down if needed, and initiates TDD workflow.

## Usage

```
/start-story <story-id>
```

**Parameters:**
- `story-id`: Required. The JIRA story ID (e.g., PRND-45001)

**Examples:**
```
/start-story PRND-45001
```

## What This Does

### 1. Validates Story

- Locates story in `dnext-dev-support/epics/PRND-*/` folders
- Verifies story is in BACKLOG or DOING state
- If DOING with children: enters **resume mode**
- Checks story is tagged `:jira:` (synced)
- Loads epic context (srs-extract.md, feature-design.md)

### 2. Creates Feature Branch

In the module repository:

```bash
git checkout -b feature/PRND-45001
# or for bugfix:
git checkout -b bugfix/PRND-45001
```

### 3. Generates Implementation Guide

Invokes **story-implementation-planner** agent:

- Reads story description and acceptance criteria
- Reads SRS extract for business context
- Reads module design.md for current state
- Analyzes complexity (files affected, integration points)
- Generates implementation guide

### 4. Breaks Down If Complex

If story is too large (>8 files, >1 day effort):

```
Story analysis complete.

Complexity: High
- Files affected: 12
- Integration points: 4
- Estimated effort: 16-24 hours

Recommend breaking into 3 child stories:
  #1: Implement API controller (4-6h)
  #2: Add validation logic (4-6h)
  #3: Integrate with event system (4-6h)

Proceed with breakdown? [Y/n]
```

When breakdown confirmed:
- Parent story becomes a "tracker" (no direct implementation)
- All code work happens on child stories
- For reporting, developer reports parent story as active
- Parent auto-completes when all children are DONE

### 5. Initiates TDD Workflow Per Child

After breakdown (or if simple story):

```
Parent story PRND-45001 is now a tracker.
Child stories created: #1, #2, #3

Ready to implement the tests for #1? [Y/n]
```

If confirmed, invokes **test-first-designer** to create tests for child story #1.

### 6. Resume Mode

If called on a DOING story with existing children:

```
/start-story PRND-45001

Existing child stories detected:
  #1: Implement API controller [DONE]
  #2: Add validation logic [DOING]
  #3: Integrate with event system [BACKLOG]

Next: #2 (currently DOING)

Ready to continue with #2? [Y/n]
```

This allows pausing and resuming work across sessions.

## Full Workflow

```
/start-story PRND-45001

Loading story context...
  Story: PRND-45001 - GET /troubleTicket/{id} endpoint
  Epic: PRND-12345
  Module: DPOMS
  SRS: srs-extract.md loaded
  Design: design.md loaded

Creating feature branch...
  git checkout -b feature/PRND-45001
  Branch created.

Generating implementation guide...

═══════════════════════════════════════════════════════════════
Implementation Guide: PRND-45001
═══════════════════════════════════════════════════════════════

## Context & Constraints

This story implements the GET endpoint for retrieving trouble
ticket details per TMF621 specification. Must integrate with
existing TroubleTicketService and respect current validation
patterns.

Key constraints from SRS:
- Response must match TMF621 TroubleTicket schema
- Authorization: only ticket owner or admin
- Include related entities based on query params

## Files Involved

### TroubleTicketController.java (Create)
- New REST controller for /troubleTicket endpoints
- GET /{id} method with authorization check
- Response mapping to TMF621 schema

### TroubleTicketService.java (Modify)
- Add findById method with authorization
- Integrate with existing repository

### TroubleTicketMapper.java (Modify)
- Add toTmfResponse mapping method
- Handle related entity expansion

### TroubleTicketControllerTest.java (Create)
- Unit tests for controller
- Authorization scenarios
- Error cases

## Component Interactions

Controller --> Service --> Repository
         v
       Mapper --> TMF Response

## Key Considerations

- Error handling: Return 404 for not found, 403 for unauthorized
- Performance: Lazy load related entities
- Testing: Mock authorization context

═══════════════════════════════════════════════════════════════

Complexity: Medium (4-6 files, 4-6 hours)

Story is appropriately sized. No breakdown needed.

Ready to implement the tests? [Y/n]
```

### After Test Confirmation

```
> Y

Invoking test-first-designer...

Creating tests for PRND-45001...
  - TroubleTicketControllerTest.java (8 tests)
  - TroubleTicketServiceTest.java (5 tests)
  - TroubleTicketMapperTest.java (3 tests)

Tests created. Running initial verification...
  16 tests, 16 failures (expected - red phase)

Ready to implement the code? [Y/n]
```

### After Implementation Confirmation

```
> Y

Invoking implementer agent...

Starting TDD implementation for PRND-45001.

I'll work through the implementation systematically:
1. TroubleTicketController - make controller tests pass
2. TroubleTicketService - make service tests pass
3. TroubleTicketMapper - make mapper tests pass

Beginning with TroubleTicketController...

[Implementation proceeds with red-->green-->refactor cycle]
```

## Story State Updates

The command updates .stories.md:

**Before:**
```markdown
### [BACKLOG] PRND-45001: GET /troubleTicket/{id} endpoint :jira:
<!-- jira: PRND-45001 | modules: DPOMS -->
```

**After:**
```markdown
### [DOING] PRND-45001: GET /troubleTicket/{id} endpoint :jira:
<!-- jira: PRND-45001 | modules: DPOMS | started: 2025-11-28T09:30:00 -->

#### Implementation Guide
[Generated guide content...]

#### Child Stories
- [BACKLOG] #1: Implement controller
- [BACKLOG] #2: Add validation
- [BACKLOG] #3: Integrate events
```

## Error Handling

### Story Not Found

```
/start-story PRND-99999

Error: Story PRND-99999 not found.

Search locations:
- dnext-dev-support/epics/PRND-*/

Ensure story exists and is synced with /sync-jira
```

### Story in Invalid State

```
/start-story PRND-45001

Error: Story PRND-45001 is in REVIEWING state.

Cannot start a story in REVIEWING or DONE state.
Use /list-stories to see available work.
```

Note: DOING stories with children will enter resume mode (see above).

### Branch Already Exists

```
/start-story PRND-45001

Warning: Branch feature/PRND-45001 already exists.

Options:
1. Switch to existing branch and continue
2. Delete branch and start fresh
3. Abort

Choice [1/2/3]:
```

### Missing Context

```
/start-story PRND-45001

Warning: srs-extract.md not found for epic PRND-12345.

Implementation guide quality may be reduced without SRS context.

Options:
1. Continue anyway
2. Abort and run /start-feature first

Choice [1/2]:
```

## Agents Invoked

This command orchestrates multiple agents:

1. **story-implementation-planner**: Generates implementation guide
2. **test-first-designer**: Creates failing tests (after confirmation)
3. **implementer**: TDD implementation (after confirmation)

## Agent Handover Flow

```
/start-story PRND-xxxxx
    │
    ├─--> story-implementation-planner
    │       Analyzes, creates children if complex
    │       Parent becomes tracker (if broken down)
    │       "Ready to implement the tests for #1? [Y/n]"
    │
    ├─--> test-first-designer (for #1)
    │       "Tests created for #1 (red). Ready to implement? [Y/n]"
    │
    ├─--> implementer (for #1)
    │       [TDD implementation loop]
    │       "All tests passing for #1."
    │       │
    │       └─--> /complete-story #1 (triggers review)
    │
    ├─--> "Ready to start #2? [Y/n]"
    │       │
    │       └─--> Repeat cycle for #2, #3, etc.
    │
    └─--> When all children DONE:
            Parent auto-completes
            /ready-for-merge
```

## Prerequisites

- Story exists and is synced to JIRA (`:jira:` tag)
- Story is in BACKLOG state
- Epic folder exists with context files
- Module repository is available
- No uncommitted changes in module repo

## See Also

- `/list-stories` - Find stories to start
- `/groom-stories` - Create stories from SRS
- `/sync-jira` - Sync story status
- `/complete-story` - Mark child story done
- `story-implementation-planner` agent
- `test-first-designer` agent
- `implementer` agent
