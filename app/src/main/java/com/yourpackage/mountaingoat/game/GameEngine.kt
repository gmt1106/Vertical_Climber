package com.yourpackage.mountaingoat.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.media.AudioAttributes
import android.media.SoundPool
import android.view.MotionEvent
import com.yourpackage.mountaingoat.R
import com.yourpackage.mountaingoat.game.entities.Entity
import com.yourpackage.mountaingoat.game.entities.EntityManager
import com.yourpackage.mountaingoat.game.entities.Ground
import com.yourpackage.mountaingoat.game.entities.Platform
import com.yourpackage.mountaingoat.game.entities.Player
import com.yourpackage.mountaingoat.game.physics.CollisionDetector
import com.yourpackage.mountaingoat.game.physics.PhysicsEngine
import com.yourpackage.mountaingoat.game.rendering.AsciiArt
import com.yourpackage.mountaingoat.game.rendering.AsciiRenderer
import com.yourpackage.mountaingoat.game.systems.IntroManager
import com.yourpackage.mountaingoat.game.systems.SlingshotManager
import com.yourpackage.mountaingoat.utils.Constants
import kotlin.random.Random
import androidx.core.content.edit

/**
 * Core game engine that manages game state and logic
 */
class GameEngine(private val context: Context, private val screenWidth: Int, private val screenHeight: Int, private val contentOffsetY: Int) {

    // Game state
    var gameState: GameState = GameState.INTRO
        private set

    // Core components
    private val renderer = AsciiRenderer(context, screenWidth, screenHeight, contentOffsetY)
    private val entityManager = EntityManager()
    private val player: Player
    private val slingshotManager: SlingshotManager
    private val ground: Ground

    // Tracks the state before pausing so resume restores it correctly
    private var stateBeforePause: GameState = GameState.READY

    // SharedPreferences for best distance
    private val prefs = context.getSharedPreferences("mountain_goat", Context.MODE_PRIVATE)

    // Game data
    private var distanceMeters: Float = 0f
    private var startingY: Float = 0f
    private var autoScrollActive: Boolean = false
    private var currentPlatform: Platform? = null
    private var lastMilestone: Int = 0
    private var milestoneTimer: Float = 0f
    private var lastGeneratedSpiked: Boolean = false
    private var newBestAchieved: Boolean = false

    // Intro sequence manager (owns its own SoundPool for typing sound)
    private val introManager = IntroManager(context)

    // Sound effects (gameplay only)
    private val soundPool: SoundPool
    private val jumpSoundId: Int

    init {
        val audioAttrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder().setMaxStreams(2).setAudioAttributes(audioAttrs).build()
        jumpSoundId = soundPool.load(context, R.raw.cartoon_jump_sound, 1)
    }

    init {
        // Initialize ground at the bottom of the visible screen (in world coords)
        val groundHeight = (AsciiArt.GROUND.size) * Constants.CHAR_HEIGHT
        ground = Ground(
            x = 0f,
            y = screenHeight.toFloat() - groundHeight,
            groundWidth = screenWidth.toFloat()
        )

        // Initialize player so feet align with ground top
        player = Player(0f, 0f) // temporary position, will adjust after dimensions are known
        val groundTop = ground.position.y
        val startY = groundTop - player.height
        val startX = (screenWidth / 2f) - (player.width / 2f)
        player.position.set(startX, startY)
        startingY = startY

        // Initialize slingshot manager (now handles input too)
        slingshotManager = SlingshotManager(
            getPlayerPosition = { player.position.copy() }
        )

        // Camera starts at origin (player is at screenHeight - STARTING_POSITION_Y, so visible near bottom)
        renderer.cameraPosY = 0f

        // Generate initial platforms
        generateInitialPlatforms()
    }

    /**
     * Update game logic
     */
    fun update(deltaTime: Float) {
        // Auto-scroll: camera moves up continuously after first jump (only during gameplay)
        if (autoScrollActive && (gameState == GameState.READY || gameState == GameState.AIMING || gameState == GameState.JUMPING)) {
            renderer.cameraPosY -= Constants.AUTO_SCROLL_SPEED * deltaTime

            // Check if player fell behind the scrolling camera
            if (player.position.y > renderer.cameraPosY + screenHeight) {
                triggerGameOver()
                return
            }
        }

         when (gameState) {
            GameState.INTRO -> {
                introManager.update(deltaTime.coerceAtMost(0.1f))
                if (introManager.isDone()) {
                    gameState = GameState.TITLE
                }
            }
            GameState.TITLE -> {
                // Title screen, no updates needed
            }
            GameState.READY -> {
                updateMovingPlatformCarry(deltaTime)
            }
            GameState.AIMING -> {
                updateMovingPlatformCarry(deltaTime)
            }
            GameState.JUMPING -> {
                // Update all entities
                entityManager.updateAll(deltaTime)

                // Update player physics
                updatePlayer(deltaTime)

                // Check collisions
                checkCollisions()

                // Update camera
                renderer.updateCamera(player.position.y)

                // Cleanup off-screen entities
                entityManager.cleanupInactive(renderer.cameraPosY, screenHeight)

                // Generate new platforms if needed
                generatePlatformsIfNeeded()

                // Check for game over (one attempt - fall off screen = death)
                if (isPlayerOffScreen()) {
                    triggerGameOver()
                }

                // Decay milestone display timer
                if (milestoneTimer > 0f) {
                    milestoneTimer -= deltaTime
                }
            }
            GameState.PAUSED -> {
                // Game is paused, no updates
            }
            GameState.GAME_OVER -> {
                // Game over, no updates
            }
        }
    }

    /**
     * Update player physics and state
     */
    private fun updatePlayer(deltaTime: Float) {
        // Apply physics to player when in air
        if (player.state == Player.State.FLYING || player.state == Player.State.LANDING) {
            PhysicsEngine.applyGravity(player, deltaTime)
            PhysicsEngine.updateVelocity(player, deltaTime)
        }

        // Update player
        player.update(deltaTime)

        // Clamp player to screen boundaries (invisible walls)
        if (player.position.x < 0f) {
            player.position.x = 0f
            player.velocity.x = 0f
        } else if (player.position.x + player.visualWidth > screenWidth) {
            player.position.x = screenWidth - player.visualWidth
            player.velocity.x = 0f
        }
    }

    /**
     * Check collisions between player and platforms
     */
    private fun checkCollisions() {
        val platforms = entityManager.getActivePlatforms()

        // Check ground collision while ground is still visible
        if (player.velocity.y > 0) {
            val groundTop = ground.position.y
            val playerBottom = player.position.y + player.height
            if (playerBottom >= groundTop) {
                player.position.y = groundTop - player.height - Constants.CHAR_HEIGHT
                player.land()
                currentPlatform = null
                gameState = GameState.READY
                return
            }
        }

        for (platform in platforms) {
            if (CollisionDetector.checkPlatformCollision(player, platform)) {
                // Spiked platform = instant death
                if (platform.hasSpikes) {
                    triggerGameOver()
                    return
                }

                // Player landed on platform
                val landingY = CollisionDetector.getLandingPositionY(player, platform)
                player.position.y = landingY

                // Handle platform-specific landing logic
                platform.onPlayerLand(player)
                currentPlatform = platform

                // Start auto-scroll on first platform landing
                if (!autoScrollActive) {
                    autoScrollActive = true
                }

                // Update distance only on landing (startingY is high, landingY is lower = higher up)
                val landedDistance = (startingY - landingY) / Constants.PIXELS_PER_METER
                if (landedDistance > distanceMeters) {
                    distanceMeters = landedDistance
                    // Check for milestone
                    val newMilestone = (distanceMeters / Constants.MILESTONE_INTERVAL_METERS).toInt()
                    if (newMilestone > lastMilestone) {
                        lastMilestone = newMilestone
                        milestoneTimer = Constants.MILESTONE_DISPLAY_DURATION
                    }
                }

                // Transition to READY state for next slingshot
                if (player.state == Player.State.IDLE) {
                    gameState = GameState.READY
                }

                break // Only collide with one platform at a time
            }
        }
    }

    /**
     * Render the game
     */
    fun render(canvas: Canvas) {
        when (gameState) {
            GameState.INTRO -> {
                renderer.renderIntro(canvas, introManager.getDisplayLines())
            }
            GameState.TITLE -> {
                renderer.renderTitle(canvas, getBestDistance())
            }
            else -> {
                val entities = mutableListOf<Entity>(player, ground)
                entities.addAll(entityManager.getAllActiveEntities())

                val milestone = if (milestoneTimer > 0f) "${(lastMilestone * Constants.MILESTONE_INTERVAL_METERS).toInt()}m!" else null

                // Build debug collision bounds: Pair<RectF, isPlayer>
                val debugBounds = if (Constants.DEBUG_SHOW_COLLISION_BOXES) {
                    val bounds = mutableListOf(Pair(player.getBounds(), true))
                    for (platform in entityManager.getActivePlatforms()) {
                        bounds.add(Pair(platform.getBounds(), false))
                    }
                    bounds
                } else {
                    emptyList()
                }

                renderer.render(
                    canvas,
                    entities,
                    distanceMeters,
                    isGameOver = gameState == GameState.GAME_OVER,
                    isPaused = gameState == GameState.PAUSED,
                    milestoneText = milestone,
                    bestDistance = getBestDistance(),
                    isNewBest = newBestAchieved,
                    debugBounds = debugBounds
                )
            }
        }
    }

    /**
     * Generate initial platforms
     */
    private fun generateInitialPlatforms() {

        val startX = (screenWidth / 2f) - (player.width / 2f)
        val playerY = ground.position.y - player.height

        // Create first platform above player (lower Y = higher on screen)
        val firstPlatformY = playerY - Constants.PLATFORM_MIN_SPACING
        entityManager.createPlatform(
            x = startX,
            y = firstPlatformY,
            type = Platform.PlatformType.NORMAL
        )

        // Generate platforms above, filling the entire visible screen up to Y = 0
        var currentY = firstPlatformY
        while (currentY > 0f) {
            val artWidth = Platform.getMaxArtWidth(Platform.PlatformType.NORMAL)
            val platformX = Random.nextFloat() * (screenWidth - artWidth)
            val spacing = Random.nextFloat() * (Constants.PLATFORM_MAX_SPACING - Constants.PLATFORM_MIN_SPACING) + Constants.PLATFORM_MIN_SPACING

            currentY -= spacing

            entityManager.createPlatform(
                x = platformX,
                y = currentY,
                type = Platform.PlatformType.NORMAL
            )
        }
    }

    /**
     * Generate new platforms as screen scrolls up
     * Platforms appear above the visible area and scroll into view from the top
     */
    private fun generatePlatformsIfNeeded() {

        val highestPlatformY = entityManager.getHighestPlatformY()

        // Generate when player is approaching the highest platform
        // Use player position as reference so platforms are always generated ahead
        val generateThreshold = player.position.y - Constants.PLATFORM_MAX_SPACING
        if (highestPlatformY > generateThreshold) {
            // Randomly assign platform type
            val isMoving = Random.nextFloat() < Constants.MOVING_PLATFORM_CHANCE
            val type = if (isMoving) Platform.PlatformType.MOVING else Platform.PlatformType.NORMAL

            val artWidth = Platform.getMaxArtWidth(type)
            val platformX = Random.nextFloat() * (screenWidth - artWidth)
            val spacing = Random.nextFloat() *
                    (Constants.PLATFORM_MAX_SPACING - Constants.PLATFORM_MIN_SPACING) +
                    Constants.PLATFORM_MIN_SPACING

            val newY = highestPlatformY - spacing

            // Spikes only on NORMAL platforms, and never two spiked in a row
            val hasSpikes = !isMoving &&
                    !lastGeneratedSpiked &&
                    Random.nextFloat() < Constants.SPIKE_PLATFORM_CHANCE
            lastGeneratedSpiked = hasSpikes

            entityManager.createPlatform(
                x = platformX,
                y = newY,
                type = type,
                hasSpikes = hasSpikes
            )
        }
    }

    /**
     * Handle touch input
     */
    fun handleTouchEvent(event: MotionEvent): Boolean {
        // Intro: tap to skip to title
        if (gameState == GameState.INTRO) {
            if (event.action == MotionEvent.ACTION_DOWN) {
                introManager.skip()
                gameState = GameState.TITLE
            }
            return true
        }

        // Title: tap to start game
        if (gameState == GameState.TITLE) {
            if (event.action == MotionEvent.ACTION_DOWN) {
                gameState = GameState.READY
            }
            return true
        }

        // Pause button tap (works in any state except GAME_OVER)
        if (event.action == MotionEvent.ACTION_DOWN) {
            val bounds = renderer.pauseButtonBounds
            if (bounds.contains(event.x, event.y)) {
                if (gameState == GameState.PAUSED) {
                    resume()
                } else if (gameState != GameState.GAME_OVER) {
                    pause()
                }
                return true
            }
        }

        // Game over: tap to restart
        if (gameState == GameState.GAME_OVER) {
            if (event.action == MotionEvent.ACTION_DOWN) {
                reset()
                return true
            }
            return false
        }

        // While paused, ignore all other input
        if (gameState == GameState.PAUSED) {
            return false
        }

        // Let slingshot manager handle the touch event
        val handled = slingshotManager.handleTouchEvent(event, gameState.name)

        // Check if we need to transition states
        if (slingshotManager.shouldTransitionToAiming && gameState == GameState.READY) {
            gameState = GameState.AIMING
        } else if (slingshotManager.shouldTransitionToJumping && gameState == GameState.AIMING) {
            val velocity = slingshotManager.getLaunchVelocity()
            player.launch(velocity)
            gameState = GameState.JUMPING
            currentPlatform = null
            soundPool.play(jumpSoundId, 0.7f, 0.7f, 1, 0, 1.0f)
        }

        return handled
    }

    /**
     * Update goat position when standing on a MOVING platform (conveyor belt effect)
     */
    private fun updateMovingPlatformCarry(deltaTime: Float) {
        val platform = currentPlatform ?: return
        if (platform.type != Platform.PlatformType.MOVING || !platform.active) return

        // Update the moving platform
        platform.update(deltaTime)

        // Carry goat at the platform's speed (direction determined by moveSpeed sign)
        player.position.x += platform.moveSpeed * deltaTime

        // Clamp player to screen boundaries
        if (player.position.x < 0f) {
            player.position.x = 0f
        } else if (player.position.x + player.visualWidth > screenWidth) {
            player.position.x = screenWidth - player.visualWidth
        }

        // Check if goat's feet still overlap the platform horizontally
        val feetLeft = player.position.x + player.collisionOffsetX
        val feetRight = feetLeft + player.width
        val platBounds = platform.getBounds()
        val platLeft = platBounds.left
        val platRight = platBounds.right
        if (feetRight < platLeft || feetLeft > platRight) {
            // Goat fell off the platform edge
            player.velocity.y = Constants.FALL_OFF_PLATFORM_VELOCITY // small downward velocity to start falling
            gameState = GameState.JUMPING
            currentPlatform = null
        }
    }

    /**
     * Check if player has fallen off screen
     */
    private fun isPlayerOffScreen(): Boolean {
        return player.position.y > renderer.cameraPosY + screenHeight + Constants.SCREEN_BOTTOM_DEATH_OFFSET
    }

    /**
     * Pause the game
     */
    fun pause() {
        if (gameState != GameState.PAUSED && gameState != GameState.GAME_OVER
            && gameState != GameState.INTRO && gameState != GameState.TITLE) {
            stateBeforePause = gameState
            gameState = GameState.PAUSED
        }
    }

    /**
     * Resume the game
     */
    fun resume() {
        if (gameState == GameState.PAUSED) {
            gameState = stateBeforePause
        }
    }

    /**
     * Reset the game
     */
    fun reset() {
        gameState = GameState.READY
        stateBeforePause = GameState.READY
        distanceMeters = 0f
        autoScrollActive = false
        currentPlatform = null
        lastMilestone = 0
        milestoneTimer = 0f
        lastGeneratedSpiked = false
        newBestAchieved = false

        player.reset()
        player.active = true
        val startY = ground.position.y - player.height
        val startX = (screenWidth / 2f) - (player.width / 2f)
        player.position.set(startX, startY)
        player.velocity.set(0f, 0f)
        startingY = startY

        // Camera starts at origin
        renderer.cameraPosY = 0f

        // Clear and regenerate platforms
        entityManager.clear()
        generateInitialPlatforms()

        // Reset slingshot
        slingshotManager.reset()
    }

    /**
     * Release sound resources
     */
    fun releaseSoundPool() {
        introManager.release()
        soundPool.release()
    }

    /**
     * Trigger game over: set state and save best distance
     */
    private fun triggerGameOver() {
        gameState = GameState.GAME_OVER
        saveBestDistance()
    }

    /**
     * Save best distance to SharedPreferences if current is higher
     */
    private fun saveBestDistance() {
        val currentMeters = distanceMeters.toInt()
        val storedBest = prefs.getInt("best_distance", 0)
        if (currentMeters > storedBest) {
            prefs.edit { putInt("best_distance", currentMeters) }
            newBestAchieved = true
        }
    }

    /**
     * Get best distance from SharedPreferences
     */
    fun getBestDistance(): Int = prefs.getInt("best_distance", 0)

    /**
     * Get current distance in meters
     */
    fun getDistanceMeters(): Float = distanceMeters
}
