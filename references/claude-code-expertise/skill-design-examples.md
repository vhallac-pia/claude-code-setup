# Skill Design Examples

Practical examples of well-designed skills following best practices.

---

## Example 1: Comprehensive Embedded Skill

### spring-testing.md (~2-3KB)

**Auto-activates when:**
- Writing JUnit 5 tests
- Using Spring Test framework
- Working with MockMvc, TestRestTemplate
- Designing test architecture

#### Quick Reference (embedded)

**Common Annotations:**

```markdown
@SpringBootTest
- Full application context
- Use for integration tests
- Slower startup, complete functionality

@WebMvcTest
- Controller layer only
- Fast, focused on web layer
- Auto-configures MockMvc

@DataJpaTest
- JPA/database layer only
- In-memory database
- Transaction rollback after each test

@MockBean
- Replaces bean in Spring context
- Use in slice tests (@WebMvcTest, @DataJpaTest)

@Autowired
- Inject test dependencies
- Works with TestRestTemplate, MockMvc
```

**AAA Structure Template:**

```java
@Test
void shouldDescribeExpectedBehavior() {
    // Arrange: Setup test data and mocks
    var entity = new TestEntity("test-data");
    when(mockRepository.findById(1L))
        .thenReturn(Optional.of(entity));

    // Act: Execute the method under test
    var result = service.getById(1L);

    // Assert: Verify outcomes
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("test-data");
    verify(mockRepository).findById(1L);
}
```

**MockMvc Patterns:**

```java
// GET request with path variable
mockMvc.perform(get("/api/products/{id}", 123))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.id").value(123))
    .andExpect(jsonPath("$.name").exists());

// POST request with JSON body
mockMvc.perform(post("/api/products")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(productDTO)))
    .andExpect(status().isCreated())
    .andExpect(header().exists("Location"));

// Error case
mockMvc.perform(get("/api/products/{id}", 999))
    .andExpect(status().isNotFound());
```

**Test Isolation Strategies:**

```markdown
1. Use @DirtiesContext sparingly
   - Expensive (restarts context)
   - Only when truly needed

2. Prefer transactional tests
   - @Transactional on test class
   - Auto-rollback after each test

3. Reset mocks between tests
   - @BeforeEach reset() or clearInvocations()
   - Prevents test interdependence

4. Avoid static state
   - Use instance fields
   - Initialize in @BeforeEach
```

**Common Assertion Patterns:**

```java
// AssertJ (preferred)
assertThat(result).isNotNull();
assertThat(result.getItems()).hasSize(3);
assertThat(result.getName()).startsWith("Product");

// Collection assertions
assertThat(products)
    .hasSize(2)
    .extracting(Product::getName)
    .containsExactly("A", "B");

// Exception assertions
assertThatThrownBy(() -> service.getById(999))
    .isInstanceOf(NotFoundException.class)
    .hasMessageContaining("Product not found");
```

#### Extended Reference (on-demand)

For comprehensive Spring Test annotations:
- Read ~/.claude/references/spring-testing/annotations-guide.md

For integration test patterns:
- Read ~/.claude/references/spring-testing/integration-patterns.md

For test data builders:
- Read ~/.claude/references/spring-testing/test-builders.md

**Size:** ~2-3KB embedded
**Token cost:** ~800-1,200 tokens
**Value:** Auto-activates for all test writing, provides immediate guidance

---

## Example 2: Lightweight Skill with References

### api-documentation.md (~1KB)

**Auto-activates when:**
- Writing OpenAPI specifications
- Documenting REST APIs
- Creating API examples

#### Core Patterns (embedded)

**OpenAPI Structure:**
```yaml
openapi: 3.0.0
info:
  title: API Name
  version: 1.0.0
paths:
  /resource:
    get:
      summary: Brief description
      responses:
        200:
          description: Success response
```

**Common Response Codes:**
- 200: Success (GET, PUT)
- 201: Created (POST)
- 204: No Content (DELETE)
- 400: Bad Request (validation)
- 404: Not Found
- 500: Server Error

#### Detailed References (on-demand)

For complete OpenAPI examples:
- Read ~/.claude/references/api-documentation/openapi-examples.md

For TMF API specific patterns:
- Read ~/.claude/references/api-documentation/tmf-patterns.md

For schema definitions:
- Read ~/.claude/references/api-documentation/schema-patterns.md

**Size:** ~1KB core + references
**Token cost:** ~300-400 tokens initial
**Value:** Low initial cost, loads details only when needed

---

## Example 3: Domain-Specific Knowledge Skill

### dnext-code-gen.md (~3-5KB target)

**Auto-activates when:**
- Working with TMF APIs
- DNext code generation
- Polymorphic mapper implementation

#### Quick Reference (embedded)

**Entity Structure Template:**

```java
@Entity
@Table(name = "product_offering")
public class ProductOfferingEntity extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "product_offering_id")
    private List<CharacteristicEntity> characteristics;

    // Getters/setters
}
```

**Mapper Decision Tree:**

```markdown
Is field a TMF @Characteristic?
├─ YES → Use PolymorphicCharacteristicMapper
│         - Handles multiple value types
│         - Uses CharacteristicValueType enum
│         - Preserves type information
│
└─ NO → Is it a complex nested object?
    ├─ YES → Create dedicated mapper
    │         - Follow standard mapper pattern
    │         - Handle bidirectional relationships
    │
    └─ NO → Use direct mapping
              - Simple field copy
              - Type conversion if needed
```

**Common Validation Patterns:**

```java
// Required field validation
@NotNull(message = "Name is required")
@Size(min = 1, max = 255, message = "Name must be 1-255 characters")
private String name;

// Enum validation
@Pattern(regexp = "Active|Inactive|Pending")
private String status;

// Custom validation for polymorphic
@Valid
private List<@Valid CharacteristicDTO> characteristics;
```

**Repository Pattern:**

```java
public interface ProductOfferingRepository
        extends JpaRepository<ProductOfferingEntity, Long> {

    Optional<ProductOfferingEntity> findByName(String name);

    List<ProductOfferingEntity> findByStatus(String status);

    @Query("SELECT p FROM ProductOfferingEntity p " +
           "LEFT JOIN FETCH p.characteristics " +
           "WHERE p.id = :id")
    Optional<ProductOfferingEntity> findByIdWithCharacteristics(
        @Param("id") Long id);
}
```

#### Detailed Reference (on-demand)

For polymorphic mapper implementation:
- Read ~/.claude/references/dnext-code-gen/polymorphic-mappers.md

For validation rules and examples:
- Read ~/.claude/references/dnext-code-gen/validation-guide.md

For known issues and solutions:
- Read ~/.claude/references/dnext-code-gen/known-issues.md

**Size:** 3-5KB embedded + references
**Token cost:** ~1,000-1,500 tokens initial, +500-2,000 when references loaded
**Access pattern:** Used frequently for TMF API work

---

## Skill Design Patterns

### Pattern 1: Comprehensive Embedded (< 5KB)

**Use when:**
- Knowledge < 5KB total
- Accessed every activation
- Specific to single domain
- Fast lookup needed

**Structure:**
```markdown
# Skill: domain-patterns

**Auto-activates when:** [specific contexts]

## Core Patterns (all embedded)
[Quick reference - ~1KB]
[Common templates - ~1-2KB]
[Decision trees - ~500B]
[Examples - ~1KB]

Total: ~3-4KB
```

**Example skills:**
- spring-testing (~2-3KB)
- dnext-code-gen (~3-5KB)
- story-conventions (~2KB)

### Pattern 2: Lightweight + References (> 5KB total)

**Use when:**
- Total knowledge > 5KB
- Some parts accessed conditionally
- Shared across multiple contexts
- Can defer loading details

**Structure:**
```markdown
# Skill: domain-patterns

**Auto-activates when:** [specific contexts]

## Quick Reference (embedded ~1KB)
[Most common patterns]
[Essential decision points]

## Detailed References (on-demand)
For X: Read ~/.claude/references/skill-name/x-patterns.md
For Y: Read ~/.claude/references/skill-name/y-guide.md
```

**Example use cases:**
- API documentation (core ~1KB, examples in references)
- Architecture patterns (principles embedded, examples referenced)
- Language features (syntax embedded, advanced features referenced)

### Pattern 3: Multi-Context Skill

**Use when:**
- Knowledge applies to multiple scenarios
- Different activation contexts
- Shared patterns across domains

**Structure:**
```markdown
# Skill: cross-cutting-patterns

**Auto-activates when:**
- Context A (e.g., writing tests)
- Context B (e.g., reviewing code)
- Context C (e.g., documenting APIs)

## Shared Patterns
[Patterns applicable to all contexts]

## Context-Specific Guidance
### When Testing
[Test-specific patterns]

### When Reviewing
[Review-specific patterns]

### When Documenting
[Documentation-specific patterns]
```

**Example:** Error handling skill activates for implementation, review, and testing

---

## Common Mistakes in Skill Design

### ❌ Mistake 1: Vague Auto-Activation

**Problem:**
```markdown
**Auto-activates when:** Coding
```

**Why it's bad:**
- Too broad - activates always
- Wastes tokens unnecessarily
- Unclear when actually needed

**Fix:**
```markdown
**Auto-activates when:**
- Writing JUnit 5 tests
- Using Spring Test framework
- Working with MockMvc/TestRestTemplate
```

### ❌ Mistake 2: Massive Embedded Content

**Problem:**
```markdown
# Skill: java-patterns

[10,000 lines of every Java pattern ever]
```

**Why it's bad:**
- Violates 3-5KB guideline
- Should use references for large content
- High token cost every activation

**Fix:**
- Embed core patterns (~3KB)
- Reference detailed examples
- Load advanced patterns on-demand

### ❌ Mistake 3: No Structure

**Problem:**
```markdown
# Skill: random-tips

Pattern 1: something
Oh also this thing
Pattern 2: another thing
By the way: random fact
```

**Why it's bad:**
- Hard to scan
- No clear organization
- Difficult to find relevant info

**Fix:**
- Clear sections with headers
- Logical grouping
- Quick reference first, details later

### ❌ Mistake 4: Duplicate Knowledge

**Problem:**
```markdown
# Skill: spring-web
[HTTP status codes list]

# Skill: api-documentation
[Same HTTP status codes list]

# Skill: rest-patterns
[Same HTTP status codes list again]
```

**Why it's bad:**
- Wastes tokens (loaded 3x)
- Maintenance nightmare
- Inconsistent if one updates

**Fix:**
- Create shared reference file
- All skills read same reference
- Single source of truth

---

## Skill Design Checklist

When designing a skill, verify:

- [ ] **Clear auto-activation triggers** - Specific contexts, not vague
- [ ] **Size appropriate** - 1-5KB for comprehensive
- [ ] **High-frequency embedded** - Common patterns in skill
- [ ] **Large content referenced** - Details in external files
- [ ] **Good structure** - Organized, scannable sections
- [ ] **No duplication** - Shared knowledge in references
- [ ] **Tested activation** - Verify it activates when expected
- [ ] **Value density** - Every byte justifies token cost
- [ ] **Examples included** - Show, don't just tell
- [ ] **References documented** - Clear pointers to detailed docs

If any item fails, refine the design.

---

## Size Estimation Guide

**How to estimate skill size:**

```
1 line of markdown ≈ 40-50 characters average
1 KB ≈ 1,024 characters ≈ 20-25 lines
3 KB ≈ 60-75 lines
5 KB ≈ 100-125 lines

Target for comprehensive skill:
- 75-125 lines of well-structured content
- Including headers, examples, formatting
- NOT including blank lines
```

**Token estimation:**

```
~3 characters per token (rough average)
1 KB ≈ 300-400 tokens
3 KB ≈ 900-1,200 tokens
5 KB ≈ 1,500-2,000 tokens
```

**Budget implications:**

```
Total budget: 200,000 tokens
Comfortable skill load: 10-20 skills × 1,500 tokens = 15,000-30,000 tokens
Leaves: 170,000-185,000 tokens for work
```

This confirms 3-5KB per skill is reasonable.
