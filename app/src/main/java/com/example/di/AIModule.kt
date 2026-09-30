package com.example.di

import android.content.Context
import com.example.core.common.HardwareMonitor
import com.example.core.common.NetworkMonitor
import com.example.core.database.dao.MemoryDao
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
import com.example.domain.models.HardwareAccelerationBackend
import com.example.domain.models.LocalInferenceEngine
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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AIModule {

    @Provides
    @Singleton
    fun provideHardwareMonitor(@ApplicationContext context: Context): HardwareMonitor {
        return HardwareMonitor(context)
    }

    @Provides
    @Singleton
    fun provideHardwareAccelerationBackend(hardwareMonitor: HardwareMonitor): HardwareAccelerationBackend {
        val info = hardwareMonitor.getHardwareInfo()
        val cpu = CpuBackend()
        val gpu = GpuBackend()
        val npu = NpuBackend(info.npuSupportInfo)
        return AutoBackend(npu, gpu, cpu)
    }

    @Provides
    @Singleton
    fun provideLocalInferenceEngine(
        backend: HardwareAccelerationBackend
    ): LocalInferenceEngine {
        return HarmesLocalInferenceEngine(backend)
    }

    @Provides
    @Singleton
    fun provideLocalModelProvider(
        inferenceEngine: LocalInferenceEngine
    ): LocalModelProvider {
        return LocalModelProvider(inferenceEngine)
    }

    @Provides
    @Singleton
    fun provideCloudModelProvider(
        networkMonitor: NetworkMonitor
    ): CloudModelProvider {
        return CloudModelProvider(networkMonitor)
    }

    @Provides
    @Singleton
    fun provideFallbackModelProvider(): FallbackModelProvider {
        return FallbackModelProvider()
    }

    @Provides
    @Singleton
    fun provideModelRouter(
        localProvider: LocalModelProvider,
        cloudProvider: CloudModelProvider,
        fallbackProvider: FallbackModelProvider,
        hardwareMonitor: HardwareMonitor,
        networkMonitor: NetworkMonitor
    ): ModelRouter {
        return ModelRouter(
            localProvider = localProvider,
            cloudProvider = cloudProvider,
            fallbackProvider = fallbackProvider,
            hardwareMonitor = hardwareMonitor,
            networkMonitor = networkMonitor
        )
    }

    @Provides
    @Singleton
    fun provideVectorStore(): LocalVectorStore {
        return LocalVectorStore()
    }

    @Provides
    @Singleton
    fun provideMemoryManager(
        memoryDao: MemoryDao,
        vectorStore: LocalVectorStore
    ): MemoryManager {
        return MemoryManager(memoryDao, vectorStore)
    }

    @Provides
    @Singleton
    fun provideToolRegistry(@ApplicationContext context: Context): ToolRegistry {
        return ToolRegistry().apply {
            register(CalculatorTool())
            register(WebSearchTool(context))
            register(ReminderTool(context))
            register(CalendarTool(context))
            register(NotificationTool(context))
            register(DocumentTool())
            register(CameraTool())
            register(ShareTool(context))
            register(AppLauncherTool(context))
        }
    }

    @Provides
    @Singleton
    fun provideAgent(
        modelRouter: ModelRouter,
        toolRegistry: ToolRegistry,
        memoryManager: MemoryManager
    ): Agent {
        return HarmesAgentEngine(
            modelRouter = modelRouter,
            toolRegistry = toolRegistry,
            memoryManager = memoryManager
        )
    }

    @Provides
    @Singleton
    fun provideSpeechRecognizer(@ApplicationContext context: Context): SpeechRecognizer {
        return HarmesSpeechRecognizer(context)
    }

    @Provides
    @Singleton
    fun provideSpeechSynthesizer(@ApplicationContext context: Context): SpeechSynthesizer {
        return HarmesSpeechSynthesizer(context)
    }
}
