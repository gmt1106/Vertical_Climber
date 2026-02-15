# Phase 2 Complete: Physics & Movement

## Summary

Phase 2 of the Mountain Goat game has been successfully implemented. The game now has proper physics, collision detection, platform entities, and entity management system. Players can now land on platforms and the game feels much more complete!

## What Was Built

### 1. PhysicsEngine.kt ✅
**Purpose**: Core physics calculations for realistic movement

**Key Methods**:
- `applyGravity(entity, deltaTime)` - Applies gravity acceleration to velocity
- `updateVelocity(entity, deltaTime)` - Updates position based on velocity with air resistance
- `calculateTrajectory(startPos, velocity)` - Predicts arc for slingshot preview (ready for Phase 3)
- `calculateVelocityToReach(from, to)` - Helper for trajectory calculations

**Physics Constants Used**:
- Gravity: 980 px/s² (realistic feel)
- Terminal velocity: 1500 px/s (max fall speed)
- Air resistance: 0.98 multiplier per frame

### 2. CollisionDetector.kt ✅
**Purpose**: Collision detection between player and platforms

**Key Methods**:
- `checkPlatformCollision(player, platform)` - Detects landing with proper rules
- `rectIntersects(rect1, rect2)` - Basic AABB collision
- `checkObstacleCollision(player, obstacle)` - For obstacles (Phase 5)
- `getCollisionPoint(player, platform)` - Returns exact landing Y coordinate
- `isOffScreen(bounds, camera, screen)` - For entity cleanup

**Collision Rules**:
- Player must be moving downward (positive Y velocity)
- Player's bottom edge must be near platform top (landing from above)
- Pass-through: Player can jump through platforms when moving upward
- 20px tolerance for smooth landing feel

### 3. Platform.kt ✅
**Purpose**: Platform entity with multiple types and behaviors

**Platform Types**:
1. **NORMAL** - Standard solid platform (`==========`)
2. **MOVING** - Oscillates horizontally (`≈≈≈≈≈≈≈≈≈≈`)
3. **BREAKING** - Disappears 0.5s after landing (`----------`)
4. **BOUNCY** - Launches player 1.5x higher (`▓▓▓▓▓▓▓▓▓▓`)

**Features**:
- Moving platforms: oscillate within range, auto-reverse at boundaries
- Breaking platforms: visual fade effect before disappearing
- Bouncy platforms: apply bounce multiplier to launch velocity
- Dynamic width: platform string length matches actual width
- Object pooling ready with reset() method

**Key Methods**:
- `update(deltaTime)` - Updates movement/breaking state
- `onPlayerLand(player)` - Platform-specific landing behavior
- `getAsciiRepresentation()` - Dynamic ASCII based on type and state

### 4. EntityManager.kt ✅
**Purpose**: Manages all game entities with object pooling

**Features**:
- Platform collection management
- Object pooling (pre-allocated 20 platforms, max pool 50)
- Entity lifecycle: create, update, cleanup
- Performance optimization through pooling

**Key Methods**:
- `updateAll(deltaTime)` - Updates all active entities
- `getAllActiveEntities()` - Returns list for rendering
- `getActivePlatforms()` - Returns platforms for collision checks
- `createPlatform(x, y, width, type)` - Creates/reuses platform from pool
- `cleanupInactive(cameraY, screenHeight)` - Removes off-screen entities
- `clear()` - Resets all entities (for game restart)
- `getHighestPlatformY()` - For procedural generation threshold
- `getPlatformCount()` - For debugging/analytics

### 5. Player.kt - Enhanced ✅
**Updates**:
- Added `Direction` enum (LEFT, RIGHT)
- Direction tracking based on horizontal velocity
- Uses detailed ASCII art from AsciiArt.kt:
  - GOAT_IDLE_RIGHT / GOAT_IDLE_LEFT (8 lines)
  - GOAT_JUMPING_RIGHT / GOAT_JUMPING_LEFT (10 lines)
  - GOAT_LANDING_RIGHT / GOAT_LANDING_LEFT (7 lines)
- Updated dimensions: 100px width, 200px height (8-line sprites)

### 6. GameEngine.kt - Major Integration ✅
**New Features**:
- EntityManager integration
- PhysicsEngine usage for all physics
- CollisionDetector for platform landing
- Procedural platform generation
- Combo multiplier system
- Dynamic platform type distribution by level

**Key Additions**:
- `generateInitialPlatforms()` - Creates 10 starting platforms
- `generatePlatformsIfNeeded()` - Procedural generation as player climbs
- `checkCollisions()` - Platform collision with scoring
- Platform type variety increases with level:
  - Level 1-2: 100% Normal
  - Level 3-4: 80% Normal, 20% Moving
  - Level 5+: 60% Normal, 20% Moving, 10% Bouncy, 10% Breaking

**Scoring System**:
- Height: 0.1 points per pixel * combo multiplier
- Platform landing: +10 points
- Combo multiplier: +0.5 per consecutive landing (max 3x)

### 7. Constants.kt - Updated ✅
**Changes**:
- Player width: 60 → 100 pixels
- Player height: 90 → 200 pixels
- Adjusted for new detailed ASCII art sprites

## Current Functionality

The game now:
1. ✅ Generates platforms procedurally as you climb
2. ✅ Applies realistic physics (gravity, air resistance, terminal velocity)
3. ✅ Detects collisions with platforms
4. ✅ Player can land on platforms and launch again
5. ✅ Moving platforms oscillate back and forth
6. ✅ Breaking platforms disappear after landing
7. ✅ Bouncy platforms launch you higher
8. ✅ Pass-through: can jump up through platforms
9. ✅ Combo scoring system rewards consecutive landings
10. ✅ Platform types vary by difficulty level
11. ✅ Camera follows player smoothly
12. ✅ Off-screen entities cleanup automatically
13. ✅ Object pooling for performance
14. ✅ Detailed mountain goat sprites (8 lines tall!)

## Game Loop Flow

```
1. Player aims slingshot
2. Launch with velocity
3. PhysicsEngine applies gravity + updates position
4. CollisionDetector checks platform collisions
5. If collision: player lands, scores points, state → READY
6. If no collision: player continues flying
7. EntityManager updates moving/breaking platforms
8. Camera follows player upward
9. Cleanup off-screen platforms
10. Generate new platforms above
11. Repeat from step 1
```

## Technical Achievements

- **Object Pooling**: Platforms reused, no garbage collection pressure
- **Physics-based**: Proper delta time integration, frame-rate independent
- **Scalable**: EntityManager design supports adding obstacles easily
- **Performance**: Spatial cleanup, only render on-screen entities
- **Smooth Landing**: 20px tolerance prevents "missed platform" frustration
- **Pass-through Logic**: Upward movement through platforms feels natural

## Known Limitations (By Design)

- No slingshot trajectory preview yet - Coming in Phase 3
- No obstacles or enemies yet - Coming in Phase 5
- No menu screens - Coming in Phase 6
- No high score persistence - Coming in Phase 6

## Files Created/Modified

**New Files** (4):
- PhysicsEngine.kt (~80 lines)
- CollisionDetector.kt (~85 lines)
- Platform.kt (~135 lines)
- EntityManager.kt (~115 lines)

**Modified Files** (3):
- Player.kt - Added direction tracking, detailed sprites
- GameEngine.kt - Full integration of Phase 2 systems
- Constants.kt - Updated player dimensions

**Total New Code**: ~500+ lines

## Build & Test

The game should now be fully playable:
1. Launch app
2. Touch and drag to aim/pull slingshot
3. Release to launch mountain goat
4. Land on platforms
5. Repeat to climb higher
6. Score increases as you climb
7. Platform types get more challenging as levels increase

## Next Phase

**Phase 3: Slingshot Mechanic** will add:
- SlingshotManager with pull-back visualization
- Trajectory preview (dotted arc)
- Visual slingshot bands stretching
- Improved aiming feedback
- Input refinement for better feel

---

**Status**: Phase 2 COMPLETE ✅
**Ready for**: Phase 3 Implementation
**Game State**: Fully playable climbing game with physics and platforms!

---

## Design Changes (Post Phase 2)

After Phase 2 completion, the game design was simplified for better focus and challenge:

### Changes Requested

1. **Simplified Platform Generation**
   - Changed from 10 initial platforms to 1-2 visible platforms
   - Tighter generation ensures only next 1-2 steps are visible
   - Reduces visual clutter, increases focus

2. **Distance-Based Scoring**
   - Removed point-based system (0.1 pts/pixel, +10 per landing, combos)
   - Changed to pure distance measurement in meters
   - Display "Distance: 45m" instead of "Score: 450"
   - Conversion: 100 pixels = 1 meter
   - More intuitive progress tracking

3. **One-Attempt Mode**
   - Removed 3-health system (♥♥♥)
   - Instant death on first obstacle hit or fall
   - No invulnerability period needed
   - Restart from beginning on death
   - UI simplified (no health hearts)

4. **Auto-Scroll Death Mechanic** ⚠️ NEW FEATURE
   - Camera scrolls upward automatically at constant speed
   - Player must keep climbing or fall behind
   - Falling behind visible area = instant death
   - Creates time pressure and prevents platform camping
   - **Not yet implemented** - will be added in Phase 4

### Impact on Implementation

**Phase 4 Changes Needed**:
- Update platform generation to create 1-2 platforms (not 5-10)
- Implement auto-scroll camera system
- Add auto-scroll death detection

**Phase 5 Changes Needed**:
- Replace ScoreManager with DistanceManager
- Remove combo multiplier system
- Remove level progression (or simplify to distance milestones)
- Update game over logic (instant death, no health)

**Phase 6 Changes Needed**:
- Update UI to show distance in meters
- Remove health display
- Update high score display to show distance instead of points

### Constants to Update

```kotlin
// Platform generation
const val INITIAL_PLATFORM_COUNT = 2 // was 10
const val PLATFORM_GENERATION_BATCH = 1 // was 5

// Auto-scroll (NEW)
const val AUTO_SCROLL_SPEED = 50f // pixels per second
const val AUTO_SCROLL_DEATH_OFFSET = 100f // how far behind before death

// Distance tracking (NEW)
const val PIXELS_PER_METER = 100f // 100 pixels = 1 meter

// Remove these:
// const val PLAYER_MAX_HEALTH = 3
// const val COMBO_MULTIPLIER_INCREASE = 0.5f
// const val COMBO_MAX_MULTIPLIER = 3f
// const val SCORE_PLATFORM_LAND = 10
```

**Design Philosophy**: Simpler, harder, more focused. One goal: climb as high as possible before dying.
