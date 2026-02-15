package com.yourpackage.mountaingoat.game.systems

import android.view.MotionEvent
import com.yourpackage.mountaingoat.utils.Constants
import com.yourpackage.mountaingoat.utils.MathUtils
import com.yourpackage.mountaingoat.utils.Vector2
import kotlin.math.min

/**
 * Manages slingshot mechanics: touch input, aiming, and launch
 * Pull DOWN = launch UP (opposite direction, like a real slingshot)
 */
class SlingshotManager(
    private val getPlayerPosition: () -> Vector2
) {

    // Slingshot state
    var isAiming: Boolean = false
        private set

    // Touch tracking
    private var touchStartPos: Vector2? = null
    private var pullPosition: Vector2? = null
    private var isTracking: Boolean = false

    // State flags for GameEngine
    var shouldTransitionToAiming: Boolean = false
        private set
    var shouldTransitionToJumping: Boolean = false
        private set

    /**
     * Handle touch events - main entry point from GameView
     */
    fun handleTouchEvent(event: MotionEvent, currentGameState: String): Boolean {
        // Clear state flags
        shouldTransitionToAiming = false
        shouldTransitionToJumping = false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                return onTouchDown(event.x, event.y, currentGameState)
            }
            MotionEvent.ACTION_MOVE -> {
                return onTouchMove(event.x, event.y, currentGameState)
            }
            MotionEvent.ACTION_UP -> {
                return onTouchUp(event.x, event.y, currentGameState)
            }
            MotionEvent.ACTION_CANCEL -> {
                return onTouchCancel()
            }
        }
        return false
    }

    /**
     * Handle touch down - start aiming if in READY state
     */
    private fun onTouchDown(x: Float, y: Float, gameState: String): Boolean {
        if (gameState == "READY") {
            touchStartPos = Vector2(x, y)
            pullPosition = Vector2(x, y)
            isTracking = true
            isAiming = true
            shouldTransitionToAiming = true
            return true
        }
        return false
    }

    /**
     * Handle touch move - update pull position
     */
    private fun onTouchMove(x: Float, y: Float, gameState: String): Boolean {
        if (!isTracking) return false

        if (gameState == "AIMING") {
            pullPosition = Vector2(x, y)
            return true
        }
        return false
    }

    /**
     * Handle touch up - release slingshot
     * Keep touchStartPos alive for getLaunchVelocity() to use
     */
    private fun onTouchUp(x: Float, y: Float, gameState: String): Boolean {
        if (!isTracking) return false

        if (gameState == "AIMING" && touchStartPos != null) {
            pullPosition = Vector2(x, y)
            shouldTransitionToJumping = true
            isTracking = false
            return true
        }

        // Cancel if not in correct state
        touchStartPos = null
        isTracking = false
        return false
    }

    /**
     * Handle touch cancel
     */
    private fun onTouchCancel(): Boolean {
        if (!isTracking) return false

        cancel()
        touchStartPos = null
        isTracking = false
        return true
    }

    /**
     * Get launch velocity and reset slingshot state
     * Called by GameEngine when transitioning to JUMPING
     */
    fun getLaunchVelocity(): Vector2 {
        val velocity = calculateLaunchVelocity()

        // Reset all state
        isAiming = false
        pullPosition = null
        touchStartPos = null

        return velocity
    }

    /**
     * Cancel aiming
     */
    private fun cancel() {
        isAiming = false
        pullPosition = null
        touchStartPos = null
    }

    /**
     * Calculate launch velocity based on pull gesture
     * Pull direction = pullPosition - touchStartPos (where user dragged)
     * Launch direction = OPPOSITE of pull (like a real slingshot)
     */
    private fun calculateLaunchVelocity(): Vector2 {
        val pull = pullPosition ?: return Vector2.ZERO
        val start = touchStartPos ?: return Vector2.ZERO

        // Pull vector: direction user dragged (from initial touch to release)
        val pullVector = Vector2(
            pull.x - start.x,
            pull.y - start.y
        )

        // Limit pull distance
        val pullDistance = min(pullVector.magnitude(), Constants.MAX_PULL_DISTANCE)

        // If pull is too small, return zero velocity
        if (pullDistance < 10f) {
            return Vector2.ZERO
        }

        // Launch OPPOSITE to pull direction (negate = real slingshot behavior)
        // Pull DOWN → launch UP, pull DOWN-RIGHT → launch UP-LEFT
        val launchDirection = Vector2(-pullVector.x, -pullVector.y).normalize()

        // Map pull distance to velocity magnitude (longer pull = faster launch)
        val velocityMag = MathUtils.mapRange(
            pullDistance,
            0f, Constants.MAX_PULL_DISTANCE,
            Constants.MIN_LAUNCH_VELOCITY, Constants.MAX_LAUNCH_VELOCITY
        )

        return launchDirection * velocityMag
    }

    /**
     * Reset slingshot state
     */
    fun reset() {
        isAiming = false
        pullPosition = null
        touchStartPos = null
        isTracking = false
        shouldTransitionToAiming = false
        shouldTransitionToJumping = false
    }
}
