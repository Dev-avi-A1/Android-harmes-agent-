package com.example.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.MessageEntity
import com.example.core.model.AiMode
import com.example.core.model.ProcessingEngine
import com.example.core.model.SenderRole
import com.example.data.repository.ConversationRepository
import com.example.domain.agent.Agent
import com.example.domain.agent.AgentExecutionStep
import com.example.domain.agent.AgentRequest
import com.example.domain.agent.AgentResult
import com.example.domain.tools.AgentTool
import com.example.domain.tools.ToolInput
import com.example.domain.voice.SpeechRecognizer
import com.example.domain.voice.SpeechSynthesizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChatUiState(
    val conversationId: String = "",
    val inputText: String = "",
    val isGenerating: Boolean = false,
    val currentActionSummary: String? = null,
    val isVoiceListening: Boolean = false,
    val requiresApproval: Boolean = false,
    val pendingActionText: String? = null,
    val pendingTool: AgentTool? = null,
    val pendingInput: ToolInput? = null,
    val attachedUri: String? = null,
    val errorMessage: String? = null
)

class ChatViewModel(
    private val conversationRepository: ConversationRepository,
    private val agent: Agent,
    val speechRecognizer: SpeechRecognizer,
    val speechSynthesizer: SpeechSynthesizer
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messages: StateFlow<List<MessageEntity>> = _messages.asStateFlow()

    init {
        // Observe speech recognition
        viewModelScope.launch {
            speechRecognizer.recognizedText.collect { text ->
                if (text.isNotBlank()) {
                    _uiState.value = _uiState.value.copy(inputText = text)
                }
            }
        }
        viewModelScope.launch {
            speechRecognizer.isListening.collect { listening ->
                _uiState.value = _uiState.value.copy(isVoiceListening = listening)
            }
        }
    }

    fun initConversation(conversationId: String?) {
        viewModelScope.launch {
            val convId = if (conversationId.isNullOrBlank()) {
                conversationRepository.createConversation("Harmes Session")
            } else {
                conversationId
            }
            _uiState.value = _uiState.value.copy(conversationId = convId)

            conversationRepository.getMessages(convId).collect { msgList ->
                _messages.value = msgList
            }
        }
    }

    fun onInputChanged(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun toggleVoiceListening() {
        if (_uiState.value.isVoiceListening) {
            speechRecognizer.stopListening()
        } else {
            speechRecognizer.startListening()
        }
    }

    fun speakMessage(text: String) {
        speechSynthesizer.speak(text)
    }

    fun stopSpeaking() {
        speechSynthesizer.stop()
    }

    fun sendMessage(prompt: String? = null, aiMode: AiMode = AiMode.HYBRID) {
        val query = prompt ?: _uiState.value.inputText.trim()
        if (query.isBlank()) return

        val convId = _uiState.value.conversationId
        _uiState.value = _uiState.value.copy(
            inputText = "",
            isGenerating = true,
            currentActionSummary = "Analyzing your request…",
            errorMessage = null
        )

        viewModelScope.launch {
            // Save user message
            conversationRepository.addMessage(
                conversationId = convId,
                role = SenderRole.USER,
                content = query,
                engine = ProcessingEngine.LOCAL_AI
            )

            try {
                // Execute agent
                _uiState.value = _uiState.value.copy(currentActionSummary = "Selecting tools and evaluating privacy…")
                val result = agent.execute(
                    AgentRequest(
                        conversationId = convId,
                        userPrompt = query,
                        aiMode = aiMode
                    )
                )

                if (result.requiresUserApproval) {
                    _uiState.value = _uiState.value.copy(
                        isGenerating = false,
                        requiresApproval = true,
                        pendingActionText = result.pendingActionDescription,
                        pendingTool = result.pendingTool,
                        pendingInput = result.pendingInput,
                        currentActionSummary = null
                    )
                    return@launch
                }

                _uiState.value = _uiState.value.copy(currentActionSummary = "Finalizing verified response…")

                // Save assistant message
                conversationRepository.addMessage(
                    conversationId = convId,
                    role = SenderRole.ASSISTANT,
                    content = result.responseText,
                    engine = result.engineUsed,
                    toolName = result.toolResult?.data?.get("sources"),
                    actionSummary = result.steps.lastOrNull()?.actionSummary ?: "Completed"
                )

                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    currentActionSummary = null
                )
            } catch (e: Exception) {
                val errorMsg = "Harmes encountered an operational error: ${e.message}. Switched to local resilient core."
                conversationRepository.addMessage(
                    conversationId = convId,
                    role = SenderRole.ASSISTANT,
                    content = errorMsg,
                    engine = ProcessingEngine.LOCAL_AI,
                    actionSummary = "Execution halted"
                )
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    currentActionSummary = null,
                    errorMessage = e.message
                )
            }
        }
    }

    fun setAttachment(uri: String?) {
        _uiState.value = _uiState.value.copy(attachedUri = uri)
    }

    fun clearConversation() {
        val oldConvId = _uiState.value.conversationId
        viewModelScope.launch {
            if (oldConvId.isNotBlank()) {
                conversationRepository.deleteConversation(oldConvId)
            }
            val newConvId = conversationRepository.createConversation("Harmes Session")
            _uiState.value = _uiState.value.copy(
                conversationId = newConvId,
                inputText = "",
                attachedUri = null,
                isGenerating = false,
                currentActionSummary = null,
                errorMessage = null
            )
            conversationRepository.getMessages(newConvId).collect { msgList ->
                _messages.value = msgList
            }
        }
    }

    fun retryLastUserMessage(aiMode: AiMode) {
        val lastUserMessage = _messages.value.lastOrNull { it.role == SenderRole.USER }
        if (lastUserMessage != null) {
            sendMessage(lastUserMessage.content, aiMode)
        }
    }

    fun approvePendingAction() {
        val tool = _uiState.value.pendingTool
        val input = _uiState.value.pendingInput
        val convId = _uiState.value.conversationId

        _uiState.value = _uiState.value.copy(
            requiresApproval = false,
            isGenerating = true,
            currentActionSummary = "Executing approved tool: ${tool?.name}…"
        )

        viewModelScope.launch {
            if (tool != null && input != null) {
                val toolResult = tool.execute(input)
                conversationRepository.addMessage(
                    conversationId = convId,
                    role = SenderRole.ASSISTANT,
                    content = "Action approved by user.\n\n${toolResult.summary}",
                    engine = ProcessingEngine.TOOL,
                    actionSummary = "Authorized action executed"
                )
            }
            _uiState.value = _uiState.value.copy(
                isGenerating = false,
                currentActionSummary = null,
                pendingTool = null,
                pendingInput = null
            )
        }
    }

    fun denyPendingAction() {
        val convId = _uiState.value.conversationId
        _uiState.value = _uiState.value.copy(
            requiresApproval = false,
            pendingTool = null,
            pendingInput = null
        )
        viewModelScope.launch {
            conversationRepository.addMessage(
                conversationId = convId,
                role = SenderRole.ASSISTANT,
                content = "Security authorization denied by user. Operation safely cancelled.",
                engine = ProcessingEngine.LOCAL_AI,
                actionSummary = "Action cancelled"
            )
        }
    }

    companion object {
        fun provideFactory(
            conversationRepository: ConversationRepository,
            agent: Agent,
            speechRecognizer: SpeechRecognizer,
            speechSynthesizer: SpeechSynthesizer
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ChatViewModel(conversationRepository, agent, speechRecognizer, speechSynthesizer) as T
            }
        }
    }
}
