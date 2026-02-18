package com.yourpackage.mountaingoat.game.entities

import android.graphics.RectF
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
    var type: PlatformType = PlatformType.NORMAL
) : Entity(
    position = Vector2(x, y)
) {

    enum class PlatformType {
        NORMAL,     // Standard platform
        MOVING      // Slides left like a conveyor belt
    }

    companion object {
        /**
         * Get the max art width in pixels for a given platform type (across all art variants)
         */
        fun getMaxArtWidth(type: PlatformType): Float {
            val charWidth = if (type == PlatformType.NORMAL) Constants.CHAR_WIDTH_BLOCK else Constants.CHAR_WIDTH
            val maxChars = when (type) {
                PlatformType.NORMAL -> maxOf(
                    AsciiArt.PLATFORM_NORMAL_1.maxOf { it.length },
                    AsciiArt.PLATFORM_NORMAL_2.maxOf { it.length },
                    AsciiArt.PLATFORM_NORMAL_3.maxOf { it.length }
                )
                PlatformType.MOVING -> maxOf(
                    AsciiArt.PLATFORM_MOVING_1.maxOf { it.length },
                    AsciiArt.PLATFORM_MOVING_2.maxOf { it.length }
                )
            }
            return maxChars * charWidth
        }
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
     * Collision box derived from actual ASCII art dimensions,
     * shifted up by CHAR_HEIGHT to account for drawText baseline offset
     */
    override fun getBounds(): RectF {
        val art = getAsciiRepresentation()
        val charWidth = if (type == PlatformType.NORMAL) Constants.CHAR_WIDTH_BLOCK else Constants.CHAR_WIDTH
        val artWidth = art.maxOf { it.length } * charWidth
        return RectF(
            position.x,
            position.y,
            position.x + artWidth,
            position.y - Constants.CHAR_HEIGHT
        )
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
    fun init(x: Float, y: Float, platformType: PlatformType, spikes: Boolean = false) {
        position.set(x, y)
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
