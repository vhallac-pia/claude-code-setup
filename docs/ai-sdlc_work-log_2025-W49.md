# AI-SDLC Work Log - 2025-W49

## 2025-12-03 - DNext API Generator Exploration & Recipe Alternative (Failed)

**Duration:** 2h 15m

### Goals

**Part 1: API Generator Discovery**
- Explore DNext api-generator project for TMF service generation
- Understand its current state and capabilities

**Part 2: Recipe Alternative**
- After api-generator proved problematic, create an AI-native recipe approach
- Migrate generated code to use DNext base classes (BaseAppMapper, BaseAppService, etc.)
- Align dependency versions with dnext-common-dependencies BOM

### Prompt Patterns

#### Pattern: Abandoning Failing Approach

**Context**: API generator experiment had multiple issues, 19 open PRs against it

**Prompt**:
> "Please stop this experiment for now, and have a look at the latest commits in the code generator in other branches."

**Why it worked**: Recognized when to cut losses and gather more information

**Outcome**: Discovered api-generator was in flux with many unmerged changes

---

#### Pattern: Strategic Pivot

**Context**: API generator too unstable to use

**Prompt**:
> "Let's try a different approach. You see the generated code from OAS. I think a recipe for you to generate them yourself would be a much better choice. Do you agree?"

**Why it worked**: Redirected from failing tooling to AI-native generation approach

**Outcome**: Created initial RECIPE.md for AI-assisted TMF API generation

---

#### Pattern: Dependency Architecture Correction

**Context**: Claude was pulling wrong dependency (common-core directly)

**Prompt**:
> "Do you think we need common-core? It should be common-mongo - who pulls in the rest"

**Why it worked**: Corrected transitive dependency understanding

**Outcome**: Partial - led to BOM version discovery issues

---

#### Pattern: BOM Version Guidance (Failed)

**Context**: Multiple attempts to find correct CA versions

**Prompt**:
> "Use 4.1.3-ca version of dependencies"
> "For common core, the branch is release/v3.38.1-ca"

**Why it worked**: Did NOT work initially - Claude used shallow clones and couldn't find branches

**Outcome**: Failed - required explicit branch name and full clone instruction

---

### Outcomes

**Part 1 - API Generator:**
- Discovered api-generator has 19 open PRs, unstable state
- Decision: abandon api-generator approach

**Part 2 - Recipe Approach:**

**Files created:**
- `temp/trouble-ticket/trouble-ticket-api/pom.xml` (incomplete)
- `temp/work/common-core/` (cloned, eventually correct branch)
- `temp/work/dnext-commons/` (cloned)

**Commits:**
- None (work incomplete)

### Lessons Learned

1. **Check project health first**: API generator had 19 open PRs - should have checked stability before deep-diving

2. **Branch naming conventions matter**: DNext uses `release/vX.Y.Z-ca` pattern for CA releases

3. **Shallow clones lose branches**: `git clone --depth 1` prevents branch checkout; use full clones

4. **BOM alignment is critical**: dnext-common-dependencies version must match common-core/common-mongo branch versions

5. **Context compression cost**: 5+ compressions caused repeated information loss

6. **Session FAILED**: Fresh start needed with proper dependency alignment from the beginning

---

## 2025-12-03 - Add Claude Code Skills and Slim Down CLAUDE.md

**Duration:** 25m

### Goals

- Research Claude Code capabilities (commands, sub-agents, skills)
- Create complementary skills referencing dnext-dev-support docs
- Extract detailed CLAUDE.md sections into skills

### Prompt Patterns

#### Pattern: Capability Research Before Implementation

**Context**: User wanted to improve Claude Code setup but wasn't sure which mechanism to use

**Prompt**:
> I want you to research claude-code capabilities, and then look at @claude-code/ for command/agent setup. Would it be a better idea to convert/augment some of the agents with skills? research specifically for commands, sub agents, skills in claude code and their use cases.

**Why it worked**: Explicit "research first" directive prevented premature implementation. Asked for recommendation rather than mandating approach.

**Outcome**: Discovered skills are model-autonomous with shared context; agents have isolated context. Recommendation: keep agents, add complementary skills.

---

#### Pattern: Scope Clarification with Domain Knowledge

**Context**: About to create state-management.md reference document

**Prompt**:
> wait. What is the state management? if it is related to srs prnd-27183, omit that

**Why it worked**: User's domain knowledge prevented unnecessary work - PRND-27183 is a separate epic, not a general pattern.

**Outcome**: Skipped state-management.md, only created service-gateway.md.

---

#### Pattern: Iterative Scope Expansion

**Context**: After creating skills, user wanted more refactoring

**Prompt**:
> Well done. Now, check @claude-code/CLAUDE.md as well. Can some of these instructions be extended, and moved into a specific skill?

**Why it worked**: Sequential expansion allowed thorough analysis before each step.

**Outcome**: CLAUDE.md reduced from 263 to 60 lines (77% reduction).

---

#### Pattern: Requesting Pushback

**Context**: User had an idea but wanted validation

**Prompt**:
> I want you to design a claude-code-expert skill... clarification: object to my new skill recommendation if it is unnecessary.

**Why it worked**: Explicitly requested critical evaluation rather than compliance.

**Outcome**: Recommended against the skill - ad-hoc analysis via `claude-code-guide` agent is more reliable than a potentially stale static skill.

---

### Outcomes

**Files created:**

- `claude-code/skills/dnext-architecture.md`
- `claude-code/skills/story-workflow.md`
- `claude-code/skills/story-conventions.md`
- `claude-code/skills/spring-testing.md`

**Files modified:**

- `claude-code/CLAUDE.md` (263→60 lines)

**Commits:**

```
(uncommitted)
```

### Lessons Learned

1. **Skills vs Agents**: Skills provide shared-context domain knowledge; agents provide isolated workflow execution. Different purposes - don't convert one to the other.

2. **Reference via @**: Use `@temp/repo/path.md` to reference existing docs rather than duplicating content into skills.

3. **CLAUDE.md = MUST rules only**: Detailed reference content belongs in skills; CLAUDE.md keeps essential rules and skill reference table.

---

## 2025-12-03 - SRS Critique Analysis and CLAUDE.md Update

**Duration:** 18m

### Goals

- Assess whether SRS critique issues block testing `/start-feature` and `/groom-stories` commands
- Add markdown formatting rule to CLAUDE.md
- Commit, push, and install claude-code files

### Prompt Patterns

#### Pattern: Scoped Feasibility Check

**Context**: User preparing to test-drive a workflow with an imperfect input document

**Prompt**:
> In temp directory, I have an SRS (not the original one) and an associated critique that identified problems in the SRS. My main purpose is to test drive the SRS to story breakdown parts of the proposed SDLC... Do any of these items in the critique present a serious impediment to this experiment?

**Why it worked**: Clearly scoped the question to "impediment to experiment" rather than asking for general advice. This let Claude focus on structural/parseability issues vs specification quality issues.

**Outcome**: Clear assessment that no issues block the workflow test - critique issues are spec quality problems, not structural problems.

---

#### Pattern: Conciseness Correction

**Context**: Claude added verbose example to a frequently-loaded file

**Prompt**:
> Isn't this too verbose for a file that is always in context?

**Why it worked**: Short, pointed feedback referencing the specific concern (context window usage). Claude immediately condensed to one line.

**Outcome**: Reduced from 20+ lines with examples to single-line rule.

---

#### Pattern: Multi-Step Task Chain

**Context**: Ready to finalize changes

**Prompt**:
> great. Please commit this first. Then push the repository to origin. Finally install the files in claude-code in my current ~/.claude directory

**Why it worked**: Clear sequence with dependencies. "First/then/finally" made execution order explicit.

**Outcome**: All three tasks completed (with minor auth issue resolved via GITHUB_TOKEN hint).

---

### Outcomes

**Files modified:**

- `claude-code/CLAUDE.md` - Added markdown formatting rule

**Commits:**

```
2ef9dfd Add markdown formatting rule to CLAUDE.md
```

**Other:**

- Pushed 5 commits to origin/main
- Installed claude-code files to `~/.claude/` (CLAUDE.md, 10 agents, 7 commands, 2 docs)

### Lessons Learned

1. When asking "will X block Y", explicitly state what Y is - enables focused analysis
2. Context-aware files benefit from aggressive brevity - examples can live elsewhere

---

## 2025-12-03 - SRS Migration Validation: Original vs IEEE 29148 Template

**Duration:** 14m

### Goals

- Verify completeness of SRS migration from original Confluence format to IEEE 29148 template
- Identify any business-relevant content lost during migration
- Update critique document to reflect issues remaining in migrated SRS

### Prompt Patterns

#### Pattern: Initial Task Definition with Context

**Context**: User had three files in temp directory - original SRS, migrated SRS, and template - and wasn't certain if migration was complete.

**Prompt**:
> In temp directory, there is an SRS original, a template, and the same SRS migrated to the original. However, during the copy from confluence, the SRS was copied only partially. I am not certain if reading was correct, and the adapter SRS is correct or not. Please compare the new style SRS against the information found in the original, and see if it needs to be updated. Remember that the template's SRS is a business level only SRS - and technical information was intentionally dropped from the original.

**Why it worked**:
- Clear file identification (original, template, migrated)
- Specific concern (partial copy, completeness uncertain)
- Important constraint provided (technical info intentionally dropped)

**Outcome**: Systematic comparison showing migration was substantially complete with enhancements.

---

#### Pattern: Requesting Specific Content Repetition

**Context**: After identifying a missing use case, user wanted to review it quickly.

**Prompt**:
> Can you repeat the use case here for my convenience?

**Why it worked**: Simple, direct request for specific content already processed. Avoided re-reading file.

**Outcome**: Use case displayed inline for quick review.

---

#### Pattern: Domain Expert Validation/Challenge

**Context**: AI identified a "missing" use case (SFS.MANAGE.ManualSpecificationEntry - admin direct DB access).

**Prompt**:
> No, this one doesn't make sense. A DB operator always has access to DB - it has no impact on the solution. Am I mistaken?

**Why it worked**:
- Direct challenge to AI's analysis
- Provided reasoning (DB access is infrastructure, not feature)
- Asked for validation ("Am I mistaken?")

**Outcome**: AI agreed the use case was correctly excluded - it describes infrastructure capability, not solution functionality.

---

#### Pattern: Sequential Task Extension

**Context**: After validating migration, user wanted critique document updated.

**Prompt**:
> Since the adapted SRS is mostly accurate, please examine the critique, and see if all points apply to the new style SRS as well.

**Why it worked**: Built on completed task (migration validation) to extend to related task (critique update).

**Outcome**: Comprehensive review showing 7 of 14 HIGH issues resolved, 7 remaining.

---

#### Pattern: File Organization Instruction

**Context**: Two versions of critique needed - one for original, one for migrated.

**Prompt**:
> Since there are differences, adapt the critique to the new SRS as well. Make sure the file name is clear enough. Alternatively, rename the existing critique to say it is for the original SRS.

**Why it worked**: Gave options for file organization rather than prescribing exact approach.

**Outcome**: Renamed original critique, created new critique for migrated SRS with updated issue status.

### Outcomes

**Files created:**

- `temp/SRS_PRND-27183_CRITIQUE_MIGRATED.md` - Updated critique for IEEE 29148 migrated SRS

**Files modified:**

- `temp/SRS_PRND-27183_CRITIQUE_WORK.md` → renamed to `temp/SRS_PRND-27183_CRITIQUE_ORIGINAL.md`

### Lessons Learned

1. Migration validation benefits from structured comparison tables showing what moved where, what was dropped, and what was added
2. User domain expertise is essential for validating AI-identified "gaps" - some perceived gaps are actually correct design decisions
3. A "missing use case" that describes capabilities outside the solution boundary (like direct DB access) should be excluded from SRS

---

## 2025-12-03 - SRS Critique: PRND-27183 State Flow Specification

**Duration:** 1h 19m

### Goals

- Perform systematic critique of existing SRS document (PRND-27183) before implementation
- Identify logical gaps, inconsistencies, and under-specified requirements
- Evaluate document from consumer perspective (can service be implemented and used?)
- Recommend architectural improvements

### Prompt Patterns

#### Pattern: Comprehensive Review Task Definition

**Context**: User had an existing SRS document that needed critical review before implementation.

**Prompt**:
> There is a file SRS_PRND-27183_ORIGINAL.md in temp. I want you to read this file, understand its purpose, and critique it. I am especially interested in logical gaps, under-/over- specified sections, gaps in requirements, unclear intent. Also review what is specified where: scenarios, detailed specifications, functional requirements, BVRs.

**Follow-up**:
> Be systematic about it, and keep track of current thinking in a helper file if needed (I do not need such a file, but you may need such a file as an externalized context to survive context compressions).

**Why it worked**:
- Clear scope: "read, understand, critique"
- Specific focus areas: logical gaps, over/under-specification, unclear intent
- Structural review: what belongs where (scenarios vs FRs vs BVRs)
- Practical advice: use external file to survive context compression

**Outcome**: Systematic critique with 21 categorized issues across HIGH/MEDIUM/LOW severity, organized by type.

---

#### Pattern: Domain Clarification Through Correction

**Context**: AI identified potential gaps that weren't actually gaps due to domain-specific patterns.

**Prompt sequence**:
> For the gaps, you can consider wholistic updates as the data document. There is no "delete a state"... So long as we have validation rules that check the consistency of the document, deleting states or transitions are not necessary to cover individually.

> There need not be a final state. It is possible that a service just loops over states - like allowing retired states to be reactivated.

> An update that sets the state to the same value can be considered to not include the state in the update. So self loops should not even touch the state validation service.

**Why it worked**: User corrected AI's assumptions about state machine semantics incrementally. Each correction built understanding without requiring full re-explanation.

**Outcome**: AI refined critique to focus on actual gaps rather than perceived ones.

---

#### Pattern: Iterative Gap Discovery

**Context**: After initial review, user revealed additional domain constraints.

**Prompt**:
> The entity reference that tells me what entity gets affected by a given specification. There are two big problems there. Let me start with the first: an EntityRef is a pointer to a single *instance* of a resource. What we need from a state spec is a resource *type* or *kind*.

**Follow-up**:
> The second is that, along with the resource type, I need the state field name/path. DNext provides TMF APIs, and the field may be named state, status, lifecycleState, etc.

**Why it worked**: User revealed gaps one at a time with explanation. Allowed AI to fully understand each issue before moving to next.

**Outcome**: Identified two critical missing requirements (EntityTypeRef, targetStateFieldPath).

---

#### Pattern: Consumer Perspective Reframe

**Context**: AI had been reviewing from API implementer perspective only.

**Prompt**:
> Now you understand more about the application domain and intent. Please do another review and see if there are any other problems. I need this SRS to reach a state where 1. we can implement a service 2. the service is actually useful: the consumers need to be able to figure out what state spec to use, and whether a given PATCH operation that touches state will be allowed.

**Why it worked**: Explicit success criteria from two perspectives (implementer AND consumer). Triggered discovery of consumer-facing gaps.

**Outcome**: Added 5 new HIGH severity issues (discovery pattern, entityStatusMapping semantics, transition lookup, etc.).

---

#### Pattern: Scope Recommendation with Options

**Context**: AI was proposing to add many consumer requirements to the SRS.

**Prompt**:
> The transition lookup, validation, etc. should be a separate SRS for consumers. The same goes for missing transitions. I think the two consumer scenarios should be removed and a fuller SRS should be written instead of fattening up this SRS. But present it as an either/or => either reduce scenarios, or add to them, and extend scope to go beyond writing/updating a REST service.

**Why it worked**: User provided architectural insight (separation of concerns) and asked for options rather than single recommendation.

**Outcome**: Critique restructured with clear Option A (reduce scope) vs Option B (expand scope) recommendation.

---

### Outcomes

**Files created:**

- `temp/SRS_PRND-27183_CRITIQUE_WORK.md` - Comprehensive critique document (21 issues)
- Confluence page 1403912194 - Critique uploaded to personal space

**Files modified:**

- `temp/SRS_PRND-27183_ORIGINAL.md` - Updated with full content from Confluence

**Issues identified:**

| Severity | Count | Key Examples |
|----------|-------|--------------|
| HIGH | 14 | stateType/stateCategory mismatch, FRs duplicate UCs, missing BVRs, wrong EntityRef type, missing targetStateFieldPath, consumer discovery undefined |
| MEDIUM | 4 | Empty FR traceability, missing lifecycle UC, lifecycleStatus not enumerated |
| LOW | 3 | Data model FRs, duplicate constraints, HTTP methods in BVRs |

**Architectural recommendation:**

- Option A (recommended): Remove SFS.VALIDATE.* consumer scenarios, keep as REST API spec, write separate Consumer SRS
- Option B: Expand scope to fully specify consumer behavior, rename to "State Flow Management Service"

### Lessons Learned

1. **Domain knowledge is iterative**: AI's initial gaps analysis had false positives (self-loops, final states). User corrections were essential.

2. **Consumer perspective reveals different gaps**: Reviewing only from implementer view missed critical usability issues (discovery, lookup semantics).

3. **Separation of concerns applies to specs**: Mixing API definition with consumer behavior patterns creates a document that does neither well.

4. **Working document survives context compression**: Keeping critique in external file (`CRITIQUE_WORK.md`) allowed review to span multiple context windows.

5. **Either/or recommendations**: When AI sees multiple valid approaches, presenting options with tradeoffs is more useful than picking one.

---

## 2025-12-03 - SRS Conversion & Section 3.2 Standardization

### Goals

- Convert existing PRND-27183 (State Flow Specification) SRS to IEEE 29148 format
- Create real SRS example in Confluence using the template
- Restructure section 3.2 format to eliminate redundancy between Use Cases table and scenarios
- Propagate format changes to all templates

### Prompt Patterns

#### Pattern: Format Conversion with Architecture Change

**Context**: User wanted to convert an old-style SRS to IEEE 29148 format, but also change the solution from an add-on to standalone service.

**Prompt**:
> In confluence there is an SRS for state flow specification service... Can you convert it to our new IEEE format?

**Follow-up**:
> This SRS assumes that the solution will be an add-on to a configuration management service. Can you revise it to make it a stand-alone service?

**Why it worked**: Two-step approach - first convert format, then adjust architecture. Keeps each change focused.

**Outcome**: Created complete IEEE 29148 SRS with standalone service perspective.

---

#### Pattern: Reference Checking via GitHub

**Context**: User wanted to verify TMF model patterns were correct.

**Prompt**:
> Can you check the OAS of trouble ticket specification (in github)? ... They should also have an externalReference

**Why it worked**: Directed AI to authoritative source (actual OAS files) rather than relying on AI's knowledge.

**Outcome**: Confirmed TMF patterns, identified addressability fields (id/href) and extensibility fields (@type/@baseType/@schemaLocation).

---

#### Pattern: Collaborative Format Design

**Context**: User noticed redundancy between Use Cases table and User Scenarios section.

**Prompt**:
> Does the IEEE format require this section to have the subsections User Scenarios and Functional Requirements? I am thinking of ditching these two subheadings, and providing the functional requirements section at top, and individual user scenarios as subheadings.

**Why it worked**: User proposed specific alternative structure, asked for validation. Enabled collaborative decision-making.

**Outcome**: Agreed on cleaner format - FR table first (normative), then scenarios as direct `####` subheadings with UC IDs.

---

#### Pattern: Interrupt to Redirect

**Context**: AI was about to update a DNextPM template that was the wrong document.

**Prompt**:
> Wait, do not update anything. I will provide you the link to the SRS template in my workspace

**Why it worked**: Clear stop command with explanation. Prevented wasted effort on wrong target.

**Outcome**: AI paused, user provided correct Confluence URL.

---

### Outcomes

**Files created:**

- `temp/SRS_PRND-27183_State-flow-specification.md` - Complete IEEE 29148 SRS
- `temp/SRS_PRND-27183_ORIGINAL.md` - Backup of original FRs/BVRs for reference
- Confluence page 1403584572 (SRS-PRND-27183: State Flow Specification)

**Files modified:**

- `temp/srs-ieee-29148_template.md` - Restructured section 3.2
- `docs/to-be-workflow-proposal.md` - Updated embedded SRS template
- Confluence page 1403748376 (SRS Template IEEE 29148) - Restructured section 3.2

**Section 3.2 format standardized:**

- Old: Use Cases table → "User Scenarios" subheading → "Functional Requirements" subheading → FR table
- New: FR table first → Note → Scenarios as `####` subheadings with UC IDs and Related Requirements

**Commits:**
```
80d4862 Update SRS template section 3.2 to standardized format
```

### Lessons Learned

1. **Real examples drive template refinement**: Converting an actual SRS (PRND-27183) exposed redundancy in section 3.2 that wasn't obvious in the abstract template.

2. **GitHub as authoritative source**: Checking actual OAS files in GitHub repos (trouble-ticket, product-ordering) provided definitive TMF patterns.

3. **Interrupt to redirect**: "Wait, do not update anything" prevented updating wrong template. User course corrections should be welcomed.

4. **Commit message preferences**: User didn't want Claude attribution lines.

---

## 2025-12-03 - Documentation Cleanup & Standardization

### Goals

- Create IEEE 29148 SRS template from proposal
- Standardize documentation conventions (single stories.md, epics/ folder)
- Remove obsolete archiving workflow
- Delete unused agents and working documents
- Upload SRS template to Confluence

### Prompt Patterns

#### Pattern: Document Extraction

**Context**: User knew a document existed in the codebase but wanted it extracted into a standalone template.

**Prompt**:
> Find the proposal that contains the short SRS format, and write it as a markdown document. Name it `srs-ieee-29148_template.md`

**Why it worked**: Specific output format requested, clear naming, assumes AI can locate the source.

**Outcome**: Created complete IEEE 29148 SRS template from proposal content.

---

#### Pattern: Consistency Check with Direction

**Context**: User had made changes and wanted to find conflicts, but also indicated what the correct state should be.

**Prompt**:
> I made a change today. It should be a single stories.md per epic, instead of a file per story. Find the conflicting statements in the proposal.

**Why it worked**: States the desired convention first, then asks for conflicts. AI knows what "correct" looks like.

**Outcome**: Found and listed all locations with old `PRND-{story-id}.stories.md` pattern.

---

#### Pattern: Cross-Repository Sync Check

**Context**: Documentation existed in multiple places that needed to stay consistent.

**Prompt**:
> There is also a dnext-dev-support repo. Is it in sync with the documents?

**Why it worked**: Open-ended enough to let AI discover issues, but scoped to a specific repo.

**Outcome**: Discovered `active/` vs `epics/` naming inconsistency across repos.

---

#### Pattern: Cleanup Confirmation

**Context**: After identifying obsolete components, user gave clear deletion instructions.

**Prompt**:
> I think specs updater is now called design updater. I don't need a product owner. Delete these.

**Why it worked**: Provides reasoning (duplicate), gives explicit action (delete), batches related items.

**Outcome**: Deleted both agents cleanly.

---

#### Pattern: Iterative Refinement via Correction

**Context**: AI generated commit messages, user wanted adjustment to style.

**Prompt 1**:
> Please follow instructions in dev-support CLAUDE.md when committing

**Prompt 2** (after seeing result):
> Please also write up a little less terse description in commit description

**Why it worked**: First prompt pointed to existing guidelines. Second prompt gave specific stylistic feedback without rewriting the whole thing.

**Outcome**: Commit messages followed conventions with appropriate detail level.

---

#### Pattern: Defer Without Losing

**Context**: User wanted to exclude a file from commit but keep it for later use.

**Prompt**:
> Let's leave this template out for now. I have other plans for it. You can move to temp to ensure it is never committed

**Why it worked**: Clear intent (exclude), provides solution (move to temp), explains why (not committed).

**Outcome**: File preserved in temp/, added to .gitignore.

---

#### Pattern: Hierarchical Content Creation

**Context**: User wanted to create a folder structure in an external system with content.

**Prompt**:
> I want you to create a folder in confluence (space ahmet vedat hallac) named ai-sdlc; under it, create a subfolder named templates, and upload our SRS template there.

**Why it worked**: Clear hierarchy (parent → child → content), specific names, references existing artifact ("our SRS template").

**Outcome**: Created three nested Confluence pages with full template content.

---

### Outcomes

**Files deleted:**

- `claude-code/agents/specs-updater.md`
- `claude-code/agents/product-owner.md`
- `claude-code/agents/story-design-architect.md`
- `claude-code/agents/spike-assistant.md`
- `ai-assisted-sdlc-workflow-analysis.md`
- `understanding-dpr-epic-srs-stories.md`
- `rough idea.txt`
- `convert_to_html.py`
- `generate_proposal_html.py`

**Files modified:**

- `docs/claude-code/index.md` - Removed archive references
- `docs/claude-code/workflow-overview.md` - Updated Stage 7, removed archive workflow
- `docs/executive-summary.md` - Changed archive → cleanup/delete
- `docs/to-be-workflow-proposal.md` - Updated lifecycle, removed archive phases
- `.gitignore` - Added temp/

**Files created:**

- `temp/srs-ieee-29148_template.md` - IEEE 29148 SRS template
- Confluence: ai-sdlc → templates → SRS Template (IEEE 29148)

**Commits:**
```
fc7afd3 Remove obsolete working documents and HTML scripts
d948e10 Remove archiving from workflow, delete unused agents
e29a941 Add temp/ to gitignore for local clones
```

### Lessons Learned

1. **Commit message feedback loop**: User corrected twice (follow CLAUDE.md, less terse). Pattern: point to guidelines first, then give stylistic feedback.

2. **Git authentication**: `pass-git-helper` not found error. Workaround: embed token in URL. Worth documenting for future sessions.

3. **Batching deletions**: User naturally grouped related deletions ("delete these two", "also drop spike assistant"). AI should propose similar groupings.

4. **Canonical source matters**: When finding inconsistencies, ask user which version is canonical before making changes across files.
## 2025-12-03 to 2025-12-05 - TMF API Code Generator Validation and Polymorphic Type Analysis

**Duration:** 11h 2m (Session 1: 4h 20m [failed], Session 2: 6h 42m [successful])

### Goals

- Validate DNext API Generator (Python-based) by generating complete working skeletons from TMF OAS files
- Test generation across multiple TMF API types (simple CRUD, polymorphic types, job-based workflows)
- Document successful generation patterns and identify generator limitations
- Compare generated code against live production implementations
- Identify critical gaps in generated code quality

### Prompt Patterns

#### Pattern: Iterative Failure Recovery and Fresh Start

**Context**: After 4+ hours of failed attempts with wrong generator versions and recipe-based approaches

**Prompt**:
> Mark this work a series of failures. I will start over in a new session. extract work log using the command, but exclude this comment from session duration calculations

Then later:
> There is a working code skeleton generator in github. I think it is called api generator. Please find it. I want you to clone branch for 34393 in temp/code-generator branch of my current project. I don't know the exact name of branch, so also find it.

**Why it worked**:
- Explicit acknowledgment of failure prevents sunk-cost fallacy
- Clean slate approach with new session
- Specific branch number (34393) provided concrete target
- "I don't know the exact name" delegated research to agent

**Outcome**: Successfully located and cloned `feature/PRND-34393` branch, which had all necessary fixes

---

#### Pattern: Living Documentation Creation

**Context**: After successful first generation with configuration trial-and-error

**Prompt**:
> I want you to create a work-instructions.md file for AI use to record each step that resulted in success. This file will be our knowledge base for code generation via AI assistance. Record the OAS copying and configuration modification step as the first entry (assume api-generator is already cloned)

**Why it worked**:
- "for AI use" clarified the audience (machine-readable instructions)
- "record each step that resulted in success" - focus on what works
- Incremental approach: "first entry... assume generator already cloned"
- Created living document that grew throughout session

**Outcome**: Created `work-instructions.md` that documented 3 successful fixes discovered during generation

---

#### Pattern: Incremental Review with Batched Feedback

**Context**: After first generation completed, reviewing output systematically

**Prompt**:
> Now, I will enter some review findings one by one. I want you to take note first. We will update the configuration accordingly. The first stage is related to naming: there are multiple places where we still have trouble-ticket instead of dttms.
>
> 1. The generated folder names.
> 2. In jenkinsfile: references to trouble-ticket
> [... 5 more issues listed]

**Why it worked**:
- "I will enter... one by one. I want you to take note first" - set expectation for batch review
- "We will update configuration accordingly" - deferred action until all issues identified
- Numbered list with specific file locations
- Focus on pattern (naming inconsistency) before details

**Outcome**: Identified configuration gaps without premature fixes, leading to comprehensive configuration updates

---

#### Pattern: Controlled Experiment Scaling

**Context**: After DTTMS success, planning next experiments

**Prompt**:
> Now, let's verify the instructions and the generator:
>
> 1. Rename generated to generated.dttms
> 2. Bring in TMF-620 v5 from [URL]
> 3. Name things as dpcms and follow the instructions
>
> Do not attempt a maven build yet. Let's just see that the generation works

**Why it worked**:
- Numbered steps create clear experiment protocol
- "Do not attempt maven build yet" - explicit scope limitation preventing premature optimization
- "Let's just see that generation works" - clear success criterion

**Outcome**: Quickly identified that TMF620 Product Catalog has Job entity complexities that need separate handling

---

#### Pattern: Pivot Based on Complexity Discovery

**Context**: DPCMS generation revealed Job entity issues

**Prompt**:
> No. Let's pick a simpler v5 that will not have these job complexities from DPCMS. We'll come back to TMF-620 with lessons learned and attempt to fix it later. Let's do two: one simple Crud: Customer Management, and one complex crud: resource inventory (has polymorphic types). Start with customer management, find the v5 OAS.

**Why it worked**:
- Direct rejection ("No") with explanation
- Defer complex case: "We'll come back... with lessons learned"
- Two-tier strategy: simple first, then complex
- Specific selection criteria: "simple CRUD" vs "complex CRUD with polymorphic types"
- Clear prioritization: "Start with customer management"

**Outcome**: Successfully generated DCMMS (Customer Management), then DACMS (Account Management with polymorphic types)

---

#### Pattern: Deep Investigative Questioning

**Context**: Noticing potential storage pattern issue with polymorphic types

**Prompt**:
> Hang on, can you show me the collections for BillingAccount and PartyAccount? If they are separate collections, the list and search operations will have a hard time combining these

**Why it worked**:
- "Hang on" - interrupt signal when something doesn't add up
- Specific evidence request: "show me the collections"
- Stated concern with reasoning: "list and search operations will have a hard time"
- Invited agent to validate or refute the concern

**Outcome**: Discovered TMF666 uses Table Per Concrete Class pattern (separate collections), not Single Table Inheritance, leading to analysis of two distinct polymorphic storage strategies

---

#### Pattern: Cross-API Pattern Recognition Request

**Context**: Trying to understand TMF design philosophy evolution

**Prompt**:
> Are there any v5 apis that are polymorphic, but uses the same endpoint? In DRIMS, they moved away from logicalResource endpoint into a shared resource endpoint.

**Why it worked**:
- Provided concrete historical example (DRIMS/Resource Inventory)
- Asked for pattern across version (v5 specifically)
- Showed understanding of design trade-offs (separate vs shared endpoints)

**Outcome**: Led to comparison between TMF639 v4 (polymorphic /resource) and TMF666 v5 (separate endpoints), documenting the philosophy shift from OOP to resource-centric microservices

---

#### Pattern: Direct Production Code Comparison

**Context**: After successful generations, validating against reality

**Prompt**:
> Before we conclude the session, is there any mapping handling difference between generated dttms code and the currently live trouble-ticket repository? Clone the develop branches of trouble-ticket-model and trouble-ticket repositories from dnext-technology organization to compare them.

**Why it worked**:
- "Before we conclude" - signals end-of-session validation
- Specific comparison: "generated vs live"
- Exact repos and branches specified
- Open-ended question allows agent to discover unexpected differences

**Outcome**: Discovered critical missing feature - generated code lacks polymorphic dispatch logic (156 lines of switch-based type discrimination) that was manually added to production code

---

### Outcomes

**Experiment Results:**

| API | Commercial Name | Status | Notes |
|-----|----------------|--------|-------|
| TMF621 Trouble Ticket v5 | DTTMS | ✅ SUCCESS | Build clean, polymorphic types (Single Table Inheritance) |
| TMF620 Product Catalog v5 | DPCMS | ❌ FAILED | Job entity complexities - deferred for future work |
| TMF629 Customer Management v5 | DCMMS | ✅ SUCCESS | Simple CRUD, no AclRelatedParty in OAS - new fix pattern |
| TMF666 Account Management v5 | DACMS | ✅ SUCCESS | 7 entities, polymorphic (Table Per Concrete Class pattern) |

**Files created:**

- `temp/code-generator/work-instructions.md` - Living documentation of successful generation steps
- `temp/code-generator/generated.dttms/` - Complete DTTMS code (model + API, compiles clean)
- `temp/code-generator/generated.dcmms/` - Complete DCMMS code (model + API)
- `temp/code-generator/generated.dacms/` - Complete DACMS code (7 entities with polymorphism)
- `temp/experiment-results/dpcms-generation-report.md` - Documented TMF620 Job entity issues
- `temp/experiment-results/dcmms-generation-report.md` - DCMMS success with AclRelatedParty handling
- `temp/experiment-results/dacms-generation-report.md` - DACMS success with 154 polymorphic patterns
- `temp/experiment-results/dttms-generated-vs-live-comparison.md` - Code comparison analysis (13.4KB)
- `temp/experiment-results/polymorphic-mapping-issue-report.md` - **Critical issue report (55.6KB, 1450+ lines)**

**Repository clones:**

- `temp/code-generator/` - api-generator:feature/PRND-34393
- `temp/work/trouble-ticket/` - dnext-technology/trouble-ticket:develop
- `temp/work/trouble-ticket-model/` - dnext-technology/trouble-ticket-model:develop

**Commits:**

```
(No commits - this was experimental validation and documentation work)
```

### Session Evolution

**Phase 1: Failed Attempts (21:08 - 01:28, 4h 20m)**
- Cloned wrong generator version (main/master branch)
- Attempted manual fixes and recipe-based generation
- Multiple configuration mismatches
- **Outcome**: Acknowledged failure, started fresh

**Phase 2: Successful Generation (20:36 - 03:18, 6h 42m)**
- Located correct branch (feature/PRND-34393)
- Generated DTTMS successfully
- Created living documentation (work-instructions.md)
- Generated DPCMS - discovered Job complexities
- Generated DCMMS - discovered AclRelatedParty handling pattern
- Generated DACMS - validated polymorphic type handling
- Compared generated vs live code - discovered critical gap

### Key Discoveries

1. **Generator Branch Matters**: feature/PRND-34393 has critical fixes absent from main/master branch. Wrong branch caused 4+ hours of wasted effort.

2. **Three Distinct AclRelatedParty Handling Patterns**:
   - **DTTMS**: AclRelatedParty in OAS → Add field to base entity + use AclOwnershipUtil
   - **DCMMS**: AclRelatedParty NOT in OAS → Remove AclOwnershipUtil entirely
   - **DACMS**: AclRelatedParty NOT in OAS, 7 entities → Remove AclOwnershipUtil from all services

3. **Two Polymorphic Storage Strategies**:
   - **Single Table Inheritance** (TMF621): All types in one collection, discriminator field determines type
   - **Table Per Concrete Class** (TMF666): Each type in separate collection, separate endpoints per type

4. **TMF API Design Evolution** (v4 → v5):
   - **v4**: Polymorphic endpoints (e.g., `/resource` handles LogicalResource, PhysicalResource)
   - **v5**: Resource-centric microservices (e.g., `/logicalResource`, `/physicalResource` separate)
   - Philosophy shift: OOP polymorphism → Domain-driven microservices

5. **Critical Generator Gap - Polymorphic Dispatch Logic**:
   - **Generated mapper**: 40 lines, basic interface, no type discrimination
   - **Production mapper**: 196 lines, includes switch-based dispatch for polymorphic types
   - **Missing**: 156 lines of critical logic for handling subtypes
   - **Impact**: Subclass-specific fields would NOT be mapped → data loss in production
   - **Root cause**: Templates don't consume OAS discriminator metadata

### Lessons Learned

1. **Version control is critical for code generators** - Wrong branch cost 4+ hours. Branch naming with JIRA ticket numbers (PRND-34393) helps track which fixes are included.

2. **Living documentation beats static README** - work-instructions.md evolved as we discovered successful patterns. Each generation added new entries. Future AI sessions can follow this playbook.

3. **Comparative analysis reveals hidden deficiencies** - Generated code compiled and built successfully, appearing correct. Only by comparing with production code did we discover the missing polymorphic dispatch logic that would cause runtime data loss.

4. **OAS schema presence drives architecture** - Generator must check for schema presence (e.g., AclRelatedParty) and adapt code accordingly. Not all TMF APIs include extension schemas.

5. **Polymorphism requires discriminator-aware templates** - MapStruct cannot auto-generate correct polymorphic mapping without explicit switch logic based on discriminator field. Templates must parse oneOf/discriminator patterns from OAS.

6. **Storage pattern affects mapper strategy**:
   - Single Table Inheritance → Need switch-based dispatch in mapper
   - Table Per Concrete Class → Simple per-type mappers suffice

7. **Build success != production readiness** - All generated code compiled cleanly with only expected MapStruct warnings. Build tools cannot detect missing polymorphic dispatch logic - requires runtime testing or production code comparison.

8. **Manual enhancements create technical debt** - Live production code was manually enhanced with 156 lines of polymorphic dispatch after initial generation. This pattern wasn't fed back into generator templates, creating maintenance burden for future APIs.

9. **Experiment scaling reveals complexity tiers**:
   - Simple CRUD (DCMMS): 1 entity, no polymorphism, no jobs → Easy
   - Polymorphic CRUD (DTTMS, DACMS): 2-7 entities with inheritance → Medium (but missing features!)
   - Job-based APIs (DPCMS): Job entity complexities → Hard (deferred)

10. **Technical debt compounds across APIs** - With 20+ TMF APIs potentially needing generation, the 3-5 hours manual enhancement per polymorphic entity would total 26+ hours of repetitive work. Automating this in templates would provide massive ROI.

---

