package com.smfahadelahi.ktalk.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import com.smfahadelahi.ktalk.speech.OfflineSpeechOutput
import com.smfahadelahi.ktalk.speech.SpeechEngineStatus
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Uses only voices that Android marks as local (not network-required).
 * A device may not have an offline Korean or Bengali voice installed.
 */
class AndroidOfflineSpeechOutput(context: Context) : OfflineSpeechOutput {
    private val initialization = CompletableDeferred<Boolean>()
    private val pending = ConcurrentHashMap<String, kotlinx.coroutines.CancellableContinuation<Unit>>()
    @Volatile private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context.applicationContext) { result ->
            if (!initialization.isCompleted) {
                initialization.complete(result == TextToSpeech.SUCCESS)
            }
        }
    }

    override suspend fun status(): SpeechEngineStatus {
        val ready = runCatching { initialization.await() }.getOrDefault(false)
        if (!ready) return SpeechEngineStatus.Failed("Android text-to-speech engine failed to initialize.")
        val voices = tts?.voices.orEmpty()
        val koreanAvailable = voices.any { it.locale.language == "ko" && !it.isNetworkConnectionRequired }
        val bengaliAvailable = voices.any { it.locale.language == "bn" && !it.isNetworkConnectionRequired }
        return if (koreanAvailable || bengaliAvailable) {
            SpeechEngineStatus.Ready("Android TTS (offline voices only)")
        } else {
            SpeechEngineStatus.ModelMissing("Install an offline Korean or Bengali voice in Android text-to-speech settings.")
        }
    }

    override suspend fun speak(text: String, languageTag: String): Result<Unit> = runCatching {
        require(text.isNotBlank()) { "Speech text cannot be blank." }
        val initialized = initialization.await()
        check(initialized) { "Android text-to-speech engine failed to initialize." }

        val engine = checkNotNull(tts) { "Text-to-speech engine is unavailable." }
        val locale = Locale.forLanguageTag(languageTag)
        val voice: Voice = engine.voices.orEmpty().firstOrNull {
            it.locale.language == locale.language && !it.isNetworkConnectionRequired
        } ?: error("No offline voice is installed for $languageTag.")

        engine.voice = voice
        val utteranceId = UUID.randomUUID().toString()
        suspendCancellableCoroutine<Unit> { continuation ->
            pending[utteranceId] = continuation
            continuation.invokeOnCancellation {
                pending.remove(utteranceId)
                engine.stop()
            }
            engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(id: String?) = Unit

                override fun onDone(id: String?) {
                    if (id != null) pending.remove(id)?.let { if (it.isActive) it.resume(Unit) }
                }

                override fun onStop(id: String?, interrupted: Boolean) {
                    if (id != null) pending.remove(id)?.let {
                        if (it.isActive) it.resumeWithException(IllegalStateException("Speech was interrupted."))
                    }
                }

                @Deprecated("Deprecated by Android")
                override fun onError(id: String?) {
                    if (id != null) pending.remove(id)?.let {
                        if (it.isActive) it.resumeWithException(IllegalStateException("Offline speech output failed."))
                    }
                }

                override fun onError(id: String?, errorCode: Int) = onError(id)
            })
            val result = engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            if (result == TextToSpeech.ERROR) {
                pending.remove(utteranceId)
                if (continuation.isActive) {
                    continuation.resumeWithException(IllegalStateException("Text-to-speech rejected the request."))
                }
            }
        }
    }

    override fun stop() {
        tts?.stop()
        pending.forEach { (id, continuation) ->
            pending.remove(id)
            if (continuation.isActive) continuation.cancel(CancellationException("Speech stopped"))
        }
    }

    fun close() {
        stop()
        tts?.shutdown()
        tts = null
    }
}
