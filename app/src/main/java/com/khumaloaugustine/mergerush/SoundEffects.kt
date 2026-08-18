package com.khumaloaugustine.mergerush

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/** Modern synthesized game sounds: offline, instant, and free of legacy phone tones. */
class SoundEffects {
    private val sampleRate = 44_100
    private val player = Executors.newSingleThreadExecutor()
    @Volatile private var released = false

    fun move() = sequence(Tone(330.0, 32, .12))
    fun merge(combo: Int) {
        val lift = (combo.coerceAtMost(5) - 1) * 55.0
        sequence(Tone(523.25 + lift, 75, .26), Tone(659.25 + lift, 105, .22))
    }
    fun milestone() = sequence(Tone(523.25, 80, .25), Tone(659.25, 80, .26), Tone(783.99, 90, .27), Tone(1046.50, 190, .30))
    fun win() = sequence(Tone(523.25, 100, .28), Tone(659.25, 100, .28), Tone(783.99, 110, .30), Tone(1046.50, 260, .34, harmony = 659.25))

    /** A warm upward retry cue, deliberately avoiding a gloomy loss fanfare. */
    fun lose() = sequence(Tone(392.0, 100, .18), Tone(440.0, 130, .20), Tone(523.25, 170, .22))
    fun powerUp() = sequence(Tone(440.0, 55, .20), Tone(587.33, 55, .22), Tone(739.99, 70, .24), Tone(880.0, 130, .26))

    private fun sequence(vararg tones: Tone) {
        if (released) return
        player.execute {
            if (released) return@execute
            val samples = tones.flatMap { synthesize(it).asIterable() }.toShortArray()
            val track = AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(sampleRate).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(samples.size * 2)
                .build()
            track.write(samples, 0, samples.size)
            track.play()
            Thread.sleep(tones.sumOf { it.durationMs }.toLong() + 40)
            track.stop()
            track.release()
        }
    }

    private fun synthesize(tone: Tone): ShortArray {
        val count = sampleRate * tone.durationMs / 1000
        return ShortArray(count) { index ->
            val time = index.toDouble() / sampleRate
            val progress = index.toDouble() / count
            val envelope = (progress / .08).coerceAtMost(1.0) * exp(-3.8 * progress)
            val fundamental = sin(2 * PI * tone.frequency * time)
            val shimmer = .18 * sin(2 * PI * tone.frequency * 2.0 * time)
            val harmony = tone.harmony?.let { .22 * sin(2 * PI * it * time) } ?: 0.0
            ((fundamental + shimmer + harmony) * envelope * tone.volume * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    fun release() { released = true; player.shutdownNow() }
    private data class Tone(val frequency: Double, val durationMs: Int, val volume: Double, val harmony: Double? = null)
}
