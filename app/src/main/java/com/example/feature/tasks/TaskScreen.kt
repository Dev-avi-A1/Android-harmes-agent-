package com.example.feature.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.entity.TaskEntity
import com.example.core.database.entity.TaskStepEntity
import com.example.core.model.AiMode
import com.example.core.model.TaskStatus
import com.example.ui.components.HarmesRiskBadge
import com.example.ui.components.HarmesTopBar
import com.example.ui.theme.HarmesAmber
import com.example.ui.theme.HarmesCyan
import com.example.ui.theme.HarmesElectricBlue
import com.example.ui.theme.HarmesRose
import com.example.ui.theme.HarmesTeal

@Composable
fun TaskScreen(
    viewModel: TaskViewModel,
    aiMode: AiMode,
    onOpenSettings: () -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()
    val selectedTaskId by viewModel.selectedTaskId.collectAsState()
    val selectedSteps by viewModel.selectedTaskSteps.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var statusFilter by remember { mutableStateOf<String>("All") }

    val filteredTasks = tasks.filter { task ->
        when (statusFilter) {
            "Active" -> task.status != TaskStatus.COMPLETED
            "Completed" -> task.status == TaskStatus.COMPLETED
            else -> true
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        HarmesTopBar(
            title = "Task Manager",
            subtitle = "Multi-Step Autonomous Plans",
            aiMode = aiMode,
            onModeClick = onOpenSettings
        )

        // Filter pills
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Active", "Completed").forEach { filter ->
                val isSelected = statusFilter == filter
                val count = when (filter) {
                    "Active" -> tasks.count { it.status != TaskStatus.COMPLETED }
                    "Completed" -> tasks.count { it.status == TaskStatus.COMPLETED }
                    else -> tasks.size
                }
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) HarmesCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, if (isSelected) HarmesCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                        modifier = Modifier.clickable { statusFilter = filter }
                    ) {
                        Text(
                            text = "$filter ($count)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) HarmesCyan else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            if (filteredTasks.isEmpty()) {
                TaskEmptyState(onCreateClick = { showCreateDialog = true })
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(2.dp)) }

                    items(filteredTasks, key = { it.id }) { task ->
                        TaskCardItem(
                            task = task,
                            isSelected = task.id == selectedTaskId,
                            steps = if (task.id == selectedTaskId) selectedSteps else emptyList(),
                            onClick = {
                                if (task.id == selectedTaskId) {
                                    viewModel.clearSelectedTask()
                                } else {
                                    viewModel.selectTask(task.id)
                                }
                            },
                            onDelete = { viewModel.deleteTask(task.id) },
                            onMarkCompleted = { viewModel.updateStatus(task.id, TaskStatus.COMPLETED) },
                            onToggleStep = { stepId -> viewModel.toggleStep(stepId) }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }

            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = HarmesCyan,
                contentColor = Color(0xFF041E26),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("create_task_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create Task")
            }
        }
    }

    if (showCreateDialog) {
        CreateTaskDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { title, desc, goal ->
                viewModel.createPlan(title, desc, goal)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun TaskCardItem(
    task: TaskEntity,
    isSelected: Boolean,
    steps: List<TaskStepEntity>,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onMarkCompleted: () -> Unit,
    onToggleStep: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(
            1.dp,
            if (isSelected) HarmesCyan.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusColor = when (task.status) {
                        TaskStatus.PLANNED -> HarmesElectricBlue
                        TaskStatus.RUNNING -> HarmesCyan
                        TaskStatus.WAITING -> HarmesAmber
                        TaskStatus.REQUIRES_CONFIRMATION -> HarmesAmber
                        TaskStatus.COMPLETED -> HarmesTeal
                        TaskStatus.FAILED -> HarmesRose
                        TaskStatus.CANCELLED -> MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = task.status.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    HarmesRiskBadge(riskLevel = task.riskLevel)
                }

                Row {
                    if (task.status != TaskStatus.COMPLETED) {
                        IconButton(onClick = onMarkCompleted, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Complete",
                                tint = HarmesTeal,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = task.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Step Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Steps (${task.completedSteps}/${task.totalSteps})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${if (task.totalSteps > 0) ((task.completedSteps.toFloat() / task.totalSteps) * 100).toInt() else 0}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = HarmesCyan
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { if (task.totalSteps > 0) task.completedSteps.toFloat() / task.totalSteps else 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = HarmesCyan,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            // Step Breakdown if expanded
            if (isSelected && steps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Decomposed Milestones (Tap to complete):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = HarmesCyan
                )
                Spacer(modifier = Modifier.height(6.dp))

                steps.forEach { step ->
                    val isDone = step.status == TaskStatus.COMPLETED
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDone) HarmesTeal.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (isDone) HarmesTeal.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable { onToggleStep(step.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (step.status) {
                                    TaskStatus.COMPLETED -> Icons.Default.CheckCircle
                                    TaskStatus.RUNNING -> Icons.Default.PlayArrow
                                    else -> Icons.Default.HourglassTop
                                },
                                contentDescription = null,
                                tint = when (step.status) {
                                    TaskStatus.COMPLETED -> HarmesTeal
                                    TaskStatus.RUNNING -> HarmesCyan
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = step.description,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isDone) FontWeight.Normal else FontWeight.Medium,
                                color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateTaskDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, desc: String, goal: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Decompose New Plan", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "Harmes Planner will analyze your goal and generate deterministic execution milestones.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = goal,
                    onValueChange = { goal = it },
                    label = { Text("Objective / Goal") },
                    placeholder = { Text("e.g. Audit security rules and backup local database") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(title, desc, goal) },
                enabled = title.isNotBlank() || goal.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = HarmesCyan)
            ) {
                Text("Decompose & Plan", color = Color(0xFF041E26), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun TaskEmptyState(onCreateClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.PlaylistAddCheck,
            contentDescription = null,
            tint = HarmesCyan,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Active Tasks",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Create multi-step plans. Harmes decomposes complex objectives into verified milestones.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onCreateClick,
            colors = ButtonDefaults.buttonColors(containerColor = HarmesCyan)
        ) {
            Text("Create New Plan", color = Color(0xFF041E26), fontWeight = FontWeight.Bold)
        }
    }
}
