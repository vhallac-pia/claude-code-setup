# Internal Entity Generation Guide

## Overview

Internal entities are MongoDB-persisted domain objects prefixed with `Internal` that map to TMF API DTO types.

## Entity Transformation Rules

| TMF Type | Internal Type | Notes |
|----------|---------------|-------|
| `Customer` | `InternalCustomer extends TenantEntity` | Managed entity |
| `CustomerRef` | `InternalCustomerRef` | Embedded object |
| `Status` enum | `InternalStatus` enum | Copy values exactly |
| `List<Address>` | `List<InternalAddress>` | Nested collections |
| `String`, primitives | Keep as-is | No prefix |
| `OffsetDateTime` | Keep as-is | Java 8 date/time |
| `Map<String, Object>` | Keep as-is | Dynamic fields |

## Managed Entity Template

```java
package com.pia.orbitant.{module}.entity;

import com.pia.orbitant.common.mongo.entity.base.TenantEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

@Document(collection = "{collection_name}")
public class Internal{Entity} extends TenantEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Field("href")
    private String href;

    @Field("at_type")
    private String atType;

    @Field("at_base_type")
    private String atBaseType;

    @Field("at_schema_location")
    private String atSchemaLocation;

    // Entity fields from OAS...
    @Field("name")
    @NotBlank
    private String name;

    @Field("status")
    private String status;

    @Field("related_party")
    private List<InternalRelatedParty> relatedParty;

    // Getters and setters...
    // equals(), hashCode(), toString()...
}
```

## Field Name Conversions

### TMF → Java → MongoDB

| TMF (JSON) | Java Field | MongoDB Field | @Field Annotation |
|------------|------------|---------------|-------------------|
| `@type` | `atType` | `at_type` | `@Field("at_type")` |
| `@baseType` | `atBaseType` | `at_base_type` | `@Field("at_base_type")` |
| `@schemaLocation` | `atSchemaLocation` | `at_schema_location` | `@Field("at_schema_location")` |
| `relatedParty` | `relatedParty` | `related_party` | `@Field("related_party")` |
| `kebab-case` | `camelCase` | `kebab_case` | `@Field("kebab_case")` |

**Rule:**
- Java: camelCase
- MongoDB: snake_case
- `@` prefix → `at` prefix

## MongoDB Storage Patterns

### Pattern 1: Single Table Inheritance

**Use when:** Discriminator + single endpoint (e.g., `/troubleTicket`)

**Parent Entity:**
```java
@Document(collection = "trouble_ticket")
public class InternalTroubleTicket extends TenantEntity implements Serializable {
    @Field(name = "at_type")
    private String atType;  // Discriminator field
    // ... base fields
}
```

**Child Entity:**
```java
// NO @Document annotation - inherits parent's collection
public class InternalBackOfficeUserTicket extends InternalTroubleTicket implements Serializable {
    // ... subclass-specific fields only
    @Field(name = "contact_medium")
    private List<InternalContactMedium> contactMedium;

    @Field(name = "bpm_platform_process_reference")
    private InternalBpmPlatformProcessReference bpmPlatformProcessReference;
}
```

**Storage:** All types in single `trouble_ticket` collection, discriminated by `at_type` field.

### Pattern 2: Table Per Concrete Class

**Use when:** Discriminator + separate endpoints (e.g., `/billingAccount`, `/partyAccount`)

**Each Type Gets Own Collection:**
```java
@Document(collection = "billing_account")
public class InternalBillingAccount extends TenantEntity implements Serializable {
    // ... fields
}

@Document(collection = "party_account")
public class InternalPartyAccount extends TenantEntity implements Serializable {
    // ... fields
}
```

**Storage:** Separate MongoDB collections, no shared parent entity.

## Detection Algorithm

```
Check OpenAPI spec for entity:
  Has discriminator property?
    YES →
      Check paths:
        Single endpoint (/troubleTicket)?
          YES → Single Table Inheritance
                ├─ Parent: @Document(collection)
                ├─ Children: NO @Document (inherit)
                └─ Discriminator: atType field
        Multiple endpoints (/billingAccount, /partyAccount)?
          YES → Table Per Concrete Class
                └─ Each type: @Document(collection)
    NO → Simple Entity
          └─ Single @Document class
```

## Critical Rules

### 1. No Field Shadowing

**WRONG:**
```java
public class InternalTroubleTicket extends TenantEntity {
    private List<InternalAclRelatedParty> aclRelatedParty;  // In parent
}

public class InternalBackOfficeUserTicket extends InternalTroubleTicket {
    private List<InternalAclRelatedParty> aclRelatedParty;  // DUPLICATE - shadows parent!
}
```

**CORRECT:**
```java
public class InternalTroubleTicket extends TenantEntity {
    private List<InternalAclRelatedParty> aclRelatedParty;  // Only in parent
}

public class InternalBackOfficeUserTicket extends InternalTroubleTicket {
    // Field inherited, not redeclared
}
```

### 2. Conditional Schema Dependencies

**AclRelatedParty Field:**
```bash
# Check if schema exists in OAS
grep -c "AclRelatedParty:" TMF629-Customer_Management-v5.0.1.oas.yaml

# If count > 0: Include field
@Field("acl_related_party")
private List<InternalAclRelatedParty> aclRelatedParty;

# If count = 0: DO NOT include field
```

### 3. Collection Naming Convention

From configuration file:
```yaml
project:
  dnext:
    database_name: dcmms_v5
    # Collection names: singular, snake_case
```

**Mapping:**
- Entity: `Customer` → Collection: `customer`
- Entity: `TroubleTicket` → Collection: `trouble_ticket`
- Entity: `BillingAccount` → Collection: `billing_account`

## Embedded Entity Template

**Non-managed entities** (referenced objects, no CRUD operations):

```java
package com.pia.orbitant.{module}.entity;

import java.io.Serializable;

public class Internal{EntityName} implements Serializable {

    private static final long serialVersionUID = 1L;

    // Fields from OAS
    private String id;
    private String name;
    private String role;

    // Getters and setters...
}
```

**Note:** No `@Document` annotation, no `extends TenantEntity`.

## Enum Template

```java
package com.pia.orbitant.{module}.entity;

public enum Internal{EnumName} {
    OPEN("open"),
    IN_PROGRESS("inProgress"),
    CLOSED("closed");

    private final String value;

    Internal{EnumName}(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Internal{EnumName} fromValue(String value) {
        for (Internal{EnumName} e : values()) {
            if (e.value.equals(value)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Unknown enum value: " + value);
    }
}
```

**Match external enum values exactly.**

## Validation Checklist

After entity generation:

- [ ] All managed entities have `Internal` prefix
- [ ] Extends `TenantEntity` for managed entities
- [ ] `@Document(collection = "...")` on parent or standalone entities
- [ ] NO `@Document` on child entities (Single Table Inheritance)
- [ ] `@Field("...")` annotations use snake_case
- [ ] `@type` → `atType` conversion correct
- [ ] No field shadowing in inheritance
- [ ] AclRelatedParty only if in OAS
- [ ] Embedded entities do NOT extend TenantEntity
- [ ] Enums match external values exactly
- [ ] serialVersionUID = 1L present

## Common Issues

### Issue: Field Shadowing Warning

**Problem:** Same field in parent and child
**Fix:** Remove from child, keep only in appropriate class per OAS schema

### Issue: Missing AclRelatedParty Type

**Problem:** Field declared but type not generated
**Fix:** Check OAS - if schema absent, remove field

### Issue: Wrong Collection Name

**Problem:** Camel case used: `troubleTicket`
**Fix:** Use snake_case: `trouble_ticket`

## Reference Examples

See: `~/.claude/references/code-generation/examples/`
- `trouble-ticket/InternalTroubleTicket.java` - Polymorphic parent
- `trouble-ticket/InternalBackOfficeUserTicket.java` - Polymorphic child
- `customer/InternalCustomer.java` - Simple entity
