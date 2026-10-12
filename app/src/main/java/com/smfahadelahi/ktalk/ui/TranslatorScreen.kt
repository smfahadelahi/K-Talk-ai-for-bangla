package com.smfahadelahi.ktalk.ui

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.smfahadelahi.ktalk.phrasebook.ConversationRole
import com.smfahadelahi.ktalk.phrasebook.PhraseScenario
import com.smfahadelahi.ktalk.phrasebook.Phrasebook
import com.smfahadelahi.ktalk.speech.AndroidOnDeviceSpeechRecognizer
import com.smfahadelahi.ktalk.tts.AndroidOfflineSpeechOutput
import com.smfahadelahi.ktalk.translation.OfflinePhraseTranslator
import com.smfahadelahi.ktalk.translation.OfflineTranslationModelStore
import com.smfahadelahi.ktalk.translation.ReplySuggestionEngine
import com.smfahadelahi.ktalk.translation.Small100TranslationEngine
import com.smfahadelahi.ktalk.translation.TranslationDirection
import com.smfahadelahi.ktalk.translation.TranslationResult
import kotlinx.coroutines.launch

@Composable
fun TranslatorScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    var translationResult by remember { mutableStateOf<TranslationResult?>(null) }
    var translationModelInstalled by remember { mutableStateOf(OfflineTranslationModelStore.findInstalled(context) != null) }
    var isTranslating by remember { mutableStateOf(false) }
    var isRecognizing by remember { mutableStateOf(false) }
    var status by remember {
        mutableStateOf("Check whether this device has on-device speech recognition and offline language packs installed.")
    }
    var selectedRole by remember { mutableStateOf(ConversationRole.CUSTOMER) }
    var selectedScenario by remember { mutableStateOf(PhraseScenario.RESTAURANT) }
    var pendingLanguage by remember { mutableStateOf("ko-KR") }

    val speechOutput = remember(context) { AndroidOfflineSpeechOutput(context.applicationContext) }
    DisposableEffect(speechOutput) {
        onDispose { speechOutput.close() }
    }
    val onDeviceRecognizer = remember(context) { AndroidOnDeviceSpeechRecognizer(context.applicationContext) }
    val translationEngine = remember(context) { Small100TranslationEngine(context.applicationContext) }
    DisposableEffect(translationEngine) {
        onDispose { translationEngine.close() }
    }

    val modelPackPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                status = "Installing offline translation model. This is a large file; keep the app open."
                val installed = OfflineTranslationModelStore.installZip(context, uri)
                installed.fold(
                    {
                        translationModelInstalled = true
                        status = "Offline Korean↔Bengali translation model installed successfully."
                    },
                    { error ->
                        translationModelInstalled = false
                        status = "Model pack install failed: ${error.message ?: "invalid ZIP"}"
                    }
                )
            }
        }
    }

    fun runRecognition(languageTag: String) {
        coroutineScope.launch {
            isRecognizing = true
            try {
                status = "Listening with Android's on-device recognizer ($languageTag). Wait until you finish speaking."
                val result = onDeviceRecognizer.recognizeOnce(languageTag)
                result.fold(
                    { transcript ->
                        input = transcript
                        val direction = if (languageTag.startsWith("ko")) {
                            TranslationDirection.KOREAN_TO_BANGLA
                        } else {
                            TranslationDirection.BANGLA_TO_KOREAN
                        }
                        val outputLanguage = if (direction == TranslationDirection.KOREAN_TO_BANGLA) "bn-BD" else "ko-KR"
                        val exact = OfflinePhraseTranslator.translateExact(transcript)

                        if (exact != null) {
                            translationResult = exact
                            status = "Exact offline phrase match found. Playing the translation…"
                            val spoken = speechOutput.speak(exact.translatedText, outputLanguage)
                            status = spoken.fold(
                                { "Phrase translated locally and played through the available offline voice." },
                                { "Phrase translated locally, but offline voice output is unavailable: ${it.message ?: "unknown error"}" }
                            )
                        } else if (translationModelInstalled) {
                            isTranslating = true
                            status = "Speech recognized. Translating locally…"
                            val translated = translationEngine.translate(transcript, direction)
                            translated.fold(
                                { value ->
                                    translationResult = value
                                    status = "Translation ready. Playing through the local voice…"
                                    val spoken = speechOutput.speak(value.translatedText, outputLanguage)
                                    status = spoken.fold(
                                        { "Speech recognized, translated locally, and played through the offline voice." },
                                        { "Translation is ready on screen, but local voice output is unavailable: ${it.message ?: "unknown error"}" }
                                    )
                                },
                                { error ->
                                    translationResult = null
                                    status = error.message ?: "Local translation failed."
                                }
                            )
                        } else {
                            translationResult = null
                            status = "Speech recognized. Import the offline model ZIP to translate arbitrary sentences; exact phrasebook matches still work."
                        }
                    },
                    { error ->
                        status = error.message ?: "On-device recognition failed. Check that the offline language pack is installed."
                    }
                )
            } finally {
                isRecognizing = false
                isTranslating = false
            }
        }
    }

    val microphonePermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) runRecognition(pendingLanguage)
        else status = "Microphone permission denied. The offline phrasebook remains available."
    }

    fun beginRecognition(languageTag: String) {
        pendingLanguage = languageTag
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            microphonePermission.launch(android.Manifest.permission.RECORD_AUDIO)
        } else {
            runRecognition(languageTag)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("K-Bridge AI", style = MaterialTheme.typography.headlineMedium)
        Text("Korean ↔ বাংলা", style = MaterialTheme.typography.titleMedium)
        Text("Private, offline-first conversation assistant", style = MaterialTheme.typography.bodyMedium)

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("System status", style = MaterialTheme.typography.titleMedium)
                Text(status, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Card {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Offline translation model", style = MaterialTheme.typography.titleMedium)
                Text(
                    if (translationModelInstalled) "SMaLL-100 model pack is installed. General Korean↔Bengali text translation can run locally."
                    else "Model not installed. Import the separate SMaLL-100 ZIP pack (~600 MB) once; after setup, translation runs locally without an app network connection.",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedButton(
                    onClick = {
                        modelPackPicker.launch(
                            arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream")
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (translationModelInstalled) "Replace offline model pack" else "Import offline model ZIP") }
            }
        }

        Text("On-device speech recognition", style = MaterialTheme.typography.titleMedium)
        Text(
            if (onDeviceRecognizer.isAvailable()) "Android reports an on-device recognition service. Korean/Bengali offline language support depends on installed packs."
            else "No on-device recognition service is available. No cloud recognizer fallback will be used.",
            style = MaterialTheme.typography.bodySmall
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { if (!isRecognizing) beginRecognition("ko-KR") },
                enabled = !isRecognizing,
                modifier = Modifier.weight(1f)
            ) { Text(if (isRecognizing && pendingLanguage == "ko-KR") "শুনছি…" else "한국어 듣기") }
            Button(
                onClick = { if (!isRecognizing) beginRecognition("bn-BD") },
                enabled = !isRecognizing,
                modifier = Modifier.weight(1f)
            ) { Text(if (isRecognizing && pendingLanguage == "bn-BD") "শুনছি…" else "বাংলা বলুন") }
        }

        Text("Offline voice test", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        status = "Checking for an installed offline Korean voice…"
                        val result = speechOutput.speak("안녕하세요. 반갑습니다.", "ko-KR")
                        status = result.fold({ "Offline Korean voice test finished." }, { "Korean offline voice unavailable: ${it.message ?: "unknown error"}" })
                    }
                },
                modifier = Modifier.weight(1f)
            ) { Text("한국어 음성") }
            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        status = "Checking for an installed offline Bengali voice…"
                        val result = speechOutput.speak("আমি কোরিয়ান ভাষা শিখছি।", "bn-BD")
                        status = result.fold({ "Offline Bengali voice test finished." }, { "Bengali offline voice unavailable: ${it.message ?: "unknown error"}" })
                    }
                },
                modifier = Modifier.weight(1f)
            ) { Text("বাংলা ভয়েস") }
        }

        Text("Role-play phrasebook", style = MaterialTheme.typography.titleLarge)
        Text("আপনি কোন ভূমিকায় কথা বলছেন?", style = MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ConversationRole.entries.forEach { role ->
                if (selectedRole == role) {
                    Button(onClick = { selectedRole = role }, modifier = Modifier.weight(1f)) {
                        Text(role.labelBn, style = MaterialTheme.typography.labelSmall)
                    }
                } else {
                    OutlinedButton(onClick = { selectedRole = role }, modifier = Modifier.weight(1f)) {
                        Text(role.labelBn, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        Text("পরিস্থিতি বেছে নিন", style = MaterialTheme.typography.bodyMedium)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PhraseScenario.entries.forEach { scenario ->
                if (selectedScenario == scenario) {
                    Button(onClick = { selectedScenario = scenario }) { Text(scenario.labelBn) }
                } else {
                    OutlinedButton(onClick = { selectedScenario = scenario }) { Text(scenario.labelBn) }
                }
            }
        }

        val suggestions = Phrasebook.suggestions(selectedRole, selectedScenario)
        if (suggestions.isEmpty()) {
            Text("এই ভূমিকা ও পরিস্থিতির জন্য আরও বাক্য যোগ করা হবে।", style = MaterialTheme.typography.bodyMedium)
        } else {
            suggestions.forEach { phrase ->
                Card {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(phrase.korean, style = MaterialTheme.typography.titleMedium)
                        Text(phrase.banglaMeaning, style = MaterialTheme.typography.bodyMedium)
                        phrase.pronunciationBn?.let { Text("উচ্চারণ: $it", style = MaterialTheme.typography.bodySmall) }
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    val result = speechOutput.speak(phrase.korean, "ko-KR")
                                    status = result.fold(
                                        { "Korean phrase played using an offline voice." },
                                        { "Offline Korean voice unavailable: ${it.message ?: "unknown error"}" }
                                    )
                                }
                            }
                        ) { Text("কোরিয়ান শুনুন") }
                    }
                }
            }
        }

        TextField(
            value = input,
            onValueChange = {
                input = it
                translationResult = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Recognized or manually entered text") },
            placeholder = { Text("এখানে লিখুন / 여기에 입력하세요") },
            minLines = 3
        )

        translationResult?.let { translated ->
            Card {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Exact offline phrase match", style = MaterialTheme.typography.titleMedium)
                    Text(translated.sourceText, style = MaterialTheme.typography.bodyMedium)
                    Text(translated.translatedText, style = MaterialTheme.typography.titleLarge)
                    Text("Reviewed phrasebook entry; this is not a general machine translation.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        val hasKoreanText = input.any { it in '\uAC00'..'\uD7A3' }
        val replySuggestions = if (hasKoreanText) {
            ReplySuggestionEngine.suggestReplyToKorean(input)
        } else {
            emptyList()
        }
        if (replySuggestions.isNotEmpty()) {
            Text("সম্ভাব্য উত্তর (অফলাইন)", style = MaterialTheme.typography.titleMedium)
            replySuggestions.forEach { suggestion ->
                Card {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(suggestion.korean, style = MaterialTheme.typography.titleMedium)
                        Text(suggestion.banglaMeaning, style = MaterialTheme.typography.bodyMedium)
                        Text(suggestion.reasonBn, style = MaterialTheme.typography.bodySmall)
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    val result = speechOutput.speak(suggestion.korean, "ko-KR")
                                    status = result.fold(
                                        { "Suggested Korean reply played using an offline voice." },
                                        { "Offline Korean voice unavailable: ${it.message ?: "unknown error"}" }
                                    )
                                }
                            }
                        ) { Text("উত্তরটি শুনুন") }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    if (input.isBlank()) {
                        translationResult = null
                        status = "Enter Korean or Bengali text first."
                    } else {
                        val exact = OfflinePhraseTranslator.translateExact(input)
                        if (exact != null) {
                            translationResult = exact
                            status = "Found an exact offline phrasebook match."
                        } else {
                            val direction = when {
                                input.any { it in '\uAC00'..'\uD7A3' } -> TranslationDirection.KOREAN_TO_BANGLA
                                input.any { it in '\u0980'..'\u09FF' } -> TranslationDirection.BANGLA_TO_KOREAN
                                else -> null
                            }
                            if (direction == null) {
                                translationResult = null
                                status = "Enter Korean or Bengali text."
                            } else {
                                coroutineScope.launch {
                                    isTranslating = true
                                    status = if (translationModelInstalled) "Translating locally on this device…" else "Exact phrasebook match not found; import the offline model ZIP to translate arbitrary text."
                                    val translated = translationEngine.translate(input, direction)
                                    translated.fold(
                                        { result ->
                                            translationResult = result
                                            status = "Translation completed locally."
                                        },
                                        { error ->
                                            translationResult = null
                                            status = error.message ?: "Local translation failed."
                                        }
                                    )
                                    isTranslating = false
                                }
                            }
                        }
                    }
                },
                enabled = !isTranslating,
                modifier = Modifier.weight(1f)
            ) { Text(if (isTranslating) "অনুবাদ হচ্ছে…" else "Translate") }
            OutlinedButton(
                onClick = { input = ""; translationResult = null; status = "Ready. Exact phrasebook translation is available for known phrases." },
                modifier = Modifier.weight(1f)
            ) { Text("Clear") }
        }

        Spacer(Modifier.height(4.dp))
        Text(
            "Working now: Android on-device speech recognition, exact phrasebook translation, local SMaLL-100 ONNX translation after model-pack import, role-play phrases, rule-based reply suggestions, and local-voice checks. The model pack is not bundled in the APK.",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "Privacy: the app declares no INTERNET permission and does not use a cloud recognizer fallback. Offline language packs must be available on the device.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
