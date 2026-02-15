package com.yourpackage.mountaingoat.utils

import kotlin.math.sqrt

/**
 * 2D Vector class for position, velocity, and direction calculations
 */
data class Vector2(var x: Float = 0f, var y: Float = 0f) {

    /**
     * Calculate the magnitude (length) of this vector
     */
    fun magnitude(): Float {
        return sqrt(x * x + y * y)
    }

    /**
     * Return a normalized version of this vector (length = 1)
     */
    fun normalize(): Vector2 {
        val mag = magnitude()
        return if (mag > 0) {
            Vector2(x / mag, y / mag)
        } else {
            Vector2(0f, 0f)
        }
    }

    /**
     * Add another vector to this one
     */
    operator fun plus(other: Vector2): Vector2 {
        return Vector2(x + other.x, y + other.y)
    }

    /**
     * Subtract another vector from this one
     */
    operator fun minus(other: Vector2): Vector2 {
        return Vector2(x - other.x, y - other.y)
    }

    /**
     * Multiply this vector by a scalar
     */
    operator fun times(scalar: Float): Vector2 {
        return Vector2(x * scalar, y * scalar)
    }

    /**
     * Divide this vector by a scalar
     */
    operator fun div(scalar: Float): Vector2 {
        return Vector2(x / scalar, y / scalar)
    }

    /**
     * Calculate dot product with another vector
     */
    fun dot(other: Vector2): Float {
        return x * other.x + y * other.y
    }

    /**
     * Calculate distance to another vector
     */
    fun distanceTo(other: Vector2): Float {
        val dx = other.x - x
        val dy = other.y - y
        return sqrt(dx * dx + dy * dy)
    }

    /**
     * Set the values of this vector
     */
    fun set(newX: Float, newY: Float) {
        x = newX
        y = newY
    }

    /**
     * Copy this vector
     */
    fun copy(): Vector2 {
        return Vector2(x, y)
    }

    companion object {
        val ZERO = Vector2(0f, 0f)
        val UP = Vector2(0f, -1f)
        val DOWN = Vector2(0f, 1f)
        val LEFT = Vector2(-1f, 0f)
        val RIGHT = Vector2(1f, 0f)
    }
}
