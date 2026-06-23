package com.studyplanner.ui.screens.subjects

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studyplanner.data.model.Subject
import com.studyplanner.ui.components.EmptyStateMessage
import com.studyplanner.ui.components.SubjectColorDot
import com.studyplanner.ui.theme.SubjectColors
import com.studyplanner.viewmodel.SubjectViewModel

@Composable
fun SubjectsScreen(viewModel: SubjectViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var editingSubject by remember { mutableStateOf<Subject?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Subjects", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editingSubject = null; showDialog = true },
                icon = { Icon(Icons.Filled.Add, "Add subject") },
                text = { Text("Add Subject") }
            )
        }
    ) { padding ->
        if (uiState.subjects.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateMessage("No subjects yet. Add one to get started!")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(uiState.subjects, key = { it.id }) { subject ->
                    SubjectCard(
                        subject = subject,
                        studyMinutesFlow = viewModel.getTotalMinutesForSubject(subject.id),
                        onEdit = { editingSubject = subject; showDialog = true },
                        onDelete = { viewModel.deleteSubject(subject) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    if (showDialog) {
        SubjectDialog(
            subject = editingSubject,
            onDismiss = { showDialog = false },
            onSave = { name, colorHex ->
                if (editingSubject != null)
                    viewModel.updateSubject(editingSubject!!.copy(name = name, colorHex = colorHex))
                else
                    viewModel.addSubject(name, colorHex)
                showDialog = false
            }
        )
    }
}

@Composable
fun SubjectCard(
    subject: Subject,
    studyMinutesFlow: kotlinx.coroutines.flow.Flow<Int>,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val totalMinutes by studyMinutesFlow.collectAsState(initial = 0)
    val hours = totalMinutes / 60
    val mins = totalMinutes % 60

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubjectColorDot(subject.colorHex, size = 20)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(subject.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = if (totalMinutes > 0) "Studied: ${hours}h ${mins}m" else "No sessions yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit subject")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete subject",
                    tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun SubjectDialog(
    subject: Subject?,
    onDismiss: () -> Unit,
    onSave: (name: String, colorHex: String) -> Unit
) {
    var name by remember { mutableStateOf(subject?.name ?: "") }
    var selectedColor by remember {
        mutableStateOf(subject?.colorHex ?: "#4A45C0")
    }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (subject != null) "Edit Subject" else "New Subject") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = false },
                    label = { Text("Subject name") },
                    isError = nameError,
                    supportingText = if (nameError) {{ Text("Name cannot be empty") }} else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Color", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(SubjectColors) { color ->
                        val hex = "#%06X".format(color.value.toLong() and 0xFFFFFF)
                        val isSelected = selectedColor.uppercase() == hex.uppercase() ||
                                (subject?.colorHex?.uppercase() == hex.uppercase() && selectedColor == subject.colorHex)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(
                                    if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    else Modifier
                                )
                                .clickable {
                                    // Store as full ARGB hex for parsing
                                    selectedColor = "#%06X".format(color.value.toLong() and 0xFFFFFF)
                                }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isBlank()) { nameError = true; return@Button }
                onSave(name.trim(), selectedColor)
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------
import androidx.compose.ui.tooling.preview.Preview
import com.studyplanner.ui.theme.StudyPlannerTheme

@Preview(showBackground = true, name = "SubjectCard – with study time")
@Composable
private fun SubjectCardPreview() {
    StudyPlannerTheme {
        SubjectCard(
            subject           = Subject(id = 1, name = "Mathematics", colorHex = "#4A45C0"),
            studyMinutesFlow  = kotlinx.coroutines.flow.flowOf(95),
            onEdit            = {},
            onDelete          = {}
        )
    }
}

@Preview(showBackground = true, name = "SubjectDialog – new")
@Composable
private fun SubjectDialogNewPreview() {
    StudyPlannerTheme {
        SubjectDialog(subject = null, onDismiss = {}, onSave = { _, _ -> })
    }
}
