package com.example.feature.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.MemoryEntity
import com.example.core.model.MemoryType
import com.example.data.repository.SettingsRepository
import com.example.domain.memory.MemoryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MemoryViewModel(
    private val memoryManager: MemoryManager,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val isMemoryEnabled: StateFlow<Boolean> = settingsRepository.memoryEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _selectedType = MutableStateFlow<MemoryType?>(null)
    val selectedType: StateFlow<MemoryType?> = _selectedType.asStateFlow()

    val allMemories: StateFlow<List<MemoryEntity>> = memoryManager.getAllMemoriesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectLayer(type: MemoryType?) {
        _selectedType.value = type
    }

    fun toggleMemoryEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setMemoryEnabled(enabled)
            memoryManager.isMemoryEnabled = enabled
        }
    }

    fun addMemory(title: String, content: String, type: MemoryType, tags: String) {
        viewModelScope.launch {
            val tagList = tags.split(",").map { it.trim() }.filter { it.isNotBlank() }
            memoryManager.saveMemory(
                title = title,
                content = content,
                type = type,
                tags = tagList
            )
        }
    }

    fun forgetMemory(id: String) {
        viewModelScope.launch {
            memoryManager.forgetMemory(id)
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            memoryManager.clearAll()
        }
    }

    companion object {
        fun provideFactory(
            memoryManager: MemoryManager,
            settingsRepository: SettingsRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MemoryViewModel(memoryManager, settingsRepository) as T
            }
        }
    }
}
