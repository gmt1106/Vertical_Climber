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
        MOVING      // Moves horizontally
    }

    // Movement properties for MOVING platforms
    var moveSpeed: Float = 50f // pixels per second
    var moveDirection: Int = 1 // 1 = right, -1 = left
    var moveRange: Float = 100f // how far to move from start
    private var startX: Float = x

    init {
        startX = x
    }

    override fun update(deltaTime: Float) {
        when (type) {
            PlatformType.MOVING -> updateMovement(deltaTime)
            else -> {}
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
     * Called when player lands on this platform
     */
    fun onPlayerLand(player: Player) {
        player.land()
    }

    override fun getAsciiRepresentation(): List<String> {
        return when (type) {
            PlatformType.NORMAL -> AsciiArt.PLATFORM_NORMAL
            PlatformType.MOVING -> AsciiArt.PLATFORM_MOVING
        }
    }

    override fun reset() {
        super.reset()
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
        startX = x
        moveDirection = 1
    }
}
