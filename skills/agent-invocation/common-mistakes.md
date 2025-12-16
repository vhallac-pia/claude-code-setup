# Common Mistakes in Agent Invocation

Detailed anti-patterns with explanations and corrections.

---

## Mistake 1: Not Invoking Agent When Beneficial

### Symptom
Manually performing complex tasks that agents could handle better.

### Examples

**❌ Doing it wrong:**
- Reading 50 files manually to find error patterns
- Reviewing every controller file individually
- Searching entire codebase with multiple Grep calls

**✅ Doing it right:**
```markdown
Use Explore agent to analyze error handling across API layer.

Scope: src/main/java/com/dnext/api/
Task: Find all error handling patterns, analyze for consistency
Output: Pattern summary with recommendations
```

### Why this matters
- Agents can synthesize information across many files
- Agents identify patterns humans might miss
- Agents save time on systematic tasks
- Context isolation helps with large-scale analysis

---

## Mistake 2: Invoking Agent for Trivial Tasks

### Symptom
Using agents for simple operations that direct tools handle better.

### Examples

**❌ Doing it wrong:**
```markdown
Invoke Explore agent to find README.md
```

**✅ Doing it right:**
```markdown
Just use: Read ./README.md
Or: Glob README.md
```

**❌ Another example:**
```markdown
Invoke agent to add a TODO comment
```

**✅ Doing it right:**
```markdown
Just use: Edit to add the comment directly
```

### Decision framework

**Use agent when:**
- Task requires multiple steps
- Need analysis or synthesis
- Complex decision-making involved
- Multiple files/locations

**Use direct tools when:**
- Single simple operation
- Known exact location
- No analysis needed
- Trivial change

---

## Mistake 3: Vague Task Descriptions

### Symptom
Agent doesn't know what to do or guesses incorrectly.

### Examples

**❌ Vague:**
```markdown
Help with the API.
Review the code.
Fix the errors.
Implement the feature.
```

**✅ Specific:**
```markdown
Generate ProductOffering API controller from openapi/product-offering.yaml.
Review ProductController.java for error handling patterns.
Fix NullPointerException in ProductMapper.toDTO() at line 47.
Implement search endpoint as specified in PRND-2847#3.
```

### Vagueness checklist

**If you can't answer these, it's too vague:**
- [ ] What exactly needs to be done?
- [ ] Which files are involved?
- [ ] What's the current state?
- [ ] What's the desired outcome?
- [ ] How to verify success?

---

## Mistake 4: Missing File Paths

### Symptom
Agent wastes time searching, might find wrong files.

### Examples

**❌ No paths:**
```markdown
Review the controller.
Update the mapper.
Fix the service.
```

**✅ With paths:**
```markdown
Review src/main/java/com/dnext/api/product/ProductController.java
Update src/main/java/com/dnext/mapper/ProductMapper.java
Fix src/main/java/com/dnext/service/ProductService.java
```

### When paths matter most
- Code review (which code?)
- Bug fixes (where's the bug?)
- Refactoring (what to refactor?)
- Implementation (where to add code?)

### Path exceptions

**Don't need paths when:**
- Creating brand new files (path is part of specification)
- Searching for something (finding the path is the task)
- Working on current conversation's visible context

---

## Mistake 5: Assuming Agent Knows Project Specifics

### Symptom
Agent guesses wrong patterns, uses generic approaches instead of project conventions.

### Examples

**❌ Assumed knowledge:**
```markdown
Generate the mapper using our standard approach.
Follow the usual pattern for controllers.
Use the normal error handling.
```

**✅ Explicit references:**
```markdown
Generate mapper following ProductMapper.java pattern:
- Polymorphic handling for @Characteristic
- Use MapStruct annotations
- Handle null fields gracefully

Follow controller pattern from OrderController.java:
- @RestController with @RequestMapping
- Service injected via constructor
- ResponseEntity<DTO> return types
- @Valid for request body validation

Error handling per ErrorHandler.java:
- @ControllerAdvice for global handling
- Return ProblemDetail for errors
- Log with MDC context
```

### What agents don't know automatically
- Your project's naming conventions
- Your architectural patterns
- Your code organization
- Your error handling approach
- Your testing practices
- Your dependencies and versions

### What to provide
- Reference to existing similar code
- Specific pattern file to follow
- Architectural documentation location
- Existing examples inline

---

## Mistake 6: Over-Specifying Implementation

### Symptom
Micro-managing agent, preventing it from using expertise.

### Examples

**❌ Over-specified:**
```markdown
First create a class called ProductController.
Add @RestController annotation at the top.
Then add @RequestMapping("/api/products") below that.
Create a private final ProductService field.
Name it productService.
Add @Autowired on a constructor.
The constructor should take ProductService parameter.
Assign it to the field.
Now create a method called getProduct.
It should take @PathVariable Long id.
Return type is ResponseEntity<ProductDTO>.
Inside the method, first call productService.getById(id).
Store result in a variable called product.
Then return ResponseEntity.ok(product).
...
```

**✅ Outcome-focused:**
```markdown
Generate ProductController following OrderController.java pattern.

Requirements:
- REST endpoint GET /api/products/{id}
- Returns ProductDTO or 404 if not found
- Uses ProductService.getById()
- Includes error handling
- Has unit tests

Let agent determine implementation details based on existing patterns.
```

### Why over-specification fails
- Brittle (breaks if patterns change)
- Wastes tokens on obvious details
- Prevents agent from applying best practices
- Reduces flexibility for optimization
- Takes longer to write prompt

### Right level of specification

**Specify:**
- What (outcome, requirements)
- Which patterns to follow (references)
- Constraints (what must be preserved)
- Success criteria (how to verify)

**Don't specify:**
- How (line-by-line implementation)
- Obvious details (agent knows these)
- Standard patterns (reference existing instead)
- Minutiae (field names, method order, etc.)

---

## Mistake 7: No Success Criteria

### Symptom
Agent doesn't know when to stop, or stops too early/late.

### Examples

**❌ No criteria:**
```markdown
Implement the search feature.
```

**✅ With criteria:**
```markdown
Implement product search feature.

Complete when:
- Endpoint GET /api/products/search works
- Query parameters: name, status, category
- Returns filtered results
- Tests pass: mvn test -Dtest=ProductControllerTest
- Integration test covers search scenarios
- No compiler warnings
- Follows OrderController.search() pattern
```

### Criteria categories

**Functional:**
- All specified features work
- Edge cases handled
- Error cases handled

**Quality:**
- Tests exist and pass
- Code follows patterns
- No warnings/errors

**Verification:**
- Specific commands to run
- Expected outputs
- Performance benchmarks (if applicable)

---

## Mistake 8: Missing Context

### Symptom
Agent makes wrong assumptions, uses generic approaches, misses requirements.

### Examples

**❌ No context:**
```markdown
Generate the Product API.
```

**✅ With context:**
```markdown
Generate Product API service.

Context:
- OpenAPI spec: openapi/product.yaml
- Project: Maven multi-module, api-layer module
- Base package: com.dnext.api.product
- Database: PostgreSQL with JPA
- Dependencies: dnext-common-core 3.38.1-ca
- Polymorphic fields: ProductOffering.characteristics

Special handling:
- @Characteristic is polymorphic (String/Number/Object types)
- Use CharacteristicMapper pattern
- Follow TMF API guidelines

Existing patterns:
- OrderAPI for controller structure
- OrderService for service layer
- OrderMapper for DTO mapping
```

### Essential context elements

**Technical:**
- Project structure
- Frameworks/libraries
- Database type
- Dependencies

**Domain:**
- Business rules
- Data model specifics
- Integration points
- Special cases

**Patterns:**
- Existing code to follow
- Architectural standards
- Naming conventions
- Code organization

---

## Mistake 9: Not Asking for Clarification

### Symptom
Agent guesses when uncertain, implements wrong approach.

### Examples

**❌ Guessing:**
```
User: "Optimize the service"
Agent: [Assumes performance optimization, implements caching]
User: "I meant code quality, not performance!"
```

**✅ Clarifying:**
```
User: "Optimize the service"
Agent: "Before proceeding, I need to clarify what type of optimization:

1. Performance optimization:
   - Add caching
   - Optimize database queries
   - Reduce N+1 problems

2. Code quality optimization:
   - Refactor complex methods
   - Improve naming
   - Add documentation

3. Resource optimization:
   - Reduce memory usage
   - Minimize database connections
   - Optimize thread usage

Which type of optimization are you looking for?"
```

### When to ask for clarification

**Ask when:**
- Multiple valid interpretations exist
- User preferences matter (coding style, architecture)
- Scope is ambiguous
- Requirements conflict
- Success criteria unclear
- Approach has significant trade-offs

**Don't ask when:**
- Information is in files you can read
- Standard patterns apply (follow conventions)
- Requirements are explicit
- Methodology is documented
- Only one reasonable approach

---

## Mistake 10: Wrong Agent Selection

### Symptom
Using wrong agent type or model tier for the task.

### Examples

**❌ Wrong agent:**
```markdown
Use Explore agent to review code quality
```
**✅ Right agent:**
```markdown
Use code-reviewer agent (specialized for quality review)
```

**❌ Wrong model tier:**
```markdown
Use implementer with haiku model (complex coding)
```
**✅ Right model tier:**
```markdown
Use implementer with opus model (requires creative problem-solving)
```

**❌ Wrong agent:**
```markdown
Use general-purpose agent to update story state
```
**✅ Right agent:**
```markdown
Use story-tracker agent (specialized for state management)
```

### Agent selection framework

**Use specialized agent when exists:**
- story-implementation-planner for story planning
- code-reviewer for code review
- test-first-designer for test design
- implementer for TDD implementation
- merge-readiness-checker for PR verification
- story-tracker for state updates
- api-generator for TMF API generation

**Use generic agent otherwise:**
- Explore for codebase exploration
- Plan for non-story planning
- general-purpose for multi-step tasks

**Model tier:**
- opus: Creative, synthesis, complex
- sonnet: Standard, structured, moderate
- haiku: Simple, mechanical, fast

---

## Mistake 11: No Scope Boundaries

### Symptom
Agent provides unwanted suggestions, goes beyond requested work.

### Examples

**❌ No boundaries:**
```markdown
Review the controller.
```

Result: Agent reviews controller, suggests refactoring service, recommends database changes, proposes new features...

**✅ With boundaries:**
```markdown
Review ProductController.java.

In scope:
- Error handling in this controller only
- Input validation in this controller only
- Test coverage for this controller only

Out of scope:
- Service layer changes
- Database schema
- Other controllers
- Performance optimization
- New features

Focus only on in-scope items.
```

### Why boundaries matter
- Prevents scope creep
- Saves time and tokens
- Keeps work focused
- Matches actual requirements
- Reduces rework

---

## Mistake 12: Missing Verification Steps

### Symptom
No way to verify correctness, quality unclear.

### Examples

**❌ No verification:**
```markdown
Generate the controller.
```

**✅ With verification:**
```markdown
Generate ProductController.

Verification steps:
1. Compile: mvn clean compile -pl api-layer
2. Tests: mvn test -Dtest=ProductControllerTest
3. Integration: mvn verify -Dtest=ProductIntegrationTest
4. Checkstyle: mvn checkstyle:check
5. Manual: Test via curl or Postman

Success criteria:
- All builds pass
- All tests green
- No checkstyle violations
- Endpoints work as expected
```

### Verification types

**Build:**
- Compilation succeeds
- No warnings
- Dependencies resolve

**Tests:**
- Unit tests pass
- Integration tests pass
- Coverage acceptable

**Quality:**
- Code style checks
- Complexity checks
- Static analysis

**Functional:**
- Manual testing
- API testing
- Smoke testing

---

## Mistake 13: Not Referencing Existing Patterns

### Symptom
Inconsistent code, doesn't match project style.

### Examples

**❌ No pattern reference:**
```markdown
Generate a controller for Product.
```

Result: Agent uses generic Spring patterns, doesn't match your project's conventions.

**✅ With pattern reference:**
```markdown
Generate ProductController following OrderController.java pattern.

Specifically:
- Constructor injection (not field injection)
- ResponseEntity<DTO> return types
- @Valid on request bodies
- ProblemDetail for errors (via ErrorHandler)
- Javadoc on public methods
- Package: com.dnext.api.{resource}
```

### Where to find patterns
- Existing similar code (best)
- Project documentation
- Architecture decision records
- Team conventions document
- Code review feedback

---

## Mistake 14: Ignoring Failure Recovery

### Symptom
Agent gets stuck in loops, repeats failing approach.

### Examples

**❌ No recovery strategy:**
```markdown
Generate the polymorphic mapper.
```

Result: Compilation fails, agent regenerates same code, fails again, repeats...

**✅ With recovery:**
```markdown
Generate polymorphic mapper for ProductOffering.

If compilation fails:
1. Read error messages carefully
2. Check existing CharacteristicMapper.java for working pattern
3. Fix incrementally (one error at a time)
4. Recompile after each fix
5. If stuck after 3 attempts, stop and report:
   - What was tried
   - Error messages
   - What seems to be the issue

Known issues:
- Polymorphic @Characteristic needs special ValueType enum
- Mapper must handle all characteristic types
- Null characteristics should map to empty list
```

### Recovery elements

**Detection:**
- What indicates failure
- Error patterns to watch

**Strategy:**
- Steps to attempt
- Resources to check
- How many retries

**Escalation:**
- When to stop retrying
- What to report
- How to ask for help

---

## Quick Diagnostic

**If agent produces wrong results, check:**
- [ ] Was task description specific enough?
- [ ] Were file paths provided?
- [ ] Was necessary context included?
- [ ] Were existing patterns referenced?
- [ ] Were success criteria defined?
- [ ] Were scope boundaries set?
- [ ] Was verification specified?
- [ ] Was right agent type selected?
- [ ] Was right model tier used?
- [ ] Should I have asked for clarification first?

**Most issues trace to one of these mistakes.**
