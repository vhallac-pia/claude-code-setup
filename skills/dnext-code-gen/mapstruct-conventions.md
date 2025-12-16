# MapStruct Conventions for DNext APIs

## Mapper Interface Template

```java
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {
        DependentMapper1.class,
        DependentMapper2.class
})
public interface {Entity}Mapper extends BaseAppMapper<{Entity}, {Entity}FVO, {Entity}MVO, Internal{Entity}> {

    {Entity}Mapper INSTANCE = Mappers.getMapper({Entity}Mapper.class);
    String MAP_QUERY_NAME = "{package}.{Entity}Mapper::getMapperMethodName";
    String MANAGED_ENTITY_TYPE = "{Entity}";

    @MapField(dto="@type", entity="atType")
    default String getMapperMethodName() {
        return MAP_QUERY_NAME;
    }

    // Override methods...
}
```

## Required Ignored Fields

**Always ignore** when mapping FVO/MVO → Entity:

```java
@Mapping(target = "id", ignore = true)
@Mapping(target = "href", ignore = true)
@Mapping(target = "revision", ignore = true)
@Mapping(target = "createdDate", ignore = true)
@Mapping(target = "updatedDate", ignore = true)
@Mapping(target = "createdBy", ignore = true)
@Mapping(target = "updatedBy", ignore = true)
@Mapping(target = "accessPolicyConstraint", ignore = true)
```

**Reason:** Framework-managed fields, not user-provided.

## Standard Mapping Annotations

### href and atSchemaLocation (Entity → DTO)

```java
@Mapping(target = "href", expression = "java(com.pia.orbitant.common.core.component.HrefUtil.getHref({Entity}.class, entity.getId()))")
@Mapping(target = "atSchemaLocation", expression = "java(com.pia.orbitant.common.core.component.HrefUtil.getSchemaLocation({Entity}.class))")
```

**Always use** for Entity → DTO/MVO mappings.

### Enum Conversions

When entity stores enum as String but DTO uses enum type:

```java
// String → Enum
@Named("statusStringToEnum")
default TroubleTicketStatusType statusStringToEnum(String enumString) {
    if (enumString == null) {
        return null;
    }
    try {
        return TroubleTicketStatusType.fromValue(enumString);
    } catch (IllegalArgumentException e) {
        throw ExceptionFactory.BadRequest.throwInvalidEnumerationException(
            null, "status", enumString);
    }
}

// Enum → String
@Named("statusEnumToString")
default String statusEnumToString(TroubleTicketStatusType enumType) {
    return enumType != null ? enumType.getValue() : null;
}

// Usage
@Mapping(target = "status", source = "status", qualifiedByName = "statusStringToEnum")
```

### Nested Object Mapping

When nested objects need special handling:

```java
@Named("toAttachmentRefOrValue")
@Mapping(target = "...", source = "...")
AttachmentRefOrValue toAttachmentRefOrValue(InternalAttachmentRefOrValue internal);

// Usage
@Mapping(target = "attachment", source = "attachment", qualifiedByName = "toAttachmentRefOrValue")
```

## Dependent Mappers

Declare in `uses` clause:

```java
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {
        ContactMediumMapper.class,          // For contactMedium field
        AttachmentRefOrValueMapper.class,   // For attachment field
        RelatedPartyMapper.class            // For relatedParty field
})
```

MapStruct will use these mappers automatically for nested objects.

## Component Model

**Always use:** `componentModel = MappingConstants.ComponentModel.SPRING`

This registers mapper as Spring bean for dependency injection.

## Null Handling

**Default strategy:** Null values in source are set to null in target.

For MVO (merge patch), use:
```java
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
```

This ignores null values during patch operations.

## Unmapped Properties

Expected MapStruct warnings during compilation:

```
Warning: Unmapped target property: "someField"
```

**This is OK** when:
- Field is framework-managed (id, revision, etc.)
- Field is mapped via expression
- Field is intentionally excluded

**Use:**
```java
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
```

Or ignore specific field:
```java
@Mapping(target = "someField", ignore = true)
```

## Collection Mapping

MapStruct handles collections automatically:

```java
// Automatic
List<InternalAddress> addresses → List<Address> addresses
```

For custom collection mapping:
```java
@Mapping(target = "addresses", source = "addresses", qualifiedByName = "toAddressList")

@Named("toAddressList")
default List<Address> toAddressList(List<InternalAddress> internalList) {
    // Custom logic
}
```

## Method Naming Conventions

### Simple Mappers

- `toDto(InternalEntity entity)` - Entity → DTO
- `toMVO(InternalEntity entity)` - Entity → MVO
- `toEntity(EntityFVO fvo)` - FVO → Entity
- `mvoToEntity(EntityMVO mvo)` - MVO → Entity

### Polymorphic Mappers (Named Methods)

- `toXxxDto(InternalXxx entity)` - Specific type → DTO
- `toXxxMVO(InternalXxx entity)` - Specific type → MVO
- `toXxxEntity(XxxFVO fvo)` - FVO → Specific type
- `xxxMvoToEntity(XxxMVO mvo)` - MVO → Specific type (lowercase prefix!)

**Note:** MVO to Entity uses lowercase prefix: `customerMvoToEntity`, not `CustomerMvoToEntity`

## Generated Code Location

MapStruct generates implementation classes at compile time:

```
target/generated-sources/annotations/{package}/mapper/{Entity}MapperImpl.java
```

**Don't edit generated code** - it's regenerated on every build.

## Validation

After mapper generation:

- [ ] `@Mapper` annotation with `componentModel = SPRING`
- [ ] Extends `BaseAppMapper`
- [ ] Ignored fields present for FVO/MVO → Entity
- [ ] HrefUtil expressions for Entity → DTO
- [ ] Enum converters if needed
- [ ] Dependent mappers in `uses` clause
- [ ] Polymorphic dispatch if applicable (see polymorphic-mapper-pattern.md)
- [ ] Compiles without errors
- [ ] Only expected unmapped property warnings

## Common Issues

### Issue: Cannot find mapper

**Error:** `Could not find mapper bean for: com.example.CustomerMapper`

**Fix:** Add `componentModel = MappingConstants.ComponentModel.SPRING`

### Issue: Missing dependency injection

**Error:** `Required dependency SomeMapper is null`

**Fix:** Add to `uses` clause: `uses = { SomeMapper.class }`

### Issue: Unmapped target warnings

**Info:** Expected for framework-managed fields

**Fix:** Add `@Mapping(target = "field", ignore = true)` or set `unmappedTargetPolicy = IGNORE`

### Issue: Enum conversion fails

**Error:** `Cannot map String to EnumType`

**Fix:** Add named converter methods (statusStringToEnum, statusEnumToString)

## Compiler Plugin Configuration

**Required in pom.xml:**

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <source>17</source>
                <target>17</target>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.mapstruct</groupId>
                        <artifactId>mapstruct-processor</artifactId>
                        <version>1.5.5.Final</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

**Version:** MapStruct 1.5.5.Final (current)

## Reference Examples

See: `~/.claude/references/code-generation/examples/`
- `trouble-ticket/TroubleTicketMapper.java` - Polymorphic mapper
- `customer/CustomerMapper.java` - Simple mapper
