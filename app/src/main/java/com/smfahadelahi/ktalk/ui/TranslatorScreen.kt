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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.smfahadelahi.ktalk.audio.AudioCaptureController

@Composable
fun TranslatorScreen() {
    val context = LocalContext.current
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    var input by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var status by remember {
        mutableStateOf("Offline models are not installed. Translation is not available yet.")
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
            onError = { message ->
                mainHandler.post { status = message }
            }
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
            status = "Microphone permission denied. You can still use text input when an offline model is available."
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

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
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
                } else if (ContextCompat.checkSelfPermission(
                        context, Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    isListening = captureController.start()
                    status = if (isListening) {
                        "Listening locally. Speak naturally; processing starts after a pause."
                    } else {
                        "Could not start microphone capture."
                    }
                } else {
                    microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isListening) "Stop listening" else "Start headphone conversation")
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
                    status = if (input.isBlank()) {
                        "Enter text first. A real offline translation model is not connected yet."
                    } else {
                        "Translation model is not installed. No fake translation was returned."
                    }
                },
                modifier = Modifier.weight(1f)
            ) { Text("Translate") }

            OutlinedButton(
                onClick = {
                    input = ""
                    status = "Ready. Offline translation model is not installed yet."
                },
                modifier = Modifier.weight(1f)
            ) { Text("Clear") }
        }

        Spacer(Modifier.height(4.dp))
        Text(
            "Current capability: local microphone capture and pause-based speech segmentation. Speech recognition, Korean↔Bangla translation, reply suggestions, and offline voice output still need real model integration.",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "Privacy: audio is processed in memory by this foundation and is not saved or uploaded. The app declares no INTERNET permission.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
