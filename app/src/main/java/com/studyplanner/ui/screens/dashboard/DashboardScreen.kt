package com.studyplanner.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studyplanner.data.model.Task
import com.studyplanner.ui.components.*
import com.studyplanner.ui.theme.StudyPlannerTheme
import com.studyplanner.util.DateUtils
import com.studyplanner.viewmodel.StatisticsViewModel
import com.studyplanner.viewmodel.TaskViewModel

@Composable
fun DashboardScreen(
    taskViewModel: TaskViewModel          = hiltViewModel(),
    statsViewModel: StatisticsViewModel   = hiltViewModel()
) {
    val todayTasks    by taskViewModel.todayTasks.collectAsStateWithLifecycle()
    val upcomingTasks by taskViewModel.upcomingTasks.collectAsStateWithLifecycle()
    val taskUiState   by taskViewModel.uiState.collectAsStateWithLifecycle()
    val statsUiState  by statsViewModel.uiState.collectAsStateWithLifecycle()

    var deletingTask by remember { mutableStateOf<Task?>(null) }

    val completionRate = if (taskUiState.totalCount > 0)
        (taskUiState.completedCount * 100 / taskUiState.totalCount) else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Study Planner", fontWeight = FontWeight.Bold)
                        Text(
                            text  = DateUtils.toDayString(System.currentTimeMillis()),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor     = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor  = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier              = Modifier.fillMaxSize().padding(padding),
            verticalArrangement   = Arrangement.spacedBy(4.dp)
        ) {
            // Quick stats row
            item {
                Row(
                    modifier                = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement   = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        label          = "Tasks Done",
                        value          = "${taskUiState.completedCount}/${taskUiState.totalCount}",
                        modifier       = Modifier.weight(1f)
                    )
                    StatCard(
                        label          = "Complete",
                        value          = "$completionRate%",
                        modifier       = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                    StatCard(
                        label          = "Wkly Hrs",
                        value          = DateUtils.formatMinutes(statsUiState.weeklyMinutes),
                        modifier       = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                }
            }

            // Today's tasks
            item { SectionHeader("Today's Tasks") }
            if (todayTasks.isEmpty()) {
                item {
                    EmptyStateMessage(
                        "No tasks due today — great work staying ahead!",
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            } else {
                items(todayTasks, key = { it.task.id }) { tws ->
                    TaskCard(
                        taskWithSubject  = tws,
                        onToggleComplete = {
                            taskViewModel.toggleTaskCompleted(tws.task.id, tws.task.completed)
                        },
                        onDelete = { deletingTask = tws.task },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            // Upcoming
            item { SectionHeader("Coming Up (next 7 days)") }
            if (upcomingTasks.isEmpty()) {
                item {
                    EmptyStateMessage(
                        "Nothing due in the next week.",
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            } else {
                items(upcomingTasks.take(5), key = { "upcoming_${it.task.id}" }) { tws ->
                    TaskCard(
                        taskWithSubject  = tws,
                        onToggleComplete = {
                            taskViewModel.toggleTaskCompleted(tws.task.id, tws.task.completed)
                        },
                        onDelete = { deletingTask = tws.task },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    // Delete confirmation
    deletingTask?.let { task ->
        AlertDialog(
            onDismissRequest = { deletingTask = null },
            title  = { Text("Delete task?") },
            text   = { Text("\"${task.title}\" will be permanently removed.") },
            confirmButton = {
                Button(
                    onClick = { taskViewModel.deleteTask(task); deletingTask = null },
                    colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deletingTask = null }) { Text("Cancel") } }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardPreview() {
    StudyPlannerTheme {
        // Lightweight structural preview — ViewModels not wired in previews
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Tasks Done", "8/20", modifier = Modifier.weight(1f))
                StatCard("Complete",   "40%",  modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer)
                StatCard("Wkly Hrs",  "3h 20m", modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            }
            SectionHeader("Today's Tasks")
            EmptyStateMessage("No tasks due today — great work staying ahead!")
        }
    }
}
