# Package Structure Conventions

## Overview

This document defines the standard package structure for DNext TMF API code generation, following team conventions for separating interfaces from implementations.

## Directory Structure

### API Layer

```
com.pia.orbitant.{project}.api/
├── {Entity}Api.java (interface)
└── impl/
    └── {Entity}ApiImpl.java (implementation)
```

**Example:**
```
com.pia.orbitant.dcmms.api/
├── CustomerApi.java
└── impl/
    └── CustomerApiImpl.java
```

**Package Declaration:**
- Interface: `package com.pia.orbitant.{project}.api;`
- Implementation: `package com.pia.orbitant.{project}.api.impl;`

**Implementation Class Requirements:**
- Must import the interface: `import com.pia.orbitant.{project}.api.{Entity}Api;`
- Must implement the interface: `implements {Entity}Api`

### Service Layer

```
com.pia.orbitant.{project}.service/
├── {Entity}Service.java (interface)
└── impl/
    └── {Entity}ServiceImpl.java (implementation)
```

**Example:**
```
com.pia.orbitant.dcmms.service/
├── CustomerService.java
└── impl/
    └── CustomerServiceImpl.java
```

**Package Declaration:**
- Interface: `package com.pia.orbitant.{project}.service;`
- Implementation: `package com.pia.orbitant.{project}.service.impl;`

**Implementation Class Requirements:**
- Must import the interface: `import com.pia.orbitant.{project}.service.{Entity}Service;`
- Must implement the interface: `implements {Entity}Service`

### Service Gateway Layer

```
com.pia.orbitant.{project}.servicegateway/
├── {Entity}ServiceGateway.java (interface)
└── impl/
    └── {Entity}ServiceGatewayImpl.java (implementation)
```

**Example:**
```
com.pia.orbitant.dcmms.servicegateway/
├── CustomerServiceGateway.java
└── impl/
    └── CustomerServiceGatewayImpl.java
```

**Package Declaration:**
- Interface: `package com.pia.orbitant.{project}.servicegateway;`
- Implementation: `package com.pia.orbitant.{project}.servicegateway.impl;`

**Implementation Class Requirements:**
- Must import the interface: `import com.pia.orbitant.{project}.servicegateway.{Entity}ServiceGateway;`
- Must implement the interface: `implements {Entity}ServiceGateway`

## Other Layers (No impl/ Subdirectory)

The following layers do NOT use impl/ subdirectories:

- **entity/** - Internal entity classes
- **event/** - Event and event payload classes
- **mapper/** - MapStruct mapper interfaces
- **repository/** - Repository interfaces

## Rationale

**Benefits of impl/ Structure:**
1. **Clear Separation:** Visual distinction between contracts (interfaces) and implementations
2. **Package Organization:** Easier navigation in large projects
3. **Team Convention:** Matches existing team practices and expectations
4. **IDE Support:** Better IDE organization and filtering

**Why Not Other Layers:**
- **entity/**: Contains only concrete classes, no interfaces
- **event/**: Contains only concrete classes, no interfaces
- **mapper/**: MapStruct generates implementations automatically
- **repository/**: Spring Data generates implementations automatically

## Implementation Guidelines

### For Code Generator

1. **Directory Creation:**
   - Always create `impl/` subdirectories for api, service, and servicegateway layers
   - Create interface files in parent directory
   - Create implementation files in impl/ subdirectory

2. **Template Updates:**
   - Update `api_impl.java.j2` to use package `{base_package}.api.impl`
   - Update `service_impl.java.j2` to use package `{base_package}.service.impl`
   - Update `service_gateway_impl.java.j2` to use package `{base_package}.servicegateway.impl`

3. **Import Statements:**
   - Implementation files must import their interfaces
   - Example for CustomerApiImpl:
     ```java
     package com.pia.orbitant.dcmms.api.impl;

     import com.pia.orbitant.dcmms.api.CustomerApi;
     ```

## Validation Checklist

When generating code, verify:

- [ ] impl/ directories exist for api, service, and servicegateway
- [ ] Interface files are in parent package
- [ ] Implementation files are in impl/ package
- [ ] Implementation classes import their interfaces
- [ ] Implementation classes implement their interfaces
- [ ] Package declarations match directory structure
- [ ] No impl/ directories for entity, event, mapper, or repository layers
