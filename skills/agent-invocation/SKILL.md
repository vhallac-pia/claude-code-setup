---
name: agent-invocation
description: Provides patterns for effective task delegation via the Task tool. Covers when to invoke agents vs. direct work, pre-invocation checklists, prompt construction patterns, agent selection criteria, and model tier choices. Auto-activates when about to invoke agents via Task tool, discussing agent selection, or constructing agent prompts.
---

# Effective Agent Invocation and Task Delegation

**Auto-activates when:** About to invoke agents via Task tool, discussing agent selection, constructing agent prompts
**Purpose:** Ensure effective task delegation through well-constructed prompts and appropriate agent selection
**Knowledge base:** Distilled patterns from empirical experience

---

## When to Invoke Agents vs. Direct Work

### Invoke Agent When

✅ **Task requires context isolation** - Planning/analysis separate from implementation
✅ **Task requires specialized expertise** - Domain-specific generation, systematic design
✅ **Task benefits from separate execution** - Long-running exploration, multiple file analysis
✅ **Task requires tool restrictions** - Read-only analysis, state-only operations
✅ **User explicitly requests agent** - Command invocations, workflow steps

### Do Direct Work When

❌ **Simple, immediate tasks** - Reading 2-3 files, specific edits to known locations
❌ **Already have full context** - Information visible, no exploration needed
❌ **Task is part of current flow** - Sequential operations, no isolation benefit
❌ **Overhead exceeds benefit** - Trivial operations, single-step tasks

---

## Pre-Invocation Checklist

Before invoking agent, ensure:

### 1. Task Description is Clear

**Vague:** "Help with the API" / "Review the code" / "Fix the errors"
**Specific:** "Generate TMF ProductOffering API from openapi/product-offering.yaml" / "Review ProductController.java for error handling patterns"

### 2. Context is Provided

**Critical context elements:**
- File paths (specific files to analyze/modify)
- Scope boundaries (what's in scope vs. out)
- Related concepts (story keys, feature names)
- Success criteria (what "done" means)
- Constraints (what must be preserved)

### 3. Clarification Needed?

**Ask when:** Multiple valid approaches, user preferences matter, scope ambiguous
**Don't ask when:** Information in files, standard patterns apply, requirements explicit

### 4. Agent Type Selected

**Named agents:** story-implementation-planner, code-reviewer, test-first-designer, implementer, merge-readiness-checker, design-updater, story-tracker, api-generator
**Generic agents:** Explore (codebase), Plan (strategy), general-purpose (multi-step)

### 5. Model Tier Appropriate

**opus:** Creative problem-solving, context synthesis, complex planning
**sonnet:** Standard workflows, structured analysis, moderate complexity (default)
**haiku:** Simple state tracking, mechanical operations, quick searches

---

## Prompt Construction Template

```markdown
[CLEAR TASK STATEMENT]

Context:
- File paths/locations
- Story keys/identifiers
- Current state/existing code
- Technical stack/project structure

Success Criteria:
- What constitutes completion
- Quality expectations
- Deliverables

Constraints:
- In scope / out of scope
- What must be preserved
- Boundaries and limits

[Optional] Methodology:
- Specific approach if needed
- Reference patterns/examples
- Verification commands
```

---

## Core Patterns (High-Frequency)

### Pattern: Context-Rich Invocation

Agents perform better with explicit context rather than assumptions.

**Example:**
```markdown
Generate TMF ProductOffering API from openapi/product-offering.yaml.

Context:
- Project: Maven multi-module (api-layer module)
- Package: com.dnext.api.productoffering
- Database: PostgreSQL with Hibernate/JPA
- Polymorphic: ProductOffering.characteristics field

Follow 8-phase iterative approach:
[phases listed]

Compile after each phase: mvn clean compile -pl api-layer
```

### Pattern: Explicit Success Criteria

Define exactly when agent should stop.

**Example:**
```markdown
Implementation complete when:
- All endpoints from OpenAPI spec implemented
- Unit tests exist and pass (mvn test)
- Integration tests exist and pass
- Clean build succeeds (mvn clean install)
- Code follows existing patterns
- No compiler warnings
```

### Pattern: Scope Boundaries

Prevent scope creep with clear boundaries.

**Example:**
```markdown
Review ProductController.java for code quality.

In scope:
- Error handling patterns
- Input validation
- Test coverage

Out of scope:
- Performance optimization (separate story)
- Additional features (not in spec)
- Refactoring other controllers

Focus only on in-scope items.
```

### Pattern: Methodology Reference

Reference existing patterns instead of re-explaining.

**Example:**
```markdown
Generate tests following TDD red-green-refactor.

Use patterns from:
- src/test/java/.../OrderControllerTest.java
- spring-testing skill has JUnit 5 and MockMvc patterns

Tests should fail initially (red phase).
```

---

## Agent Selection Quick Guide

### Story Workflow Agents

| State Transition | Agent | Model |
|------------------|-------|-------|
| BACKLOG → DESIGNING | story-implementation-planner | opus |
| DESIGNING → IMPLEMENTING | test-first-designer | sonnet |
| IMPLEMENTING → REVIEWING | implementer | opus |
| REVIEWING → DONE | code-reviewer | sonnet |
| DONE → PR | merge-readiness-checker | sonnet |
| After DONE | design-updater | sonnet |
| State updates | story-tracker | haiku |

### Specialized Agents

- **api-generator** (opus): TMF API from OpenAPI
- **Explore** (haiku/sonnet/opus): Codebase exploration, pattern finding
- **Plan** (opus): Implementation strategy design
- **general-purpose** (sonnet): Multi-step tasks without specialized agent

---

## Common Anti-Patterns

### ❌ Vague Task Descriptions

**Bad:** "Help with the API"
**Fix:** "Generate ProductOffering controller from openapi/product-offering.yaml"

### ❌ Missing File Paths

**Bad:** "Review the controller"
**Fix:** "Review src/main/java/com/dnext/api/product/ProductController.java"

### ❌ Assuming Project Knowledge

**Bad:** "Use our standard approach"
**Fix:** "Follow pattern in OrderController.java: constructor injection, ResponseEntity returns"

### ❌ Over-Specifying Implementation

**Bad:** "First create class, then add field, then add method..."
**Fix:** "Generate controller following OrderController.java pattern, implement endpoints from OpenAPI"

### ❌ No Success Criteria

**Bad:** "Implement the feature"
**Fix:** "Complete when: endpoints work, tests pass, build succeeds, no warnings"

### ❌ Invoking Agent for Trivial Tasks

**Bad:** "Invoke Explore to find README.md"
**Fix:** "Just use Read ./README.md or Glob README.md"

---

## Quick Reference Card

### Pre-Invocation Checklist

- [ ] Task is clear and specific
- [ ] File paths provided
- [ ] Context included (story keys, tech stack, current state)
- [ ] Success criteria defined
- [ ] Scope boundaries set
- [ ] Agent type selected
- [ ] Model tier appropriate
- [ ] Clarifying questions asked if needed
- [ ] References to existing patterns
- [ ] Verification steps specified

### Prompt Essentials

**Always include:**
- Clear task statement (what to do)
- File paths (where to work)
- Context (project specifics, current state)
- Success criteria (when done)

**Often include:**
- Scope boundaries (what's in/out)
- Pattern references (existing code to follow)
- Verification commands (how to check)
- Methodology (approach if specific)

**Sometimes include:**
- Failure recovery (complex tasks)
- Conditional depth (exploratory tasks)
- Phased approach (multi-step generation)

### Agent Selection Decision Tree

```
What type of task?
│
├─ Story lifecycle? → Named agents (planner/designer/implementer/reviewer)
├─ Code generation? → api-generator (TMF) or implementer (other)
├─ Codebase exploration? → Explore (haiku/sonnet/opus by depth)
├─ Planning? → story-implementation-planner (story) or Plan (other)
└─ Multi-step? → general-purpose
```

---

## Detailed Guides (On-Demand)

### Prompt Construction Patterns

Comprehensive patterns for effective prompts:
- Context-rich invocation
- Explicit success criteria
- Scope boundaries
- Methodology reference
- Failure recovery strategy
- Conditional depth
- Incremental refinement
- Strategic pivot
- Phased execution

**Read:** [prompt-construction-patterns.md](prompt-construction-patterns.md)

### Agent Selection Guide

Detailed guide for each agent type:
- When to use each named agent
- Named agent prompt templates
- Generic agent usage patterns
- Model tier selection criteria
- Decision matrices
- Common selection mistakes

**Read:** [agent-selection-guide.md](agent-selection-guide.md)

### Prompt Examples

Concrete good vs. bad examples:
- Code review requests
- API generation
- Test design
- Implementation tasks
- Exploration tasks
- Bug fixes
- Refactoring
- Planning

**Read:** [prompt-examples.md](prompt-examples.md)

### Common Mistakes

Detailed anti-patterns with corrections:
- Not invoking when beneficial
- Invoking for trivial tasks
- Vague descriptions
- Missing file paths
- Assuming project knowledge
- Over-specifying implementation
- No success criteria
- Missing context
- Not asking for clarification
- Wrong agent selection
- No scope boundaries
- Missing verification
- Not referencing patterns
- Ignoring failure recovery

**Read:** [common-mistakes.md](common-mistakes.md)

---

**Skill Status:** Production-ready
**Last Updated:** 2024-12-16 (refactored following best practices)
**Size:** ~4KB (within 3-5KB guideline)
**Auto-activation:** Constructing prompts for Task tool, selecting agents, discussing delegation strategy
