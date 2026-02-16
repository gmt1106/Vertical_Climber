# Phase 4.5: Post Phase 4 Bug Fixes

## Summary

Bug fixes identified after Phase 4 testing.

## Bug Fixes

### 1. Slingshot Allows Shooting Goat Downward

**Problem**:
- Slingshot had no angle restriction — user could launch goat in any direction including downward
- Pulling up on screen launched goat down, wasting the attempt

**Solution**:
- Added `MAX_LAUNCH_ANGLE = 70f` constant (degrees from straight up)
- In `SlingshotManager.calculateLaunchVelocity()`, after calculating launch direction, clamp the angle
- Angle from vertical calculated via `atan2(abs(x), -y)`
- If launch direction points downward (`y >= 0`) or exceeds 70 degrees from vertical, clamp to the 70-degree boundary
- Left/right direction preserved when clamping
- Acceptable launch cone: 140 degrees total (70 left + 70 right from straight up)

**Files Modified**:
- `Constants.kt`: Added `MAX_LAUNCH_ANGLE = 70f`
- `SlingshotManager.kt`: Added angle clamping logic in `calculateLaunchVelocity()`, added math imports

### 2. Platforms Using Dynamic Characters Instead of Fixed ASCII Art

**Problem**:
- `Platform.getAsciiRepresentation()` dynamically generated a single line by repeating a character (`=`, `≈`, etc.) based on platform width
- AsciiArt.kt already had multi-line platform art (`PLATFORM_NORMAL`, `PLATFORM_MOVING`) that was not being used

**Solution**:
- Changed `getAsciiRepresentation()` in Platform.kt to return the fixed ASCII art from AsciiArt.kt
- NORMAL/BREAKING/BOUNCY types return `AsciiArt.PLATFORM_NORMAL` (3-line rock texture)
- MOVING type returns `AsciiArt.PLATFORM_MOVING` (4-line bordered platform)

**Files Modified**:
- `Platform.kt`: Replaced dynamic character generation with `AsciiArt.PLATFORM_NORMAL` / `AsciiArt.PLATFORM_MOVING`, added AsciiArt import

### 3. Remove BREAKING and BOUNCY Platform Types

**Problem**:
- BREAKING and BOUNCY platform types were defined but never used in platform generation
- Added unnecessary code complexity (break timer, bounce multiplier, special landing logic)

**Solution**:
- Removed `BREAKING` and `BOUNCY` from `PlatformType` enum (only NORMAL and MOVING remain)
- Removed breaking properties (`breaking`, `breakTimer`, `breakDelay`)
- Removed bouncy properties (`bounceMultiplier`)
- Removed `updateBreaking()` method
- Simplified `onPlayerLand()` — always calls `player.land()` directly
- Simplified `getAsciiRepresentation()` — only NORMAL and MOVING branches
- Cleaned up `reset()` and `init()` — removed breaking-related resets

**Files Modified**:
- `Platform.kt`: Removed BREAKING/BOUNCY enum values, properties, methods, and branches

---

**Status**: In Progress
