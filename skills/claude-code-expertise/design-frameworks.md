# Component Design Decision Frameworks

Detailed questionnaires and decision trees for designing Claude Code components.

---

## Agent Design Framework

### Question 1: What is the single primary task?

**Purpose:** Ensure focused responsibility

**How to answer:**
- Describe task in 1-2 sentences
- If you need more, task is probably too broad
- If it overlaps significantly with existing agent, consolidate instead

**Examples:**

✅ **Good - Focused:**
- "Review code quality for a completed child story implementation"
- "Generate complete TMF API service from OpenAPI specification"
- "Design comprehensive failing tests following TDD approach"

❌ **Bad - Too broad:**
- "Help with development tasks"
- "Manage the entire story lifecycle"
- "Do everything related to code quality"

**Decision point:**
- Can describe in 1-2 sentences → Continue
- Requires paragraph to explain → Too broad, split into multiple agents
- Overlaps with existing agent → Consolidate or clarify distinction

### Question 2: What behavioral guidance is needed?

**Purpose:** Determine what goes in the agent prompt

**Categories to consider:**

**A. Workflow steps:**
- What sequence of operations?
- Are steps conditional or always executed?
- What triggers next step?

**B. Methodology:**
- What approach should agent use? (TDD, iterative, analytical)
- Are there specific principles to follow?
- Industry standards or best practices?

**C. Quality criteria:**
- How does agent know work is "done"?
- What standards must be met?
- What constitutes success vs. failure?

**D. Decision points:**
- Where does agent need to make choices?
- What information informs those choices?
- When to ask user vs. decide autonomously?

**Examples:**

**code-reviewer behavioral guidance:**
```markdown
Workflow:
1. Read all implementation files
2. Analyze against quality criteria
3. Categorize findings (critical/major/minor)
4. Create review items in .stories.md

Quality criteria:
- Error handling present and appropriate
- Input validation comprehensive
- Test coverage exists
- Follows project conventions

Decision points:
- Severity of each finding (use examples)
- Whether to flag minor issues (if >10, summarize instead)
```

**Decision point:**
- Clear workflow + criteria → Include in agent
- Vague or "figure it out" → Need more specificity
- Requires deep domain knowledge → Reference skill instead

### Question 3: What knowledge is needed?

**Purpose:** Determine what to embed vs. reference

**Decision tree:**

```
What knowledge does agent need?
│
├─ < 500 lines AND agent-specific
│  └─ Embed in agent prompt
│
├─ < 5KB AND shared across components
│  └─ Create or reference skill
│
├─ > 5KB OR rarely needed
│  └─ Create reference doc, read on-demand
│
└─ Constantly changing (e.g., project files)
   └─ Read from project, don't embed
```

**Examples:**

**Embed in agent (< 500 lines, specific):**
- Review severity criteria (50 lines)
- Test naming conventions (30 lines)
- Error message format (20 lines)

**Use skill (shared knowledge):**
- Spring testing patterns → spring-testing skill
- TMF API patterns → dnext-code-gen skill
- Story conventions → story-conventions skill

**Reference doc (large, infrequent):**
- Complete code quality guide (1,000+ lines)
- Historical examples of good/bad patterns
- Detailed polymorphic mapper patterns

**Read from project (dynamic):**
- SRS documents
- Module design files
- Existing implementations

**Decision point:**
- Agent-specific knowledge < 500 lines → Embed
- Shared knowledge → Use skill
- Large reference → External doc
- Project-specific → Read on-demand

### Question 4: What tools are necessary?

**Purpose:** Grant minimum necessary permissions

**Tool categories:**

**Read-only tools:**
- Read: Read specific files
- Grep: Search file contents
- Glob: Find files by pattern

**Write tools:**
- Write: Create new files
- Edit: Modify existing files

**Execution tools:**
- Bash: Run commands (builds, tests, git)

**Agent-specific tools:**
- Task: Invoke other agents
- Specialized tools (if any)

**Decision matrix:**

| Agent Type | Typical Tools | Restrictions |
|------------|---------------|--------------|
| Analyzer/Reviewer | Read, Grep, Glob | No Write/Edit to code |
| Implementer | Read, Write, Edit, Bash | Full access needed |
| State Manager | Read, Edit | Only to state files |
| Planner | Read, Grep, Glob, Task | No Write (planning only) |

**Examples:**

**code-reviewer:**
```markdown
Tools: Read, Grep, Glob, Edit

Restrictions:
- Edit ONLY for .stories.md (review items)
- CANNOT modify implementation code
- CANNOT run builds or tests (read-only analysis)
```

**api-generator:**
```markdown
Tools: Read, Write, Edit, Bash

Full access because:
- Generates new code (Write)
- Fixes compilation errors (Edit)
- Runs compiler (Bash)
- Reads existing patterns (Read)
```

**Decision point:**
- Read-only analysis → Read, Grep, Glob only
- Implementation → Read, Write, Edit, Bash
- State tracking → Read, Edit (specific files)
- Complex orchestration → Add Task tool

### Question 5: What model tier?

**Purpose:** Optimize cost vs. capability

**Model characteristics:**

**opus:**
- Most capable, most expensive
- Complex reasoning and creativity
- Context synthesis across many files
- Architectural decisions

**sonnet:**
- Balanced capability and cost
- Standard tasks with moderate complexity
- Most workflows use this (default)

**haiku:**
- Fast and economical
- Simple, mechanical operations
- State tracking, basic analysis
- Well-defined tasks with clear logic

**Decision criteria:**

```
Does task require creativity/innovation?
├─ YES → opus
└─ NO → Continue

Is task mechanical with clear rules?
├─ YES → haiku
└─ NO → sonnet
```

**Examples by model:**

**opus tasks:**
- api-generator (creative code generation, architectural decisions)
- story-implementation-planner (synthesis of SRS + design into plan)
- implementer (TDD implementation requires design thinking)

**sonnet tasks:**
- code-reviewer (rule-based but needs judgment)
- test-first-designer (structured approach, moderate complexity)
- design-updater (extract and organize information)

**haiku tasks:**
- story-tracker (state transitions, simple rules)
- file-organizer (mechanical operations)
- status-reporter (gather and format information)

**Decision point:**
- Creative/synthesis tasks → opus
- Mechanical/rule-based → haiku
- Standard workflows → sonnet (default)

### Complete Agent Design Template

Use this after answering all 5 questions:

```markdown
# Agent: {agent-name}

## Purpose
{1-2 sentence description from Q1}

## Workflow
{Behavioral guidance from Q2}

1. {Step 1}
   - {Details}
   - {Success criteria}

2. {Step 2}
   - {Details}
   - {Decision points}

3. {Step N}
   - {Final verification}
   - {Deliverables}

## Knowledge Sources
{From Q3}
- {Skill references}
- {Documentation to read on-demand}

## Tool Access
{From Q4}
{List of tools with restrictions}

## Model
{From Q5} ({justification})

## Success Criteria
{What constitutes completion}

## Error Handling
{When to stop and report}
```

**Target size:** 200-800 lines of behavioral guidance

---

## Skill Design Framework

### Question 1: What triggers auto-activation?

**Purpose:** Define when skill should load

**Be specific, not vague:**

❌ **Too vague:**
- "When coding"
- "Working with Java"
- "Writing tests"

✅ **Specific:**
- "Writing JUnit 5 tests with Spring Test framework"
- "Working with TMF API code generation"
- "Editing .stories.md files for story management"

**Multiple triggers allowed:**
```markdown
**Auto-activates when:**
- Writing JUnit 5 tests
- Using Spring Test framework
- Working with MockMvc or TestRestTemplate
- Designing test architecture
```

**Test your triggers:**
- Would this activate for irrelevant work? → Too broad
- Would this miss relevant work? → Too narrow
- Does it overlap with other skills? → Consider consolidation

**Decision point:**
- Triggers are specific and testable → Continue
- Triggers too broad → Narrow scope
- Triggers unclear → Rethink skill boundaries

### Question 2: How much knowledge is needed?

**Purpose:** Determine embedded vs. referenced split

**Estimation process:**

1. **List all knowledge areas:**
   - Core patterns
   - Common templates
   - Decision trees
   - Examples
   - Edge cases
   - Advanced patterns

2. **Categorize by frequency:**
   - Every use: Embed
   - Most uses: Embed
   - Sometimes: Consider embedding if small
   - Rarely: Reference file
   - Never used together: Definitely reference

3. **Calculate size:**
   - Estimate lines for each embedded area
   - 1 KB ≈ 20-25 lines of content
   - 3-5 KB ≈ 75-125 lines

**Decision matrix:**

| Total Knowledge | Frequency | Pattern |
|-----------------|-----------|---------|
| < 3KB | All high-frequency | Embed everything |
| 3-5KB | Mixed frequency | Embed common, reference rare |
| > 5KB | All high-frequency | Embed core, reference details |
| > 5KB | Mixed frequency | Embed essentials only |

**Examples:**

**spring-testing (3KB total, high frequency):**
- Common annotations: Embed (500 lines)
- AAA template: Embed (50 lines)
- MockMvc patterns: Embed (400 lines)
- Test isolation: Embed (200 lines)
- Advanced integration patterns: Reference
- Test data builders: Reference

**api-documentation (10KB total, mixed frequency):**
- OpenAPI structure: Embed (200 lines)
- Common response codes: Embed (100 lines)
- Complete examples: Reference (5KB)
- Schema patterns: Reference (4KB)

**Decision point:**
- < 5KB, all high-frequency → Comprehensive embedded
- > 5KB OR mixed frequency → Lightweight + references

### Question 3: How often will it be accessed?

**Purpose:** Validate token cost is worthwhile

**Access patterns:**

**Every relevant task:**
- Skill activates for every instance of trigger
- Knowledge used immediately
- Example: spring-testing for every test file

**Most tasks:**
- Skill activates frequently
- Knowledge needed in 80%+ of cases
- Example: dnext-code-gen for TMF API work

**Conditionally:**
- Skill activates but knowledge needed only sometimes
- Less than 50% of activations use all knowledge
- Example: Advanced error handling only for complex cases

**Rarely:**
- Skill might activate but knowledge rarely needed
- Token cost not justified
- Should be reference doc, not skill

**Decision framework:**

```
How often is knowledge used?
│
├─ Every activation (100%)
│  └─ High value → Embed generously (up to 5KB)
│
├─ Most activations (70-90%)
│  └─ Good value → Embed common patterns (3-4KB)
│
├─ Sometimes (30-70%)
│  └─ Mixed value → Embed essentials (1-2KB), reference rest
│
└─ Rarely (< 30%)
   └─ Low value → Reference doc, not skill
```

**Decision point:**
- High frequency (>70%) → Justify larger skill
- Mixed frequency → Keep lean, use references
- Low frequency (<30%) → Don't create skill

### Question 4: Is knowledge shared with other components?

**Purpose:** Avoid duplication, enable reuse

**Scenarios:**

**Skill-specific knowledge:**
- Used only by this skill
- Doesn't apply elsewhere
- Example: JUnit-specific test patterns

**Shared across skills:**
- Multiple skills need same information
- Example: HTTP status codes (API, testing, documentation skills)

**Shared with agents:**
- Agents read same references
- Example: Code quality standards (reviewer agent + quality skill)

**Decision tree:**

```
Is knowledge needed elsewhere?
│
├─ NO (skill-specific)
│  └─ Embed in skill OR skill's reference directory
│
├─ YES (shared across skills)
│  └─ Create in ~/.claude/references/shared/
│
└─ YES (shared with agents)
   └─ Create in ~/.claude/references/shared/
      All components read same file
```

**Example structures:**

**Skill-specific:**
```
~/.claude/skills/spring-testing.md
~/.claude/references/spring-testing/
  ├── annotations-guide.md
  └── integration-patterns.md
```

**Shared knowledge:**
```
~/.claude/references/shared/
  ├── http-status-codes.md
  ├── error-handling-patterns.md
  └── validation-rules.md

# Referenced by:
~/.claude/skills/api-documentation.md
~/.claude/skills/rest-testing.md
~/.claude/agents/api-generator.md
```

**Decision point:**
- Skill-specific → Keep in skill's reference directory
- Shared → Move to shared references
- Update all consumers → Point to shared location

### Question 5: What's the access pattern?

**Purpose:** Optimize for how knowledge is used

**Access patterns:**

**Quick reference (scan and find):**
- User needs to look up specific item
- Not reading sequentially
- Example: Annotation reference, status codes

**Linear reading (sequential):**
- User reads section by section
- Building understanding progressively
- Example: Tutorial, methodology guide

**Example-driven (find similar):**
- User looking for example matching their case
- Scanning examples for patterns
- Example: Code templates, pattern library

**Decision-driven (follow logic):**
- User answering questions to reach decision
- Following decision tree or flowchart
- Example: Which mapper to use, which test type

**Optimization by pattern:**

**Quick reference → Structure:**
- Tables for scanning
- Short descriptions
- Alphabetical or logical grouping
- Embedded in skill (fast access)

**Linear reading → Structure:**
- Clear progression
- Building complexity
- Can be in reference doc
- Skill has summary, reference has details

**Example-driven → Structure:**
- Multiple concrete examples
- Organized by use case
- Short examples embedded, detailed in reference

**Decision-driven → Structure:**
- Decision trees or flowcharts
- Clear yes/no questions
- Embed in skill (needed for every decision)

**Decision point:**
- Quick reference → Embed with table/list format
- Linear/example-driven → Summary embedded, details referenced
- Decision-driven → Embed decision logic, reference details

### Complete Skill Design Template

Use this after answering all 5 questions:

```markdown
# Skill: {skill-name}

**Auto-activates when:**
{Specific triggers from Q1}

## Quick Reference (embedded)
{High-frequency knowledge from Q2, Q3}
{Structured per access pattern from Q5}

### {Section 1}
{Core patterns, always needed}

### {Section 2}
{Common templates, frequently used}

### {Section 3}
{Decision trees, every use}

## Detailed Reference (on-demand)
{Low-frequency or large knowledge from Q2, Q3}

For {specific topic}:
- Read ~/.claude/references/{skill-name}/{topic}.md

For {shared knowledge}:
- Read ~/.claude/references/shared/{knowledge}.md

---

**Size:** {Estimated KB}
**Token cost:** {Estimated tokens}
**Access pattern:** {From Q5}
```

**Target size:** 1-5KB embedded content

---

## Command Design Framework

### Question 1: Does it orchestrate multiple agents?

**Purpose:** Validate command vs. single agent invocation

**Scenarios:**

**Single agent:**
```markdown
# Not a command
Just invoke: Task("code-reviewer", "Review ProductController.java")
```

**Multiple agents in sequence:**
```markdown
# Good command candidate
1. story-implementation-planner → generate guide
2. test-first-designer → create tests
3. implementer → implement
4. code-reviewer → review
```

**Multiple agents conditionally:**
```markdown
# Good command candidate
1. merge-readiness-checker → verify
2. If pass: Create PR
3. If fail: Show issues (no further agents)
```

**Decision point:**
- Single agent → Don't create command
- Multiple agents → Continue evaluation
- Conditional agent execution → Good command candidate

### Question 2: Is it a common workflow?

**Purpose:** Ensure command will be used frequently enough

**Frequency assessment:**

**Used every story:**
- /start-story
- /ready-for-merge
- Value: Very high

**Used multiple times per story:**
- /review-code (each child story)
- /run-tests
- Value: High

**Used occasionally:**
- /create-api (when story involves API)
- /setup-epic (new epic initialization)
- Value: Medium (still worthwhile if saves significant time)

**Used rarely:**
- /one-time-migration
- /special-cleanup
- Value: Low (probably not worth command)

**Decision framework:**

```
How often will this be used?
│
├─ Every story (20+ times/month)
│  └─ Definitely create command
│
├─ Multiple times per week (10-20 times/month)
│  └─ Probably create command
│
├─ Occasionally (3-10 times/month)
│  └─ Create if saves significant time
│
└─ Rarely (< 3 times/month)
   └─ Don't create command
```

**Decision point:**
- Frequent use → Create command
- Occasional but time-saving → Create command
- Rare use → Don't create command

### Question 3: Does it enforce sequencing?

**Purpose:** Validate value of command over manual steps

**Sequencing value:**

**Must happen in order:**
```markdown
# High value - command prevents errors

1. Verify story in BACKLOG (must be first)
2. Generate implementation guide (needs story verified)
3. Create branch (needs guide complete)
4. Update story to DESIGNING (needs branch created)

Without command: User might skip verification or create branch first
```

**Can happen in any order:**
```markdown
# Low value - command adds little

1. Read file A
2. Read file B
3. Display summary

Without command: User can read files in any order
```

**Partial sequencing:**
```markdown
# Medium value

1. Must run first: Validation
2-3. Any order: Analysis tasks
4. Must run last: Update state

Without command: User might forget validation or state update
```

**Decision framework:**

```
Does order matter?
│
├─ YES - Critical sequencing
│  └─ High value → Create command
│
├─ PARTIALLY - Some steps must be ordered
│  └─ Medium value → Consider creating
│
└─ NO - Steps independent
   └─ Low value → Don't create command
```

**Decision point:**
- Critical sequencing → Create command
- Partial sequencing + frequent use → Create command
- No sequencing requirement → Don't create command

### Question 4: Does it gather parameters?

**Purpose:** Assess if command simplifies complex inputs

**Parameter scenarios:**

**Complex parameter construction:**
```markdown
# High value

Command input: /create-api PRND-2847 ProductOffering

Behind the scenes:
- Derives OpenAPI path: openapi/product-offering.yaml
- Determines package: com.dnext.api.productoffering
- Finds story context from .stories.md
- Configures agent with 8-phase workflow

Without command: User needs to provide all these details
```

**Simple passthrough:**
```markdown
# Low value

Command input: /read-file path/to/file

Behind the scenes:
- Read path/to/file

Without command: Just use Read tool
```

**Parameter validation:**
```markdown
# Medium value

Command input: /link-stories PRND-2847 PRND-2850

Behind the scenes:
- Validates both stories exist
- Checks relationship is valid
- Prevents circular dependencies
- Updates both stories correctly

Without command: User might create invalid relationships
```

**Decision framework:**

```
Do parameters require processing?
│
├─ YES - Derives additional context
│  └─ High value → Create command
│
├─ YES - Validates complex rules
│  └─ Medium value → Consider creating
│
└─ NO - Simple passthrough
   └─ Low value → Don't create command
```

**Decision point:**
- Complex derivation → Create command
- Validation prevents errors → Create command
- Simple passthrough → Don't create command

### Complete Command Design Template

Use this after answering all 4 questions:

```markdown
# Command: /{command-name} {PARAM1} {PARAM2}

**Purpose:**
{Clear description from Q1}

**Parameters:**
- {PARAM1}: {Description and example}
- {PARAM2}: {Description and example}

**Steps:**
{From Q1 - orchestration of multiple agents}

1. {Validation/preparation}
2. {Agent 1 invocation}
3. {Conditional logic if applicable}
4. {Agent 2 invocation if needed}
5. {Final steps and confirmation}

**Prerequisites:**
{What must be true before command runs}

**Error handling:**
{What happens if steps fail}

**Value proposition:**
{Why command vs. manual - from Q2, Q3, Q4}
```

**Target size:** 100-200 lines

---

## Quick Decision Summary

**Create Agent when:**
- [ ] Complex multi-step task requiring orchestration
- [ ] Needs context isolation from main conversation
- [ ] Benefits from specific tool restrictions
- [ ] Has clear single responsibility
- [ ] Used frequently enough to warrant creation

**Create Skill when:**
- [ ] Domain knowledge auto-activates in specific contexts
- [ ] Knowledge < 5KB or can split to embedded + references
- [ ] Used frequently (>70% of relevant contexts)
- [ ] Provides patterns, templates, or decision logic
- [ ] Not duplicating knowledge from other skills

**Create Command when:**
- [ ] Orchestrates multiple agents or complex steps
- [ ] Common workflow (used 3+ times/month)
- [ ] Enforces critical sequencing
- [ ] Gathers and validates complex parameters
- [ ] Saves significant user time and prevents errors

**Don't create anything when:**
- [ ] Single simple operation
- [ ] Used very rarely
- [ ] No clear value over manual execution
- [ ] Would duplicate existing component
