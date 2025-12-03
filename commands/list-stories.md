# List Stories Command

Display stories from .stories.md files across active epics.

## Usage

```
/list-stories [filter] [options]
```

**Parameters:**
- `filter`: Optional. Filter by status, epic, or tag

**Options:**
- `--epic <id>`: Show stories for specific epic only
- `--jira`: Show only JIRA-synced stories
- `--local`: Show only local stories (not synced)

**Examples:**
```
/list-stories
/list-stories BACKLOG
/list-stories --epic PRND-12345
/list-stories DOING --jira
/list-stories :urgent:
```

## What This Does

### 1. Scans Epics

- Reads all folders in `dnext-dev-support/epics/`
- Parses each `PRND-*.stories.md` file
- Extracts story metadata and status

### 2. Filters and Groups

**Default view (no filter):**
- Shows BACKLOG, DOING, REVIEWING stories
- Groups by status
- Sorted by priority within each group

**Filtered view:**
- Status filter: BACKLOG, DOING, HELD, REVIEWING, DONE
- Epic filter: --epic PRND-xxxxx
- Tag filter: :jira:, :urgent:, :blocked:

### 3. Displays Results

For each story shows:
- Story ID (JIRA or local)
- Status
- Title
- Epic
- Module(s)
- Tags
- Estimates/duration

## Output Format

### Default View

```
/list-stories

Active Stories
═══════════════════════════════════════════════════════════════

DOING (In Progress)
────────────────────────────────────────────────────────────
  PRND-45001  GET /troubleTicket/{id} endpoint    [DPOMS]
              Epic: PRND-12345 | Started: 2h ago | :jira:
              └─ #1: Implement controller         [DOING]
              └─ #2: Add validation               [BACKLOG]

BACKLOG (Ready to Start)
────────────────────────────────────────────────────────────
  PRND-45002  POST /troubleTicket endpoint        [DPOMS]
              Epic: PRND-12345 | Est: 8-12h | :jira:

  PRND-45003  State transition validation         [DPOMS]
              Epic: PRND-12345 | Est: 4-6h | :jira:

  PRND-45004  Event publishing                    [DPOMS, common-events]
              Epic: PRND-12345 | Est: 2-4h | :jira:

REVIEWING (Code Review)
────────────────────────────────────────────────────────────
  (none)

═══════════════════════════════════════════════════════════════
Summary: 1 DOING, 3 BACKLOG, 0 REVIEWING

Next recommended: PRND-45002 (highest priority BACKLOG)
Use: /start-story PRND-45002
```

### Epic-Specific View

```
/list-stories --epic PRND-12345

Stories for PRND-12345: Trouble Ticket API Enhancement
═══════════════════════════════════════════════════════════════

JIRA Stories (synced)
────────────────────────────────────────────────────────────
  [DOING]     PRND-45001  GET /troubleTicket/{id}   [4-6h]
  [BACKLOG]   PRND-45002  POST /troubleTicket       [8-12h]
  [BACKLOG]   PRND-45003  State validation          [4-6h]
  [BACKLOG]   PRND-45004  Event publishing          [2-4h]

Local Stories (not synced)
────────────────────────────────────────────────────────────
  [DOING]     #1          Implement controller
  [BACKLOG]   #2          Add validation
  [BACKLOG]   #1.R1       Fix error handling        :review:

═══════════════════════════════════════════════════════════════
Total: 4 JIRA stories, 3 local stories
Est. remaining: 14-22h
```

### Status Filter

```
/list-stories DOING

Stories In Progress
═══════════════════════════════════════════════════════════════

Epic: PRND-12345 (Trouble Ticket API)
  PRND-45001  GET /troubleTicket/{id} endpoint
              Started: 2025-11-28 09:30 (2h 15m ago)
              Child stories: 1 DOING, 1 BACKLOG

Epic: PRND-12400 (Product Inventory)
  PRND-45100  Implement bulk update API
              Started: 2025-11-27 14:00 (20h ago)
              Child stories: 2 DONE, 1 REVIEWING

═══════════════════════════════════════════════════════════════
2 stories in progress across 2 epics
```

### Tag Filter

```
/list-stories :urgent:

Urgent Stories
═══════════════════════════════════════════════════════════════

  [DOING]     PRND-45001  GET /troubleTicket/{id}   :urgent: :jira:
              Epic: PRND-12345 | DPOMS

  [BACKLOG]   PRND-45200  Fix authentication bug    :urgent: :jira:
              Epic: PRND-12500 | DPOMS

═══════════════════════════════════════════════════════════════
2 urgent stories found
```

## Story Status Indicators

| Icon | Status | Meaning |
|------|--------|---------|
| `[BACKLOG]` | Ready | Can be picked up with /start-story |
| `[DOING]` | Active | Work in progress |
| `[HELD]` | Blocked | Waiting on dependency |
| `[REVIEWING]` | Review | Code review in progress |
| `[DONE]` | Complete | Finished |

## Story Type Indicators

| Tag | Meaning |
|-----|---------|
| `:jira:` | Synced to JIRA |
| `:review:` | Review item from code review |
| `:urgent:` | High priority |
| `:blocked:` | Has blocking dependency |

## Child Story Display

For DOING stories, child stories are shown indented:

```
  PRND-45001  Parent story title
              └─ #1: Child story 1         [DONE]
              └─ #2: Child story 2         [DOING]
              └─ #3: Child story 3         [BACKLOG]
              └─ #2.R1: Review item        [BACKLOG] :review:
```

## Recommendations

Based on story states, provides suggestions:

**When BACKLOG has stories:**
```
Next recommended: PRND-45002 (highest priority)
Use: /start-story PRND-45002
```

**When DOING has multiple:**
```
Warning: 3 stories in progress
Consider completing current work before starting new
```

**When HELD stories exist:**
```
Blocked: PRND-45003 held by PRND-45002
Unblock by completing PRND-45002 first
```

## Agent Used

This command invokes the **story-tracker** agent which:
- Scans .stories.md files
- Parses story metadata
- Aggregates across epics
- Provides recommendations

## Prerequisites

- Epics exist in `dnext-dev-support/epics/`
- Stories created via `/groom-stories`

## See Also

- `/start-story` - Pick up a story
- `/groom-stories` - Create stories from SRS
- `/sync-jira` - Sync stories to JIRA
- `story-tracker` agent - Underlying implementation
