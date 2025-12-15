# AI-SDLC Work Log - 2025-W50

## 2025-12-08 - Building AI Skeleton Code Generator: Pilot Project Analysis

**Duration:** 5h 13m

### Goals

- Create an AI-based skeleton code generator to replace Python/Jinja2 template generator
- Use DCMMS (Customer Management - simple entity) and DTTMS (Trouble Ticket - polymorphic entity) as pilot projects
- Analyze what AI generator must produce by fixing its initial output and comparing to Python generator
- Document all required patterns, files, and configurations for successful code generation
- Create comprehensive action plan for implementing the AI generator

### Prompt Patterns

#### Pattern: Strategic Pilot Project Selection

**Context**: Beginning work on AI code generator replacement, need to understand requirements

**Prompt**:
> "I will work on the code generator with an alternative approach: there is my past work in temp/code-generator. The main code is in python, and I've added a set of instructions to fix the generated code (as best as we can). There are still gaps, though... I want you to examine the massive amount of information, and replace the code generator with you as the assistant."

**Why it worked**: User provided concrete examples (working Python generator, generated code samples, experiment results) rather than abstract requirements. Using real TMF APIs (TMF629 Customer, TMF621 Trouble Ticket) as pilots grounded the analysis in actual production needs.

**Outcome**: Established two-pilot strategy - DCMMS for simple entities, DTTMS for polymorphic complexity, providing full coverage of generator requirements.

---

#### Pattern: Critical Redirect for Proper Learning Sequence

**Context**: Initially analyzed DTTMS (complex polymorphic case), discovered version mismatches and missing base classes

**Prompt**:
> "I think the fact that AI generator didn't use baseappmapper for dcmms is wrong. These classes should always be used when generating the skeleton. In production they should either be extended, or replaced (for services that break the common-core provided code flows). Before we generate an action plan, I want you to go back to dcmms and update both the service gateway and service classes to use the bases."

**Why it worked**: User recognized that understanding the simple case (DCMMS) completely before tackling complexity (DTTMS) would yield better generator requirements. The redirect established that AI generator should ALWAYS produce base class patterns, even if production overrides them.

**Outcome**: Clear architectural principle for generator: "Base classes should ALWAYS be in generated skeleton." Pilot validated this produces compilable code with correct DNext framework integration.

---

#### Pattern: Two-Stage Analysis Strategy

**Context**: AI generator output used different versions than Python generator, making comparison difficult

**Prompt**:
> "Let's do it in stages: first aim is to replicate python generator's capabilities. So downgrade common-dependencies to match the version of common-core that python generator uses first. Then we will address the generated code by matching it against python generator output (for generics and constructors)"

**Why it worked**: Breaking analysis into (1) achieve parity with Python generator, then (2) identify improvements prevented mixing version differences with actual pattern differences. Established clear baseline for what AI generator must produce.

**Outcome**: Version compatibility matrix documented (common-mongo 3.12.0-ca → common-core 3.13.0-ca), clear understanding of which patterns come from framework vs. generator decisions.

---

#### Pattern: Pilot-Driven Requirements Discovery

**Context**: Through iterative fixing of DCMMS, discovered missing files and patterns

**Prompt**:
> "After the commit, ignore those internal/only fields and compile again. We can do slightly better than python generator"

**Why it worked**: User encouraged learning through doing rather than upfront analysis. Each compilation error revealed a generator requirement. Aspirational goal ("slightly better") pushed beyond mere replication.

**Outcome**: Discovered AI generator must produce 60+ files per entity (not just service implementation), including all Internal* entities, event infrastructure, and mapper overrides Python generator lacks.

---

#### Pattern: Document-Then-Build for Knowledge Capture

**Context**: After DCMMS pilot success, before implementing generator

**Prompt**:
> "Yes, let's generate the action plan... Do not commit the plan. Name it to indicate that this is actions for test round 1. Make it a sibling to the test and test-fixes directories."

**Why it worked**: Captured requirements immediately after pilot validation, while patterns and decisions were fresh. Positioned as "test round 1" action plan, acknowledging this is iterative learning for generator development.

**Outcome**: Comprehensive ACTION_PLAN_TEST_ROUND_1.md documenting all generator requirements: 10 file types per entity, dependency patterns, polymorphic handling, validation steps, migration path.

---

### Outcomes

**Generator Requirements Discovered:**

- Must generate 10+ file types per entity (Service, ServiceImpl, ServiceGateway, ServiceGatewayImpl, Mapper, Repository, API, APIImpl, EventMapper, plus 8 event files)
- Must create Internal* version of ALL TMF model objects (51 for DCMMS, 48 for DTTMS), not just main entity
- Must use direct version management with common-mongo 3.12.0-ca (not BOM)
- Must extend all DNext base classes (BaseAppServiceImpl, BaseAppServiceGatewayImpl, BaseAppEventMapper, BaseRepository)
- Must add ignore directives for polymorphic fields and audit fields in MapStruct mappers
- Must include mvoToEntity override that Python generator lacks
- Must use correct import paths (BusinessValidationService from validator.business.common, etc.)

**Pilot Project Status:**

✅ **DCMMS (simple entity)**: Complete and validated
- 60+ files created/modified
- Compiles with BUILD SUCCESS
- Zero warnings (Python generator has 6)
- All base classes properly integrated
- Full event infrastructure
- All Internal* entities present

⏸️ **DTTMS (polymorphic entity)**: Started, errors documented for next session
- pom.xml updated
- Mapper updated with mvoToEntity override
- Compilation reveals additional complexity:
  - MapStruct polymorphic field errors (attachment, relatedParty.partyOrPartyRole)
  - Enum mapping mismatches (IN_PROGRESS vs INPROGRESS)
  - Import path errors (BaseAppRepository, BusinessValidationService, AclOwnershipUtil, ValidationUtil)
- Errors provide clear requirements for generator's polymorphic handling

**Documentation:**

- `ACTION_PLAN_TEST_ROUND_1.md` (19KB) - Complete generator specification based on DCMMS pilot learnings

### Lessons Learned

1. **Pilot-driven requirements beat upfront specification** - Rather than analyzing Python generator templates abstractly, running AI generator on real cases (DCMMS, DTTMS) and fixing the output revealed actual requirements through compilation errors. Each error = one generator requirement discovered.

2. **Simple-first sequencing prevents complexity confusion** - User's redirect from DTTMS→DCMMS established pattern: solve simple case completely before tackling complex case. This separated universal patterns (needed for all entities) from edge cases (polymorphic types only).

3. **Parity-then-improvement stages clarify value** - "First replicate Python generator capabilities, then improve" prevented mixing version mismatches with actual enhancements. Made it clear when AI generator was achieving baseline vs. adding value beyond existing tools.

4. **Compilation as validation checkpoint** - User's "compile again, we can do slightly better" pattern established rapid feedback loops. Each successful compile proved a requirement was correctly understood. Each error revealed a gap in understanding.

5. **Incremental correction over full explanation** - When assistant misunderstood which fields to ignore in mapper, user provided minimal correction ("Not the polymorphic fields. The createdBy, createdAt, etc.") rather than full re-explanation. Faster than re-teaching entire context.

6. **Documentation timing matters** - User requested action plan immediately after DCMMS success, before starting DTTMS. Captured knowledge while patterns were fresh and validated, creating specifications from working examples rather than theory.

7. **Aspirational goals drive beyond baseline** - "We can do slightly better than python generator" set expectation that AI generator should improve on existing tooling, not just replicate it. DCMMS achieved zero warnings vs. Python generator's 6 warnings.

8. **Strategic interrupts reshape approach** - User's "stop and go back to DCMMS" redirect mid-DTTMS analysis prevented pursuing wrong path. Willingness to interrupt and redirect proved more valuable than completing original plan.

9. **Named artifacts enable incremental work** - "ACTION_PLAN_TEST_ROUND_1.md" naming acknowledged this is iterative learning. "Test round 1" signals expectation of round 2, round 3, reducing pressure for perfection and enabling continuous refinement.

10. **Executable documentation from working examples** - The action plan wasn't written from specs but distilled from successfully fixed code. This inverts typical docs→code flow: code→understanding→docs→future code. The pilots taught us what to document, documentation will teach AI generator what to produce.

11. **Stop at failure points for fresh context** - Session ended with DTTMS compilation errors documented but unfixed. User chose to stop and continue in next session rather than push through when complexity increased. This preserves failure analysis for fresh thinking and prevents exhaustion-driven mistakes.

---
