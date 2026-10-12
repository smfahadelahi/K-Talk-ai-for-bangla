package com.smfahadelahi.ktalk.speech

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

/**
 * Uses Android's explicit on-device speech-recognition service only.
 * It does not fall back to the general/cloud recognizer. Availability and
 * language support depend on the speech service and installed offline packs.
 */
class AndroidOnDeviceSpeechRecognizer(context: Context) {
    private val appContext = context.applicationContext

    fun isAvailable(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            SpeechRecognizer.isOnDeviceRecognitionAvailable(appContext)

    suspend fun recognizeOnce(languageTag: String): Result<String> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return Result.failure(IllegalStateException("On-device speech recognition requires Android 12 or newer."))
        }
        if (!isAvailable()) {
            return Result.failure(IllegalStateException("No on-device speech-recognition service is available on this device."))
        }

        return suspendCancellableCoroutine { continuation ->
            val recognizer = try {
                SpeechRecognizer.createOnDeviceSpeechRecognizer(appContext)
            } catch (e: Exception) {
                continuation.resume(Result.failure(e))
                return@suspendCancellableCoroutine
            }
            val completed = AtomicBoolean(false)

            fun finish(result: Result<String>) {
                if (completed.compareAndSet(false, true)) {
                    runCatching { recognizer.destroy() }
                    if (continuation.isActive) continuation.resume(result)
                }
            }

            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) = Unit
                override fun onBeginningOfSpeech() = Unit
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() = Unit
                override fun onPartialResults(partialResults: Bundle?) = Unit
                override fun onEvent(eventType: Int, params: Bundle?) = Unit

                override fun onResults(results: Bundle?) {
                    val transcript = results
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        ?.trim()
                    if (transcript.isNullOrBlank()) {
                        finish(Result.failure(IllegalStateException("No speech was recognized. Try again in a quieter place.")))
                    } else {
                        finish(Result.success(transcript))
                    }
                }

                override fun onError(error: Int) {
                    val message = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
                        SpeechRecognizer.ERROR_CLIENT -> "Speech recognizer client error."
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required."
                        SpeechRecognizer.ERROR_NETWORK,
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "The on-device speech service reported a network-related error; no cloud fallback was attempted."
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech was recognized. Try speaking more clearly."
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy. Try again shortly."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech was detected."
                        else -> "On-device speech recognition failed (code $error)."
                    }
                    finish(Result.failure(IllegalStateException(message)))
                }
            })

            continuation.invokeOnCancellation {
                if (completed.compareAndSet(false, true)) {
                    runCatching { recognizer.cancel() }
                    runCatching { recognizer.destroy() }
                }
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageTag)
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }

            try {
                recognizer.startListening(intent)
            } catch (e: Exception) {
                finish(Result.failure(IllegalStateException("Could not start on-device speech recognition.", e)))
            }
        }
    }
}
