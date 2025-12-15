# Nested Polymorphic Field Mapper Pattern

**Critical Pattern for TMF Polymorphic Types Inside Wrapper Classes**

## Problem Statement

When a polymorphic type (e.g., `PartyOrPartyRole` with 10 subtypes) is nested inside a wrapper class (e.g., `RelatedPartyOrPartyRole`), standard MapStruct mapping fails due to:

1. **Method Overloading Limitation**: Cannot have two `map()` methods with same parameters but different return types (DTO vs MVO)
2. **Recursive Field Mapping**: MapStruct cannot automatically resolve which polymorphic mapping method to use for nested fields
3. **Type Ambiguity**: Multiple @Named methods for same entity type, MapStruct doesn't know which to invoke

## The Solution: Wrapper Mapper with qualifiedByName

Create a **separate wrapper mapper** that explicitly delegates to the polymorphic mapper using `qualifiedByName` references.

## When to Use This Pattern

Use this pattern when **ALL** these conditions are true:

1. **Polymorphic Type Exists**: Base type has 2+ concrete subtypes (e.g., PartyOrPartyRole → BusinessPartner, Consumer, Customer, Individual, Organization, PartyRef, PartyRole, PartyRoleRef, Producer, Supplier)
2. **Wrapper Class Exists**: A "Related*" or container class wraps the polymorphic type (e.g., RelatedPartyOrPartyRole contains PartyOrPartyRole field)
3. **Union Entity Pattern**: Python generator creates flat `InternalPartyOrPartyRole` that contains ALL fields from ALL subtypes
4. **Nested Mapping Required**: The wrapper class is used as a field type in managed entities (e.g., Customer has `List<RelatedPartyOrPartyRole> relatedParty`)

**Examples in TMF:**
- `RelatedPartyOrPartyRole` wrapping `PartyOrPartyRole` (TMF629 Customer Management)
- `RelatedPartyRefOrPartyRoleRef` wrapping `PartyRefOrPartyRoleRef` (TMF621 Trouble Ticket)
- `AttachmentRefOrValue` wrapping polymorphic attachment types

## Pattern Structure

### Layer 1: Polymorphic Type Mapper

The polymorphic type mapper handles the 2-10 concrete subtypes using switch-based dispatch.

**File**: `PartyOrPartyRoleMapper.java`

```java
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = PartyOrPartyRoleMapper.class)
public interface PartyOrPartyRoleMapper {

    PartyOrPartyRoleMapper INSTANCE = Mappers.getMapper(PartyOrPartyRoleMapper.class);
    String MAP_QUERY_NAME = "com.pia.orbitant.dcmms.mapper.PartyOrPartyRoleMapper::getMapperMethodName";

    default String getMapperMethodName() {
        return MAP_QUERY_NAME;
    }

    // ========== FVO to Entity (using instanceof pattern for Java 17) ==========

    @Named("toEntityPartyOrPartyRoleFVO")
    default InternalPartyOrPartyRole toEntityFromFVO(PartyOrPartyRoleFVO fvo) {
        if (fvo == null) {
            return null;
        }
        if (fvo instanceof BusinessPartnerFVO) {
            return toEntityFromBusinessPartnerFVO((BusinessPartnerFVO) fvo);
        } else if (fvo instanceof ConsumerFVO) {
            return toEntityFromConsumerFVO((ConsumerFVO) fvo);
        } else if (fvo instanceof CustomerFVO) {
            return toEntityFromCustomerFVO((CustomerFVO) fvo);
        }
        // ... more instanceof checks for all 10 subtypes
        throw new IllegalArgumentException("Unknown type: " + fvo.getClass().getName());
    }

    InternalPartyOrPartyRole toEntityFromBusinessPartnerFVO(BusinessPartnerFVO fvo);
    InternalPartyOrPartyRole toEntityFromConsumerFVO(ConsumerFVO fvo);
    // ... method declarations for each subtype

    // ========== Entity to DTO (using switch on discriminator) ==========

    @Named("toPartyOrPartyRoleDto")
    default PartyOrPartyRole toDto(InternalPartyOrPartyRole entity) {
        if (entity == null) {
            return null;
        }
        return switch (entity.getAtType()) {
            case "BusinessPartner" -> toBusinessPartner(entity);
            case "Consumer" -> toConsumer(entity);
            case "Customer" -> toCustomer(entity);
            case "Individual" -> toIndividual(entity);
            case "Organization" -> toOrganization(entity);
            case "PartyRef" -> toPartyRef(entity);
            case "PartyRole" -> toPartyRole(entity);
            case "PartyRoleRef" -> toPartyRoleRef(entity);
            case "Producer" -> toProducer(entity);
            case "Supplier" -> toSupplier(entity);
            default -> throw new IllegalArgumentException("Unknown @type: " + entity.getAtType());
        };
    }

    @Named("toBusinessPartner")
    BusinessPartner toBusinessPartner(InternalPartyOrPartyRole entity);

    @Named("toConsumer")
    Consumer toConsumer(InternalPartyOrPartyRole entity);

    // ... @Named methods for each of 10 subtypes

    // ========== Entity to MVO ==========

    @Named("toPartyOrPartyRoleMVO")
    default PartyOrPartyRoleMVO toMVO(InternalPartyOrPartyRole entity) {
        if (entity == null) {
            return null;
        }
        return switch (entity.getAtType()) {
            case "BusinessPartner" -> toBusinessPartnerMVO(entity);
            case "Consumer" -> toConsumerMVO(entity);
            // ... all 10 cases
            default -> throw new IllegalArgumentException("Unknown @type: " + entity.getAtType());
        };
    }

    @Named("toBusinessPartnerMVO")
    @Mapping(target = "relatedParty", ignore = true)  // Avoid circular dependency
    BusinessPartnerMVO toBusinessPartnerMVO(InternalPartyOrPartyRole entity);

    // ... @Named MVO methods for each subtype

    // ========== MVO to Entity (for merge operations) ==========

    @Named("toEntityForMergePartyOrPartyRole")
    default InternalPartyOrPartyRole toEntityFromMVO(PartyOrPartyRoleMVO mvo) {
        if (mvo == null) {
            return null;
        }
        return switch (mvo.getAtType()) {
            case "BusinessPartner" -> toEntityFromBusinessPartnerMVO((BusinessPartnerMVO) mvo);
            case "Consumer" -> toEntityFromConsumerMVO((ConsumerMVO) mvo);
            // ... all 10 cases
            default -> throw new IllegalArgumentException("Unknown @type: " + mvo.getAtType());
        };
    }

    @BeanMapping(
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
    )
    InternalPartyOrPartyRole toEntityFromBusinessPartnerMVO(BusinessPartnerMVO mvo);

    // ... method declarations for each subtype
}
```

**CRITICAL Implementation Notes:**

1. **Self-Reference in uses clause**: `uses = PartyOrPartyRoleMapper.class` allows the mapper to reference itself for collection mappings
2. **Java 17 Limitation**: Cannot use pattern matching in switch (`case Type variable ->`), must use instanceof checks for FVO→Entity
3. **String-Based Switch OK**: Can use switch expressions with strings (`switch (entity.getAtType())`) for Entity→DTO/MVO
4. **4 Named Dispatch Methods**: Must have @Named methods for all 4 mapping directions
5. **Ignore Circular Fields**: Use `@Mapping(target = "relatedParty", ignore = true)` on MVO methods to avoid infinite recursion

### Layer 2: Wrapper Mapper

The wrapper mapper delegates polymorphic field mapping using explicit `qualifiedByName` references.

**File**: `RelatedPartyOrPartyRoleMapper.java`

```java
package com.pia.orbitant.dcmms.mapper;

import com.pia.orbitant.dcmms.entity.InternalRelatedPartyOrPartyRole;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.tmforum.openapi.dcmms.model.*;

/**
 * MapStruct mapper for RelatedPartyOrPartyRole wrapper class
 * Delegates polymorphic PartyOrPartyRole mapping to PartyOrPartyRoleMapper
 */
@jakarta.annotation.Generated(value = "DNext API Generator")
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = PartyOrPartyRoleMapper.class)
public interface RelatedPartyOrPartyRoleMapper {

    RelatedPartyOrPartyRoleMapper INSTANCE = Mappers.getMapper(RelatedPartyOrPartyRoleMapper.class);

    @Mapping(target = "partyOrPartyRole", source = "partyOrPartyRole", qualifiedByName = "toEntityForMergePartyOrPartyRole")
    @BeanMapping(
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
    )
    InternalRelatedPartyOrPartyRole mvoToEntity(RelatedPartyOrPartyRoleMVO mvo);

    @Mapping(target = "partyOrPartyRole", source = "partyOrPartyRole", qualifiedByName = "toPartyOrPartyRoleDto")
    RelatedPartyOrPartyRole toDto(InternalRelatedPartyOrPartyRole entity);

    @Mapping(target = "partyOrPartyRole", source = "partyOrPartyRole", qualifiedByName = "toEntityPartyOrPartyRoleFVO")
    InternalRelatedPartyOrPartyRole toEntity(RelatedPartyOrPartyRoleFVO fvo);

    @Mapping(target = "partyOrPartyRole", source = "partyOrPartyRole", qualifiedByName = "toPartyOrPartyRoleMVO")
    RelatedPartyOrPartyRoleMVO toMVO(InternalRelatedPartyOrPartyRole entity);
}
```

**CRITICAL Implementation Notes:**

1. **uses Clause**: Must include `uses = PartyOrPartyRoleMapper.class` to inject the polymorphic mapper
2. **qualifiedByName References**: Each method uses explicit @Mapping with `qualifiedByName` to reference the corresponding @Named method from PartyOrPartyRoleMapper
3. **Method Name Mapping**:
   - `mvoToEntity` → `toEntityForMergePartyOrPartyRole`
   - `toDto` → `toPartyOrPartyRoleDto`
   - `toEntity` → `toEntityPartyOrPartyRoleFVO`
   - `toMVO` → `toPartyOrPartyRoleMVO`
4. **Solves Method Overloading**: Wrapper mapper has different method names for different mapping directions, avoiding Java's overloading limitations
5. **Breaks Recursion**: Explicit qualifiedByName breaks circular dependency cycles

### Layer 3: Entity Mapper Usage

The managed entity mapper (e.g., CustomerMapper) uses the wrapper mapper, NOT the polymorphic mapper directly.

**File**: `CustomerMapper.java`

```java
@jakarta.annotation.Generated(value = "DNext API Generator")
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {RelatedPartyOrPartyRoleMapper.class})
public interface CustomerMapper
        extends BaseAppMapper<Customer, CustomerFVO, CustomerMVO, InternalCustomer> {

    CustomerMapper INSTANCE = Mappers.getMapper(CustomerMapper.class);
    String MAP_QUERY_NAME = "com.pia.orbitant.dcmms.mapper.CustomerMapper::getMapperMethodName";
    String MANAGED_ENTITY_TYPE = "Customer";

    @MapField(dto="@type", entity="atType")
    default String getMapperMethodName() {
        return MAP_QUERY_NAME;
    }

    @Override
    @Mapping(target = "href", expression = "java(com.pia.orbitant.common.core.component.HrefUtil.getHref(org.tmforum.openapi.dcmms.model.Customer.class, entity.getId()))")
    @Mapping(target = "atSchemaLocation", expression = "java(com.pia.orbitant.common.core.component.HrefUtil.getSchemaLocation(org.tmforum.openapi.dcmms.model.Customer.class))")
    Customer toDto(InternalCustomer entity);

    @Override
    CustomerMVO toMVO(InternalCustomer entity);

    @Override
    @Mapping(target = "revision", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "accessPolicyConstraint", ignore = true)
    @Mapping(target = "href", ignore = true)
    InternalCustomer toEntity(CustomerFVO fvo);

    @Override
    InternalCustomer mvoToEntity(CustomerMVO mvo);
}
```

**CRITICAL Implementation Notes:**

1. **Uses Wrapper Mapper**: `uses = {RelatedPartyOrPartyRoleMapper.class}`, NOT PartyOrPartyRoleMapper
2. **Automatic Nested Mapping**: MapStruct automatically uses RelatedPartyOrPartyRoleMapper for `List<RelatedPartyOrPartyRole> relatedParty` fields
3. **No Explicit @Mapping Needed**: MapStruct finds the wrapper mapper through the uses clause and applies it automatically
4. **Non-Polymorphic Customer**: If Customer itself is NOT polymorphic (has own @Document), no switch dispatch needed in CustomerMapper

## Entity Structure

### InternalPartyRole (Parent - No @Document)

```java
@Generated(value = "DNext API Generator")
public class InternalPartyRole extends TenantEntity implements Serializable {

    @Field(name = "at_type")
    private String atType;  // Discriminator field

    @Field(name = "name")
    private String name;

    @Field(name = "description")
    private String description;

    @Field(name = "role")
    private String role;

    // Common fields shared by all PartyRole subtypes
}
```

### InternalCustomer (Child - Has @Document)

```java
@Generated(value = "DNext API Generator")
@Document(collection = "customer")
public class InternalCustomer extends InternalPartyRole implements Serializable {
    // Customer has its own collection
    // BusinessPartner, Consumer, etc. are SIBLINGS of Customer, not children
    // They all extend InternalPartyRole but have separate @Document collections (if managed)
}
```

### InternalPartyOrPartyRole (Union Entity - No @Document)

```java
@Generated(value = "DNext API Generator")
public class InternalPartyOrPartyRole implements Serializable {

    @Field(name = "at_type")
    private String atType;

    // Contains ALL fields from ALL 10 subtypes (union/flattened structure)
    // BusinessPartner fields
    // Consumer fields
    // Customer fields
    // Individual fields
    // Organization fields
    // PartyRef fields
    // PartyRole fields
    // PartyRoleRef fields
    // Producer fields
    // Supplier fields
}
```

### InternalRelatedPartyOrPartyRole (Wrapper Entity)

```java
@Generated(value = "DNext API Generator")
public class InternalRelatedPartyOrPartyRole implements Serializable {

    @Field(name = "at_type")
    private String atType;

    @Field(name = "at_base_type")
    private String atBaseType;

    @Field(name = "at_schema_location")
    private String atSchemaLocation;

    @Field(name = "role")
    private String role;

    @Field(name = "party_or_party_role")
    private InternalPartyOrPartyRole partyOrPartyRole;  // Nested polymorphic field
}
```

## Detection Logic for Code Generator

```python
def needs_wrapper_mapper(schema_name: str, oas: dict) -> bool:
    """
    Determine if a schema needs a wrapper mapper pattern.

    Returns True if:
    1. Schema name starts with "Related" or "Wrapped" or similar wrapper prefix
    2. Schema has a property that references a polymorphic union type
    3. Referenced type has 2+ concrete subtypes (oneOf/anyOf with discriminator)
    """
    schema = oas['components']['schemas'].get(schema_name)
    if not schema:
        return False

    # Check for wrapper naming pattern
    if not any(prefix in schema_name for prefix in ['Related', 'Wrapped', 'Container']):
        return False

    # Check for nested polymorphic field
    for prop_name, prop_schema in schema.get('properties', {}).items():
        ref = prop_schema.get('$ref', '')
        if not ref:
            continue

        # Extract referenced type
        ref_type = ref.split('/')[-1]
        ref_schema = oas['components']['schemas'].get(ref_type)

        if not ref_schema:
            continue

        # Check if referenced type is polymorphic
        has_discriminator = 'discriminator' in ref_schema
        has_oneof = 'oneOf' in ref_schema
        has_anyof = 'anyOf' in ref_schema

        if has_discriminator or has_oneof or has_anyof:
            return True

    return False

def get_polymorphic_field_name(schema_name: str, oas: dict) -> str:
    """
    Get the name of the polymorphic field within the wrapper class.

    Example: For RelatedPartyOrPartyRole, returns "partyOrPartyRole"
    """
    schema = oas['components']['schemas'].get(schema_name)

    for prop_name, prop_schema in schema.get('properties', {}).items():
        ref = prop_schema.get('$ref', '')
        if not ref:
            continue

        ref_type = ref.split('/')[-1]
        ref_schema = oas['components']['schemas'].get(ref_type)

        if ref_schema and ('discriminator' in ref_schema or 'oneOf' in ref_schema):
            return prop_name

    return None
```

## Generation Workflow

### Phase 1: Analyze OAS

```python
wrapper_mappers = []
polymorphic_mappers = []

for schema_name in oas['components']['schemas']:
    if has_discriminator(schema_name):
        polymorphic_mappers.append({
            'name': schema_name,
            'subtypes': get_subtypes(schema_name),
            'field_name': get_polymorphic_field_name(schema_name)
        })

    if needs_wrapper_mapper(schema_name):
        wrapper_mappers.append({
            'name': schema_name,
            'wrapped_type': get_wrapped_type(schema_name),
            'field_name': get_polymorphic_field_name(schema_name)
        })
```

### Phase 2: Generate Polymorphic Mappers First

```python
for mapper_info in polymorphic_mappers:
    generate_polymorphic_mapper(
        name=mapper_info['name'],
        subtypes=mapper_info['subtypes'],
        template='polymorphic_mapper.java.j2'
    )
```

### Phase 3: Generate Wrapper Mappers

```python
for wrapper_info in wrapper_mappers:
    generate_wrapper_mapper(
        name=wrapper_info['name'],
        wrapped_type=wrapper_info['wrapped_type'],
        field_name=wrapper_info['field_name'],
        template='wrapper_mapper.java.j2'
    )
```

### Phase 4: Generate Entity Mappers

```python
for entity in managed_entities:
    uses_clause = []

    # Check entity fields for wrapper types
    for field in get_entity_fields(entity):
        if field.type in [w['name'] for w in wrapper_mappers]:
            # Use wrapper mapper, not polymorphic mapper
            uses_clause.append(f"{field.type}Mapper.class")

    generate_entity_mapper(
        entity=entity,
        uses=uses_clause,
        template='entity_mapper.java.j2'
    )
```

## Common Mistakes to Avoid

### ❌ WRONG: Using Polymorphic Mapper Directly in Entity Mapper

```java
// WRONG - This causes method overloading and recursion issues
@Mapper(uses = {PartyOrPartyRoleMapper.class})
public interface CustomerMapper extends BaseAppMapper<...> {
    // MapStruct can't resolve which map() method to use for nested fields
}
```

### ✅ CORRECT: Using Wrapper Mapper

```java
// CORRECT - Wrapper mapper solves overloading and provides explicit routing
@Mapper(uses = {RelatedPartyOrPartyRoleMapper.class})
public interface CustomerMapper extends BaseAppMapper<...> {
    // MapStruct uses wrapper mapper for List<RelatedPartyOrPartyRole> fields
}
```

### ❌ WRONG: Missing @Named Annotations

```java
// WRONG - Methods not marked as @Named can't be referenced by qualifiedByName
default PartyOrPartyRole toDto(InternalPartyOrPartyRole entity) {
    return switch (entity.getAtType()) {
        case "Customer" -> toCustomer(entity);  // toCustomer() must be @Named!
    };
}
```

### ✅ CORRECT: All Dispatch Targets are @Named

```java
// CORRECT - @Named annotation allows qualifiedByName reference
@Named("toPartyOrPartyRoleDto")
default PartyOrPartyRole toDto(InternalPartyOrPartyRole entity) {
    return switch (entity.getAtType()) {
        case "Customer" -> toCustomer(entity);
    };
}

@Named("toCustomer")
Customer toCustomer(InternalPartyOrPartyRole entity);
```

### ❌ WRONG: Forgetting Self-Reference in uses Clause

```java
// WRONG - Polymorphic mapper can't map List<InternalPartyOrPartyRole> without self-reference
@Mapper(componentModel = SPRING)  // Missing: uses = PartyOrPartyRoleMapper.class
public interface PartyOrPartyRoleMapper {
    // Cannot map collection fields that contain same type
}
```

### ✅ CORRECT: Self-Reference for Collection Mapping

```java
// CORRECT - Self-reference allows mapping collections of same type
@Mapper(componentModel = SPRING, uses = PartyOrPartyRoleMapper.class)
public interface PartyOrPartyRoleMapper {
    // Can now map List<InternalPartyOrPartyRole> relatedParty fields
}
```

## Validation Checklist

After generating mappers, verify:

### Polymorphic Mapper Validation
- [ ] Has 4 @Named dispatch methods (toDto, toMVO, toEntity, mvoToEntity)
- [ ] Each dispatch method uses switch or instanceof for type routing
- [ ] All concrete subtypes have corresponding @Named methods
- [ ] MVO methods include `@Mapping(target = "relatedParty", ignore = true)`
- [ ] Self-reference in uses clause: `uses = {ThisMapper.class}`
- [ ] FVO→Entity uses instanceof (not pattern matching) for Java 17
- [ ] Entity→DTO/MVO use switch expressions on discriminator

### Wrapper Mapper Validation
- [ ] Has 4 mapping methods (mvoToEntity, toDto, toEntity, toMVO)
- [ ] Uses clause includes polymorphic mapper: `uses = PolymorphicMapper.class`
- [ ] Each method has @Mapping with qualifiedByName for nested polymorphic field
- [ ] qualifiedByName values match @Named methods in polymorphic mapper
- [ ] No switch expressions (wrapper mapper is NOT polymorphic)

### Entity Mapper Validation
- [ ] Uses wrapper mapper, not polymorphic mapper directly
- [ ] MapStruct automatically applies wrapper mapper to nested fields
- [ ] No explicit @Mapping needed for wrapper fields (handled automatically)
- [ ] Compilation succeeds with zero errors

### Build Validation
```bash
mvn clean compile
```

Expected result:
- ✅ BUILD SUCCESS
- ⚠️ MapStruct warnings about unmapped fields (normal for framework-managed fields)
- ❌ Zero compilation errors

## Real-World Examples

### TMF629 Customer Management (DCMMS)

**Polymorphic Type**: `PartyOrPartyRole` (10 subtypes)
**Wrapper Type**: `RelatedPartyOrPartyRole`
**Managed Entity**: `Customer` (has `List<RelatedPartyOrPartyRole> relatedParty`)

**Generated Files**:
- `PartyOrPartyRoleMapper.java` - Polymorphic mapper with switch dispatch
- `RelatedPartyOrPartyRoleMapper.java` - Wrapper mapper with qualifiedByName
- `CustomerMapper.java` - Uses wrapper mapper

**Result**: Customer can store references to any PartyRole subtype (BusinessPartner, Consumer, etc.) in relatedParty field, with full polymorphic support.

### TMF621 Trouble Ticket

**Polymorphic Type**: `PartyRefOrPartyRoleRef` (2 subtypes: PartyRef, PartyRoleRef)
**Wrapper Type**: `RelatedPartyRefOrPartyRoleRef`
**Managed Entity**: `TroubleTicket` (has `List<RelatedPartyRefOrPartyRoleRef> relatedParty`)

**Generated Files**:
- `PartyRefOrPartyRoleRefMapper.java` - Polymorphic mapper
- `RelatedPartyPartyRefOrPartyRoleRefMapper.java` - Wrapper mapper
- `TroubleTicketMapper.java` - Uses wrapper mapper

## References

**Production Code Examples**:
- `trouble-ticket/mapper/PartyRefOrPartyRoleRefMapper.java` - Polymorphic mapper pattern
- `trouble-ticket/mapper/RelatedPartyPartyRefOrPartyRoleRefMapper.java` - Wrapper mapper pattern
- `dcmms-test/mapper/PartyOrPartyRoleMapper.java` - 10-subtype polymorphic mapper
- `dcmms-test/mapper/RelatedPartyOrPartyRoleMapper.java` - Wrapper mapper for 10 subtypes

**Related Documentation**:
- `polymorphic-mapper-pattern.md` - Base polymorphic mapper pattern
- `validation-checklist.md` - Build validation steps
- `known-issues-fixes.md` - Common compilation errors

**Framework**:
- MapStruct 1.5.5.Final
- Java 17 (no pattern matching in switch)
- Spring Boot 3.2.5
- common-core 3.12.0-ca
