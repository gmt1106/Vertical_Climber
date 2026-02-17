package com.yourpackage.mountaingoat.game

import android.graphics.Canvas
import android.view.MotionEvent
import com.yourpackage.mountaingoat.game.entities.Entity
import com.yourpackage.mountaingoat.game.entities.EntityManager
import com.yourpackage.mountaingoat.game.entities.Ground
import com.yourpackage.mountaingoat.game.entities.Platform
import com.yourpackage.mountaingoat.game.entities.Player
import com.yourpackage.mountaingoat.game.physics.CollisionDetector
import com.yourpackage.mountaingoat.game.physics.PhysicsEngine
import com.yourpackage.mountaingoat.game.rendering.AsciiArt
import com.yourpackage.mountaingoat.game.rendering.AsciiRenderer
import com.yourpackage.mountaingoat.game.systems.SlingshotManager
import com.yourpackage.mountaingoat.utils.Constants
import kotlin.random.Random

/**
 * Core game engine that manages game state and logic
 */
class GameEngine(private val screenWidth: Int, private val screenHeight: Int) {

    // Game state
    var gameState: GameState = GameState.READY
        private set

    // Core components
    private val renderer = AsciiRenderer(screenWidth, screenHeight)
    private val entityManager = EntityManager()
    private val player: Player
    private val slingshotManager: SlingshotManager
    private val ground: Ground

    // Tracks the state before pausing so resume restores it correctly
    private var stateBeforePause: GameState = GameState.READY

    // Game data
    private var distanceMeters: Float = 0f
    private var startingY: Float = 0f
    private var autoScrollActive: Boolean = false
    private var currentPlatform: Platform? = null
    private var lastMilestone: Int = 0
    private var milestoneTimer: Float = 0f
    private var lastGeneratedSpiked: Boolean = false

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
        val startY = groundTop - player.height - Constants.CHAR_HEIGHT
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
        // Auto-scroll: camera moves up continuously after first jump
        if (autoScrollActive && gameState != GameState.PAUSED && gameState != GameState.GAME_OVER) {
            renderer.cameraPosY -= Constants.AUTO_SCROLL_SPEED * deltaTime

            // Check if player fell behind the scrolling camera
            if (player.position.y > renderer.cameraPosY + screenHeight) {
                gameState = GameState.GAME_OVER
                return
            }
        }

         when (gameState) {
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
                    gameState = GameState.GAME_OVER
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
                    gameState = GameState.GAME_OVER
                    return
                }

                // Player landed on platform
                val landingY = CollisionDetector.getCollisionPoint(player, platform)
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
        val entities = mutableListOf<Entity>(player, ground)
        entities.addAll(entityManager.getAllActiveEntities())

        val milestone = if (milestoneTimer > 0f) "${(lastMilestone * Constants.MILESTONE_INTERVAL_METERS).toInt()}m!" else null

        renderer.render(
            canvas,
            entities,
            distanceMeters,
            isGameOver = gameState == GameState.GAME_OVER,
            isPaused = gameState == GameState.PAUSED,
            milestoneText = milestone
        )
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
            x = startX - 20f,
            y = firstPlatformY,
            width = Constants.PLATFORM_MAX_WIDTH,
            type = Platform.PlatformType.NORMAL
        )

        // Generate platforms above, filling the entire visible screen up to Y = 0
        var currentY = firstPlatformY
        while (currentY > 0f) {
            val platformWidth = Random.nextFloat() * (Constants.PLATFORM_MAX_WIDTH - Constants.PLATFORM_MIN_WIDTH) + Constants.PLATFORM_MIN_WIDTH
            val platformX = Random.nextFloat() * (screenWidth - platformWidth)
            val spacing = Random.nextFloat() * (Constants.PLATFORM_MAX_SPACING - Constants.PLATFORM_MIN_SPACING) + Constants.PLATFORM_MIN_SPACING

            currentY -= spacing

            entityManager.createPlatform(
                x = platformX,
                y = currentY,
                width = platformWidth,
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
            val platformWidth = Random.nextFloat() *
                    (Constants.PLATFORM_MAX_WIDTH - Constants.PLATFORM_MIN_WIDTH) +
                    Constants.PLATFORM_MIN_WIDTH
            val platformX = Random.nextFloat() * (screenWidth -
                    platformWidth)
            val spacing = Random.nextFloat() *
                    (Constants.PLATFORM_MAX_SPACING - Constants.PLATFORM_MIN_SPACING) +
                    Constants.PLATFORM_MIN_SPACING

            val newY = highestPlatformY - spacing

            // Randomly assign platform type
            val isMoving = Random.nextFloat() < Constants.MOVING_PLATFORM_CHANCE
            val type = if (isMoving) Platform.PlatformType.MOVING else Platform.PlatformType.NORMAL

            // Spikes only on NORMAL platforms, and never two spiked in a row
            val hasSpikes = !isMoving &&
                    !lastGeneratedSpiked &&
                    Random.nextFloat() < Constants.SPIKE_PLATFORM_CHANCE
            lastGeneratedSpiked = hasSpikes

            entityManager.createPlatform(
                x = platformX,
                y = newY,
                width = platformWidth,
                type = type,
                hasSpikes = hasSpikes
            )
        }
    }

    /**
     * Handle touch input
     */
    fun handleTouchEvent(event: MotionEvent): Boolean {
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

        // Carry goat left at the platform's speed
        player.position.x -= platform.moveSpeed * deltaTime

        // Clamp player to screen boundaries
        if (player.position.x < 0f) {
            player.position.x = 0f
        } else if (player.position.x + player.visualWidth > screenWidth) {
            player.position.x = screenWidth - player.visualWidth
        }

        // Check if goat's feet still overlap the platform horizontally
        val feetLeft = player.position.x + player.collisionOffsetX
        val feetRight = feetLeft + player.width
        val platLeft = platform.position.x
        val platRight = platLeft + platform.width
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
        if (gameState != GameState.PAUSED && gameState != GameState.GAME_OVER) {
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

        player.reset()
        player.active = true
        val startY = ground.position.y - player.height - Constants.CHAR_HEIGHT
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
     * Get current distance in meters
     */
    fun getDistanceMeters(): Float = distanceMeters
}
