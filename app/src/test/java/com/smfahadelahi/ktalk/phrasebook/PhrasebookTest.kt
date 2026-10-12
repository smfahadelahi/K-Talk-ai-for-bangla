package com.smfahadelahi.ktalk.phrasebook

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhrasebookTest {
    @Test
    fun restaurantCustomerHasUsefulPoliteReplies() {
        val phrases = Phrasebook.suggestions(
            ConversationRole.CUSTOMER,
            PhraseScenario.RESTAURANT
        )

        assertTrue(phrases.any { it.korean == "메뉴판 주세요." })
        assertTrue(phrases.all { it.banglaMeaning.isNotBlank() })
    }

    @Test
    fun phrasebookContainsShopkeeperAndEmergencyPhrases() {
        assertFalse(
            Phrasebook.suggestions(ConversationRole.STAFF, PhraseScenario.RESTAURANT).isEmpty()
        )
        assertFalse(
            Phrasebook.suggestions(ConversationRole.TRAVELER, PhraseScenario.HELP).isEmpty()
        )
    }
}
