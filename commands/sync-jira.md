# Sync JIRA Command

Push groomed stories from .stories.md to JIRA. One-way sync, multiple invocations supported.

## Usage

```
/sync-jira <epic-id> [options]
```

**Parameters:**
- `epic-id`: Required. The JIRA epic ID (e.g., PRND-12345)

**Options:**
- `--dry-run`: Preview changes without creating JIRA issues
- `--status-only`: Only sync status updates, not descriptions
- `--force`: Sync even if stories haven't changed

**Examples:**
```
/sync-jira PRND-12345
/sync-jira PRND-12345 --dry-run
/sync-jira PRND-12345 --status-only
```

## What This Does

### 1. Identifies Stories to Sync

- Reads `stories.md` from the epic folder
- Finds stories tagged with `:jira:`
- Excludes child stories (#1, #2) - these stay local
- Compares with existing JIRA issues

### 2. Sync Operations

**For new stories (PRND-{tbd-N}):**
- Creates JIRA issue under epic
- **Links story to parent epic** in JIRA
- Updates .stories.md with real JIRA ID
- Sets initial status, description, metadata
- **Creates dependency links** (if `depends_on` metadata exists)

**For existing stories (already have JIRA ID):**
- Updates description if changed
- Updates status if changed
- Syncs metadata (start time, duration)
- **Updates dependency links** if changed

### 3. What Gets Synced

| Field | Synced | Notes |
|-------|--------|-------|
| Title | Yes | Story title |
| Description | Yes | Description + Acceptance Criteria + Use Case reference |
| Status | Yes | BACKLOG-->To Do, DOING-->In Progress, DONE-->Done |
| Epic Link | Yes | Story linked to parent epic |
| Dependencies | Yes | `depends_on` metadata → JIRA "is blocked by" links |
| Start Time | Yes | When story moved to DOING |
| Duration | Yes | Time spent on story |
| Implementation Guide | No | Too detailed for JIRA |
| Review Items | No | Internal tracking only |
| Child Stories | No | Not tagged :jira: |

### 4. Status Mapping

| .stories.md | JIRA Status |
|-------------|-------------|
| BACKLOG | To Do |
| DOING | In Progress |
| HELD | Blocked |
| REVIEWING | In Review |
| DONE | Done |

## Output Format

### Dry Run

```
/sync-jira PRND-12345 --dry-run

Analyzing stories for PRND-12345...

Stories to CREATE in JIRA:
  PRND-{tbd-1} --> "Create TroubleTicket (Happy Path)"
  PRND-{tbd-2} --> "Create TroubleTicket (Full Validation)"
                   depends_on: PRND-{tbd-1}
  PRND-{tbd-3} --> "Update TroubleTicket Status"
  PRND-{tbd-4} --> "Search TroubleTickets"

Epic link: All stories will be linked to PRND-12345

Dependencies to CREATE:
  PRND-{tbd-2} is blocked by PRND-{tbd-1}

Stories to UPDATE in JIRA:
  (none)

Stories unchanged:
  (none)

Dry run complete. Run without --dry-run to execute.
```

### Initial Sync (Create)

```
/sync-jira PRND-12345

Creating JIRA issues...

Created:
  PRND-{tbd-1} --> PRND-45001: Create TroubleTicket (Happy Path)
  PRND-{tbd-2} --> PRND-45002: Create TroubleTicket (Full Validation)
  PRND-{tbd-3} --> PRND-45003: Update TroubleTicket Status
  PRND-{tbd-4} --> PRND-45004: Search TroubleTickets

Epic links:
  All 4 stories linked to epic PRND-12345

Dependencies created:
  PRND-45002 is blocked by PRND-45001

Updated .stories.md with JIRA IDs.

4 stories synced to JIRA.
```

### Status Update Sync

```
/sync-jira PRND-12345

Syncing status updates...

Updated:
  PRND-45001: BACKLOG --> DOING (In Progress)
    Start time: 2025-11-28 09:30
  PRND-45002: DOING --> DONE (Done)
    Duration: 6h 45m

2 stories updated in JIRA.
```

### No Changes

```
/sync-jira PRND-12345

All stories are in sync with JIRA.
No updates needed.
```

## .stories.md Updates

After sync, stories are updated with JIRA IDs:

**Before:**
```markdown
### [BACKLOG] PRND-{tbd-1}: GET /troubleTicket/{id} endpoint :jira:
<!-- modules: DPOMS | complexity: medium -->
```

**After:**
```markdown
### [BACKLOG] PRND-45001: GET /troubleTicket/{id} endpoint :jira:
<!-- jira: PRND-45001 | modules: DPOMS | complexity: medium -->
```

## Metadata Tracking

When stories move to DOING, tracking starts:

```markdown
### [DOING] PRND-45001: GET /troubleTicket/{id} endpoint :jira:
<!-- jira: PRND-45001 | modules: DPOMS | started: 2025-11-28T09:30:00 -->
```

When stories complete:

```markdown
### [DONE] PRND-45001: GET /troubleTicket/{id} endpoint :jira:
<!-- jira: PRND-45001 | modules: DPOMS | started: 2025-11-28T09:30 | duration: 6h45m -->
```

## Error Handling

### JIRA Connection Failed

```
/sync-jira PRND-12345

Error: Unable to connect to JIRA.

Please verify:
- Atlassian MCP is configured
- Authentication is valid
- Network connectivity

Retry with: /sync-jira PRND-12345
```

### Partial Failure

```
/sync-jira PRND-12345

Created:
  PRND-{tbd-1} --> PRND-45001

Failed:
  PRND-{tbd-2}: Error creating issue - Required field missing: "component"
  PRND-{tbd-3}: Error creating issue - Epic not found

2 stories failed. Fix issues and retry.
```

### Story Already Exists

```
/sync-jira PRND-12345

Warning: PRND-45001 already exists in JIRA but not in .stories.md

Options:
1. Link existing JIRA issue to .stories.md story
2. Skip this story
3. Abort sync

Choice [1/2/3]:
```

## What Does NOT Sync

These stay local in .stories.md only:

- **Child stories (#1, #2, #3)**: Implementation breakdown
- **Review items (#1.R1)**: Code review findings
- **Implementation guide**: Too detailed
- **Technical notes**: Internal context

This keeps JIRA clean while preserving detailed tracking locally.

## Multiple Syncs

The command is designed for multiple invocations:

1. **Initial sync**: Create JIRA issues from groomed stories
2. **Status updates**: Sync as work progresses
3. **Completion**: Final status sync when done

Each sync is idempotent - running twice produces same result.

## Agent Used

This command invokes the **story-tracker** agent (JIRA sync mode) which:
- Connects to Atlassian MCP
- Creates/updates JIRA issues
- Maintains bidirectional ID mapping
- Tracks sync state

## Prerequisites

- Stories groomed (`/groom-stories` completed)
- Stories tagged with `:jira:`
- Atlassian MCP configured
- Write access to JIRA project

## See Also

- `/groom-stories` - Create story breakdown
- `/start-story` - Pick up a story for implementation
- `/list-stories` - View stories with sync status
- `story-tracker` agent - Underlying implementation
