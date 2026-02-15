package com.yourpackage.mountaingoat.utils

import kotlin.math.max
import kotlin.math.min

/**
 * Utility functions for common math operations
 */
object MathUtils {

    /**
     * Linear interpolation between two values
     */
    fun lerp(start: Float, end: Float, t: Float): Float {
        return start + (end - start) * t
    }

    /**
     * Clamp a value between min and max
     */
    fun clamp(value: Float, min: Float, max: Float): Float {
        return max(min, min(max, value))
    }

    /**
     * Map a value from one range to another
     */
    fun mapRange(
        value: Float,
        fromMin: Float,
        fromMax: Float,
        toMin: Float,
        toMax: Float
    ): Float {
        val normalized = (value - fromMin) / (fromMax - fromMin)
        return toMin + normalized * (toMax - toMin)
    }

    /**
     * Check if two rectangles intersect
     */
    fun rectIntersects(
        x1: Float, y1: Float, w1: Float, h1: Float,
        x2: Float, y2: Float, w2: Float, h2: Float
    ): Boolean {
        return x1 < x2 + w2 &&
               x1 + w1 > x2 &&
               y1 < y2 + h2 &&
               y1 + h1 > y2
    }

    /**
     * Check if a point is inside a rectangle
     */
    fun pointInRect(px: Float, py: Float, rx: Float, ry: Float, rw: Float, rh: Float): Boolean {
        return px >= rx && px <= rx + rw && py >= ry && py <= ry + rh
    }
}
