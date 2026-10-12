package com.smfahadelahi.ktalk.translation

import com.smfahadelahi.ktalk.phrasebook.Phrasebook

/**
 * Exact-match translation over reviewed phrasebook entries only.
 * Unknown text returns null; it is never guessed or machine-translated here.
 */
object OfflinePhraseTranslator {
    fun translateExact(input: String): TranslationResult? {
        val normalized = normalize(input)
        if (normalized.isBlank()) return null

        val isKorean = input.any { it in '\uAC00'..'\uD7A3' }
        val isBengali = input.any { it in '\u0980'..'\u09FF' }

        return when {
            isKorean -> {
                val phrase = Phrasebook.phrases.firstOrNull { normalize(it.korean) == normalized }
                    ?: return null
                TranslationResult(
                    sourceText = input,
                    translatedText = phrase.banglaMeaning,
                    direction = TranslationDirection.KOREAN_TO_BANGLA
                )
            }
            isBengali -> {
                val phrase = Phrasebook.phrases.firstOrNull { normalize(it.banglaMeaning) == normalized }
                    ?: return null
                TranslationResult(
                    sourceText = input,
                    translatedText = phrase.korean,
                    direction = TranslationDirection.BANGLA_TO_KOREAN
                )
            }
            else -> null
        }
    }

    private fun normalize(value: String): String =
        value.trim()
            .lowercase()
            .replace(Regex("[.!?。！？,،]+$"), "")
            .replace(Regex("\\s+"), " ")
}
