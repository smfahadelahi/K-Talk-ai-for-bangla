package com.smfahadelahi.ktalk.translation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OfflinePhraseTranslatorTest {
    @Test
    fun translatesKnownKoreanPhraseToBengali() {
        val result = OfflinePhraseTranslator.translateExact("메뉴판 주세요.")
        assertEquals("মেনুটা দিন, দয়া করে।", result?.translatedText)
        assertEquals(TranslationDirection.KOREAN_TO_BANGLA, result?.direction)
    }

    @Test
    fun translatesKnownBengaliPhraseToKorean() {
        val result = OfflinePhraseTranslator.translateExact("একটু পানি দিন, দয়া করে।")
        assertEquals("물 좀 주세요.", result?.translatedText)
        assertEquals(TranslationDirection.BANGLA_TO_KOREAN, result?.direction)
    }

    @Test
    fun unknownSentenceIsNotGuessed() {
        assertNull(OfflinePhraseTranslator.translateExact("이 문장은 사전에 없습니다"))
    }
}
