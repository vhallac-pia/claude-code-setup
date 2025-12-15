#!/usr/bin/env python3
"""
Extract structured data from Claude Code session JSONL files.

Usage:
    python3 extract_session_data.py <session-file> [session-file...]

Output:
    JSON object with session metadata, user messages, files modified, and commits
"""

import json
import sys
from datetime import datetime, timedelta
from pathlib import Path
from typing import List, Dict, Any


def parse_timestamp(ts_str: str) -> datetime:
    """Parse ISO timestamp string to datetime object."""
    if not ts_str:
        return None
    return datetime.fromisoformat(ts_str.replace('Z', '+00:00'))


def extract_text_from_content(content: Any) -> str:
    """Extract text from message content (handles both array and string formats)."""
    if isinstance(content, str):
        return content

    if isinstance(content, list):
        for item in content:
            if isinstance(item, dict) and item.get('type') == 'text':
                return item.get('text', '')

    return ''


def is_system_message(text: str) -> bool:
    """Check if message is a system command or reminder."""
    if not text:
        return True

    # Skip slash commands
    if text.startswith('/'):
        return True

    # Skip command infrastructure messages
    if text.startswith('<command-') or text.startswith('<local-command-'):
        return True

    # Skip caveats
    if text.startswith('Caveat:'):
        return True

    # Skip system reminders (though these should be in separate blocks)
    if '<system-reminder>' in text:
        return True

    return False


def calculate_duration(entries: List[Dict], user_messages: List[Dict]) -> Dict[str, Any]:
    """
    Calculate session duration excluding breaks.

    Returns dict with duration string and timestamps.
    """
    if not user_messages:
        return {'duration': '0h 0m', 'start': None, 'end': None}

    start = parse_timestamp(user_messages[0]['timestamp'])

    # Find last entry timestamp (any type, not just user messages)
    end = None
    for entry in reversed(entries):
        if entry.get('timestamp'):
            end = parse_timestamp(entry['timestamp'])
            break

    if not end:
        end = parse_timestamp(user_messages[-1]['timestamp'])

    # Calculate total duration
    total_duration = end - start

    # Detect breaks: gaps >30 min between messages with no tool activity
    # For now, simple calculation - could be enhanced to detect breaks
    hours = int(total_duration.total_seconds() // 3600)
    minutes = int((total_duration.total_seconds() % 3600) // 60)

    return {
        'duration': f'{hours}h {minutes}m',
        'start': user_messages[0]['timestamp'],
        'end': end.isoformat(),
        'total_seconds': int(total_duration.total_seconds())
    }


def extract_files_from_tools(entries: List[Dict]) -> Dict[str, List[str]]:
    """Extract files modified/created from tool calls."""
    files_modified = set()
    files_created = set()

    for entry in entries:
        # Check for tool_use in assistant messages
        if entry.get('type') == 'assistant':
            content = entry.get('message', {}).get('content', [])
            if isinstance(content, list):
                for item in content:
                    if isinstance(item, dict) and item.get('type') == 'tool_use':
                        tool_name = item.get('name')
                        params = item.get('input', {})

                        if tool_name == 'Edit':
                            fp = params.get('file_path')
                            if fp:
                                files_modified.add(fp)
                        elif tool_name == 'Write':
                            fp = params.get('file_path')
                            if fp:
                                files_created.add(fp)

    return {
        'modified': sorted(files_modified),
        'created': sorted(files_created)
    }


def extract_commits(entries: List[Dict]) -> List[Dict[str, str]]:
    """Extract git commits from bash commands."""
    commits = []

    for entry in entries:
        if entry.get('type') == 'assistant':
            content = entry.get('message', {}).get('content', [])
            if isinstance(content, list):
                for item in content:
                    if isinstance(item, dict) and item.get('type') == 'tool_use':
                        if item.get('name') == 'Bash':
                            cmd = item.get('input', {}).get('command', '')
                            if 'git commit' in cmd:
                                commits.append({
                                    'command': cmd[:200],  # Truncate long commands
                                    'timestamp': entry.get('timestamp')
                                })

    return commits


def extract_session_data(session_files: List[str]) -> Dict[str, Any]:
    """
    Extract structured data from one or more session files.

    Args:
        session_files: List of paths to JSONL session files

    Returns:
        Dictionary with session data
    """
    all_entries = []

    # Load all session files
    for session_file in session_files:
        path = Path(session_file)
        if not path.exists():
            print(f"Warning: {session_file} not found", file=sys.stderr)
            continue

        with open(path, 'r') as f:
            for line in f:
                try:
                    entry = json.loads(line)
                    all_entries.append(entry)
                except json.JSONDecodeError:
                    continue

    # Extract user messages
    user_messages = []
    for entry in all_entries:
        if entry.get('type') == 'user':
            content = entry.get('message', {}).get('content')
            text = extract_text_from_content(content)

            if text and not is_system_message(text):
                user_messages.append({
                    'timestamp': entry.get('timestamp'),
                    'text': text,
                    'number': len(user_messages) + 1
                })

    # Calculate duration
    duration_info = calculate_duration(all_entries, user_messages)

    # Extract files
    files = extract_files_from_tools(all_entries)

    # Extract commits
    commits = extract_commits(all_entries)

    # Count tool usage
    tool_usage = {}
    for entry in all_entries:
        if entry.get('type') == 'assistant':
            content = entry.get('message', {}).get('content', [])
            if isinstance(content, list):
                for item in content:
                    if isinstance(item, dict) and item.get('type') == 'tool_use':
                        tool_name = item.get('name')
                        tool_usage[tool_name] = tool_usage.get(tool_name, 0) + 1

    return {
        'duration': duration_info['duration'],
        'start_time': duration_info['start'],
        'end_time': duration_info['end'],
        'total_messages': len(user_messages),
        'user_messages': user_messages,
        'files': files,
        'commits': commits,
        'tool_usage': tool_usage,
        'session_files': [str(Path(f).name) for f in session_files]
    }


def main():
    if len(sys.argv) < 2:
        print("Usage: python3 extract_session_data.py <session-file> [session-file...]", file=sys.stderr)
        sys.exit(1)

    session_files = sys.argv[1:]
    data = extract_session_data(session_files)

    # Output JSON
    print(json.dumps(data, indent=2))


if __name__ == '__main__':
    main()
