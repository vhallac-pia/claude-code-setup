---
name: dnext-code-gen
description: DNext code generation patterns for TMF API services. Covers polymorphic type handling, MapStruct mapper generation, entity patterns, MongoDB storage strategies, and conditional code generation logic. Auto-activates when generating DNext API services from OpenAPI specs, working with TMF entities, or creating MapStruct mappers.
---

# DNext Code Generation Skill

Domain knowledge for AI-assisted generation of DNext TMF API services from OpenAPI specifications.

## Reference Materials

**Core Documentation:**
- `~/.claude/docs/code-generation/polymorphic-mapper-pattern.md` - Critical polymorphic dispatch patterns
- `~/.claude/docs/code-generation/entity-generation-guide.md` - Entity creation rules
- `~/.claude/docs/code-generation/mongodb-storage-patterns.md` - Storage pattern selection
- `~/.claude/docs/code-generation/mapstruct-conventions.md` - MapStruct configuration
- `~/.claude/docs/code-generation/configuration-reference.md` - YAML config structure
- `~/.claude/docs/code-generation/validation-checklist.md` - Post-generation validation
- `~/.claude/docs/code-generation/known-issues-fixes.md` - Common problems and solutions

**Examples:**
- `~/.claude/references/code-generation/examples/trouble-ticket/` - Polymorphic entity example
- `~/.claude/references/code-generation/examples/customer/` - Simple entity example

**DNext Integration:** (REQUIRED)
- `dnext-dev-support/references/code-generation/` - Framework-specific patterns and integration

## DNext Layered Architecture

```
API Layer (Controllers)
    ↓
Service Gateway Layer
    ↓
Service Layer (Business Logic)
    ↓
Repository Layer
    ↓
MongoDB
```

### Base Classes (MANDATORY)

**CRITICAL:** ALL generated classes MUST extend appropriate base classes. These are NOT optional patterns but required by DNext framework.

- **TenantEntity**: Base for all managed entities (adds tenant isolation, audit fields)
- **BaseAppMapper**: MapStruct mapper interface for DTO ↔ Entity conversion (4 generic params)
- **BaseAppService**: Service interface with CRUD operations (1 generic param)
- **BaseAppServiceImpl**: Service implementation with validation, patching, access control (1 generic param, 5-6 constructor params)
- **BaseRepository**: MongoDB repository interface (2 generic params: entity, ID type)
- **MongoRepository**: Spring Data MongoDB repository (2 generic params: entity, ID type)
- **BaseAppServiceGateway**: Orchestration layer connecting API to services (3 generic params)
- **BaseAppServiceGatewayImpl**: Service gateway implementation (8 generic params, 4 constructor params)
- **BaseAppEventMapper**: Event mapper interface (4 event type generic params)

### Package Structure

```
com.pia.orbitant.{module}/
├── api/                    # REST controllers
├── servicegateway/         # Service gateway implementations
├── service/                # Business services
├── entity/                 # Internal entities (MongoDB)
├── mapper/                 # MapStruct mappers
├── repository/             # MongoDB repositories
├── event/                  # Kafka event mappers
├── validation/             # Business validation
├── validator/              # Validators (post, patch, delete)
├── util/                   # Utilities
└── security/               # Security configuration
```

## TMF API Conventions

### DTO Types

1. **DTO (Data Transfer Object)** - GET response
   - Example: `TroubleTicket`
   - Has `@type`, `href`, `@schemaLocation` fields
   - Used in API responses

2. **FVO (Full Value Object)** - POST request body
   - Example: `TroubleTicketFVO`
   - Complete object for creation
   - Missing metadata fields (id, href, createdDate, etc.)

3. **MVO (Modification Value Object)** - PATCH request body
   - Example: `TroubleTicketMVO`
   - Partial object for updates
   - Supports merge patch semantics

### Internal Entity Requirements

**CRITICAL:** Must create Internal* version of ALL TMF model objects, not just managed entities.

Count should match OpenAPI Generator model classes MINUS these base types: Entity, Error, Addressable, Extensible, Hub.

This includes:
- Main managed entity and its subtypes
- All reference types (EntityRef, AccountRef, AttachmentRef, etc. → Internal* versions)
- All polymorphic types that share an endpoint (PartyOrPartyRole, AttachmentRefOrValue → Internal* versions)
- All polymorphic types with dedicated endpoints (Individual, Organization extending Party → Internal* versions)
- All value objects (Characteristic, TimePeriod, Quantity → Internal* versions)
- All related party references (RelatedParty with aclRelatedParty for access control → Internal* version)

**Rule:** If it exists in TMF model, create Internal* version for MongoDB persistence.

### Field Name Conversions

| TMF (JSON) | Java Field | MongoDB Field |
|------------|------------|---------------|
| `@type` | `atType` | `at_type` |
| `@baseType` | `atBaseType` | `at_base_type` |
| `@schemaLocation` | `atSchemaLocation` | `at_schema_location` |
| `kebab-case` | `camelCase` | `kebab_case` |

### Event Framework

**CRITICAL:** DNext uses Internal event wrappers, NOT TMF event types directly.

**TMF Event Types (defined in OAS, generated by OpenAPI Generator):**
- `{Entity}CreateEvent`
- `{Entity}DeleteEvent`
- `{Entity}StateChangeEvent`
- `{Entity}AttributeValueChangeEvent`

**DNext Internal Event Types (must be generated):**
- `Internal{Entity}CreateEvent` + `Internal{Entity}CreateEventPayload`
- `Internal{Entity}DeleteEvent` + `Internal{Entity}DeleteEventPayload`
- `Internal{Entity}AttributeValueChangeEvent` + `Internal{Entity}AttributeValueChangeEventPayload`
- `Internal{Entity}StateChangeEvent` + `Internal{Entity}StateChangeEventPayload`

**Total:** 8 files per entity (4 events + 4 payloads)

**Event Structure Pattern:**

```java
// Internal Event (extends AbstractEvent)
public class Internal{Entity}CreateEvent extends AbstractEvent implements Serializable {
    // Event metadata (framework fields)
    private String atType;
    private String atBaseType;
    private String href;
    private String id;
    private String correlationId;
    private String domain;
    private String title;
    private String description;
    private String priority;
    private OffsetDateTime timeOccurred;
    private InternalEntityRef source;
    private InternalEntityRef reportingSystem;
    private List<InternalRelatedPartyRefOrPartyRoleRef> relatedParty;
    private List<InternalCharacteristic> analyticCharacteristic;

    // Event-specific fields
    private String eventId;
    private OffsetDateTime eventTime;
    private String eventType;
    private Internal{Entity}CreateEventPayload event;  // Payload wrapper
}

// Event Payload (wraps TMF model)
public class Internal{Entity}CreateEventPayload implements Serializable {
    private {Entity} {entity};  // TMF model type, NOT Internal

    public Internal{Entity}CreateEventPayload({Entity} {entity}) {
        this.{entity} = {entity};
    }
}
```

**Event Mapper Pattern:**

```java
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface {Entity}EventMapper extends BaseAppEventMapper<
        {Entity},                              // TMF DTO
        Internal{Entity}CreateEvent,           // CREATE_EVENT
        Internal{Entity}DeleteEvent,           // DELETE_EVENT
        Internal{Entity}AttributeValueChangeEvent,  // ATTRIBUTE_CHANGE_EVENT
        Internal{Entity}StateChangeEvent> {    // STATE_CHANGE_EVENT

    // Constants
    String APPLICATION_NAME = "{domain}";  // e.g., "partyManagement"
    String EVENT_TYPE_CREATE = "Internal{Entity}CreateEvent";

    // Mapping method (framework fields ignored, use expressions)
    @Override
    @Mapping(target = "href", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "source", ignore = true)
    @Mapping(target = "relatedParty", ignore = true)
    @Mapping(target = "analyticCharacteristic", ignore = true)
    // ... more ignored fields
    @Mapping(target = "timeOccurred", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "eventId", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "event", expression = "java(new Internal{Entity}CreateEventPayload(dto))")
    @Mapping(target = "eventType", constant = EVENT_TYPE_CREATE)
    Internal{Entity}CreateEvent mapToCreateEvent({Entity} dto);

    // Repeat for other 3 event types
}
```


## Repository Pattern

**CRITICAL:** Repository must extend BOTH MongoRepository and BaseRepository

```java
@Repository
public interface CustomerRepository extends
        MongoRepository<InternalCustomer, String>,
        BaseRepository<InternalCustomer, String> {
}
```

**Required imports:**
- `com.pia.orbitant.common.core.repository.BaseRepository`
- `org.springframework.data.mongodb.repository.MongoRepository`
- `org.springframework.stereotype.Repository`

**Pattern Notes:**
- Must extend BOTH interfaces (dual inheritance)
- Generic parameters: `<EntityType, IDType>`
- ID type is always `String` for MongoDB entities

## MongoDB Storage Patterns

### Single Table Inheritance (TMF621 Pattern)

**Use when:** OAS has discriminator with single polymorphic endpoint

```java
@Document(collection = "trouble_ticket")
public class InternalTroubleTicket extends TenantEntity {
    @Field(name = "at_type")
    private String atType;  // Discriminator
    // ... base fields
}

// Child does NOT have @Document - inherits parent's collection
public class InternalBackOfficeUserTicket extends InternalTroubleTicket {
    // ... subclass fields
}
```

**Storage:** All types in single MongoDB collection, discriminated by `at_type` field.

### Table Per Concrete Class (TMF666 Pattern)

**Use when:** OAS has discriminator but separate endpoints per type

```java
@Document(collection = "billing_account")
public class InternalBillingAccount extends TenantEntity {
    // ... fields
}

@Document(collection = "party_account")
public class InternalPartyAccount extends TenantEntity {
    // ... fields
}
```

**Storage:** Each type has own MongoDB collection.

## MapStruct Mapper Patterns

### Simple (Non-Polymorphic) Mapper

**CRITICAL:** Must override ALL 4 methods (toDto, toMVO, toEntity, mvoToEntity). Python generator lacks mvoToEntity causing warnings.

```java
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CustomerMapper
        extends BaseAppMapper<Customer, CustomerFVO, CustomerMVO, InternalCustomer> {

    CustomerMapper INSTANCE = Mappers.getMapper(CustomerMapper.class);
    String MAP_QUERY_NAME = "com.pia.orbitant.dcmms.mapper.CustomerMapper::getMapperMethodName";
    String MANAGED_ENTITY_TYPE = "Customer";

    default String getMapperMethodName() {
        return MAP_QUERY_NAME;
    }

    @Override
    @Mapping(target = "relatedParty", ignore = true)  // Ignore polymorphic fields
    CustomerMVO toMVO(InternalCustomer entity);

    @Override
    @Mapping(target = "href", expression = "java(com.pia.orbitant.common.core.component.HrefUtil.getHref(Customer.class, entity.getId()))")
    @Mapping(target = "atSchemaLocation", expression = "java(com.pia.orbitant.common.core.component.HrefUtil.getSchemaLocation(Customer.class))")
    @Mapping(target = "relatedParty", ignore = true)  // Ignore polymorphic fields
    Customer toDto(InternalCustomer entity);

    @Override
    @Mapping(target = "revision", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "accessPolicyConstraint", ignore = true)
    @Mapping(target = "href", ignore = true)
    @Mapping(target = "relatedParty", ignore = true)  // Ignore polymorphic fields
    InternalCustomer toEntity(CustomerFVO fvo);

    @Override
    @Mapping(target = "revision", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "accessPolicyConstraint", ignore = true)
    @Mapping(target = "relatedParty", ignore = true)  // Ignore polymorphic fields
    InternalCustomer mvoToEntity(CustomerMVO mvo);
}
```

**Polymorphic Field Ignore Rules:**
- Add `@Mapping(target = "fieldName", ignore = true)` to ALL methods for fields with:
  - Types ending in `OrValue`, `OrYYY` (e.g., AttachmentRefOrValue, PartyOrPartyRole)
  - Abstract class or interface types
  - Example: `relatedParty`, `attachment`, `partyOrPartyRole`
- MapStruct cannot map abstract/interface types - must ignore them

### Polymorphic Mapper (CRITICAL PATTERN)

**When to use:** Entity has discriminator in OAS + Single Table Inheritance

**Requirements:**
1. Switch-based dispatch in 4 methods: `toDto`, `toMVO`, `toEntity`, `mvoToEntity`
2. Named mappers for each concrete type
3. Type-specific field mappings for subclass fields

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
    // POLYMORPHIC DISPATCH METHODS (4 total)
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
    // NAMED TYPE-SPECIFIC MAPPERS
    // ========================================

    @Named("toTroubleTicketDto")
    @Mapping(target = "href", expression = "java(com.pia.orbitant.common.core.component.HrefUtil.getHref(TroubleTicket.class, entity.getId()))")
    @Mapping(target = "atSchemaLocation", expression = "java(com.pia.orbitant.common.core.component.HrefUtil.getSchemaLocation(TroubleTicket.class))")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusStringToEnum")
    @Mapping(target = "attachment", source = "attachment", qualifiedByName = "toAttachmentRefOrValue")
    TroubleTicket toTroubleTicketDto(InternalTroubleTicket entity);

    @Named("toBackOfficeUserTicketDto")
    @Mapping(target = "href", expression = "java(com.pia.orbitant.common.core.component.HrefUtil.getHref(TroubleTicket.class, entity.getId()))")
    @Mapping(target = "atSchemaLocation", expression = "java(com.pia.orbitant.common.core.component.HrefUtil.getSchemaLocation(TroubleTicket.class))")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusStringToEnum")
    @Mapping(target = "attachment", source = "attachment", qualifiedByName = "toAttachmentRefOrValue")
    @Mapping(target = "contactMedium", source = "contactMedium", qualifiedByName = "toContactMediumDto")
    @Mapping(target = "bpmPlatformProcessReference", source = "bpmPlatformProcessReference", qualifiedByName = "backOfficeUserTicketSpecificationBpmPlatformDomainToDto")
    BackOfficeUserTicket toBackOfficeUserTicketDto(InternalBackOfficeUserTicket entity);

    // Similar patterns for toMVO, toEntity, mvoToEntity named methods...
}
```

**Critical:** Missing polymorphic dispatch causes data loss - subclass fields are not mapped!

## Code Generation Decision Trees

### 1. Polymorphic Type Detection

```
Check OAS for managed entity:
  └─ Has discriminator property?
      ├─ YES → Check paths in OAS
      │   ├─ Single endpoint (e.g., /troubleTicket)
      │   │   └─ Generate Single Table Inheritance
      │   │       ├─ Parent with @Document(collection)
      │   │       ├─ Children without @Document
      │   │       └─ Polymorphic mapper with switch dispatch
      │   └─ Multiple endpoints (e.g., /billingAccount, /partyAccount)
      │       └─ Generate Table Per Concrete Class
      │           ├─ Each type with @Document(collection)
      │           └─ Simple mappers (no switch)
      └─ NO → Generate simple entity + mapper
```

### 2. AclRelatedParty Handling

```
Check OAS for AclRelatedParty schema:
  ├─ Present (grep "AclRelatedParty:" > 0)
  │   └─ Add aclRelatedParty field to entities
  │       └─ Include AclOwnershipUtil in services
  └─ Absent (grep "AclRelatedParty:" = 0)
      └─ DO NOT add field
          └─ DO NOT generate AclOwnershipUtil
              └─ Remove from service implementations
```

### 3. Event Definition Check

```
For each managed_entity:
  └─ Check OAS for event schemas
      ├─ Has all 4 events (Create, Delete, StateChange, AttributeValueChange)
      │   └─ Generate full CRUD + events
      ├─ Missing events
      │   └─ SKIP entity (don't add to managed_entities)
      └─ Example: Account base class has no events
          └─ Only generate concrete types (BillingAccount, etc.)
```

## Common Dependencies

**CRITICAL:** Use direct version management with explicit properties, NOT BOM (Bill of Materials)

### Maven Coordinates

```xml
<properties>
    <java.version>17</java.version>
    <common-mongo.version>3.12.0-ca</common-mongo.version>
    <mapstruct.version>1.5.5.Final</mapstruct.version>
    <lombok.version>1.18.30</lombok.version>
</properties>

<dependencies>
    <!-- DNext Common -->
    <dependency>
        <groupId>com.pia.orbitant</groupId>
        <artifactId>common-mongo</artifactId>
        <version>${common-mongo.version}</version>
    </dependency>

    <dependency>
        <groupId>com.pia.orbitant</groupId>
        <artifactId>business-validator</artifactId>
        <version>3.12.0-ca</version>
    </dependency>

    <!-- Jackson Nullable (REQUIRED for event classes) -->
    <dependency>
        <groupId>org.openapitools</groupId>
        <artifactId>jackson-databind-nullable</artifactId>
        <version>0.2.6</version>
    </dependency>

    <!-- MapStruct -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>${mapstruct.version}</version>
    </dependency>

    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-mongodb</artifactId>
        <version>3.2.5</version>
    </dependency>
</dependencies>
```

**Version Notes:**
- `common-mongo 3.12.0-ca` provides `common-core 3.13.0-ca` with 8-parameter BaseAppServiceGatewayImpl
- `jackson-databind-nullable` is **REQUIRED** for event classes to compile
- Do NOT use BOM for version management at this stage

### Compiler Plugin for MapStruct

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct-processor</artifactId>
                <version>1.5.5.Final</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

## Critical Import Paths

**IMPORTANT:** Use correct import paths to avoid compilation errors

```java
// Repository
import com.pia.orbitant.common.core.repository.BaseRepository;  // NOT BaseAppRepository
import org.springframework.data.mongodb.repository.MongoRepository;

// Service Layer
import com.pia.orbitant.common.core.service.BaseAppService;
import com.pia.orbitant.common.core.service.BaseAppServiceImpl;
import com.pia.orbitant.common.core.component.Patcher;
import com.pia.orbitant.common.core.component.ValidationUtil;  // NOT util.ValidationUtil
import com.pia.orbitant.common.core.service.IfMatchService;
import com.pia.orbitant.common.core.util.AclOwnershipUtil;
import com.pia.orbitant.validator.business.common.BusinessValidationService;  // NOTE: validator.business.common, NOT common.validator
import com.pia.orbitant.common.exception.common.OrbitantException;

// Service Gateway
import com.pia.orbitant.common.core.servicegateway.BaseAppServiceGateway;
import com.pia.orbitant.common.core.servicegateway.BaseAppServiceGatewayImpl;
import com.pia.orbitant.common.core.service.EventService;
import com.pia.orbitant.common.core.model.EventSubscriptionModel;

// Mapper
import com.pia.orbitant.common.core.mapper.BaseAppMapper;
import com.pia.orbitant.common.core.mapper.BaseAppEventMapper;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
```

## Ignored Fields in Mappers

Always ignore these fields when mapping FVO/MVO → Entity:

```java
@Mapping(target = "revision", ignore = true)
@Mapping(target = "createdDate", ignore = true)
@Mapping(target = "updatedDate", ignore = true)
@Mapping(target = "createdBy", ignore = true)
@Mapping(target = "updatedBy", ignore = true)
@Mapping(target = "accessPolicyConstraint", ignore = true)
@Mapping(target = "href", ignore = true)
```

**Also ignore polymorphic fields** (fields with abstract/interface types):
```java
@Mapping(target = "relatedParty", ignore = true)
@Mapping(target = "attachment", ignore = true)
@Mapping(target = "partyOrPartyRole", ignore = true)
```

These are managed by the framework or cannot be mapped by MapStruct.

## Generation Validation Checklist

After generation, verify:

- [ ] `mvn clean compile` succeeds (both model and API)
- [ ] Polymorphic mappers have switch-based dispatch (if applicable)
- [ ] No field shadowing (same field in parent and child)
- [ ] AclOwnershipUtil only present if AclRelatedParty in OAS
- [ ] All managed entities have event definitions in OAS
- [ ] Collection names match configuration
- [ ] Package structure follows conventions
- [ ] MapStruct warnings are expected (unmapped properties ok)
- [ ] Zero compilation errors

## Common Issues and Fixes

### Issue: Data Loss in Polymorphic Mapping

**Symptom:** Subclass-specific fields (contactMedium, bpmPlatformProcessReference) are null after mapping

**Cause:** Missing switch-based dispatch in mapper

**Fix:** Add polymorphic dispatch methods (see pattern above)

### Issue: Compilation Error - AclOwnershipUtil not found

**Symptom:** Service implementation can't find AclOwnershipUtil

**Cause:** AclRelatedParty schema not in OAS

**Fix:** Remove AclOwnershipUtil from service constructor and usage

### Issue: Field Shadowing Warning

**Symptom:** Same field defined in both parent and child entity

**Cause:** Template added field to both classes

**Fix:** Remove field from child, keep only in appropriate class per OAS schema

## When to Use This Skill

- Generating DNext TMF API services from OpenAPI specs
- Creating MapStruct mappers for TMF entities
- Implementing polymorphic type handling
- Deciding MongoDB storage pattern
- Troubleshooting mapper generation issues
- Validating generated code structure
