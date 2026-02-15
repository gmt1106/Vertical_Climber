package com.yourpackage.mountaingoat.game.entities

import com.yourpackage.mountaingoat.game.rendering.AsciiArt
import com.yourpackage.mountaingoat.utils.Constants
import com.yourpackage.mountaingoat.utils.Vector2

/**
 * Platform entity that player can land on
 */
class Platform(
    x: Float = 0f,
    y: Float = 0f,
    platformWidth: Float = Constants.PLATFORM_MIN_WIDTH,
    val type: PlatformType = PlatformType.NORMAL
) : Entity(
    position = Vector2(x, y),
    width = platformWidth,
    height = Constants.PLATFORM_HEIGHT
) {

    enum class PlatformType {
        NORMAL,     // Standard platform
        MOVING,     // Moves horizontally
        BREAKING,   // Breaks after landing
        BOUNCY      // Launches player higher
    }

    // Movement properties for MOVING platforms
    var moveSpeed: Float = 50f // pixels per second
    var moveDirection: Int = 1 // 1 = right, -1 = left
    var moveRange: Float = 100f // how far to move from start
    private var startX: Float = x

    // Breaking platform properties
    var breaking: Boolean = false
    private var breakTimer: Float = 0f
    private val breakDelay: Float = 0.5f // time before platform disappears

    // Bouncy platform properties
    val bounceMultiplier: Float = 1.5f

    init {
        startX = x
    }

    override fun update(deltaTime: Float) {
        when (type) {
            PlatformType.MOVING -> updateMovement(deltaTime)
            PlatformType.BREAKING -> updateBreaking(deltaTime)
            else -> {} // NORMAL and BOUNCY don't need updates
        }
    }

    /**
     * Update movement for moving platforms
     */
    private fun updateMovement(deltaTime: Float) {
        position.x += moveSpeed * moveDirection * deltaTime

        // Reverse direction at boundaries
        if (position.x > startX + moveRange) {
            position.x = startX + moveRange
            moveDirection = -1
        } else if (position.x < startX - moveRange) {
            position.x = startX - moveRange
            moveDirection = 1
        }
    }

    /**
     * Update breaking state
     */
    private fun updateBreaking(deltaTime: Float) {
        if (breaking) {
            breakTimer += deltaTime
            if (breakTimer >= breakDelay) {
                active = false
            }
        }
    }

    /**
     * Called when player lands on this platform
     */
    fun onPlayerLand(player: Player) {
        when (type) {
            PlatformType.NORMAL, PlatformType.MOVING -> {
                // Just land normally
                player.land()
            }
            PlatformType.BREAKING -> {
                player.land()
                breaking = true
            }
            PlatformType.BOUNCY -> {
                // Launch player with extra bounce
                val bounceVelocity = Vector2(
                    player.velocity.x,
                    -Math.abs(player.velocity.y) * bounceMultiplier
                )
                player.launch(bounceVelocity)
            }
        }
    }

    override fun getAsciiRepresentation(): List<String> {
        // Generate platform string based on width
        val charCount = (width / Constants.CHAR_WIDTH).toInt().coerceAtLeast(3)

        val platformChar = when (type) {
            PlatformType.NORMAL -> '='
            PlatformType.MOVING -> '≈'
            PlatformType.BREAKING -> if (breaking) '·' else '-'
            PlatformType.BOUNCY -> '▓'
        }

        return listOf(platformChar.toString().repeat(charCount))
    }

    override fun reset() {
        super.reset()
        breaking = false
        breakTimer = 0f
        moveDirection = 1
        position.x = startX
    }

    /**
     * Initialize platform at a new position
     */
    fun init(x: Float, y: Float, platformWidth: Float, platformType: PlatformType) {
        position.set(x, y)
        width = platformWidth
        active = true
        breaking = false
        breakTimer = 0f
        startX = x
        moveDirection = 1
    }
}
