# Phase 5 Complete

## Overview

Phase 5 adds moving platform behavior, spiked platforms (obstacles), distance milestones, and verifies pause/resume functionality to complete core game mechanics.

## Features Implemented

### 1. Moving Platforms (Conveyor Belt)

**Files changed:** `Platform.kt`, `GameEngine.kt`, `Constants.kt`

- MOVING platforms slide left continuously at `MOVING_PLATFORM_SPEED` (80 px/s)
- Alternating ASCII art animation between `PLATFORM_MOVING_1` and `PLATFORM_MOVING_2` every 0.3s
- When goat stands on a MOVING platform (READY/AIMING state):
  - Goat is carried left at the platform's speed
  - Screen boundary clamping applied (goat can't slide off screen edges)
  - If goat's feet no longer overlap the platform horizontally, goat falls off (transitions to JUMPING with small downward velocity)
- 20% chance (`MOVING_PLATFORM_CHANCE`) for newly generated platforms to be MOVING type

### 2. Spiked Platforms (Obstacles)

**Files changed:** `Platform. kt`, `AsciiArt.kt`, `EntityManager.kt`, `GameEngine.kt`, `Constants.kt`

- Spikes are a boolean property on Platform (`hasSpikes`), not a separate entity
- Visual: `/\` pattern repeated across the platform width, rendered as an extra row on top of the normal platform art
- Landing on a spiked platform = instant game over
- 15% chance (`SPIKE_PLATFORM_CHANCE`) on NORMAL platforms only (MOVING platforms never have spikes)
- Never two spiked platforms in a row (`lastGeneratedSpiked` flag ensures a safe landing option)
- `OBSTACLE_SPIKE` constant added to `AsciiArt.kt`: `/\`

### 3. Distance Milestones

**Files changed:** `GameEngine.kt`, `AsciiRenderer.kt`

- Every 50m triggers a milestone display (e.g. "50m!", "100m!", "150m!")
- Milestone text shown centered below the distance counter for 2 seconds
- Tracked via `lastMilestone` (integer milestone count) and `milestoneTimer` (countdown) in GameEngine
- `AsciiRenderer.render()` accepts optional `milestoneText` parameter, rendered in `renderUI()`

### 4. Pause/Resume

- `pause()` and `resume()` methods already existed from prior phases
- Verified to work correctly with new features (auto-scroll stops when paused, moving platforms freeze)

## Constants Added

| Constant                      | Value | Description                                   |
|-------------------------------|-------|-----------------------------------------------|
| `MOVING_PLATFORM_SPEED`       | 80f   | Pixels per second sliding left                |
| `PLATFORM_ANIMATION_INTERVAL` | 0.3f  | Seconds between conveyor art frame toggles    |
| `MOVING_PLATFORM_CHANCE`      | 0.2f  | 20% chance to spawn a MOVING platform         |
| `SPIKE_PLATFORM_CHANCE`       | 0.15f | 15% chance to spawn spikes on NORMAL platform |

## New Fields in GameEngine

| Field                 | Type        | Description                                            |
|-----------------------|-------------|--------------------------------------------------------|
| `currentPlatform`     | `Platform?` | Platform the goat is currently standing on             |
| `lastMilestone`       | `Int`       | Last milestone count reached (1 = 50m, 2 = 100m, etc.) |
| `milestoneTimer`      | `Float`     | Countdown timer for milestone display (starts at 2s)   |
| `lastGeneratedSpiked` | `Boolean`   | Whether the last generated platform had spikes         |

## Key Methods Added/Modified

- **`GameEngine.updateMovingPlatformCarry(deltaTime)`** — Carries goat on MOVING platform, detects fall-off
- **`GameEngine.checkCollisions()`** — Added spike death check and milestone detection on landing
- **`GameEngine.generatePlatformsIfNeeded()`** — Random MOVING type and spike assignment
- **`Platform.init()`** — Accepts `spikes` parameter
- **`Platform.getAsciiRepresentation()`** — Prepends spike row when `hasSpikes` is true
- **`Platform.updateAnimation()`** — Toggles between conveyor art frames
- **`AsciiRenderer.render()`** — Added `milestoneText` parameter
- **`AsciiRenderer.renderUI()`** — Renders milestone text when present
- **`EntityManager.createPlatform()`** — Accepts `hasSpikes` parameter
