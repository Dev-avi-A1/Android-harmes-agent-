package com.example.feature.settings

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.AiMode
import com.example.data.repository.SettingsRepository
import com.example.ui.components.HarmesTopBar
import com.example.ui.theme.HarmesCyan
import com.example.ui.theme.HarmesElectricBlue
import com.example.ui.theme.HarmesTeal
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    currentAiMode: AiMode,
    settingsRepository: SettingsRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var strictLocalPrivateData by remember { mutableStateOf(true) }
    var selectedModel by remember { mutableStateOf("Harmes Neural-Lite 1.2B") }

    Column(modifier = Modifier.fillMaxSize()) {
        HarmesTopBar(
            title = "Agent Settings",
            subtitle = "AI Modes, Models & Security",
            aiMode = currentAiMode,
            onModeClick = {}
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // AI Mode Selection Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = HarmesCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Operational Mode", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        AiMode.values().forEach { mode ->
                            val isSelected = currentAiMode == mode
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) HarmesCyan.copy(alpha = 0.15f) else Color.Transparent,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) HarmesCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        scope.launch {
                                            settingsRepository.setAiMode(mode)
                                            Toast.makeText(context, "AI Mode set to ${mode.displayName}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .testTag("ai_mode_${mode.name.lowercase()}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            scope.launch { settingsRepository.setAiMode(mode) }
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = HarmesCyan)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(text = mode.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text(text = mode.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Model Management Section
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Memory, contentDescription = null, tint = HarmesCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Configured Inference Models", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        ModelEntry(
                            name = "Harmes Neural-Lite 1.2B",
                            badge = "Local Q4_K_M",
                            details = "Hardware: NPU/CPU | Context: 4k tokens",
                            isActive = selectedModel == "Harmes Neural-Lite 1.2B",
                            onClick = {
                                selectedModel = "Harmes Neural-Lite 1.2B"
                                Toast.makeText(context, "Active model: Harmes Neural-Lite 1.2B", Toast.LENGTH_SHORT).show()
                            }
                        )
                        ModelEntry(
                            name = "Harmes Mobile-Agent 3.8B",
                            badge = "Local INT4",
                            details = "Hardware: GPU OpenCL | Context: 8k tokens",
                            isActive = selectedModel == "Harmes Mobile-Agent 3.8B",
                            onClick = {
                                selectedModel = "Harmes Mobile-Agent 3.8B"
                                Toast.makeText(context, "Active model: Harmes Mobile-Agent 3.8B", Toast.LENGTH_SHORT).show()
                            }
                        )
                        ModelEntry(
                            name = "Gemini 3.5 Flash",
                            badge = "Cloud AI Gateway",
                            details = "Hardware: Cloud TPU | Context: 1M tokens",
                            isActive = selectedModel == "Gemini 3.5 Flash",
                            onClick = {
                                selectedModel = "Gemini 3.5 Flash"
                                Toast.makeText(context, "Active model: Gemini 3.5 Flash", Toast.LENGTH_SHORT).show()
                            }
                        )
                        ModelEntry(
                            name = "Gemini 3.1 Pro (Deep Reasoner)",
                            badge = "Cloud AI Heavy",
                            details = "Hardware: Cloud Cluster | Context: 2M tokens",
                            isActive = selectedModel == "Gemini 3.1 Pro (Deep Reasoner)",
                            onClick = {
                                selectedModel = "Gemini 3.1 Pro (Deep Reasoner)"
                                Toast.makeText(context, "Active model: Gemini 3.1 Pro", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            // Privacy & Data Governance Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = HarmesCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Zero-Trust Privacy Governance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Strict Local for Private Data", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text("Never send personal notes or documents to remote servers.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = strictLocalPrivateData,
                                onCheckedChange = { strictLocalPrivateData = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF041E26), checkedTrackColor = HarmesCyan)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Require Approval for Sensitive Tools", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text("Enforces confirmation prompt before modifying device state.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = true,
                                onCheckedChange = {},
                                enabled = false,
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF041E26), checkedTrackColor = HarmesCyan)
                            )
                        }
                    }
                }
            }

            // Publishing & Compliance Overview
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = HarmesTeal)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Publishing & Security Compliance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        ComplianceBadge("Target SDK", "Android 16 (API 36)")
                        ComplianceBadge("Storage Privacy", "Zero-Permission Photo Picker compliant")
                        ComplianceBadge("Data Safety", "On-device Room DB with encrypted vault")
                        ComplianceBadge("Network Security", "Encrypted TLS 1.3 endpoints with zero telemetry egress")
                    }
                }
            }

            // Reset Onboarding Button
            item {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            settingsRepository.setOnboardingCompleted(false)
                            Toast.makeText(context, "Restarting onboarding setup…", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Re-run First Launch Onboarding")
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun ComplianceBadge(title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = HarmesTeal, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ModelEntry(
    name: String,
    badge: String,
    details: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isActive) HarmesCyan.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = HarmesCyan.copy(alpha = 0.15f)
                    ) {
                        Text(text = badge, fontSize = 9.sp, color = HarmesCyan, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = details, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (isActive) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Active", tint = HarmesCyan, modifier = Modifier.size(18.dp))
            }
        }
    }
}
