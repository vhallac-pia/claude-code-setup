# Code Generation Validation Checklist

## Build Validation

### Phase 1: Model Build

```bash
cd {project}-model
mvn clean compile
```

**Success Criteria:**
- [ ] `BUILD SUCCESS` message
- [ ] No compilation errors
- [ ] Generated model classes in `target/generated-sources/`
- [ ] API interfaces generated

**Install to Local Maven:**
```bash
mvn install
```

- [ ] Installed to `~/.m2/repository/`
- [ ] Available for API project dependency

### Phase 2: API Build

```bash
cd {project}-api
mvn clean compile
```

**Success Criteria:**
- [ ] `BUILD SUCCESS` message
- [ ] Zero compilation errors
- [ ] MapStruct implementations generated in `target/generated-sources/annotations/`
- [ ] All layers compiled

**Expected Warnings (OK):**
```
Warning: Unmapped target property: "someField"
```

MapStruct warnings about unmapped properties are expected for framework-managed fields.

**Unacceptable Errors:**
- Missing class/interface
- Type mismatch
- Method not found
- Cannot resolve symbol

## Code Structure Validation

### Generated Files Checklist

**Model Repository:**
- [ ] `{project}-model/pom.xml` exists
- [ ] OAS file copied to model directory
- [ ] Model classes in `src/main/java/{tmf.package}/model/`
- [ ] API interfaces in `src/main/java/{tmf.package}/api/`

**API Repository:**
- [ ] `{project}-api/pom.xml` exists
- [ ] `{Project}Application.java` main class exists
- [ ] `application.yml` configured correctly

**For Each Managed Entity:**
- [ ] `Internal{Entity}.java` in `entity/` package
- [ ] `{Entity}Mapper.java` in `mapper/` package
- [ ] `{Entity}Repository.java` in `repository/` package
- [ ] `{Entity}Service.java` interface in `service/` package
- [ ] `{Entity}ServiceImpl.java` impl in `service/` package
- [ ] `{Entity}ServiceGateway.java` interface in `servicegateway/` package
- [ ] `{Entity}ServiceGatewayImpl.java` impl in `servicegateway/` package
- [ ] `{Entity}ApiImpl.java` in `api/` package

**Event Framework (Required for Each Managed Entity):**
- [ ] `{Entity}EventMapper.java` in `event/` package
- [ ] `event/{entity}/Internal{Entity}CreateEvent.java`
- [ ] `event/{entity}/Internal{Entity}CreateEventPayload.java`
- [ ] `event/{entity}/Internal{Entity}DeleteEvent.java`
- [ ] `event/{entity}/Internal{Entity}DeleteEventPayload.java`
- [ ] `event/{entity}/Internal{Entity}AttributeValueChangeEvent.java`
- [ ] `event/{entity}/Internal{Entity}AttributeValueChangeEventPayload.java`
- [ ] `event/{entity}/Internal{Entity}StateChangeEvent.java`
- [ ] `event/{entity}/Internal{Entity}StateChangeEventPayload.java`

**Total:** 8 event files + 1 event mapper per entity

## Polymorphic Type Validation

**If entity has discriminator:**

### Entity Classes
- [ ] Parent has `@Document(collection = "...")`
- [ ] Parent has discriminator field (`atType`)
- [ ] Children do NOT have `@Document` (Single Table Inheritance)
- [ ] Children extend parent correctly
- [ ] NO field shadowing (same field in parent and child)

### Mapper Validation
- [ ] Mapper has 4 `@Override` dispatch methods
- [ ] Each dispatch method has `switch` expression
- [ ] Switch on discriminator field (`getAtType()`)
- [ ] Explicit type casts in switch cases: `(InternalSubType) entity`
- [ ] Default case present
- [ ] Named methods exist for each concrete type (`@Named("toXxxDto")`)
- [ ] Subclass named methods include subclass-specific `@Mapping` annotations

**Compare with live code:**
```bash
# If available
diff {project}-api/src/.../mapper/{Entity}Mapper.java \
     temp/work/{reference-project}/src/.../mapper/{Entity}Mapper.java
```

## Conditional Logic Validation

### AclRelatedParty Check

```bash
grep -c "AclRelatedParty:" {OAS-file}
```

**If count > 0:**
- [ ] `aclRelatedParty` field in entity
- [ ] `AclOwnershipUtil` in service dependencies
- [ ] `aclOwnershipUtil.addDefaultEntityOwner()` called in `create()`

**If count = 0:**
- [ ] NO `aclRelatedParty` field in entity
- [ ] NO `AclOwnershipUtil` in service
- [ ] No references to `aclOwnershipUtil` in code

### Event Definitions Check

**For each managed entity:**
```bash
grep -c "{Entity}CreateEvent:" {OAS-file}
```

- [ ] All 4 event types present in OAS
- [ ] Event mapper generated
- [ ] Service gateway includes event mapper

**If events missing:**
- [ ] Entity removed from `managed_entities` configuration
- [ ] OR: Event mapper generation skipped (acceptable)

## Package Structure Validation

```
{base-package}/
├── {Project}Application.java
├── api/
│   └── {Entity}ApiImpl.java
├── servicegateway/
│   ├── {Entity}ServiceGateway.java
│   └── {Entity}ServiceGatewayImpl.java
├── service/
│   ├── {Entity}Service.java
│   └── {Entity}ServiceImpl.java
├── entity/
│   ├── Internal{Entity}.java
│   └── Internal{ChildEntity}.java (if polymorphic)
├── mapper/
│   └── {Entity}Mapper.java
├── repository/
│   └── {Entity}Repository.java
├── event/
│   ├── {Entity}EventMapper.java
│   └── {entity}/
│       ├── Internal{Entity}CreateEvent.java
│       ├── Internal{Entity}CreateEventPayload.java
│       ├── Internal{Entity}DeleteEvent.java
│       ├── Internal{Entity}DeleteEventPayload.java
│       ├── Internal{Entity}AttributeValueChangeEvent.java
│       ├── Internal{Entity}AttributeValueChangeEventPayload.java
│       ├── Internal{Entity}StateChangeEvent.java
│       └── Internal{Entity}StateChangeEventPayload.java
├── validation/
│   └── (validators)
└── util/
    └── (utilities)
```

## Dependency Validation

### pom.xml Dependencies

**BOM Present:**
```xml
<dependencyManagement>
    <dependency>
        <groupId>com.pia.orbitant</groupId>
        <artifactId>dnext-common-dependencies</artifactId>
        <version>3.12.0-ca</version>
        <type>pom</type>
        <scope>import</scope>
    </dependency>
</dependencyManagement>
```

**Common Libraries (versions managed by BOM):**
- [ ] `common-mongo` dependency
- [ ] `business-validator` dependency (if validation needed)

**Model Dependency:**
- [ ] `{tmf.artifact_id}` dependency with correct version

**MapStruct:**
- [ ] `mapstruct` dependency version 1.5.5.Final
- [ ] `mapstruct-processor` in annotation processor paths

## Configuration Validation

### application.yml

- [ ] `spring.application.name` set
- [ ] `spring.data.mongodb.uri` configured
- [ ] `server.port` matches configuration
- [ ] `server.servlet.context-path` matches configuration

### Collection Names

- [ ] Collection names are singular, snake_case
- [ ] Match configuration database_name prefix

## Integration Validation (Optional)

### Runtime Tests

**If MongoDB available:**
```bash
# Start application
mvn spring-boot:run

# Check health
curl http://localhost:{port}/actuator/health
```

**Test CRUD operations:**
```bash
# Create
curl -X POST http://localhost:{port}/{context-path}/{entity} \
  -H "Content-Type: application/json" \
  -d '{"name":"Test"}'

# Read
curl http://localhost:{port}/{context-path}/{entity}/{id}

# List
curl http://localhost:{port}/{context-path}/{entity}
```

## Comparison with Reference Examples

**Compare generated code with reference examples (trouble-ticket, customer):**

### Mapper Comparison
- [ ] Line count similar to reference example (±20% acceptable)
- [ ] Polymorphic dispatch present (if applicable)
- [ ] Same named method patterns
- [ ] Same mapping annotation patterns

### Entity Comparison
- [ ] Field naming follows reference pattern
- [ ] Collection annotations follow reference pattern
- [ ] No extra/missing fields (except AclRelatedParty conditional)

### Service Comparison
- [ ] Constructor dependencies match framework patterns
- [ ] AclOwnershipUtil handling follows reference pattern

## Final Checklist

**Before declaring success:**

- [ ] Model: `mvn clean compile` → SUCCESS
- [ ] API: `mvn clean compile` → SUCCESS
- [ ] Zero compilation errors
- [ ] Only expected MapStruct warnings
- [ ] All managed entities fully generated
- [ ] Polymorphic dispatch correct (if applicable)
- [ ] No field shadowing warnings
- [ ] AclOwnershipUtil handled correctly
- [ ] Package structure matches conventions
- [ ] Configuration files present and valid
- [ ] Comparison with reference examples shows expected patterns

## Common Issues

### Build Fails: Missing Class

**Check:**
- [ ] Model installed to local Maven?
- [ ] Dependency versions correct?
- [ ] Import statements correct?

### MapStruct Errors

**Check:**
- [ ] Annotation processor configured in pom.xml?
- [ ] Dependent mappers in `uses` clause?
- [ ] Method signatures correct?

### Polymorphic Mapping Data Loss

**Check:**
- [ ] Switch-based dispatch present?
- [ ] Named methods for each concrete type?
- [ ] Subclass-specific mappings present?

**Compare:** Generated vs. reference example mapper (trouble-ticket)
