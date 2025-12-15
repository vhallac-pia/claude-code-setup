# Extract Work Log

Extract a work log entry from the current session, merging any context compressions into a single logical session.

## Session Identification

1. **Locate session files**:

   - Convert current working directory to project path: `-` prefix + path with `/` replaced by `-`
   - Session directory: `~/.claude/projects/<project-path>/`
   - Session files: `*.jsonl` (exclude `agent-*.jsonl`)

2. **Determine session boundary** (in order of precedence):

   - If `session-start-marker.txt` exists, use the session file and timestamp from it (from /start-work command)
   - Otherwise, apply best-effort automatic discovery (for when work start was not marked):
     - Identify sessions from today (or most recent work day) by file modification time
     - Group sessions: same day within 2 hours, or different days (part of same logical work)

3. **Detect context compressions**:

   - Look for user messages containing "This session is being continued from a previous conversation"
   - These indicate the session was split due to context limits
   - Merge all related session files into one logical session

## Data Extraction

**Use the extraction script** (recommended):

```bash
python3 ~/.claude/scripts/extract_session_data.py <session-file> [session-file...]
```

This outputs JSON with:
- User messages (filtered, with timestamps)
- Session duration (with break detection)
- Files created/modified
- Commits made
- Tool usage summary

**Manual extraction** (if needed):

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

   - **Duration**: Calculate from the **initial task-defining prompt** to session end
     - Exclude breaks: gaps >30 min between messages with no tool activity
     - Exception: If a >30 min gap ends with a tool result (long build/test), count it as active work
     - Format duration as `Xh Ym`
   - Session date for the heading

## Work Log File

Work logs are organized weekly using ISO week numbers:

- File pattern: `~/.claude/docs/<project-slug>_work-log_YYYY-WXX.md`
- Example: `ai-sdlc_work-log_2025-W49.md`
- Determine current week: `date +%Y-W%V`
- If the weekly file doesn't exist, create it with header: `# <Project Name> Work Log - YYYY-WXX`
- Example header: `# AI-SDLC Work Log - 2025-W49`

## Work Log Format

Generate a work log entry following this format:

```markdown
## YYYY-MM-DD - Brief Session Title

**Duration:** Xh Ym

### Goals

- [Inferred from initial user messages]

### Prompt Patterns

#### Pattern: [Task Type]

**Context**: [What preceded this prompt]

**Prompt**:
> [User prompt - exact or paraphrased]

**Why it worked**: [Analysis of effectiveness]

**Outcome**: [Brief result]

[Repeat for notable patterns]

### Prompt Anti-Patterns

#### Pattern: [Task Type]

**Context**: [What preceded this prompt]

**Prompt**:
> [User prompt - exact or paraphrased]

**Why it was considered bad**: [Analysis of ineffectiveness]

**Outcome**: [Brief result]

[Repeat for notable anti-patterns]

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

**IMPORTANT**: The work log entry format ends here. Do NOT include the reference material below in the generated work log output.

---

# Reference Material (For Command Execution Only)

The sections below provide guidance for selecting and rating prompts. Use this material when executing the command, but DO NOT include it in the generated work log entry.

## Selection Criteria for Prompt Patterns

### Systematic Rating Methodology

**Rate all substantive prompts against these 10 core principles** (in priority order):

1. **Clarity & Specificity** - Clear instructions, specific success criteria, unambiguous requirements
2. **Context Provision** - Relevant background, constraints, current state, what's changed
3. **Constraint Definition** - Boundaries, requirements, what to avoid, scope limits
4. **Output Specification** - Desired format, structure, level of detail
5. **Verification & Validation** - Cross-checking, review criteria, quality gates
6. **Iterative Refinement** - Building on previous responses, course correction based on results
7. **Task Decomposition** - Breaking complex work into manageable, sequenced steps
8. **Source Grounding** - Pointing to authoritative references rather than relying on AI memory
9. **Role/Capability Matching** - Using appropriate models/agents for specific tasks
10. **Example Provision** - Few-shot learning, concrete demonstrations

**Scoring Process**:

1. Rate each prompt 0-10 on each principle
2. Apply 5% penalty per priority rank:
   - Principle 1: 100% weight (×1.00)
   - Principle 2: 95% weight (×0.95)
   - Principle 3: 90% weight (×0.90)
   - Principle 4: 85% weight (×0.85)
   - Principle 5: 80% weight (×0.80)
   - Principle 6: 75% weight (×0.75)
   - Principle 7: 70% weight (×0.70)
   - Principle 8: 65% weight (×0.65)
   - Principle 9: 60% weight (×0.60)
   - Principle 10: 55% weight (×0.55)
3. Calculate: Score = Σ(principle_rating × weight)
4. Rank prompts by final score
5. Identify first noticeable drop-off (typically 8-10+ point gap)
6. Select prompts above the drop-off as primary examples (maximum 10)

**Always include**:

- **The initial task-defining prompt** - Usually scores highest, establishes scope and success criteria
- Top-scoring prompts from systematic rating (maximum 10, typically 4-7 before first major drop-off)

**Skip**:

- Routine confirmations ("yes", "ok", "good")
- Technical errors unrelated to prompting
- Redundant examples of same pattern
- Context compression continuations (unless they impacted work visibly)

## Selection Criteria for Prompt Anti-Patterns

### Violation Scoring Methodology

Anti-patterns are user prompts that violated prompt engineering principles, not necessarily prompts that led to AI errors.

**Calculate violation scores** by inverting the principle ratings:

1. Rate each prompt 0-10 on each principle (same as patterns)
2. Calculate violation for each principle: `Violation = (10 - rating)`
3. Apply same 5% penalty weights as patterns:
   - Principle 1 (Clarity): ×1.00
   - Principle 2 (Context): ×0.95
   - Principle 3 (Constraint): ×0.90
   - Principle 4 (Output Spec): ×0.85
   - Principle 5 (Verification): ×0.80
   - Principle 6 (Iterative): ×0.75
   - Principle 7 (Decomposition): ×0.70
   - Principle 8 (Source Grounding): ×0.65
   - Principle 9 (Role Matching): ×0.60
   - Principle 10 (Examples): ×0.55
4. Calculate: `Violation_Score = Σ((10 - principle_rating) × weight)`
5. Rank prompts by violation score (highest = worst)
6. Identify first noticeable drop-off (typically 2-3 point gap)
7. Select top 3-5 highest violation prompts as anti-patterns

**Common anti-pattern characteristics:**
- Too vague (low Clarity score → high violation)
- Missing constraints (low Constraint score → high violation)
- No output specification (low Output Spec → high violation)
- Ignores context (low Context Provision → high violation)
- Wrong capability matching (e.g., agent vs model switch)

**Note**: Low-scoring prompts aren't always anti-patterns - some may be simple but effective. Focus on prompts where violations of high-priority principles (Clarity, Context, Constraints) led to poor outcomes.

## Correction Penalty System

### Philosophy

**Core Principle**: The AI doesn't make mistakes on its own - ambiguous or incomplete prompts are the user's responsibility. Prompts that require corrections should be penalized in selection scoring.

### Detection and Classification

**Step 1: Identify correction prompts** using these markers:
- Explicit: "fix", "wrong", "incorrect", "mistakenly", "should have", "should mention"
- Clarification: "is ambiguous", "is unclear", "easy to misinterpret"
- Refinement: "that's not right", "actually", "I meant", "clarification"

**Step 2: Classify severity:**
- **Minor**: Formatting, typos, minor clarifications
- **Moderate**: Ambiguity requiring re-work, missing constraints
- **Major**: Fundamental misunderstanding, wasted significant work

**Step 3: Attribute to source prompt:**
1. Try semantic matching: Search backward in task for keywords from correction
2. Fall back to immediate predecessor if no match
3. Identify collateral: All prompts between source and correction in same task

### Penalty Structure

**Configuration** (tested and validated):
- Base penalty: 2%
- Target multiplier: 5× (prompt that caused the issue)
- Collateral multiplier: 1× (prompts in the confusion zone)
- Additive multiplier: 5× (for anti-pattern violations)

**Penalty values:**

| Severity | Target Pattern | Target Anti-Pattern | Collateral Pattern | Collateral Anti-Pattern |
|----------|---------------|---------------------|--------------------|-----------------------|
| Minor    | ×0.95         | +3                  | ×0.99              | +1                    |
| Moderate | ×0.90         | +5                  | ×0.98              | +1                    |
| Major    | ×0.80         | +10                 | ×0.96              | +2                    |

### Application Process

**Using the scoring script** (recommended):

1. Rate all prompts (CSV format):
```csv
ID,Timestamp,Prompt_Preview,C1_Clarity,C2_Context,C3_Constraint,C4_Output,C5_Verify,C6_Iterate,C7_Decomp,C8_Source,C9_Role,C10_Example
```

2. Identify corrections and create JSON:
```json
[
    {
        "number": 51,
        "severity": "moderate",
        "target_pid": "P12",
        "collateral_pids": ["P13", "P14", "P15"]
    }
]
```

3. Run scoring script:
```bash
python3 ~/.claude/scripts/score_prompts_with_corrections.py ratings.csv corrections.json
```

**Manual application** (if needed):

For each correction:
1. Find target prompt (semantic match or predecessor)
2. Apply target penalty: `adjusted_score = base_score × target_mult`
3. Apply target violation: `adjusted_violation = base_violation + target_add`
4. For each collateral prompt:
   - Apply collateral penalty: `adjusted_score = base_score × collateral_mult`
   - Apply collateral violation: `adjusted_violation = base_violation + collateral_add`

### Expected Impact

**Pattern selection:**
- Demotes prompts that caused confusion/rework
- Promotes clear, unambiguous prompts that worked first time
- ~10% improvement in pattern selection accuracy

**Anti-pattern selection:**
- Elevates prompts that required corrections
- Highlights root causes of confusion
- Makes weak prompts stand out more clearly

**Convergence validation:**
- Base penalty: 2% and 3% produce identical selections (2% is sufficient)
- Target multiplier: 5× and 6× produce identical selections (5× is sufficient)
- Lower values (1%, 4×) fail to exclude corrected prompts

### Example

```
Correction at prompt #51: "Gap 5 description is easy to misinterpret"
Severity: moderate
Target: P12 (prompt #17, first mentioned Gap 5)
Collateral: P13, P14, P15 (prompts between #17 and #51)

Results:
- P12: 54.6 → 48.2 (×0.90) | Violation: 22.9 → 28.9 (+5) → Anti-pattern #3
- P13: 57.9 → 55.6 (×0.98) | Violation: 19.6 → 21.6 (+1) → Still pattern
- P14: 57.0 → 54.7 (×0.98) | Violation: 20.5 → 22.5 (+1) → Borderline
- P15: 50.1 → 48.1 (×0.98) | Violation: 27.4 → 29.4 (+1) → Anti-pattern #2
```

### When to Skip

**Don't apply corrections for:**
- Iterative refinement (user expanding scope, not fixing mistakes)
- AI tool errors (permissions, environment issues)
- Scope evolution ("actually, let's also add X")
- User changing their mind (preference, not correction)

**Only penalize when:**
- Correction traces to prompt ambiguity/incompleteness
- Re-work was required due to unclear instructions
- Follow-up clarification was needed to get correct result

## Output

1. **Display the generated entry** for user review
2. **Ask user** for:
   - Session title confirmation/edit
   - Any patterns to add/remove
   - Permission to append to work log file
3. **On confirmation**:
   - Determine current ISO week (`date +%Y-W%V`)
   - Append entry to `~/.claude/docs/<project-slug>_work-log_<week>.md`
   - If file doesn't exist, create with header: `# <Project> Work Log - <week>`
   - Place new entries at the TOP of the file (after header)
   - Clean up `session-start-marker.txt` if it exists

## Project Slug Detection

Derive project slug from:
1. Git remote URL if available (repository name)
2. Otherwise, last component of working directory path
