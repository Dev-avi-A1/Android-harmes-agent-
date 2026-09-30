package com.example.domain.tools

import com.example.core.model.RiskLevel

data class ToolInput(
    val parameters: Map<String, String> = emptyMap(),
    val rawQuery: String = ""
)

data class ToolResult(
    val success: Boolean,
    val summary: String,
    val data: Map<String, String> = emptyMap(),
    val errorMessage: String? = null
)

interface AgentTool {
    val id: String
    val name: String
    val description: String
    val riskLevel: RiskLevel
    val isEnabled: Boolean
    val category: String

    suspend fun execute(input: ToolInput): ToolResult
}

class ToolRegistry {
    private val registeredTools = mutableMapOf<String, AgentTool>()
    private val enabledState = mutableMapOf<String, Boolean>()

    fun register(tool: AgentTool) {
        registeredTools[tool.id] = tool
        if (!enabledState.containsKey(tool.id)) {
            enabledState[tool.id] = tool.isEnabled
        }
    }

    fun getAllTools(): List<AgentTool> = registeredTools.values.toList()

    fun getTool(id: String): AgentTool? = registeredTools[id]

    fun isToolEnabled(id: String): Boolean = enabledState[id] ?: true

    fun setToolEnabled(id: String, enabled: Boolean) {
        enabledState[id] = enabled
    }

    fun findRelevantTool(query: String): AgentTool? {
        val lower = query.lowercase()
        return when {
            lower.contains("calculate") || lower.matches(Regex(".*\\d+\\s*[+\\-*/^]\\s*\\d+.*")) ->
                getTool("calculator")
            lower.contains("search") || lower.contains("find online") || lower.contains("web") ->
                getTool("web_search")
            lower.contains("remind") || lower.contains("reminder") || lower.contains("alarm") ->
                getTool("reminder")
            lower.contains("calendar") || lower.contains("schedule") || lower.contains("meeting") ->
                getTool("calendar")
            lower.contains("notify") || lower.contains("notification") || lower.contains("alert me") ->
                getTool("notification")
            lower.contains("scan") || lower.contains("camera") || lower.contains("ocr") || lower.contains("read text") ->
                getTool("camera")
            lower.contains("document") || lower.contains("read file") || lower.contains("pdf") ->
                getTool("document")
            lower.contains("open") || lower.contains("launch") ->
                getTool("app_launcher")
            lower.contains("share") || lower.contains("send to") ->
                getTool("share")
            else -> null
        }
    }
}
