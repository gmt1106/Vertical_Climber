package com.yourpackage.mountaingoat.game.physics

import android.graphics.RectF
import com.yourpackage.mountaingoat.game.entities.Platform
import com.yourpackage.mountaingoat.game.entities.Player
import com.yourpackage.mountaingoat.utils.Constants
import com.yourpackage.mountaingoat.utils.MathUtils

/**
 * Collision detection system for game entities
 */
object CollisionDetector {

    /**
     * Check if player is landing on a platform
     * Rules:
     * - Player must be moving downward
     * - Player's bottom edge must be near platform's top edge
     * - Player can pass through platforms when moving upward
     */
    fun checkPlatformCollision(player: Player, platform: Platform): Boolean {
        if (!platform.active) return false

        val playerBounds = player.getBounds()
        val platformBounds = platform.getBounds()

        // Must have basic intersection
        if (!rectIntersects(playerBounds, platformBounds)) {
            return false
        }

        // Player must be moving downward (positive Y velocity)
        if (player.velocity.y <= 0) {
            return false
        }

        // Player's bottom must be at or above platform top (landing from above)
        // Allow some tolerance for smooth landing
        val tolerance = Constants.PLATFORM_COLLISION_TOLERANCE
        if (playerBounds.bottom > platformBounds.top + tolerance) return false

        // At least half of goat's collision width must overlap the platform horizontally
        val overlapLeft = maxOf(playerBounds.left, platformBounds.left)
        val overlapRight = minOf(playerBounds.right, platformBounds.right)
        val overlapWidth = overlapRight - overlapLeft
        return overlapWidth >= player.width * Constants.PLATFORM_X_OVERLAP_RATIO
    }

    /**
     * Check if two rectangles intersect
     */
    fun rectIntersects(rect1: RectF, rect2: RectF): Boolean {
        return MathUtils.rectIntersects(
            rect1.left, rect1.top, rect1.width(), rect1.height(),
            rect2.left, rect2.top, rect2.width(), rect2.height()
        )
    }

    /**
     * Check if player collides with obstacle (instant death, no invulnerability)
     */
    fun checkObstacleCollision(player: Player, obstacleBounds: RectF): Boolean {
        val playerBounds = player.getBounds()
        return rectIntersects(playerBounds, obstacleBounds)
    }

    /**
     * Get collision point between player and platform
     * Returns the Y coordinate where player should land
     *
     * player.height is dynamically calculated as (lines - 1) * CHAR_HEIGHT
     * This aligns the last ASCII line (feet) baseline with the platform position
     */
    fun getCollisionPoint(player: Player, platform: Platform): Float {
        return platform.position.y - player.height - (Constants.CHAR_HEIGHT * 2)
    }

    /**
     * Check if entity is completely off screen
     * @param entityBounds Entity bounds
     * @param cameraY Top of camera view in world space
     * @param screenHeight Height of screen
     * @param buffer Extra buffer for early cleanup
     */
    fun isOffScreen(
        entityBounds: RectF,
        cameraY: Float,
        screenHeight: Int,
        buffer: Float = 200f
    ): Boolean {
        return entityBounds.bottom < cameraY - buffer ||
               entityBounds.top > cameraY + screenHeight + buffer
    }
}
