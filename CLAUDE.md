# D-NEXT AI-Assisted Development - Claude Code Instructions

## Git Operations

- `GITHUB_TOKEN` env var contains API token for remote operations (push, pull, clone)
- When modifying CLAUDE.md, value brevity - minimal words, maximum clarity

## Commit Messages

- First line: 50-70 chars, focus on what and why
- MUST keep commits atomic, grouped by purpose
- MUST separate: story/spec commits from code commits, refactor from usage
- MUST check if there are modified files in repo before attempting a commit
- Describe what IS in the commit, not what WILL BE done (no future plans or next steps)
- Keep message factual and focused on the changes made
- Summarize changes at a high level - brevity is a quality measure
- MUST NOT add "generated with claude code" or similar to commit messages

## Story Workflow

### Story States
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

### Story Lifecycle
1. **Pick JIRA story** from BACKLOG, move to DOING
2. **Create branch** in module repo: `feature/PRND-xxxxx` or `bugfix/PRND-xxxxx`
3. **Generate implementation guide** using context sources (see below)
4. **Break down into child stories** (#1, #2, #3) if too complex
5. **Implement each child story** on the same branch (test-first per child)
6. **Review loop** for each child (may create review items #1.R1, #2.R1)
7. **Final PR review** when all child stories DONE
8. **Merge PR**, parent JIRA story and all children marked DONE
9. **Update module design.md** with implementation summary

### Parent Story as Tracker
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

### Resume Behavior
If developer pauses mid-story:

- Child story states preserved in .stories.md
- Running `/start-story PRND-xxxxx` again detects existing children
- Prompts to continue with next BACKLOG child

### Branch Rules
- MUST create branch only for JIRA stories (PRND-xxxxx), NOT for epics
- Branch format: `feature/PRND-xxxxx` or `bugfix/PRND-xxxxx`
- MUST NOT create separate branches for child stories (#1, #2) or review items (#1.R1)
- All child story work happens on the parent JIRA story branch
- MUST NOT create branches in dnext-dev-support repo (documentation only)

### Story Discovery During Development
When discovering beneficial work during implementation:
- **Technical work** (refactoring, tech debt): Inform user, add to BACKLOG in current .stories.md
- **Cross-module dependency**: Inform user, create JIRA enabler story, add HELD status to current story
- Add only sufficient detail when discovered. Full grooming happens at pickup.

## Technical Context Sources

When writing implementation guides or analyzing code, use these sources in order:

### 1. System-Wide Reference
- **Architecture guide**: `dnext-dev-support/architecture.md`
  - Component anatomy (3-domain architecture)
  - Request flow patterns
  - Core library overview
  - Project structure conventions
  - TMF API compliance rules
  - Links to detailed guides (model-mapping.md, event-publishing.md, etc.)

### 2. Current State (What IS)
- **Module design.md**: `{module-repo}/docs/design.md`
  - Current architecture, API surface, data models
  - Validation rules (BVRs + TVRs)
  - Recent changes
- **Exported services**: `{module-repo}/docs/exported-services.md`
  - Public API for other modules to consume
  - Contains "AI Usage Instructions" section

### 3. Target State (What's TO BE)
- **SRS extract**: `dnext-dev-support/epics/PRND-{epic}/srs-extract.md`
  - Business requirements from Confluence
  - Given/When/Then scenarios
  - BVR definitions
- **Feature design**: `dnext-dev-support/epics/PRND-{epic}/feature-design.md`
  - Technical approach for multi-module epics
  - Cross-module coordination
  - Only exists for multi-module epics
- **Story description**: In `.stories.md` file
  - Scoped TO BE for this specific story
  - Implementation guide once generated

### AI Usage Instructions
Many documents contain an "AI Usage Instructions" section with specific guidance:
- **exported-services.md**: How to integrate with the module
- **design.md**: Module-specific patterns to follow
- **architecture.md**: System-wide conventions

Always check for and follow these instructions when present.

### Context Loading Priority
1. Read current branch name to determine JIRA story ID
2. Locate story file: `dnext-dev-support/epics/PRND-{epic}-*/stories.md`
3. Read story metadata from YAML front matter and story section
4. Load architecture.md for system-wide patterns
5. Load module design.md from affected module
6. Load srs-extract.md and feature-design.md from epic folder
7. For dependencies: use module-catalog.md to find exported-services.md

## Repository Structure

### Two-Repository Workspace
AI-assisted development requires at minimum two repositories:

1. **dnext-dev-support** - Central development support repo (always present)
   ```
   dnext-dev-support/
   ├── agent-instructions.md      # AI agent directives
   ├── module-catalog.md          # Index of all modules
   ├── templates/                  # Document templates
   └── epics/                     # Active epic folders
       └── PRND-{epic}-{slug}/
           ├── srs-extract.md
           ├── feature-design.md   # If multi-module
           └── stories.md          # All stories for this epic
   ```

   Note: When all stories in an epic are DONE and merged, delete the epic folder.
   Git history preserves everything if needed later.

2. **Module repository** - The primary code being worked on
   ```
   {module}/
   ├── src/
   ├── docs/
   │   ├── design.md              # Module design (living)
   │   └── exported-services.md   # Public API
   └── pom.xml
   ```

3. **Additional repositories** (optional) - Dependencies or related modules
   - Common libraries (e.g., `common-core`) when modifying shared code
   - Related modules when coordinating multi-module changes
   - Added to workspace as needed for the current story

### File Locations Reference
| Document | Location |
|----------|----------|
| Architecture guide | `dnext-dev-support/architecture.md` |
| Module catalog | `dnext-dev-support/module-catalog.md` |
| Stories file | `dnext-dev-support/epics/PRND-{epic}-{slug}/stories.md` |
| SRS extract | `dnext-dev-support/epics/PRND-{epic}-{slug}/srs-extract.md` |
| Feature design | `dnext-dev-support/epics/PRND-{epic}-{slug}/feature-design.md` |
| Module design | `{module-repo}/docs/design.md` |
| Exported services | `{module-repo}/docs/exported-services.md` |

## Code Quality

- MUST use design.md and srs-extract.md before code when reasoning
- MUST compile cleanly: no errors, no warnings
- MUST use merge-readiness-checker agent before merge instructions/actions
- MUST do clean build and test for final verification before branch merge
- MUST update module design.md when story marked DONE (before PR)

## Markdown Formatting

- MUST add a blank line before lists, code blocks, and tables

## Story File Conventions (stories.md)

### Vertical Slice Stories
Each JIRA story must be a **vertical slice** - delivering end-to-end functionality for a use case:
- Based on use cases, not technical layers
- Includes all FRs and BVRs for that use case
- Independently deployable and testable
- Split large use cases progressively: happy path first, validation/exceptions later

### File Structure
- One `stories.md` per epic folder
- YAML front matter with epic context
- All stories at H1 level (JIRA, child, and review items)
- Logical hierarchy via placement (child after parent, review after child)

### Heading Levels
- `#` (H1): All stories (JIRA, child, review items)
- `##` (H2): Implementation Guide (subsection under JIRA story)

### Status in Headings with Anchors
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

### Placement Rules
- **Child stories**: Place immediately after parent JIRA story (after Implementation Guide)
- **Review items**: Place immediately after the child story they belong to

### Metadata Format
```markdown
<!-- key: value | key2: value2 -->
```

Common keys: `modules`, `complexity`, `hours`, `use_case`, `depends_on`, `held_by`, `blocks`, `severity`

### MODULES Naming
- **Services**: Use commercial name (e.g., `DPOMS`, `DPOM-OFS`, `DPCI`)
- **Libraries**: Use repository name (e.g., `common-core`, `dnext-commons`)

### Tags
Tags appear at end of headings: `:foundation:`, `:review:`, `:urgent:`, `:backend:`

## Agent Usage

Agents are invoked for specific workflow stages. Key agents:

| Agent | When to Use |
|-------|-------------|
| story-implementation-planner | When picking up a BACKLOG story to generate implementation guide |
| code-reviewer | When child story implementation complete, before REVIEWING |
| merge-readiness-checker | Before creating PR or providing merge instructions |
| specs-updater | When story DONE, to update module design.md |

See `agents/` directory for full agent definitions.
