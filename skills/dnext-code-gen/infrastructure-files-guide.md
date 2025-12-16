# Infrastructure Files Generation Guide

## Overview

Infrastructure files provide project-specific configuration, security, and utility classes that support the generated TMF API code. These files are generated using Jinja2 templates.

## Infrastructure File Categories

### 1. Configuration Files

**Template:** `templates/configuration.java.j2`

**Generated File:** `{base_package}/configuration/{Project}AppProperties.java`

**Purpose:** Spring Boot configuration properties placeholder

**Content:**
```java
package com.pia.orbitant.{project}.configuration;

import org.springframework.stereotype.Component;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * This package is responsible for handling the project specific configuration.
 */
@jakarta.annotation.Generated(value = "DNext API Generator")
@Component
@ConfigurationProperties(prefix = "{project}-app", ignoreUnknownFields = false)
public class {Project}AppProperties {}
```

**Key Features:**
- Empty class providing placeholder for future configuration properties
- Uses Spring's `@ConfigurationProperties` with project-specific prefix
- Follows naming convention: `{Project}AppProperties` (e.g., DcmmsAppProperties)

### 2. Security Files

**Template:** `templates/security.java.j2`

**Generated File:** `{base_package}/security/{Project}AppSecurity.java`

**Purpose:** Project-specific security logic placeholder

**Content:**
```java
package com.pia.orbitant.{project}.security;

import org.springframework.stereotype.Component;

/**
 * This package is responsible for handling the project specific security logic.
 */
@jakarta.annotation.Generated(value = "DNext API Generator")
@Component
public class {Project}AppSecurity {}
```

**Key Features:**
- Empty class providing placeholder for future security logic
- Spring-managed component via `@Component`
- Follows naming convention: `{Project}AppSecurity` (e.g., DcmmsAppSecurity)

### 3. Constants File

**Template:** `templates/constants.java.j2`

**Generated File:** `{base_package}/util/Constants.java`

**Purpose:** Constants for non-patchable attributes and common field names

**Content:**
```java
package com.pia.orbitant.{project}.util;

public class Constants {

    public static final String ID = "id";
    public static final String HREF = "href";
    public static final String atTYPE = "atType";
    public static final String TYPE = "@type";
    public static final String atBASE_TYPE = "atBaseType";
    public static final String BASE_TYPE = "@baseType";
    public static final String atSCHEMA_LOCATION = "atSchemaLocation";
    public static final String SCHEMA_LOCATION = "@schemaLocation";
    public static final String CREATION_DATE = "creationDate";

    private Constants() {
    }
}
```

**Key Features:**
- Defines TMF-standard non-patchable field names
- Includes both JSON (atType) and annotation (@type) formats
- Private constructor prevents instantiation
- Used by validation utilities

### 4. Base Validation Util

**Template:** `templates/base_validation_util.java.j2`

**Generated File:** `{base_package}/util/BaseValidationUtil.java`

**Purpose:** JSON Patch validation utilities for non-patchable attributes

**Content:**
```java
package com.pia.orbitant.{project}.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.pia.orbitant.common.exception.common.ExceptionFactory;
import com.pia.orbitant.common.exception.common.OrbitantException;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class BaseValidationUtil {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BaseValidationUtil.class);
    private static final String NAME_PATH = "name";
    private static final String PATH_PATTERN = "path: \"/%s\"";
    private static final Set<String> NON_PATCHABLE_ATTRIBUTES = new HashSet<>(Arrays.asList(
        Constants.ID,
        Constants.HREF,
        Constants.atTYPE,
        Constants.TYPE,
        Constants.atBASE_TYPE,
        Constants.BASE_TYPE,
        Constants.atSCHEMA_LOCATION,
        Constants.SCHEMA_LOCATION,
        Constants.CREATION_DATE
    ));
    private final ObjectMapper objectMapper;

    public BaseValidationUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void checkForNonPatchableAttributesInJsonPatch(JsonPatch jsonPatch) throws OrbitantException {
        String jsonPatchAsString = jsonPatch.toString();
        for (String nonPatchableField : NON_PATCHABLE_ATTRIBUTES) {
            String searchedStr = String.format(PATH_PATTERN, nonPatchableField);
            if (jsonPatchAsString.contains(searchedStr)) {
                throw ExceptionFactory.BadRequest.throwOtherBadRequestException("Request contains non patchable attributes " + nonPatchableField);
            }
        }
    }

    public void validateAtTypeFieldForJsonPatch(JsonPatch patch) throws OrbitantException {
        List<HashMap<String, String>> convertPatchToJsonNode = objectMapper.convertValue(patch, List.class);
        for (HashMap<String, String> stringStringHashMap : convertPatchToJsonNode) {
            String operationValue = stringStringHashMap.get("op");
            String pathValue = stringStringHashMap.get("path");
            pathValue = pathValue.replaceFirst("/", "");
            if (operationValue.equals("remove") && (pathValue.contains("atType") || pathValue.contains("@type"))) {
                throw ExceptionFactory.BadRequest.throwOtherBadRequestException("Field @type cannot be removed");
            }
        }
    }
}
```

**Key Features:**
- Actual business logic (not a placeholder)
- Validates JSON Patch operations against non-patchable attributes
- Prevents removal of @type field
- Uses Constants class for non-patchable field names
- Spring-managed component with ObjectMapper injection
- Logger for debugging validation issues

## Template Variables

### Common Variables

All infrastructure templates use these variables:

- `generator['project']['name']` - Project name (e.g., "dcmms")
- `generator['project']['dnext']['structure']['configuration']['package']` - Full package path
- `generator['project']['dnext']['structure']['configuration']['package_info']` - Package description
- `generator['project']['dnext']['structure']['security']['package']` - Full package path
- `generator['project']['dnext']['structure']['security']['package_info']` - Package description

### Example Values

For dcmms project:
```yaml
project:
  name: dcmms
  dnext:
    structure:
      configuration:
        package: com.pia.orbitant.dcmms.configuration
        package_info: This package is responsible for handling the project specific configuration.
      security:
        package: com.pia.orbitant.dcmms.security
        package_info: This package is responsible for handling the project specific security logic.
```

## Generation Instructions

### Directory Structure

Create these directories during generation:
```
{base_package}/
├── configuration/
├── security/
└── util/
```

### File Generation Order

1. Create Constants.java first (no dependencies)
2. Create BaseValidationUtil.java (depends on Constants)
3. Create {Project}AppProperties.java
4. Create {Project}AppSecurity.java

### Validation Checklist

After generating infrastructure files, verify:

- [ ] configuration/ directory exists
- [ ] security/ directory exists
- [ ] util/ directory exists
- [ ] Constants.java defines all non-patchable attributes
- [ ] BaseValidationUtil.java imports Constants
- [ ] BaseValidationUtil.java has both validation methods
- [ ] {Project}AppProperties.java has correct prefix
- [ ] {Project}AppSecurity.java is a valid Spring component
- [ ] All files have @jakarta.annotation.Generated annotation
- [ ] Project compiles successfully

## Dependencies

Infrastructure files require these dependencies (should already be in pom.xml):

```xml
<!-- Spring Boot -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
</dependency>

<!-- JSON Patch -->
<dependency>
    <groupId>com.github.java-json-tools</groupId>
    <artifactId>json-patch</artifactId>
</dependency>

<!-- Jackson -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>

<!-- Common Core (for OrbitantException, ExceptionFactory) -->
<dependency>
    <groupId>com.pia.orbitant</groupId>
    <artifactId>common-core</artifactId>
</dependency>
```

## Usage in Generated Code

### BaseValidationUtil Usage

The BaseValidationUtil is used by validator classes:

```java
@Component
public class CustomerCheckForNonPatchableAttributesInJsonPatchValidator
        implements JsonPatchValidator {

    private final BaseValidationUtil validationUtil;

    public CustomerCheckForNonPatchableAttributesInJsonPatchValidator(
            BaseValidationUtil validationUtil) {
        this.validationUtil = validationUtil;
    }

    @Override
    public boolean isValid(JsonPatch validationObject) {
        try {
            validationUtil.checkForNonPatchableAttributesInJsonPatch(validationObject);
        } catch (OrbitantException e) {
            return false;
        }
        return true;
    }
}
```

## Notes

**Placeholder vs. Functional:**
- Configuration and Security files are **placeholders** for future customization
- Constants and BaseValidationUtil contain **actual business logic**

**Why Generate Placeholders:**
- Establishes standard package structure
- Provides clear extension points for custom logic
- Ensures consistent organization across projects
- Documents where project-specific code should go
