# Prompt Examples: Good vs. Bad

Concrete examples of effective and ineffective agent prompts across different scenarios.

---

## Scenario 1: Code Review Request

### ❌ Bad: Vague and Minimal

```
Review the code.
```

**Problems:**
- Which code?
- What aspects to review?
- What standards to use?
- What to do with findings?

### ✅ Good: Specific and Complete

```
Review code quality for child story PRND-2847#3 implementation.

Context:
- Implementation: src/main/java/com/dnext/api/product/ProductController.java
- Story: .stories.md (search for PRND-2847#3)
- Scope: REST API controller for ProductOffering

Review criteria:
- Error handling patterns (try-catch placement, exception types)
- Input validation (null checks, @Valid usage, constraint validation)
- Test coverage (unit tests exist for all endpoints)
- Code organization (follows OrderController.java pattern)
- REST conventions (HTTP methods, status codes, response structure)

Deliverables:
- Create review items in .stories.md under PRND-2847#3
- Format: PRND-2847#3.R1, PRND-2847#3.R2, etc.
- Categorize by severity:
  - Critical: Blocks merge (security, data loss)
  - Major: Should fix (quality, maintainability)
  - Minor: Nice to have (style, optimization)
- Provide specific file:line references for each issue
```

**Why this works:**
- Exact file path (no searching needed)
- Clear review criteria (knows what to check)
- Specific deliverables (knows format and location)
- Severity guidelines (consistent categorization)

---

## Scenario 2: API Generation

### ❌ Bad: Missing Critical Context

```
Generate the ProductOffering API.
```

**Problems:**
- No OpenAPI spec location
- No package information
- No project structure details
- No special handling notes

### ✅ Good: Context-Rich

```
Generate complete TMF ProductOffering API service from OpenAPI specification.

Context:
- OpenAPI spec: openapi/product-offering.yaml
- Project structure: Maven multi-module (api-layer module)
- Base package: com.dnext.api.productoffering
- Database: PostgreSQL with Hibernate/JPA
- Common dependencies: dnext-common-core (BaseEntity, BaseMapper, BaseService)
- Polymorphic handling required: ProductOffering.characteristics field

Special notes:
- @Characteristic is polymorphic (StringCharacteristic, NumberCharacteristic, etc.)
- Use PolymorphicCharacteristicMapper pattern from existing code
- Follow naming: {Entity}Entity, {Entity}DTO, {Entity}Mapper, {Entity}Repository, etc.

Follow 8-phase iterative approach:
1. External models (DTOs from OpenAPI) → compile → fix
2. Internal entities (JPA entities) → compile → fix
3. Polymorphic mappers (handle @Characteristic) → compile → fix
4. Repository (Spring Data JPA) → compile → fix
5. Service layer (business logic) → compile → fix
6. Controller (REST endpoints) → compile → fix
7. Unit tests (MockMvc, Mockito) → verify
8. Full build and integration tests → verify

Compile after each phase: mvn clean compile -pl api-layer
Test after implementation: mvn test -pl api-layer

Implementation complete when:
- All phases compile cleanly
- All tests pass
- No compiler warnings
- Code follows existing API patterns (see OrderAPI for reference)
```

**Why this works:**
- Spec location explicit
- Project structure clear
- Special handling noted upfront
- Phased approach prevents cascading failures
- Clear verification steps
- Success criteria defined

---

## Scenario 3: Test Design

### ❌ Bad: Over-Specified

```
Create a test class called ProductControllerTest.
Put it in src/test/java/com/dnext/api/product/.
Add @WebMvcTest annotation.
Create a setup method with @BeforeEach.
Add MockMvc field with @Autowired.
Add mockProductService field with @MockBean.
Create testGetProduct method.
Inside it, use mockMvc.perform(get("/api/products/1")).
Add andExpect(status().isOk()).
Also add andExpect(jsonPath("$.id").value(1)).
And verify the service was called once...
```

**Problems:**
- Micro-managing implementation
- Prevents agent from using expertise
- Brittle (if patterns change, prompt fails)
- Wastes tokens on obvious details

### ✅ Good: Outcome-Focused

```
Design comprehensive failing tests for child story PRND-2847#3.

Context:
- Story: .stories.md (PRND-2847#3 - "Implement GET /products/{id}")
- Implementation will be in: ProductController.java
- Service layer exists: ProductService.java
- Test patterns: src/test/java/com/dnext/api/order/OrderControllerTest.java

Create tests covering:

Happy path:
- GET existing product returns 200 with product data
- Product data matches expected structure

Error cases:
- GET non-existent product returns 404
- GET with invalid ID format returns 400
- Service throws exception, returns 500

Edge cases:
- Product with null optional fields
- Product with maximum field lengths
- Product with special characters in name

Integration:
- Service called with correct parameters
- Mapping from entity to DTO correct
- Response headers correct (Content-Type, etc.)

Tests should:
- Follow AAA pattern (Arrange, Act, Assert)
- Use MockMvc for HTTP testing
- Mock ProductService with @MockBean
- Use AssertJ for assertions
- Fail initially (red phase - no implementation yet)

Reference spring-testing skill for MockMvc patterns.
```

**Why this works:**
- Focuses on what to test, not how
- Agent applies expertise for implementation details
- Leverages existing patterns (OrderControllerTest)
- References skill for common patterns
- Covers comprehensive scenarios

---

## Scenario 4: Implementation Task

### ❌ Bad: Assumed Knowledge

```
Implement the product search using our standard approach.
```

**Problems:**
- What is "our standard approach"?
- No specification reference
- No existing pattern reference
- Agent must guess

### ✅ Good: Explicit References

```
Implement product search functionality for PRND-2847#4.

Context:
- Specification: .stories.md (PRND-2847#4 - acceptance criteria)
- Endpoint: GET /api/products/search?name={name}&status={status}
- Existing pattern: OrderController.searchOrders() method
- Service layer: ProductService (already has search method stub)

Requirements:
- Support query parameters: name (optional), status (optional)
- If no params, return all products
- Name matching: case-insensitive contains
- Status matching: exact match
- Return 200 with list (empty list if no matches)
- Pagination not required (future story)

Follow patterns from:
- OrderController.searchOrders() for endpoint structure
- OrderService.searchOrders() for service layer logic
- Use Spring Data JPA query methods in repository

Tests:
- src/test/java/com/dnext/api/product/ProductControllerTest.java
- Add test methods for search scenarios
- Use existing test setup

Implementation complete when:
- Endpoint works as specified
- Tests pass
- Follows existing search pattern
- Code compiles with no warnings
```

**Why this works:**
- References specific existing patterns
- Clear specification location
- Explicit requirements
- Existing code to follow
- Tests specified
- Success criteria clear

---

## Scenario 5: Exploration Task

### ❌ Bad: No Guidance

```
Find error handling in the codebase.
```

**Problems:**
- Too broad (entire codebase?)
- What to do with findings?
- How deep to analyze?
- What format for results?

### ✅ Good: Scoped and Directive

```
Explore error handling patterns in API layer.

Scope:
- Directory: src/main/java/com/dnext/api/
- Focus: Exception handling in controllers and services
- Depth: Identify patterns, analyze 2-3 examples

Analysis approach:
1. Quick scan: Find try-catch blocks, exception classes
2. If consistent patterns found: Document pattern with 2 examples
3. If inconsistent: Report variations with recommendations
4. If no patterns: Report gap and suggest approach

Report findings:
- Pattern description (if exists)
- Examples with file:line references
- Consistency assessment
- Recommendations for standardization

Don't:
- Analyze every file exhaustively
- Include non-API code
- Suggest changes (just report patterns)

Output format:
Summary paragraph + bullet list of findings
```

**Why this works:**
- Clear scope boundaries
- Conditional depth (adapts to findings)
- Specific reporting format
- Efficient (pattern identification, not exhaustive review)
- Actionable output

---

## Scenario 6: Bug Fix

### ❌ Bad: No Context

```
Fix the null pointer exception.
```

**Problems:**
- Which NPE? (could be many)
- Where is it occurring?
- How to reproduce?
- What's expected behavior?

### ✅ Good: Complete Debugging Info

```
Fix NullPointerException in ProductController.getProduct().

Context:
- Error location: ProductController.java:47
- Stack trace shows: productMapper.toDTO(entity) throws NPE
- Occurs when: entity.characteristics is null
- Expected: Handle null characteristics gracefully

Root cause analysis:
- ProductEntity.characteristics can be null (optional field)
- ProductMapper.toDTO() doesn't check for null
- Mapper calls characteristics.stream() on null

Fix approach:
1. Add null check in ProductMapper.toDTO()
2. Return empty list for null characteristics
3. Add test case for product with null characteristics
4. Verify fix with: mvn test -Dtest=ProductControllerTest#testGetProductWithNullCharacteristics

Success criteria:
- Test passes (create if doesn't exist)
- No NPE on null characteristics
- Existing tests still pass
- Follows null handling pattern in OrderMapper
```

**Why this works:**
- Specific error location
- Root cause identified
- Fix approach outlined
- Test requirement included
- Verification step provided
- Pattern reference for consistency

---

## Scenario 7: Refactoring

### ❌ Bad: Vague Improvement

```
Improve the code quality.
```

**Problems:**
- What does "improve" mean?
- Which code?
- What aspects of quality?
- What changes are acceptable?

### ✅ Good: Specific Refactoring Goal

```
Refactor ProductService to reduce method complexity.

Context:
- File: src/main/java/com/dnext/service/ProductService.java
- Problem: createProduct() method is 150 lines (exceeds 50-line guideline)
- Complexity: Cyclomatic complexity 15 (should be < 10)

Refactoring goals:
- Extract validation logic to separate private methods
- Extract characteristic handling to CharacteristicProcessor helper
- Extract database operations to focused methods
- Reduce createProduct() to < 50 lines

Constraints:
- Maintain existing behavior (no functional changes)
- Keep all existing tests passing
- Add tests for new private methods if complex
- Follow Single Responsibility Principle

Verification:
- All tests pass: mvn test -Dtest=ProductServiceTest
- Complexity reduced (checkstyle should not warn)
- Code more readable (subjective but clear structure)

Reference: OrderService.createOrder() is good example of clean structure
```

**Why this works:**
- Specific file and method
- Clear problem stated (150 lines, complexity 15)
- Explicit goals (what to extract)
- Constraints prevent scope creep
- Verification steps
- Reference to good example

---

## Scenario 8: Planning

### ❌ Bad: Minimal Information

```
Plan how to implement the new feature.
```

**Problems:**
- Which feature?
- What requirements?
- What constraints?
- What's current state?

### ✅ Good: Complete Planning Context

```
Plan implementation for story PRND-2847: Product Search & Filtering.

Context:
- Story state: BACKLOG
- SRS: docs/epics/product-management/srs-extract.md (Section 3.2)
- Feature design: docs/epics/product-management/feature-design.md (Search section)
- Module design: docs/modules/api-layer/design.md

Requirements from SRS:
- Search by name (partial match, case-insensitive)
- Filter by status (exact match)
- Filter by category (exact match)
- Combine filters with AND logic
- Pagination support (page size 20)

Current state:
- ProductController exists
- ProductService exists
- ProductRepository exists
- Basic CRUD operations implemented

Generate implementation guide:
1. Analyze complexity:
   - Single child story if simple (add search endpoint)
   - Multiple children if complex (search + filtering + pagination)

2. Identify dependencies:
   - Need Spring Data JPA query methods
   - May need Specification for dynamic queries
   - Pagination requires Pageable support

3. Define technical approach:
   - Repository layer changes (query methods)
   - Service layer changes (search logic)
   - Controller changes (search endpoint)
   - DTO changes if needed

4. Create child stories if needed:
   - #1: Search by name
   - #2: Add status filtering
   - #3: Add category filtering
   - #4: Add pagination

Output: Implementation guide in story in .stories.md
```

**Why this works:**
- Complete context (SRS, design docs)
- Requirements extracted
- Current state clear
- Guidance for analysis
- Child story breakdown if complex
- Specific output location

---

## Quick Checklist for Effective Prompts

**Every agent prompt should have:**
- [ ] Clear, specific task statement
- [ ] Relevant file paths (if applicable)
- [ ] Context (project structure, tech stack, special cases)
- [ ] Success criteria (what "done" looks like)
- [ ] Scope boundaries (what's in/out of scope)
- [ ] References to existing patterns (for consistency)
- [ ] Verification steps (how to check correctness)

**When in doubt:**
- Be specific, not vague
- Provide context, don't assume knowledge
- Reference existing code for patterns
- Define success criteria explicitly
- Include verification commands
