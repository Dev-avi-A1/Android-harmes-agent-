package com.example.feature.tools

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.AiMode
import com.example.domain.tools.AgentTool
import com.example.domain.tools.ToolInput
import com.example.domain.tools.ToolRegistry
import com.example.ui.components.HarmesRiskBadge
import com.example.ui.components.HarmesTopBar
import com.example.ui.theme.HarmesCyan
import com.example.ui.theme.HarmesTeal
import kotlinx.coroutines.launch

@Composable
fun ToolsScreen(
    toolRegistry: ToolRegistry,
    aiMode: AiMode,
    onOpenSettings: () -> Unit
) {
    val tools = remember { toolRegistry.getAllTools() }
    val enabledMap = remember {
        mutableStateMapOf<String, Boolean>().apply {
            tools.forEach { put(it.id, toolRegistry.isToolEnabled(it.id)) }
        }
    }
    var activeTestingTool by remember { mutableStateOf<AgentTool?>(null) }
    var testInput by remember { mutableStateOf("") }
    var testResultOutput by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        HarmesTopBar(
            title = "Agent Tools",
            subtitle = "Permitted Operating Integrations",
            aiMode = aiMode,
            onModeClick = onOpenSettings
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Security & Risk Model",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = HarmesCyan
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tools are isolated with zero-trust policies. Low-risk operations execute automatically on-device. High-risk operations halt until you grant explicit cryptographic confirmation.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Test execution drawer if active
            if (activeTestingTool != null) {
                item {
                    val tool = activeTestingTool!!
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.dp, HarmesCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Testing: ${tool.name}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = HarmesCyan
                                )
                                Button(
                                    onClick = {
                                        activeTestingTool = null
                                        testResultOutput = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                                ) {
                                    Text("Close", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = testInput,
                                onValueChange = { testInput = it },
                                label = { Text("Tool Parameters / Query") },
                                placeholder = { Text("e.g. 450 * 12 or Search query") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    scope.launch {
                                        val res = tool.execute(ToolInput(rawQuery = testInput))
                                        testResultOutput = res.summary
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = HarmesCyan),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Execute Test", color = Color(0xFF041E26), fontWeight = FontWeight.Bold)
                            }

                            if (testResultOutput != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = testResultOutput ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            items(tools, key = { it.id }) { tool ->
                val isEnabled = enabledMap[tool.id] ?: true
                ToolCardItem(
                    tool = tool,
                    isEnabled = isEnabled,
                    onToggle = { checked ->
                        enabledMap[tool.id] = checked
                        toolRegistry.setToolEnabled(tool.id, checked)
                    },
                    onTestClick = {
                        activeTestingTool = tool
                        testInput = when (tool.id) {
                            "calculator" -> "150 * 12"
                            "web_search" -> "Gemini on-device model architectures"
                            "reminder" -> "Prepare weekly progress brief"
                            else -> "Execute sample routine"
                        }
                        testResultOutput = null
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun ToolCardItem(
    tool: AgentTool,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onTestClick: () -> Unit
) {
    val icon: ImageVector = when (tool.id) {
        "calculator" -> Icons.Default.Calculate
        "web_search" -> Icons.Default.Search
        "reminder" -> Icons.Default.Alarm
        "calendar" -> Icons.Default.CalendarMonth
        "notification" -> Icons.Default.Notifications
        "document" -> Icons.Default.Description
        "camera" -> Icons.Default.CameraAlt
        "share" -> Icons.Default.Share
        "app_launcher" -> Icons.AutoMirrored.Filled.Launch
        else -> Icons.Default.Handyman
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = tool.name,
                        tint = HarmesCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = tool.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = tool.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF041E26),
                        checkedTrackColor = HarmesCyan
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = tool.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HarmesRiskBadge(riskLevel = tool.riskLevel)

                Button(
                    onClick = onTestClick,
                    enabled = isEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HarmesCyan.copy(alpha = 0.15f),
                        contentColor = HarmesCyan
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Test",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Test Run", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
