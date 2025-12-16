# Designing Effective Claude Code Components: Evidence-Based Guide

Combines insights from Anthropic's engineering blog, official documentation, and empirical testing of Claude Code's context management.

---

## Why Component Design Matters

"Skills are a simple concept with a correspondingly simple format" - yet their effectiveness depends entirely on design decisions. Poor design wastes tokens, confuses triggering logic, and creates maintenance burden. Good design enables Claude to discover, load, and use exactly what each task requires.

This guide distills principles from three sources:
1. **Anthropic's engineering insights** on Agent Skills architecture
2. **Official Claude Code documentation** on best practices
3. **Empirical testing** of context management (December 2024)

---

## Foundational Principles

### Progressive Disclosure: The Core Pattern

Skills use **layered information architecture** enabling arbitrarily complex capabilities without context bloat:

**Level 1: Metadata (Always Loaded)**
- `name` and `description` fields from YAML frontmatter
- Preloaded into system prompt at startup
- Drives skill discovery and triggering
- Token cost: ~20-50 per skill

**Level 2: SKILL.md Body (Loaded When Triggered)**
- Full content of SKILL.md file
- Loaded only when Claude deems skill relevant
- Should contain essential guidance and pointers
- Target: <500 lines for optimal performance

**Level 3+: Reference Files (Loaded As Needed)**
- Additional bundled files: `reference.md`, `examples.md`, `patterns.md`
- Claude accesses via filesystem tools (Read, Grep)
- Only loaded when explicitly needed
- Token cost: 0 until accessed

**Implication:** Skills can grow arbitrarily large because agents with filesystem access don't load everything simultaneously.

### Self-Contained Architecture

Skills must be **self-contained directories** with all documentation embedded:

```
my-skill/
├── SKILL.md              # Core instructions with YAML frontmatter
├── reference.md          # Detailed API/pattern documentation
├── examples.md           # Concrete usage examples
└── scripts/
    └── helper.py         # Executable utilities
```

**Why:** Enables proper packaging, distribution, and isolation. Skills are modular resources that should work independently.

### Code Integration

Skills can bundle **executable scripts** (Python, JavaScript) that Claude invokes via Bash tools:

**Pattern 1: Execution (Most Common)**
```markdown
Run `analyze_form.py` to extract field definitions.
```
Claude executes script, only output consumes tokens.

**Pattern 2: Reference (For Complex Logic)**
```markdown
See `analyze_form.py` for the field extraction algorithm.
```
Claude reads script as documentation.

**Benefits of bundling scripts:**
- More reliable than generated code
- Saves tokens (no generation needed)
- Ensures consistency across uses
- Enables deterministic operations

---

## Empirical Evidence: Context Management Study

### Test Methodology

Conducted December 2024 using controlled experiments:
- Test 1: Context persistence over session lifetime
- Test 2: Token budget tracking and updates
- Test 3: Granular recall with multiple similar components
- Test 4: Tool-read data vs. embedded content

### Proven Characteristics

**Persistent Context (Test 1)**
- Commands/skills persist after invocation
- No automatic eviction detected
- Recall quality stable over session
- Tool-read data persists with same fidelity as embedded content

**Token Budget (Test 2)**
- Total: 200,000 tokens per session
- All loaded content counts against budget
- Updates visible in `<system_warning>` tags after tool calls only
- Typical baseline: 10,000-20,000 tokens
- Skills: ~500-1,500 tokens each when loaded

**Granular Access (Test 3)**
- Multiple similar commands coexist without cross-contamination
- Selective recall works (can request specific content)
- Tested scalability: 5-10 related components viable
- No confusion between similarly-named items

**Tool-Read Equivalence (Test 4)**
- Data read via Read tool persists identically to embedded content
- No degradation in recall quality
- Supports progressive disclosure pattern
- External files as effective as inline content

### Design Implications

1. **Both patterns work:** Comprehensive skills (3-5KB embedded) AND lightweight + references (1KB + external files) are viable
2. **Choose by access frequency:** High-frequency knowledge → embed; conditional knowledge → reference
3. **Token cost is primary concern:** Not context limits (200K is generous), but efficiency
4. **Progressive loading proven:** External references don't hurt recall, enable scaling

### Thresholds and Monitoring

**Token Budget Utilization:**
- < 30% (< 60K): Nominal operation
- 30-50% (60K-100K): Monitor for growth
- 50-75% (100K-150K): Review loaded content
- > 75% (> 150K): Consider refactoring or session restart

**Measurement:** Check `<system_warning>` tags after tool calls for current usage.

---

## Progressive Disclosure in Practice

### Pattern 1: High-Level Guide with References

**Use when:** Knowledge domain has multiple sub-areas, each substantial but conditionally accessed.

```markdown
# BigQuery Data Analysis

## Available Datasets

**Finance**: Revenue, ARR, billing → See [finance.md](finance.md)
**Sales**: Opportunities, pipeline → See [sales.md](sales.md)
**Product**: API usage, features → See [product.md](product.md)

## Quick Search

Find metrics: `grep -i "revenue" finance.md`
```

**Why this works:** User asking about revenue only triggers finance.md load, not sales or product data.

### Pattern 2: Conditional Details

**Use when:** Basic usage covers 80% of cases, advanced features needed occasionally.

```markdown
# PDF Processing

## Creating Documents

Use pdfplumber for text extraction:
[basic example]

## Advanced Features

**For tracked changes:** See [redlining.md](redlining.md)
**For OOXML details:** See [ooxml-spec.md](ooxml-spec.md)
```

**Why this works:** Most users get answer from SKILL.md; complex cases trigger specific reference loads.

### Pattern 3: Domain-Specific Organization

**Use when:** Multiple domains exist within skill, each with substantial documentation.

```
skill/
├── SKILL.md (overview, navigation)
└── domains/
    ├── authentication.md
    ├── authorization.md
    ├── session-management.md
    └── audit-logging.md
```

**Why this works:** Claude loads only relevant domain docs, not entire security encyclopedia.

### Anti-Pattern: Deeply Nested References

**Don't do this:**
```
SKILL.md → advanced.md → details.md → actual info
```

**Problem:** Claude may use partial reads (head -100) on nested references, missing information.

**Do this instead:**
```
SKILL.md → advanced.md (complete info)
SKILL.md → reference.md (complete info)
SKILL.md → examples.md (complete info)
```

**Rule:** Keep references one level deep from SKILL.md.

---

## Skill Design Principles

### Metadata: The Discovery Engine

**Description field determines triggering.** Claude uses this to select skills from potentially 100+ available.

**Good description (specific, includes triggers):**
```yaml
description: Extract text and tables from PDF files, fill forms, merge documents. Use when working with PDF files or when the user mentions PDFs, forms, or document extraction.
```

**Bad description (vague):**
```yaml
description: Helps with documents
```

**Format requirements:**
- Write in third person ("Processes files" not "I can process")
- Include WHAT skill does AND WHEN to use it
- Use keywords Claude might encounter in queries
- Maximum 1024 characters, but aim for 200-400
- Cannot contain XML tags

**Name requirements:**
- lowercase, numbers, hyphens only
- Maximum 64 characters
- Use gerund form (verb + -ing): `processing-pdfs`, `analyzing-data`
- Avoid vague names: `helper`, `utils`, `tools`

### Size Guidelines

**SKILL.md body:**
- Target: <500 lines for optimal performance
- Should provide overview and navigation
- Split larger content into separate reference files

**Reference files:**
- No size limit (loaded on-demand)
- Include table of contents if >100 lines
- Structure with clear headings for navigation

**Total skill:**
- Comprehensive embedded: 3-5KB total (all in SKILL.md)
- Lightweight + references: 1KB core + unbounded references

### Decision Matrix: Embed vs. Reference

| Knowledge Size | Access Frequency | Pattern | Rationale |
|---------------|------------------|---------|-----------|
| < 3KB | Every use | Embed in SKILL.md | Minimal cost, always needed |
| < 3KB | Conditional | Embed in SKILL.md | Still small enough |
| 3-5KB | Every use | Embed in SKILL.md | Acceptable cost for constant use |
| 3-5KB | Conditional | Reference files | Save tokens on conditional access |
| > 5KB | Every use | Reference files | Too large even for constant use |
| > 5KB | Conditional | Reference files | Definitely use progressive disclosure |

**Access frequency definitions:**
- Every use: >90% of skill invocations need this knowledge
- Most: 60-90% need it
- Conditional: <60% need it

### Content Principles

**Be Concise**

Claude is already very smart. Only add context Claude doesn't have.

**Challenge each piece:**
- "Does Claude really need this explanation?"
- "Can I assume Claude knows this?"
- "Does this paragraph justify its token cost?"

**Good example (50 tokens):**
```markdown
## Extract PDF text

Use pdfplumber:

```python
import pdfplumber
with pdfplumber.open("file.pdf") as pdf:
    text = pdf.pages[0].extract_text()
```
```

**Bad example (150 tokens):**
```markdown
## Extract PDF text

PDF (Portable Document Format) files are a common file format...
There are many libraries available, but we recommend pdfplumber
because it's easy to use and handles most cases well. First,
you'll need to install it using pip...
```

**Set Appropriate Degrees of Freedom**

Match specificity to task fragility:

**High freedom (text-based):** Multiple valid approaches, heuristics guide decisions
```markdown
1. Analyze code structure
2. Check for bugs or edge cases
3. Suggest improvements
```

**Medium freedom (pseudocode):** Preferred pattern exists, some variation acceptable
```markdown
def generate_report(data, format="markdown"):
    # Process data
    # Generate output in specified format
```

**Low freedom (specific script):** Operations fragile, consistency critical
```markdown
Run exactly: `python migrate.py --verify --backup`
Do not modify or add flags.
```

**Analogy:**
- Narrow bridge with cliffs = low freedom (specific instructions)
- Open field with no hazards = high freedom (general direction)

### Test with All Models

Skills act as additions to models, effectiveness depends on base model.

**Testing considerations:**
- **Haiku**: Does skill provide enough guidance?
- **Sonnet**: Is skill clear and efficient?
- **Opus**: Does skill avoid over-explaining?

What works for Opus might need more detail for Haiku.

---

## Agent Design Principles

### Core Responsibility

Agents provide **behavioral guidance** for complex multi-step tasks:

**Size target:** 200-800 lines of guidance
**Token cost:** ~500-2,000 when loaded

**Questions to ask:**
1. What is agent's core responsibility? (single clear purpose)
2. What behavioral guidance is needed? (workflow, methodology, criteria)
3. What knowledge is needed? (< 5KB embed, > 5KB reference)
4. What tools are necessary? (minimum required)
5. What model tier? (opus/sonnet/haiku based on complexity)

### Agent + Skill Pattern

**Best practice:** Agents orchestrate behavior, skills provide knowledge.

**Example:**
```
Agent: code-reviewer
- Behavioral: What to check, how to assess, when to flag issues
- References: code-quality-standards skill (patterns, thresholds, examples)

Agent: api-generator
- Behavioral: 8-phase generation workflow, error recovery, iteration
- References: dnext-code-gen skill (TMF patterns, polymorphic handling)
```

**Anti-pattern:** Massive agent with embedded reference material duplicating what skills could provide.

### Tool Restrictions

Use `allowed-tools` in agent prompt (not skill YAML) to constrain capabilities:

**Read-only agent:**
```markdown
You have access to: Read, Grep, Glob
You do NOT have access to: Edit, Write, Bash
```

**Benefits:** Safety, clarity, prevents accidental modifications.

---

## Iteration and Evaluation

### Evaluation-Driven Development

Anthropic recommendation: **Build evaluations BEFORE writing extensive documentation.**

**Process:**
1. **Identify gaps:** Run Claude on tasks without skill, document failures
2. **Create evaluations:** Build 3+ scenarios testing gaps
3. **Establish baseline:** Measure performance without skill
4. **Write minimal instructions:** Address gaps, pass evaluations
5. **Iterate:** Execute evaluations, refine based on results

**Why:** Ensures solving actual problems, not imagined requirements.

### Iterate with Claude

"Work with one instance of Claude ('Claude A') to create a skill that will be used by other instances ('Claude B')."

**Creating new skill:**
1. Complete task with Claude A using normal prompting
2. Notice what context you repeatedly provide
3. Ask Claude A to create skill capturing that pattern
4. Review for conciseness (remove obvious explanations)
5. Test with Claude B on similar tasks
6. Iterate based on observations

**Improving existing skill:**
1. Use skill in real workflows (Claude B)
2. Observe where Claude B struggles or succeeds
3. Return to Claude A with observations
4. Claude A suggests improvements
5. Apply changes, test with Claude B
6. Repeat based on usage

**Why this works:** Claude models understand both agent needs and effective instruction format.

### Observe Navigation Patterns

Watch how Claude actually uses skills:

**Signals to monitor:**
- Unexpected exploration paths (structure not intuitive)
- Missed connections (references not explicit enough)
- Overreliance on certain sections (content should move to SKILL.md)
- Ignored content (file unnecessary or poorly signaled)

**Iterate based on observations, not assumptions.**

---

## Security Considerations

### Trust and Audit Requirements

Skills expand capabilities through instructions and code, creating vulnerabilities.

**Anthropic's recommendation:** "Install skills only from trusted sources."

**For less-trusted skills:**
1. **Audit bundled code:** Review all scripts for malicious operations
2. **Check dependencies:** Verify required packages are legitimate
3. **Examine instructions:** Look for directions to untrusted external connections
4. **Review bundled resources:** Ensure data files are benign

**Red flags:**
- Instructions directing Claude to untrusted URLs
- Code with obfuscated logic
- Unnecessary network permissions
- Requests for sensitive information without clear need

### Principle of Least Privilege

**For skills:**
- Don't request tools skills don't need
- Use `allowed-tools` to restrict agent capabilities
- Document why each tool is necessary

**For agents:**
- Specify minimum required tools
- Explain tool restrictions in agent prompt
- Consider read-only agents for analysis tasks

---

## Common Anti-Patterns

### Skills

**❌ Massive SKILL.md:** Embedding 50KB of documentation instead of using references
**✓ Fix:** Split into SKILL.md (overview) + reference files (details)

**❌ Vague description:** "Helps with files"
**✓ Fix:** "Extract text from PDF files, fill forms, merge documents. Use when working with PDFs."

**❌ Assuming knowledge:** "Use our standard approach"
**✓ Fix:** "Follow pattern in example.py: [concrete example]"

**❌ Deep nesting:** SKILL.md → advanced.md → details.md
**✓ Fix:** All references one level from SKILL.md

**❌ Over-explaining:** "PDF stands for Portable Document Format..."
**✓ Fix:** Assume Claude knows basics, provide only novel context

### Agents

**❌ Embedding reference material:** 800-line agent with API documentation
**✓ Fix:** 300-line agent + reference to api-documentation skill

**❌ No tool restrictions:** Agent can write when it should only read
**✓ Fix:** Explicit allowed-tools list, explain restrictions

**❌ Vague workflow:** "Implement the feature"
**✓ Fix:** Step-by-step workflow with decision points

**❌ No verification steps:** Generate code without testing
**✓ Fix:** Explicit verification: compile, test, validate

---

## Quick Reference

### Skill Design Checklist

**Metadata:**
- [ ] Name: lowercase, hyphens, gerund form
- [ ] Description: what it does + when to use (200-400 chars)
- [ ] Description in third person
- [ ] Keywords for discovery included

**Structure:**
- [ ] SKILL.md < 500 lines
- [ ] Additional details in reference files
- [ ] References one level deep (not nested)
- [ ] Self-contained (all files in skill directory)

**Content:**
- [ ] Concise (no over-explaining)
- [ ] Appropriate degrees of freedom
- [ ] Examples are concrete, not abstract
- [ ] Consistent terminology throughout

**Testing:**
- [ ] Tested with haiku, sonnet, opus
- [ ] Evaluations created
- [ ] Real usage observed
- [ ] Iterated based on observations

### Agent Design Checklist

**Responsibility:**
- [ ] Single clear purpose
- [ ] Behavioral guidance only (not reference material)
- [ ] Size: 200-800 lines

**Tools:**
- [ ] Minimum necessary tools specified
- [ ] Tool restrictions explained
- [ ] Appropriate for autonomy level

**Workflow:**
- [ ] Clear steps and decision points
- [ ] Verification checkpoints
- [ ] Error recovery guidance
- [ ] Success criteria defined

**Knowledge:**
- [ ] References skills for domain knowledge
- [ ] Embeds only critical context
- [ ] Points to reference docs for details

### Progressive Disclosure Patterns

**High-level guide + references:**
```
SKILL.md: Overview + pointers → topic1.md, topic2.md, topic3.md
```

**Conditional details:**
```
SKILL.md: Basic usage + advanced → advanced.md
```

**Domain organization:**
```
SKILL.md: Navigation → domains/auth.md, domains/sessions.md, domains/audit.md
```

---

## Monitoring and Measurement

### Token Budget Tracking

**Check:** `<system_warning>` tags after tool calls

**Interpretation:**
```
Token usage: 45000/200000; 155000 remaining
             ^^^^^ current   ^^^^^^^ available
```

**Thresholds:**
- < 60K: Green (nominal)
- 60K-100K: Yellow (monitor)
- 100K-150K: Orange (review)
- > 150K: Red (refactor or restart)

### Component Review Triggers

**Skills:**
- Skill rarely triggers when expected → Review description
- Skill always loads same reference files → Embed in SKILL.md
- SKILL.md > 500 lines → Split into references
- High token cost relative to value → Condense content

**Agents:**
- Agent often exceeds task scope → Tighten boundaries
- Agent requests tools it doesn't need → Review allowed-tools
- Agent repeatedly fails at task → Add guidance or examples
- Agent prompt > 800 lines → Extract knowledge to skill

---

## Integration with Development Workflow

### Skill Development Lifecycle

1. **Identify gap:** Task Claude struggles with consistently
2. **Create evaluation:** 3+ test cases demonstrating gap
3. **Measure baseline:** Claude's success rate without skill
4. **Design skill:** Minimal content to address gap
5. **Test with eval:** Compare against baseline
6. **Iterate:** Refine based on results
7. **Deploy:** Add to ~/.claude/skills/
8. **Monitor usage:** Observe real-world application
9. **Maintain:** Update based on observations

### Agent Development Lifecycle

1. **Define responsibility:** What task does agent orchestrate?
2. **Identify knowledge needs:** What skills should it reference?
3. **Design workflow:** Step-by-step behavioral guidance
4. **Specify tools:** Minimum set for task completion
5. **Set boundaries:** In-scope vs. out-of-scope
6. **Test execution:** Run on representative tasks
7. **Observe behavior:** Where does agent struggle?
8. **Refine guidance:** Iterate based on observations
9. **Deploy:** Add to ~/.claude/agents/
10. **Monitor effectiveness:** Success rate on tasks

---

## Future Directions

Per Anthropic's engineering team:

**Near term:**
- Enhanced lifecycle support (creation, editing, discovery, sharing)
- Integration with Model Context Protocol (MCP) servers
- Improved skill packaging and distribution

**Long term:**
- Agents autonomously creating their own skills
- Agents evaluating skill effectiveness
- Dynamic skill composition based on task requirements

**Current state:** Skills are "a simple concept with a simple format" - accessible for organizations, developers, and end users without extensive custom engineering.

---

## Summary

**Core principle:** Progressive disclosure enables arbitrarily complex capabilities while maintaining efficiency.

**Design for discovery:** Metadata drives skill selection - invest in clear, specific descriptions.

**Test empirically:** Our study proves both comprehensive and lightweight patterns work; choose based on access frequency and knowledge size.

**Iterate with Claude:** Best skills emerge from observing real usage and collaborating with Claude itself.

**Monitor constantly:** Token budget and usage patterns reveal optimization opportunities.

**Trust but verify:** Audit skills from untrusted sources; security matters.

---

**Document Status:** Production-ready
**Sources:** Anthropic engineering blog (2024), Official Claude Code documentation, Empirical context management study (December 2024)
**Last Updated:** 2024-12-16
