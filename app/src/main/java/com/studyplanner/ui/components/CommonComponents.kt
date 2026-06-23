package com.studyplanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.studyplanner.data.model.Subject
import com.studyplanner.data.model.Task
import com.studyplanner.data.model.TaskWithSubject
import com.studyplanner.ui.theme.StudyPlannerTheme
import com.studyplanner.util.DateUtils

// ---------------------------------------------------------------------------
// Subject colour dot
// ---------------------------------------------------------------------------
@Composable
fun SubjectColorDot(colorHex: String, size: Int = 14) {
    val color = runCatching { Color(android.graphics.Color.parseColor(colorHex)) }
        .getOrDefault(MaterialTheme.colorScheme.primary)
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color)
    )
}

// ---------------------------------------------------------------------------
// Stat card
// ---------------------------------------------------------------------------
@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer
) {
    Card(
        modifier = modifier,
        colors   = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier              = Modifier.padding(16.dp),
            horizontalAlignment   = Alignment.CenterHorizontally
        ) {
            Text(
                text  = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Task card  (with optional edit action)
// ---------------------------------------------------------------------------
@Composable
fun TaskCard(
    taskWithSubject: TaskWithSubject,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    onEdit: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val task    = taskWithSubject.task
    val subject = taskWithSubject.subject
    val isOverdue = task.dueDate != null && !task.completed && DateUtils.isOverdue(task.dueDate)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = if (task.completed)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (task.completed) 0.dp else 2.dp)
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = task.completed, onCheckedChange = { onToggleComplete() })

            Spacer(Modifier.width(4.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text            = task.title,
                    style           = MaterialTheme.typography.titleSmall,
                    maxLines        = 1,
                    overflow        = TextOverflow.Ellipsis,
                    textDecoration  = if (task.completed) TextDecoration.LineThrough else null
                )
                if (subject != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SubjectColorDot(subject.colorHex, size = 8)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text  = subject.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (task.description.isNotBlank()) {
                    Text(
                        text     = task.description,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                task.dueDate?.let { due ->
                    Text(
                        text  = "Due: ${DateUtils.toRelativeLabel(due)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            isOverdue -> MaterialTheme.colorScheme.error
                            DateUtils.isToday(due) -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            // Edit button (optional)
            if (onEdit != null) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector        = Icons.Default.Edit,
                        contentDescription = "Edit task",
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector        = Icons.Default.Delete,
                    contentDescription = "Delete task",
                    tint               = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Section header
// ---------------------------------------------------------------------------
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text      = title,
        style     = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier  = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

// ---------------------------------------------------------------------------
// Empty state
// ---------------------------------------------------------------------------
@Composable
fun EmptyStateMessage(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            text  = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------
@Preview(showBackground = true, name = "StatCard – light")
@Composable
private fun StatCardPreview() {
    StudyPlannerTheme {
        StatCard(label = "Tasks Done", value = "12/20", modifier = Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true, name = "TaskCard – pending overdue")
@Composable
private fun TaskCardPendingPreview() {
    StudyPlannerTheme {
        TaskCard(
            taskWithSubject = TaskWithSubject(
                task    = Task(
                    id        = 1,
                    title     = "Complete algebra worksheet",
                    description = "Chapter 5 exercises",
                    subjectId = 1,
                    dueDate   = System.currentTimeMillis() - 86_400_000L, // yesterday
                    completed = false
                ),
                subject = Subject(id = 1, name = "Mathematics", colorHex = "#4A45C0")
            ),
            onToggleComplete = {},
            onDelete         = {},
            onEdit           = {},
            modifier         = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "TaskCard – completed")
@Composable
private fun TaskCardCompletedPreview() {
    StudyPlannerTheme {
        TaskCard(
            taskWithSubject = TaskWithSubject(
                task    = Task(
                    id        = 2,
                    title     = "Read chapter 3",
                    subjectId = 2,
                    dueDate   = null,
                    completed = true
                ),
                subject = Subject(id = 2, name = "Biology", colorHex = "#006B5F")
            ),
            onToggleComplete = {},
            onDelete         = {},
            modifier         = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "SubjectColorDot")
@Composable
private fun ColorDotPreview() {
    StudyPlannerTheme {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("#4A45C0","#006B5F","#9B3620","#006E1C","#7B4F00").forEach {
                SubjectColorDot(it, size = 24)
            }
        }
    }
}
