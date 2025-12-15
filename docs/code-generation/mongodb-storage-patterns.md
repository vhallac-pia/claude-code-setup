# MongoDB Storage Patterns for Polymorphic Types

## Pattern Selection Decision Tree

```
Does entity have discriminator in OAS?
  NO → Simple Entity (single @Document class)
  YES →
    Check API endpoints:
      Single endpoint (e.g., /troubleTicket)?
        → Single Table Inheritance
      Multiple endpoints (e.g., /billingAccount, /partyAccount)?
        → Table Per Concrete Class
```

## Pattern 1: Single Table Inheritance

**When:** Discriminator + single polymorphic endpoint

**Example:** TMF621 TroubleTicket API
- Endpoint: `/troubleTicket` (single)
- Types: TroubleTicket, BackOfficeUserTicket
- Discriminator: `@type`

### Entity Structure

**Parent Entity:**
```java
@Document(collection = "trouble_ticket")
public class InternalTroubleTicket extends TenantEntity implements Serializable {

    @Field(name = "at_type")
    private String atType;  // Discriminator field

    // Base fields shared by all types
    @Field(name = "id")
    private String id;

    @Field(name = "name")
    private String name;

    @Field(name = "status")
    private String status;

    // ... other base fields
}
```

**Child Entity:**
```java
// NO @Document annotation - inherits parent's collection
public class InternalBackOfficeUserTicket extends InternalTroubleTicket implements Serializable {

    // Only subclass-specific fields
    @Field(name = "contact_medium")
    private List<InternalContactMedium> contactMedium;

    @Field(name = "bpm_platform_process_reference")
    private InternalBpmPlatformProcessReference bpmPlatformProcessReference;

    @Field(name = "candidate_roles")
    private List<String> candidateRoles;

    // ... other subclass fields
}
```

### MongoDB Storage

**Single Collection:** `trouble_ticket`

**Documents:**
```json
// Base type document
{
  "_id": "TICKET-001",
  "at_type": "TroubleTicket",
  "name": "Generic Issue",
  "status": "open"
}

// Subclass document (same collection)
{
  "_id": "TICKET-002",
  "at_type": "BackOfficeUserTicket",
  "name": "Network Outage",
  "status": "critical",
  "contact_medium": [...],
  "bpm_platform_process_reference": {...},
  "candidate_roles": ["NETWORK_ADMIN"]
}
```

**Query behavior:**
- Query returns all types from same collection
- Application filters by `at_type` discriminator
- Polymorphic mapper routes to correct subtype

### Mapper Requirement

**MUST use polymorphic dispatch** (see polymorphic-mapper-pattern.md)

Switch-based dispatch required to handle different types from same collection.

## Pattern 2: Table Per Concrete Class

**When:** Discriminator + separate endpoints per type

**Example:** TMF666 Account Management API
- Endpoints: `/billingAccount`, `/partyAccount`, `/settlementAccount` (separate)
- Types: BillingAccount, PartyAccount, SettlementAccount
- Discriminator: `@type` (but separate endpoints)

### Entity Structure

**Each Type = Separate Entity:**

```java
@Document(collection = "billing_account")
public class InternalBillingAccount extends TenantEntity implements Serializable {
    // All billing account fields
}

@Document(collection = "party_account")
public class InternalPartyAccount extends TenantEntity implements Serializable {
    // All party account fields
}

@Document(collection = "settlement_account")
public class InternalSettlementAccount extends TenantEntity implements Serializable {
    // All settlement account fields
}
```

**NO shared parent entity** (or parent is abstract/not persisted).

### MongoDB Storage

**Separate Collections:**
- `billing_account` collection
- `party_account` collection
- `settlement_account` collection

**Documents:**
```json
// billing_account collection
{
  "_id": "ACC-001",
  "at_type": "BillingAccount",
  ...
}

// party_account collection
{
  "_id": "ACC-002",
  "at_type": "PartyAccount",
  ...
}
```

### Mapper Requirement

**Simple mappers** - NO polymorphic dispatch needed.

Each type has own endpoint, repository, service, and mapper.

## Pattern 3: Simple Entity (Non-Polymorphic)

**When:** No discriminator in OAS

**Example:** TMF629 Customer Management API
- Endpoint: `/customer`
- Type: Customer (single)
- No discriminator

### Entity Structure

```java
@Document(collection = "customer")
public class InternalCustomer extends TenantEntity implements Serializable {
    // All fields
}
```

### Mapper Requirement

**Simple mapper** - extends BaseAppMapper directly.

## Detection Algorithm

### Step 1: Check for Discriminator

```bash
grep -c "discriminator:" TMF621-Trouble_Ticket-v5.0.0.oas.yaml
```

- Count > 0 → Has discriminator, continue to Step 2
- Count = 0 → Simple entity, use Pattern 3

### Step 2: Analyze Endpoints

```yaml
paths:
  /troubleTicket:        # Single endpoint → Pattern 1
    get:
    post:
```

vs.

```yaml
paths:
  /billingAccount:       # Multiple endpoints → Pattern 2
    get:
    post:
  /partyAccount:
    get:
    post:
```

### Step 3: Pattern Selection

```
If discriminator:
  If single endpoint for all types:
    → Pattern 1: Single Table Inheritance
       - Parent with @Document
       - Children without @Document
       - Polymorphic mapper with switch dispatch
  If separate endpoint per type:
    → Pattern 2: Table Per Concrete Class
       - Each type with @Document
       - Separate collections
       - Simple mappers
Else:
  → Pattern 3: Simple Entity
     - Single @Document class
     - Simple mapper
```

## Implementation Checklist

### For Single Table Inheritance:

- [ ] Parent has `@Document(collection = "...")`
- [ ] Parent has discriminator field (`atType`)
- [ ] Children do NOT have `@Document`
- [ ] Children extend parent
- [ ] Children have only subclass-specific fields
- [ ] NO field shadowing (same field in parent and child)
- [ ] Mapper has polymorphic dispatch (4 switch methods)
- [ ] Mapper has named methods for each type

### For Table Per Concrete Class:

- [ ] Each type has `@Document(collection = "...")`
- [ ] Separate collections for each type
- [ ] Each type can be independent or share interface
- [ ] Each type has own repository, service, mapper
- [ ] Simple mappers (no polymorphic dispatch)

### For Simple Entity:

- [ ] Single `@Document(collection = "...")`
- [ ] No inheritance
- [ ] Simple mapper

## Common Mistakes

### ❌ Child with @Document in Single Table

```java
// WRONG
@Document(collection = "trouble_ticket")
public class InternalTroubleTicket extends TenantEntity { }

@Document(collection = "trouble_ticket")  // WRONG - duplicates parent!
public class InternalBackOfficeUserTicket extends InternalTroubleTicket { }
```

**Fix:** Remove `@Document` from child - it inherits from parent.

### ❌ Missing Discriminator Field

```java
// WRONG - Single Table Inheritance without discriminator
@Document(collection = "trouble_ticket")
public class InternalTroubleTicket extends TenantEntity {
    // Missing: private String atType;
}
```

**Fix:** Add discriminator field matching OAS `propertyName`.

### ❌ Wrong Pattern for Endpoints

```java
// API has separate /billingAccount and /partyAccount endpoints
// But generated as Single Table Inheritance (shared parent)
// WRONG - Should be Table Per Concrete Class
```

**Fix:** Check endpoints in OAS - separate endpoints = separate entities.

## MongoDB Query Implications

### Single Table Inheritance

**Queries must filter by discriminator:**
```java
// Find all BackOfficeUserTickets
mongoTemplate.find(
    Query.query(Criteria.where("at_type").is("BackOfficeUserTicket")),
    InternalBackOfficeUserTicket.class,
    "trouble_ticket"
);
```

### Table Per Concrete Class

**Queries are collection-specific:**
```java
// Find all billing accounts
mongoTemplate.find(
    Query.query(...),
    InternalBillingAccount.class,
    "billing_account"  // Separate collection
);
```

## Reference

See examples in: `~/.claude/references/code-generation/examples/`

- **Single Table:** trouble-ticket example
- **Simple:** customer example
