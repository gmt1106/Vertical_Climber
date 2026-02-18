package com.yourpackage.mountaingoat.game.entities

import com.yourpackage.mountaingoat.utils.Constants

/**
 * Manages all game entities with object pooling
 */
class EntityManager {

    // Entity collections
    val platforms = mutableListOf<Platform>()
    private val obstacles = mutableListOf<Entity>() // Will be used in Phase 5

    // Object pools for performance
    private val platformPool = mutableListOf<Platform>()
    private val maxPoolSize = 50

    init {
        // Pre-allocate some platforms
        for (i in 0 until 20) {
            platformPool.add(Platform())
        }
    }

    /**
     * Update all active entities
     */
    fun updateAll(deltaTime: Float) {
        // Update platforms
        platforms.forEach { platform ->
            if (platform.active) {
                platform.update(deltaTime)
            }
        }

        // Update obstacles (Phase 5)
        obstacles.forEach { obstacle ->
            if (obstacle.active) {
                obstacle.update(deltaTime)
            }
        }
    }

    /**
     * Get all active entities for rendering
     */
    fun getAllActiveEntities(): List<Entity> {
        val entities = mutableListOf<Entity>()
        entities.addAll(platforms.filter { it.active })
        entities.addAll(obstacles.filter { it.active })
        return entities
    }

    /**
     * Get all active platforms
     */
    fun getActivePlatforms(): List<Platform> {
        return platforms.filter { it.active }
    }

    /**
     * Create a new platform (from pool if possible)
     */
    fun createPlatform(
        x: Float,
        y: Float,
        type: Platform.PlatformType = Platform.PlatformType.NORMAL,
        hasSpikes: Boolean = false
    ): Platform {
        // Try to get from pool
        val platform = if (platformPool.isNotEmpty()) {
            platformPool.removeAt(platformPool.size - 1)
        } else {
            Platform()
        }

        // Initialize platform
        platform.init(x, y, type, hasSpikes)
        platforms.add(platform)

        return platform
    }

    /**
     * Remove inactive entities and return them to pool
     */
    fun cleanupInactive(cameraY: Float, screenHeight: Int) {
        // Cleanup platforms
        val iterator = platforms.iterator()
        while (iterator.hasNext()) {
            val platform = iterator.next()
            if (!platform.active || platform.position.y > cameraY + screenHeight) {
                // Return to pool if pool not full
                if (platformPool.size < maxPoolSize) {
                    platform.reset()
                    platformPool.add(platform)
                }
                iterator.remove()
            }
        }

    }

    /**
     * Clear all entities
     */
    fun clear() {
        platforms.forEach { platform ->
            if (platformPool.size < maxPoolSize) {
                platform.reset()
                platformPool.add(platform)
            }
        }
        platforms.clear()
        obstacles.clear()
    }

    /**
     * Get the highest platform Y position
     */
    fun getHighestPlatformY(): Float {
        return platforms.filter { it.active }
            .minByOrNull { it.position.y }
            ?.position?.y
            ?: Float.MAX_VALUE
    }

    /**
     * Get platform count
     */
    fun getPlatformCount(): Int = platforms.count { it.active }
}
