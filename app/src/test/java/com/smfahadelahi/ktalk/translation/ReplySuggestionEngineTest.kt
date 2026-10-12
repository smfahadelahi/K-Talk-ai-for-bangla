package com.smfahadelahi.ktalk.translation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReplySuggestionEngineTest {
    @Test
    fun restaurantOrderProducesRelevantKoreanReplies() {
        val suggestions = ReplySuggestionEngine.suggestReplyToKorean("주문하시겠어요?")
        assertTrue(suggestions.any { it.korean == "비빔밥 하나 주세요." })
        assertTrue(suggestions.all { it.banglaMeaning.isNotBlank() })
    }

    @Test
    fun unknownSpeechOffersClarificationPhrasesInsteadOfInventingMeaning() {
        val suggestions = ReplySuggestionEngine.suggestReplyToKorean("낯선 문장")
        assertFalse(suggestions.isEmpty())
        assertTrue(suggestions.any { it.korean == "다시 한 번 말씀해 주세요." })
    }
}
