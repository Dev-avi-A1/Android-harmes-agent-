package com.example.feature.hardware

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.common.DeviceHardwareInfo
import com.example.core.model.AiMode
import com.example.domain.models.HardwareAccelerationBackend
import com.example.ui.components.HarmesTopBar
import com.example.ui.theme.HarmesAmber
import com.example.ui.theme.HarmesCyan
import com.example.ui.theme.HarmesElectricBlue
import com.example.ui.theme.HarmesTeal

@Composable
fun HardwareScreen(
    hardwareInfo: DeviceHardwareInfo,
    backend: HardwareAccelerationBackend,
    isOnline: Boolean,
    aiMode: AiMode,
    onOpenSettings: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        HarmesTopBar(
            title = "Hardware Monitor",
            subtitle = "Live Chipset & Sensor Telemetry",
            aiMode = aiMode,
            onModeClick = onOpenSettings
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Chipset & SoC Section
            item {
                SectionCard(
                    title = "SoC & Compute Backend",
                    icon = Icons.Default.Memory
                ) {
                    InfoRow("Device Model", hardwareInfo.deviceModel)
                    InfoRow("Manufacturer", hardwareInfo.manufacturer)
                    InfoRow("OS Version", hardwareInfo.androidVersion)
                    InfoRow("CPU Architecture", hardwareInfo.cpuArch)
                    InfoRow("CPU Execution Cores", "${hardwareInfo.cpuCores} physical/logical threads")
                    InfoRow("Active AI Backend", backend.name)
                    InfoRow("Estimated Throughput", backend.getThroughputEstimate())
                    InfoRow("NPU / NNAPI Status", hardwareInfo.npuSupportInfo)
                }
            }

            // Memory & Storage Section
            item {
                SectionCard(
                    title = "Memory & Storage Allocation",
                    icon = Icons.Default.Storage
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("System RAM (${hardwareInfo.ramUsagePercent}% used)", style = MaterialTheme.typography.bodySmall)
                            Text("${hardwareInfo.totalRamMb - hardwareInfo.availableRamMb} MB / ${hardwareInfo.totalRamMb} MB", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { hardwareInfo.ramUsagePercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = HarmesCyan,
                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val storageUsedGb = hardwareInfo.totalStorageGb - hardwareInfo.availableStorageGb
                            Text("Device Internal Storage", style = MaterialTheme.typography.bodySmall)
                            Text("$storageUsedGb GB / ${hardwareInfo.totalStorageGb} GB", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val storagePct = if (hardwareInfo.totalStorageGb > 0) {
                            ((hardwareInfo.totalStorageGb - hardwareInfo.availableStorageGb).toFloat() / hardwareInfo.totalStorageGb).coerceIn(0f, 1f)
                        } else 0f
                        LinearProgressIndicator(
                            progress = { storagePct },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = HarmesElectricBlue,
                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            // Battery, Power & Thermals Section
            item {
                SectionCard(
                    title = "Power & Thermal Governors",
                    icon = Icons.Default.Thermostat
                ) {
                    InfoRow("Battery Level", "${hardwareInfo.batteryPercent}%")
                    InfoRow("Charging Status", if (hardwareInfo.isCharging) "Charging (AC/USB)" else "Discharging")
                    InfoRow(
                        "Battery Temperature",
                        if (hardwareInfo.batteryTemperatureCelsius != null) "${hardwareInfo.batteryTemperatureCelsius}°C" else "Not available"
                    )
                    InfoRow("Thermal Throttling State", hardwareInfo.thermalStatus)
                    InfoRow("Power Policy", "Auto-throttle local inference below 15% battery")
                }
            }

            // Connectivity & Network
            item {
                SectionCard(
                    title = "Network & Cloud Gateway",
                    icon = Icons.Default.NetworkCheck
                ) {
                    InfoRow("Internet Connectivity", if (isOnline) "Connected (Active Gateway)" else "Offline (Local Engine Engaged)")
                    InfoRow("Cloud Gateway Protocol", "TLS 1.3 / HTTPS REST")
                    InfoRow("Telemetry Egress", "Strictly zero without consent")
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = HarmesCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
