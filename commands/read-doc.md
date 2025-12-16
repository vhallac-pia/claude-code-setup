Read the document at $ARGUMENTS and build a working mental map for yourself:

- Section/heading hierarchy
- Line number ranges for each section
- Brief note on each section's purpose (few words)
- Cross-references or dependencies between sections

Don't output this map — keep it as internal context. When I ask you to find, edit, or reference parts of this document, use your map to:
1. Go directly to the relevant line range
2. Check if edits affect other sections
3. Update your map if structure changes

Confirm when ready with a one-line summary of the document's overall structure.

## Usage
```
/read-doc [documents]
```

**Parameters:**
- `documents`: Required. A list of documents to read. This parameter is either a full path, or a prompt that identifies a list of files.
