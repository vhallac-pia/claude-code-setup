# Start Feature Command

Initialize a new epic/feature folder and guide SRS extraction from Confluence.

## Usage

```
/start-feature <epic-id> [slug]
```

**Parameters:**
- `epic-id`: Required. The JIRA epic ID (e.g., PRND-12345)
- `slug`: Optional. Short name for the folder (auto-generated from epic title if not provided)

**Examples:**
```
/start-feature PRND-12345
/start-feature PRND-12345 trouble-ticket-api
```

## What This Does

### 1. Validates Epic

- Connects to JIRA via Atlassian MCP
- Fetches epic details (title, description, status)
- Confirms epic exists and is in appropriate state
- Extracts linked Confluence SRS page URL if available

### 2. Creates Epic Folder Structure

Creates folder in `dnext-dev-support/epics/`:

```
dnext-dev-support/epics/PRND-{epic-id}-{slug}/
├── srs-extract.md           # SRS content with metadata front matter
└── feature-design.md        # Only if multi-module epic
```

### 3. Populates Initial Files

Files are created from templates in `dnext-dev-support/templates/`:

| File | Template |
|------|----------|
| `srs-extract.md` | `srs-extract.template.md` |
| `feature-design.md` | `feature-design.template.md` (if multi-module) |

YAML front matter is populated with epic-specific values (ID, title, URLs, date).

### 4. Guides SRS Extraction

Provides step-by-step instructions:

```
Epic folder created: dnext-dev-support/epics/PRND-12345-trouble-ticket-api/

Next steps to extract SRS:

1. Open Confluence SRS:
   {confluence-url}

2. Export to Word:
   - Click "..." menu (top right)
   - Select "Export" > "Export to Word"
   - Save the .docx file

3. Convert to Markdown:
   pandoc -f docx -t markdown -o srs-content.md "{downloaded-file}.docx"

4. Replace content in srs-extract.md:
   - Keep the YAML front matter (---)
   - Replace everything after with converted content

5. Clean up formatting:
   - Review headers and structure
   - Fix tables if needed
   - Ensure Given/When/Then scenarios are properly formatted

6. When ready, run:
   /groom-stories PRND-12345

Ready to proceed with SRS extraction?
```

## Multi-Module Detection

If epic description or linked issues suggest multiple modules:

```
Detected potential multi-module epic:
- References: DPOMS, DPCI, common-core

This will require a feature-design.md for cross-module coordination.
Created placeholder: feature-design.md

After SRS extraction, consider:
1. Identify module boundaries
2. Document cross-module APIs
3. Define integration sequence
```

## Output Format

### Success

```
/start-feature PRND-12345

Fetching epic from JIRA...
Epic: "Trouble Ticket API Enhancement"
Status: In Progress
SRS: https://dnext-technology.atlassian.net/wiki/spaces/...

Creating folder structure...
  dnext-dev-support/epics/PRND-12345-trouble-ticket-api/
  └── srs-extract.md

Epic folder initialized.

Next: Extract SRS from Confluence
      See instructions in srs-extract.md

When SRS is ready: /groom-stories PRND-12345
```

### Epic Not Found

```
/start-feature PRND-99999

Error: Epic PRND-99999 not found in JIRA.

Please verify:
- Epic ID is correct
- You have access to the epic
- Epic exists in D-NEXT project
```

### Folder Already Exists

```
/start-feature PRND-12345

Warning: Folder already exists:
  dnext-dev-support/epics/PRND-12345-trouble-ticket-api/

Options:
1. Continue (update srs-extract.md front matter with latest JIRA info)
2. Abort (keep existing folder unchanged)

Choice [1/2]:
```

## Agent Used

This command invokes the **feature-starter** agent which:
- Connects to Atlassian MCP for JIRA/Confluence data
- Creates folder structure
- Generates initial files from templates
- Provides extraction guidance

## Prerequisites

- Atlassian MCP configured and authenticated
- Write access to dnext-dev-support repository
- Epic exists in JIRA with appropriate status

## See Also

- `/groom-stories` - Create story breakdown after SRS extraction
- `/sync-jira` - Push groomed stories to JIRA
- `feature-starter` agent - Underlying implementation
