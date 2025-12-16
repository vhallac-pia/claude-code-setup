# Prompt Construction Patterns for Agent Invocation

Distilled patterns for constructing effective agent prompts based on empirical experience.

---

## Pattern: Context-Rich Invocation

**When to use:** Every agent invocation

**Structure:**
```markdown
[Task statement]

Context:
- Project structure: [architecture details]
- Package/location: [specific paths]
- Technical stack: [frameworks, databases]
- Special handling: [polymorphic fields, edge cases]

Approach:
[Specific methodology if applicable]

Commands:
[Exact commands for compilation, testing, verification]
```

**Why this works:**
- Agent doesn't need to guess project specifics
- Special requirements made explicit upfront
- Verification commands provided (reduces back-and-forth)
- Prevents assumptions that lead to wrong approach

**Example:**
```markdown
Generate TMF ProductOffering API service from openapi/product-offering.yaml.

Context:
- Project: Maven multi-module (common-core is dependency)
- Package: com.dnext.api.productoffering
- Database: PostgreSQL with Hibernate/JPA
- Special: ProductOffering has @Characteristic (polymorphic handling required)

Approach:
Follow 8-phase iterative generation:
1. External models → compile → fix
2. Internal entities → compile → fix
3. Polymorphic mappers → compile → fix
[etc.]

Compile after each phase: mvn clean compile -pl api-layer
```

---

## Pattern: Explicit Success Criteria

**When to use:** When "done" is ambiguous

**Structure:**
```markdown
[Task statement]

Implementation is complete when:
- [Specific deliverable 1]
- [Specific deliverable 2]
- [Verification step 1]
- [Verification step 2]
- [Quality bar: patterns followed, no warnings]
```

**Why this works:**
- Agent knows exactly when to stop
- Can self-verify completion
- Prevents under/over-implementation
- Quality expectations clear

**Example:**
```markdown
Implement ProductOffering REST controller.

Complete when:
- All endpoints from OpenAPI spec implemented
- Unit tests exist and pass (mvn test)
- Integration tests exist and pass
- Clean build succeeds (mvn clean install)
- Code follows OrderController.java patterns
- No compiler warnings
```

**Anti-pattern:**
```markdown
Implement the feature and make sure it works.
```
Problem: "Works" is subjective, no clear stopping point

---

## Pattern: Scope Boundaries

**When to use:** To prevent scope creep

**Structure:**
```markdown
[Task statement]

In scope:
- [Specific item 1]
- [Specific item 2]
- [Specific item 3]

Out of scope:
- [Related but separate item 1]
- [Future work item 2]
- [Different area item 3]

Focus only on in-scope items.
```

**Why this works:**
- Prevents "helpful" but unwanted suggestions
- Keeps work focused and manageable
- Saves time and tokens
- Clear boundaries reduce confusion

**Example:**
```markdown
Review ProductController.java for code quality.

In scope:
- Error handling patterns
- Input validation
- Test coverage
- REST conventions

Out of scope:
- Performance optimization (separate story)
- Additional features (not in spec)
- Refactoring other controllers
- Database schema changes

Focus only on in-scope items.
```

---

## Pattern: Methodology Reference

**When to use:** When established patterns exist

**Structure:**
```markdown
[Task statement]

Follow patterns from:
- [Existing example file 1]
- [Existing example file 2]
- [Auto-activating skill that has patterns]

[Brief methodology note]
```

**Why this works:**
- Leverages existing knowledge (skills, examples)
- Avoids repeating well-known patterns
- Shorter prompts, same effectiveness
- Ensures consistency with codebase

**Example:**
```markdown
Generate tests following TDD red-green-refactor.

Use patterns from:
- src/test/java/com/dnext/api/order/OrderControllerTest.java
- spring-testing skill (has JUnit 5 and MockMvc patterns)

Tests should fail initially (red phase).
```

**Anti-pattern:**
```markdown
Generate tests, and make sure they follow the Arrange-Act-Assert pattern,
and use JUnit 5, and mock dependencies with Mockito, and use @WebMvcTest
for controllers, and use MockMvc for HTTP testing...
```
Problem: Re-explaining what skills/examples already cover

---

## Pattern: Failure Recovery Strategy

**When to use:** For complex or error-prone tasks

**Structure:**
```markdown
[Task statement]

If [specific error/problem]:
1. [Recovery step 1]
2. [Recovery step 2]
3. [When to stop and report]

Known issues:
- [Issue 1 and handling]
- [Issue 2 and handling]
```

**Why this works:**
- Agent has recovery strategy upfront
- Won't get stuck in loops
- References helpful resources
- Knows when to ask for help vs. retry

**Example:**
```markdown
Generate polymorphic mapper for ProductOffering.

If compilation fails:
1. Read error messages carefully
2. Fix incrementally (don't regenerate everything)
3. Recompile after each fix
4. If stuck after 3 attempts, stop and report

Known issue:
- Polymorphic @Characteristic needs special handling
- Reference existing CharacteristicMapper.java for pattern
```

---

## Pattern: Conditional Depth

**When to use:** For exploratory tasks

**Structure:**
```markdown
[Task statement]

Initial scan: [Quick overview approach]
If [condition]: [Deeper analysis]
If [different condition]: [Alternative approach]
If [third condition]: [Recommendation approach]

Don't [exhaustive analysis] - identify patterns and summarize.
```

**Why this works:**
- Efficient exploration
- Agent adapts depth to findings
- Focuses on actionable insights
- Avoids wasted effort on low-value details

**Example:**
```markdown
Explore error handling patterns in API layer.

Quick scan: Look for try-catch blocks, exception classes
If patterns found: Analyze 2-3 examples in detail
If inconsistent: Report variations, suggest standardization
If none found: Report gap, recommend approach

Don't analyze every file - identify patterns and report summary.
```

---

## Pattern: Incremental Refinement

**When to use:** For complex or ambiguous tasks

**Approach:**
```
Step 1: Start with clear outcome and basic context
Step 2: Agent asks clarifying questions or proposes approach
Step 3: Refine prompt based on agent feedback
Step 4: Agent proceeds with clear direction
```

**Don't:**
- Try to anticipate every detail upfront
- Over-specify when uncertainty exists
- Guess at requirements

**Do:**
- Start with clear outcome statement
- Let agent identify information gaps
- Refine collaboratively
- Build understanding iteratively

**Example progression:**

Initial prompt:
```markdown
Implement authentication for the API.
```

Agent response:
```markdown
I need to clarify the authentication approach:
1. Token-based (JWT) or session-based?
2. User storage: Database or external service?
3. Endpoints that need protection vs. public?
```

Refined prompt:
```markdown
Implement JWT authentication for the API.

Context:
- User storage: PostgreSQL (User entity already exists)
- Protected: All /api/* endpoints except /api/public/*
- Token expiry: 24 hours

Follow pattern in existing AuthController if one exists,
otherwise use Spring Security defaults.
```

---

## Pattern: Strategic Pivot

**When to use:** When current approach is failing

**Recognition signals:**
- Multiple failed attempts (3+)
- Errors suggest fundamental issue
- Approach not matching project architecture
- Better alternative becomes apparent

**Action:**
```markdown
Stop current approach.

[Brief analysis of why current approach failed]

Alternative approach:
[New direction with rationale]

Do you agree this is better?
```

**Why this works:**
- Recognizes sunk cost fallacy
- Explicitly abandons failing path
- Proposes clear alternative
- Seeks alignment before proceeding

**Example:**
```markdown
Stop the api-generator approach.

Analysis:
- Tool has 19 open PRs, unstable
- Generated code doesn't match our patterns
- Heavy customization required

Alternative:
Use AI-native generation with explicit recipe.
Generate code directly following our patterns.

This gives us full control and matches our architecture.
Proceed with this approach?
```

---

## Pattern: Phased Execution with Verification

**When to use:** For multi-step generation or transformation

**Structure:**
```markdown
Follow phased approach:

Phase 1: [Step 1]
- [Specific actions]
- Verify: [Command to verify]
- Fix if needed, then proceed

Phase 2: [Step 2]
- [Specific actions]
- Verify: [Command to verify]
- Fix if needed, then proceed

[etc.]

Do NOT proceed to next phase if verification fails.
```

**Why this works:**
- Catches errors early (easier to fix)
- Prevents cascading failures
- Clear checkpoints
- Iterative refinement vs. big-bang

**Example:**
```markdown
Generate TMF API service in phases:

Phase 1: External models
- Generate from OpenAPI spec
- Compile: mvn clean compile -pl api-layer
- Fix compilation errors
- Proceed only when Phase 1 compiles

Phase 2: Internal entities
- Generate entity classes
- Compile: mvn clean compile -pl api-layer
- Fix compilation errors
- Proceed only when Phase 2 compiles

[etc.]
```

---

## Pattern: File Path Specificity

**When to use:** Always when files are involved

**Structure:**
```markdown
[Task statement]

Files:
- [Exact path 1]
- [Exact path 2]
- [Pattern for multiple: src/**/*Controller.java]
```

**Why this works:**
- No ambiguity about which files
- Agent doesn't waste time searching
- Faster execution
- Prevents wrong file selection

**Good:**
```markdown
Review src/main/java/com/dnext/api/product/ProductController.java
for error handling patterns.
```

**Bad:**
```markdown
Review the product controller for issues.
```

**When multiple files:**
```markdown
Review all controller files:
- src/main/java/com/dnext/api/product/ProductController.java
- src/main/java/com/dnext/api/order/OrderController.java
- src/main/java/com/dnext/api/customer/CustomerController.java

Check for consistent error handling across all three.
```

---

## Anti-Pattern: Vague Task Descriptions

**Problem:**
```markdown
Help with the API implementation.
```

**What's wrong:**
- Which API?
- What kind of help? (generate? review? debug?)
- Current state unclear

**Fix:**
```markdown
Generate ProductOffering REST API controller from openapi/product-offering.yaml.
Service layer already exists. Follow OrderController.java pattern.
```

---

## Anti-Pattern: Assuming Project Knowledge

**Problem:**
```markdown
Generate the mapper using our standard approach.
```

**What's wrong:**
- What is "our standard approach"?
- Agent doesn't have project context
- Leads to guessing and mistakes

**Fix:**
```markdown
Generate polymorphic mapper following pattern in:
src/main/java/com/dnext/mapper/OrderMapper.java

Handle @Characteristic polymorphism per CharacteristicMapper.java
```

---

## Anti-Pattern: Over-Specification

**Problem:**
```markdown
First create a class, then add fields one by one in this order: id, name, description.
Then create getters and setters. Then add @Entity annotation. Then add @Table.
Then add @Id to id field. Then add @Column to other fields...
```

**What's wrong:**
- Micro-managing prevents agent expertise
- Brittle (breaks if patterns change)
- Wastes tokens on obvious steps
- Reduces flexibility

**Fix:**
```markdown
Generate ProductOffering entity from OpenAPI spec.
Follow JPA patterns in OrderEntity.java.
Handle polymorphic @Characteristic field appropriately.
```

---

## Anti-Pattern: Missing Verification Steps

**Problem:**
```markdown
Generate the code.
```

**What's wrong:**
- No way to verify correctness
- Agent might stop before fully functional
- Quality unclear

**Fix:**
```markdown
Generate the code.

Verification:
- mvn clean compile (must pass)
- mvn test (must pass)
- No compiler warnings
- Follows checkstyle rules
```

---

## Quick Reference

**Essential prompt elements:**
- [ ] Clear, specific task statement
- [ ] File paths (where applicable)
- [ ] Context (project structure, tech stack, special cases)
- [ ] Success criteria (what "done" means)
- [ ] Scope boundaries (in/out of scope)
- [ ] Verification commands (how to check correctness)
- [ ] Methodology reference (existing patterns to follow)
- [ ] Failure recovery (for complex tasks)

**When in doubt:**
- Be specific, not vague
- Provide context, don't assume
- Define success criteria clearly
- Reference existing patterns
- Give verification steps
