# Phase 7.5: Collision Debug & Alignment Fixes - Complete

## Debug Collision Boxes

### 1. Debug Rendering System ✅
- Added `DEBUG_SHOW_COLLISION_BOXES` flag in `Constants.kt`
- Red stroke rectangles for player collision box, green for platforms
- Debug bounds passed from `GameEngine` to `AsciiRenderer.render()` and drawn in world-to-screen coordinates
- Set flag to `false` when done testing

## Collision Alignment Fixes

### 2. Platform Collision Box from ASCII Art ✅
- Platform `getBounds()` now derives width from actual ASCII art character count instead of the old `platformWidth` parameter
- Top shifted up by `CHAR_HEIGHT` to account for `drawText` baseline offset (text renders above the y coordinate)

### 3. Player Collision Box Baseline Fix ✅
- Player `getBounds()` top adjusted for `drawText` baseline offset
- `collisionOffsetX` positions the box at the feet's leading space position in the idle art

### 4. Landing Position Fix ✅
- Renamed `getCollisionPoint()` to `getLandingPositionY()` with updated description
- Formula: `(platform.position.y - CHAR_HEIGHT) - player.height` — places goat so feet sit on platform's visible top
- Ground landing positions in `GameEngine` updated to match

### 5. Removed Unused `platformWidth` ✅
- Removed `platformWidth` parameter from `Platform` constructor and `init()`
- Removed `width` parameter from `EntityManager.createPlatform()`
- Removed `PLATFORM_MIN_WIDTH`, `PLATFORM_MAX_WIDTH`, `PLATFORM_HEIGHT` constants
- Platform X positioning now uses `Platform.getMaxArtWidth(type)` instead of random width
- Moving platform fall-off-edge check uses `platform.getBounds()` instead of `platform.width`

### 6. Runtime Character Width Measurement ✅
- `CHAR_WIDTH` changed from `const val` to `var`, measured at runtime via `textPaint.measureText("X")`
- Added `CHAR_WIDTH_BLOCK` for block element characters (`░▒▓█`), measured via `textPaint.measureText("▓")`
- `AsciiRenderer` init sets both values before any entities are created
- Platform `getBounds()` uses `CHAR_WIDTH_BLOCK` for NORMAL platforms, `CHAR_WIDTH` for MOVING platforms
- Player collision uses `CHAR_WIDTH` (ASCII art)

### 7. Dynamic Platform Art Width ✅
- Added `Platform.getMaxArtWidth(type)` companion method — returns max art width in pixels per platform type
- Added `Platform.getArtCharWidth()` instance method — returns character count of current platform's art
- `GameEngine` uses `getMaxArtWidth(type)` for platform X positioning during generation

## Files Modified

| File                   | Change                                                                                                                                                                   |
|------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `Constants.kt`         | Added `DEBUG_SHOW_COLLISION_BOXES`, `CHAR_WIDTH_BLOCK`. Changed `CHAR_WIDTH` to runtime `var`. Removed `PLATFORM_MIN_WIDTH`, `PLATFORM_MAX_WIDTH`, `PLATFORM_HEIGHT`     |
| `AsciiRenderer.kt`     | Added debug paints, debug bounds rendering in `render()`. Measures `CHAR_WIDTH` and `CHAR_WIDTH_BLOCK` at init                                                           |
| `GameEngine.kt`        | Passes debug bounds to renderer. Removed `platformWidth` from `createPlatform` calls. Uses `Platform.getMaxArtWidth()` for positioning. Fall-off-edge uses `getBounds()` |
| `Player.kt`            | `getBounds()` baseline fix. `collisionOffsetX` from idle art leading spaces. `updateDimensions()` no longer overwrites offset                                            |
| `Platform.kt`          | `getBounds()` from ASCII art dimensions with correct char width per type. Removed `platformWidth` param. Added `getMaxArtWidth()` and `getArtCharWidth()`                |
| `EntityManager.kt`     | Removed `width` param from `createPlatform()`                                                                                                                            |
| `CollisionDetector.kt` | Renamed `getCollisionPoint` to `getLandingPositionY` with updated formula and description                                                                                |
