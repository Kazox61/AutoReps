package com.kazox.autoreps.core.sound

import kotlin.math.PI
import kotlin.math.sin

/** Audio format every platform player is handed. Mono 16-bit is the common denominator. */
const val SAMPLE_RATE_HZ: Int = 44_100

/**
 * A short synthesised beep.
 *
 * Synthesised rather than bundled as an asset so the pitch is defined here, in one place, and is
 * identical on every platform — a rep tone that sounds different on iOS than on Android is a tone
 * the user has to learn twice.
 */
data class Tone(
    val frequencyHz: Int,
    val durationMillis: Int,
    /** 0f..1f. Well below 1 for the repeated ones: this plays next to your ear on the floor. */
    val volume: Float,
)

/**
 * The app's vocabulary of sounds.
 *
 * Pitch carries the meaning, because the ear is all you have mid-set: the countdown ticks sit
 * below the rep tone, and the "go" jumps an octave above the ticks so the start is unmistakable
 * without counting the beeps that led to it.
 */
object Tones {
    val Rep = Tone(frequencyHz = 880, durationMillis = 90, volume = 0.45f)
    val CountdownTick = Tone(frequencyHz = 587, durationMillis = 90, volume = 0.4f)
    val CountdownGo = Tone(frequencyHz = 1175, durationMillis = 260, volume = 0.55f)
}

/**
 * The tone as little-endian signed 16-bit mono PCM.
 *
 * The envelope is not decoration: a sine cut off mid-cycle ends on a step, which every speaker
 * reproduces as a click louder than the tone itself.
 */
fun Tone.toPcm16(): ByteArray {
    val sampleCount = SAMPLE_RATE_HZ * durationMillis / 1000
    val attack = (SAMPLE_RATE_HZ * ATTACK_SECONDS).toInt().coerceAtLeast(1)
    val release = (SAMPLE_RATE_HZ * RELEASE_SECONDS).toInt().coerceAtLeast(1)
    val bytes = ByteArray(sampleCount * 2)

    for (index in 0 until sampleCount) {
        val envelope =
            when {
                index < attack -> index.toFloat() / attack
                index >= sampleCount - release -> (sampleCount - index).toFloat() / release
                else -> 1f
            }
        val angle = 2.0 * PI * frequencyHz * index / SAMPLE_RATE_HZ
        val sample =
            (sin(angle) * envelope * volume * Short.MAX_VALUE)
                .toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())

        bytes[index * 2] = (sample and 0xFF).toByte()
        bytes[index * 2 + 1] = ((sample shr 8) and 0xFF).toByte()
    }
    return bytes
}

/**
 * The same samples wrapped in a 44-byte WAV header.
 *
 * For the players that want a container rather than bare samples — iOS's AVAudioPlayer and the
 * JVM's Clip both parse a format, while Android's AudioTrack takes the PCM directly.
 */
fun Tone.toWav(): ByteArray {
    val pcm = toPcm16()
    val header = ByteArray(WAV_HEADER_SIZE)
    var offset = 0

    fun ascii(value: String) {
        value.forEach { header[offset++] = it.code.toByte() }
    }

    fun int32(value: Int) {
        header[offset++] = (value and 0xFF).toByte()
        header[offset++] = ((value shr 8) and 0xFF).toByte()
        header[offset++] = ((value shr 16) and 0xFF).toByte()
        header[offset++] = ((value shr 24) and 0xFF).toByte()
    }

    fun int16(value: Int) {
        header[offset++] = (value and 0xFF).toByte()
        header[offset++] = ((value shr 8) and 0xFF).toByte()
    }

    val byteRate = SAMPLE_RATE_HZ * CHANNELS * BITS_PER_SAMPLE / 8

    ascii("RIFF")
    int32(WAV_HEADER_SIZE - 8 + pcm.size)
    ascii("WAVE")
    ascii("fmt ")
    int32(16) // PCM subchunk size
    int16(1) // format: uncompressed PCM
    int16(CHANNELS)
    int32(SAMPLE_RATE_HZ)
    int32(byteRate)
    int16(CHANNELS * BITS_PER_SAMPLE / 8) // block align
    int16(BITS_PER_SAMPLE)
    ascii("data")
    int32(pcm.size)

    return header + pcm
}

private const val ATTACK_SECONDS = 0.005f
private const val RELEASE_SECONDS = 0.02f
private const val CHANNELS = 1
private const val BITS_PER_SAMPLE = 16
private const val WAV_HEADER_SIZE = 44
