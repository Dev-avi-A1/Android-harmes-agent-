package com.example.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.core.database.HarmesDatabase
import com.example.core.database.entity.ConversationEntity
import com.example.core.database.entity.DocumentEntity
import com.example.core.database.entity.MemoryEntity
import com.example.core.database.entity.MessageEntity
import com.example.core.database.entity.ModelEntity
import com.example.core.database.entity.TaskEntity
import com.example.core.database.entity.TaskStepEntity
import com.example.core.database.entity.ToolExecutionEntity
import com.example.core.model.AiMode
import com.example.core.model.MemoryType
import com.example.core.model.ProcessingEngine
import com.example.core.model.RiskLevel
import com.example.core.model.SenderRole
import com.example.core.model.TaskStatus
import com.example.domain.tools.ToolRegistry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore by preferencesDataStore(name = "harmes_settings")

interface ConversationRepository {
    fun getConversations(): Flow<List<ConversationEntity>>
    suspend fun createConversation(title: String): String
    fun getMessages(conversationId: String): Flow<List<MessageEntity>>
    suspend fun addMessage(
        conversationId: String,
        role: SenderRole,
        content: String,
        engine: ProcessingEngine = ProcessingEngine.LOCAL_AI,
        toolName: String? = null,
        actionSummary: String? = null,
        attachmentUri: String? = null
    ): MessageEntity
    suspend fun deleteConversation(id: String)
}

class ConversationRepositoryImpl(
    private val database: HarmesDatabase
) : ConversationRepository {
    override fun getConversations(): Flow<List<ConversationEntity>> =
        database.conversationDao().getAllConversations()

    override suspend fun createConversation(title: String): String {
        val id = UUID.randomUUID().toString()
        val conv = ConversationEntity(id = id, title = title)
        database.conversationDao().insertConversation(conv)
        return id
    }

    override fun getMessages(conversationId: String): Flow<List<MessageEntity>> =
        database.messageDao().getMessagesForConversation(conversationId)

    override suspend fun addMessage(
        conversationId: String,
        role: SenderRole,
        content: String,
        engine: ProcessingEngine,
        toolName: String?,
        actionSummary: String?,
        attachmentUri: String?
    ): MessageEntity {
        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            role = role,
            content = content,
            engine = engine,
            toolName = toolName,
            actionSummary = actionSummary,
            attachmentUri = attachmentUri,
            timestamp = System.currentTimeMillis()
        )
        database.messageDao().insertMessage(message)
        // Touch conversation updated timestamp
        database.conversationDao().getConversationById(conversationId)?.let { conv ->
            database.conversationDao().updateConversation(conv.copy(updatedAt = System.currentTimeMillis()))
        }
        return message
    }

    override suspend fun deleteConversation(id: String) {
        database.conversationDao().deleteConversation(id)
    }
}

interface TaskRepository {
    fun getAllTasks(): Flow<List<TaskEntity>>
    fun getTaskSteps(taskId: String): Flow<List<TaskStepEntity>>
    suspend fun createTask(
        title: String,
        description: String,
        riskLevel: RiskLevel = RiskLevel.LOW,
        steps: List<String>
    ): String
    suspend fun updateTaskStatus(taskId: String, status: TaskStatus)
    suspend fun completeTaskStep(stepId: String, resultSummary: String)
    suspend fun deleteTask(taskId: String)
}

class TaskRepositoryImpl(
    private val database: HarmesDatabase
) : TaskRepository {
    override fun getAllTasks(): Flow<List<TaskEntity>> = database.taskDao().getAllTasks()

    override fun getTaskSteps(taskId: String): Flow<List<TaskStepEntity>> =
        database.taskDao().getStepsForTaskFlow(taskId)

    override suspend fun createTask(
        title: String,
        description: String,
        riskLevel: RiskLevel,
        steps: List<String>
    ): String {
        val taskId = UUID.randomUUID().toString()
        val task = TaskEntity(
            id = taskId,
            title = title,
            description = description,
            status = TaskStatus.PLANNED,
            riskLevel = riskLevel,
            totalSteps = steps.size.coerceAtLeast(1),
            completedSteps = 0
        )
        database.taskDao().insertTask(task)

        val stepEntities = steps.mapIndexed { index, stepDesc ->
            TaskStepEntity(
                id = UUID.randomUUID().toString(),
                taskId = taskId,
                stepIndex = index + 1,
                description = stepDesc,
                toolName = null,
                status = if (index == 0) TaskStatus.RUNNING else TaskStatus.PLANNED
            )
        }
        database.taskDao().insertTaskSteps(stepEntities)
        return taskId
    }

    override suspend fun updateTaskStatus(taskId: String, status: TaskStatus) {
        database.taskDao().getTaskById(taskId)?.let { task ->
            database.taskDao().updateTask(task.copy(status = status))
        }
    }

    override suspend fun completeTaskStep(stepId: String, resultSummary: String) {
        val step = database.taskDao().getTaskStepById(stepId) ?: return
        val newStatus = if (step.status == TaskStatus.COMPLETED) TaskStatus.RUNNING else TaskStatus.COMPLETED
        database.taskDao().updateTaskStep(step.copy(status = newStatus, resultSummary = resultSummary))
        val allSteps = database.taskDao().getStepsForTask(step.taskId)
        val completedCount = allSteps.count { it.status == TaskStatus.COMPLETED }
        val task = database.taskDao().getTaskById(step.taskId)
        if (task != null) {
            val allDone = completedCount == allSteps.size && allSteps.isNotEmpty()
            database.taskDao().updateTask(
                task.copy(
                    completedSteps = completedCount,
                    status = if (allDone) TaskStatus.COMPLETED else TaskStatus.RUNNING,
                    completedAt = if (allDone) System.currentTimeMillis() else null
                )
            )
        }
    }

    override suspend fun deleteTask(taskId: String) {
        database.taskDao().deleteTask(taskId)
    }
}

interface SettingsRepository {
    val aiModeFlow: Flow<AiMode>
    val memoryEnabledFlow: Flow<Boolean>
    val hasCompletedOnboardingFlow: Flow<Boolean>
    suspend fun setAiMode(mode: AiMode)
    suspend fun setMemoryEnabled(enabled: Boolean)
    suspend fun setOnboardingCompleted(completed: Boolean)
}

class SettingsRepositoryImpl(private val context: Context) : SettingsRepository {
    private val KEY_AI_MODE = stringPreferencesKey("harmes_ai_mode")
    private val KEY_MEMORY_ENABLED = booleanPreferencesKey("harmes_memory_enabled")
    private val KEY_ONBOARDING = booleanPreferencesKey("harmes_onboarding_completed")

    override val aiModeFlow: Flow<AiMode> = context.dataStore.data.map { prefs ->
        val modeStr = prefs[KEY_AI_MODE] ?: AiMode.HYBRID.name
        try {
            AiMode.valueOf(modeStr)
        } catch (_: Exception) {
            AiMode.HYBRID
        }
    }

    override val memoryEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_MEMORY_ENABLED] ?: true
    }

    override val hasCompletedOnboardingFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING] ?: false
    }

    override suspend fun setAiMode(mode: AiMode) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AI_MODE] = mode.name
        }
    }

    override suspend fun setMemoryEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_MEMORY_ENABLED] = enabled
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING] = completed
        }
    }
}
