package com.smfahadelahi.ktalk.audio

import kotlin.math.sqrt

/**
 * Lightweight energy-based speech segmenter for 16 kHz mono PCM16 audio.
 * This is a pragmatic first-pass VAD, not a neural speech detector; tune on
 * real environments before relying on it for noisy conversations.
 */
internal class SpeechSegmenter(
    private val sampleRateHz: Int = 16_000,
    private val frameMillis: Int = 20,
    private val silenceToFinishMillis: Int = 600,
    private val minimumSpeechMillis: Int = 250,
    private val maximumUtteranceMillis: Int = 12_000,
    private val rmsThreshold: Double = 550.0
) {
    private val frameSamples = sampleRateHz * frameMillis / 1_000
    private val silenceFramesToFinish = silenceToFinishMillis / frameMillis
    private val minimumSpeechFrames = minimumSpeechMillis / frameMillis
    private val maximumFrames = maximumUtteranceMillis / frameMillis
    private val utterance = ArrayList<Short>(sampleRateHz * 3)
    private var speechFrames = 0
    private var silenceFrames = 0
    private var inSpeech = false

    fun push(frame: ShortArray, length: Int): ShortArray? {
        if (length <= 0) return null
        val rms = rms(frame, length)
        val voiced = rms >= rmsThreshold

        if (voiced) {
            inSpeech = true
            speechFrames++
            silenceFrames = 0
        } else if (inSpeech) {
            silenceFrames++
        }

        if (inSpeech) {
            for (i in 0 until length) utterance.add(frame[i])
        }

        val reachedSilence = inSpeech && silenceFrames >= silenceFramesToFinish
        val reachedMaximum = inSpeech && utterance.size >= frameSamples * maximumFrames
        if (reachedSilence || reachedMaximum) return finish()

        return null
    }

    fun flush(): ShortArray? = if (inSpeech) finish() else null

    private fun finish(): ShortArray? {
        val result = if (speechFrames >= minimumSpeechFrames) utterance.toShortArray() else null
        utterance.clear()
        speechFrames = 0
        silenceFrames = 0
        inSpeech = false
        return result
    }

    private fun rms(samples: ShortArray, length: Int): Double {
        var sum = 0.0
        for (i in 0 until length) {
            val value = samples[i].toDouble()
            sum += value * value
        }
        return sqrt(sum / length)
    }
}
