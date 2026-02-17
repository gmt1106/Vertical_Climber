package com.yourpackage.mountaingoat.game.systems

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.yourpackage.mountaingoat.R
import com.yourpackage.mountaingoat.utils.Constants

/**
 * Manages the terminal intro sequence: typewriter text reveal with sound, then disappear
 */
class IntroManager(context: Context) {

    private val messages = listOf(
        "> Do you want to play the Mountain Goat Game?",
        "> Press enter to start!",
        "> ",
        "> Okay! Let's start!"
    )

    // Animation state
    private var timer: Float = 0f
    private var messageIndex: Int = 0
    private var charIndex: Int = 0
    private var typingDone: Boolean = false
    private var allMessagesDone: Boolean = false
    private var disappearing: Boolean = false
    private var disappearedCount: Int = 0

    // Typing sound (own SoundPool, separate from gameplay sounds)
    private val soundPool: SoundPool
    private val typingSoundId: Int
    private var typingStreamId: Int = 0

    init {
        val audioAttrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder().setMaxStreams(2).setAudioAttributes(audioAttrs).build()
        typingSoundId = soundPool.load(context, R.raw.typing_keyboard_sound, 1)
    }

    /**
     * Advance the intro animation by deltaTime
     */
    fun update(deltaTime: Float) {
        timer += deltaTime
        if (!disappearing) {
            if (!allMessagesDone) {
                if (!typingDone) {
                    // Typing characters one by one
                    val currentMsg = messages[messageIndex]
                    if (charIndex < currentMsg.length) {
                        // Start looping sound when typing begins
                        if (typingStreamId == 0) {
                            typingStreamId = soundPool.play(typingSoundId, 0.5f, 0.5f, 1, -1, 1.0f)
                        }
                        if (timer >= Constants.INTRO_CHAR_DELAY) {
                            charIndex++
                            timer = 0f
                        }
                    } else {
                        // Current message fully typed — stop sound, pause before next
                        stopTypingSound()
                        typingDone = true
                        timer = 0f
                    }
                } else {
                    // Waiting after message is fully typed
                    if (timer >= Constants.INTRO_MESSAGE_DELAY) {
                        messageIndex++
                        if (messageIndex >= messages.size) {
                            allMessagesDone = true
                        } else {
                            charIndex = 0
                            typingDone = false
                        }
                        timer = 0f
                    }
                }
            } else {
                // All messages done, wait then start disappearing
                if (timer >= Constants.INTRO_FINAL_PAUSE) {
                    disappearing = true
                    timer = 0f
                }
            }
        } else {
            // Messages disappearing from top to bottom
            if (timer >= Constants.INTRO_DISAPPEAR_DELAY) {
                disappearedCount++
                timer = 0f
            }
        }
    }

    /**
     * Get the list of strings to render (with typewriter partial reveal)
     */
    fun getDisplayLines(): List<String> {
        val lines = mutableListOf<String>()
        // Fully typed messages (skip disappeared ones)
        for (i in disappearedCount until messageIndex.coerceAtMost(messages.size)) {
            lines.add(messages[i])
        }
        // Currently typing message (partial)
        if (!allMessagesDone && messageIndex < messages.size && messageIndex >= disappearedCount) {
            lines.add(messages[messageIndex].take(charIndex))
        }
        return lines
    }

    /**
     * Skip the intro (stops sound immediately)
     */
    fun skip() {
        stopTypingSound()
    }

    /**
     * Whether the entire intro sequence has finished
     */
    fun isDone(): Boolean = disappearing && disappearedCount >= messages.size

    /**
     * Release sound resources
     */
    fun release() {
        stopTypingSound()
        soundPool.release()
    }

    private fun stopTypingSound() {
        if (typingStreamId != 0) {
            soundPool.stop(typingStreamId)
            typingStreamId = 0
        }
    }
}
