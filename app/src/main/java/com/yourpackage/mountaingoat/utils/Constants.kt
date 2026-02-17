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
    const val MAX_PULL_DISTANCE = 900f // maximum pixels player can pull slingshot
    const val MIN_LAUNCH_VELOCITY = 300f // minimum launch speed
    const val MAX_LAUNCH_VELOCITY = 1500f // maximum launch speed
    const val MAX_LAUNCH_ANGLE = 70f // max degrees from vertical (straight up) for launch direction
    const val TRAJECTORY_POINTS = 30 // number of points for trajectory calculation
    const val TRAJECTORY_TIME_STEP = 0.05f // time between trajectory points

    // Rendering Constants
    const val TARGET_FPS = 60
    const val TARGET_FRAME_TIME = 1000L / TARGET_FPS // milliseconds per frame

    // Text sizes for different rendering contexts
    const val TEXT_SIZE_ENTITY = 20f // ASCII art entities (goat, platforms, ground)
    const val TEXT_SIZE_UI = 40f // UI overlay (distance display)
    const val TEXT_SIZE_GAME_OVER_SUB = 36f // Game over sub text (distance, tap to restart)
    const val TEXT_SIZE_MILESTONE = 90f // Milestone level progression text

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
    const val PLATFORM_MAX_SPACING = 600f // 70% of maximum jump height (challenging but achievable)

    // Moving Platform Constants
    const val MOVING_PLATFORM_SPEED = 80f // pixels per second sliding left
    const val PLATFORM_ANIMATION_INTERVAL = 0.3f // seconds between art frame toggles
    const val MOVING_PLATFORM_CHANCE = 0.3f // 30% chance to spawn a MOVING platform
    // 70% chance to spawn a NORMAL platform
    const val SPIKE_PLATFORM_CHANCE = 0.15f // 15% chance to spawn spikes on NORMAL platform
    // 0.7 * 0.15 = 0.1025 = 10.25% chance to spawn spiked NORMAL platform


    // Camera Constants
    const val CAMERA_FOLLOW_THRESHOLD = 0.5f // follow when player is above this % of screen
    const val CAMERA_LERP_SPEED = 0.1f // smooth camera following speed

    // Distance Tracking
    const val PIXELS_PER_METER = 100f // 100 pixels = 1 meter

    // Auto-Scroll
    const val AUTO_SCROLL_SPEED = 100f // pixels per second (camera moves up)

    // Game State
    const val SCREEN_BOTTOM_DEATH_OFFSET = 200f // how far below camera before game over
    const val FALL_OFF_PLATFORM_VELOCITY = 50f // initial downward velocity when goat falls off moving platform
    const val MILESTONE_INTERVAL_METERS = 10f // distance in meters between milestone notifications
    const val MILESTONE_DISPLAY_DURATION = 1f // seconds to show milestone text on screen

    // Collision
    const val PLATFORM_COLLISION_TOLERANCE = 300f // pixels of tolerance for landing on platforms
    const val PLATFORM_X_OVERLAP_RATIO = 0.5f // minimum fraction of goat width that must overlap platform to land

    // Pause Button
    const val PAUSE_BUTTON_PADDING = 20f // padding from screen edges
    const val PAUSE_BUTTON_SIZE = 60f // width and height of the tap target
    const val PAUSE_BUTTON_Y = 20f // top offset for the button
}
