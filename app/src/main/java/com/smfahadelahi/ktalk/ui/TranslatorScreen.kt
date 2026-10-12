package com.smfahadelahi.ktalk.ui

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TranslatorScreen() {
    var input by remember { mutableStateOf("") }
    var status by remember {
        mutableStateOf("Offline engine not configured yet. No translation has been generated.")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("K-Bridge AI", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Korean ↔ বাংলা",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            "Offline-first translator foundation",
            style = MaterialTheme.typography.bodyMedium
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Model status", style = MaterialTheme.typography.titleMedium)
                Text(status, style = MaterialTheme.typography.bodyMedium)
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
                    status = if (input.isBlank()) {
                        "Enter text first. A real offline translation model is not connected yet."
                    } else {
                        "Translation model is not installed. No fake translation was returned."
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Translate")
            }
            OutlinedButton(
                onClick = {
                    input = ""
                    status = "Offline engine not configured yet. No translation has been generated."
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Clear")
            }
        }

        Spacer(Modifier.height(4.dp))
        Text("Planned next: microphone capture, end-of-speech detection, local ASR, Korean↔Bangla model, and offline speech output.")
        Text(
            "Privacy: this foundation declares no INTERNET permission. Real offline operation still requires compatible local models and voices.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
