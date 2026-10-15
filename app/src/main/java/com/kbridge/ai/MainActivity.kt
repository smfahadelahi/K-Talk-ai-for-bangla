package com.kbridge.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { KBridgeApp() }
    }
}

private val Ink = Color(0xFF10212B)
private val Teal = Color(0xFF1D9C91)
private val Mist = Color(0xFFF3F7F7)

@Composable
fun KBridgeApp() {
    var selected by remember { mutableStateOf("Home") }
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Mist) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                Text("K-BRIDGE AI", color = Teal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Korean for real life.", color = Ink, fontSize = 29.sp, fontWeight = FontWeight.ExtraBold)
                Text("Your Bengali-first Korean companion", color = Color(0xFF607078), fontSize = 14.sp)
                Spacer(Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Ink)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = Color(0xFF75E0D3))
                            Text("  YOUR PRIVATE AI TUTOR", color = Color(0xFF75E0D3), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text("Learn it. Say it. Use it.", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Designed for offline-first Korean practice, Bengali explanations and real-life conversations.",
                            color = Color(0xFFD1DEDF), fontSize = 14.sp
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { selected = "AI Tutor" }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Mic, contentDescription = null)
                            Text("  Open AI Tutor")
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))
                Text("YOUR DAILY MISSIONS", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                MissionCard("Order at a Korean restaurant", "Practice ordering food and asking the price", "🍜") { selected = "Missions" }
                Spacer(Modifier.height(9.dp))
                MissionCard("Talk to your professor", "Learn polite Korean for university life", "🎓") { selected = "Missions" }
                Spacer(Modifier.height(9.dp))
                MissionCard("Headphone conversation mode", "Prepare Korean replies with Bengali meaning", "🎧") { selected = "Headphones" }

                Spacer(Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavItem("Home", "⌂", selected) { selected = it }
                    NavItem("AI Tutor", "✦", selected) { selected = it }
                    NavItem("Missions", "▣", selected) { selected = it }
                    NavItem("Headphones", "♫", selected) { selected = it }
                }
                if (selected != "Home") {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        when (selected) {
                            "AI Tutor" -> "AI tutor interface scaffold: local model loading and conversation engine are the next implementation step."
                            "Missions" -> "Mission library scaffold: scenario dialogue and Bengali explanations will be added next."
                            else -> "Headphone mode scaffold: offline Korean speech recognition, turn detection and spoken Bengali output need integration."
                        },
                        color = Color(0xFF607078), fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MissionCard(title: String, subtitle: String, emoji: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 25.sp)
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(title, color = Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, color = Color(0xFF607078), fontSize = 12.sp)
            }
            Text("›", color = Teal, fontSize = 28.sp)
        }
    }
}

@Composable
private fun NavItem(label: String, symbol: String, selected: String, onClick: (String) -> Unit) {
    OutlinedButton(onClick = { onClick(label) }) {
        Text("$symbol\n$label", fontSize = 10.sp, lineHeight = 12.sp)
    }
}
