# Agent Design Examples

Practical examples of well-designed agents following best practices.

---

## Example 1: Refactoring api-generator

### Original Proposal (Anti-Pattern)

**Size:** 5,995 lines total
- Agent: 1,543 lines of behavioral guidance
- Embedded docs: 4,452 lines of reference material

**Problems:**
- Violates size guidelines (should be 200-800 lines)
- Pre-loads massive documentation
- Wastes ~15,000-20,000 tokens upfront
- Knowledge hoarding instead of delegation

### Refactored Design (Best Practice)

**Agent: api-generator.md (~600 lines)**

```markdown
# Agent: api-generator

## Purpose
Generate complete TMF API service from OpenAPI specification.

## Workflow
1. Parse OpenAPI specification
2. Generate external models → compile → fix
3. Generate internal entities → compile → fix
4. Generate polymorphic mappers → compile → fix
5. Generate repository → compile → fix
6. Generate service → compile → fix
7. Generate controller → compile → fix
8. Run full build and tests

## Model
opus (complex code generation decisions requiring creativity)

## Tools
Read, Write, Edit, Bash

## Relies On
- dnext-code-gen skill (auto-activates for TMF API work)
- Reference docs (read on-demand when needed)

## Behavioral Guidance
[~600 lines of specific instructions for each phase...]
[Error handling strategies...]
[When to stop and report issues...]
[Quality criteria for each phase...]
```

**Skill: dnext-code-gen.md (expand to 3-5KB)**

```markdown
# Skill: dnext-code-gen

**Auto-activates when:** Working with TMF APIs, DNext code generation

## Embedded (~3KB)

### Entity Structure Templates
[Common entity patterns...]

### Mapper Decision Trees
- When to use polymorphic mappers
- Standard mapper patterns
- Complex type handling

### Common Validation Patterns
[Validation rules...]

### Repository/Service Patterns
[Standard patterns...]

### Quick Reference for Annotations
[JPA, Spring, validation annotations...]

## References (on-demand)

For detailed polymorphic mapper patterns:
- Read ~/.claude/references/dnext-code-gen/polymorphic-mappers.md

For comprehensive validation rules:
- Read ~/.claude/references/dnext-code-gen/validation-guide.md

For known issues and fixes:
- Read ~/.claude/references/dnext-code-gen/known-issues.md
```

**Reference Files (loaded on-demand):**

`~/.claude/references/dnext-code-gen/`
- `polymorphic-mappers.md` - Detailed patterns with examples
- `validation-guide.md` - Comprehensive validation rules
- `known-issues.md` - Historical problems and solutions

### Token Impact Analysis

**Original approach:**
- Agent: ~15,000-20,000 tokens (all upfront)
- Total: ~15,000-20,000 tokens

**Refactored approach:**
- Agent: ~1,500 tokens (behavioral guidance only)
- Skill: ~1,200 tokens (auto-activates for TMF work)
- References: ~2,000-5,000 tokens (loaded only when needed)
- Total: ~2,700 tokens initial, ~4,700-7,700 tokens if references needed

**Savings:** 70-80% token reduction while maintaining full capability

### Why This Works

**Separation of concerns:**
- Agent: Task orchestration and workflow
- Skill: Domain knowledge and patterns
- References: Detailed examples and edge cases

**Efficiency:**
- Skill auto-activates only for relevant work
- References loaded only when complex scenarios encountered
- No wasted tokens on unused documentation

**Maintainability:**
- Agent updates: Change workflow logic
- Skill updates: Update common patterns
- Reference updates: Add new examples without affecting core

---

## Example 2: Well-Designed Read-Only Agent

**Agent: code-reviewer**

```markdown
# Agent: code-reviewer

## Purpose
Review implementation for code quality, identify issues, create review items.

## Scope
- Analyze existing code (no modifications)
- Check against quality standards
- Create structured review feedback
- Categorize findings by severity

## Workflow

1. **Read Implementation**
   - Read all files in scope
   - Understand code structure
   - Identify patterns used

2. **Quality Analysis**
   - Code organization and structure
   - Error handling patterns
   - Input validation
   - Test coverage
   - Following project conventions

3. **Categorize Findings**
   - Critical: Must fix before merge
   - Major: Should fix, impacts quality
   - Minor: Nice to have improvements

4. **Create Review Items**
   - Add to .stories.md under relevant child story
   - Format: PRND-xxxx#y.R1, R2, etc.
   - Include file/line references
   - Provide specific recommendations

## Tool Access
Read, Grep, Glob, Edit (only for .stories.md review items)

**Restrictions:**
- CANNOT modify implementation code
- CANNOT run builds or tests
- CAN only read and analyze

## Model
sonnet (standard complexity, balanced cost/performance)

## Quality Criteria
[Specific checklist for what to review...]
[Examples of good vs. bad patterns...]
[Project-specific conventions...]
```

**Size:** ~400 lines
**Key feature:** Tool restrictions enforce read-only analysis
**Relies on:** spring-testing skill, code quality standards docs

---

## Example 3: Iterative Implementation Agent

**Agent: implementer**

```markdown
# Agent: implementer

## Purpose
Implement functionality following TDD red-green-refactor cycle.

## Prerequisites
- Failing tests already exist (created by test-first-designer)
- Implementation guide available
- Clear success criteria defined

## Workflow

1. **Red Phase: Verify Failing Tests**
   - Run existing tests
   - Confirm they fail for right reasons
   - Understand what needs to be implemented

2. **Green Phase: Minimal Implementation**
   - Write minimum code to make tests pass
   - Focus on making tests green, not perfect code
   - Run tests after each small change

3. **Refactor Phase: Improve Code**
   - Clean up implementation
   - Remove duplication
   - Improve naming and structure
   - Ensure tests still pass

4. **Iterate**
   - Move to next failing test
   - Repeat red-green-refactor
   - Build functionality incrementally

## Tool Access
Read, Write, Edit, Bash (full implementation access)

## Model
opus (requires creative problem-solving and design decisions)

## When to Stop
- All tests passing
- Code meets quality standards
- Implementation complete per guide

## When to Ask for Help
- Tests pass but behavior seems wrong
- Stuck after 3 refactor attempts
- Unclear requirements in guide
```

**Size:** ~500 lines
**Key feature:** Follows specific methodology (TDD)
**Relies on:** Existing tests, implementation guide, language-specific skills

---

## Common Patterns

### Pattern: Phased Workflow Agent

**Use when:** Task has clear sequential phases

**Structure:**
```markdown
## Workflow
1. Phase 1: [Initial step]
   - Specific actions
   - Success criteria
   - What to do if fails

2. Phase 2: [Next step]
   - Requires Phase 1 complete
   - Specific actions
   - Success criteria

3. Phase N: [Final step]
   - Final verification
   - Deliverables
```

**Example agents:** api-generator (8 phases), implementer (red-green-refactor)

### Pattern: Analysis-Only Agent

**Use when:** Need insights without modifications

**Structure:**
```markdown
## Tool Access
Read, Grep, Glob (no Write/Edit)

## Deliverables
- Analysis report
- Findings documented in specific format
- Recommendations (but no implementation)
```

**Example agents:** code-reviewer, dependency-analyzer

### Pattern: State Management Agent

**Use when:** Tracking state across workflow

**Structure:**
```markdown
## Tool Access
Read, Edit (only for state files like .stories.md)

## Model
haiku (simple state transitions, low cost)

## Workflow
1. Read current state
2. Validate transition is allowed
3. Update state file
4. Confirm change
```

**Example agents:** story-tracker

---

## Anti-Patterns in Agent Design

### ❌ Swiss Army Knife Agent

**Problem:**
```markdown
# Agent: dev-helper

Can do everything:
- Generate code
- Review code
- Run tests
- Fix errors
- Update documentation
- Manage stories
- Deploy applications
```

**Why it's bad:**
- No clear purpose
- Can't optimize model/tools for specific task
- Confusion about when to use
- Violates single responsibility

**Fix:** Create focused agents for each task

### ❌ Knowledge Hoarder Agent

**Problem:**
```markdown
# Agent: java-expert

[3,000 lines of behavioral guidance]
[10,000 lines of Java patterns]
[5,000 lines of Spring framework docs]
[2,000 lines of testing patterns]
```

**Why it's bad:**
- Wastes tokens on embedded docs
- Knowledge should be in skills
- High token cost every invocation

**Fix:** Extract knowledge to skills and references

### ❌ Micro-Task Agent

**Problem:**
```markdown
# Agent: add-getter-setter

Adds getter and setter methods to a class.
```

**Why it's bad:**
- Too narrow scope
- Could be done directly
- Agent overhead not justified

**Fix:** Use command or do directly, don't create agent

---

## Design Checklist

When designing an agent, verify:

- [ ] **Clear single purpose** - Can describe in 1-2 sentences
- [ ] **Appropriate size** - 200-800 lines of behavioral guidance
- [ ] **No embedded docs** - Knowledge in skills/references
- [ ] **Proper tool restrictions** - Only what's needed
- [ ] **Right model tier** - opus/sonnet/haiku based on complexity
- [ ] **Clear workflow** - Steps are explicit
- [ ] **Success criteria** - Knows when done
- [ ] **Error handling** - Knows when to stop and ask
- [ ] **No overlap** - Doesn't duplicate existing agents

If any checklist item fails, reconsider the design.
