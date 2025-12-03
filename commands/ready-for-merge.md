# Ready for Merge Command

Validate branch is ready for PR creation. Performs comprehensive checks before merge.

## Usage

```
/ready-for-merge [target-branch]
```

**Parameters:**
- `target-branch`: Optional. Target branch for merge (default: prompts for selection)

**Examples:**
```
/ready-for-merge
/ready-for-merge develop
/ready-for-merge main
```

## What This Does

### 1. Environment Check

- Identifies current branch (e.g., `feature/PRND-45001`)
- Determines target branch (asks if not provided)
- Counts commits ahead of target
- Verifies no uncommitted changes

### 2. Story Status Check

- Reads .stories.md for the JIRA story
- Verifies all child stories are DONE
- Verifies all review items are DONE or deleted
- Confirms parent story ready for completion

### 3. Build & Test

- Runs clean build: `mvn clean compile`
- Runs full test suite: `mvn test`
- Checks for warnings
- Verifies test count matches expectations

### 4. Code Quality Review

- Reviews all changes vs target branch
- Checks for:
  - Leftover TODO/FIXME comments
  - Unused code
  - Debug statements
  - Code duplication
  - Obvious issues

### 5. Design Doc Check

- Verifies design.md was updated
- Confirms updates match implementation

### 6. Decision

Either approves for merge or creates blocking issues.

## Full Workflow

### Approved for Merge

```
/ready-for-merge

Merge Readiness Check: feature/PRND-45001
═══════════════════════════════════════════════════════════════

Which branch should this merge into?
1. develop
2. main
3. Other (specify)

Choice [1/2/3]: 1

Target: develop

Branch Information
────────────────────────────────────────────────────────────
  Current branch: feature/PRND-45001
  Target branch: develop
  Commits ahead: 12
  Story: PRND-45001 - GET /troubleTicket/{id} endpoint

Story Status
────────────────────────────────────────────────────────────
  Parent: PRND-45001 [DOING]
  Child stories:
    #1: Implement controller       [DONE]
    #2: Add validation             [DONE]
    #3: Integrate events           [DONE]
  Review items:
    #1.R1: Extract validation      [DONE]
    #1.R2: Null check              [DONE]

  All child stories and review items complete.

Build & Test
────────────────────────────────────────────────────────────
  Clean build: SUCCESS
  Tests: 47 passed, 0 failures
  Warnings: 0

Code Quality
────────────────────────────────────────────────────────────
  TODO/FIXME comments: 0
  Unused code: None detected
  Debug statements: None
  Code duplication: None detected

Design Documentation
────────────────────────────────────────────────────────────
  design.md updated: Yes
  Last update: 2025-11-28 14:30
  Matches implementation: Yes

═══════════════════════════════════════════════════════════════

Decision: APPROVED

Branch is ready to merge.

Suggested steps:
1. Create PR:
   gh pr create --base develop --title "PRND-45001: GET /troubleTicket/{id} endpoint"

2. After PR merged:
   - Mark JIRA story DONE
   - Delete epic folder when all stories complete
```

### Blocked - Issues Found

```
/ready-for-merge develop

Merge Readiness Check: feature/PRND-45001
═══════════════════════════════════════════════════════════════

[... branch info ...]

Story Status
────────────────────────────────────────────────────────────
  Parent: PRND-45001 [DOING]
  Child stories:
    #1: Implement controller       [DONE]
    #2: Add validation             [REVIEWING]  <-- Not complete
    #3: Integrate events           [BACKLOG]    <-- Not started

  BLOCKED: 2 child stories not complete.

═══════════════════════════════════════════════════════════════

Decision: BLOCKED

Issues to resolve:
  1. Complete child story #2 (currently in REVIEWING)
  2. Complete child story #3 (not started)

Run /list-stories --epic PRND-12345 to see details.
```

### Blocked - Code Quality Issues

```
/ready-for-merge develop

[... story status OK ...]

Build & Test
────────────────────────────────────────────────────────────
  Clean build: SUCCESS
  Tests: 45 passed, 2 failures  <-- BLOCKED

  Failing tests:
    - TroubleTicketControllerTest.shouldValidateInput
    - TroubleTicketServiceTest.shouldHandleNullParty

Code Quality
────────────────────────────────────────────────────────────
  TODO/FIXME comments: 2 found  <-- Warning
    - TroubleTicketController.java:89 // TODO: add rate limiting
    - TroubleTicketService.java:45 // FIXME: handle edge case

  Debug statements: 1 found  <-- Warning
    - TroubleTicketMapper.java:23 System.out.println("debug")

═══════════════════════════════════════════════════════════════

Decision: BLOCKED

Critical issues:
  1. 2 test failures - must fix

Warnings (should fix):
  2. 2 TODO/FIXME comments in production code
  3. 1 debug statement to remove

Fix issues and run /ready-for-merge again.
```

### Blocked - Design Not Updated

```
/ready-for-merge develop

[... build OK ...]

Design Documentation
────────────────────────────────────────────────────────────
  design.md updated: No  <-- BLOCKED
  Last update: 2025-11-25 (3 days ago)
  Expected updates:
    - TroubleTicketController (new)
    - TroubleTicketMapper (modified)

═══════════════════════════════════════════════════════════════

Decision: BLOCKED

Issue: design.md not updated with implementation.

Run /complete-story for remaining child stories to trigger design update.
Or manually update design.md and commit.
```

## Checks Performed

| Check | Blocking | Description |
|-------|----------|-------------|
| Uncommitted changes | Yes | All work must be committed |
| Child stories DONE | Yes | All child stories complete |
| Review items DONE | Yes | All accepted review items complete |
| Build succeeds | Yes | `mvn clean compile` passes |
| Tests pass | Yes | `mvn test` all green |
| No new warnings | Advisory | Compiler warnings |
| No TODO/FIXME | Advisory | Comments in production code |
| No debug code | Advisory | System.out, console.log, etc. |
| No unused code | Advisory | Dead code detection |
| design.md updated | Yes | Documentation current |

## JIRA Sync

Before creating PR, the command offers to sync status:

```
Story PRND-45001 ready for merge.

Sync final status to JIRA? [Y/n]
  - Status: DOING --> In Review
  - Duration: 6h 30m

> Y

JIRA updated.
```

## Agent Used

This command invokes the **merge-readiness-checker** agent which:
- Analyzes branch state
- Runs build and tests
- Reviews code quality
- Checks documentation
- Provides actionable feedback

## Prerequisites

- On a feature/bugfix branch
- Story in DOING state
- Module repository with build system

## See Also

- `/complete-story` - Complete child stories first
- `/sync-jira` - Sync status to JIRA
- `/list-stories` - Check story status
- `merge-readiness-checker` agent
