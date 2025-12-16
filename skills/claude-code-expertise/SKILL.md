---
name: claude-code-expertise
description: Guides optimal design of Claude Code components (agents, skills, commands) based on empirical evidence. Covers context management principles, component design patterns, decision frameworks, and token optimization. Auto-activates when designing agents, skills, commands, or discussing Claude Code architecture.
---

# Claude Code Component Design Expertise

**Auto-activates when:** Designing agents, skills, commands, or discussing Claude Code architecture
**Purpose:** Guide optimal design of Claude Code components based on empirical evidence
**Evidence base:** Context management study (December 2024), official Claude Code documentation

---

## Context Management: Empirical Foundations

### Proven Characteristics (Test-Validated)

**Persistent Context:**
- Commands/skills persist in context after invocation
- Tool-read data persists with same fidelity as embedded content
- No automatic eviction detected
- Recall quality doesn't degrade over session lifetime

**Token Budget:**
- Total budget: 200,000 tokens per session
- All loaded content counts against budget
- Updates visible in `<system_warning>` tags after tool calls only
- Typical session baseline: 10,000-20,000 tokens

**Granular Access:**
- Multiple similar commands coexist without cross-contamination
- Selective recall works (can request specific content)
- Tested scalability: 5-10 related components viable

**Design implication:** Both comprehensive skills and lightweight-with-external-refs patterns work. Choose based on access frequency and knowledge size, not fear of context limits.

---

## Component Design Principles

### Agents: Task Delegators with Context Isolation

**What they are:**
- Separate context window (preserves main conversation)
- Specific purpose and expertise area
- Custom system prompt for behavioral guidance
- Configured tool access (only what's necessary)

**File Structure:**
```
~/.claude/agents/
├── agent-name.md              # Full agent content (200-800 lines)
│   ├── YAML frontmatter: name, description, model
│   ├── Behavioral guidance and workflow
│   └── References to agent-name/*.md files
└── agent-name/                # Reference documentation
    ├── reference-1.md         # Detailed patterns
    ├── reference-2.md         # Examples
    └── examples/              # Code samples
        └── example.java
```

**Encapsulation Principles:**
- ✅ Self-contained: All references in `~/.claude/agents/{agent}/`
- ✅ Relative paths resolve to `~/.claude/agents/{agent}/<path>`
- ✅ Encapsulation over DRY: Duplicate docs if needed across agents
- ✅ YAML frontmatter: name, description, model (no dashes in name)

**Good design:**
- ✅ Focused single responsibility (1-2 sentence description)
- ✅ Size: 200-800 lines of behavioral guidance
- ✅ Proper tool restrictions (only what's needed)
- ✅ Clear description (enables discovery)
- ✅ Model tier: opus (creative) / sonnet (standard) / haiku (simple)

**Bad design:**
- ❌ Unfocused scope (too many responsibilities)
- ❌ Pre-loaded documentation (>1,000 lines embedded)
- ❌ Over-engineering (agent for single operation)
- ❌ External references outside ~/.claude/agents/{agent}/

**Pattern:** Behavioral guidance in .md file, detailed knowledge in agent/ subdirectory

### Skills: Auto-Activating Capabilities

**What they are:**
- Auto-activate based on context (no explicit invocation)
- Contain embedded knowledge, templates, patterns
- Shared across all agents and main conversation
- Persist in context once activated

**Size guidelines (evidence-based):**
- Sweet spot: 3-5KB (~1,000-1,500 tokens)
- Budget impact: 10-20 comprehensive skills = ~15,000-30,000 tokens
- Leaves ~170K for conversation and work

**Decision matrix:**

| Knowledge Size | Access Frequency | Recommended Approach |
|----------------|------------------|----------------------|
| < 1KB | Any | Embed in skill |
| 1-5KB | Every activation | Embed in skill |
| 1-5KB | Conditional | Embed with conditional sections |
| > 5KB | Every activation | Skill + reference file |
| > 5KB | Conditional | Read on-demand only |

**Good design:**
- ✅ Clear auto-activation triggers (specific contexts)
- ✅ Embedded high-frequency knowledge
- ✅ References to detailed docs for large/rare content

### Commands: Workflow Orchestration

**What they are:**
- Slash commands that expand to full prompt
- Compose multiple agents or operations
- Provide user convenience for common workflows
- Enforce consistent sequencing

**Good use cases:**
- ✅ Multi-agent orchestration
- ✅ Common workflow sequences (used 3+ times/month)
- ✅ Consistent parameter gathering
- ✅ Sequencing enforcement

**Bad use cases:**
- ❌ Single operation (just invoke agent directly)
- ❌ Complex logic (belongs in agent)
- ❌ Rarely used workflow

**Pattern:** 100-200 lines of orchestration logic

---

## Documentation Organization

### Self-Contained Component Pattern

```
~/.claude/
├── agents/
│   ├── agent-name.md           # Full agent (200-800 lines)
│   └── agent-name/             # Agent's reference files
│       ├── reference-1.md
│       └── examples/
├── skills/
│   └── skill-name/             # Self-contained skill
│       ├── SKILL.md            # Main skill (with YAML frontmatter)
│       ├── reference-1.md      # Supporting docs
│       └── examples/
├── commands/
│   ├── command-name.md         # Command definition
│   └── command-name/           # Command's references (if needed)
└── docs/                       # Project work logs, analyses
```

**Encapsulation Principles:**
- Each component is self-contained in its directory
- Reference files live alongside the component
- Relative paths resolve to component's directory
- Encapsulation over DRY: Duplicate docs if multiple components need them
- No shared references directory needed

---

## Anti-Patterns to Avoid

### ❌ Massive Agent Prompts

**Wrong:** 5,995 lines (1,543 agent + 4,452 embedded docs)
**Right:** 600 lines agent + 3-5KB skill + on-demand references
**Impact:** 70-80% token reduction, full capability maintained

### ❌ Pre-loading Everything

**Wrong:** Load all reference docs at session start
**Right:** Read documentation when needed (content persists once loaded)

### ❌ External References

**Wrong:** Reference docs outside component's directory (`~/.claude/references/shared/`)
**Right:** Self-contained components with references in `{component}/` subdirectory
**Note:** Encapsulation over DRY - duplicate docs if needed for independence

### ❌ Skill Proliferation

**Wrong:** 50 tiny skills (100-200 lines each)
**Right:** 10-20 comprehensive skills (3-5KB each)

---

## Design Decision Framework

### When Designing a New Agent

**Questions to ask:**

1. **What is the single primary task?** (1-2 sentences)
2. **What behavioral guidance is needed?** (workflow, methodology, criteria)
3. **What knowledge is needed?** (< 5KB embed, > 5KB reference)
4. **What tools are necessary?** (minimum required)
5. **What model tier?** (opus/sonnet/haiku)

**Size target:** 200-800 lines of behavioral guidance

**Detailed framework:**
- Read [design-frameworks.md](design-frameworks.md)

### When Designing a New Skill

**Questions to ask:**

1. **What triggers auto-activation?** (be specific)
2. **How much knowledge is needed?** (< 5KB or > 5KB)
3. **How often will it be accessed?** (every use, most, sometimes, rarely)
4. **Is knowledge shared with other components?** (deduplicate if yes)
5. **What's the access pattern?** (quick reference, linear, example-driven, decision-driven)

**Size target:** 1-5KB for comprehensive, or 1KB + references

**Detailed framework:**
- Read [design-frameworks.md](design-frameworks.md)

### When Designing a New Command

**Questions to ask:**

1. **Does it orchestrate multiple agents?** (yes → good candidate)
2. **Is it a common workflow?** (3+ times/month)
3. **Does it enforce sequencing?** (yes → adds value)
4. **Does it gather parameters?** (yes → simplifies usage)

**Size target:** 100-200 lines of orchestration

**Detailed framework:**
- Read [design-frameworks.md](design-frameworks.md)

---

## Quick Reference Card

### Component Selection Decision Tree

```
Need to create something?
│
├─ Is it a complex multi-step task?
│  └─ YES → Create AGENT (200-800 lines behavioral)
│     └─ Needs knowledge? → Reference SKILL or docs
│
├─ Is it domain knowledge that auto-activates?
│  └─ YES → Create SKILL (1-5KB)
│     └─ > 5KB total? → Use references
│
├─ Is it workflow orchestration?
│  └─ YES → Create COMMAND (100-200 lines)
│     └─ Invokes agents in sequence
│
└─ Is it detailed reference material?
   └─ YES → Create REFERENCE DOC
      └─ Agents/skills read on-demand
```

### Size Guidelines Summary

| Component | Target Size | Token Cost | What to Include |
|-----------|-------------|------------|-----------------|
| Agent | 200-800 lines | ~500-2,000 | Behavioral guidance, workflow |
| Skill | 1-5KB | ~500-1,500 | Patterns, templates, quick ref |
| Command | 100-200 lines | ~300-600 | Orchestration steps |
| Reference | Any size | 0 until read | Detailed knowledge, examples |

### Pattern Quick Pick

**Use comprehensive skill (3-5KB embedded) when:**
- Knowledge < 5KB total
- Accessed every activation
- Specific to this domain

**Use lightweight skill + references when:**
- Knowledge > 5KB total
- Conditionally accessed
- Shared across domains

**Use agent + skill + docs when:**
- Complex task requiring orchestration
- Domain knowledge exists in skill
- Detailed patterns in reference docs

---

## Monitoring and Optimization

### Token Budget Tracking

**Check periodically:** Look for `<system_warning>` tags after tool calls

**Thresholds:**
- < 30% budget (< 60K): Nominal operation
- 30-50% (60K-100K): Monitor for growth
- 50-75% (100K-150K): Review loaded content
- > 75% (> 150K): Consider refactoring or session restart

### Component Review Criteria

**Agents:**
- [ ] Single clear responsibility?
- [ ] Behavioral guidance only (not massive docs)?
- [ ] Appropriate tool restrictions?
- [ ] Correct model tier?
- [ ] Size: 200-800 lines?

**Skills:**
- [ ] Clear auto-activation triggers?
- [ ] Size appropriate (1-5KB)?
- [ ] High-frequency knowledge embedded?
- [ ] Large references external?
- [ ] Tested for auto-activation?

**Commands:**
- [ ] Orchestrates multiple steps?
- [ ] Common workflow?
- [ ] Enforces sequencing?
- [ ] Size: 100-200 lines?

**Detailed monitoring guide:**
- Read [monitoring-optimization.md](monitoring-optimization.md)

---

## Detailed Examples and Guides

### Agent Design Examples

Practical examples of well-designed agents:
- api-generator refactoring (before/after, token impact)
- code-reviewer (read-only pattern)
- implementer (TDD pattern)
- Common patterns and anti-patterns

**Read:** [agent-design-examples.md](agent-design-examples.md)

### Skill Design Examples

Practical examples of well-designed skills:
- spring-testing (comprehensive embedded)
- api-documentation (lightweight + references)
- dnext-code-gen (domain-specific)
- Size estimation guide

**Read:** [skill-design-examples.md](skill-design-examples.md)

### Command Design Examples

Practical examples of well-designed commands:
- /ready-for-merge (multi-agent orchestration)
- /start-story (workflow sequence)
- /create-api (parameterized command)
- Common patterns and anti-patterns

**Read:** [command-design-examples.md](command-design-examples.md)

### Design Decision Frameworks

Detailed questionnaires for designing components:
- Agent design framework (5 questions with decision trees)
- Skill design framework (5 questions with decision matrices)
- Command design framework (4 questions with validation)
- Complete templates for each component type

**Read:** [design-frameworks.md](design-frameworks.md)

### Monitoring and Optimization

Comprehensive guide to tracking and optimizing components:
- Token budget tracking methodology
- Component review checklists
- Performance patterns
- Optimization techniques
- Measurement framework

**Read:** [monitoring-optimization.md](monitoring-optimization.md)

---

## Evidence Base

**Foundational Guide:**
- Comprehensive synthesis: [foundations.md](foundations.md) - Combines Anthropic engineering insights, empirical study, and official documentation

**Empirical studies:**
- Context management study: [context-study-findings.md](context-study-findings.md)
- Architecture analysis: [architecture-analysis-summary.md](architecture-analysis-summary.md)

**Official documentation:**
- Claude Code agent design guidelines (consult via claude-code-guide agent)

**Related skills:**
- agent-invocation: Effective agent usage and prompt construction
- dnext-code-gen: TMF API code generation patterns
- dnext-architecture: D-NEXT project architecture
- story-conventions: Story file formatting and structure
- story-workflow: Story lifecycle and state management
- spring-testing: Spring/JUnit testing patterns

---

**Skill Status:** Production-ready
**Last Updated:** 2024-12-16 (refactored following own best practices)
**Size:** ~4KB (within 3-5KB guideline)
**Auto-activation:** Designing agents, skills, commands; discussing Claude Code architecture
