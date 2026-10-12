package com.smfahadelahi.ktalk.translation

enum class TranslationDirection {
    KOREAN_TO_BANGLA,
    BANGLA_TO_KOREAN
}

data class TranslationResult(
    val sourceText: String,
    val translatedText: String,
    val direction: TranslationDirection
)

sealed interface TranslationStatus {
    data class Ready(val engineName: String) : TranslationStatus
    data class ModelMissing(val detail: String) : TranslationStatus
    data class Failed(val detail: String) : TranslationStatus
}

/**
 * Implementations must run locally. Do not silently fall back to a cloud API
 * or return fabricated translations when a model is missing.
 */
interface TranslationEngine {
    suspend fun status(): TranslationStatus
    suspend fun translate(
        text: String,
        direction: TranslationDirection
    ): Result<TranslationResult>
}
