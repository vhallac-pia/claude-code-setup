# Code Generation Examples

Reference implementations demonstrating DNext TMF API code generation patterns.

## Structure

### trouble-ticket/ - Polymorphic Entity Example

**API:** TMF621 Trouble Ticket Management v5.0.0

**Pattern:** Single Table Inheritance with polymorphic dispatch

**Key Files:**
- `TMF621-Trouble_Ticket-v5.0.0.oas.yaml` - OpenAPI specification with discriminator
- `entity/InternalTroubleTicket.java` - Parent entity with @Document annotation
- `entity/InternalBackOfficeUserTicket.java` - Child entity (NO @Document, inherits parent's collection)
- `mapper/TroubleTicketMapper.java` - Polymorphic mapper with switch-based dispatch

**Demonstrates:**
- Discriminator field (`@type` → `atType`)
- Single MongoDB collection (`trouble_ticket`) for all types
- Parent-child entity relationship
- No field shadowing (subclass-specific fields only in child)
- 4 dispatch methods (toDto, toMVO, toEntity, mvoToEntity)
- Named methods for each concrete type
- Explicit type casts in switch expressions

**Use Case:** When API has discriminator AND single endpoint serving multiple types (e.g., `/troubleTicket`)

### customer/ - Simple Entity Example

**API:** TMF629 Customer Management v5.0.1

**Pattern:** Simple entity (non-polymorphic)

**Key Files:**
- `TMF629-Customer_Management-v5.0.1.oas.yaml` - OpenAPI specification without discriminator
- `entity/InternalCustomer.java` - Standalone entity with @Document annotation
- `mapper/CustomerMapper.java` - Simple mapper extending BaseAppMapper

**Demonstrates:**
- Single entity class
- Single MongoDB collection (`customer`)
- Simple mapper without polymorphic dispatch
- Standard BaseAppMapper extension
- Field name conversions (@type → atType, relatedParty → related_party)

**Use Case:** When API has NO discriminator (standard CRUD entity)

## Usage

These examples serve as reference implementations when generating new TMF APIs:

1. **Check OAS for discriminator** to determine pattern
2. **Compare generated code** against appropriate example
3. **Validate key patterns** (collection annotations, mapper dispatch, field naming)

## Key Differences

| Aspect | Simple (Customer) | Polymorphic (TroubleTicket) |
|--------|-------------------|----------------------------|
| Discriminator | None | `@type` property |
| Entity Structure | Single class | Parent + Children |
| @Document | On single class | On parent only |
| Mapper Pattern | Direct mapping | Switch dispatch |
| MongoDB Storage | Single collection, one type | Single collection, multiple types |
| Complexity | Low | High |

## Validation

When generating code, verify:

**For Simple Entities:**
- [ ] Entity has `@Document(collection = "...")`
- [ ] Collection name is singular, snake_case
- [ ] Mapper extends BaseAppMapper
- [ ] No polymorphic dispatch methods

**For Polymorphic Entities:**
- [ ] Parent has `@Document`, children do not
- [ ] Parent has discriminator field (atType)
- [ ] No field shadowing between parent/child
- [ ] Mapper has 4 dispatch methods with switch
- [ ] Named methods for each concrete type
- [ ] Explicit type casts in switch cases

## Related Documentation

- Core Patterns: `~/.claude/docs/code-generation/`
  - `polymorphic-mapper-pattern.md` - Complete polymorphic mapping guide
  - `entity-generation-guide.md` - Entity creation rules
  - `mongodb-storage-patterns.md` - Storage pattern decision tree
  - `mapstruct-conventions.md` - MapStruct configuration
  - `validation-checklist.md` - Post-generation validation
  - `known-issues-fixes.md` - Common problems and solutions

- DNext Integration: `dnext-dev-support/references/code-generation/`
  - Framework-specific integration patterns
  - Common library usage
  - Deployment configurations
