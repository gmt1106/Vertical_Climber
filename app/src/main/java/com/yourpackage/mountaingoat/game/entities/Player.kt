package com.yourpackage.mountaingoat.game.entities

import android.graphics.RectF
import com.yourpackage.mountaingoat.game.rendering.AsciiArt
import com.yourpackage.mountaingoat.utils.Constants
import com.yourpackage.mountaingoat.utils.Vector2

/**
 * Player entity - Mountain Goat character
 */
class Player(startX: Float, startY: Float) : Entity(
    position = Vector2(startX, startY),
    width = 0f,
    height = 0f
) {

    enum class State {
        IDLE,       // Standing on platform
        FLYING,     // In the air after launch
        LANDING     // About to land on platform
    }

    enum class Direction {
        LEFT,
        RIGHT
    }

    var state: State = State.IDLE
    var direction: Direction = Direction.RIGHT

    init {
        updateCollisionFromIdle()
        updateDimensions()
    }

    /**
     * Update width and height based on current ASCII art
     * Height = (lines - 1) * CHAR_HEIGHT so that the last line baseline aligns with the bottom
     * Width and offset based on the feet line (last line) for accurate platform collision
     */
    // Horizontal offset from position.x to where the feet start (based on IDLE art)
    var collisionOffsetX: Float = 0f
        private set

    // Full visual width of the ASCII art (for screen boundary clamping)
    var visualWidth: Float = 0f
        private set

    private fun updateCollisionFromIdle() {
        val idleArt = if (direction == Direction.RIGHT) AsciiArt.GOAT_IDLE_RIGHT else AsciiArt.GOAT_IDLE_LEFT
        val feetLine = idleArt.last()
        val feetTrimmed = feetLine.trim()

        width = feetTrimmed.length * Constants.CHAR_WIDTH
        val leadingSpaces = feetLine.length - feetLine.trimStart().length
        collisionOffsetX = leadingSpaces * Constants.CHAR_WIDTH
    }

    private fun updateDimensions() {
        val asciiLines = getAsciiRepresentation()
        height = asciiLines.size * Constants.CHAR_HEIGHT
        visualWidth = asciiLines.maxOf { it.length } * Constants.CHAR_WIDTH
    }

    /**
     * Collision box offset by leading spaces so it matches the visible art
     */
    override fun getBounds(): RectF {
        return RectF(
            position.x + collisionOffsetX,
            position.y,
            position.x + collisionOffsetX + width,
            position.y + height
        )
    }

    override fun update(deltaTime: Float) {
        // Update position based on velocity
        position.x += velocity.x * deltaTime
        position.y += velocity.y * deltaTime

        // Determine state based on velocity
        val previousState = state
        state = when {
            velocity.y == 0f -> State.IDLE
            velocity.y > 0 -> State.LANDING
            else -> State.FLYING
        }

        // Recalculate dimensions when state changes (different ASCII art sizes)
        if (state != previousState) {
            updateDimensions()
        }
    }

    /**
     * Launch the goat with a given velocity
     */
    fun launch(launchVelocity: Vector2) {
        velocity.set(launchVelocity.x, launchVelocity.y)
        state = State.FLYING

        // Update direction based on horizontal velocity
        if (launchVelocity.x > 0) {
            direction = Direction.RIGHT
        } else if (launchVelocity.x < 0) {
            direction = Direction.LEFT
        }

        updateCollisionFromIdle()
        updateDimensions()
    }

    /**
     * Make the goat land on a platform
     */
    fun land() {
        velocity.set(0f, 0f)
        state = State.IDLE
        updateDimensions()
    }

    override fun getAsciiRepresentation(): List<String> {
        // Use detailed mountain goat ASCII art based on state and direction
        return when (state) {
            State.IDLE -> {
                if (direction == Direction.RIGHT) {
                    AsciiArt.GOAT_IDLE_RIGHT
                } else {
                    AsciiArt.GOAT_IDLE_LEFT
                }
            }
            State.FLYING -> {
                if (direction == Direction.RIGHT) {
                    AsciiArt.GOAT_JUMPING_RIGHT
                } else {
                    AsciiArt.GOAT_JUMPING_LEFT
                }
            }
            State.LANDING -> {
                if (direction == Direction.RIGHT) {
                    AsciiArt.GOAT_LANDING_RIGHT
                } else {
                    AsciiArt.GOAT_LANDING_LEFT
                }
            }
        }
    }

    override fun reset() {
        super.reset()
        state = State.IDLE
        updateCollisionFromIdle()
        updateDimensions()
    }
}
