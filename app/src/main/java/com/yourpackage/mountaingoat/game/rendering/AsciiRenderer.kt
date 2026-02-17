package com.yourpackage.mountaingoat.game.rendering

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.yourpackage.mountaingoat.game.entities.Entity
import android.graphics.RectF
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

    private val milestonePaint = Paint().apply {
        color = Color.WHITE
        textSize = Constants.TEXT_SIZE_MILESTONE
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

    private val playButtonPaint = Paint().apply {
        color = Color.WHITE
        textSize = Constants.TEXT_SIZE_UI * 2f
        typeface = Typeface.MONOSPACE
        isAntiAlias = true
    }

    private val pausedOverlayPaint = Paint().apply {
        color = Color.argb(150, 0, 0, 0)
    }

    private val pausedTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = Constants.TEXT_SIZE_MILESTONE
        typeface = Typeface.MONOSPACE
        isAntiAlias = true
    }

    private val pausedSubPaint = Paint().apply {
        color = Color.LTGRAY
        textSize = Constants.TEXT_SIZE_GAME_OVER_SUB
        typeface = Typeface.MONOSPACE
        isAntiAlias = true
    }

    // Pause button bounds (top-right corner)
    val pauseButtonBounds = RectF(
        screenWidth - Constants.PAUSE_BUTTON_PADDING - Constants.PAUSE_BUTTON_SIZE,
        Constants.PAUSE_BUTTON_Y,
        screenWidth - Constants.PAUSE_BUTTON_PADDING,
        Constants.PAUSE_BUTTON_Y + Constants.PAUSE_BUTTON_SIZE
    )

    // Camera position (Y coordinate of top of screen in world space)
    var cameraPosY: Float = 0f

    /**
     * Render the entire game state
     */
    fun render(
        canvas: Canvas,
        entities: List<Entity>,
        distanceMeters: Float = 0f,
        isGameOver: Boolean = false,
        isPaused: Boolean = false,
        milestoneText: String? = null
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
        renderUI(canvas, distanceMeters, isPaused, milestoneText)

        // Render paused overlay
        if (isPaused) {
            renderPaused(canvas)
        }

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
     * Render UI overlay (distance top-left, pause button top-right)
     */
    private fun renderUI(canvas: Canvas, distanceMeters: Float, isPaused: Boolean, milestoneText: String? = null) {
        // Top-left: Distance in meters
        val distanceText = "> ${distanceMeters.toInt()}m"
        canvas.drawText(distanceText, Constants.PAUSE_BUTTON_PADDING, 50f, uiPaint)

        // Top-right: Pause / Play button
        if (isPaused) {
            val buttonText = "▶"
            val buttonTextWidth = playButtonPaint.measureText(buttonText)
            val buttonX = pauseButtonBounds.right - buttonTextWidth
            val buttonY = pauseButtonBounds.bottom - (pauseButtonBounds.height() - playButtonPaint.textSize) / 2
            canvas.drawText(buttonText, buttonX, buttonY, playButtonPaint)
        } else {
            val buttonText = "▐▐ "
            val buttonTextWidth = uiPaint.measureText(buttonText)
            val buttonX = pauseButtonBounds.right - buttonTextWidth
            val buttonY = pauseButtonBounds.bottom - (pauseButtonBounds.height() - uiPaint.textSize) / 2
            canvas.drawText(buttonText, buttonX, buttonY, uiPaint)
        }

        // Milestone message
        if (milestoneText != null) {
            val milestoneWidth = milestonePaint.measureText(milestoneText)
            canvas.drawText(milestoneText, (screenWidth - milestoneWidth) / 2, screenHeight / 2f, milestonePaint)
        }
    }

    /**
     * Render paused overlay
     */
    private fun renderPaused(canvas: Canvas) {
        // Semi-transparent dark overlay
        canvas.drawRect(0f, 0f, screenWidth.toFloat(), screenHeight.toFloat(), pausedOverlayPaint)

        val centerX = screenWidth / 2f
        val centerY = screenHeight / 2f

        // "PAUSED" text
        val pausedText = "PAUSED"
        val pausedWidth = pausedTextPaint.measureText(pausedText)
        canvas.drawText(pausedText, centerX - pausedWidth / 2, centerY, pausedTextPaint)

        // "Tap ▶ to resume" hint
        val hintText = "Tap  ▶  to resume"
        val hintWidth = pausedSubPaint.measureText(hintText)
        canvas.drawText(hintText, centerX - hintWidth / 2, centerY + 60f, pausedSubPaint)
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
