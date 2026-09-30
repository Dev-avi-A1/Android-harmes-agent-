package com.example

import android.app.Application
import com.example.core.common.HardwareMonitor
import com.example.core.common.NetworkMonitor
import com.example.core.database.HarmesDatabase
import com.example.data.repository.ConversationRepository
import com.example.data.repository.ConversationRepositoryImpl
import com.example.data.repository.SettingsRepository
import com.example.data.repository.SettingsRepositoryImpl
import com.example.data.repository.TaskRepository
import com.example.data.repository.TaskRepositoryImpl
import com.example.domain.agent.Agent
import com.example.domain.agent.HarmesAgentEngine
import com.example.domain.memory.LocalVectorStore
import com.example.domain.memory.MemoryManager
import com.example.domain.models.AutoBackend
import com.example.domain.models.CloudModelProvider
import com.example.domain.models.CpuBackend
import com.example.domain.models.FallbackModelProvider
import com.example.domain.models.GpuBackend
import com.example.domain.models.HarmesLocalInferenceEngine
import com.example.domain.models.LocalModelProvider
import com.example.domain.models.ModelRouter
import com.example.domain.models.NpuBackend
import com.example.domain.tools.AppLauncherTool
import com.example.domain.tools.CalculatorTool
import com.example.domain.tools.CalendarTool
import com.example.domain.tools.CameraTool
import com.example.domain.tools.DocumentTool
import com.example.domain.tools.NotificationTool
import com.example.domain.tools.ReminderTool
import com.example.domain.tools.ShareTool
import com.example.domain.tools.ToolRegistry
import com.example.domain.tools.WebSearchTool
import com.example.domain.voice.HarmesSpeechRecognizer
import com.example.domain.voice.HarmesSpeechSynthesizer
import com.example.domain.voice.SpeechRecognizer
import com.example.domain.voice.SpeechSynthesizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(val application: Application) {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database: HarmesDatabase by lazy {
        HarmesDatabase.getDatabase(application, applicationScope)
    }

    val hardwareMonitor: HardwareMonitor by lazy {
        HardwareMonitor(application)
    }

    val networkMonitor: NetworkMonitor by lazy {
        NetworkMonitor(application)
    }

    private val hardwareInfo by lazy { hardwareMonitor.getHardwareInfo() }

    val cpuBackend by lazy { CpuBackend() }
    val gpuBackend by lazy { GpuBackend() }
    val npuBackend by lazy { NpuBackend(hardwareInfo.npuSupportInfo) }
    val autoBackend by lazy { AutoBackend(npuBackend, gpuBackend, cpuBackend) }

    val localInferenceEngine by lazy {
        HarmesLocalInferenceEngine(autoBackend)
    }

    val localModelProvider by lazy {
        LocalModelProvider(localInferenceEngine)
    }

    val cloudModelProvider by lazy {
        CloudModelProvider(networkMonitor)
    }

    val fallbackModelProvider by lazy {
        FallbackModelProvider()
    }

    val modelRouter by lazy {
        ModelRouter(
            localProvider = localModelProvider,
            cloudProvider = cloudModelProvider,
            fallbackProvider = fallbackModelProvider,
            hardwareMonitor = hardwareMonitor,
            networkMonitor = networkMonitor
        )
    }

    val toolRegistry by lazy {
        ToolRegistry().apply {
            register(CalculatorTool())
            register(WebSearchTool(application))
            register(ReminderTool(application))
            register(CalendarTool(application))
            register(NotificationTool(application))
            register(DocumentTool())
            register(CameraTool())
            register(ShareTool(application))
            register(AppLauncherTool(application))
        }
    }

    val vectorStore by lazy {
        LocalVectorStore()
    }

    val memoryManager by lazy {
        MemoryManager(database.memoryDao(), vectorStore)
    }

    val agent: Agent by lazy {
        HarmesAgentEngine(
            modelRouter = modelRouter,
            toolRegistry = toolRegistry,
            memoryManager = memoryManager
        )
    }

    val speechRecognizer: SpeechRecognizer by lazy {
        HarmesSpeechRecognizer(application)
    }

    val speechSynthesizer: SpeechSynthesizer by lazy {
        HarmesSpeechSynthesizer(application)
    }

    val conversationRepository: ConversationRepository by lazy {
        ConversationRepositoryImpl(database)
    }

    val taskRepository: TaskRepository by lazy {
        TaskRepositoryImpl(database)
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(application)
    }
}

class HarmesApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
