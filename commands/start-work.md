# Start Work Session Marker

Mark the current session as the start of a new logical work session for work log extraction.

## Instructions

1. **Identify the current session file**:
   - Convert the current working directory to the project path format used in `~/.claude/projects/`
   - Path format: `-` prefix + path with `/` replaced by `-` (e.g., `/home/vedat/work/project` → `-home-vedat-work-project`)
   - Find the most recently modified `.jsonl` file (excluding `agent-*.jsonl` files)

2. **Create a session marker file**:
   - Create `~/.claude/projects/<project-path>/session-start-marker.txt`
   - Write the current session file name and timestamp to it
   - Format: `<session-uuid>.jsonl|<ISO-timestamp>`

3. **Confirm to user**:
   - Report the session file identified
   - Report the marker file created
   - Explain that `/extract-work-log` will use this as the session boundary

## Example Output

```
Work session started.
Session file: 20f26e94-957d-41d3-919f-cf2d11f3518f.jsonl
Marker created: ~/.claude/projects/-home-vedat-work-project/session-start-marker.txt

Use /extract-work-log at the end of your session to generate a work log entry.
```
