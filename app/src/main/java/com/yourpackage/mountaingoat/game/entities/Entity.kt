package com.yourpackage.mountaingoat.game.entities

import android.graphics.RectF
import com.yourpackage.mountaingoat.utils.Vector2

/**
 * Base class for all game entities (player, platforms, obstacles)
 */
abstract class Entity(
    val position: Vector2 = Vector2(),
    val velocity: Vector2 = Vector2(),
    var width: Float = 0f,
    var height: Float = 0f
) {
    var active: Boolean = true

    /**
     * Update entity logic
     * @param deltaTime Time elapsed since last frame in seconds
     */
    abstract fun update(deltaTime: Float)

    /**
     * Get the bounding box for collision detection
     */
    open fun getBounds(): RectF {
        return RectF(
            position.x,
            position.y,
            position.x + width,
            position.y + height
        )
    }

    /**
     * Get ASCII art representation of this entity
     * @return List of strings, one per line of ASCII art
     */
    abstract fun getAsciiRepresentation(): List<String>

    /**
     * Reset entity to inactive state (for object pooling)
     */
    open fun reset() {
        active = false
        position.set(0f, 0f)
        velocity.set(0f, 0f)
    }
}
