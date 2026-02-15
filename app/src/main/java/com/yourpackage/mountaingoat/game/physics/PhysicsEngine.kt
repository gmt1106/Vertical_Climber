package com.yourpackage.mountaingoat.game.physics

import com.yourpackage.mountaingoat.game.entities.Entity
import com.yourpackage.mountaingoat.utils.Constants
import com.yourpackage.mountaingoat.utils.Vector2

/**
 * Physics engine for applying forces and calculating trajectories
 */
object PhysicsEngine {

    /**
     * Apply gravity to an entity's velocity
     */
    fun applyGravity(entity: Entity, deltaTime: Float) {
        entity.velocity.y += Constants.GRAVITY * deltaTime

        // Clamp to terminal velocity
        if (entity.velocity.y > Constants.TERMINAL_VELOCITY) {
            entity.velocity.y = Constants.TERMINAL_VELOCITY
        }
    }

    /**
     * Update entity position based on velocity
     */
    fun updateVelocity(entity: Entity, deltaTime: Float) {
        entity.position.x += entity.velocity.x * deltaTime
        entity.position.y += entity.velocity.y * deltaTime

        // Apply air resistance
        entity.velocity.x *= Constants.AIR_RESISTANCE
        entity.velocity.y *= Constants.AIR_RESISTANCE
    }

    /**
     * Calculate trajectory arc for slingshot preview
     * @param startPos Starting position
     * @param velocity Launch velocity
     * @return List of points along the predicted arc
     */
    fun calculateTrajectory(startPos: Vector2, velocity: Vector2): List<Vector2> {
        val points = mutableListOf<Vector2>()

        // Simulate physics for trajectory preview
        var currentPos = startPos.copy()
        var currentVel = velocity.copy()

        for (i in 0 until Constants.TRAJECTORY_POINTS) {
            points.add(currentPos.copy())

            // Apply physics
            currentVel.y += Constants.GRAVITY * Constants.TRAJECTORY_TIME_STEP
            currentVel.x *= Constants.AIR_RESISTANCE
            currentVel.y *= Constants.AIR_RESISTANCE

            currentPos.x += currentVel.x * Constants.TRAJECTORY_TIME_STEP
            currentPos.y += currentVel.y * Constants.TRAJECTORY_TIME_STEP
        }

        return points
    }

    /**
     * Calculate velocity needed to reach a target position
     * @param from Starting position
     * @param to Target position
     * @return Velocity vector needed
     */
    fun calculateVelocityToReach(from: Vector2, to: Vector2): Vector2 {
        val dx = to.x - from.x
        val dy = to.y - from.y

        // Simple parabolic arc calculation
        val time = 1.0f // Assume 1 second flight time
        val vx = dx / time
        val vy = (dy / time) - (0.5f * Constants.GRAVITY * time)

        return Vector2(vx, vy)
    }
}
