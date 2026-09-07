package com.kazox.autoreps.core.sound

import java.io.ByteArrayInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip

/**
 * A [Clip] per tone, opened once from the generated WAV.
 *
 * Desktop is the review target rather than a place anyone works out, so this only has to be
 * audible and not throw on a machine with no sound card — hence the swallowed failures.
 */
private class ClipSoundPlayer : SoundPlayer {
    private val clips = mutableMapOf<Tone, Clip?>()

    override fun play(tone: Tone) {
        val clip = clips.getOrPut(tone) { openClip(tone) } ?: return
        // Rewind first: a clip left at its end plays nothing at all on the second call.
        clip.stop()
        clip.framePosition = 0
        clip.start()
    }

    override fun release() {
        clips.values.filterNotNull().forEach { it.close() }
        clips.clear()
    }

    private fun openClip(tone: Tone): Clip? =
        runCatching {
            AudioSystem.getClip().apply {
                open(AudioSystem.getAudioInputStream(ByteArrayInputStream(tone.toWav())))
            }
        }.getOrNull()
}

actual fun createSoundPlayer(): SoundPlayer = ClipSoundPlayer()
