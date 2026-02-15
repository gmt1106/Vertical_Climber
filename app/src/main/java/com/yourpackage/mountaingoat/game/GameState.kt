package com.yourpackage.mountaingoat.game

/**
 * Possible states of the game
 */
enum class GameState {
    READY,      // Game loaded, ready to aim
    AIMING,     // Player is pulling slingshot
    JUMPING,    // Player is in the air
    PAUSED,     // Game is paused
    GAME_OVER   // Player died
}
