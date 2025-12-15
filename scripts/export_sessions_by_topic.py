#!/usr/bin/env python3
"""
Export Claude Code conversation sessions filtered by topic/keywords to HTML
Usage: python3 export_sessions_by_topic.py <keywords> <output_file> [--project <project_path>] [--include-current]
Example: python3 export_sessions_by_topic.py "authentication,security" auth-sessions.html
"""

import json
import os
import sys
from collections import defaultdict
from datetime import datetime
import html
import re

def print_usage():
    print(__doc__)
    sys.exit(1)

# Parse arguments
if len(sys.argv) < 3:
    print_usage()

keywords_arg = sys.argv[1]
output_file = sys.argv[2]
keywords = [k.strip().lower() for k in keywords_arg.split(',')]

# Optional arguments
project_path = '/home/vedat/t/atlassian'
include_current = False

i = 3
while i < len(sys.argv):
    if sys.argv[i] == '--project' and i + 1 < len(sys.argv):
        project_path = sys.argv[i + 1]
        i += 2
    elif sys.argv[i] == '--include-current':
        include_current = True
        i += 1
    else:
        i += 1

print(f"Searching for sessions with keywords: {', '.join(keywords)}")
print(f"Project: {project_path}")
print(f"Include current session: {include_current}")

# Paths
history_file = os.path.expanduser("~/.claude/history.jsonl")
project_name = project_path.replace('/', '-')
sessions_dir = os.path.expanduser(f"~/.claude/projects/{project_name}/")

if not os.path.exists(sessions_dir):
    print(f"Error: Sessions directory not found: {sessions_dir}")
    sys.exit(1)

# Read history to find matching sessions
sessions_meta = {}
with open(history_file, 'r') as f:
    for line in f:
        try:
            entry = json.loads(line)
            if entry.get('project') == project_path:
                session_id = entry.get('sessionId')
                if session_id and session_id not in sessions_meta:
                    sessions_meta[session_id] = {
                        'first_query': entry.get('display', ''),
                        'timestamp': entry.get('timestamp', 0)
                    }
        except:
            pass

# Find sessions matching keywords
matching_session_ids = set()
for session_id, meta in sessions_meta.items():
    query = meta['first_query'].lower()
    if any(kw in query for kw in keywords):
        matching_session_ids.add(session_id)

# Also search within conversation content for better matching
for session_id in sessions_meta.keys():
    if session_id in matching_session_ids:
        continue

    conv_file = os.path.join(sessions_dir, f"{session_id}.jsonl")
    if not os.path.exists(conv_file):
        continue

    try:
        with open(conv_file, 'r') as f:
            content = f.read().lower()
            if any(kw in content for kw in keywords):
                matching_session_ids.add(session_id)
    except:
        pass

print(f"Found {len(matching_session_ids)} matching sessions")

# Load full conversations
conversations = {}
for session_id in matching_session_ids:
    conv_file = os.path.join(sessions_dir, f"{session_id}.jsonl")
    if not os.path.exists(conv_file):
        continue

    messages = []
    with open(conv_file, 'r') as f:
        for line in f:
            try:
                msg = json.loads(line)
                if msg.get('type') in ['user', 'assistant']:
                    messages.append(msg)
            except:
                pass

    if messages:
        conversations[session_id] = messages

# Sort sessions by timestamp
sorted_sessions = sorted(
    conversations.items(),
    key=lambda x: sessions_meta.get(x[0], {}).get('timestamp', 0)
)

def extract_text_from_content(content, check_tools=False):
    """Extract text from message content"""
    has_tools = False
    if isinstance(content, str):
        return content, has_tools
    if isinstance(content, list):
        texts = []
        for item in content:
            if isinstance(item, dict):
                if item.get('type') == 'text':
                    texts.append(item.get('text', ''))
                elif item.get('type') == 'tool_use':
                    has_tools = True
                    continue
            elif isinstance(item, str):
                texts.append(item)
        return ' '.join(texts), has_tools
    return '', has_tools

def clean_user_message(text):
    """Clean up user messages"""
    if '[Request interrupted by user]' in text or 'Request interrupted by user' in text:
        return 'INTERRUPTED'

    # Filter out session continuation messages
    if 'This session is being continued from a previous conversation' in text:
        return 'SESSION_CONTINUATION'

    text = re.sub(r'<command-name>.*?</command-name>', '', text)
    text = re.sub(r'<command-message>.*?</command-message>', '', text)
    text = re.sub(r'<command-args>.*?</command-args>', '', text)
    text = re.sub(r'<local-command-stdout>.*?</local-command-stdout>', '', text, flags=re.DOTALL)
    text = re.sub(r'<user-memory-input>(.*?)</user-memory-input>', r'\1', text)
    text = re.sub(r'Caveat:.*?user explicitly asks you to\.', '', text, flags=re.DOTALL)
    text = re.sub(r'<system-reminder>.*?</system-reminder>', '', text, flags=re.DOTALL)
    return text.strip()

def generate_session_summary(messages, session_id):
    """Generate a brief summary of what was achieved in the session"""
    user_queries = []

    for msg in messages:
        msg_content = msg.get('message', {})
        content = msg_content.get('content', '')
        text, _ = extract_text_from_content(content)
        text = clean_user_message(text)

        if not text or text.startswith('/') or 'Caveat:' in text or text == 'INTERRUPTED' or text == 'SESSION_CONTINUATION':
            continue

        if msg.get('type') == 'user':
            user_queries.append(text)

    if not user_queries:
        return "Session details not available."

    # Detect topics from keywords
    topics = []
    all_text = ' '.join(user_queries).lower()
    for kw in keywords:
        if kw in all_text:
            topics.append(kw)

    summary = f"This session covered {', '.join(topics) if topics else 'related topics'} "
    summary += f"through {len(user_queries)} user {'query' if len(user_queries) == 1 else 'queries'}."

    return summary

# Generate HTML with same template as original
html_content = """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Session Export - """ + ', '.join(keywords) + """</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
            line-height: 1.6;
            color: #333;
            background: #f5f5f5;
        }

        .container {
            max-width: 1400px;
            margin: 0 auto;
            background: white;
            box-shadow: 0 0 20px rgba(0,0,0,0.1);
        }

        header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 50px 40px;
            text-align: center;
        }

        header h1 {
            font-size: 2.5em;
            margin-bottom: 10px;
        }

        header p {
            font-size: 1.2em;
            opacity: 0.9;
        }

        nav {
            background: #f8f9fa;
            padding: 40px;
            border-bottom: 2px solid #e9ecef;
        }

        nav h2 {
            color: #495057;
            margin-bottom: 30px;
            font-size: 1.8em;
        }

        .toc-session {
            margin-bottom: 30px;
            background: white;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }

        .toc-session-title {
            font-size: 1.2em;
            font-weight: bold;
            color: #667eea;
            margin-bottom: 15px;
            cursor: pointer;
            display: flex;
            align-items: center;
        }

        .toc-session-title:before {
            content: '';
            display: inline-block;
            width: 6px;
            height: 6px;
            background: #667eea;
            border-radius: 50%;
            margin-right: 12px;
        }

        .toc-queries {
            list-style: none;
            padding-left: 25px;
        }

        .toc-queries li {
            margin: 8px 0;
            padding-left: 20px;
            position: relative;
        }

        .toc-queries li:before {
            content: '→';
            position: absolute;
            left: 0;
            color: #667eea;
        }

        .toc-queries a {
            color: #495057;
            text-decoration: none;
            transition: all 0.2s;
        }

        .toc-queries a:hover {
            color: #667eea;
            text-decoration: underline;
        }

        .content {
            padding: 40px;
        }

        .stats {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            padding: 30px;
            background: #f8f9fa;
            border-radius: 8px;
            margin: 30px 0;
        }

        .stat-item {
            text-align: center;
            padding: 20px;
            background: white;
            border-radius: 8px;
        }

        .stat-number {
            font-size: 2.5em;
            font-weight: bold;
            color: #667eea;
        }

        .stat-label {
            color: #6c757d;
            font-size: 0.9em;
            margin-top: 5px;
        }

        .session {
            margin-bottom: 80px;
            page-break-inside: avoid;
        }

        .session-header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 30px;
            border-radius: 8px;
            margin-bottom: 30px;
        }

        .session-header h2 {
            font-size: 2em;
            margin-bottom: 15px;
        }

        .session-meta {
            font-size: 0.95em;
            opacity: 0.9;
            margin-bottom: 15px;
        }

        .session-summary {
            background: rgba(255, 255, 255, 0.1);
            padding: 15px;
            border-radius: 4px;
            margin-top: 15px;
            font-size: 1.05em;
            line-height: 1.7;
        }

        .conversation {
            margin-top: 30px;
        }

        .message {
            margin: 25px 0;
            padding: 25px;
            border-radius: 8px;
            border-left: 4px solid;
            page-break-inside: avoid;
        }

        .message.user {
            background: #fff3cd;
            border-left-color: #ffc107;
        }

        .message.assistant {
            background: #d1ecf1;
            border-left-color: #0c5460;
        }

        .message.interrupted {
            background: #f8d7da;
            border-left-color: #dc3545;
        }

        .message.interrupted .message-label {
            color: #721c24;
        }

        .message-label {
            font-weight: bold;
            color: #495057;
            margin-bottom: 12px;
            font-size: 0.9em;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .message.user .message-label {
            color: #856404;
        }

        .message.assistant .message-label {
            color: #0c5460;
        }

        .message-text {
            color: #212529;
            white-space: pre-wrap;
            word-wrap: break-word;
            font-size: 1.05em;
            line-height: 1.8;
        }

        .timestamp {
            color: #6c757d;
            font-size: 0.85em;
            margin-top: 12px;
            font-style: italic;
        }

        footer {
            background: #343a40;
            color: white;
            padding: 40px;
            text-align: center;
        }

        footer p {
            opacity: 0.8;
            margin: 5px 0;
        }

        @media print {
            body {
                background: white;
            }

            .container {
                box-shadow: none;
            }

            .session {
                page-break-after: always;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <header>
            <h1>Session Export</h1>
            <p>Filtered by: """ + ', '.join(keywords) + """</p>
        </header>

        <nav>
            <h2>Table of Contents</h2>
"""

# Generate TOC
toc_data = []
query_counter = defaultdict(int)

for idx, (session_id, messages) in enumerate(sorted_sessions, 1):
    meta = sessions_meta.get(session_id, {})
    first_msg = meta.get('first_query', 'Session')[:80]
    timestamp = meta.get('timestamp', 0)
    date_str = datetime.fromtimestamp(timestamp/1000).strftime('%Y-%m-%d %H:%M') if timestamp else 'Unknown'

    first_msg = clean_user_message(first_msg)
    if first_msg.startswith('#'):
        first_msg = first_msg[1:].strip()
    if not first_msg or first_msg.startswith('/') or first_msg == 'INTERRUPTED' or first_msg == 'SESSION_CONTINUATION':
        for msg in messages:
            if msg.get('type') == 'user':
                msg_content = msg.get('message', {})
                content = msg_content.get('content', '')
                text, _ = extract_text_from_content(content)
                text = clean_user_message(text)
                if text and not text.startswith('/') and text != 'INTERRUPTED' and text != 'SESSION_CONTINUATION':
                    first_msg = text[:80]
                    break

    queries = []
    for msg in messages:
        if msg.get('type') == 'user':
            msg_content = msg.get('message', {})
            content = msg_content.get('content', '')
            text, _ = extract_text_from_content(content)
            text = clean_user_message(text)
            if text and not text.startswith('/') and len(text) > 10 and text != 'INTERRUPTED' and text != 'SESSION_CONTINUATION':
                query_counter[session_id] += 1
                queries.append((query_counter[session_id], text[:100]))

    toc_data.append((idx, session_id, first_msg, date_str, queries))

    html_content += f"""
            <div class="toc-session">
                <div class="toc-session-title">
                    <a href="#session-{idx}">Session {idx}: {html.escape(first_msg)}</a>
                </div>
                <div style="font-size: 0.9em; color: #6c757d; margin-bottom: 10px;">{date_str} | {len(queries)} queries</div>
                <ul class="toc-queries">
"""

    for q_num, query_text in queries[:10]:
        html_content += f'                    <li><a href="#session-{idx}-q{q_num}">{html.escape(query_text)}</a></li>\n'

    if len(queries) > 10:
        html_content += f'                    <li><em>... and {len(queries) - 10} more queries</em></li>\n'

    html_content += """                </ul>
            </div>
"""

html_content += """        </nav>

        <div class="content">
"""

# Add statistics
total_messages = sum(len(messages) for _, messages in sorted_sessions)
total_queries = sum(len(q[4]) for q in toc_data)

html_content += f"""
            <div class="stats">
                <div class="stat-item">
                    <div class="stat-number">{len(sorted_sessions)}</div>
                    <div class="stat-label">Sessions</div>
                </div>
                <div class="stat-item">
                    <div class="stat-number">{total_queries}</div>
                    <div class="stat-label">User Queries</div>
                </div>
                <div class="stat-item">
                    <div class="stat-number">{total_messages}</div>
                    <div class="stat-label">Total Messages</div>
                </div>
                <div class="stat-item">
                    <div class="stat-number">{total_queries // len(sorted_sessions) if sorted_sessions else 0}</div>
                    <div class="stat-label">Avg Queries/Session</div>
                </div>
            </div>
"""

# Add full conversations
for idx, (session_id, messages) in enumerate(sorted_sessions, 1):
    meta = sessions_meta.get(session_id, {})
    timestamp = meta.get('timestamp', 0)
    date_str = datetime.fromtimestamp(timestamp/1000).strftime('%Y-%m-%d %H:%M:%S') if timestamp else 'Unknown'

    first_msg = meta.get('first_query', 'Session')[:100]
    first_msg = clean_user_message(first_msg)
    if first_msg.startswith('#'):
        first_msg = first_msg[1:].strip()

    summary = generate_session_summary(messages, session_id)

    html_content += f"""
            <div class="session" id="session-{idx}">
                <div class="session-header">
                    <h2>Session {idx}: {html.escape(first_msg)}</h2>
                    <div class="session-meta">
                        Date: {date_str} | Session ID: {session_id}
                    </div>
                    <div class="session-summary">
                        <strong>Summary:</strong> {html.escape(summary)}
                    </div>
                </div>
                <div class="conversation">
"""

    # Add messages - merge consecutive assistant responses
    query_num = 0
    i = 0
    while i < len(messages):
        msg = messages[i]
        msg_type = msg.get('type')

        if msg_type not in ['user', 'assistant']:
            i += 1
            continue

        msg_content = msg.get('message', {})
        content = msg_content.get('content', '')
        text, has_tools = extract_text_from_content(content)
        text = clean_user_message(text)

        if text == 'INTERRUPTED':
            html_content += """
                    <div class="message interrupted">
                        <div class="message-label">⚠ Interrupted</div>
                        <div class="message-text">Request interrupted by user</div>
                    </div>
"""
            i += 1
            continue

        if not text or text.startswith('/clear') or text.startswith('/login') or text.startswith('/mcp') or text == 'SESSION_CONTINUATION':
            i += 1
            continue

        is_user = msg_type == 'user'

        if is_user:
            msg_timestamp = msg.get('timestamp', '')
            if msg_timestamp:
                try:
                    time_str = datetime.fromisoformat(msg_timestamp.replace('Z', '+00:00')).strftime('%H:%M:%S')
                except:
                    time_str = ''
            else:
                time_str = ''

            query_num += 1
            anchor = f' id="session-{idx}-q{query_num}"' if len(text) > 10 else ''

            html_content += f"""
                    <div class="message user"{anchor}>
                        <div class="message-label">User Query</div>
                        <div class="message-text">{html.escape(text)}</div>
                        <div class="timestamp">{time_str}</div>
                    </div>
"""
            i += 1
        else:
            merged_texts = []
            used_tools = False
            first_timestamp = msg.get('timestamp', '')

            while i < len(messages) and messages[i].get('type') == 'assistant':
                msg_content = messages[i].get('message', {})
                content = msg_content.get('content', '')
                text, has_tools = extract_text_from_content(content)
                text = clean_user_message(text)

                if text and not text.startswith('/'):
                    merged_texts.append(text)
                if has_tools:
                    used_tools = True
                i += 1

            if merged_texts:
                if first_timestamp:
                    try:
                        time_str = datetime.fromisoformat(first_timestamp.replace('Z', '+00:00')).strftime('%H:%M:%S')
                    except:
                        time_str = ''
                else:
                    time_str = ''

                combined_text = '\n\n'.join(merged_texts)
                if used_tools and '<using tool' not in combined_text.lower():
                    combined_text = '<using tools>\n\n' + combined_text

                html_content += f"""
                    <div class="message assistant">
                        <div class="message-label">Assistant Response</div>
                        <div class="message-text">{html.escape(combined_text)}</div>
                        <div class="timestamp">{time_str}</div>
                    </div>
"""

    html_content += """
                </div>
            </div>
"""

html_content += """
        </div>

        <footer>
            <p>Generated from Claude Code conversation history</p>
            <p>Keywords: """ + ', '.join(keywords) + """</p>
            <p>Export Date: """ + datetime.now().strftime('%Y-%m-%d %H:%M:%S') + """</p>
        </footer>
    </div>
</body>
</html>
"""

# Make output path absolute if relative
if not os.path.isabs(output_file):
    output_file = os.path.abspath(output_file)

# Write HTML file
with open(output_file, 'w', encoding='utf-8') as f:
    f.write(html_content)

print(f"\n✓ Session export created: {output_file}")
print(f"✓ Extracted {len(sorted_sessions)} sessions")
print(f"✓ Total queries: {total_queries}")
print(f"✓ Total messages: {total_messages}")
print("\nSession Overview:")
for idx, sid, first_msg, date_str, queries in toc_data:
    print(f"  {idx}. [{date_str}] {first_msg[:70]}... ({len(queries)} queries)")
