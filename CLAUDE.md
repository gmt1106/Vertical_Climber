# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Environment

This is a Docker-based development environment running Node.js 20 with Claude Code CLI pre-installed.

### Docker Setup

- **Base image**: node:20-slim
- **Working directory**: /workspace
- **Pre-installed tools**: git, curl, vim
- **Claude Code**: Globally installed via `@anthropic-ai/claude-code`

## Getting Started

This workspace is currently empty. To begin development:

1. Clone or copy your codebase into `/workspace`
2. Install dependencies appropriate to your project
3. Update this CLAUDE.md with project-specific build commands, architecture, and development workflows

## Rule on How to Work on the Code

1. When I tell you my requirement, provide only analysis and proposed direction; do not output any concrete code
2. After the analysis is approved, output code only where changes are strictly necessary, and limit it to code-block, function, or handler level
3. Do not remove existing comments without explicit approval
4. Do not change indentation arbitrarily
5. Do not refactor any code unrelated to the review scope, not even a single line
6. If I submit code modified based on the AI’s suggestions, review whether the changes were applied correctly
7. After I test the modified code and share the results, analyze any remaining issues
8. Do not modify code based solely on assumptions (e.g., “it is likely that…”)
9. Before changing code based on an assumed root cause, first verify that the assumption is true (e.g., by inserting debug code), and only then apply the fix
