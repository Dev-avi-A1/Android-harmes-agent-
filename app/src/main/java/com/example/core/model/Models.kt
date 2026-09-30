package com.example.core.model

enum class AiMode(val displayName: String, val description: String) {
    LOCAL("Local", "Fast, private on-device processing"),
    HYBRID("Hybrid", "Local for simple queries, cloud for deep tasks"),
    CLOUD("Cloud", "Full cloud AI capabilities and large reasoning models")
}

enum class SenderRole {
    USER,
    ASSISTANT,
    SYSTEM
}

enum class ProcessingEngine {
    LOCAL_AI,
    CLOUD_AI,
    TOOL,
    HYBRID
}

enum class MemoryType(val displayName: String, val description: String) {
    WORKING("Working Memory", "Current active context"),
    EPISODIC("Episodic Memory", "Logs of past interactions & tasks"),
    SEMANTIC("Semantic Memory", "Long-term curated facts & preferences"),
    KNOWLEDGE("Knowledge Memory", "Imported documents and indexed data")
}

enum class TaskStatus(val displayName: String) {
    PLANNED("Planned"),
    RUNNING("Running"),
    WAITING("Waiting"),
    REQUIRES_CONFIRMATION("Requires Confirmation"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    CANCELLED("Cancelled")
}

enum class RiskLevel(val displayName: String) {
    LOW("Low Risk"),
    MEDIUM("Moderate Risk"),
    HIGH("Sensitive / High Risk")
}

enum class HardwareBackendType {
    CPU,
    GPU,
    NPU,
    AUTO
}
