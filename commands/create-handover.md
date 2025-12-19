# Create Session Handover Document

Generate a structured handover document capturing the essential context from recent work for resuming in a fresh session.

## Purpose

When working on complex tasks that span multiple sessions or require context handoff, this command creates a concise handover document containing:
- Document locations (files created/modified)
- External resources used (Confluence pages, GitHub repos, APIs)
- Key knowledge/context acquired during the work
- Next steps for continuing the work
- Critical references for context rebuilding

## Instructions

1. **Ask the user for handover scope**:
   - **Task/topic description**: What area of work to capture (e.g., "SRS conversion", "API generator", "BMAD analysis")
   - **Time window** (optional): How far back to look (default: current session, options: last N hours, since specific message)
   - **Output location** (optional): Where to save (default: `temp/<topic>_HANDOVER_<date>.md`)

2. **Analyze the specified scope**:
   - Review messages and tool usage within the time window
   - Identify files created, modified, or heavily referenced
   - Note external resources accessed (Confluence pages with IDs, GitHub repos, web resources)
   - Extract key decisions, insights, or context learned
   - Identify pending tasks or logical next steps

3. **Generate handover document** with this structure:

```markdown
# <Task/Topic> - Session Handover

**Date**: <YYYY-MM-DD>
**Task**: <Brief description>

---

## Documents and Locations

### Input Documents
- **<Name>**: `<path>`
  - <Key details: size, source, purpose>

### External Resources Used
- **<Resource Type>**:
  - Page/Repo ID: <ID>
  - Location: <URL or description>
  - Purpose: <Why referenced>

### Output Documents
- **<Name>**: `<path>`
  - <Key details: size, what it contains>

---

## Key Context: What Was "Learned"

### 1. <Major Topic Area>
<Bullet points of key insights, decisions, or discoveries>

### 2. <Another Topic Area>
<More context>

### 3. <Technical Details>
<Specific patterns, constraints, or architectural decisions>

---

## Next Steps (If Continuing This Work)

1. **<Action>**: <Description and rationale>
2. **<Action>**: <Description and rationale>

---

## Critical References for Context Rebuilding

**To quickly rebuild context:**

1. Read: `<path>` - <What to focus on>
2. Review: <External resource> - <Why important>
3. Key Constraints to Remember:
   - <Constraint 1>
   - <Constraint 2>

---

**Session End**: <timestamp>
**Status**: <Current state: complete, in-progress, blocked, etc.>
```

4. **Save the document** and report:
   - File path where saved
   - File size
   - Brief summary of what was captured

## Example Usage

**User**: `/create-handover`

**Assistant**: "I'll create a handover document for your recent work. Please provide:
1. What task/topic should I capture? (e.g., 'API generator refactoring', 'SRS template migration')
2. Time window? (default: current session, or specify 'last 3 hours', 'since message #15')
3. Output location? (default: temp/<topic>_HANDOVER_<date>.md)"

**User**: "SRS conversion work, just the last part of this session after we finished the critique review"

**Assistant**: [Analyzes messages and creates handover document focusing on template migration work]

## Prerequisites

- Substantial work completed in session (multiple tool calls/file operations)
- Clear topic boundary (handover for focused work, not general chat)
- Session is recent (within current conversation context)

## Session Analysis Method

This command relies on **in-context session memory**. Best results when:
- Key work happened recently (not buried under context compressions)
- User can point to specific time boundary ("since message X", "last 2 hours")
- Session has clear beginning/end for the topic

**Limitation**: Unlike `/extract-work-log` which uses session file analysis, this command works from current conversation memory. For work spanning multiple sessions or after context compressions, you may need to manually reference key decisions.

## Guidelines

### Token Budget: 2000 tokens maximum

**Purpose**: Keep handover concise to reduce context load and prevent hallucinations when loaded into fresh sessions.

**Target structure**:
- Documents section: 300-400 tokens (file paths + brief metadata)
- Key Context: 800-1000 tokens (5-10 bullet points of critical insights)
- Next Steps: 200-300 tokens (3-5 actionable items)
- Critical References: 200-300 tokens (3-5 key files/constraints)

**Enforcement**:
- Prioritize unique insights over recoverable details
- Reference files instead of describing their contents
- Use bullets over paragraphs
- Limit "Key Context" subsections to 3-4 major areas
- Each bullet point: 1-2 sentences maximum

### Content Guidelines

- **Be ruthlessly concise**: Capture essentials only, not exhaustive details
- **Focus on non-obvious context**: Things not easily recovered from file reading
- **Include specific identifiers**: Page IDs, commit hashes, exact file paths
- **Prioritize actionability**: "Next Steps" should be clear and actionable
- **Make it scannable**: Use headings, bullets, and clear structure

## What NOT to Include

- Routine messages or confirmations ("yes", "ok")
- Full file contents (reference paths instead)
- Detailed code snippets (unless critical to understanding)
- Implementation details available in committed code
- Temporary debugging or exploration that didn't lead anywhere

## Error Handling

### No Relevant Content Found

If scope analysis finds minimal substantive content:
- Report what was searched (time window, message count)
- Ask user to clarify scope or expand time window
- Offer to capture available context anyway with warning about sparseness

### Output Directory Missing

If `temp/` directory doesn't exist:
- Create it automatically
- Report creation to user

### File Already Exists

If handover file already exists at target path:
- Append numeric suffix: `_2`, `_3`, etc.
- Report both original and new filename to user

### Token Budget Exceeded

If generated handover exceeds 2000 tokens:
- Report token count and overage
- Offer to regenerate with stricter pruning
- Ask user which section to compress (usually "Key Context")

## Output Location Convention

Default: `temp/<topic-slug>_HANDOVER_<YYYY-MM-DD>.md`

Where `<topic-slug>` is derived from the task description (lowercase, hyphens for spaces).

Examples:
- `temp/srs-conversion_HANDOVER_2024-12-19.md`
- `temp/api-generator_HANDOVER_2024-12-20.md`
- `temp/bmad-analysis_HANDOVER_2024-12-15.md`
