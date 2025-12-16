# Known Issues and Fixes

## Critical Issues

### Issue 1: Polymorphic Mapper Data Loss

**Severity:** CRITICAL - Results in data loss

**Symptoms:**
- Subclass-specific fields return null in API responses
- Data exists in MongoDB but not in DTOs
- No compilation errors or warnings

**Root Cause:**

Simple mapper implementation loses subclass data:

```java
// WRONG - Direct mapping loses subclass fields
@Mapping(target = "href", expression = "java(...)")
TroubleTicket toDto(InternalTroubleTicket entity);
```

When entity is `InternalBackOfficeUserTicket` (subclass), the simple mapping only copies fields from `InternalTroubleTicket` (parent). Subclass-specific fields are ignored.

**Fix:**

Implement switch-based dispatch pattern with named methods for each concrete type:

```java
@Override
default TroubleTicket toDto(InternalTroubleTicket entity) {
    if (entity == null) return null;

    return switch (entity.getAtType()) {
        case "TroubleTicket" -> toTroubleTicketDto(entity);
        case "BackOfficeUserTicket" -> toBackOfficeUserTicketDto((InternalBackOfficeUserTicket) entity);
        default -> toTroubleTicketDto(entity);
    };
}

@Named("toBackOfficeUserTicketDto")
@Mapping(target = "contactMedium", source = "contactMedium")
@Mapping(target = "bpmPlatformProcessReference", source = "bpmPlatformProcessReference")
@Mapping(target = "candidateRoles", source = "candidateRoles")
TroubleTicket toBackOfficeUserTicketDto(InternalBackOfficeUserTicket entity);
```

**Validation:**
- [ ] 4 dispatch methods present (toDto, toMVO, toEntity, mvoToEntity)
- [ ] Switch expression on discriminator field
- [ ] Explicit type casts: `(InternalSubType) entity`
- [ ] Named methods for each concrete type
- [ ] Subclass-specific `@Mapping` annotations

**Reference:** polymorphic-mapper-pattern.md for complete implementation.

### Issue 2: AclOwnershipUtil Compilation Errors

**Severity:** HIGH - Breaks build

**Symptoms:**
```
[ERROR] cannot find symbol: class AclOwnershipUtil
[ERROR] cannot find symbol: method addDefaultEntityOwner(...)
```

**Root Cause:**

Service implementation uses `AclOwnershipUtil` but:
1. Dependency not declared in pom.xml, OR
2. OAS schema missing `AclRelatedParty` definition, OR
3. Import statement incorrect

**Fix:**

Check OAS for AclRelatedParty schema:

```bash
grep -c "AclRelatedParty:" {OAS-file}
```

**If count > 0:**

1. Add dependency with explicit version:
```xml
<dependency>
    <groupId>com.pia.orbitant</groupId>
    <artifactId>common-acl</artifactId>
    <version>${common-mongo.version}</version>
</dependency>
```

2. Include in service:
```java
import com.pia.orbitant.common.acl.util.AclOwnershipUtil;

public class CustomerServiceImpl extends BaseAppServiceImpl<...> {

    private final AclOwnershipUtil aclOwnershipUtil;

    public CustomerServiceImpl(..., AclOwnershipUtil aclOwnershipUtil) {
        this.aclOwnershipUtil = aclOwnershipUtil;
    }

    @Override
    public CustomerFVO create(CustomerFVO fvo) {
        InternalCustomer entity = mapper.toEntity(fvo);
        aclOwnershipUtil.addDefaultEntityOwner(entity);
        entity = repository.save(entity);
        return mapper.toFVO(entity);
    }
}
```

**If count = 0:**

Remove all AclOwnershipUtil references - not applicable for this API.

**Validation:**
- [ ] `grep "AclRelatedParty:" {OAS}` matches code usage
- [ ] Dependency present if needed
- [ ] NO references if schema absent

### Issue 3: Field Shadowing in Polymorphic Entities

**Severity:** MEDIUM - Causes warnings, potential bugs

**Symptoms:**
```
[WARNING] Field 'aclRelatedParty' in InternalBackOfficeUserTicket shadows field in InternalTroubleTicket
```

**Root Cause:**

Same field declared in both parent and child entity:

```java
// WRONG
public class InternalTroubleTicket extends TenantEntity {
    private List<InternalAclRelatedParty> aclRelatedParty;
}

public class InternalBackOfficeUserTicket extends InternalTroubleTicket {
    private List<InternalAclRelatedParty> aclRelatedParty;  // SHADOWS parent!
}
```

**Fix:**

Analyze OAS schema to determine correct placement:

1. If field in parent schema → declare in parent only
2. If field in child schema only → declare in child only
3. Field is inherited automatically, never redeclare

```java
// CORRECT
public class InternalTroubleTicket extends TenantEntity {
    private List<InternalAclRelatedParty> aclRelatedParty;  // In parent
}

public class InternalBackOfficeUserTicket extends InternalTroubleTicket {
    // Field inherited, not redeclared
    private List<InternalContactMedium> contactMedium;  // Child-specific only
}
```

**Validation:**
- [ ] Check OAS schema allOf/inheritance structure
- [ ] Shared fields in parent only
- [ ] Subclass-specific fields in children only
- [ ] No field name duplication

### Issue 4: Incorrect Polymorphic Mapper for Non-Polymorphic Entity

**Severity:** CRITICAL - Causes 24+ compilation errors

**Symptoms:**
```
[ERROR] incompatible types: InternalCustomer cannot be converted to InternalBusinessPartner
[ERROR] incompatible types: InternalCustomer cannot be converted to InternalConsumer
[ERROR] method toBusinessPartner in interface CustomerMapper cannot be applied to given types
[ERROR] cannot find symbol: method getAtType()
```

**Root Cause:**

Generator incorrectly creates polymorphic dispatch pattern for sibling entities that share a common parent but are NOT in an inheritance relationship:

```java
// WRONG - Customer is NOT polymorphic
@Document(collection = "customer")  // Own collection, NOT Single Table Inheritance
public class InternalCustomer extends InternalPartyRole {
    // Customer-specific fields
}

@Mapper(...)
public interface CustomerMapper extends BaseAppMapper<Customer, ..., InternalCustomer> {
    // WRONG - Switch dispatch for sibling types (not subtypes!)
    @Override
    default Customer toDto(InternalCustomer entity) {
        return switch (entity.getAtType()) {  // InternalCustomer has no atType field!
            case "BusinessPartner" -> toBusinessPartner(entity);  // Type cast fails!
            case "Consumer" -> toConsumer(entity);  // Type cast fails!
            default -> toCustomerDto(entity);
        };
    }

    Customer toBusinessPartner(InternalBusinessPartner entity);  // Wrong parameter type!
}
```

**Key Detection Logic:**

An entity is NOT polymorphic (should NOT use switch dispatch) if:
1. It has `@Document(collection = "entity_name")` with its OWN collection name
2. Sibling entities have DIFFERENT collection names
3. No discriminator field in entity class
4. No inheritance relationship with sibling types

**Fix:**

1. **Simplify Entity Mapper** (remove polymorphism):

```java
// CORRECT - Simple mapper for non-polymorphic entity
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {RelatedPartyOrPartyRoleMapper.class})
public interface CustomerMapper
        extends BaseAppMapper<Customer, CustomerFVO, CustomerMVO, InternalCustomer> {

    @Override
    @Mapping(target = "href", expression = "java(com.pia.orbitant.common.core.component.HrefUtil.getHref(org.tmforum.openapi.dcmms.model.Customer.class, entity.getId()))")
    Customer toDto(InternalCustomer entity);

    @Override
    CustomerMVO toMVO(InternalCustomer entity);

    @Override
    @Mapping(target = "revision", ignore = true)
    // ... framework fields
    InternalCustomer toEntity(CustomerFVO fvo);

    @Override
    InternalCustomer mvoToEntity(CustomerMVO mvo);
}
```

2. **Create Wrapper Mapper for Nested Polymorphic Fields:**

If entity contains fields like `relatedParty` that CAN be multiple types (PartyOrPartyRole), create wrapper mapper:

```java
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = PartyOrPartyRoleMapper.class)
public interface RelatedPartyOrPartyRoleMapper {

    @Mapping(target = "partyOrPartyRole", source = "partyOrPartyRole",
             qualifiedByName = "toPartyOrPartyRoleDto")
    RelatedPartyOrPartyRole toDto(InternalRelatedPartyOrPartyRole entity);

    @Mapping(target = "partyOrPartyRole", source = "partyOrPartyRole",
             qualifiedByName = "toEntityPartyOrPartyRoleFVO")
    InternalRelatedPartyOrPartyRole toEntity(RelatedPartyOrPartyRoleFVO fvo);

    // ... other methods with qualifiedByName
}
```

3. **Fix Event Type Imports:**

```java
// WRONG - Using TMF event classes
import org.tmforum.openapi.dcmms.model.CustomerCreateEvent;

// CORRECT - Using Internal event classes
import com.pia.orbitant.dcmms.event.customer.InternalCustomerCreateEvent;
```

**Validation:**

- [ ] Entity has @Document with own collection → NO polymorphic dispatch
- [ ] Entity has @Document with parent collection → USE polymorphic dispatch
- [ ] Nested polymorphic fields → CREATE wrapper mapper
- [ ] Wrapper mapper uses qualifiedByName for all polymorphic fields
- [ ] Event imports use Internal* classes, not TMF classes
- [ ] Build succeeds with zero compilation errors

**Important Note on Audit Fields:**

Sub-entity mappers (like PartyOrPartyRoleMapper) do NOT need audit field ignore mappings because:
- They map to value objects (implement Serializable, NOT extend TenantEntity)
- They have NO `@Document` annotation (not persisted)
- They have NO audit fields (`revision`, `createdDate`, etc.)
- Only persisted entity mappers (extend BaseAppMapper, entity has @Document) need audit field ignores

**Reference:**
- `nested-polymorphic-field-mapper-pattern.md` for wrapper mapper details
- `polymorphic-mapper-pattern.md` for true polymorphic entities
- api-generator.md Phase 3.6 for audit field patterns

### Issue 5: Missing Event Mapper for Managed Entity

**Severity:** MEDIUM - Breaks event publishing

**Symptoms:**
```
[ERROR] cannot find symbol: class CustomerEventMapper
```

**Root Cause:**

Service gateway references event mapper that wasn't generated because OAS missing event definitions.

**Detection:**

```bash
grep -c "CustomerCreateEvent:" {OAS-file}
```

**If count = 0:** No event definitions in OAS.

**Fix Option 1:** Add event definitions to OAS

Required events for each managed entity:
- `{Entity}CreateEvent`
- `{Entity}AttributeValueChangeEvent`
- `{Entity}StateChangeEvent`
- `{Entity}DeleteEvent`

**Fix Option 2:** Remove event publishing

If events not needed, remove from service gateway:

```java
// BEFORE
public class CustomerServiceGatewayImpl implements CustomerServiceGateway {
    private final CustomerEventMapper eventMapper;  // REMOVE

    public CustomerServiceGatewayImpl(CustomerService service, CustomerEventMapper eventMapper) {
        this.eventMapper = eventMapper;  // REMOVE
    }
}

// AFTER
public class CustomerServiceGatewayImpl implements CustomerServiceGateway {

    public CustomerServiceGatewayImpl(CustomerService service) {
    }
}
```

**Validation:**
- [ ] Event schemas match configuration `managed_entities`
- [ ] All 4 event types present if any
- [ ] Event mapper generated if events present
- [ ] No event mapper references if events absent

## Build Issues

### Issue 6: Model Not Found in API Build

**Symptoms:**
```
[ERROR] Failed to execute goal on project dcmms: Could not resolve dependencies
[ERROR] The following artifacts could not be resolved: org.tmforum.openapi.dcmms:dcmms-model:jar:5.0.1
```

**Root Cause:**

Model project not installed to local Maven repository.

**Fix:**

```bash
cd {project}-model
mvn clean install

# Verify installation
ls ~/.m2/repository/org/tmforum/openapi/{project}/{project}-model/{version}/
```

**Expected output:**
```
dcmms-model-5.0.1.jar
dcmms-model-5.0.1.pom
```

**Validation:**
- [ ] Model build succeeds
- [ ] JAR in ~/.m2/repository
- [ ] Version matches API pom.xml dependency

### Issue 7: MapStruct Processor Not Configured

**Symptoms:**
```
[ERROR] No implementation was created for CustomerMapper
```

**Root Cause:**

MapStruct annotation processor not in compiler configuration.

**Fix:**

Add to API pom.xml:

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

**Validation:**
- [ ] Implementation classes in `target/generated-sources/annotations/`
- [ ] File: `{Entity}MapperImpl.java` exists
- [ ] Build succeeds without "no implementation" errors

### Issue 8: Wrong @Document on Child Entity

**Symptoms:**
```
[WARNING] Multiple @Document annotations on inheritance hierarchy
```

Or: Data duplicated across collections

**Root Cause:**

Child entity has `@Document` annotation in Single Table Inheritance pattern.

**Fix:**

```java
// WRONG
@Document(collection = "trouble_ticket")
public class InternalTroubleTicket extends TenantEntity { }

@Document(collection = "trouble_ticket")  // WRONG - duplicates parent!
public class InternalBackOfficeUserTicket extends InternalTroubleTicket { }

// CORRECT
@Document(collection = "trouble_ticket")
public class InternalTroubleTicket extends TenantEntity { }

// NO @Document - inherits from parent
public class InternalBackOfficeUserTicket extends InternalTroubleTicket { }
```

**Validation:**
- [ ] Check storage pattern (mongodb-storage-patterns.md)
- [ ] Single Table: parent has @Document, children don't
- [ ] Table Per Class: each type has @Document
- [ ] MongoDB query returns expected documents

## Configuration Issues

### Issue 9: Incorrect Collection Naming

**Symptoms:**

MongoDB collection created as `troubleTicket` (camelCase) instead of `trouble_ticket` (snake_case).

**Root Cause:**

Collection name not following convention.

**Fix:**

```java
// WRONG
@Document(collection = "troubleTicket")

// CORRECT
@Document(collection = "trouble_ticket")
```

**Rule:** Singular, snake_case

**Examples:**
- Customer → `customer`
- TroubleTicket → `trouble_ticket`
- BillingAccount → `billing_account`

**Validation:**
- [ ] All collections singular
- [ ] All collections snake_case
- [ ] Matches configuration database_name prefix

## Dependency Resolution

### Issue 10: Cannot Find BaseAppMapper

**Symptoms:**
```
[ERROR] cannot find symbol: class BaseAppMapper
```

**Root Cause:**

Missing dependency on common library providing base classes.

**Fix:**

1. Check which library provides BaseAppMapper:
```bash
# Check common-mongo pom.xml
grep -A5 "<dependency>" ~/.m2/repository/com/pia/orbitant/common-mongo/3.12.0-ca/common-mongo-3.12.0-ca.pom
```

2. Add to API pom.xml with explicit version:
```xml
<dependency>
    <groupId>com.pia.orbitant</groupId>
    <artifactId>common-mongo</artifactId>
    <version>${common-mongo.version}</version>
</dependency>
```

**Discovery Pattern:**

Need to know what library provides `BaseAppMapper`?

1. Check `common-mongo/docs/exported-services.md`
2. If not there, check `common-mongo/pom.xml` dependencies
3. Recurse to dependent libraries if needed

**Validation:**
- [ ] common-mongo dependency present with explicit version
- [ ] jackson-databind-nullable dependency present
- [ ] No BOM in dependencyManagement
- [ ] Build succeeds

### Issue 11: Version Conflict

**Symptoms:**
```
[WARNING] Version conflict for org.springframework.boot
```

**Root Cause:**

Inconsistent versions or missing version properties.

**Fix:**

```xml
<!-- WRONG - Using BOM -->
<dependencyManagement>
    <dependency>
        <groupId>com.pia.orbitant</groupId>
        <artifactId>dnext-common-dependencies</artifactId>
        <version>3.12.0-ca</version>
        <type>pom</type>
        <scope>import</scope>
    </dependency>
</dependencyManagement>

<!-- CORRECT - Direct version management -->
<properties>
    <common-mongo.version>3.12.0-ca</common-mongo.version>
</properties>

<dependencies>
    <dependency>
        <groupId>com.pia.orbitant</groupId>
        <artifactId>common-mongo</artifactId>
        <version>${common-mongo.version}</version>
    </dependency>
</dependencies>
```

**Validation:**
- [ ] Version properties defined
- [ ] All dependencies have explicit versions
- [ ] No BOM in dependencyManagement
- [ ] `mvn dependency:tree` shows consistent versions

## Quick Reference

### Diagnostic Commands

```bash
# Check for discriminator
grep -c "discriminator:" {OAS-file}

# Check for AclRelatedParty
grep -c "AclRelatedParty:" {OAS-file}

# Check for event definitions
grep -c "{Entity}CreateEvent:" {OAS-file}

# Verify model installation
ls ~/.m2/repository/{group-path}/{artifact-id}/{version}/

# Check generated mappers
ls target/generated-sources/annotations/{package}/mapper/

# Validate build
mvn clean compile
```

### Build Success Criteria

- [ ] Model: `mvn clean compile` → BUILD SUCCESS
- [ ] Model: `mvn install` → JAR in ~/.m2/repository
- [ ] API: `mvn clean compile` → BUILD SUCCESS
- [ ] Zero compilation errors
- [ ] Only expected MapStruct unmapped property warnings
- [ ] Generated sources in target/generated-sources/

### Critical Validations

- [ ] Polymorphic dispatch if discriminator present
- [ ] AclOwnershipUtil matches OAS schema
- [ ] No field shadowing in inheritance
- [ ] Event mapper matches event definitions
- [ ] Collection names singular, snake_case
- [ ] Direct version management (NO BOM)
- [ ] jackson-databind-nullable dependency present
