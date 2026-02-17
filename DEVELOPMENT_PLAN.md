# Mountain Goat - Android ASCII Vertical Jumper Game - Implementation Plan

## Project Overview
"Mountain Goat" is a native Android vertical jumper game using Kotlin with Canvas rendering and ASCII art graphics. Players control a mountain goat using a slingshot mechanic to launch upward through sparse platforms, avoiding obstacles in a one-attempt, distance-climbing challenge. The mountain goat must climb as high as possible before dying, with auto-scrolling creating constant time pressure. Minimalist design with distance tracking in meters, no health system, and focused gameplay.

## Technology Stack
- **Language**: Kotlin
- **Rendering**: Custom Canvas View with ASCII art
- **Architecture**: MVC variant (Model-View-Controller)
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 34
- **Dependencies**: Minimal (AndroidX Core, Material, Navigation, JSON serialization)

## Key Architectural Decisions

### 1. Custom View Game Loop
- Thread-based game loop at 60 FPS (not coroutines)
- Fixed timestep updates for consistent physics
- SurfaceView with double-buffered Canvas rendering
- Separate rendering thread from UI thread

### 2. Component-Based Entity System
- Base `Entity` class with position, velocity, bounds
- Specialized entities: Player, Platform, Obstacle
- EntityManager coordinates all entities
- Object pooling for performance optimization

### 3. Physics-Based Slingshot
- Touch input calculates pull vector
- Launch velocity scales with pull distance (clamped)
- Trajectory preview using physics simulation
- Gravity constant: 980 pixels/sec² for realistic feel

### 4. ASCII Rendering System
- Monospace font (Roboto Mono)
- Multi-line ASCII art support
- Camera scrolling follows player (smooth lerp)
- World-to-screen coordinate conversion

### 5. Platform Generation
- Sparse generation: Only 1-2 platforms visible at any time
- Procedural generation as player ascends
- Reachability validation (no impossible jumps)
- Tight spacing for focused gameplay (next step always visible)
- Platform types: Normal, Moving, Breaking, Bouncy

## Project Structure

```
app/src/main/java/com/yourpackage/jumpergame/
├── MainActivity.kt
├── ui/
│   ├── fragments/
│   │   ├── MenuFragment.kt
│   │   ├── GameFragment.kt
│   │   └── HighScoreFragment.kt
│   └── views/
│       └── GameView.kt
├── game/
│   ├── GameEngine.kt           [CRITICAL]
│   ├── GameThread.kt
│   ├── GameState.kt
│   ├── entities/
│   │   ├── Entity.kt
│   │   ├── Player.kt
│   │   ├── Platform.kt
│   │   ├── Obstacle.kt
│   │   └── EntityManager.kt
│   ├── physics/
│   │   ├── PhysicsEngine.kt    [CRITICAL]
│   │   ├── Vector2.kt
│   │   └── CollisionDetector.kt [CRITICAL]
│   ├── systems/
│   │   ├── SlingshotManager.kt  [CRITICAL]
│   │   ├── PlatformGenerator.kt
│   │   ├── LevelManager.kt
│   │   ├── ScoreManager.kt
│   │   └── InputManager.kt
│   └── rendering/
│       ├── AsciiRenderer.kt     [CRITICAL]
│       └── AsciiArt.kt
├── data/
│   └── HighScoreRepository.kt
└── utils/
    ├── Constants.kt
    └── MathUtils.kt
```

## Implementation Phases

### Phase 1: Project Setup & Foundation
**Goal**: Create project structure with basic rendering

1. **Initialize Android Project**
   - Create new Android project in Android Studio
   - Package: `com.yourpackage.mountaingoat`
   - Set up build.gradle.kts with dependencies
   - Configure AndroidManifest.xml (portrait, immersive mode)

2. **Core Math & Utilities**
   - Create `Vector2.kt`: Vector class with math operations
     - Properties: x, y
     - Methods: magnitude(), normalize(), plus(), times()
   - Create `Constants.kt`: Game constants
     - Physics: GRAVITY, TERMINAL_VELOCITY, AIR_RESISTANCE
     - Slingshot: MAX_PULL_DISTANCE, LAUNCH_FORCE_MULTIPLIER
     - Rendering: CHAR_WIDTH, CHAR_HEIGHT, TARGET_FPS
     - Platform Generation: INITIAL_PLATFORM_COUNT = 2 (was 10), PLATFORM_GENERATION_BATCH = 1
     - Auto-Scroll: AUTO_SCROLL_SPEED = 50f, AUTO_SCROLL_DEATH_OFFSET = 100f
     - Distance Tracking: PIXELS_PER_METER = 100f
     - Remove: PLAYER_MAX_HEALTH, COMBO_*, SCORE_* constants

3. **Entity Foundation**
   - Create `Entity.kt`: Abstract base class
     - Properties: position, velocity, size, active
     - Methods: update(deltaTime), getBounds(), getAsciiRepresentation()
   - Create `Player.kt`: Player entity (mountain goat)
     - ASCII art: 3-line mountain goat character with horns
     - States: IDLE, FLYING, LANDING
     - Methods: launch(velocity), land()

4. **Basic Rendering**
   - Create `AsciiRenderer.kt`: Rendering system
     - Setup: Monospace font Paint, background Paint
     - Methods: render(), renderEntity(), drawAscii()
     - Camera: cameraPosY, worldToScreen()
   - Create `AsciiArt.kt`: ASCII art constants
     - Mountain goat player animations (jumping, landing states)
     - Platform types (normal, moving, breaking, bouncy)
     - Obstacles (spikes, hazards)

5. **Game View & Loop**
   - Create `GameView.kt`: Custom SurfaceView
     - Implements SurfaceHolder.Callback
     - Methods: surfaceCreated/Destroyed(), onDraw(), onTouchEvent()
   - Create `GameThread.kt`: Game loop thread
     - Target: 60 FPS with frame limiting
     - Loop: Update game state → Render → Sleep if needed
   - Create `MainActivity.kt`: Host GameView
     - Set content view, handle lifecycle

**Verification**: App runs, player renders on screen, falls with gravity

### Phase 2: Physics & Movement
**Goal**: Implement realistic physics and collision detection

1. **Physics Engine**
   - Create `PhysicsEngine.kt`
     - `applyGravity(entity, deltaTime)`: Add gravity to velocity
     - `updateVelocity(entity, deltaTime)`: Update position from velocity
     - `calculateTrajectory(startPos, velocity)`: Predict arc (for slingshot preview)
     - Constants: GRAVITY = 980f pixels/sec²

2. **Collision Detection**
   - Create `CollisionDetector.kt`
     - `checkPlatformCollision(player, platform)`: AABB collision + landing check
     - `rectIntersects(rect1, rect2)`: Basic rectangle intersection
     - Landing rules: Player moving downward + bottom edge above platform top
     - Pass-through: Allow upward movement through platforms

3. **Platform Entity**
   - Create `Platform.kt`
     - Types: NORMAL, MOVING, BREAKING, BOUNCY
     - Properties: width, type, moveSpeed, moveDirection
     - Methods: update(deltaTime), onPlayerLand(), getAsciiRepresentation()
     - ASCII: "==========" (Normal), "≈≈≈≈≈≈≈≈≈≈" (Moving), etc.

4. **Entity Management**
   - Create `EntityManager.kt`
     - Collections: player, platforms, obstacles
     - Methods: updateAll(), getActiveEntities(), cleanupInactive()
     - Object pooling: Reuse inactive entities for performance

**Verification**: Player falls, lands on platform, stops moving. Pass-through works when jumping upward.

### Phase 3: Slingshot Mechanic
**Goal**: Implement core slingshot gameplay with trajectory preview

1. **Input Handling**
   - Create `InputManager.kt`
     - Process MotionEvent: ACTION_DOWN, ACTION_MOVE, ACTION_UP
     - Route touch to SlingshotManager based on game state
     - Methods: handleTouchEvent(), onTouchDown/Move/Up()

2. **Slingshot System**
   - Create `SlingshotManager.kt`
     - Properties: slingshotAnchor, pullPosition, isAiming, trajectoryPoints
     - `startAim(touchPos)`: Begin aiming from player position
     - `updateAim(touchPos)`: Calculate pull vector, generate trajectory
     - `release()`: Calculate velocity, launch player
     - `calculateLaunchVelocity(pullVector)`: Map pull distance to velocity
     - **INVERTED PULL**: Pull DOWN on screen = Launch UP (opposite direction)
     - Constants: MAX_PULL = 150px, MIN_VEL = 300, MAX_VEL = 1500

3. **Trajectory Preview**
   - In SlingshotManager: `generateTrajectory()`
     - Simulate physics: position += velocity * dt; velocity.y += gravity * dt
     - Generate 30 points along arc
     - Return List<Vector2> for rendering

4. **Visual Rendering (Minimalist)**
   - In AsciiRenderer: `renderTrajectory(canvas, trajectoryPoints)`
     - **NO slingshot graphics** (no base, no bands)
     - Draw trajectory dots only (every 3rd point for dotted effect)
     - Visual feedback: White/yellow trajectory dots showing launch arc
     - Clean, minimal UI focused on trajectory

5. **Game State Management**
   - Uses existing `GameState.kt`: Enum (READY, AIMING, JUMPING, PAUSED, GAME_OVER)
   - State transitions:
     - READY → AIMING (touch down)
     - AIMING → JUMPING (touch up/release)
     - JUMPING → READY (land on platform)

**Verification**: Touch and drag down shows trajectory arc going UP. Release launches player along predicted path upward.

### Phase 4: Platform Generation, Scrolling & Boundaries
**Goal**: Sparse platform generation with auto-scrolling death mechanic, screen boundary walls, and correct initial platform placement

1. **Screen Boundary Walls**
   - Goat must not go outside left and right edges of the screen
   - Clamp player X position: `0` to `screenWidth - player.width`
   - When hitting a wall while moving laterally, zero out horizontal velocity
   - Gravity continues to apply — goat slides down along the wall edge
   - Check and clamp after each position update in the game loop

2. **Fix Initial Platform Placement**
   - First platform generated in `generateInitialPlatforms()` must be ABOVE the goat character (lower Y value), not below
   - Currently placed at `startY + player.height + PLATFORM_MIN_SPACING` (below goat — wrong direction)
   - Should be placed at `startY - PLATFORM_MIN_SPACING` (above goat — correct, gives goat a target to jump to)
   - Subsequent platforms continue stacking upward (decreasing Y) from there

3. **Auto-Scroll Death Mechanic** ⚠️ NEW FEATURE
   - Constant upward screen scrolling creates time pressure
   - In GameEngine: `updateAutoScroll(deltaTime)`
     - Auto-scroll speed: `AUTO_SCROLL_SPEED` pixels/second (e.g., 50 px/s)
     - Increment `autoScrollOffset` each frame
     - Adjust camera: `cameraPosY -= AUTO_SCROLL_SPEED * deltaTime`
   - Death detection: Player falls behind visible area
     - Calculate bottom of screen: `cameraY + screenHeight + DEATH_OFFSET`
     - If `player.y > bottomThreshold`: Instant death
   - Constants needed:
     - `AUTO_SCROLL_SPEED = 50f // pixels per second`
     - `AUTO_SCROLL_DEATH_OFFSET = 100f // grace distance`
   - Creates urgency: Can't camp on platforms forever

**Verification**: Goat cannot move past left/right screen edges and slides down along walls. First platform appears above goat at game start. Camera follows player smoothly. Auto-scroll forces upward movement. Player dies if falling behind screen.

### Phase 5: Game Systems (Moving Platforms, Obstacles, Distance Milestones)
**Goal**: Complete game mechanics with moving platforms, obstacles, and simplified progression

1. **Moving Platform Behavior**
   - **Visual rotation**: Alternate between `PLATFORM_MOVING_1` and `PLATFORM_MOVING_2` ASCII art on a timer (e.g. every 0.3s) to create a conveyor belt animation effect
   - **Left-only movement**: MOVING platforms slide left at a constant speed (remove bidirectional bouncing)
   - **Goat carried by platform**: When goat is standing on a MOVING platform (READY/AIMING state), goat's X position shifts left at the platform's move speed — like a real conveyor belt
   - **Fall off edge**: When goat's feet no longer overlap the platform horizontally, goat falls off naturally (gravity kicks in, transitions to JUMPING state)
   - Files: `Platform.kt` (animation timer, left-only movement, art switching), `GameEngine.kt` (apply platform velocity to player when standing on MOVING platform, detect falling off edge)

2. **Spiked Platforms (Obstacles)**
   - Not a separate entity — spikes are a property of a platform
   - Add `hasSpikes: Boolean` to `Platform.kt`
   - ASCII art: `OBSTACLE_SPIKE = "/\"` constant in `AsciiArt.kt`, repeated on top of normal platform art to match platform width
   - Rendering: spike row rendered above the normal platform lines
   - On landing: if `hasSpikes` is true → instant death (game over)
   - Platform generation: ensure a step-able (non-spiked) platform always exists nearby when a spiked platform is generated
   - Files: `Platform.kt` (hasSpikes flag, spike rendering), `GameEngine.kt` (death check on spiked landing), `AsciiArt.kt` (OBSTACLE_SPIKE constant)

3. **Level Progression**
   - Simplify to distance milestones
     - Every 50 meters = milestone marker (visual feedback only)
     - No difficulty changes, consistent platform types
     - Display: "50m", "100m" milestone messages

4. **Pause/Resume**
   - GameEngine: pause() / resume() methods
   - GameThread: suspend/resume loop
   - Touch during JUMPING: Pause game, show menu overlay

**Verification**: Moving platforms slide left with conveyor animation. Goat rides moving platform and falls off edge. Distance milestones display correctly. Obstacles cause instant death.

### Phase 6: UI & Navigation
**Goal**: Terminal-style intro sequence, title screen, menu system, and persistent storage

1. **Terminal Intro Sequence** (before title screen)
   - New game state: `INTRO` added to `GameState.kt`
   - **Terminal background image**: Load `res/drawable/mac_terminal_screen.png` as a `Bitmap` in `AsciiRenderer`, scaled to screen size once during init. Draw as background during INTRO and title screen states. Keep this background during gameplay.
   - Messages render from top-left using monospace font, each prefixed with `"> "`
   - Messages appear one by one with a timed delay between each:
     1. `> Do you want to play the Mountain Goat Game? Press enter to start!`
     2. `> ` (blank prompt line)
     3. `> Okay! Let's start!`
   - After all messages are shown, they disappear one by one from top to bottom (as if the terminal is scrolling down), with a timed delay between each removal
   - After all messages have disappeared, transition to title screen (step 2)
   - Tap anywhere to skip the intro and go directly to title screen

2. **Title Screen & Menu**
   - ASCII art "MOUNTAIN GOAT" title logo (`AsciiArt.TITLE_MOUNTAIN_GOAT`) rendered centered on screen
   - Menu options rendered below the title as ASCII text:
     - `> Start Game`
   - Best distance displayed below (e.g., `Best: 142m`) if one exists
   - Tap "Start Game" → transition to READY state (gameplay begins)

3. **Game Over Screen**
   - Show game over overlay (already implemented) with current distance and best distance
   - If new best: display a "NEW BEST!" indicator

4. **Persistent Storage (Best Distance Only)**
   - Use SharedPreferences directly — no repository class needed
     - Single key: `"best_distance"` storing an `Int` (meters)
     - On game over: compare current distance to stored best, update if higher
     - Methods in `GameEngine`: `saveBestDistance()`, `getBestDistance()`
     - Requires passing `Context` (or SharedPreferences) to GameEngine

5. **UI Overlays (In-Game HUD)**
   - Best distance shown on HUD during gameplay (e.g., `Best: 208m`) below or next to current distance, fetched from SharedPreferences

**Verification**: Terminal intro plays through with timed message sequence. Title screen shows ASCII logo. Best distance persists across app restarts. In-game HUD displays accurate info. Back button pauses game.

### Phase 7: Google Ads Integration
**Goal**: Add a non-intrusive banner ad at the bottom of the screen

1. **Dependencies & Setup**
   - Add Google Mobile Ads SDK dependency to `build.gradle.kts`: `com.google.android.gms:play-services-ads`
   - Add `APPLICATION_ID` meta-data to `AndroidManifest.xml`
   - Initialize Mobile Ads SDK in `MainActivity.onCreate()` via `MobileAds.initialize()`

2. **Banner Ad Layout**
   - Change `MainActivity` from setting `GameView` directly via `setContentView(gameView)` to using an XML layout
   - Layout structure: vertical `FrameLayout` with `GameView` filling the top and an `AdView` (banner) anchored at the bottom
   - Banner size: `AdSize.BANNER` (320x50dp) — smallest standard size, minimal screen intrusion
   - `GameView` resizes to fill remaining space above the ad — game rendering area shrinks slightly but gameplay is unaffected

3. **Ad Loading & Lifecycle**
   - Load ad in `MainActivity.onCreate()` after layout inflation: `adView.loadAd(AdRequest.Builder().build())`
   - Handle lifecycle: `adView.pause()` in `onPause()`, `adView.resume()` in `onResume()`, `adView.destroy()` in `onDestroy()`
   - Use test ad unit ID during development, replace with real ID before release

4. **Screen Adjustment**
   - `GameView` receives its actual dimensions via `surfaceCreated()` — already uses `width`/`height` from there, so the game automatically adapts to the reduced screen height
   - No changes needed in `GameEngine`, `AsciiRenderer`, or game logic — they already work with whatever screen dimensions `GameView` reports

**Verification**: Banner ad displays at the bottom of the screen. Game renders correctly in the remaining space above the ad. Ad does not overlap game content. Game performance (60 FPS) is not affected. Ad loads and displays without crashes.


**Verification**: Game runs smoothly at 60 FPS. No crashes or memory leaks. Difficulty feels balanced. All edge cases handled.

## Critical Implementation Details

### Slingshot Velocity Calculation
```kotlin
fun calculateLaunchVelocity(pullPosition: Vector2): Vector2 {
    val pullVector = Vector2(
        slingshotAnchor.x - pullPosition.x,
        slingshotAnchor.y - pullPosition.y
    )
    val pullDistance = min(pullVector.magnitude(), MAX_PULL_DISTANCE)
    val direction = pullVector.normalize()
    val velocityMag = mapRange(
        pullDistance,
        0f, MAX_PULL_DISTANCE,
        MIN_LAUNCH_VELOCITY, MAX_LAUNCH_VELOCITY
    )
    return direction * velocityMag
}
```

### Game Loop Pattern
```kotlin
override fun run() {
    var lastTime = System.nanoTime()
    while (running) {
        val currentTime = System.nanoTime()
        val deltaTime = (currentTime - lastTime) / 1_000_000_000f
        lastTime = currentTime

        gameEngine.update(deltaTime)

        val canvas = surfaceHolder.lockCanvas()
        canvas?.let {
            gameEngine.render(it)
            surfaceHolder.unlockCanvasAndPost(it)
        }

        val frameTime = System.currentTimeMillis() - currentTime / 1_000_000
        if (frameTime < TARGET_FRAME_TIME) {
            Thread.sleep(TARGET_FRAME_TIME - frameTime)
        }
    }
}
```

### Platform Landing Collision
```kotlin
fun checkPlatformCollision(player: Player, platform: Platform): Boolean {
    val playerBounds = player.getBounds()
    val platformBounds = platform.getBounds()

    if (!rectIntersects(playerBounds, platformBounds)) return false

    // Must be moving downward
    if (player.velocity.y <= 0) return false

    // Bottom edge must be above platform top (landing from above)
    return playerBounds.bottom <= platformBounds.top + 10f
}
```

### Camera Smooth Follow
```kotlin
fun updateCamera(playerY: Float) {
    val threshold = screenHeight / 2f
    if (playerY < threshold) {
        val targetY = playerY - threshold
        cameraPosY = lerp(cameraPosY, targetY, 0.1f)
    }
}

fun lerp(start: Float, end: Float, t: Float) = start + (end - start) * t
```

## Build Configuration

### build.gradle.kts (App)
```kotlin
android {
    namespace = "com.yourpackage.mountaingoat"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.yourpackage.mountaingoat"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    testImplementation("junit:junit:4.13.2")
}
```

### AndroidManifest.xml
```xml
<activity
    android:name=".MainActivity"
    android:exported="true"
    android:screenOrientation="portrait"
    android:configChanges="orientation|screenSize">
</activity>
```

## Verification & Testing

### End-to-End Testing Flow
1. **Launch App** → Main menu appears with "MOUNTAIN GOAT" ASCII logo
2. **Tap "Start Game"** → Game screen loads, mountain goat in slingshot at bottom
3. **Touch & Drag Down** → Slingshot pulls back, trajectory preview shows
4. **Release** → Player launches along predicted arc
5. **Land on Platform** → Player stops, score increases, can slingshot again
6. **Jump Multiple Times** → Height increases, camera scrolls, new platforms generate
7. **Reach 1000px** → Level 2 message, difficulty increases
8. **Hit Obstacle** → Instant death, game over screen shows distance
9. **Fall off screen** → Instant death, game over screen shows distance
10. **Check High Scores** → New high score saved and displayed
11. **Restart Game** → Everything resets correctly

### Performance Targets
- **Frame Rate**: Stable 60 FPS
- **APK Size**: < 10 MB
- **Memory**: < 50 MB RAM usage
- **Load Time**: < 2 seconds from tap to gameplay
- **Input Latency**: < 50ms touch response

## Risk Mitigation

### Technical Risks
1. **Frame rate drops**: Object pooling, spatial partitioning, profiling
2. **Touch input lag**: High-priority input thread, optimized event handling
3. **Memory leaks**: Proper lifecycle, weak references, profiling
4. **ASCII rendering slow**: Batch rendering, cache Paint objects

### Design Risks
1. **Difficulty too hard/easy**: Playtesting, adjustable constants, analytics
2. **Slingshot feel wrong**: Iterative tuning, multiple playtest sessions
3. **Screen size issues**: Density-independent pixels, multiple device testing

## Success Criteria
- ✅ Slingshot mechanic feels responsive and fun
- ✅ Physics feel realistic (smooth arc, proper gravity)
- ✅ Platform generation never creates impossible jumps
- ✅ Game runs at 60 FPS on mid-range devices (2-year-old phones)
- ✅ Progression system provides steady difficulty increase
- ✅ High scores persist across app restarts
- ✅ No crashes during 30-minute play session
- ✅ ASCII art is readable and visually appealing
