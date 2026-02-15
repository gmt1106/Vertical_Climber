package com.yourpackage.mountaingoat.game

import android.graphics.Canvas
import android.view.SurfaceHolder
import com.yourpackage.mountaingoat.utils.Constants

/**
 * Game loop thread that updates and renders the game at target FPS
 */
class GameThread(
    private val surfaceHolder: SurfaceHolder,
    private val gameEngine: GameEngine
) : Thread() {

    @Volatile
    var running: Boolean = false
        private set

    @Volatile
    var paused: Boolean = false

    /**
     * Start the game loop
     */
    fun startLoop() {
        running = true
        start()
    }

    /**
     * Stop the game loop
     */
    fun stopLoop() {
        running = false
        var retry = true
        while (retry) {
            try {
                join()
                retry = false
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    override fun run() {
        var lastTime = System.nanoTime()

        while (running) {
            if (!paused) {
                val currentTime = System.nanoTime()
                val deltaTime = (currentTime - lastTime) / 1_000_000_000f
                lastTime = currentTime

                // Update game logic
                gameEngine.update(deltaTime)

                // Render
                var canvas: Canvas? = null
                try {
                    canvas = surfaceHolder.lockCanvas()
                    canvas?.let {
                        synchronized(surfaceHolder) {
                            gameEngine.render(it)
                        }
                    }
                } finally {
                    canvas?.let {
                        try {
                            surfaceHolder.unlockCanvasAndPost(it)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                // Frame rate limiting
                val frameTime = (System.nanoTime() - currentTime) / 1_000_000
                if (frameTime < Constants.TARGET_FRAME_TIME) {
                    try {
                        sleep(Constants.TARGET_FRAME_TIME - frameTime)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            } else {
                // If paused, just sleep to avoid busy waiting
                try {
                    sleep(100)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }
}
