# Complete Story Command

Mark a child story as complete, triggering review and design update workflow.

## Usage

```
/complete-story <child-story-id>
```

**Parameters:**
- `child-story-id`: Required. The child story ID (e.g., #1, #2, #1.R1)

**Examples:**
```
/complete-story #1
/complete-story #2
/complete-story #1.R1
```

## What This Does

### 1. Validates Completion

- Verifies child story exists in current .stories.md
- Checks story is in DOING state
- Runs tests to confirm all passing
- Checks for uncommitted changes

### 2. Triggers Code Review

Invokes **code-reviewer** agent:

- Analyzes commits for this child story
- Reviews code quality, patterns, issues
- Creates review items if problems found

### 3. Handles Review Results

Presents review findings with options:

```
Review complete for #1: Implement controller

Found 2 issues:

#1.R1: Extract duplicate validation logic [Medium]
  - DRY violation in TroubleTicketController
  - Similar validation in 3 methods
  - Suggest: Extract to ValidationHelper

#1.R2: Add null check in mapper [High]
  - Potential NPE in toTmfResponse
  - relatedParty can be null
  - Suggest: Add null guard

Actions:
1. Accept all and create review items
2. Review individually (accept/reject each)
3. Reject all (no review items)

Choice [1/2/3]:
```

### 4. Updates Design

After review items resolved (or if none), invokes **design-updater**:

- Synthesizes implementation guide from child story
- Updates module design.md
- Commits design changes

### 5. Marks Complete

- Updates child story status to DONE
- Records completion timestamp
- Syncs to JIRA if parent is `:jira:` tagged

## Full Workflow

### Child Story Without Issues

```
/complete-story #1

Validating completion...
  Story: #1 - Implement controller
  Parent: PRND-45001
  Status: DOING

Running tests...
  16 tests passed, 0 failures

Checking uncommitted changes...
  All changes committed.

Invoking code-reviewer...

Review complete for #1.

No significant issues found. Code approved.

Updating design.md...
  Added: TroubleTicketController documentation
  Updated: Component interaction diagram

design.md updated successfully.

Marking #1 as DONE...
  Duration: 2h 15m

#1 complete.

Child story status:
  #1: DONE
  #2: BACKLOG
  #3: BACKLOG

Continue to #2? [Y/n]
```

### Child Story With Review Items

```
/complete-story #1

[... validation and tests ...]

Invoking code-reviewer...

Review complete for #1.

Found 2 issues:

#1.R1: Extract duplicate validation logic [Medium]
  Location: TroubleTicketController.java:45, 78, 112
  Issue: Same validation pattern repeated 3 times
  Suggest: Create ValidationHelper.validateTicketAccess()

#1.R2: Add null check in mapper [High]
  Location: TroubleTicketMapper.java:34
  Issue: relatedParty can be null, causing NPE
  Suggest: Add Optional.ofNullable guard

Actions:
1. Accept all and create review items
2. Review individually (accept/reject each)
3. Reject all (no review items)

Choice [1/2/3]: 2

#1.R1: Extract duplicate validation logic [Medium]
  Accept this review item? [Y/n]: Y
  Created #1.R1

#1.R2: Add null check in mapper [High]
  Accept this review item? [Y/n]: Y
  Created #1.R2

Review items created: #1.R1, #1.R2

#1 remains in REVIEWING until review items complete.

Fix review items and run:
  /complete-story #1.R1
  /complete-story #1.R2
```

### Completing Review Items

```
/complete-story #1.R1

Validating completion...
  Story: #1.R1 - Extract duplicate validation logic
  Parent: #1
  Status: DOING

Running tests...
  18 tests passed, 0 failures

Invoking code-reviewer...

Review complete for #1.R1. No issues found.

Checking sibling review items...
  #1.R1: DONE (just completed)
  #1.R2: BACKLOG

Review not complete. #1.R2 still pending.

Continue to #1.R2? [Y/n]
```

### All Review Items Complete

```
/complete-story #1.R2

[... validation and review ...]

Checking sibling review items...
  #1.R1: DONE
  #1.R2: DONE (just completed)

All review items for #1 complete.

Updating design.md...
  Synthesized from: #1, #1.R1, #1.R2
  Added: ValidationHelper documentation
  Updated: TroubleTicketMapper null handling

design.md updated successfully.

Marking #1 as DONE...
  Duration: 3h 45m (including review fixes)

#1 complete.

Child story status:
  #1: DONE
  #2: BACKLOG
  #3: BACKLOG

Continue to #2? [Y/n]
```

## Story State Updates

### After Review Items Created

```markdown
### [REVIEWING] #1: Implement controller
<!-- started: 2025-11-28T09:30 -->

[Implementation guide...]

#### Review Items
- [BACKLOG] #1.R1: Extract duplicate validation logic :review:
- [BACKLOG] #1.R2: Add null check in mapper :review:
```

### After All Complete

```markdown
### [DONE] #1: Implement controller
<!-- started: 2025-11-28T09:30 | completed: 2025-11-28T12:15 | duration: 2h45m -->

[Implementation guide...]

#### Review Items
- [DONE] #1.R1: Extract duplicate validation logic :review:
- [DONE] #1.R2: Add null check in mapper :review:
```

## Error Handling

### Tests Failing

```
/complete-story #1

Running tests...
  14 tests passed, 2 failures

Cannot complete story with failing tests.

Failures:
  - TroubleTicketControllerTest.shouldReturn404ForUnknownId
  - TroubleTicketControllerTest.shouldReturn403ForUnauthorized

Fix tests and try again.
```

### Uncommitted Changes

```
/complete-story #1

Checking uncommitted changes...
  Modified: TroubleTicketController.java
  Modified: TroubleTicketService.java

Uncommitted changes found.

Options:
1. Commit changes and continue
2. Abort

Choice [1/2]:
```

### Story Not in DOING

```
/complete-story #1

Error: #1 is in BACKLOG state.

Story must be in DOING to complete.
Start with: Move #1 to DOING first.
```

## Agents Invoked

1. **code-reviewer**: Reviews implementation, creates review items
2. **review-completion-checker**: Checks if all review items done
3. **design-updater**: Updates design.md with implementation

## Agent Handover Flow

```
/complete-story #1
    │
    ├─--> code-reviewer
    │       "Found N issues. Accept? [1/2/3]"
    │
    ├─--> [If review items] User fixes items
    │       /complete-story #1.R1
    │       /complete-story #1.R2
    │
    ├─--> review-completion-checker
    │       "All review items complete."
    │
    └─--> design-updater
            "design.md updated. Continue to #2? [Y/n]"
```

## Design Update Details

The **design-updater** agent:

1. Reads implementation guide from completed child story
2. Follows review chain (parent + review items)
3. Synthesizes complete implementation picture
4. Updates module design.md:
   - New components/classes
   - API changes
   - Validation rules
   - Integration points
5. Commits design changes

## Prerequisites

- Child story exists and is in DOING state
- Tests pass
- Changes committed
- Parent JIRA story is in DOING

## See Also

- `/start-story` - Begin story implementation
- `/ready-for-merge` - Check merge readiness
- `/list-stories` - View story status
- `code-reviewer` agent
- `design-updater` agent
- `review-completion-checker` agent
