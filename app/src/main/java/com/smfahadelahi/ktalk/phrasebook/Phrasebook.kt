package com.smfahadelahi.ktalk.phrasebook

enum class ConversationRole(val labelBn: String) {
    CUSTOMER("ক্রেতা / কাস্টমার"),
    STAFF("দোকানদার / কর্মী"),
    TRAVELER("যাত্রী"),
    STUDENT_WORKER("শিক্ষার্থী / কর্মী")
}

enum class PhraseScenario(val labelBn: String) {
    RESTAURANT("রেস্টুরেন্ট"),
    SHOPPING("দোকান"),
    TRANSPORT("যাতায়াত"),
    WORK_STUDY("কাজ ও পড়াশোনা"),
    HELP("জরুরি সাহায্য")
}

data class Phrase(
    val korean: String,
    val banglaMeaning: String,
    val role: ConversationRole,
    val scenario: PhraseScenario,
    val pronunciationBn: String? = null
)

/**
 * Curated offline phrases. These are explicitly labelled phrasebook content,
 * not generated translations of a live speaker's words.
 */
object Phrasebook {
    val phrases: List<Phrase> = listOf(
        Phrase("메뉴판 주세요.", "মেনুটা দিন, দয়া করে।", ConversationRole.CUSTOMER, PhraseScenario.RESTAURANT, "মেনুপান জুসেয়ো"),
        Phrase("이거 하나 주세요.", "এটা একটা দিন, দয়া করে।", ConversationRole.CUSTOMER, PhraseScenario.RESTAURANT, "ইগো হানা জুসেয়ো"),
        Phrase("물 좀 주세요.", "একটু পানি দিন, দয়া করে।", ConversationRole.CUSTOMER, PhraseScenario.RESTAURANT, "মুল জোম জুসেয়ো"),
        Phrase("계산해 주세요.", "বিল করে দিন, দয়া করে।", ConversationRole.CUSTOMER, PhraseScenario.RESTAURANT, "কেসানহে জুসেয়ো"),
        Phrase("포장해 주세요.", "প্যাক করে দিন, দয়া করে।", ConversationRole.CUSTOMER, PhraseScenario.RESTAURANT, "পোজাংহে জুসেয়ো"),
        Phrase("맵지 않게 해 주세요.", "ঝাল কম করে দিন, দয়া করে।", ConversationRole.CUSTOMER, PhraseScenario.RESTAURANT, "মেপচি আনকে হে জুসেয়ো"),
        Phrase("어서 오세요.", "স্বাগতম।", ConversationRole.STAFF, PhraseScenario.RESTAURANT, "ওসো ওসেয়ো"),
        Phrase("주문하시겠어요?", "আপনি কি অর্ডার করবেন?", ConversationRole.STAFF, PhraseScenario.RESTAURANT, "জুমুনহাশিগেসসয়ো"),
        Phrase("여기서 드세요, 포장하세요?", "এখানে খাবেন, নাকি প্যাক করে দেব?", ConversationRole.STAFF, PhraseScenario.RESTAURANT, "ইয়োগিসো দুসেয়ো, পোজাংহাসেয়ো"),
        Phrase("잠시만 기다려 주세요.", "একটু অপেক্ষা করুন, দয়া করে।", ConversationRole.STAFF, PhraseScenario.RESTAURANT, "জামশিমান কিদারিয়ো জুসেয়ো"),
        Phrase("이거 얼마예요?", "এটার দাম কত?", ConversationRole.CUSTOMER, PhraseScenario.SHOPPING, "ইগো অলমায়েয়ো"),
        Phrase("다른 색 있어요?", "অন্য রং আছে?", ConversationRole.CUSTOMER, PhraseScenario.SHOPPING, "তারুন সেক ইসসয়ো"),
        Phrase("카드 돼요?", "কার্ডে পেমেন্ট করা যাবে?", ConversationRole.CUSTOMER, PhraseScenario.SHOPPING, "খাদু দোয়েয়ো"),
        Phrase("봉투 주세요.", "একটা ব্যাগ দিন, দয়া করে।", ConversationRole.CUSTOMER, PhraseScenario.SHOPPING, "বংতু জুসেয়ো"),
        Phrase("잠시만요. 확인해 드릴게요.", "একটু অপেক্ষা করুন। আমি দেখে দিচ্ছি।", ConversationRole.STAFF, PhraseScenario.SHOPPING),
        Phrase("이 역에서 내리면 돼요?", "এই স্টেশনে নামলেই হবে?", ConversationRole.TRAVELER, PhraseScenario.TRANSPORT, "ই ইওগেসো নেরিমিয়ন দোয়েয়ো"),
        Phrase("여기 어떻게 가요?", "এখানে কীভাবে যাব?", ConversationRole.TRAVELER, PhraseScenario.TRANSPORT, "ইয়োগি অত্তকে গায়ো"),
        Phrase("이 버스 서울역에 가요?", "এই বাস কি সিউল স্টেশনে যায়?", ConversationRole.TRAVELER, PhraseScenario.TRANSPORT, "ই বওসু সওউলইওগে গায়ো"),
        Phrase("다시 한 번 말씀해 주세요.", "আরেকবার বলবেন, দয়া করে?", ConversationRole.STUDENT_WORKER, PhraseScenario.WORK_STUDY, "তাশি হান বон মালসুমহে জুসেয়ো"),
        Phrase("천천히 말씀해 주세요.", "একটু ধীরে বলবেন, দয়া করে?", ConversationRole.STUDENT_WORKER, PhraseScenario.WORK_STUDY, "চনচনহি মালসুমহে জুসেয়ো"),
        Phrase("잘 이해하지 못했어요.", "আমি ভালোভাবে বুঝতে পারিনি।", ConversationRole.STUDENT_WORKER, PhraseScenario.WORK_STUDY, "জাল ইহে হাজি মোতহেসসয়ো"),
        Phrase("도와주세요!", "দয়া করে সাহায্য করুন!", ConversationRole.TRAVELER, PhraseScenario.HELP, "তোওয়াজুসেয়ো"),
        Phrase("병원에 가야 해요.", "আমাকে হাসপাতালে যেতে হবে।", ConversationRole.TRAVELER, PhraseScenario.HELP, "বিয়ংওনে গায়া হেয়ো"),
        Phrase("경찰을 불러 주세요.", "দয়া করে পুলিশ ডাকুন।", ConversationRole.TRAVELER, PhraseScenario.HELP, "কিয়ংচারুল বুল্লো জুসেয়ো"),
        Phrase("저는 방글라데시에서 왔어요.", "আমি বাংলাদেশ থেকে এসেছি।", ConversationRole.STUDENT_WORKER, PhraseScenario.HELP, "জোনুন বাংগুল্লাদেশি এসো ওয়াসসয়ো")
    )

    fun suggestions(role: ConversationRole, scenario: PhraseScenario): List<Phrase> =
        phrases.filter { it.role == role && it.scenario == scenario }
}
