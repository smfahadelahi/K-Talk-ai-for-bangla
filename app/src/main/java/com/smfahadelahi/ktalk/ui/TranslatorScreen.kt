package com.smfahadelahi.ktalk.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
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
import com.smfahadelahi.ktalk.audio.AudioCaptureController
import com.smfahadelahi.ktalk.phrasebook.ConversationRole
import com.smfahadelahi.ktalk.phrasebook.PhraseScenario
import com.smfahadelahi.ktalk.phrasebook.Phrasebook
import com.smfahadelahi.ktalk.tts.AndroidOfflineSpeechOutput
import kotlinx.coroutines.launch

@Composable
fun TranslatorScreen() {
    val context = LocalContext.current
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val coroutineScope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var status by remember {
        mutableStateOf("Offline models are not installed. Translation is not available yet.")
    }
    var selectedRole by remember { mutableStateOf(ConversationRole.CUSTOMER) }
    var selectedScenario by remember { mutableStateOf(PhraseScenario.RESTAURANT) }

    val speechOutput = remember(context) { AndroidOfflineSpeechOutput(context.applicationContext) }
    DisposableEffect(speechOutput) {
        onDispose { speechOutput.close() }
    }

    val captureController = remember(context) {
        AudioCaptureController(
            context = context.applicationContext,
            onUtterance = { audio, sampleRate ->
                mainHandler.post {
                    val seconds = audio.size / sampleRate
                    status = "Speech segment captured locally (${seconds}s). The offline Korean speech model is not connected yet, so no transcript was invented."
                }
            },
            onError = { message -> mainHandler.post { status = message } }
        )
    }

    DisposableEffect(captureController) {
        onDispose { captureController.stop() }
    }

    val microphonePermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            isListening = captureController.start()
            status = if (isListening) {
                "Listening locally. Speak naturally; processing starts after a pause."
            } else {
                "Could not start microphone capture. Check Android microphone permission."
            }
        } else {
            status = "Microphone permission denied. You can still use the offline phrasebook."
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

        Button(
            onClick = {
                if (isListening) {
                    captureController.stop()
                    isListening = false
                    status = "Microphone stopped. Captured audio was only held in memory."
                } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    isListening = captureController.start()
                    status = if (isListening) "Listening locally. Speak naturally; processing starts after a pause." else "Could not start microphone capture."
                } else {
                    microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (isListening) "Stop listening" else "Start headphone conversation") }

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
            ) { Text("한국어 듣기") }
            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        status = "Checking for an installed offline Bengali voice…"
                        val result = speechOutput.speak("আমি কোরিয়ান ভাষা শিখছি।", "bn-BD")
                        status = result.fold({ "Offline Bengali voice test finished." }, { "Bengali offline voice unavailable: ${it.message ?: "unknown error"}" })
                    }
                },
                modifier = Modifier.weight(1f)
            ) { Text("বাংলা শুনুন") }
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
                    Column(
                        Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(phrase.korean, style = MaterialTheme.typography.titleMedium)
                        Text(phrase.banglaMeaning, style = MaterialTheme.typography.bodyMedium)
                        phrase.pronunciationBn?.let {
                            Text("উচ্চারণ: $it", style = MaterialTheme.typography.bodySmall)
                        }
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
            onValueChange = { input = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Korean or Bangla text") },
            placeholder = { Text("এখানে লিখুন / 여기에 입력하세요") },
            minLines = 3
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    status = if (input.isBlank()) "Enter text first. A real offline translation model is not connected yet."
                    else "Translation model is not installed. No fake translation was returned."
                },
                modifier = Modifier.weight(1f)
            ) { Text("Translate") }
            OutlinedButton(
                onClick = { input = ""; status = "Ready. Offline translation model is not installed yet." },
                modifier = Modifier.weight(1f)
            ) { Text("Clear") }
        }

        Spacer(Modifier.height(4.dp))
        Text(
            "Working now: local microphone capture, pause-based speech segmentation, offline phrasebook with role selection, and device-installed offline voice checks. Live speech recognition, Korean↔Bangla translation, and contextual AI reply suggestions still need real model integration.",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "Privacy: audio is processed in memory and is not saved or uploaded. The app declares no INTERNET permission.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
