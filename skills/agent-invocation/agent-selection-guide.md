# Agent Selection Guide

Detailed guide for selecting the right agent type and model tier for different tasks.

---

## Named Agents: When and How to Use

### story-implementation-planner

**Purpose:** Generate implementation guide for story pickup

**When to use:**
- Story is in BACKLOG state
- Need breakdown into child stories
- Complex feature requiring planning
- First step in story workflow

**When NOT to use:**
- Story is already planned (guide exists)
- Simple bug fix (no planning needed)
- Non-story work (exploratory tasks)

**Model tier:** opus (requires synthesis of SRS, design docs, architectural decisions)

**Prompt template:**
```markdown
Plan implementation for story PRND-{key}.

Context:
- Story state: BACKLOG
- SRS: docs/epics/{epic}/srs-extract.md
- Feature design: docs/epics/{epic}/feature-design.md
- Module design: docs/modules/{module}/design.md

Generate implementation guide:
- Analyze complexity (single vs. multiple children)
- Identify dependencies (other stories, modules)
- Define technical approach
- Create child stories if needed
```

---

### code-reviewer

**Purpose:** Review implementation for code quality issues

**When to use:**
- Child story implementation complete
- Before transitioning to REVIEWING state
- Regular quality checks
- Pre-merge verification

**When NOT to use:**
- Code not yet complete
- Planning phase (no code to review)
- Build failures (fix those first)

**Model tier:** sonnet (structured analysis with moderate complexity)

**Prompt template:**
```markdown
Review implementation for child story PRND-{key}#{child}.

Context:
- Implementation files: {specific paths}
- Story: .stories.md (search for PRND-{key}#{child})
- State: Implementation complete

Review criteria:
- Code organization and structure
- Error handling patterns
- Input validation
- Test coverage
- Follows project conventions

Deliverables:
- Create review items in .stories.md
- Format: PRND-{key}#{child}.R1, R2, etc.
- Categorize by severity (critical/major/minor)
- Provide specific file/line references
```

---

### test-first-designer

**Purpose:** Design comprehensive failing tests before implementation

**When to use:**
- Following TDD approach
- Before implementing child story
- Complex testing scenarios
- Need comprehensive test coverage

**When NOT to use:**
- Tests already exist
- Simple trivial code (write tests directly)
- Exploratory prototyping

**Model tier:** sonnet (structured test design, moderate complexity)

**Prompt template:**
```markdown
Design comprehensive failing tests for child story PRND-{key}#{child}.

Context:
- Story: .stories.md (PRND-{key}#{child})
- Implementation guide section: {specific section}
- Test patterns: {existing test examples}

Create tests covering:
- Happy path scenarios (typical usage)
- Error cases (invalid input, exceptions)
- Edge cases (boundary conditions)
- Integration points (dependencies, external systems)

Tests should fail initially (red phase of TDD).
```

---

### implementer

**Purpose:** Implement functionality following TDD red-green-refactor

**When to use:**
- Failing tests already created
- Complex implementation requiring design
- TDD workflow
- Iterative refinement needed

**When NOT to use:**
- No tests exist (create tests first)
- Simple changes (do directly)
- Just fixing bugs (targeted fix better)

**Model tier:** opus (requires creative problem-solving and design decisions)

**Prompt template:**
```markdown
Implement child story PRND-{key}#{child} following TDD.

Context:
- Failing tests: {test file paths}
- Implementation guide: {guide section}
- Related code: {existing implementations}

Workflow:
1. Red: Verify tests fail
2. Green: Minimal code to pass
3. Refactor: Clean up while keeping tests green
4. Iterate for each test

Complete when:
- All tests passing
- Code clean and well-organized
- Follows project patterns
```

---

### design-updater

**Purpose:** Update module design.md after story completion

**When to use:**
- Story marked DONE
- Implementation complete and reviewed
- Before creating PR
- Captures implementation decisions

**When NOT to use:**
- Story not complete
- No significant design changes
- Temporary/experimental code

**Model tier:** sonnet (extraction and organization of information)

**Prompt template:**
```markdown
Update module design.md for completed story PRND-{key}.

Context:
- Implementation files: {paths to implemented code}
- Story: .stories.md (PRND-{key})
- Module design: docs/modules/{module}/design.md

Extract and update:
- Component structure (new classes/interfaces)
- Data flow patterns (how data moves)
- Integration points (external dependencies)
- Design decisions (why choices made)
- Patterns used (architectural patterns)
```

---

### merge-readiness-checker

**Purpose:** Verify branch ready for PR creation

**When to use:**
- Before creating pull request
- Before providing merge instructions
- Quality gate before merge
- Final verification step

**When NOT to use:**
- Known failures exist
- Work incomplete
- Story not in DONE state

**Model tier:** sonnet (systematic checks, moderate complexity)

**Prompt template:**
```markdown
Verify branch ready for PR creation.

Checks required:
- Clean build: mvn clean install
- All tests pass: mvn test
- Story alignment: implementation matches guide
- Design.md updated (if story DONE)
- No uncommitted changes

If all pass:
- Show merge instructions
- Suggest PR title/description

If any fail:
- List blocking issues
- Suggest remediation
- Do NOT provide merge instructions
```

---

### story-tracker

**Purpose:** Manage story state transitions

**When to use:**
- Updating story state
- Simple state transitions
- Tracking story progress
- Mechanical state management

**When NOT to use:**
- Complex workflow logic
- Need analysis or decisions
- Creating/editing story content

**Model tier:** haiku (simple state tracking, low cost)

**Prompt template:**
```markdown
Update story PRND-{key} state to {NEW_STATE}.

Validation:
- Current state: {CURRENT_STATE}
- Transition {CURRENT_STATE} → {NEW_STATE} is valid
- Story exists in .stories.md

Actions:
- Update state in .stories.md
- Verify transition completed
- Confirm new state
```

---

### api-generator

**Purpose:** Generate complete TMF API service from OpenAPI

**When to use:**
- New TMF API needed
- OpenAPI specification available
- Following D-NEXT patterns
- Complete service generation

**When NOT to use:**
- No OpenAPI spec
- Non-TMF APIs
- Partial generation (use direct implementation)

**Model tier:** opus (complex code generation with architectural decisions)

**Prompt template:**
```markdown
Generate complete TMF API service from openapi/{api-name}.yaml.

Context:
- OpenAPI spec: openapi/{api-name}.yaml
- Project: Maven multi-module
- Package: com.dnext.api.{apiname}
- Database: PostgreSQL with JPA
- Polymorphic handling: {if applicable, which fields}

Follow 8-phase iterative approach:
1. External models → compile → fix
2. Internal entities → compile → fix
3. Polymorphic mappers → compile → fix
4. Repository → compile → fix
5. Service → compile → fix
6. Controller → compile → fix
7. Unit tests → verify
8. Full build → verify

Compile after each phase: mvn clean compile -pl api-layer
```

---

## Generic Agents: When and How to Use

### Explore Agent

**Purpose:** Find files, search patterns, understand codebase structure

**When to use:**
- Don't know where code is
- Finding all instances of pattern
- Understanding codebase organization
- Answering "where" questions

**When NOT to use:**
- Know exact file location (use Read directly)
- Simple file search (use Glob)
- Single file content search (use Grep)

**Model tiers:**
- **haiku**: Quick file location, simple searches
- **sonnet**: Moderate analysis, pattern identification
- **opus**: Complex architectural understanding, deep analysis

**Prompt templates:**

Quick search (haiku):
```markdown
Find all files containing {specific pattern}.

Approach: quick

Search strategy:
- Glob: **/*{pattern}*.java
- Report file paths with brief description
```

Moderate exploration (sonnet):
```markdown
Find all error handling patterns in API layer.

Approach: medium

Analysis:
- Identify try-catch patterns
- Find exception classes
- Note consistency
- Report 2-3 examples with recommendations
```

Deep analysis (opus):
```markdown
Understand the authentication architecture across the codebase.

Approach: very thorough

Analyze:
- All auth-related files
- Integration points
- Data flow
- Security patterns
- Provide architectural diagram (text-based)
```

---

### Plan Agent

**Purpose:** Design implementation strategy without story context

**When to use:**
- Need implementation plan
- No formal story structure
- Exploratory design work
- Architectural planning

**When NOT to use:**
- Story exists (use story-implementation-planner)
- Simple task (do directly)
- Just need code review

**Model tier:** opus (complex planning and architectural decisions)

**Prompt template:**
```markdown
Design implementation strategy for {task description}.

Context:
- Current state: {what exists now}
- Desired state: {what should exist}
- Constraints: {technical/architectural limits}
- Related systems: {dependencies}

Deliverables:
- Step-by-step implementation plan
- File changes required (create/modify/delete)
- Risk assessment
- Alternative approaches with trade-offs
- Recommended approach with rationale
```

---

### general-purpose Agent

**Purpose:** Complex multi-step tasks without specialized agent

**When to use:**
- Task doesn't fit other agents
- Multiple different operations
- Exploratory work
- Custom workflow

**When NOT to use:**
- Specialized agent exists
- Simple single task
- Workflow is standard (use command)

**Model tier:** sonnet (default for balanced tasks)

**Prompt template:**
```markdown
{Clear description of multi-step task}

Steps:
1. {Step 1 with details}
2. {Step 2 with details}
3. {Step 3 with details}

Context:
{Relevant information}

Success criteria:
{What "done" means}
```

---

## Model Tier Selection Guide

### Use opus when:
- **Creative problem-solving needed**
  - Novel solutions required
  - Multiple valid approaches
  - Design decisions with trade-offs

- **Context synthesis across many files**
  - Understanding complex interactions
  - Architectural analysis
  - Cross-cutting concerns

- **Complex planning**
  - Breaking down large features
  - Dependency analysis
  - Risk assessment

**Examples:**
- story-implementation-planner (synthesizes SRS + design → plan)
- implementer (creative coding, design choices)
- api-generator (complex generation with decisions)
- Plan agent (architectural planning)

**Cost:** Highest, use judiciously

---

### Use sonnet when:
- **Standard workflows**
  - Following established patterns
  - Moderate complexity
  - Structured tasks

- **Code review and analysis**
  - Quality checks
  - Pattern verification
  - Finding issues

- **Documentation updates**
  - Extracting information
  - Organizing content
  - Updating files

**Examples:**
- code-reviewer (structured quality analysis)
- test-first-designer (structured test design)
- design-updater (extraction and organization)
- merge-readiness-checker (systematic checks)
- Explore agent (moderate analysis)

**Cost:** Moderate, good default choice

---

### Use haiku when:
- **Simple mechanical operations**
  - Clear rules
  - No creativity needed
  - Straightforward logic

- **State tracking**
  - Status updates
  - Simple transitions
  - Data recording

- **Quick searches**
  - File location
  - Simple patterns
  - Basic information retrieval

**Examples:**
- story-tracker (simple state transitions)
- Explore agent with "quick" approach (file finding)
- Simple status updates
- Basic file operations

**Cost:** Lowest, use for simple tasks

---

## Decision Matrix

| Task Type | Agent | Model | Why |
|-----------|-------|-------|-----|
| Story planning | story-implementation-planner | opus | Synthesis, creativity |
| Code review | code-reviewer | sonnet | Structured analysis |
| Test design | test-first-designer | sonnet | Structured creation |
| Implementation | implementer | opus | Creative coding |
| Design update | design-updater | sonnet | Information extraction |
| Merge check | merge-readiness-checker | sonnet | Systematic verification |
| State update | story-tracker | haiku | Simple transition |
| API generation | api-generator | opus | Complex generation |
| File finding | Explore | haiku | Simple search |
| Pattern analysis | Explore | sonnet | Moderate analysis |
| Architecture understanding | Explore | opus | Deep synthesis |
| Implementation planning | Plan | opus | Creative planning |
| Multi-step task | general-purpose | sonnet | Balanced default |

---

## Common Selection Mistakes

### Mistake: Using generic when specialized exists

**Wrong:**
```markdown
Use Explore agent to review code quality
```

**Right:**
```markdown
Use code-reviewer agent (specialized for quality review)
```

---

### Mistake: Wrong model tier

**Wrong:**
```markdown
Use story-tracker with opus model to update story state
```

**Right:**
```markdown
Use story-tracker with haiku model (simple state transition)
```

**Wrong:**
```markdown
Use implementer with haiku model to implement complex feature
```

**Right:**
```markdown
Use implementer with opus model (requires creative coding)
```

---

### Mistake: Not using agent when beneficial

**Wrong:**
```markdown
Read 50 files manually to find all error handling patterns
```

**Right:**
```markdown
Use Explore agent to find all error handling patterns across codebase
```

---

### Mistake: Using agent for trivial task

**Wrong:**
```markdown
Invoke Explore agent to find README.md
```

**Right:**
```markdown
Just use Glob or Read ./README.md
```

---

## Quick Reference

**Story workflow:**
1. BACKLOG → story-implementation-planner (opus)
2. DESIGNING → test-first-designer (sonnet)
3. IMPLEMENTING → implementer (opus)
4. REVIEWING → code-reviewer (sonnet)
5. DONE → design-updater (sonnet)
6. Ready for PR → merge-readiness-checker (sonnet)

**Code generation:**
- TMF API → api-generator (opus)
- Other code → implementer (opus) or direct

**Codebase exploration:**
- Quick find → Explore (haiku)
- Pattern analysis → Explore (sonnet)
- Architecture understanding → Explore (opus)

**Planning:**
- Story → story-implementation-planner (opus)
- Other → Plan (opus)

**State management:**
- Story state → story-tracker (haiku)
