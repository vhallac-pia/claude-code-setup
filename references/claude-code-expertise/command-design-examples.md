# Command Design Examples

Practical examples of well-designed commands following best practices.

---

## Example 1: Multi-Agent Orchestration

### /ready-for-merge

**Purpose:** Verify branch is ready for PR creation

**Command structure:**

```markdown
# Command: /ready-for-merge

**Purpose:** Verify branch is ready for PR creation

**Steps:**

1. Run merge-readiness-checker agent:
   - Verify clean build (mvn clean install)
   - Run all tests
   - Check story alignment (implementation matches guide)
   - Verify design.md updated (if story in DONE state)

2. Display results:
   - If all checks pass:
     * Show merge instructions
     * List files changed (git diff --stat main)
     * Suggest PR title/description from story
     * Display merge command

   - If checks fail:
     * List blocking issues with priorities
     * Suggest remediation steps
     * Do NOT provide merge instructions
     * Exit with clear next steps

3. Ask if user wants to proceed with PR creation:
   - If yes: Run gh pr create with suggested title/body
   - If no: Display summary and exit

**Prerequisites:**
- On feature branch (not main)
- No uncommitted changes
- Story in DONE state (or REVIEWING with all review items resolved)
```

**Size:** ~150 lines
**Value:** Ensures consistent quality gate, prevents premature merges
**Agent used:** merge-readiness-checker

---

## Example 2: Workflow Sequence

### /start-story STORY_KEY

**Purpose:** Begin work on a BACKLOG story

**Command structure:**

```markdown
# Command: /start-story STORY_KEY

**Purpose:** Begin work on a BACKLOG story

**Parameters:**
- STORY_KEY: Story identifier (e.g., PRND-2847)

**Steps:**

1. Verify story state:
   - Read .stories.md
   - Find story with STORY_KEY
   - Confirm state is BACKLOG
   - If not BACKLOG: Error with current state, exit

2. Invoke story-implementation-planner agent:
   - Analyze SRS: docs/epics/{epic}/srs-extract.md
   - Review feature design: docs/epics/{epic}/feature-design.md
   - Check module design: docs/modules/{module}/design.md
   - Generate implementation guide
   - Create child stories if needed (complexity assessment)
   - Update story state to DESIGNING

3. Create feature branch:
   - Verify git working directory is clean
   - Determine branch type (feature/ or bugfix/)
   - Create branch: git checkout -b {type}/STORY_KEY
   - Confirm branch creation

4. Display implementation guide summary:
   - Show approach and complexity
   - List child stories if created
   - Next steps for user

**Prerequisites:**
- Story must exist in .stories.md
- Story must be in BACKLOG state
- Git working directory must be clean
- On main branch

**Error handling:**
- Story not found: List available BACKLOG stories
- Story not in BACKLOG: Show current state, suggest workflow
- Git not clean: List uncommitted changes, suggest commit/stash
- Not on main: Show current branch, suggest switching
```

**Size:** ~200 lines
**Value:** Consistent story startup, enforces workflow sequence
**Agent used:** story-implementation-planner

---

## Example 3: Parameterized Command

### /create-api STORY_KEY API_NAME

**Purpose:** Generate TMF API service from OpenAPI specification

**Command structure:**

```markdown
# Command: /create-api STORY_KEY API_NAME

**Purpose:** Generate complete TMF API service from OpenAPI specification

**Parameters:**
- STORY_KEY: Story identifier (e.g., PRND-2847)
- API_NAME: API resource name (e.g., ProductOffering)

**Steps:**

1. Validate inputs:
   - Story exists and in DESIGNING or IMPLEMENTING state
   - OpenAPI spec exists: openapi/{api-name-kebab}.yaml
   - Target package is clear from spec or story
   - No existing implementation (or confirm overwrite)

2. Invoke api-generator agent:
   - Pass OpenAPI spec path
   - Pass target package
   - Pass story key (for context)
   - Agent runs 8-phase iterative generation
   - Agent handles compile-fix cycles

3. Verify compilation:
   - Agent should have verified, but confirm
   - Run: mvn clean install -DskipTests
   - If fails: Report issue, agent should have caught this

4. Update story with completion status:
   - If successful: Add completion notes to story
   - List generated files
   - Update child story state if applicable
   - Suggest next steps (write tests, review)

**Prerequisites:**
- OpenAPI specification exists
- Story in appropriate state
- Project builds cleanly before generation

**Error handling:**
- Missing OpenAPI spec: List available specs
- Story wrong state: Show current state
- Compilation fails: Show errors, suggest reviewing agent output
- Package conflicts: Suggest resolution strategy
```

**Size:** ~180 lines
**Value:** Streamlines API generation workflow, ensures consistency
**Agent used:** api-generator

---

## Command Design Patterns

### Pattern 1: Simple Orchestration

**Use when:** Invoke single agent with standard parameters

**Structure:**
```markdown
# Command: /simple-task

1. Validate prerequisites
2. Invoke agent with parameters
3. Display results
4. Update state if needed
```

**Example:** /review-code, /plan-story

### Pattern 2: Multi-Agent Chain

**Use when:** Multiple agents must run in sequence

**Structure:**
```markdown
# Command: /multi-step-workflow

1. Agent 1: Initial analysis
2. Use Agent 1 results to determine next step
3. Agent 2: Based on analysis
4. Agent 3: Finalize (if needed)
5. Aggregate results
```

**Example:** /complete-story (plan → implement → test → review)

### Pattern 3: Conditional Execution

**Use when:** Different paths based on conditions

**Structure:**
```markdown
# Command: /conditional-task

1. Check condition A
   - If true: Path A (Agent X)
   - If false: Path B (Agent Y)

2. Process results from chosen path

3. Final steps based on outcome
```

**Example:** /ready-for-merge (pass → merge instructions, fail → remediation)

### Pattern 4: Interactive Command

**Use when:** User input needed during execution

**Structure:**
```markdown
# Command: /interactive-task

1. Perform initial analysis
2. Present options to user
3. Wait for user choice
4. Execute based on choice
5. Confirm completion
```

**Example:** /resolve-conflict (show conflicts, ask resolution strategy, apply)

---

## When to Create Commands

### ✅ Good Use Cases

**1. Common workflow sequences:**
- /start-story (multiple steps, used frequently)
- /ready-for-merge (quality gate, used every story)
- /complete-review (review → verify → update state)

**2. Multi-agent orchestration:**
- /full-analysis (explore → analyze → recommend)
- /end-to-end-test (setup → test → teardown)

**3. Consistent parameter gathering:**
- /create-api STORY_KEY API_NAME
- /link-stories PARENT_KEY CHILD_KEY
- /assign-reviewer STORY_KEY REVIEWER

**4. Workflow enforcement:**
- /start-sprint (validate → create → initialize)
- /release-prep (check → build → tag)

### ❌ Bad Use Cases

**1. Single operation (just invoke agent):**
```markdown
# Command: /read-file FILENAME
Read a file and display contents
```
**Problem:** Just use Read tool directly

**2. Rarely used workflow:**
```markdown
# Command: /special-one-time-operation
Used once for migration
```
**Problem:** Create agent or do manually, not worth command

**3. Complex logic (better in agent):**
```markdown
# Command: /analyze-and-decide
[500 lines of decision logic]
```
**Problem:** Should be agent, not command

**4. Too granular:**
```markdown
# Command: /git-status
Run git status
```
**Problem:** Just use bash command directly

---

## Command Anti-Patterns

### ❌ Anti-Pattern 1: Doing Work in Command

**Problem:**
```markdown
# Command: /generate-code

1. Read OpenAPI spec
2. Parse spec and extract entities
3. Generate entity classes
4. Generate mappers
5. Generate repositories
[50 more lines of implementation logic]
```

**Why it's bad:**
- Commands should orchestrate, not implement
- Logic should be in agent
- Command becomes unmaintainable

**Fix:**
```markdown
# Command: /generate-code

1. Validate OpenAPI spec exists
2. Invoke api-generator agent
3. Display results
```

### ❌ Anti-Pattern 2: No Error Handling

**Problem:**
```markdown
# Command: /start-story STORY_KEY

1. Create branch feature/STORY_KEY
2. Run story-implementation-planner
3. Done
```

**Why it's bad:**
- What if story doesn't exist?
- What if not in BACKLOG?
- What if git not clean?
- Silent failures confuse users

**Fix:** Add validation and clear error messages at each step

### ❌ Anti-Pattern 3: Parameter Confusion

**Problem:**
```markdown
# Command: /do-thing ARG1 ARG2 ARG3 ARG4

Parameters: ARG1, ARG2, ARG3, ARG4
(no description of what each parameter is)
```

**Why it's bad:**
- Users can't remember parameter order
- Easy to mix up arguments
- No validation help

**Fix:**
```markdown
# Command: /create-api STORY_KEY API_NAME

**Parameters:**
- STORY_KEY: Story identifier (e.g., PRND-2847)
- API_NAME: API resource name in PascalCase (e.g., ProductOffering)

**Validation:**
- STORY_KEY must match pattern PRND-\d+
- API_NAME must be valid Java class name
```

---

## Command Design Checklist

When designing a command, verify:

- [ ] **Orchestrates multiple steps** - Not just single operation
- [ ] **Common workflow** - Used frequently enough to warrant command
- [ ] **Enforces sequencing** - Steps must happen in order
- [ ] **Parameters documented** - Clear descriptions and examples
- [ ] **Prerequisites listed** - User knows when command can be used
- [ ] **Error handling** - Each step has failure path
- [ ] **Delegates to agents** - Doesn't implement complex logic
- [ ] **Size appropriate** - 100-200 lines of orchestration
- [ ] **Clear purpose** - Users understand when to use it
- [ ] **No duplication** - Doesn't replicate existing commands

If any item fails, reconsider whether command is needed.

---

## Command Size Guidelines

**Target:** 100-200 lines

**Breakdown:**
- Header and purpose: ~10 lines
- Parameters documentation: ~10 lines
- Prerequisites: ~10 lines
- Steps (5-10 steps × 10-15 lines each): 50-150 lines
- Error handling: ~20 lines
- Examples/notes: ~10 lines

**Total:** ~110-200 lines

**If exceeding 200 lines:**
- Too much logic in command (move to agent)
- Too many steps (break into multiple commands)
- Too much validation (create validation agent)

---

## Quick Reference

**Command creation decision:**

```
Is it used frequently?
├─ YES → Continue
└─ NO → Don't create command

Does it orchestrate multiple steps?
├─ YES → Continue
└─ NO → Use agent or direct invocation

Does it enforce specific sequencing?
├─ YES → Create command
└─ NO → Maybe not needed

Is logic simple (orchestration only)?
├─ YES → Create command
└─ NO → Put logic in agent, create simple command to invoke
```

**Success criteria:**
- Saves user time (don't need to remember steps)
- Enforces consistency (same workflow every time)
- Prevents errors (validates before executing)
- Clear and maintainable (100-200 lines)
