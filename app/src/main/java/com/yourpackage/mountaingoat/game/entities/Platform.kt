package com.yourpackage.mountaingoat.game.entities

import com.yourpackage.mountaingoat.game.rendering.AsciiArt
import com.yourpackage.mountaingoat.utils.Constants
import com.yourpackage.mountaingoat.utils.Vector2
import kotlin.random.Random

/**
 * Platform entity that player can land on
 */
class Platform(
    x: Float = 0f,
    y: Float = 0f,
    platformWidth: Float = Constants.PLATFORM_MIN_WIDTH,
    var type: PlatformType = PlatformType.NORMAL
) : Entity(
    position = Vector2(x, y),
    width = platformWidth,
    height = Constants.PLATFORM_HEIGHT
) {

    enum class PlatformType {
        NORMAL,     // Standard platform
        MOVING      // Slides left like a conveyor belt
    }

    // Movement properties for MOVING platforms
    var moveSpeed: Float = Constants.MOVING_PLATFORM_SPEED
    private var startX: Float = x

    // Animation for MOVING platforms
    private var animationTimer: Float = 0f
    private var useAltFrame: Boolean = false

    // Normal platform art variant (0, 1, or 2)
    private var normalVariant: Int = Random.nextInt(3)

    // Spike obstacle on top of platform
    var hasSpikes: Boolean = false

    init {
        startX = x
    }

    override fun update(deltaTime: Float) {
        when (type) {
            PlatformType.MOVING -> {
                updateAnimation(deltaTime)
            }
            else -> {}
        }
    }

    /**
     * Toggle between PLATFORM_MOVING_1 and PLATFORM_MOVING_2 for conveyor animation
     */
    private fun updateAnimation(deltaTime: Float) {
        animationTimer += deltaTime
        if (animationTimer >= Constants.PLATFORM_ANIMATION_INTERVAL) {
            useAltFrame = !useAltFrame
            animationTimer = 0f
        }
    }

    /**
     * Called when player lands on this platform
     */
    fun onPlayerLand(player: Player) {
        player.land()
    }

    override fun getAsciiRepresentation(): List<String> {
        val baseArt = when (type) {
            PlatformType.NORMAL -> when (normalVariant) {
                0 -> AsciiArt.PLATFORM_NORMAL_1
                1 -> AsciiArt.PLATFORM_NORMAL_2
                else -> AsciiArt.PLATFORM_NORMAL_3
            }
            PlatformType.MOVING -> if (useAltFrame) AsciiArt.PLATFORM_MOVING_2 else AsciiArt.PLATFORM_MOVING_1
        }

        if (!hasSpikes) return baseArt

        // Prepend spike rows on top of platform art
        val platformCharWidth = baseArt.maxOf { it.length }
        val spikePattern = AsciiArt.OBSTACLE_SPIKE
        val patternWidth = spikePattern.maxOf { it.length }
        val spikeRows = spikePattern.map { line ->
            line.repeat((platformCharWidth / patternWidth) + 1).take(platformCharWidth)
        }
        return spikeRows + baseArt
    }

    override fun reset() {
        super.reset()
        type = PlatformType.NORMAL
        position.x = startX
        animationTimer = 0f
        useAltFrame = false
        hasSpikes = false
    }

    /**
     * Initialize platform at a new position
     */
    fun init(x: Float, y: Float, platformWidth: Float, platformType: PlatformType, spikes: Boolean = false) {
        position.set(x, y)
        width = platformWidth
        type = platformType
        normalVariant = Random.nextInt(3)
        active = true
        startX = x
        animationTimer = 0f
        useAltFrame = false
        hasSpikes = spikes
        moveSpeed = if (platformType == PlatformType.MOVING) {
            val speed = Constants.MOVING_PLATFORM_SPEED
            if (Random.nextBoolean()) speed else -speed
        } else {
            Constants.MOVING_PLATFORM_SPEED
        }
    }
}
