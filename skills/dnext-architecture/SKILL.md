---
name: dnext-architecture
description: DNext platform architecture patterns, TMF630 API standards, component anatomy, module conventions, context loading, and repository structure. Auto-activates when working with DNext Java/Spring code, implementing TMF APIs, planning implementations, or discussing module design. Use when you see packages like com.pia.dnext, TMF entity patterns, or DNext module structure.
---

# DNext Architecture Skill

This skill provides domain knowledge for D-NEXT platform development.

## Core Reference Documents

- @temp/dnext-dev-support/architecture.md - Component anatomy, three-domain architecture, request flows
- @temp/dnext-dev-support/module-catalog.md - All 201+ modules with GitHub locations and TMF mappings

## Implementation Guides

- @temp/dnext-dev-support/references/service-gateway.md - Service Gateway pattern
- @temp/dnext-dev-support/references/model-mapping.md - DTO to Entity mapping rules
- @temp/dnext-dev-support/references/event-publishing.md - Kafka event patterns
- @temp/dnext-dev-support/references/business-validation.md - BVR framework
- @temp/dnext-dev-support/references/query-generation.md - TMF630 query features
- @temp/dnext-dev-support/references/error-handling.md - TMF-compliant error model
- @temp/dnext-dev-support/references/security.md - RBAC/ABAC patterns
- @temp/dnext-dev-support/references/testing.md - Testing patterns

## Technical Context Sources

When writing implementation guides or analyzing code, use these sources in order:

### 1. System-Wide Reference

- **Architecture guide**: `dnext-dev-support/architecture.md`
  - Component anatomy (3-domain architecture)
  - Request flow patterns
  - Core library overview
  - Project structure conventions
  - TMF API compliance rules

### 2. Current State (What IS)

- **Module design.md**: `{module-repo}/docs/design.md`
  - Current architecture, API surface, data models
  - Validation rules (BVRs + TVRs)
  - Recent changes
- **Exported services**: `{module-repo}/docs/exported-services.md`
  - Public API for other modules to consume
  - Contains "AI Usage Instructions" section

### 3. Target State (What's TO BE)

- **SRS extract**: `dnext-dev-support/epics/PRND-{epic}/srs-extract.md`
  - Business requirements from Confluence
  - Given/When/Then scenarios
  - BVR definitions
- **Feature design**: `dnext-dev-support/epics/PRND-{epic}/feature-design.md`
  - Technical approach for multi-module epics
  - Only exists for multi-module epics
- **Story description**: In `.stories.md` file
  - Scoped TO BE for this specific story

### Context Loading Priority

1. Read current branch name to determine JIRA story ID
2. Locate story file: `dnext-dev-support/epics/PRND-{epic}-*/stories.md`
3. Read story metadata from YAML front matter and story section
4. Load architecture.md for system-wide patterns
5. Load module design.md from affected module
6. Load srs-extract.md and feature-design.md from epic folder
7. For dependencies: use module-catalog.md to find exported-services.md

## Repository Structure

### Two-Repository Workspace

AI-assisted development requires at minimum two repositories:

1. **dnext-dev-support** - Central development support repo (always present)

   ```text
   dnext-dev-support/
   ├── architecture.md           # System architecture
   ├── module-catalog.md         # Index of all modules
   ├── references/               # Implementation guides
   ├── templates/                # Document templates
   └── epics/                    # Active epic folders
       └── PRND-{epic}-{slug}/
           ├── srs-extract.md
           ├── feature-design.md  # If multi-module
           └── stories.md
   ```

2. **Module repository** - The primary code being worked on

   ```text
   {module}/
   ├── src/
   ├── docs/
   │   ├── design.md             # Module design (living)
   │   └── exported-services.md  # Public API
   └── pom.xml
   ```

### File Locations Reference

| Document | Location |
|----------|----------|
| Architecture guide | `dnext-dev-support/architecture.md` |
| Module catalog | `dnext-dev-support/module-catalog.md` |
| Stories file | `dnext-dev-support/epics/PRND-{epic}-{slug}/stories.md` |
| SRS extract | `dnext-dev-support/epics/PRND-{epic}-{slug}/srs-extract.md` |
| Feature design | `dnext-dev-support/epics/PRND-{epic}-{slug}/feature-design.md` |
| Module design | `{module-repo}/docs/design.md` |
| Exported services | `{module-repo}/docs/exported-services.md` |

## AI Usage Instructions

Many documents contain an "AI Usage Instructions" section with specific guidance:

- **exported-services.md**: How to integrate with the module
- **design.md**: Module-specific patterns to follow
- **architecture.md**: System-wide conventions

Always check for and follow these instructions when present.

## When to Use This Skill

- Implementing new endpoints or services
- Understanding DNext module structure
- Following TMF630 API guidelines
- Mapping DTOs to entities
- Publishing Kafka events
- Implementing business validation
- Planning story implementation
- Loading context for development
