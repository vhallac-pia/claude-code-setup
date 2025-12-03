# Groom Stories Command

Create story breakdown from SRS with Tech Lead, iterating in .stories.md before JIRA sync.

## Usage

```
/groom-stories <epic-id>
```

**Parameters:**
- `epic-id`: Required. The JIRA epic ID (e.g., PRND-12345)

**Examples:**
```
/groom-stories PRND-12345
```

## What This Does

### 1. Loads Context

- Reads epic folder: `dnext-dev-support/epics/PRND-{epic-id}-*/`
- Loads `srs-extract.md` for business requirements
- Loads `feature-design.md` if exists (multi-module)
- Loads module `design.md` for current state context

### 2. Analyzes SRS

- Identifies functional requirements
- Extracts Given/When/Then scenarios
- Maps BVRs to implementation needs
- Detects API changes, data model changes
- Estimates complexity per requirement area

### 3. Proposes Story Breakdown

Interactive session with Tech Lead:

```
Analyzing SRS for PRND-12345: Trouble Ticket API Enhancement

Identified requirement areas:
1. New API endpoints (3 endpoints)
2. Validation rule changes (5 BVRs)
3. State machine updates (2 transitions)
4. Event publishing (1 new event)

Proposed story breakdown:

PRND-{tbd-1}: Implement GET /troubleTicket/{id} endpoint
  Modules: DPOMS
  Complexity: Medium (4-6 hours)
  Covers: FR-1, FR-2, BVR-101

PRND-{tbd-2}: Implement POST /troubleTicket endpoint
  Modules: DPOMS
  Complexity: High (8-12 hours)
  Covers: FR-3, FR-4, BVR-102, BVR-103

PRND-{tbd-3}: Add state transition validation
  Modules: DPOMS
  Complexity: Medium (4-6 hours)
  Covers: FR-5, BVR-104, BVR-105

PRND-{tbd-4}: Implement TroubleTicketCreatedEvent publishing
  Modules: DPOMS, common-events
  Complexity: Low (2-4 hours)
  Covers: FR-6

Does this breakdown look right?
1. Accept and create .stories.md
2. Adjust (specify changes)
3. Start over with different grouping

Choice [1/2/3]:
```

### 4. Creates Stories File

After Tech Lead approves, creates `stories.md` using the format from `dnext-dev-support/templates/stories.template.md`.

Each story includes:
- Anchor for stable linking: `<a id="PRND-{tbd-N}"></a>`
- Status heading: `### [BACKLOG] PRND-{tbd-N}: {Title} :jira:`
- Metadata comment: `<!-- modules: {MODULE} | complexity: {level} | hours: {range} -->`
- Description, acceptance criteria, and SRS references

### 5. Iteration Loop

Tech Lead can iterate on stories:

```
Stories created in: stories.md

Review the stories and let me know:
- "split PRND-{tbd-2}" - Break a story into smaller pieces
- "merge PRND-{tbd-3} PRND-{tbd-4}" - Combine stories
- "adjust PRND-{tbd-1}" - Modify a story's scope
- "add story" - Add a new story
- "remove PRND-{tbd-4}" - Remove a story
- "done" - Finalize and proceed to /sync-jira

What would you like to adjust?
```

### 6. Tagging for JIRA

Stories tagged with `:jira:` will be synced to JIRA.
Child implementation stories (created during /start-story) do NOT get this tag.

```markdown
### [BACKLOG] PRND-{tbd-1}: Story title :jira:
<!-- This will be synced to JIRA -->

### [BACKLOG] #1: Child story from breakdown
<!-- This stays local, NOT synced to JIRA -->
```

## Output Format

### Initial Analysis

```
/groom-stories PRND-12345

Loading epic context...
  Epic folder: dnext-dev-support/epics/PRND-12345-trouble-ticket-api/
  SRS: srs-extract.md (2,450 lines)
  Feature design: feature-design.md (exists)
  Modules: DPOMS

Analyzing SRS...
  Functional requirements: 12
  Business validation rules: 8
  API endpoints: 4
  Data model changes: 2

Generating story breakdown...
```

### Story Creation

```
Created: stories.md

Stories:
  1. PRND-{tbd-1}: GET /troubleTicket/{id} [Medium, 4-6h]
  2. PRND-{tbd-2}: POST /troubleTicket [High, 8-12h]
  3. PRND-{tbd-3}: State transition validation [Medium, 4-6h]
  4. PRND-{tbd-4}: Event publishing [Low, 2-4h]

Total estimated effort: 18-28 hours

Ready to iterate or finalize?
Type "done" when ready for /sync-jira
```

### Finalization

```
> done

Stories finalized:
  4 stories tagged :jira: ready for sync

Run: /sync-jira PRND-12345
```

## Story Attributes

Each story includes:

| Attribute | Description |
|-----------|-------------|
| Status | [BACKLOG] initially |
| ID | PRND-{tbd-N} (placeholder until JIRA sync) |
| Title | Descriptive action-oriented title |
| Tags | `:jira:` for JIRA-sync stories |
| Modules | Affected module(s) |
| Complexity | Low/Medium/High |
| Hours | Estimated effort range |
| Description | What needs to be done |
| Acceptance Criteria | Verifiable outcomes |
| SRS References | Links to requirements |

## Agent Used

This command invokes the **story-groomer** agent which:
- Parses SRS content
- Identifies logical story boundaries
- Estimates complexity
- Facilitates iteration with Tech Lead
- Maintains .stories.md format

## Prerequisites

- Epic folder exists (`/start-feature` completed)
- `srs-extract.md` populated with SRS content
- Tech Lead available for interactive session

## See Also

- `/start-feature` - Initialize epic folder
- `/sync-jira` - Push groomed stories to JIRA
- `/list-stories` - View stories across epics
- `story-groomer` agent - Underlying implementation
