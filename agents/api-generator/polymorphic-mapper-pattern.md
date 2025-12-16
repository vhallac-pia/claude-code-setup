# Polymorphic Mapper Pattern for MapStruct

**Critical Pattern for TMF APIs with Discriminated Types**

## When to Use

Use this pattern when:
- TMF API has `discriminator` property in OpenAPI spec
- Single endpoint serves multiple concrete types (e.g., `/troubleTicket`)
- Entity uses Single Table Inheritance (all types in same MongoDB collection)
- Examples: TMF621 TroubleTicket, TMF620 ProductSpecification

**DO NOT use** when:
- No discriminator in OAS
- Separate endpoints per type (Table Per Concrete Class pattern)
- Simple non-polymorphic entity

## The Problem

**Without polymorphic dispatch**: Subclass-specific fields are LOST during DTO↔Entity conversion.

**Example failure:**
```java
InternalBackOfficeUserTicket entity = new InternalBackOfficeUserTicket();
entity.setContactMedium(contactList);      // Subclass field
entity.setBpmPlatformProcessReference(ref); // Subclass field

// Simple mapper WITHOUT switch dispatch:
TroubleTicket dto = mapper.toDto(entity);

// Result: dto is base TroubleTicket, contactMedium and bpmRef are NULL!
// DATA LOSS - subclass fields not mapped!
```

## The Solution: Switch-Based Dispatch

### Complete Pattern

```java
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {
        ContactMediumMapper.class,
        AttachmentRefOrValueMapper.class
})
public interface TroubleTicketMapper
        extends BaseAppMapper<TroubleTicket, TroubleTicketFVO, TroubleTicketMVO, InternalTroubleTicket> {

    TroubleTicketMapper INSTANCE = Mappers.getMapper(TroubleTicketMapper.class);
    String MAP_QUERY_NAME = "com.pia.orbitant.dttms.mapper.TroubleTicketMapper::getMapperMethodName";
    String MANAGED_ENTITY_TYPE = "TroubleTicket";

    @MapField(dto="@type", entity="atType")
    default String getMapperMethodName() {
        return MAP_QUERY_NAME;
    }

    // ========================================
    // REQUIRED: 4 POLYMORPHIC DISPATCH METHODS
    // ========================================

    @Override
    default TroubleTicket toDto(InternalTroubleTicket entity) {
        if (entity == null) {
            return null;
        }

        return switch (entity.getAtType()) {
            case "TroubleTicket" -> toTroubleTicketDto(entity);
            case "BackOfficeUserTicket" -> toBackOfficeUserTicketDto((InternalBackOfficeUserTicket) entity);
            default -> toTroubleTicketDto(entity);
        };
    }

    @Override
    default TroubleTicketMVO toMVO(InternalTroubleTicket entity) {
        if (entity == null) {
            return null;
        }

        return switch (entity.getAtType()) {
            case "TroubleTicket" -> toTroubleTicketMVO(entity);
            case "BackOfficeUserTicket" -> toBackOfficeUserTicketMVO((InternalBackOfficeUserTicket) entity);
            default -> toTroubleTicketMVO(entity);
        };
    }

    @Override
    default InternalTroubleTicket toEntity(TroubleTicketFVO fvo) {
        if (fvo == null) {
            return null;
        }

        return switch (fvo.getAtType()) {
            case "TroubleTicket" -> toTroubleTicketEntity(fvo);
            case "BackOfficeUserTicket" -> toBackOfficeUserTicketEntity((BackOfficeUserTicketFVO) fvo);
            default -> toTroubleTicketEntity(fvo);
        };
    }

    @Override
    default InternalTroubleTicket mvoToEntity(TroubleTicketMVO mvo) {
        if (mvo == null) {
            return null;
        }

        return switch (mvo.getAtType()) {
            case "TroubleTicket" -> troubleTicketMvoToEntity(mvo);
            case "BackOfficeUserTicket" -> backOfficeUserTicketMvoToEntity((BackOfficeUserTicketMVO) mvo);
            default -> troubleTicketMvoToEntity(mvo);
        };
    }

    // ========================================
    // REQUIRED: NAMED TYPE-SPECIFIC MAPPERS
    // ========================================

    // Base type: Entity → DTO
    @Named("toTroubleTicketDto")
    @Mapping(target = "href", expression = "java(com.pia.orbitant.common.core.component.HrefUtil.getHref(TroubleTicket.class, entity.getId()))")
    @Mapping(target = "atSchemaLocation", expression = "java(com.pia.orbitant.common.core.component.HrefUtil.getSchemaLocation(TroubleTicket.class))")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusStringToEnum")
    @Mapping(target = "attachment", source = "attachment", qualifiedByName = "toAttachmentRefOrValue")
    TroubleTicket toTroubleTicketDto(InternalTroubleTicket entity);

    // Subclass type: Entity → DTO
    @Named("toBackOfficeUserTicketDto")
    @Mapping(target = "href", expression = "java(com.pia.orbitant.common.core.component.HrefUtil.getHref(TroubleTicket.class, entity.getId()))")
    @Mapping(target = "atSchemaLocation", expression = "java(com.pia.orbitant.common.core.component.HrefUtil.getSchemaLocation(TroubleTicket.class))")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusStringToEnum")
    @Mapping(target = "attachment", source = "attachment", qualifiedByName = "toAttachmentRefOrValue")
    @Mapping(target = "contactMedium", source = "contactMedium", qualifiedByName = "toContactMediumDto")
    @Mapping(target = "bpmPlatformProcessReference", source = "bpmPlatformProcessReference", qualifiedByName = "backOfficeUserTicketSpecificationBpmPlatformDomainToDto")
    BackOfficeUserTicket toBackOfficeUserTicketDto(InternalBackOfficeUserTicket entity);

    // Base type: Entity → MVO
    @Named("toTroubleTicketMVO")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusStringToEnum")
    @Mapping(target = "attachment", source = "attachment", qualifiedByName = "toAttachmentRefOrValueMVO")
    TroubleTicketMVO toTroubleTicketMVO(InternalTroubleTicket entity);

    // Subclass type: Entity → MVO
    @Named("toBackOfficeUserTicketMVO")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusStringToEnum")
    @Mapping(target = "attachment", source = "attachment", qualifiedByName = "toAttachmentRefOrValueMVO")
    BackOfficeUserTicketMVO toBackOfficeUserTicketMVO(InternalBackOfficeUserTicket entity);

    // Base type: FVO → Entity
    @Named("toTroubleTicketEntity")
    @Mapping(target = "revision", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "accessPolicyConstraint", ignore = true)
    @Mapping(target = "href", ignore = true)
    @Mapping(target = "status", source = "status", qualifiedByName = "statusEnumToString")
    @Mapping(target = "attachment", source = "attachment", qualifiedByName = "toInternalAttachmentRefOrValue")
    InternalTroubleTicket toTroubleTicketEntity(TroubleTicketFVO fvo);

    // Subclass type: FVO → Entity
    @Named("toBackOfficeUserTicketEntity")
    @Mapping(target = "revision", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "accessPolicyConstraint", ignore = true)
    @Mapping(target = "href", ignore = true)
    @Mapping(target = "status", source = "status", qualifiedByName = "statusEnumToString")
    @Mapping(target = "attachment", source = "attachment", qualifiedByName = "toInternalAttachmentRefOrValue")
    @Mapping(target = "bpmPlatformProcessReference", source = "bpmPlatformProcessReference", qualifiedByName = "backOfficeUserTicketSpecificationBpmPlatformDomainToEntity")
    InternalBackOfficeUserTicket toBackOfficeUserTicketEntity(BackOfficeUserTicketFVO fvo);

    // Base type: MVO → Entity
    @Named("troubleTicketMvoToEntity")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusEnumToString")
    @Mapping(target = "attachment", source = "attachment", qualifiedByName = "mvoToEntityAttachmentRefOrValue")
    InternalTroubleTicket troubleTicketMvoToEntity(TroubleTicketMVO mvo);

    // Subclass type: MVO → Entity
    @Named("backOfficeUserTicketMvoToEntity")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusEnumToString")
    @Mapping(target = "attachment", source = "attachment", qualifiedByName = "mvoToEntityAttachmentRefOrValue")
    @Mapping(target = "bpmPlatformProcessReference", source = "bpmPlatformProcessReference", qualifiedByName = "backOfficeUserTicketSpecificationBpmPlatformDomainToEntity")
    InternalBackOfficeUserTicket backOfficeUserTicketMvoToEntity(BackOfficeUserTicketMVO mvo);

    // Helper converters (if needed)
    @Named("statusStringToEnum")
    default TroubleTicketStatusType statusStringToEnum(String enumString) {
        return enumString != null ? TroubleTicketStatusType.fromValue(enumString) : null;
    }

    @Named("statusEnumToString")
    default String statusEnumToString(TroubleTicketStatusType enumType) {
        return enumType != null ? enumType.getValue() : null;
    }
}
```

## Key Requirements

### 1. Four Dispatch Methods

**Must override all 4 methods** with switch expressions:
- `toDto(InternalEntity entity)` - Entity → DTO
- `toMVO(InternalEntity entity)` - Entity → MVO
- `toEntity(EntityFVO fvo)` - FVO → Entity
- `mvoToEntity(EntityMVO mvo)` - MVO → Entity

### 2. Switch Expression Pattern

```java
return switch (entity.getAtType()) {
    case "BaseType" -> toBaseTypeDto(entity);
    case "SubType" -> toSubTypeDto((InternalSubType) entity);
    default -> toBaseTypeDto(entity);  // Fallback to base
};
```

**Critical:**
- Switch on discriminator field (`atType`)
- Explicit type cast for subclass: `(InternalSubType) entity`
- Default case for unknown types (graceful degradation)

### 3. Named Type-Specific Mappers

**For each concrete type**, create 4 named methods:
- `@Named("toXxxDto")` - Entity → DTO
- `@Named("toXxxMVO")` - Entity → MVO
- `@Named("toXxxEntity")` - FVO → Entity
- `@Named("xxxMvoToEntity")` - MVO → Entity

**Naming convention:**
- Methods for base type: `toTroubleTicketDto`
- Methods for subtypes: `toBackOfficeUserTicketDto`
- MVO to Entity: lowercase prefix: `troubleTicketMvoToEntity`

### 4. Subclass-Specific Field Mappings

In subclass named methods, add mappings for subclass-only fields:

```java
@Named("toBackOfficeUserTicketDto")
// ... base field mappings ...
@Mapping(target = "contactMedium", source = "contactMedium", qualifiedByName = "toContactMediumDto")
@Mapping(target = "bpmPlatformProcessReference", source = "bpmPlatformProcessReference", qualifiedByName = "backOfficeUserTicketSpecificationBpmPlatformDomainToDto")
BackOfficeUserTicket toBackOfficeUserTicketDto(InternalBackOfficeUserTicket entity);
```

## Detection Algorithm

**How to know if polymorphic dispatch is needed:**

```
1. Parse OpenAPI specification for managed entity
2. Check for discriminator property:
   discriminator:
     propertyName: '@type'
     mapping:
       TroubleTicket: '#/components/schemas/TroubleTicket'
       BackOfficeUserTicket: '#/components/schemas/BackOfficeUserTicket'
3. Check API paths:
   - Single endpoint (/troubleTicket) → Single Table Inheritance → NEEDS DISPATCH
   - Multiple endpoints (/billingAccount, /partyAccount) → Table Per Class → NO DISPATCH
4. If discriminator + single endpoint → Generate polymorphic mapper
```

## Common Mistakes

### ❌ Missing Switch Dispatch

```java
// WRONG - No override, MapStruct auto-generates without type awareness
public interface TroubleTicketMapper extends BaseAppMapper<...> {
    // Missing @Override methods!
}
```

**Result:** Data loss - subclass fields not mapped

### ❌ Incorrect Type Casting

```java
// WRONG - No cast
case "BackOfficeUserTicket" -> toBackOfficeUserTicketDto(entity);
// Should be:
case "BackOfficeUserTicket" -> toBackOfficeUserTicketDto((InternalBackOfficeUserTicket) entity);
```

**Result:** Compilation error or ClassCastException

### ❌ Missing Named Methods

```java
// WRONG - Switch exists but no named methods to call
return switch (entity.getAtType()) {
    case "BackOfficeUserTicket" -> toBackOfficeUserTicketDto(...);
    // No @Named("toBackOfficeUserTicketDto") method exists!
};
```

**Result:** Compilation error - method not found

### ❌ Missing Subclass Field Mappings

```java
// WRONG - Named method exists but missing subclass field mappings
@Named("toBackOfficeUserTicketDto")
@Mapping(target = "href", expression = "...")
// Missing: contactMedium, bpmPlatformProcessReference mappings!
BackOfficeUserTicket toBackOfficeUserTicketDto(InternalBackOfficeUserTicket entity);
```

**Result:** Subclass fields mapped as null - data loss

## Validation

After generating polymorphic mapper, verify:

- [ ] All 4 dispatch methods present with `@Override`
- [ ] Each dispatch method has switch expression
- [ ] Switch on discriminator field (`getAtType()`)
- [ ] Explicit type casts in switch cases
- [ ] Default case present
- [ ] Named methods exist for each case
- [ ] Subclass named methods include subclass-specific `@Mapping` annotations
- [ ] Helper converters present (if enums or special types)
- [ ] Compare with reference example (e.g., trouble-ticket mapper)

## Reference Implementation

See reference example: `~/.claude/references/code-generation/examples/trouble-ticket/TroubleTicketMapper.java`

**Pattern comparison:**
- Reference mapper: 196 lines with full polymorphic dispatch
- Simple generated mapper: 40 lines WITHOUT dispatch = BROKEN

**Impact:** Missing dispatch causes data loss - subclass fields not mapped!

---

# Nested Polymorphic Field Pattern

**For polymorphic types used as NESTED FIELDS (not main entity)**

## When to Use

Use this pattern when:
- A polymorphic interface is used as a **nested field** in an entity
- Internal entity stores TMF types directly (not Internal* types)
- Example: `PartyOrPartyRole partyOrPartyRole` field inside `RelatedPartyOrPartyRole`
- MapStruct error: "Can't map property... Consider to declare/implement a mapping method"

**DO NOT use** when:
- Polymorphic type is the main managed entity (use main pattern above)
- Internal entity has flattened Internal* types for nested objects

## The Problem

**MapStruct error:**
```
Can't map property "PartyOrPartyRole relatedParty[].partyOrPartyRole" to
"PartyOrPartyRoleMVO relatedParty[].partyOrPartyRole".
Consider to declare/implement a mapping method: "PartyOrPartyRoleMVO map(PartyOrPartyRole value)".
```

**Root Cause:** MapStruct cannot automatically generate mapping methods for abstract/interface types with polymorphic subtypes.

## The Solution: Dedicated Passthrough Mapper

### Pattern Structure

When internal entity stores TMF types directly, create a simple passthrough mapper:

```java
package com.pia.orbitant.dcmms.mapper;

import org.mapstruct.Mapper;
import org.tmforum.openapi.dcmms.model.*;

@Mapper(componentModel = "spring")
public interface PartyOrPartyRoleMapper {

    // Single-item conversions for nested polymorphic fields
    // FVO/MVO are marker interfaces extending base types, so we can safely cast

    // FVO -> TMF (passthrough, they're compatible)
    default PartyOrPartyRole map(PartyOrPartyRoleFVO fvo) {
        return fvo;  // FVO extends PartyOrPartyRole
    }

    // TMF -> MVO (cast)
    default PartyOrPartyRoleMVO map(PartyOrPartyRole dto) {
        return (PartyOrPartyRoleMVO) dto;  // Safe cast if dto is MVO-compatible
    }

    // MVO -> TMF (passthrough)
    default PartyOrPartyRole mapMvoToDto(PartyOrPartyRoleMVO mvo) {
        return mvo;  // MVO extends PartyOrPartyRole
    }
}
```

### Integration with Main Mapper

Add `uses` clause to reference the dedicated mapper:

```java
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {PartyOrPartyRoleMapper.class})
public interface CustomerMapper
        extends BaseAppMapper<Customer, CustomerFVO, CustomerMVO, InternalCustomer> {
    // MapStruct automatically uses PartyOrPartyRoleMapper methods
    // when mapping nested PartyOrPartyRole fields
}
```


## When NOT to Use This Pattern

**Use the full flattened Internal* pattern instead** when:
- Internal entity should use Internal* types for all nested objects (proper separation of concerns)
- Need type-safe internal representation independent of external API model
- Persistence layer should not store polymorphic TMF structures
- Multiple nested polymorphic types with complex field unions

**Proper architecture (future refactoring):**
```java
// Internal entity with Internal* types
public class InternalCustomer {
    private List<InternalRelatedPartyOrPartyRole> relatedParty;  // Internal type
}

// Flattened internal type
public class InternalPartyOrPartyRole {
    private String atType;  // discriminator
    // Union of ALL fields from all 10 subtypes (~88 fields)
    private String href;
    private String id;
    // ... Individual-specific fields
    // ... Organization-specific fields
    // ... PartyRole fields
    // ... Reference fields
}

// Dedicated mapper with instanceof/switch dispatch
@Mapper(componentModel = "spring")
public interface PartyOrPartyRoleMapper {
    // FVO -> Internal (instanceof dispatch)
    default InternalPartyOrPartyRole toInternal(PartyOrPartyRoleFVO fvo) {
        if (fvo instanceof IndividualFVO) return toInternal((IndividualFVO) fvo);
        if (fvo instanceof OrganizationFVO) return toInternal((OrganizationFVO) fvo);
        // ... handle all 10 subtypes
    }

    // Internal -> TMF (switch on @type)
    default PartyOrPartyRole toDto(InternalPartyOrPartyRole internal) {
        return switch (internal.getAtType()) {
            case "Individual" -> toIndividual(internal);
            case "Organization" -> toOrganization(internal);
            // ... handle all 10 subtypes
        };
    }
}
```

## Detection Algorithm

**How to know if nested polymorphic mapper is needed:**

```
1. Compilation fails with MapStruct error about abstract/interface type
2. Error mentions nested field path (e.g., "relatedParty[].partyOrPartyRole")
3. Check internal entity:
   - Stores TMF type directly? → Use passthrough mapper
   - Stores Internal* type? → Use flattened entity + dispatch mapper
4. Check if FVO/MVO extend base type → Confirms passthrough compatibility
```

## Common Mistakes

### ❌ Creating Unnecessary Internal Entity

```java
// WRONG - Creating flattened Internal* when internal entity stores TMF type
public class InternalPartyOrPartyRole {
    // 88 fields union...
}
```

**Problem:** Compilation fails - missing all Internal* dependency classes

**Fix:** Check what internal entity actually stores first

### ❌ Missing `uses` Clause

```java
// WRONG - Forgot to reference the mapper
@Mapper(componentModel = "spring")
public interface CustomerMapper extends BaseAppMapper<...> {
    // MapStruct still can't find PartyOrPartyRole mapping methods!
}
```

**Fix:** Add `uses = {PartyOrPartyRoleMapper.class}`

### ❌ Wrong Method Signatures

```java
// WRONG - MapStruct looks for specific signatures
default PartyOrPartyRole convert(PartyOrPartyRoleFVO fvo) {  // Wrong name
    return fvo;
}
```

**Fix:** Use `map()` as method name (MapStruct convention)

## Validation

After generating nested polymorphic mapper, verify:

- [ ] Dedicated mapper interface created (e.g., `PartyOrPartyRoleMapper`)
- [ ] Three passthrough methods: FVO→TMF, TMF→MVO, MVO→TMF
- [ ] Main entity mapper has `uses` clause referencing dedicated mapper
- [ ] Compilation succeeds without "Can't map property" errors
- [ ] Internal entity stores TMF types (not Internal* types)

## TODO: Future Architecture Refactoring

The current passthrough solution works but couples persistence to external API model. For production:

1. Create flattened `InternalPartyOrPartyRole` with union of all subtype fields
2. Create dedicated mapper with instanceof/switch dispatch (like main pattern)
3. Update `InternalCustomer` to use `List<InternalRelatedPartyOrPartyRole>`
4. Requires creating all missing Internal* dependency classes

**See:** `temp/ai-api-generator-todo.md` for tracking this architectural issue
