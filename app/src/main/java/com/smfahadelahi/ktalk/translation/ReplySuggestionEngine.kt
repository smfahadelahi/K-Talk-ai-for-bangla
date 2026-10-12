package com.smfahadelahi.ktalk.translation

data class ReplySuggestion(
    val korean: String,
    val banglaMeaning: String,
    val reasonBn: String
)

/**
 * Deterministic, fully local phrase suggestions for common Korean situations.
 * This is a transparent rules/phrasebook engine, not a generative AI model.
 */
object ReplySuggestionEngine {
    fun suggestReplyToKorean(heardText: String): List<ReplySuggestion> {
        val text = heardText.trim().lowercase()
        if (text.isBlank()) return emptyList()

        return when {
            listOf("주문", "뭐 드릴", "드시겠", "메뉴").any(text::contains) -> listOf(
                ReplySuggestion("비빔밥 하나 주세요.", "একটা বিবিমবাপ দিন, দয়া করে।", "খাবার অর্ডার"),
                ReplySuggestion("김치찌개 하나 주세요.", "এক বাটি কিমচি-চিগে দিন, দয়া করে।", "খাবার অর্ডার"),
                ReplySuggestion("추천 메뉴가 뭐예요?", "আপনাদের কোন খাবারটা ভালো?", "খাবার সম্পর্কে জানতে")
            )
            listOf("포장", "여기서 드", "매장").any(text::contains) -> listOf(
                ReplySuggestion("여기서 먹을게요.", "আমি এখানে খেতে চাই।", "এখানে খাওয়ার জন্য"),
                ReplySuggestion("포장해 주세요.", "প্যাক করে দিন, দয়া করে।", "প্যাকেট নেওয়ার জন্য")
            )
            listOf("맵", "매운").any(text::contains) -> listOf(
                ReplySuggestion("맵지 않게 해 주세요.", "ঝাল কম করে দিন, দয়া করে।", "কম ঝালের জন্য"),
                ReplySuggestion("조금 매워도 괜찮아요.", "একটু ঝাল হলেও সমস্যা নেই।", "ঝাল খাবার গ্রহণ করতে")
            )
            listOf("카드", "결제", "계산").any(text::contains) -> listOf(
                ReplySuggestion("카드로 결제할게요.", "আমি কার্ডে পেমেন্ট করব।", "কার্ডে পেমেন্ট"),
                ReplySuggestion("현금으로 할게요.", "আমি নগদ টাকা দেব।", "নগদ পেমেন্ট")
            )
            listOf("어디", "어떻게", "몇 번", "몇 시").any(text::contains) -> listOf(
                ReplySuggestion("천천히 말씀해 주세요.", "একটু ধীরে বলবেন, দয়া করে?", "কথা বুঝতে অসুবিধা হলে"),
                ReplySuggestion("다시 한 번 말씀해 주세요.", "আরেকবার বলবেন, দয়া করে?", "আবার শুনতে চাইলে")
            )
            listOf("괜찮", "필요").any(text::contains) -> listOf(
                ReplySuggestion("네, 괜찮아요.", "জি, ঠিক আছে।", "সম্মতি জানাতে"),
                ReplySuggestion("아니요, 괜찮습니다. 감사합니다.", "না, লাগবে না। ধন্যবাদ।", "ভদ্রভাবে না বলতে")
            )
            else -> listOf(
                ReplySuggestion("죄송하지만, 잘 이해하지 못했어요.", "দুঃখিত, আমি ভালোভাবে বুঝতে পারিনি।", "নিরাপদ সাধারণ উত্তর"),
                ReplySuggestion("천천히 말씀해 주세요.", "একটু ধীরে বলবেন, দয়া করে?", "ধীরে বলতে অনুরোধ"),
                ReplySuggestion("다시 한 번 말씀해 주세요.", "আরেকবার বলবেন, দয়া করে?", "আবার বলতে অনুরোধ")
            )
        }
    }
}
