package com.studyplanner.ui.screens.tasks

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studyplanner.data.model.Subject
import com.studyplanner.data.model.Task
import com.studyplanner.data.model.TaskWithSubject
import com.studyplanner.ui.components.EmptyStateMessage
import com.studyplanner.ui.components.TaskCard
import com.studyplanner.util.DateUtils
import com.studyplanner.viewmodel.SubjectViewModel
import com.studyplanner.viewmodel.TaskFilter
import com.studyplanner.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    taskViewModel: TaskViewModel = hiltViewModel(),
    subjectViewModel: SubjectViewModel = hiltViewModel()
) {
    val uiState by taskViewModel.uiState.collectAsStateWithLifecycle()
    val subjectState by subjectViewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<TaskWithSubject?>(null) }
    var deletingTask by remember { mutableStateOf<Task?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tasks", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Filled.Add, "Add task") },
                text = { Text("Add Task") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.filter == TaskFilter.ALL,
                        onClick = { taskViewModel.setFilter(TaskFilter.ALL) },
                        label = { Text("All") }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.filter == TaskFilter.PENDING,
                        onClick = { taskViewModel.setFilter(TaskFilter.PENDING) },
                        label = { Text("Pending") }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.filter == TaskFilter.COMPLETED,
                        onClick = { taskViewModel.setFilter(TaskFilter.COMPLETED) },
                        label = { Text("Completed") }
                    )
                }
                items(subjectState.subjects) { subject ->
                    FilterChip(
                        selected = uiState.selectedSubjectId == subject.id,
                        onClick = {
                            taskViewModel.setSubjectFilter(
                                if (uiState.selectedSubjectId == subject.id) null else subject.id
                            )
                        },
                        label = { Text(subject.name) }
                    )
                }
            }

            if (uiState.tasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyStateMessage("No tasks here. Tap + to add one!")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.tasks, key = { it.task.id }) { twSubject ->
                        SwipeToDismissTaskCard(
                            taskWithSubject = twSubject,
                            onToggleComplete = {
                                taskViewModel.toggleTaskCompleted(twSubject.task.id, twSubject.task.completed)
                            },
                            onEdit   = { editingTask = twSubject },
                            onDelete = { deletingTask = twSubject.task }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Add dialog
    if (showAddDialog) {
        TaskDialog(
            subjects   = subjectState.subjects,
            taskToEdit = null,
            onDismiss  = { showAddDialog = false },
            onSave     = { title, desc, subjectId, dueDate ->
                taskViewModel.addTask(title, desc, subjectId, dueDate)
                showAddDialog = false
            }
        )
    }

    // Edit dialog
    editingTask?.let { tws ->
        TaskDialog(
            subjects   = subjectState.subjects,
            taskToEdit = tws,
            onDismiss  = { editingTask = null },
            onSave     = { title, desc, subjectId, dueDate ->
                taskViewModel.updateTask(
                    tws.task.copy(
                        title       = title,
                        description = desc,
                        subjectId   = subjectId,
                        dueDate     = dueDate
                    )
                )
                editingTask = null
            }
        )
    }

    // Delete confirmation
    deletingTask?.let { task ->
        AlertDialog(
            onDismissRequest = { deletingTask = null },
            title = { Text("Delete task?") },
            text  = { Text("\"${task.title}\" will be permanently removed.") },
            confirmButton = {
                Button(
                    onClick = { taskViewModel.deleteTask(task); deletingTask = null },
                    colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deletingTask = null }) { Text("Cancel") }
            }
        )
    }
}

// ---------------------------------------------------------------------------
// Swipe-to-dismiss wrapper
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissTaskCard(
    taskWithSubject: TaskWithSubject,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        },
        positionalThreshold = { it * 0.4f }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val bgColor by animateColorAsState(
                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                    MaterialTheme.colorScheme.errorContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                label = "swipe_bg"
            )
            val iconScale by animateFloatAsState(
                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) 1.2f else 0.8f,
                label = "swipe_icon_scale"
            )
            Box(
                modifier = Modifier.fillMaxSize().background(bgColor),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier.padding(end = 24.dp).scale(iconScale),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    ) {
        TaskCard(
            taskWithSubject  = taskWithSubject,
            onToggleComplete = onToggleComplete,
            onEdit           = onEdit,
            onDelete         = onDelete
        )
    }
}

// ---------------------------------------------------------------------------
// Unified Add / Edit Dialog
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDialog(
    subjects: List<Subject>,
    taskToEdit: TaskWithSubject?,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, subjectId: Long, dueDate: Long?) -> Unit
) {
    val isEditing = taskToEdit != null

    var title       by remember { mutableStateOf(taskToEdit?.task?.title ?: "") }
    var description by remember { mutableStateOf(taskToEdit?.task?.description ?: "") }
    var selectedSubject by remember {
        mutableStateOf(
            subjects.firstOrNull { it.id == taskToEdit?.task?.subjectId }
                ?: taskToEdit?.subject
                ?: subjects.firstOrNull()
        )
    }
    var dueDateText by remember {
        mutableStateOf(taskToEdit?.task?.dueDate?.let { DateUtils.toInputString(it) } ?: "")
    }
    var titleError   by remember { mutableStateOf(false) }
    var subjectError by remember { mutableStateOf(false) }
    var subjectDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Edit Task" else "New Task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Title
                OutlinedTextField(
                    value         = title,
                    onValueChange = { title = it; titleError = false },
                    label         = { Text("Title") },
                    isError       = titleError,
                    supportingText = if (titleError) {{ Text("Title cannot be empty") }} else null,
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
                // Description
                OutlinedTextField(
                    value         = description,
                    onValueChange = { description = it },
                    label         = { Text("Description (optional)") },
                    maxLines      = 3,
                    modifier      = Modifier.fillMaxWidth()
                )
                // Subject dropdown
                ExposedDropdownMenuBox(
                    expanded        = subjectDropdownExpanded,
                    onExpandedChange = { subjectDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value         = selectedSubject?.name ?: "Select subject",
                        onValueChange = {},
                        readOnly      = true,
                        label         = { Text("Subject") },
                        isError       = subjectError,
                        trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(subjectDropdownExpanded) },
                        modifier      = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded        = subjectDropdownExpanded,
                        onDismissRequest = { subjectDropdownExpanded = false }
                    ) {
                        subjects.forEach { subject ->
                            DropdownMenuItem(
                                text    = { Text(subject.name) },
                                onClick = {
                                    selectedSubject = subject
                                    subjectDropdownExpanded = false
                                    subjectError = false
                                }
                            )
                        }
                    }
                }
                // Due date
                OutlinedTextField(
                    value         = dueDateText,
                    onValueChange = { dueDateText = it },
                    label         = { Text("Due date  (MM/dd/yyyy)") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                var hasError = false
                if (title.isBlank())          { titleError   = true; hasError = true }
                if (selectedSubject == null)  { subjectError = true; hasError = true }
                if (hasError) return@Button

                val dueDate = if (dueDateText.isNotBlank())
                    DateUtils.parseInputDate(dueDateText)
                else null

                onSave(title.trim(), description.trim(), selectedSubject!!.id, dueDate)
            }) { Text(if (isEditing) "Update" else "Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
