package com.yourpackage.mountaingoat.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.yourpackage.mountaingoat.game.GameEngine
import com.yourpackage.mountaingoat.game.GameState
import com.yourpackage.mountaingoat.game.GameThread
import com.yourpackage.mountaingoat.utils.Constants

/**
 * Custom view for rendering the game
 */
class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback {

    private lateinit var gameEngine: GameEngine
    private lateinit var gameThread: GameThread

    init {
        holder.addCallback(this)
        isFocusable = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        // Compute content area below terminal title bar
        val contentOffsetY = (height * Constants.TERMINAL_TITLE_BAR_RATIO).toInt()
        val contentHeight = height - contentOffsetY

        // Initialize game engine with content area dimensions
        gameEngine = GameEngine(context, width, contentHeight, contentOffsetY)

        // Create and start game thread
        gameThread = GameThread(holder, gameEngine)
        gameThread.startLoop()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Handle surface size changes if needed
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // Stop game thread
        if (::gameThread.isInitialized) {
            gameThread.stopLoop()
        }
        // Release sound resources
        if (::gameEngine.isInitialized) {
            gameEngine.releaseSoundPool()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!::gameEngine.isInitialized) return false

        // Delegate touch handling to GameEngine (which uses InputManager + SlingshotManager)
        return gameEngine.handleTouchEvent(event)
    }

    /**
     * Pause the game
     */
    fun pauseGame() {
        if (::gameThread.isInitialized) gameThread.paused = true
        if (::gameEngine.isInitialized) gameEngine.pause()
    }

    /**
     * Resume the game
     */
    fun resumeGame() {
        if (::gameThread.isInitialized) gameThread.paused = false
        if (::gameEngine.isInitialized) gameEngine.resume()
    }

    /**
     * Reset the game
     */
    fun resetGame() {
        if (::gameEngine.isInitialized) gameEngine.reset()
    }

    /**
     * Check if currently in active gameplay (not intro or title)
     */
    fun isInActiveGame(): Boolean {
        if (!::gameEngine.isInitialized) return false
        val state = gameEngine.gameState
        return state != GameState.INTRO && state != GameState.TITLE && state != GameState.PAUSED
    }
}
