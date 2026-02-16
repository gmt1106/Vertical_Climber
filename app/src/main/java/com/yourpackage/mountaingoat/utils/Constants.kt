package com.yourpackage.mountaingoat.utils

/**
 * Game constants and configuration values
 */
object Constants {

    // Physics Constants
    const val GRAVITY = 980f // pixels per second squared (realistic gravity feel)
    const val TERMINAL_VELOCITY = 1500f // maximum falling speed
    const val AIR_RESISTANCE = 0.98f // velocity multiplier per frame (slight air drag)

    // Slingshot Constants
    const val MAX_PULL_DISTANCE = 150f // maximum pixels player can pull slingshot
    const val MIN_LAUNCH_VELOCITY = 300f // minimum launch speed
    const val MAX_LAUNCH_VELOCITY = 1500f // maximum launch speed
    const val LAUNCH_FORCE_MULTIPLIER = 10f // multiplier for pull distance to velocity
    const val TRAJECTORY_POINTS = 30 // number of points for trajectory calculation
    const val TRAJECTORY_TIME_STEP = 0.05f // time between trajectory points

    // Rendering Constants
    const val TARGET_FPS = 60
    const val TARGET_FRAME_TIME = 1000L / TARGET_FPS // milliseconds per frame

    // Text sizes for different rendering contexts
    const val TEXT_SIZE_ENTITY = 20f // ASCII art entities (goat, platforms, ground)
    const val TEXT_SIZE_UI = 40f // UI overlay (distance display)
    const val TEXT_SIZE_GAME_OVER_SUB = 36f // Game over sub text (distance, tap to restart)

    // Character dimensions derived from entity text size
    // For monospace font: width ≈ textSize * 0.6, height ≈ textSize
    const val CHAR_WIDTH = TEXT_SIZE_ENTITY * 0.6f // width of one monospace character
    const val CHAR_HEIGHT = TEXT_SIZE_ENTITY // height of one line of text

    // Platform Constants
    const val PLATFORM_MIN_WIDTH = 200f
    const val PLATFORM_MAX_WIDTH = 300f
    const val PLATFORM_HEIGHT = 40f

    // Platform spacing based on physics (jump height = v² / (2*g))
    // MAX_JUMP_HEIGHT = (1500)² / (2 * 980) ≈ 1148 pixels
    // MIN_JUMP_HEIGHT = (300)² / (2 * 980) ≈ 46 pixels
    const val PLATFORM_MIN_SPACING = 400f // 40% of maximum jump height and larger than minimum jump height (reachable with weak pull)
    const val PLATFORM_MAX_SPACING = 700f // 70% of maximum jump height (challenging but achievable)
    const val PLATFORM_HORIZONTAL_SPREAD = 0.7f // platforms spawn within screen width * this value

    // Camera Constants
    const val CAMERA_FOLLOW_THRESHOLD = 0.5f // follow when player is above this % of screen
    const val CAMERA_LERP_SPEED = 0.1f // smooth camera following speed

    // Distance Tracking
    const val PIXELS_PER_METER = 100f // 100 pixels = 1 meter

    // Level Constants
    const val PIXELS_PER_LEVEL = 1000 // height needed to advance one level
    const val PLATFORM_GENERATION_BATCH = 1 // generate 1 platform at a time (gradual)

    // Auto-Scroll
    const val AUTO_SCROLL_SPEED = 100f // pixels per second (camera moves up)

    // Game State
    const val STARTING_POSITION_Y = 100f // initial player Y position from bottom
    const val SCREEN_BOTTOM_DEATH_OFFSET = 200f // how far below camera before game over
}
