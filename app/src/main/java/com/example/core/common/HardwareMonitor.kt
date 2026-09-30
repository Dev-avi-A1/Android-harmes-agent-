package com.example.core.common

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File

data class DeviceHardwareInfo(
    val deviceModel: String,
    val manufacturer: String,
    val androidVersion: String,
    val sdkInt: Int,
    val cpuArch: String,
    val cpuCores: Int,
    val totalRamMb: Long,
    val availableRamMb: Long,
    val ramUsagePercent: Int,
    val batteryPercent: Int,
    val isCharging: Boolean,
    val batteryTemperatureCelsius: Float?,
    val totalStorageGb: Long,
    val availableStorageGb: Long,
    val npuSupportInfo: String,
    val thermalStatus: String
)

class HardwareMonitor(private val context: Context) {

    fun getHardwareInfo(): DeviceHardwareInfo {
        // RAM info
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)
        val totalRamMb = memInfo.totalMem / (1024 * 1024)
        val availRamMb = memInfo.availMem / (1024 * 1024)
        val ramUsage = if (totalRamMb > 0) (((totalRamMb - availRamMb).toDouble() / totalRamMb) * 100).toInt() else 0

        // Battery info
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct: Int = if (level >= 0 && scale > 0) ((level.toFloat() / scale.toFloat()) * 100).toInt() else 100
        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
        val rawTemp: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
        val tempCelsius: Float? = if (rawTemp > 0) rawTemp / 10.0f else null

        // Storage info
        val statFs = StatFs(Environment.getDataDirectory().path)
        val totalStorageGb = (statFs.blockSizeLong * statFs.blockCountLong) / (1024 * 1024 * 1024)
        val availStorageGb = (statFs.blockSizeLong * statFs.availableBlocksLong) / (1024 * 1024 * 1024)

        // CPU & SoC
        val cpuArch = Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown"
        val cpuCores = Runtime.getRuntime().availableProcessors()

        // NPU / AI Accelerator detection via real Android system capabilities
        val npuInfo = detectNpuAcceleration()

        // Thermal status
        val thermal = when {
            tempCelsius != null && tempCelsius > 45.0f -> "High (Throttling possible)"
            tempCelsius != null && tempCelsius > 38.0f -> "Warm"
            else -> "Nominal"
        }

        return DeviceHardwareInfo(
            deviceModel = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
            manufacturer = Build.MANUFACTURER,
            androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            sdkInt = Build.VERSION.SDK_INT,
            cpuArch = cpuArch,
            cpuCores = cpuCores,
            totalRamMb = totalRamMb,
            availableRamMb = availRamMb,
            ramUsagePercent = ramUsage,
            batteryPercent = batteryPct,
            isCharging = isCharging,
            batteryTemperatureCelsius = tempCelsius,
            totalStorageGb = totalStorageGb,
            availableStorageGb = availStorageGb,
            npuSupportInfo = npuInfo,
            thermalStatus = thermal
        )
    }

    private fun detectNpuAcceleration(): String {
        // Detect NNAPI and hardware properties
        val socModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Build.SOC_MODEL
        } else {
            Build.HARDWARE
        }

        val hasNnapi = File("/system/lib64/libneuralnetworks.so").exists() ||
                File("/system/lib/libneuralnetworks.so").exists()

        return when {
            socModel.isNotBlank() && socModel != "unknown" ->
                "Supported via NNAPI / $socModel"
            hasNnapi ->
                "NNAPI Acceleration Available"
            else ->
                "CPU / GPU Acceleration Supported"
        }
    }
}
