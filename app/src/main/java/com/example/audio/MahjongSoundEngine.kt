package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class MahjongSoundEngine {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    var enabled: Boolean = true

    fun playTileTap() {
        if (!enabled) return
        // Crisp ivory tile click (short pitched wood/ceramic transient)
        playToneSequence(
            listOf(
                ToneSpec(freqHz = 780.0, durationMs = 42, gain = 0.24),
                ToneSpec(freqHz = 1120.0, durationMs = 28, gain = 0.14),
            ),
        )
    }

    fun playBlockedTap() {
        if (!enabled) return
        playToneSequence(
            listOf(ToneSpec(freqHz = 210.0, durationMs = 65, gain = 0.18)),
        )
    }

    fun playTripleMatch(combo: Int) {
        if (!enabled) return
        val semitoneBoost = (combo - 1).coerceIn(0, 6)
        val pitchFactor = Math.pow(2.0, semitoneBoost / 12.0)
        playToneSequence(
            listOf(
                ToneSpec(freqHz = 523.25 * pitchFactor, durationMs = 75, gain = 0.24),
                ToneSpec(freqHz = 659.25 * pitchFactor, durationMs = 75, gain = 0.24),
                ToneSpec(freqHz = 783.99 * pitchFactor, durationMs = 130, gain = 0.28),
            ),
        )
    }

    fun playUndo() {
        if (!enabled) return
        playToneSequence(
            listOf(
                ToneSpec(freqHz = 620.0, durationMs = 45, gain = 0.18),
                ToneSpec(freqHz = 460.0, durationMs = 55, gain = 0.16),
            ),
        )
    }

    fun playShuffle() {
        if (!enabled) return
        playToneSequence(
            listOf(
                ToneSpec(freqHz = 420.0, durationMs = 45, gain = 0.16),
                ToneSpec(freqHz = 560.0, durationMs = 45, gain = 0.18),
                ToneSpec(freqHz = 720.0, durationMs = 65, gain = 0.20),
            ),
        )
    }

    fun playWinFanfare() {
        if (!enabled) return
        playToneSequence(
            listOf(
                ToneSpec(freqHz = 523.25, durationMs = 95, gain = 0.25),
                ToneSpec(freqHz = 659.25, durationMs = 95, gain = 0.25),
                ToneSpec(freqHz = 783.99, durationMs = 95, gain = 0.25),
                ToneSpec(freqHz = 1046.50, durationMs = 240, gain = 0.30),
            ),
        )
    }

    fun playLoseTone() {
        if (!enabled) return
        playToneSequence(
            listOf(
                ToneSpec(freqHz = 392.0, durationMs = 120, gain = 0.22),
                ToneSpec(freqHz = 311.13, durationMs = 200, gain = 0.22),
            ),
        )
    }

    private data class ToneSpec(val freqHz: Double, val durationMs: Int, val gain: Double)

    private fun playToneSequence(tones: List<ToneSpec>) {
        scope.launch {
            runCatching {
                val sampleRate = 22050
                val totalSamples = tones.sumOf { (sampleRate * it.durationMs) / 1000 }
                if (totalSamples <= 0) return@runCatching

                val pcm = ShortArray(totalSamples)
                var offset = 0
                for (tone in tones) {
                    val count = (sampleRate * tone.durationMs) / 1000
                    for (i in 0 until count) {
                        val t = i.toDouble() / sampleRate
                        val progress = i.toDouble() / count
                        val envelope = exp(-4.5 * progress) * (1.0 - exp(-60.0 * t))
                        val wave = sin(2.0 * PI * tone.freqHz * t) +
                            0.25 * sin(4.0 * PI * tone.freqHz * t)
                        val sample = (wave * envelope * tone.gain * Short.MAX_VALUE)
                            .toInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                        pcm[offset + i] = sample.toShort()
                    }
                    offset += count
                }

                val bufferSizeBytes = pcm.size * 2
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build(),
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setSampleRate(sampleRate)
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build(),
                    )
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .setBufferSizeInBytes(bufferSizeBytes)
                    .build()

                track.write(pcm, 0, pcm.size)
                track.play()
                val totalMs = tones.sumOf { it.durationMs }.toLong() + 60L
                kotlinx.coroutines.delay(totalMs)
                track.stop()
                track.release()
            }
        }
    }
}
