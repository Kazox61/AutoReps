package com.kazox.autoreps.core.sound

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ToneTest {
    private val tone = Tone(frequencyHz = 880, durationMillis = 100, volume = 0.5f)

    @Test
    fun `pcm holds two bytes per sample for the requested duration`() {
        val expectedSamples = SAMPLE_RATE_HZ * 100 / 1000
        assertEquals(expectedSamples * 2, tone.toPcm16().size)
    }

    @Test
    fun `tone starts and ends silent`() {
        val samples = tone.toPcm16().toShorts()

        // The envelope's whole job: a sine cut off mid-cycle ends on a step, which reproduces as
        // a click louder than the tone. Both ends must ramp through zero.
        assertEquals(0, samples.first())
        assertTrue(abs(samples.last().toInt()) < 500, "ends at ${samples.last()}, expected near 0")
    }

    @Test
    fun `tone reaches close to the requested volume in the middle`() {
        val samples = tone.toPcm16().toShorts()
        val peak = samples.maxOf { abs(it.toInt()) }
        val expected = (Short.MAX_VALUE * 0.5f).toInt()

        assertTrue(
            peak in (expected * 9 / 10)..expected,
            "peak $peak should be just under $expected",
        )
    }

    @Test
    fun `wav header describes the samples that follow it`() {
        val wav = tone.toWav()
        val pcmSize = tone.toPcm16().size

        assertEquals("RIFF", wav.ascii(0, 4))
        assertEquals("WAVE", wav.ascii(8, 4))
        assertEquals("fmt ", wav.ascii(12, 4))
        assertEquals("data", wav.ascii(36, 4))

        assertEquals(HEADER_SIZE + pcmSize, wav.size)
        assertEquals(HEADER_SIZE - 8 + pcmSize, wav.int32(4), "RIFF chunk size")
        assertEquals(1, wav.int16(20), "format should be uncompressed PCM")
        assertEquals(1, wav.int16(22), "channels")
        assertEquals(SAMPLE_RATE_HZ, wav.int32(24), "sample rate")
        assertEquals(SAMPLE_RATE_HZ * 2, wav.int32(28), "byte rate")
        assertEquals(16, wav.int16(34), "bits per sample")
        assertEquals(pcmSize, wav.int32(40), "data chunk size")
    }

    @Test
    fun `every tone the app uses is audible and short`() {
        listOf(Tones.Rep, Tones.CountdownTick, Tones.CountdownGo).forEach { tone ->
            assertTrue(tone.frequencyHz in 100..8_000, "${tone.frequencyHz} Hz is outside speech range")
            assertTrue(tone.durationMillis in 20..500, "${tone.durationMillis} ms")
            assertTrue(tone.volume > 0f && tone.volume <= 1f, "volume ${tone.volume}")
            assertTrue(tone.toPcm16().isNotEmpty())
        }
    }

    private companion object {
        const val HEADER_SIZE = 44
    }
}

private fun ByteArray.toShorts(): List<Short> =
    (indices step 2).map { index ->
        ((this[index].toInt() and 0xFF) or (this[index + 1].toInt() shl 8)).toShort()
    }

private fun ByteArray.ascii(offset: Int, length: Int): String =
    (offset until offset + length).map { this[it].toInt().toChar() }.joinToString("")

private fun ByteArray.int16(offset: Int): Int =
    (this[offset].toInt() and 0xFF) or ((this[offset + 1].toInt() and 0xFF) shl 8)

private fun ByteArray.int32(offset: Int): Int =
    int16(offset) or (int16(offset + 2) shl 16)
