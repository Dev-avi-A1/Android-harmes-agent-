package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.core.model.MemoryType
import com.example.core.model.ProcessingEngine
import com.example.core.model.RiskLevel
import com.example.core.model.SenderRole
import com.example.core.model.TaskStatus

@Entity(
    tableName = "conversations",
    indices = [Index(value = ["updatedAt"])]
)
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val summary: String? = null
)

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["conversationId"]),
        Index(value = ["timestamp"])
    ]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val role: SenderRole,
    val content: String,
    val engine: ProcessingEngine,
    val toolName: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val actionSummary: String? = null,
    val attachmentUri: String? = null
)

@Entity(
    tableName = "tasks",
    indices = [Index(value = ["createdAt"]), Index(value = ["status"])]
)
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val riskLevel: RiskLevel = RiskLevel.LOW,
    val totalSteps: Int = 1,
    val completedSteps: Int = 0
)

@Entity(
    tableName = "task_steps",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["taskId"])]
)
data class TaskStepEntity(
    @PrimaryKey val id: String,
    val taskId: String,
    val stepIndex: Int,
    val description: String,
    val toolName: String?,
    val status: TaskStatus,
    val resultSummary: String? = null
)

@Entity(
    tableName = "memories",
    indices = [Index(value = ["type"]), Index(value = ["createdAt"])]
)
data class MemoryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val type: MemoryType,
    val confidence: Float = 1.0f,
    val createdAt: Long = System.currentTimeMillis(),
    val tags: String = "",
    val isPinned: Boolean = false
)

@Entity(
    tableName = "documents",
    indices = [Index(value = ["importedAt"])]
)
data class DocumentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val mimeType: String,
    val excerpt: String,
    val wordCount: Int,
    val chunkCount: Int,
    val summary: String,
    val importedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "tool_executions",
    indices = [Index(value = ["executionTimestamp"])]
)
data class ToolExecutionEntity(
    @PrimaryKey val id: String,
    val toolName: String,
    val executionTimestamp: Long = System.currentTimeMillis(),
    val inputSummary: String,
    val outputSummary: String,
    val status: String,
    val durationMs: Long
)

@Entity(
    tableName = "models"
)
data class ModelEntity(
    @PrimaryKey val id: String,
    val name: String,
    val provider: String,
    val sizeMb: Long,
    val quantization: String,
    val isLocal: Boolean,
    val isActive: Boolean,
    val supportedHardware: String,
    val contextLength: Int
)
