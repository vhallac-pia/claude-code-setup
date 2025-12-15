---
description: Export conversation sessions filtered by topic keywords to HTML
---

I need you to export conversation sessions from my Claude Code history to a well-formatted HTML file.

Use the script at `~/.claude/scripts/export_sessions_by_topic.py` to perform the export.

**Ask me for:**
1. **Topic/keywords** to filter sessions (comma-separated, e.g., "authentication,security,login")
2. **Output filename** (will be saved in current directory, e.g., "auth-sessions.html")
3. **Optional:** Custom project path (default is current project)

**Then run:**
```bash
python3 ~/.claude/scripts/export_sessions_by_topic.py "<keywords>" "<output_file>" --project "<project_path>"
```

**After running, report:**
- Number of sessions found
- Total queries extracted
- Output file location with full path

**Example:**
```bash
python3 ~/.claude/scripts/export_sessions_by_topic.py "authentication,oauth,login" auth-sessions.html --project /home/vedat/t/myproject
```
