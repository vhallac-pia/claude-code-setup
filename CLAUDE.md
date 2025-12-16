# D-NEXT AI-Assisted Development - Claude Code Instructions

## Git Operations

- `GITHUB_TOKEN` env var contains API token for remote operations (push, pull, clone)
- When modifying CLAUDE.md, value brevity - minimal words, maximum clarity

## Commit Messages

- First line: 50-70 chars, focus on what and why
- MUST keep commits atomic, grouped by purpose
- MUST separate: story/spec commits from code commits, refactor from usage
- MUST check if there are modified files in repo before attempting a commit
- Describe what IS in the commit, not what WILL BE done
- MUST NOT add "generated with claude code" or similar to commit messages

## Code Quality

- MUST use design.md and srs-extract.md before code when reasoning
- MUST compile cleanly: no errors, no warnings
- MUST use merge-readiness-checker agent before merge instructions/actions
- MUST do clean build and test for final verification before branch merge
- MUST update module design.md when story marked DONE (before PR)

## Markdown Formatting

- MUST add a blank line before lists, code blocks, and tables

## Branch Rules

- MUST create branch only for JIRA stories (PRND-xxxxx), NOT for epics
- Branch format: `feature/PRND-xxxxx` or `bugfix/PRND-xxxxx`
- MUST NOT create separate branches for child stories (#1, #2) or review items (#1.R1)
- MUST NOT create branches in dnext-dev-support repo (documentation only)

# Reference Management

- Skills: self-contained in `~/.claude/skills/{skill}/` - embed all reference docs within
- Other components: use `~/.claude/references/{component}/` when >3 docs
- Never reference files outside ~/.claude
