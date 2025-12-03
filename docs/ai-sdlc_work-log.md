# AI-SDLC Work Log

## Overview

This file documents AI-assisted development sessions for the AI-SDLC project. It serves two purposes:

1. **Historical record** of accomplishments across sessions
2. **Prompt engineering reference** for learning effective interaction patterns

### Instructions for AI Agents

When concluding a session or when asked to update this log, follow these guidelines:

**Adding a new session:**

1. Add a new H2 heading with format: `## YYYY-MM-DD - Brief Session Title`
2. Place new sessions at the TOP (after this Overview section)
3. Include these subsections:
   - `### Goals` - What the user wanted to accomplish
   - `### Prompt Patterns` - Effective prompts with context (see format below)
   - `### Outcomes` - What was accomplished, files changed
   - `### Lessons Learned` - Optional: iterations, corrections, anti-patterns

**Prompt Pattern format:**

```markdown
#### Pattern: [Task Type]

**Context**: [What preceded this prompt, why it was needed]

**Prompt**:
> [Exact or paraphrased user prompt]

**Why it worked**: [What made this prompt effective]

**Outcome**: [Brief result]
```

**What to capture:**

- Prompts that required iteration (show the refinement)
- Effective single-shot prompts (explain why they worked)
- User corrections that improved output
- Task delegation patterns (when user directed vs. AI proposed)

**What to skip:**

- Routine confirmations ("yes", "ok", "good")
- Technical errors unrelated to prompting (e.g., network issues)
- Redundant examples of the same pattern

---

## 2025-12-03 - SRS Conversion & Section 3.2 Standardization

### Goals

- Convert existing PRND-27183 (State Flow Specification) SRS to IEEE 29148 format
- Create real SRS example in Confluence using the template
- Restructure section 3.2 format to eliminate redundancy between Use Cases table and scenarios
- Propagate format changes to all templates

### Prompt Patterns

#### Pattern: Format Conversion with Architecture Change

**Context**: User wanted to convert an old-style SRS to IEEE 29148 format, but also change the solution from an add-on to standalone service.

**Prompt**:
> In confluence there is an SRS for state flow specification service... Can you convert it to our new IEEE format?

**Follow-up**:
> This SRS assumes that the solution will be an add-on to a configuration management service. Can you revise it to make it a stand-alone service?

**Why it worked**: Two-step approach - first convert format, then adjust architecture. Keeps each change focused.

**Outcome**: Created complete IEEE 29148 SRS with standalone service perspective.

---

#### Pattern: Reference Checking via GitHub

**Context**: User wanted to verify TMF model patterns were correct.

**Prompt**:
> Can you check the OAS of trouble ticket specification (in github)? ... They should also have an externalReference

**Why it worked**: Directed AI to authoritative source (actual OAS files) rather than relying on AI's knowledge.

**Outcome**: Confirmed TMF patterns, identified addressability fields (id/href) and extensibility fields (@type/@baseType/@schemaLocation).

---

#### Pattern: Collaborative Format Design

**Context**: User noticed redundancy between Use Cases table and User Scenarios section.

**Prompt**:
> Does the IEEE format require this section to have the subsections User Scenarios and Functional Requirements? I am thinking of ditching these two subheadings, and providing the functional requirements section at top, and individual user scenarios as subheadings.

**Why it worked**: User proposed specific alternative structure, asked for validation. Enabled collaborative decision-making.

**Outcome**: Agreed on cleaner format - FR table first (normative), then scenarios as direct `####` subheadings with UC IDs.

---

#### Pattern: Interrupt to Redirect

**Context**: AI was about to update a DNextPM template that was the wrong document.

**Prompt**:
> Wait, do not update anything. I will provide you the link to the SRS template in my workspace

**Why it worked**: Clear stop command with explanation. Prevented wasted effort on wrong target.

**Outcome**: AI paused, user provided correct Confluence URL.

---

### Outcomes

**Files created:**

- `temp/SRS_PRND-27183_State-flow-specification.md` - Complete IEEE 29148 SRS
- `temp/SRS_PRND-27183_ORIGINAL.md` - Backup of original FRs/BVRs for reference
- Confluence page 1403584572 (SRS-PRND-27183: State Flow Specification)

**Files modified:**

- `temp/srs-ieee-29148_template.md` - Restructured section 3.2
- `docs/to-be-workflow-proposal.md` - Updated embedded SRS template
- Confluence page 1403748376 (SRS Template IEEE 29148) - Restructured section 3.2

**Section 3.2 format standardized:**

- Old: Use Cases table → "User Scenarios" subheading → "Functional Requirements" subheading → FR table
- New: FR table first → Note → Scenarios as `####` subheadings with UC IDs and Related Requirements

**Commits:**
```
80d4862 Update SRS template section 3.2 to standardized format
```

### Lessons Learned

1. **Real examples drive template refinement**: Converting an actual SRS (PRND-27183) exposed redundancy in section 3.2 that wasn't obvious in the abstract template.

2. **GitHub as authoritative source**: Checking actual OAS files in GitHub repos (trouble-ticket, product-ordering) provided definitive TMF patterns.

3. **Interrupt to redirect**: "Wait, do not update anything" prevented updating wrong template. User course corrections should be welcomed.

4. **Commit message preferences**: User didn't want Claude attribution lines.

---

## 2025-12-03 - Documentation Cleanup & Standardization

### Goals

- Create IEEE 29148 SRS template from proposal
- Standardize documentation conventions (single stories.md, epics/ folder)
- Remove obsolete archiving workflow
- Delete unused agents and working documents
- Upload SRS template to Confluence

### Prompt Patterns

#### Pattern: Document Extraction

**Context**: User knew a document existed in the codebase but wanted it extracted into a standalone template.

**Prompt**:
> Find the proposal that contains the short SRS format, and write it as a markdown document. Name it `srs-ieee-29148_template.md`

**Why it worked**: Specific output format requested, clear naming, assumes AI can locate the source.

**Outcome**: Created complete IEEE 29148 SRS template from proposal content.

---

#### Pattern: Consistency Check with Direction

**Context**: User had made changes and wanted to find conflicts, but also indicated what the correct state should be.

**Prompt**:
> I made a change today. It should be a single stories.md per epic, instead of a file per story. Find the conflicting statements in the proposal.

**Why it worked**: States the desired convention first, then asks for conflicts. AI knows what "correct" looks like.

**Outcome**: Found and listed all locations with old `PRND-{story-id}.stories.md` pattern.

---

#### Pattern: Cross-Repository Sync Check

**Context**: Documentation existed in multiple places that needed to stay consistent.

**Prompt**:
> There is also a dnext-dev-support repo. Is it in sync with the documents?

**Why it worked**: Open-ended enough to let AI discover issues, but scoped to a specific repo.

**Outcome**: Discovered `active/` vs `epics/` naming inconsistency across repos.

---

#### Pattern: Cleanup Confirmation

**Context**: After identifying obsolete components, user gave clear deletion instructions.

**Prompt**:
> I think specs updater is now called design updater. I don't need a product owner. Delete these.

**Why it worked**: Provides reasoning (duplicate), gives explicit action (delete), batches related items.

**Outcome**: Deleted both agents cleanly.

---

#### Pattern: Iterative Refinement via Correction

**Context**: AI generated commit messages, user wanted adjustment to style.

**Prompt 1**:
> Please follow instructions in dev-support CLAUDE.md when committing

**Prompt 2** (after seeing result):
> Please also write up a little less terse description in commit description

**Why it worked**: First prompt pointed to existing guidelines. Second prompt gave specific stylistic feedback without rewriting the whole thing.

**Outcome**: Commit messages followed conventions with appropriate detail level.

---

#### Pattern: Defer Without Losing

**Context**: User wanted to exclude a file from commit but keep it for later use.

**Prompt**:
> Let's leave this template out for now. I have other plans for it. You can move to temp to ensure it is never committed

**Why it worked**: Clear intent (exclude), provides solution (move to temp), explains why (not committed).

**Outcome**: File preserved in temp/, added to .gitignore.

---

#### Pattern: Hierarchical Content Creation

**Context**: User wanted to create a folder structure in an external system with content.

**Prompt**:
> I want you to create a folder in confluence (space ahmet vedat hallac) named ai-sdlc; under it, create a subfolder named templates, and upload our SRS template there.

**Why it worked**: Clear hierarchy (parent → child → content), specific names, references existing artifact ("our SRS template").

**Outcome**: Created three nested Confluence pages with full template content.

---

### Outcomes

**Files deleted:**

- `claude-code/agents/specs-updater.md`
- `claude-code/agents/product-owner.md`
- `claude-code/agents/story-design-architect.md`
- `claude-code/agents/spike-assistant.md`
- `ai-assisted-sdlc-workflow-analysis.md`
- `understanding-dpr-epic-srs-stories.md`
- `rough idea.txt`
- `convert_to_html.py`
- `generate_proposal_html.py`

**Files modified:**

- `docs/claude-code/index.md` - Removed archive references
- `docs/claude-code/workflow-overview.md` - Updated Stage 7, removed archive workflow
- `docs/executive-summary.md` - Changed archive → cleanup/delete
- `docs/to-be-workflow-proposal.md` - Updated lifecycle, removed archive phases
- `.gitignore` - Added temp/

**Files created:**

- `temp/srs-ieee-29148_template.md` - IEEE 29148 SRS template
- Confluence: ai-sdlc → templates → SRS Template (IEEE 29148)

**Commits:**
```
fc7afd3 Remove obsolete working documents and HTML scripts
d948e10 Remove archiving from workflow, delete unused agents
e29a941 Add temp/ to gitignore for local clones
```

### Lessons Learned

1. **Commit message feedback loop**: User corrected twice (follow CLAUDE.md, less terse). Pattern: point to guidelines first, then give stylistic feedback.

2. **Git authentication**: `pass-git-helper` not found error. Workaround: embed token in URL. Worth documenting for future sessions.

3. **Batching deletions**: User naturally grouped related deletions ("delete these two", "also drop spike assistant"). AI should propose similar groupings.

4. **Canonical source matters**: When finding inconsistencies, ask user which version is canonical before making changes across files.
