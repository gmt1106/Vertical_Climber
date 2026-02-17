package com.yourpackage.mountaingoat.game.entities

import com.yourpackage.mountaingoat.game.rendering.AsciiArt
import com.yourpackage.mountaingoat.utils.Constants
import com.yourpackage.mountaingoat.utils.Vector2

/**
 * Ground entity that appears at the starting position
 * Scrolls down as player climbs up
 */
class Ground(
    x: Float = 0f,
    y: Float = 0f,
    groundWidth: Float = 1000f
) : Entity(
    position = Vector2(x, y),
    width = groundWidth,
    height = (AsciiArt.GROUND.size) * Constants.CHAR_HEIGHT
) {

    override fun update(deltaTime: Float) {
        // Ground doesn't move or update, just stays in place
    }

    override fun getAsciiRepresentation(): List<String> {
        // Repeat each ground pattern line to fill the screen width
        val totalCharsNeeded = (width / Constants.CHAR_WIDTH).toInt()
        return AsciiArt.GROUND.map { line ->
            val repetitions = (totalCharsNeeded / line.length) + 2
            line.repeat(repetitions)
        }
    }

    override fun reset() {
        super.reset()
    }
}
