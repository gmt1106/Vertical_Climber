package com.yourpackage.mountaingoat.game.rendering

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.yourpackage.mountaingoat.game.entities.Entity
import com.yourpackage.mountaingoat.utils.Constants
import com.yourpackage.mountaingoat.utils.Vector2
import androidx.core.graphics.toColorInt

/**
 * Rendering system for ASCII art graphics
 */
class AsciiRenderer(private val screenWidth: Int, private val screenHeight: Int) {

    // Paint objects for rendering
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = Constants.TEXT_SIZE_ENTITY
        typeface = Typeface.MONOSPACE
        isAntiAlias = true
    }

    private val backgroundPaint = Paint().apply {
        color = "#1A1A1A".toColorInt()
    }

    private val uiPaint = Paint().apply {
        color = Color.WHITE
        textSize = Constants.TEXT_SIZE_UI
        typeface = Typeface.MONOSPACE
        isAntiAlias = true
    }

    private val overlayPaint = Paint().apply {
        color = Color.argb(180, 0, 0, 0)
    }

    // Game over ASCII art paint - text size calculated dynamically to fit screen
    private val gameOverArtPaint = Paint().apply {
        color = Color.WHITE
        typeface = Typeface.MONOSPACE
        isAntiAlias = true
        // Calculate text size so the longest line of GAME_OVER art fits screen width (with padding)
        val longestLine = AsciiArt.GAME_OVER.maxOf { it.length }
        textSize = Constants.TEXT_SIZE_ENTITY
        val measuredWidth = measureText("X") * longestLine
        textSize = (screenWidth * 0.9f) / (measuredWidth / Constants.TEXT_SIZE_ENTITY)
    }

    private val gameOverSubPaint = Paint().apply {
        color = Color.LTGRAY
        textSize = Constants.TEXT_SIZE_GAME_OVER_SUB
        typeface = Typeface.MONOSPACE
        isAntiAlias = true
    }

    // Camera position (Y coordinate of top of screen in world space)
    var cameraPosY: Float = 0f

    /**
     * Render the entire game state
     */
    fun render(
        canvas: Canvas,
        entities: List<Entity>,
        distanceMeters: Float = 0f,
        isGameOver: Boolean = false
    ) {
        // Clear screen
        canvas.drawRect(0f, 0f, screenWidth.toFloat(), screenHeight.toFloat(), backgroundPaint)

        // Render all entities
        entities.forEach { entity ->
            if (entity.active) {
                renderEntity(canvas, entity)
            }
        }

        // Render UI overlay
        renderUI(canvas, distanceMeters)

        // Render game over overlay on top of everything
        if (isGameOver) {
            renderGameOver(canvas, distanceMeters)
        }
    }

    /**
     * Render a single entity with its ASCII art
     */
    fun renderEntity(canvas: Canvas, entity: Entity) {
        val asciiLines = entity.getAsciiRepresentation()
        val screenPos = worldToScreen(entity.position)

        // Only render if on screen (with buffer)
        if (screenPos.y > -100 && screenPos.y < screenHeight + 100) {
            asciiLines.forEachIndexed { index, line ->
                val y = screenPos.y + (index * Constants.CHAR_HEIGHT)
                canvas.drawText(line, screenPos.x, y, textPaint)
            }
        }
    }

    /**
     * Render UI overlay (distance only)
     */
    private fun renderUI(canvas: Canvas, distanceMeters: Float) {
        // Top-center: Distance in meters
        val distanceText = "${distanceMeters.toInt()}m"
        val distanceWidth = uiPaint.measureText(distanceText)
        canvas.drawText(distanceText, (screenWidth - distanceWidth) / 2, 50f, uiPaint)
    }

    /**
     * Render game over overlay with ASCII art
     */
    private fun renderGameOver(canvas: Canvas, distanceMeters: Float) {
        // Semi-transparent dark overlay
        canvas.drawRect(0f, 0f, screenWidth.toFloat(), screenHeight.toFloat(), overlayPaint)

        val centerX = screenWidth / 2f
        val artLines = AsciiArt.GAME_OVER
        val lineHeight = gameOverArtPaint.textSize * 1.1f // slight spacing between lines
        val totalArtHeight = artLines.size * lineHeight

        // Start Y so the art is centered vertically (shifted up a bit to leave room for sub-text)
        val startY = (screenHeight - totalArtHeight) / 2f - 30f

        // Draw each line of ASCII art centered horizontally
        artLines.forEachIndexed { index, line ->
            val lineWidth = gameOverArtPaint.measureText(line)
            val x = centerX - lineWidth / 2f
            val y = startY + (index * lineHeight) + lineHeight
            canvas.drawText(line, x, y, gameOverArtPaint)
        }

        // Distance text below the art
        val belowArtY = startY + totalArtHeight + 50f
        val distanceText = "${distanceMeters.toInt()}m"
        val distanceWidth = gameOverSubPaint.measureText(distanceText)
        canvas.drawText(distanceText, centerX - distanceWidth / 2, belowArtY, gameOverSubPaint)

        // "Tap to restart" hint
        val hintText = "Tap to restart"
        val hintWidth = gameOverSubPaint.measureText(hintText)
        canvas.drawText(hintText, centerX - hintWidth / 2, belowArtY + 50f, gameOverSubPaint)
    }

    /**
     * Update camera position to follow player
     */
    fun updateCamera(playerY: Float) {
        val threshold = screenHeight * Constants.CAMERA_FOLLOW_THRESHOLD
        if (playerY < cameraPosY + threshold) {
            val targetY = playerY - threshold
            cameraPosY += (targetY - cameraPosY) * Constants.CAMERA_LERP_SPEED
        }
    }

    /**
     * Convert world coordinates to screen coordinates
     */
    fun worldToScreen(worldPos: Vector2): Vector2 {
        return Vector2(
            worldPos.x,
            worldPos.y - cameraPosY
        )
    }

    /**
     * Convert screen coordinates to world coordinates
     */
    fun screenToWorld(screenPos: Vector2): Vector2 {
        return Vector2(
            screenPos.x,
            screenPos.y + cameraPosY
        )
    }
}
