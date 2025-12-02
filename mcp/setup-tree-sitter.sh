#!/bin/bash
# Setup script for tree-sitter MCP server
# Creates a Python virtual environment with mcp-server-tree-sitter installed

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
VENV_DIR="${SCRIPT_DIR}/tree-sitter-env"

echo "Creating virtual environment at ${VENV_DIR}..."
python3 -m venv "${VENV_DIR}"

echo "Installing mcp-server-tree-sitter..."
"${VENV_DIR}/bin/pip" install --upgrade pip
"${VENV_DIR}/bin/pip" install -r "${SCRIPT_DIR}/requirements-tree-sitter.txt"

echo "Done. Tree-sitter MCP server installed at:"
echo "  ${VENV_DIR}/bin/python3 -m mcp_server_tree_sitter.server"
