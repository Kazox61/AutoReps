package com.kazox.autoreps.core.sound

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack

/**
 * One pre-loaded [AudioTrack] per distinct tone.
 *
 * MODE_STATIC with the samples written once, so replaying is stop/rewind/play rather than
 * building a track and pushing bytes through it — the rep tone has to land while the rep is still
 * happening, and allocating an AudioTrack per rep does not.
 */
private class AudioTrackSoundPlayer : SoundPlayer {
    private val tracks = mutableMapOf<Tone, AudioTrack>()

    // Rep tones arrive on the MediaPipe camera thread while the countdown plays from the main
    // dispatcher: concurrent plays would race the lazy track map and the stop → reload → play
    // sequence and can throw deep inside AudioTrack. One lock keeps both out — a tone is a few
    // KB of samples, contention is irrelevant at rep rate.
    @Synchronized
    override fun play(tone: Tone) {
        val track = tracks.getOrPut(tone) { buildTrack(tone) }
        if (track.state != AudioTrack.STATE_INITIALIZED) return

        // Rewinding a static track is stop() then reloadStaticData(); without the reload it
        // resumes from wherever the last play ended and the tone is silently truncated.
        if (track.playState != AudioTrack.PLAYSTATE_STOPPED) track.stop()
        track.reloadStaticData()
        track.play()
    }

    @Synchronized
    override fun release() {
        tracks.values.forEach { it.release() }
        tracks.clear()
    }

    private fun buildTrack(tone: Tone): AudioTrack {
        val pcm = tone.toPcm16()
        val track =
            AudioTrack
                .Builder()
                .setAudioAttributes(
                    AudioAttributes
                        .Builder()
                        // MEDIA, not SONIFICATION, mirroring the iOS side's Playback category:
                        // the tone is the rep count while you are face down, so it must play
                        // through silent mode and DND, and mix over workout music. SONIFICATION
                        // rides the system volume stream — a separate slider that silent mode
                        // mutes outright — so the beep would be inaudible exactly when it
                        // matters. MEDIA follows the volume the user actually has up.
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                ).setAudioFormat(
                    AudioFormat
                        .Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE_HZ)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                ).setBufferSizeInBytes(pcm.size)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

        track.write(pcm, 0, pcm.size)
        return track
    }
}

actual fun createSoundPlayer(): SoundPlayer = AudioTrackSoundPlayer()
