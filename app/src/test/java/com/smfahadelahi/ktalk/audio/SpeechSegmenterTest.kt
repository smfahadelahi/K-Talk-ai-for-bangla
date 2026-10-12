package com.smfahadelahi.ktalk.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

class SpeechSegmenterTest {
    @Test
    fun emitsUtteranceAfterSustainedSpeechAndSilence() {
        val segmenter = SpeechSegmenter()
        val speech = ShortArray(320) { 2_000 }
        val silence = ShortArray(320) { 0 }

        repeat(20) { assertNull(segmenter.push(speech, speech.size)) }

        var result: ShortArray? = null
        repeat(30) {
            val emitted = segmenter.push(silence, silence.size)
            if (emitted != null) result = emitted
        }

        assertNotNull(result)
        assertEquals(16_000 * 20 / 1_000 * 50, result!!.size)
    }

    @Test
    fun discardsShortNoiseBelowMinimumSpeechDuration() {
        val segmenter = SpeechSegmenter()
        val noise = ShortArray(320) { 2_000 }
        val silence = ShortArray(320) { 0 }

        repeat(3) { segmenter.push(noise, noise.size) }
        var result: ShortArray? = null
        repeat(30) {
            val emitted = segmenter.push(silence, silence.size)
            if (emitted != null) result = emitted
        }

        assertNull(result)
    }
}
