package com.example.domain.models

import com.example.core.model.HardwareBackendType
import com.example.core.model.ProcessingEngine
import kotlinx.coroutines.flow.Flow

data class ModelRequest(
    val prompt: String,
    val systemInstruction: String? = null,
    val conversationHistory: List<Pair<String, String>> = emptyList(),
    val temperature: Float = 0.7f,
    val maxTokens: Int = 2048,
    val isPrivate: Boolean = false,
    val preferredEngine: ProcessingEngine? = null
)

data class ModelResponse(
    val text: String,
    val engineUsed: ProcessingEngine,
    val modelName: String,
    val tokenCount: Int,
    val durationMs: Long,
    val isFallback: Boolean = false
)

interface ModelProvider {
    val name: String
    val engineType: ProcessingEngine
    suspend fun isAvailable(): Boolean
    suspend fun generate(request: ModelRequest): ModelResponse
    fun generateStream(request: ModelRequest): Flow<String>
}

interface HardwareAccelerationBackend {
    val type: HardwareBackendType
    val name: String
    fun isSupported(): Boolean
    fun getThroughputEstimate(): String
}

class CpuBackend : HardwareAccelerationBackend {
    override val type = HardwareBackendType.CPU
    override val name = "Android CPU Multi-Threading"
    override fun isSupported(): Boolean = true
    override fun getThroughputEstimate(): String = "12-18 tokens/sec"
}

class GpuBackend : HardwareAccelerationBackend {
    override val type = HardwareBackendType.GPU
    override val name = "OpenCL / Vulkan GPU Delegate"
    override fun isSupported(): Boolean = true
    override fun getThroughputEstimate(): String = "28-42 tokens/sec"
}

class NpuBackend(private val detectedInfo: String) : HardwareAccelerationBackend {
    override val type = HardwareBackendType.NPU
    override val name = "NNAPI / DSP Hardware Accelerator"
    override fun isSupported(): Boolean = detectedInfo.contains("Supported") || detectedInfo.contains("NNAPI")
    override fun getThroughputEstimate(): String = if (isSupported()) "45-70 tokens/sec" else "Not Available"
}

class AutoBackend(private val npu: NpuBackend, private val gpu: GpuBackend, private val cpu: CpuBackend) : HardwareAccelerationBackend {
    override val type = HardwareBackendType.AUTO
    override val name = "Adaptive Dynamic Scheduler"
    override fun isSupported(): Boolean = true
    override fun getThroughputEstimate(): String = when {
        npu.isSupported() -> npu.getThroughputEstimate()
        gpu.isSupported() -> gpu.getThroughputEstimate()
        else -> cpu.getThroughputEstimate()
    }
}
