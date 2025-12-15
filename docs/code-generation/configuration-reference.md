# Code Generation Configuration Reference

## Configuration File Structure

**Format:** YAML
**Name:** `{project}-generation-config.yml` or `generator.yml`

## Complete Example

```yaml
name: DNext Code Generator
description: End-to-end code generator for TMF APIs
version: 0.0.1

project:
  name: dcmms                    # Project name (kebab-case)
  title: TMF 629 - Customer Management API
  version: 5.0.1
  description: The Customer Management API provides standardized customer management.
  managed_entities: [ Customer ] # Root entities from OAS to generate

  authorization:
    roles: dcmms                 # Authorization role name

  spring:
    version: 3.2.5               # Spring Boot version

  common:
    mongo:
      enabled: true              # Use MongoDB
      version: 3.12.0-ca         # DNext common version
    postgresql:
      enabled: false             # PostgreSQL not used
      version:

  devbox:                        # CI/CD configuration
    docker: true
    jenkins: true
    git: https://github.com/dnext-technology/dcmms
    image_url: dnext-technology/orbitant/dcmms/dcmms
    sonar_organization: dnext-technology
    sonar_project_key: dnext-technology_dcmms

  tmf:                           # TMF model configuration
    group_id: org.tmforum.openapi.dcmms
    artifact_id: dcmms-model
    version: 5.0.1
    package: org.tmforum.openapi.dcmms.model
    openapi_file: TMF629-Customer_Management-v5.0.1.oas.yaml
    url: https://github.com/dnext-technology/dcmms-model

  dnext:                         # DNext API configuration
    group_id: com.pia.orbitant
    artifact_id: dcmms
    version: 5.0.1
    package: com.pia.orbitant.dcmms
    context_path: api/customer/v5
    database_name: dcmms_v5
    module_name: DCMMS
    port: 8097                   # Developer responsible for uniqueness

    structure:                   # Package structure (optional - has defaults)
      api:
        name: api
        package: com.pia.orbitant.dcmms.api
        package_info: Entry point of the application
      service_gateway:
        name: service_gateway
        package: com.pia.orbitant.dcmms.servicegateway
        package_info: Communication between controller and service
      service:
        name: service
        package: com.pia.orbitant.dcmms.service
        package_info: Business logic
      entity:
        name: entity
        package: com.pia.orbitant.dcmms.entity
        package_info: Domain logic
        prefix: Internal             # Prefix for entities
      repository:
        name: repository
        package: com.pia.orbitant.dcmms.repository
        package_info: Database operations
      mapper:
        name: mapper
        package: com.pia.orbitant.dcmms.mapper
        package_info: DTO/Entity transformations
      event:
        name: event
        package: com.pia.orbitant.dcmms.event
        package_info: Event publishing
      validation:
        name: validation
        package: com.pia.orbitant.dcmms.validation
        package_info: Validation logic
      util:
        name: util
        package: com.pia.orbitant.dcmms.util
        package_info: Utilities
      security:
        name: security
        package: com.pia.orbitant.dcmms.security
        package_info: Security configuration

      default:
        validation:
          non_patchable_fields: ["id", "href", "atType", "@type",
                                 "atBaseType", "@baseType",
                                 "atSchemaLocation", "@schemaLocation",
                                 "creationDate"]
```

## Field Descriptions

### Project Metadata

| Field | Required | Description | Example |
|-------|----------|-------------|---------|
| `name` | Yes | Project identifier (kebab-case) | `dcmms` |
| `title` | Yes | Human-readable title | `TMF 629 - Customer Management API` |
| `version` | Yes | API version | `5.0.1` |
| `description` | Yes | Project description | Full sentence |
| `managed_entities` | Yes | List of root entities to generate | `[Customer, Product]` |

### Spring Configuration

| Field | Value | Description |
|-------|-------|-------------|
| `spring.version` | `3.2.5` | Spring Boot version (current) |

### Common Libraries

```yaml
common:
  mongo:
    enabled: true            # Use MongoDB
    version: 3.12.0-ca       # DNext common dependencies version
```

**Version:** Use direct version management (e.g., `3.12.0-ca`)

**IMPORTANT:** Do NOT use BOM (Bill of Materials) - use explicit version properties instead.

### TMF Model Configuration

| Field | Description | Example |
|-------|-------------|---------|
| `group_id` | Maven group for model | `org.tmforum.openapi.dcmms` |
| `artifact_id` | Model artifact name | `dcmms-model` |
| `version` | Model version (matches TMF) | `5.0.1` |
| `package` | Java package for model | `org.tmforum.openapi.dcmms.model` |
| `openapi_file` | OAS filename | `TMF629-Customer_Management-v5.0.1.oas.yaml` |
| `url` | Model repository URL | GitHub URL |

### DNext API Configuration

| Field | Required | Description | Example |
|-------|----------|-------------|---------|
| `group_id` | Yes | Maven group for API | `com.pia.orbitant` |
| `artifact_id` | Yes | API artifact name | `dcmms` |
| `package` | Yes | Base Java package | `com.pia.orbitant.dcmms` |
| `context_path` | Yes | API context path | `api/customer/v5` |
| `database_name` | Yes | MongoDB database | `dcmms_v5` |
| `module_name` | Yes | Module code (logging) | `DCMMS` |
| `port` | Yes | Server port | `8097` |

**Note:** Developer is responsible for ensuring port uniqueness.

### Package Structure (Optional)

Default structure is used if not specified. Override only if custom structure needed.

Each package definition:
```yaml
{layer}:
  name: {layer}
  package: {full.package.name}
  package_info: Description
  prefix: Internal           # For entities only
```

## Maven Dependency Configuration

### Model Project pom.xml

```xml
<groupId>${project.tmf.group_id}</groupId>
<artifactId>${project.tmf.artifact_id}</artifactId>
<version>${project.tmf.version}</version>
```

### API Project pom.xml

**CRITICAL:** Use direct version management with explicit properties (NOT BOM)

**Properties:**
```xml
<properties>
    <java.version>17</java.version>
    <common-mongo.version>3.12.0-ca</common-mongo.version>
    <mapstruct.version>1.5.5.Final</mapstruct.version>
    <lombok.version>1.18.30</lombok.version>
</properties>
```

**Dependencies:**
```xml
<dependencies>
    <!-- DNext Common -->
    <dependency>
        <groupId>com.pia.orbitant</groupId>
        <artifactId>common-mongo</artifactId>
        <version>${common-mongo.version}</version>
    </dependency>

    <!-- TMF Model -->
    <dependency>
        <groupId>${project.tmf.group_id}</groupId>
        <artifactId>${project.tmf.artifact_id}</artifactId>
        <version>${project.tmf.version}</version>
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
</dependencies>
```

**Version Notes:**
- `common-mongo 3.12.0-ca` provides `common-core 3.13.0-ca` with 8-parameter BaseAppServiceGatewayImpl
- `jackson-databind-nullable` is **REQUIRED** for event classes to compile
- Do NOT use BOM (Bill of Materials) for version management

**Discovery:** To find what interfaces a DNext common library provides, check that library's `docs/exported-services.md`. If not there, check the library's pom.xml and recurse.

**Example:** "Need to know what BaseAppMapper provides? Check `common-mongo/docs/exported-services.md`. If it's not there, check `common-mongo/pom.xml` for dependencies and recurse."

## Application Configuration

### application.yml

```yaml
spring:
  application:
    name: ${project.name}
  data:
    mongodb:
      uri: mongodb://localhost:27017/${project.dnext.database_name}

server:
  port: ${project.dnext.port}
  servlet:
    context-path: /${project.dnext.context_path}

application:
  version: @project.version@
  module: ${project.dnext.module_name}
```

## Naming Conventions

### Collection Names

Entity: `Customer` → Collection: `customer`
Entity: `TroubleTicket` → Collection: `trouble_ticket`

**Rule:** Singular, snake_case

### Package Names

Base package: `com.pia.orbitant.{module}`
Layer packages: `{base}.{layer}` (e.g., `com.pia.orbitant.dcmms.api`)

### Artifact Naming

Model: `{module}-model` (e.g., `dcmms-model`)
API: `{module}` (e.g., `dcmms`)

## Validation

After configuration:

- [ ] All required fields present
- [ ] `managed_entities` match OAS schemas
- [ ] Package names follow convention
- [ ] Port number provided (developer ensures uniqueness)
- [ ] Database name unique
- [ ] OAS file exists and accessible
- [ ] Spring Boot version compatible (3.2.5)
- [ ] DNext common version correct (3.12.0-ca)

## Examples

See: `~/.claude/references/code-generation/examples/`
- `trouble-ticket/generator.yml` - Polymorphic entity config
- `customer/generator.yml` - Simple entity config
