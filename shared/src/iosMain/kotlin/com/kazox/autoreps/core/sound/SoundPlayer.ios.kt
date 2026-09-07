package com.kazox.autoreps.core.sound

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionMixWithOthers
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.create

/**
 * An [AVAudioPlayer] per tone, built once from the generated WAV.
 *
 * Two things make iOS harder than the other platforms here, and both are why the session is
 * re-asserted on every play instead of configured once:
 *
 * 1. [AVCaptureSession] reconfigures the app's shared audio session when it starts running, and
 *    on this screen the camera starts *after* this player is built. Configuring in `init` alone
 *    means the camera silently replaces our category a moment later.
 * 2. Playback is the right category despite ignoring the ring/silent switch. The tone is the
 *    feedback — you are face down and cannot see the counter — so a muted phone must not mute
 *    the rep count, the same call every interval timer makes. MixWithOthers keeps it layered
 *    over whatever music is already playing rather than interrupting it.
 */
@OptIn(ExperimentalForeignApi::class)
private class AVAudioSoundPlayer : SoundPlayer {
    private val players = mutableMapOf<Tone, AVAudioPlayer>()

    override fun play(tone: Tone) {
        configureSession()

        val player = players[tone] ?: buildPlayer(tone)?.also { players[tone] = it } ?: return
        // Rewinding rather than letting it run out: reps can arrive faster than a tone decays,
        // and without this the second one is dropped because the player is still busy.
        player.currentTime = 0.0
        if (!player.play()) warn("AVAudioPlayer refused to play $tone")
    }

    override fun release() {
        players.values.forEach { it.stop() }
        players.clear()
    }

    /**
     * Puts the shared session back into the category this player needs.
     *
     * Called before every tone rather than once: cheap when nothing changed, and the only way to
     * survive the capture session taking the audio session over mid-workout. Activation is not
     * repeated once it has succeeded — that part does stick.
     */
    private fun configureSession() {
        val session = AVAudioSession.sharedInstance()
        if (session.category == AVAudioSessionCategoryPlayback && activated) return

        memScoped {
            val error = alloc<ObjCObjectVar<NSError?>>()
            val configured =
                session.setCategory(
                    category = AVAudioSessionCategoryPlayback,
                    withOptions = AVAudioSessionCategoryOptionMixWithOthers,
                    error = error.ptr,
                )
            if (!configured) {
                warn("setCategory failed: ${error.value?.localizedDescription}")
                return@memScoped
            }

            val active = session.setActive(true, error.ptr)
            if (active) activated = true else warn("setActive failed: ${error.value?.localizedDescription}")
        }
    }

    private var activated = false

    private fun buildPlayer(tone: Tone): AVAudioPlayer? =
        memScoped {
            val wav = tone.toWav()
            val data =
                wav.usePinned { pinned ->
                    // dataWithBytes:length: copies, so the samples outlive the pinning.
                    NSData.create(bytes = pinned.addressOf(0), length = wav.size.toULong())
                }

            val error = alloc<ObjCObjectVar<NSError?>>()
            val player = runCatching { AVAudioPlayer(data = data, error = error.ptr) }.getOrNull()
            if (player == null) {
                warn("could not build a player for $tone: ${error.value?.localizedDescription}")
                return@memScoped null
            }

            player.prepareToPlay()
            player
        }

    /**
     * Silence used to be indistinguishable from a tone that never got built. It goes to stdout,
     * which is where the Xcode console reads from.
     */
    private fun warn(message: String) = println("AutoReps sound: $message")
}

actual fun createSoundPlayer(): SoundPlayer = AVAudioSoundPlayer()
