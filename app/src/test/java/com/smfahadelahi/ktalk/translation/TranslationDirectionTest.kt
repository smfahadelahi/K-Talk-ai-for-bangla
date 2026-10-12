package com.smfahadelahi.ktalk.translation

import org.junit.Assert.assertEquals
import org.junit.Test

class TranslationDirectionTest {
    @Test
    fun bothDirectionsAreAvailable() {
        assertEquals(
            listOf(TranslationDirection.KOREAN_TO_BANGLA, TranslationDirection.BANGLA_TO_KOREAN),
            TranslationDirection.entries
        )
    }
}
