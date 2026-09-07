package com.kazox.autoreps.core.sound

import java.io.ByteArrayInputStream
import javax.sound.sampled.AudioSystem
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Checks the generated header against a real WAV parser rather than against our own reading of
 * it — hand-rolled headers are exactly the thing that looks right in a unit test and is rejected
 * by the platform.
 */
class WavFormatTest {
    @Test
    fun `javax sound accepts the generated wav`() {
        val tone = Tone(frequencyHz = 880, durationMillis = 100, volume = 0.5f)

        val stream = AudioSystem.getAudioInputStream(ByteArrayInputStream(tone.toWav()))

        assertEquals(SAMPLE_RATE_HZ.toFloat(), stream.format.sampleRate)
        assertEquals(16, stream.format.sampleSizeInBits)
        assertEquals(1, stream.format.channels)
        assertEquals(false, stream.format.isBigEndian)
        assertEquals((SAMPLE_RATE_HZ * 100 / 1000).toLong(), stream.frameLength)
    }
}
