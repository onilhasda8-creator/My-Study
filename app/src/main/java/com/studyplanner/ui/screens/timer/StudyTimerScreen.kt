package com.studyplanner.ui.screens.timer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studyplanner.ui.theme.StudyPlannerTheme
import com.studyplanner.util.DateUtils
import com.studyplanner.viewmodel.StudyTimerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyTimerScreen(viewModel: StudyTimerViewModel = hiltViewModel()) {
    val state by viewModel.timerState.collectAsStateWithLifecycle()
    var subjectDropdownExpanded by remember { mutableStateOf(false) }

    val primaryColor   = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    // Arc animates through each full hour (3600 s)
    val animatedProgress by animateFloatAsState(
        targetValue     = (state.elapsedSeconds % 3600) / 3600f,
        animationSpec   = tween(durationMillis = 300, easing = LinearEasing),
        label           = "timer_arc"
    )

    // Pulsing ring while running
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by pulseAnim.animateFloat(
        initialValue    = 0.3f,
        targetValue     = 0f,
        animationSpec   = infiniteRepeatable(tween(1200, easing = EaseOut), RepeatMode.Restart),
        label           = "pulse_alpha"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title  = { Text("Study Timer", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier              = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement   = Arrangement.spacedBy(24.dp)
        ) {
            // Subject selector
            ExposedDropdownMenuBox(
                expanded        = subjectDropdownExpanded,
                onExpandedChange = { if (!state.isRunning) subjectDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value         = state.selectedSubject?.name ?: "Select a subject",
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text("Subject") },
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(subjectDropdownExpanded) },
                    enabled       = !state.isRunning,
                    modifier      = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded        = subjectDropdownExpanded,
                    onDismissRequest = { subjectDropdownExpanded = false }
                ) {
                    if (state.subjects.isEmpty()) {
                        DropdownMenuItem(
                            text    = { Text("No subjects — add one first") },
                            onClick = { subjectDropdownExpanded = false },
                            enabled = false
                        )
                    } else {
                        state.subjects.forEach { subject ->
                            DropdownMenuItem(
                                text    = { Text(subject.name) },
                                onClick = { viewModel.selectSubject(subject); subjectDropdownExpanded = false }
                            )
                        }
                    }
                }
            }

            // Timer ring
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(260.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke  = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
                    val inset   = 9.dp.toPx()
                    val arcRect = Offset(inset, inset)
                    val arcSize = Size(size.width - inset * 2, size.height - inset * 2)

                    // Background track
                    drawArc(
                        color      = surfaceVariant,
                        startAngle = -90f, sweepAngle = 360f,
                        useCenter  = false,
                        topLeft    = arcRect, size = arcSize, style = stroke
                    )
                    // Pulse ring (visible when running)
                    if (state.isRunning) {
                        drawArc(
                            color      = primaryColor.copy(alpha = pulseAlpha),
                            startAngle = -90f, sweepAngle = 360f,
                            useCenter  = false,
                            topLeft    = Offset(inset - 8.dp.toPx(), inset - 8.dp.toPx()),
                            size       = Size(size.width - (inset - 8.dp.toPx()) * 2,
                                             size.height - (inset - 8.dp.toPx()) * 2),
                            style      = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    // Progress arc
                    if (animatedProgress > 0f) {
                        drawArc(
                            color      = primaryColor,
                            startAngle = -90f, sweepAngle = animatedProgress * 360f,
                            useCenter  = false,
                            topLeft    = arcRect, size = arcSize, style = stroke
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text       = DateUtils.formatElapsedTime(state.elapsedSeconds),
                        fontSize   = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text  = when {
                            state.isRunning                          -> "Studying…"
                            state.elapsedSeconds > 0                 -> "Paused"
                            else                                     -> "Ready"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Primary controls
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (!state.isRunning) {
                    Button(
                        onClick  = { viewModel.startTimer() },
                        modifier = Modifier.weight(1f),
                        enabled  = state.selectedSubject != null
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (state.elapsedSeconds > 0) "Resume" else "Start")
                    }
                } else {
                    Button(
                        onClick  = { viewModel.stopTimer() },
                        modifier = Modifier.weight(1f),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Filled.Pause, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Pause")
                    }
                }

                AnimatedVisibility(!state.isRunning && state.elapsedSeconds >= 60) {
                    Button(
                        onClick  = { viewModel.saveSession() },
                        modifier = Modifier.weight(1f),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Save")
                    }
                }
            }

            // Reset button
            AnimatedVisibility(
                visible = !state.isRunning && state.elapsedSeconds > 0,
                enter   = fadeIn(), exit = fadeOut()
            ) {
                OutlinedButton(
                    onClick  = { viewModel.resetTimer() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Reset")
                }
            }

            // Error snack
            state.errorMessage?.let { msg ->
                Card(
                    colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier          = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Warning, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text(msg, style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f))
                        TextButton(onClick = { viewModel.clearError() }) { Text("OK") }
                    }
                }
            }

            // Saved session confirmation
            state.lastSavedSession?.let { session ->
                Card(
                    colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier          = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text  = "Session saved! ${DateUtils.formatMinutes(session.durationMinutes)} recorded.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // No-subjects hint
            if (state.subjects.isEmpty()) {
                Text(
                    text  = "Go to Subjects and add one to start studying.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Timer – idle")
@Composable
private fun TimerIdlePreview() {
    StudyPlannerTheme {
        // Static preview of the layout
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("00:00", fontSize = 48.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.PlayArrow, null)
                Spacer(Modifier.width(8.dp))
                Text("Start")
            }
        }
    }
}
