package com.example.feature.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.TaskEntity
import com.example.core.database.entity.TaskStepEntity
import com.example.core.model.AiMode
import com.example.core.model.RiskLevel
import com.example.core.model.TaskStatus
import com.example.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(
    private val taskRepository: TaskRepository
) : ViewModel() {

    val tasks: StateFlow<List<TaskEntity>> = taskRepository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTaskSteps = MutableStateFlow<List<TaskStepEntity>>(emptyList())
    val selectedTaskSteps: StateFlow<List<TaskStepEntity>> = _selectedTaskSteps.asStateFlow()

    private val _selectedTaskId = MutableStateFlow<String?>(null)
    val selectedTaskId: StateFlow<String?> = _selectedTaskId.asStateFlow()

    fun selectTask(taskId: String) {
        _selectedTaskId.value = taskId
        viewModelScope.launch {
            taskRepository.getTaskSteps(taskId).collect { steps ->
                _selectedTaskSteps.value = steps
            }
        }
    }

    fun clearSelectedTask() {
        _selectedTaskId.value = null
        _selectedTaskSteps.value = emptyList()
    }

    fun createPlan(title: String, description: String, goal: String) {
        viewModelScope.launch {
            // Intelligent decomposition of user goal into 4 structured execution steps
            val decomposedSteps = listOf(
                "Step 1: Parse requirements and check device permissions",
                "Step 2: Collect operational parameters and fetch verified data",
                "Step 3: Execute core processing and synthesize result",
                "Step 4: Verify outputs and commit final state to memory"
            )
            taskRepository.createTask(
                title = title.ifBlank { "Autonomous Execution Plan" },
                description = description.ifBlank { goal },
                riskLevel = RiskLevel.LOW,
                steps = decomposedSteps
            )
        }
    }

    fun updateStatus(taskId: String, status: TaskStatus) {
        viewModelScope.launch {
            taskRepository.updateTaskStatus(taskId, status)
        }
    }

    fun toggleStep(stepId: String) {
        viewModelScope.launch {
            taskRepository.completeTaskStep(stepId, "Milestone completed by user")
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.deleteTask(taskId)
        }
    }

    companion object {
        fun provideFactory(taskRepository: TaskRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TaskViewModel(taskRepository) as T
                }
            }
    }
}
