package com.smfahadelahi.ktalk.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Process
import androidx.core.content.ContextCompat
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/**
 * Captures microphone audio locally and emits complete utterances after silence.
 * Audio is kept in memory and is not written to disk or sent over a network.
 */
class AudioCaptureController(
    private val context: Context,
    private val onUtterance: (ShortArray, Int) -> Unit,
    private val onError: (String) -> Unit
) {
    companion object {
        const val SAMPLE_RATE_HZ = 16_000
    }

    private val running = AtomicBoolean(false)
    @Volatile private var recorder: AudioRecord? = null
    @Volatile private var worker: Thread? = null

    fun start(): Boolean {
        if (running.get()) return true
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            onError("Microphone permission is required.")
            return false
        }

        val minBuffer = AudioRecord.getMinBufferSize(
            SAMPLE_RATE_HZ,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (minBuffer <= 0) {
            onError("This device does not support the requested audio format.")
            return false
        }

        val audioRecord = try {
            AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                SAMPLE_RATE_HZ,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                maxOf(minBuffer * 2, SAMPLE_RATE_HZ)
            )
        } catch (e: SecurityException) {
            onError("Microphone access was denied by Android.")
            return false
        } catch (e: IllegalArgumentException) {
            onError("Could not initialize microphone capture.")
            return false
        }

        if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
            audioRecord.release()
            onError("Microphone could not be initialized.")
            return false
        }

        recorder = audioRecord
        return try {
            audioRecord.startRecording()
            running.set(true)
            worker = thread(name = "KBridge-AudioCapture") {
                Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)
                captureLoop(audioRecord)
            }
            true
        } catch (e: Exception) {
            running.set(false)
            recorder = null
            runCatching { audioRecord.release() }
            onError("Microphone capture failed to start.")
            false
        }
    }

    fun stop() {
        running.set(false)
        runCatching { recorder?.stop() }
        worker?.join(700)
        worker = null
        recorder?.release()
        recorder = null
    }

    private fun captureLoop(audioRecord: AudioRecord) {
        val frame = ShortArray(SAMPLE_RATE_HZ * 20 / 1_000)
        val segmenter = SpeechSegmenter()
        try {
            while (running.get()) {
                val count = audioRecord.read(frame, 0, frame.size, AudioRecord.READ_BLOCKING)
                if (count > 0) {
                    val utterance = segmenter.push(frame, count)
                    if (utterance != null && running.get()) onUtterance(utterance, SAMPLE_RATE_HZ)
                } else if (count < 0 && running.get()) {
                    onError("Audio capture read failed (code $count).")
                    break
                }
            }
            segmenter.flush()?.let { onUtterance(it, SAMPLE_RATE_HZ) }
        } catch (_: IllegalStateException) {
            if (running.get()) onError("Audio capture stopped unexpectedly.")
        } finally {
            running.set(false)
        }
    }
}
