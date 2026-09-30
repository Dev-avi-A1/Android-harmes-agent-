package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.PlaylistAddCheck
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.outlined.Handyman
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.AiMode
import com.example.feature.chat.ChatScreen
import com.example.feature.chat.ChatViewModel
import com.example.feature.hardware.HardwareScreen
import com.example.feature.home.HomeScreen
import com.example.feature.memory.MemoryScreen
import com.example.feature.memory.MemoryViewModel
import com.example.feature.onboarding.OnboardingScreen
import com.example.feature.settings.SettingsScreen
import com.example.feature.tasks.TaskScreen
import com.example.feature.tasks.TaskViewModel
import com.example.feature.tools.ToolsScreen
import com.example.feature.vision.ScanScreen
import com.example.ui.theme.HarmesCyan
import com.example.ui.theme.HarmesTheme
import kotlinx.coroutines.launch

enum class HarmesNavDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
    CHAT("Chat", Icons.AutoMirrored.Filled.Chat, Icons.AutoMirrored.Outlined.Chat, "nav_chat"),
    TASKS("Tasks", Icons.AutoMirrored.Filled.PlaylistAddCheck, Icons.AutoMirrored.Outlined.PlaylistAddCheck, "nav_tasks"),
    MEMORY("Memory", Icons.Filled.Memory, Icons.Outlined.Memory, "nav_memory"),
    TOOLS("Tools", Icons.Filled.Handyman, Icons.Outlined.Handyman, "nav_tools")
}

enum class SecondaryScreen {
    SETTINGS,
    HARDWARE,
    SCAN
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as HarmesApplication
        val container = app.container

        setContent {
            HarmesTheme {
                HarmesMainApp(container = container)
            }
        }
    }
}

@Composable
fun HarmesMainApp(container: AppContainer) {
    val scope = rememberCoroutineScope()

    // Preferences & state
    val aiMode by container.settingsRepository.aiModeFlow.collectAsState(initial = AiMode.HYBRID)
    val hasCompletedOnboarding by container.settingsRepository.hasCompletedOnboardingFlow.collectAsState(initial = false)
    val activeTasks by container.taskRepository.getAllTasks().collectAsState(initial = emptyList())
    val isOnline by container.networkMonitor.isOnline.collectAsState(initial = true)

    // ViewModels
    val chatViewModel: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = ChatViewModel.provideFactory(
            container.conversationRepository,
            container.agent,
            container.speechRecognizer,
            container.speechSynthesizer
        )
    )

    val taskViewModel: TaskViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = TaskViewModel.provideFactory(container.taskRepository)
    )

    val memoryViewModel: MemoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = MemoryViewModel.provideFactory(
            container.memoryManager,
            container.settingsRepository
        )
    )

    var currentDestination by remember { mutableStateOf(HarmesNavDestination.HOME) }
    var secondaryScreen by remember { mutableStateOf<SecondaryScreen?>(null) }

    // Initialize initial chat conversation
    remember {
        chatViewModel.initConversation(null)
        true
    }

    if (!hasCompletedOnboarding) {
        OnboardingScreen(
            onComplete = { selectedMode ->
                scope.launch {
                    container.settingsRepository.setAiMode(selectedMode)
                    container.settingsRepository.setOnboardingCompleted(true)
                }
            }
        )
        return
    }

    // Back handling
    if (secondaryScreen != null) {
        BackHandler {
            secondaryScreen = null
        }
    } else if (currentDestination != HarmesNavDestination.HOME) {
        BackHandler {
            currentDestination = HarmesNavDestination.HOME
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (secondaryScreen == null) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    HarmesNavDestination.values().forEach { dest ->
                        val selected = currentDestination == dest
                        NavigationBarItem(
                            selected = selected,
                            onClick = { currentDestination = dest },
                            icon = {
                                Icon(
                                    imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                    contentDescription = dest.title,
                                    tint = if (selected) HarmesCyan else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            label = {
                                Text(
                                    text = dest.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) HarmesCyan else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = HarmesCyan.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag(dest.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = secondaryScreen to currentDestination,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { (secondary, dest) ->
                when {
                    secondary == SecondaryScreen.SETTINGS -> {
                        SettingsScreen(
                            currentAiMode = aiMode,
                            settingsRepository = container.settingsRepository,
                            onNavigateBack = { secondaryScreen = null }
                        )
                    }
                    secondary == SecondaryScreen.HARDWARE -> {
                        HardwareScreen(
                            hardwareInfo = container.hardwareMonitor.getHardwareInfo(),
                            backend = container.autoBackend,
                            isOnline = isOnline,
                            aiMode = aiMode,
                            onOpenSettings = { secondaryScreen = SecondaryScreen.SETTINGS }
                        )
                    }
                    secondary == SecondaryScreen.SCAN -> {
                        ScanScreen(
                            aiMode = aiMode,
                            onSendExtractedToChat = { text ->
                                secondaryScreen = null
                                currentDestination = HarmesNavDestination.CHAT
                                chatViewModel.sendMessage(text, aiMode)
                            },
                            onOpenSettings = { secondaryScreen = SecondaryScreen.SETTINGS }
                        )
                    }
                    else -> when (dest) {
                        HarmesNavDestination.HOME -> {
                            HomeScreen(
                                aiMode = aiMode,
                                hardwareInfo = container.hardwareMonitor.getHardwareInfo(),
                                activeTasks = activeTasks,
                                onNavigateToChat = { prompt ->
                                    currentDestination = HarmesNavDestination.CHAT
                                    if (prompt != null) {
                                        chatViewModel.sendMessage(prompt, aiMode)
                                    }
                                },
                                onNavigateToVoice = {
                                    currentDestination = HarmesNavDestination.CHAT
                                    chatViewModel.toggleVoiceListening()
                                },
                                onNavigateToScan = {
                                    secondaryScreen = SecondaryScreen.SCAN
                                },
                                onNavigateToTasks = {
                                    currentDestination = HarmesNavDestination.TASKS
                                },
                                onNavigateToHardware = {
                                    secondaryScreen = SecondaryScreen.HARDWARE
                                },
                                onOpenSettings = {
                                    secondaryScreen = SecondaryScreen.SETTINGS
                                }
                            )
                        }
                        HarmesNavDestination.CHAT -> {
                            ChatScreen(
                                viewModel = chatViewModel,
                                aiMode = aiMode,
                                onOpenSettings = { secondaryScreen = SecondaryScreen.SETTINGS }
                            )
                        }
                        HarmesNavDestination.TASKS -> {
                            TaskScreen(
                                viewModel = taskViewModel,
                                aiMode = aiMode,
                                onOpenSettings = { secondaryScreen = SecondaryScreen.SETTINGS }
                            )
                        }
                        HarmesNavDestination.MEMORY -> {
                            MemoryScreen(
                                viewModel = memoryViewModel,
                                aiMode = aiMode,
                                onOpenSettings = { secondaryScreen = SecondaryScreen.SETTINGS }
                            )
                        }
                        HarmesNavDestination.TOOLS -> {
                            ToolsScreen(
                                toolRegistry = container.toolRegistry,
                                aiMode = aiMode,
                                onOpenSettings = { secondaryScreen = SecondaryScreen.SETTINGS }
                            )
                        }
                    }
                }
            }
        }
    }
}
