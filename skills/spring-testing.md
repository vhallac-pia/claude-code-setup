---
name: spring-testing
description: Spring Boot and Java testing patterns for DNext modules. Auto-activates when writing JUnit tests, Spring integration tests, or discussing test strategy. Use when you see @Test, @SpringBootTest, MockMvc, or test file patterns like *Test.java, *SpringTest.java.
---

# Spring Testing Skill

This skill provides testing patterns for DNext Java/Spring modules.

## Core Reference Documents

- @temp/dnext-dev-support/references/testing.md - Unit, integration, contract testing patterns
- @temp/dnext-dev-support/references/business-validation.md - BVR testing patterns

## Test Types

### Unit Tests (*Test.java)

- Pure Java, no Spring context
- Mock dependencies with Mockito
- Fast execution
- Test single class in isolation

### Integration Tests (*SpringTest.java)

- Full Spring context with @SpringBootTest
- Real MongoDB (or embedded)
- Test component interactions
- Slower but more realistic

### API Tests (*ApiTest.java)

- MockMvc for endpoint testing
- Test request/response contracts
- Validate TMF compliance

## AAA Pattern

```java
@Test
void should_returnProduct_when_validIdProvided() {
    // Arrange - setup test data
    var product = TestEntityFactory.createProduct();
    when(repository.findById(any())).thenReturn(Optional.of(product));

    // Act - execute the method under test
    var result = service.getProduct("123");

    // Assert - verify results
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo(product.getName());
}
```

## Common Patterns

- Use `@MockBean` for service mocks in Spring tests
- Use `TestEntityFactory` for consistent test data
- Follow naming: `should_expectedBehavior_when_condition`
- Group tests by scenario with nested classes
- Use `@DisplayName` for readable test names

## BVR Testing

```java
@Test
void should_throwValidationException_when_nameIsBlank() {
    var entity = TestEntityFactory.createProduct();
    entity.setName("");

    assertThatThrownBy(() -> validator.validate(entity))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("name");
}
```
