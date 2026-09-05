package com.kazox.autoreps.core.sound

/**
 * Plays the app's short cues.
 *
 * Deliberately fire-and-forget with no completion callback: every caller is on a frame or a timer
 * tick and has already moved on. Overlapping plays are expected — reps can come faster than a
 * tone decays — and must not queue up behind each other.
 */
interface SoundPlayer {
    fun play(tone: Tone)

    /** Frees the platform's audio resources. The owner calls this when it goes away. */
    fun release()
}

/**
 * Builds the platform's player.
 *
 * A plain function rather than a factory class in [platformModule]: unlike the database and the
 * settings store, no platform needs a Context or any other injected handle to make one.
 */
expect fun createSoundPlayer(): SoundPlayer
