# 1. Use the lightweight Node image
FROM node:20-slim

# 2. Install everything: Git, Curl, Vim, and Claude Code
# We combine these to keep the image small and efficient
RUN apt-get update && apt-get install -y \
    git \
    curl \
    vim \
    && npm install -g @anthropic-ai/claude-code \
    && rm -rf /var/lib/apt/lists/*

# 3. Prepare Claude cache mount point for volumes
RUN mkdir -p /claude && ln -s /claude/.claude /root/.claude

# 6. Set the default folder to your project workspace
WORKDIR /workspace