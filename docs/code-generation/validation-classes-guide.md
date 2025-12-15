# Validation Classes Generation Guide

## Overview

Validation classes provide business validation logic for CRUD operations on TMF entities. The framework uses validator interfaces from common-core, and generators create stub implementations that developers can customize.

## Validation Class Categories

### 1. Delete Validation

**Template:** `templates/delete_validation.java.j2`

**Generated File:** `{base_package}/validation/{Entity}DeleteValidation.java`

**Purpose:** Validate entity before deletion (e.g., check dependencies, business rules)

**Content:**
```java
package com.pia.orbitant.{project}.validation;

import com.pia.orbitant.validator.business.validator.DeleteValidator;
import com.pia.orbitant.{project}.entity.Internal{Entity};
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * This package is responsible for handling the validation logic.
 */
@jakarta.annotation.Generated(value = "DNext API Generator")
@Component
public class {Entity}DeleteValidation implements DeleteValidator<Internal{Entity}> {

    @Override
    public String errorMessage(Internal{Entity} entity) {
        return null;
    }

    @Override
    public boolean isValid(Internal{Entity} entity) {
        return false;
    }

    @Override
    public String bvrShortName() {
        return null;
    }
}
```

**Key Features:**
- Implements `DeleteValidator<Internal{Entity}>`
- Stub returns `false` (validation fails by default - safe behavior)
- Developers customize to implement actual deletion rules

### 2. Entity Validation

**Template:** `templates/entity_validation.java.j2`

**Generated File:** `{base_package}/validation/{Entity}EntityValidation.java`

**Purpose:** Validate entity fields and business rules during create/update

**Content:**
```java
package com.pia.orbitant.{project}.validation;

import com.pia.orbitant.validator.business.validator.EntityValidator;
import com.pia.orbitant.{project}.entity.Internal{Entity};
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * This package is responsible for handling the validation logic.
 */
@jakarta.annotation.Generated(value = "DNext API Generator")
@Component
public class {Entity}EntityValidation implements EntityValidator<Internal{Entity}> {

    @Override
    public String errorMessage(Internal{Entity} entity) {
        return null;
    }

    @Override
    public boolean isValid(Internal{Entity} entity) {
        return false;
    }

    @Override
    public String bvrShortName() {
        return null;
    }
}
```

**Key Features:**
- Implements `EntityValidator<Internal{Entity}>`
- Validates entity state and business rules
- Stub returns `false` for safe default

### 3. JSON Patch Validation

**Template:** `templates/json_patch_validation.java.j2`

**Generated File:** `{base_package}/validation/{Entity}JsonPatchValidation.java`

**Purpose:** Validate JSON Patch operations beyond non-patchable attribute checks

**Content:**
```java
package com.pia.orbitant.{project}.validation;

import com.pia.orbitant.validator.business.validator.JsonPatchValidator;
import com.pia.orbitant.{project}.entity.Internal{Entity};
import org.springframework.stereotype.Component;
import com.github.fge.jsonpatch.JsonPatch;

import java.util.List;

/**
 * This package is responsible for handling the validation logic.
 */
@jakarta.annotation.Generated(value = "DNext API Generator")
@Component
public class {Entity}JsonPatchValidation implements JsonPatchValidator {

    @Override
    public String errorMessage(JsonPatch jsonPatch) {
        return null;
    }

    @Override
    public boolean isValid(JsonPatch jsonPatch) {
        return false;
    }

    @Override
    public Class<?> entityType() {
        return null;
    }

    @Override
    public String bvrShortName() {
        return null;
    }
}
```

**Key Features:**
- Implements `JsonPatchValidator`
- Validates patch operations (beyond standard non-patchable checks)
- Must return entity type via `entityType()` method
- Stub returns `false` for safe default

### 4. Update Validation

**Template:** `templates/update_validation.java.j2`

**Generated File:** `{base_package}/validation/{Entity}UpdateValidation.java`

**Purpose:** Validate updates by comparing original and patched entity

**Content:**
```java
package com.pia.orbitant.{project}.validation;

import com.pia.orbitant.validator.business.validator.UpdateValidator;
import com.pia.orbitant.{project}.entity.Internal{Entity};
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * This package is responsible for handling the validation logic.
 */
@jakarta.annotation.Generated(value = "DNext API Generator")
@Component
public class {Entity}UpdateValidation implements UpdateValidator<Internal{Entity}> {

    @Override
    public String errorMessage(Internal{Entity} original, Internal{Entity} patched) {
        return null;
    }

    @Override
    public boolean isValid(Internal{Entity} original, Internal{Entity} patched) {
        return false;
    }

    @Override
    public String bvrShortName() {
        return null;
    }
}
```

**Key Features:**
- Implements `UpdateValidator<Internal{Entity}>`
- Compares original and patched entity states
- Can validate field changes, state transitions, etc.
- Stub returns `false` for safe default

### 5. Non-Patchable Attributes Validator (Functional)

**Template:** `templates/validators/check_for_non_patchable_attributes_in_json_patch_validator.java.j2`

**Generated File:** `{base_package}/validator/{entity}/jsonpatch/{Entity}CheckForNonPatchableAttributesInJsonPatchValidator.java`

**Purpose:** Prevent patching of immutable TMF fields (@type, id, href, etc.)

**Content:**
```java
package com.pia.orbitant.{project}.validator.{entity}.jsonpatch;

import com.pia.orbitant.validator.business.validator.JsonPatchValidator;
import com.pia.orbitant.{project}.entity.Internal{Entity};
import org.springframework.stereotype.Component;
import com.github.fge.jsonpatch.JsonPatch;
import com.pia.orbitant.common.exception.common.OrbitantException;
import com.pia.orbitant.{project}.util.BaseValidationUtil;

import java.util.List;

/**
 * This package is responsible for handling the validation logic.
 */
@jakarta.annotation.Generated(value = "DNext API Generator")
@Component
public class {Entity}CheckForNonPatchableAttributesInJsonPatchValidator
        implements JsonPatchValidator {

    private final BaseValidationUtil validationUtil;

    public {Entity}CheckForNonPatchableAttributesInJsonPatchValidator(
            BaseValidationUtil validationUtil) {
        this.validationUtil = validationUtil;
    }

    @Override
    public String errorMessage(JsonPatch jsonPatch) {
        return "They are non patchable attributes in the path";
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

    @Override
    public Class<?> entityType() {
        return Internal{Entity}.class;
    }

    @Override
    public String bvrShortName() {
        return "{Entity}CheckForNonPatchableAttributesInJsonPatchValidator";
    }
}
```

**Key Features:**
- **Functional validator** (not a stub - has actual logic)
- Uses BaseValidationUtil for validation
- Prevents patching: id, href, @type, @baseType, @schemaLocation, creationDate
- Returns meaningful error message
- Required for proper JSON Patch RFC 6902 compliance

## Package Structure

### validation/ Package (Stubs)
```
{base_package}/validation/
├── {Entity}DeleteValidation.java
├── {Entity}EntityValidation.java
├── {Entity}JsonPatchValidation.java
└── {Entity}UpdateValidation.java
```

### validator/ Package (Functional)
```
{base_package}/validator/
└── {entity}/
    └── jsonpatch/
        └── {Entity}CheckForNonPatchableAttributesInJsonPatchValidator.java
```

**Note:** validator/ uses lowercase entity name in path, validation/ does not.

## Template Variables

### Required Variables

- `generator['project']['name']` - Project name (e.g., "dcmms")
- `entity_name` - Entity name in PascalCase (e.g., "Customer")
- `entity_name_lower` - Entity name in lowercase (e.g., "customer")
- `base_package` - Base package path (e.g., "com.pia.orbitant.dcmms")

### Example Template Usage

```jinja2
package {{ base_package }}.validation;

import {{ base_package }}.entity.Internal{{ entity_name }};

public class {{ entity_name }}DeleteValidation
        implements DeleteValidator<Internal{{ entity_name }}> {
    // ...
}
```

## Generation Instructions

### Directory Structure

Create these directories during generation:
```
{base_package}/
├── validation/
└── validator/
    └── {entity_name_lower}/
        └── jsonpatch/
```

### File Generation Order

1. Create validation/ directory
2. Generate 4 stub validators (Delete, Entity, JsonPatch, Update)
3. Create validator/{entity}/jsonpatch/ directory
4. Generate CheckForNonPatchableAttributesInJsonPatchValidator

### Dependencies

Generated validators depend on:
- BaseValidationUtil (must be generated first)
- Constants (must be generated first)
- Internal entity classes
- common-core validator interfaces

## Validation Framework Integration

### How Validators Are Used

The BusinessValidationService from common-core automatically discovers and runs all validators:

1. **Component Scanning:** Spring finds all `@Component` validators
2. **Interface Detection:** Framework identifies validator type by interface
3. **Automatic Execution:** Validators run during appropriate operations
4. **Failure Handling:** If `isValid()` returns false, operation is rejected with `errorMessage()`

### Developer Customization

Developers customize stub validators by:

1. **Implementing isValid():**
   ```java
   @Override
   public boolean isValid(InternalCustomer entity) {
       // Check business rules
       if (entity.getName() == null || entity.getName().isEmpty()) {
           return false;
       }
       return true;
   }
   ```

2. **Providing Error Messages:**
   ```java
   @Override
   public String errorMessage(InternalCustomer entity) {
       return "Customer name is required";
   }
   ```

3. **Setting BVR Name:**
   ```java
   @Override
   public String bvrShortName() {
       return "CUSTOMER_NAME_REQUIRED";
   }
   ```

## Validation Checklist

After generating validation classes, verify:

- [ ] validation/ directory exists
- [ ] validator/{entity}/jsonpatch/ directory exists
- [ ] All 4 stub validators generated (Delete, Entity, JsonPatch, Update)
- [ ] CheckForNonPatchableAttributesInJsonPatchValidator generated
- [ ] All validators have @Component annotation
- [ ] All validators have @Generated annotation
- [ ] Functional validator imports BaseValidationUtil
- [ ] Functional validator implements all interface methods
- [ ] Package names match directory structure
- [ ] Entity types are correct (Internal{Entity})
- [ ] Project compiles successfully

## Why Generate Stubs?

**Stub validators serve important purposes:**

1. **Framework Registration:** Spring discovers them even with stub implementation
2. **Clear Extension Points:** Developers know exactly where to add validation
3. **Safe Defaults:** Returning `false` prevents unvalidated operations
4. **Consistent Structure:** All projects have same validation organization
5. **Documentation:** Stubs document what validation is possible

**Production Requirement:** Before production deployment, developers must implement actual validation logic in stubs or remove unused validators.

## Example: Customizing a Stub Validator

**Generated Stub:**
```java
@Component
public class CustomerDeleteValidation implements DeleteValidator<InternalCustomer> {
    @Override
    public boolean isValid(InternalCustomer entity) {
        return false;  // Stub - blocks all deletions
    }

    @Override
    public String errorMessage(InternalCustomer entity) {
        return null;
    }
}
```

**Developer Customization:**
```java
@Component
public class CustomerDeleteValidation implements DeleteValidator<InternalCustomer> {

    private final OrderRepository orderRepository;

    public CustomerDeleteValidation(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public boolean isValid(InternalCustomer entity) {
        // Prevent deletion if customer has active orders
        return orderRepository.countByCustomerIdAndStatus(
            entity.getId(), "ACTIVE") == 0;
    }

    @Override
    public String errorMessage(InternalCustomer entity) {
        return "Cannot delete customer with active orders";
    }

    @Override
    public String bvrShortName() {
        return "CUSTOMER_HAS_ACTIVE_ORDERS";
    }
}
```

## Notes

**Stub vs. Functional:**
- 4 validators are **stubs** (developers must customize)
- 1 validator is **functional** (CheckForNonPatchableAttributesInJsonPatchValidator)

**Safe Defaults:**
- Stubs return `false` to prevent unvalidated operations
- This is intentionally restrictive for safety
- Developers must explicitly allow operations by implementing logic

**Template Reuse:**
- Same templates work for all entities (Customer, Product, etc.)
- Only entity name changes between generations
