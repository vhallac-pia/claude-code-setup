# Component Monitoring and Optimization

Guidelines for tracking and optimizing Claude Code component performance.

---

## Token Budget Tracking

### How Token Tracking Works

**Updates appear in `<system_warning>` tags:**
```
<system_warning>Token usage: 45,443/200,000; 154,557 remaining</system_warning>
```

**Key insight:** Token counts update **only after tool calls**, not after every message.

**What this means:**
- User messages don't trigger updates
- Slash commands don't trigger updates
- Only Read, Write, Grep, Glob, Bash, etc. trigger updates
- Check token counts after tool use for accurate tracking

### Tracking Methodology

**Establish baseline:**
```
1. Start fresh session
2. Use any tool (e.g., Read README.md)
3. Check <system_warning> for baseline
```

**Typical baselines:**
- Fresh session, no skills loaded: ~10,000-20,000 tokens
- After global CLAUDE.md loaded: ~12,000-22,000 tokens
- First tool use in session: Good baseline point

**Track deltas:**
```
Before operation: 45,443 tokens used
After operation: 50,184 tokens used
Delta: 4,741 tokens consumed
```

**Attribute consumption:**
- Identify what happened between measurements
- Skills activated?
- Commands invoked?
- Large files read?
- Agents spawned?

### Budget Thresholds

**Total budget:** 200,000 tokens per session

**Consumption stages:**

**< 30% (< 60,000 tokens) - Nominal operation:**
- Fresh to moderate session
- Normal skill/command usage
- No action needed
- Continue working normally

**30-50% (60,000-100,000 tokens) - Monitor for growth:**
- Typical mid-session state
- Watch what's consuming tokens
- Consider if large files needed
- Still comfortable range

**50-75% (100,000-150,000 tokens) - Review loaded content:**
- Higher than typical
- Check what's loaded in context:
  - Multiple large skills activated?
  - Large reference docs read?
  - Many agent invocations?
- Consider: Is all content still needed?
- Options:
  - Continue if work requires it
  - Restart session if content not needed anymore

**> 75% (> 150,000 tokens) - Take action:**
- Critical threshold approaching
- Actions:
  1. Review token usage pattern
  2. Identify heavy consumers
  3. Options:
     - Complete current task and restart session
     - Refactor heavy components (if skills/agents too large)
     - Use /clear or /reset if available
     - Start new session for new work

**> 90% (> 180,000 tokens) - Session restart imminent:**
- Approaching budget limit
- Finish current critical task
- Plan to restart session soon
- Save any important context manually

### Token Budget Analysis

**Questions to ask when budget is high:**

1. **Which skills activated?**
   - Expected activations?
   - Unexpected activations suggest trigger too broad
   - Each 3-5KB skill ≈ 1,000-1,500 tokens

2. **What files were read?**
   - Large files (>1,000 lines) consume significant tokens
   - Multiple reads of same file? (Should persist in context)
   - Unnecessary files read?

3. **How many agents invoked?**
   - Each agent has separate context
   - Agent context counts against budget
   - Multiple agents expected or excessive?

4. **Were large commands used?**
   - Commands can load substantial content
   - Expected for workflow commands
   - Check command size if unexpectedly high

5. **Any reference docs loaded?**
   - On-demand reference docs consumed tokens
   - Expected for complex scenarios
   - Still needed or can continue without?

### Optimization Strategies

**When skills are heavy:**
- Refactor to lightweight + references pattern
- Move large examples to reference files
- Keep activation triggers specific (avoid over-activation)

**When files consume too much:**
- Read selectively (specific line ranges)
- Use Grep instead of reading entire files
- Glob to find files, Read only necessary ones

**When agents are expensive:**
- Review agent prompt size (should be 200-800 lines)
- Check if agent embeds large docs (should use skills/references)
- Consider haiku model for simple agents (lower token cost)

**When commands load heavily:**
- Review command size (should be 100-200 lines)
- Check for embedded logic (should delegate to agents)
- Simplify orchestration if overly complex

---

## Component Review Criteria

### Agent Review Checklist

Use this checklist periodically or when agent seems problematic:

- [ ] **Single clear responsibility?**
  - Can describe purpose in 1-2 sentences
  - Doesn't overlap significantly with other agents
  - Users understand when to use vs. not use

- [ ] **Behavioral guidance only (not massive docs)?**
  - Size: 200-800 lines of guidance
  - No embedded reference material (> 500 lines)
  - Knowledge delegated to skills or reference docs

- [ ] **Appropriate tool restrictions?**
  - Only tools necessary for task
  - Read-only agents don't have Write/Edit
  - Write access justified and documented

- [ ] **Correct model tier?**
  - opus: Complex creative tasks, synthesis, planning
  - sonnet: Standard workflows, balanced (default)
  - haiku: Simple mechanical operations, state tracking

- [ ] **Size: 200-800 lines?**
  - Within guidelines
  - If larger: Contains too much embedded knowledge
  - If smaller: Might be too simple (command instead?)

**If any checkbox fails:** Review and refactor agent

### Skill Review Checklist

Use this checklist for new skills or when reviewing existing:

- [ ] **Clear auto-activation triggers?**
  - Specific contexts listed
  - Not too vague ("when coding")
  - Not too broad (activates unnecessarily)
  - Tested and verified to activate when expected

- [ ] **Size appropriate (1-5KB)?**
  - Comprehensive embedded: 3-5KB
  - Lightweight + references: 1-2KB embedded
  - Exceeding 5KB: Should use references instead

- [ ] **High-frequency knowledge embedded?**
  - Common patterns, templates in skill
  - Quick reference material included
  - Frequently used decision trees embedded

- [ ] **Large references external?**
  - Detailed examples in reference files
  - Advanced patterns in references
  - Large datasets externalized
  - On-demand reading configured

- [ ] **Tested for auto-activation?**
  - Verified it activates in expected contexts
  - Confirmed no unexpected activations
  - Token cost measured and acceptable

**If any checkbox fails:** Review and refactor skill

### Command Review Checklist

Use this checklist when creating commands or reviewing existing:

- [ ] **Orchestrates multiple steps?**
  - Not just single operation
  - Sequences agents or operations
  - Provides value beyond direct invocation

- [ ] **Common workflow?**
  - Used at least 3+ times per month
  - Saves significant user time
  - Worth maintaining

- [ ] **Enforces sequencing?**
  - Steps must happen in order
  - Prevents user errors
  - Validates prerequisites

- [ ] **Size: 100-200 lines?**
  - Within guidelines
  - If larger: Too much logic (move to agent)
  - If smaller: Might not need to be command

**If any checkbox fails:** Review whether command is needed

---

## Performance Patterns

### Pattern: Session Management

**Healthy session pattern:**
```
Start: 10-15K tokens (baseline)
After loading skills: 20-30K tokens (5-8 skills)
Mid-session work: 40-60K tokens (reading files, agents)
End of major task: 60-90K tokens (heavy work)
Session end: Restart before exceeding 150K
```

**Unhealthy session pattern:**
```
Start: 10-15K tokens (baseline)
First command: 50K tokens (what loaded?!)
After 2-3 operations: 100K tokens (too fast)
Mid-session: 150K tokens (approaching limit)
Problem: Something consuming tokens excessively
```

**Action:** If session consumption is faster than expected, investigate what's loading.

### Pattern: Skill Activation Monitoring

**Expected activation:**
```
User: "Write a test for ProductController"
→ spring-testing skill activates (~1,200 tokens)
→ Normal and expected
```

**Unexpected activation:**
```
User: "What is the weather today?"
→ dnext-code-gen skill activates (???)
→ Activation trigger too broad
→ Fix skill's auto-activation criteria
```

**How to detect:**
- Token jump without obvious cause
- Skill content appears when not relevant
- Check skill activation triggers

### Pattern: Reference Loading Strategy

**Efficient pattern:**
```
1. Skill activates (core patterns, ~1-2KB)
2. Work with core patterns (80% of cases)
3. Complex case encountered
4. Read specific reference doc (~2-3KB)
5. Continue with full knowledge
```

**Inefficient pattern:**
```
1. Load all references upfront (~10KB)
2. Use only 20% of content
3. Waste 80% of tokens on unused material
```

**Guideline:** Load references on-demand, not preemptively.

### Pattern: Agent Context Isolation

**Good practice:**
```
1. Invoke agent for focused task
2. Agent works in isolated context
3. Returns concise results
4. Main context remains lean
```

**Bad practice:**
```
1. Do complex work directly in main context
2. Load many files into main context
3. Main context becomes bloated
4. High token cost for entire session
```

**Guideline:** Use agents for complex multi-file work to keep main context lean.

---

## Component Optimization Techniques

### Technique 1: Skill Splitting

**When to split:**
- Skill > 5KB
- Knowledge has distinct domains
- Some parts rarely used together

**How to split:**

**Before (monolithic):**
```
# Skill: java-everything (12KB)
- Core syntax (2KB)
- Spring framework (3KB)
- Testing patterns (3KB)
- Build tools (2KB)
- Deployment (2KB)
```

**After (split):**
```
# Skill: java-core (2KB)
- Core syntax
- Common patterns

# Skill: spring-patterns (3KB)
- Spring framework specifics

# Skill: java-testing (3KB)
- Testing patterns

# References: (in demand)
- build-tools.md
- deployment.md
```

**Benefit:** Activate only relevant skills, reduce baseline cost

### Technique 2: Reference Extraction

**When to extract:**
- Skill > 5KB
- Contains large examples
- Detailed edge cases rarely needed

**How to extract:**

**Before:**
```
# Skill: api-patterns (8KB embedded)
- Core patterns (2KB)
- Common examples (2KB)
- Detailed examples (2KB)
- Edge cases (2KB)
```

**After:**
```
# Skill: api-patterns (4KB embedded)
- Core patterns (2KB)
- Common examples (2KB)
- Pointer to references

# Reference: api-patterns/
- detailed-examples.md (2KB)
- edge-cases.md (2KB)
```

**Benefit:** 50% token reduction, full capability maintained

### Technique 3: Agent Prompt Condensing

**When to condense:**
- Agent > 800 lines
- Contains embedded knowledge
- Repeats information from skills

**How to condense:**

**Before:**
```
# Agent: code-reviewer (1,200 lines)
- Workflow (300 lines)
- Quality criteria (400 lines)
- Spring testing patterns (300 lines) ← Should be in skill!
- Example reviews (200 lines) ← Should be reference!
```

**After:**
```
# Agent: code-reviewer (400 lines)
- Workflow (300 lines)
- Quality criteria (100 lines)
- References spring-testing skill (auto-activates)
- Reads quality-standards.md when needed
```

**Benefit:** 67% size reduction, relies on skills for knowledge

### Technique 4: Command Simplification

**When to simplify:**
- Command > 200 lines
- Contains complex logic
- Reimplements agent work

**How to simplify:**

**Before:**
```
# Command: /complex-workflow (350 lines)
[100 lines of validation logic]
[150 lines of processing logic]
[100 lines of error handling]
```

**After:**
```
# Command: /complex-workflow (120 lines)
[20 lines of validation]
Invoke validation-agent (handles complex validation)
Invoke processing-agent (handles processing)
[20 lines of error handling]
```

**Benefit:** Simpler command, leverages agents for complexity

---

## Measurement Framework

### Baseline Measurements

**Take baselines periodically:**
```
1. Fresh session start
2. First tool call
3. After first command
4. After first agent
5. Mid-session checkpoint
6. End of session
```

**Record:**
- Token count
- What loaded between measurements
- Time elapsed
- Work accomplished

**Use for:**
- Understanding typical consumption patterns
- Identifying anomalies
- Optimizing heavy components

### Component Cost Analysis

**Measure individual components:**

**Skill cost:**
```
Before activation: 25,000 tokens
Invoke skill trigger (e.g., "write test")
After activation: 26,500 tokens
Skill cost: 1,500 tokens
```

**Command cost:**
```
Before command: 30,000 tokens
Run /command
After command: 32,000 tokens
Command cost: 2,000 tokens (including any agents invoked)
```

**Agent cost:**
```
Before agent: 35,000 tokens
Invoke agent
After agent: 40,000 tokens
Agent cost: 5,000 tokens (agent context + results)
```

**Use measurements to:**
- Identify expensive components
- Prioritize optimization efforts
- Validate refactoring benefits

### Refactoring Impact Measurement

**Before refactoring:**
```
Component size: X lines
Token cost: Y tokens
Usage frequency: Z times/week
Weekly token impact: Y × Z
```

**After refactoring:**
```
Component size: X' lines (target: reduce)
Token cost: Y' tokens (target: reduce)
Usage frequency: Z times/week (same)
Weekly token impact: Y' × Z (should be lower)
Savings: (Y - Y') × Z tokens/week
```

**Example:**
```
Before: 8KB skill × 10 uses/week = ~24,000 tokens/week
After: 4KB skill × 10 uses/week = ~12,000 tokens/week
Savings: 12,000 tokens/week (50% reduction)
```

---

## Quick Reference

### Token Budget Quick Check

```
Current usage: [Check <system_warning>]
│
├─ < 60K (30%)
│  └─ ✓ Normal operation
│
├─ 60-100K (30-50%)
│  └─ ⚠ Monitor growth
│
├─ 100-150K (50-75%)
│  └─ ⚠ Review loaded content
│
└─ > 150K (75%+)
   └─ ⛔ Consider session restart
```

### Component Health Indicators

**Healthy component:**
- ✓ Clear, specific purpose
- ✓ Size within guidelines
- ✓ Used regularly
- ✓ Positive user feedback
- ✓ Measured token cost acceptable

**Unhealthy component:**
- ⛔ Purpose unclear or overlapping
- ⛔ Size exceeding guidelines
- ⛔ Rarely used
- ⛔ User confusion or errors
- ⛔ Excessive token consumption

### Optimization Priority

**High priority:**
- Components > 2x size guideline
- High token cost + frequent use
- User-reported issues

**Medium priority:**
- Components 1.5-2x size guideline
- Moderate token cost, moderate use
- Minor user friction

**Low priority:**
- Components within guidelines
- Low token cost or infrequent use
- No user issues

Focus optimization effort on high-priority components for maximum impact.
