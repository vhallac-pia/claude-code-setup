# Extract Work Log

Extract a work log entry from the current session, merging any context compressions into a single logical session.

## Session Identification

1. **Locate session files**:
   - Convert current working directory to project path: `-` prefix + path with `/` replaced by `-`
   - Session directory: `~/.claude/projects/<project-path>/`
   - Session files: `*.jsonl` (exclude `agent-*.jsonl`)

2. **Determine session boundary** (in order of precedence):
   - If `session-start-marker.txt` exists, use the session file and timestamp from it
   - Otherwise, identify sessions from today (or most recent work day) by file modification time
   - Group sessions that are part of the same logical work session (within a few hours of each other)

3. **Detect context compressions**:
   - Look for user messages containing "This session is being continued from a previous conversation"
   - These indicate the session was split due to context limits
   - Merge all related session files into one logical session

## Data Extraction

For each session file, parse JSONL and extract:

1. **User messages** (`"type":"user"`):
   - Extract `message.content` and `timestamp`
   - Skip system commands (`/login`, `/clear`, etc.)
   - Skip system reminders (`<system-reminder>`)

2. **Key outcomes**:
   - Files created/modified (from tool calls)
   - Commits made (from git commands)
   - External resources created (Confluence pages, etc.)

3. **Session metadata**:
   - First user message timestamp → session start
   - Last message timestamp → session end
   - Session date for the heading

## Work Log Format

Generate a work log entry following the format in `~/.claude/docs/<project>_work-log.md`:

```markdown
## YYYY-MM-DD - Brief Session Title

### Goals

- [Inferred from initial user messages]

### Prompt Patterns

#### Pattern: [Task Type]

**Context**: [What preceded this prompt]

**Prompt**:
> [User prompt - exact or paraphrased]

**Why it worked**: [Analysis of effectiveness]

**Outcome**: [Brief result]

---

[Repeat for 3-5 notable patterns]

### Outcomes

**Files created:**

- [list]

**Files modified:**

- [list]

**Commits:**

```
[commit hashes and messages]
```

### Lessons Learned

1. [Key insight from session]
```

## Selection Criteria for Prompt Patterns

**Always include first (most important)**:

- **The initial task-defining prompt** - The prompt that kicked off the session's main work. This establishes scope, focus areas, and success criteria. Often the most valuable pattern for others to learn from.

**Also include**:

- Prompts that required iteration (show refinement)
- Effective single-shot prompts
- User corrections that improved output
- Interrupt/redirect patterns
- Collaborative decision-making
- Domain clarification exchanges

**Skip**:

- Routine confirmations ("yes", "ok", "good")
- Technical errors unrelated to prompting
- Redundant examples of same pattern
- Context compression continuations (unless they impacted work visibly)

## Output

1. **Display the generated entry** for user review
2. **Ask user** for:
   - Session title confirmation/edit
   - Any patterns to add/remove
   - Permission to append to work log file
3. **On confirmation**:
   - Append entry to `~/.claude/docs/<project-slug>_work-log.md`
   - Place after the Overview section, before existing entries
   - Clean up `session-start-marker.txt` if it exists

## Project Slug Detection

Derive project slug from:
1. Git remote URL if available (repository name)
2. Otherwise, last component of working directory path
