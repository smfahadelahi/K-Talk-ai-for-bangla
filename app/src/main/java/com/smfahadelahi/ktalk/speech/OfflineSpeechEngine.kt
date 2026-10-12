package com.smfahadelahi.ktalk.speech

sealed interface SpeechEngineStatus {
    data class Ready(val engineName: String) : SpeechEngineStatus
    data class ModelMissing(val detail: String) : SpeechEngineStatus
    data class Failed(val detail: String) : SpeechEngineStatus
}

/** Local Korean speech recognition adapter. No network fallback is permitted. */
interface OfflineSpeechRecognizer {
    suspend fun status(): SpeechEngineStatus
    suspend fun transcribe(audioPcm16: ShortArray, sampleRateHz: Int): Result<String>
}

/** Local speech output adapter; voice availability must be checked at runtime. */
interface OfflineSpeechOutput {
    suspend fun status(): SpeechEngineStatus
    suspend fun speak(text: String, languageTag: String): Result<Unit>
    fun stop()
}
