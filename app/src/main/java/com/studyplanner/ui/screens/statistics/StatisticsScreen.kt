package com.studyplanner.ui.screens.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studyplanner.data.model.Subject
import com.studyplanner.ui.components.StatCard
import com.studyplanner.ui.theme.StudyPlannerTheme
import com.studyplanner.util.DateUtils
import com.studyplanner.viewmodel.StatisticsViewModel
import com.studyplanner.viewmodel.SubjectStudyData

@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val subjectData by viewModel.subjectStudyData.collectAsStateWithLifecycle()
    val weeklyData by viewModel.weeklyDailyData.collectAsStateWithLifecycle()

    val completionRate = if (uiState.totalTaskCount > 0)
        (uiState.completedTaskCount * 100f / uiState.totalTaskCount)
    else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Study time summary
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        label = "This Week",
                        value = DateUtils.formatMinutes(uiState.weeklyMinutes),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "This Month",
                        value = DateUtils.formatMinutes(uiState.monthlyMinutes),
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                }
            }

            // Task completion
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Task Completion", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${uiState.completedTaskCount} of ${uiState.totalTaskCount} tasks")
                            Text("${completionRate.toInt()}%", fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { completionRate / 100f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }

            // Weekly bar chart
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Weekly Study Hours", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(16.dp))
                        WeeklyBarChart(
                            data = weeklyData.map { it.dayLabel to it.minutes },
                            modifier = Modifier.fillMaxWidth().height(160.dp)
                        )
                    }
                }
            }

            // Per-subject breakdown
            if (subjectData.isNotEmpty()) {
                item {
                    Text("By Subject", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                val maxMinutes = subjectData.maxOfOrNull { it.totalMinutes } ?: 1
                items(subjectData) { data ->
                    SubjectStudyRow(data = data, maxMinutes = maxMinutes)
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun WeeklyBarChart(
    data: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    val barColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurface
    val maxVal = data.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data.forEach { (day, minutes) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    if (minutes > 0) {
                        Text(
                            text = if (minutes >= 60) "${minutes / 60}h" else "${minutes}m",
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    val fraction = minutes.toFloat() / maxVal
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .fillMaxHeight(fraction.coerceAtLeast(0.02f))
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(if (minutes > 0) barColor else barColor.copy(alpha = 0.15f))
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data.forEach { (day, _) ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                    color = labelColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun SubjectStudyRow(data: SubjectStudyData, maxMinutes: Int) {
    val subjectColor = runCatching {
        Color(android.graphics.Color.parseColor(data.subject.colorHex))
    }.getOrDefault(MaterialTheme.colorScheme.primary)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(data.subject.name, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = DateUtils.formatMinutes(data.totalMinutes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { data.totalMinutes.toFloat() / maxMinutes },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = subjectColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------
@Preview(showBackground = true, name = "WeeklyBarChart – sample data")
@Composable
private fun WeeklyBarChartPreview() {
    StudyPlannerTheme {
        Card(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Weekly Study Hours", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(16.dp))
                WeeklyBarChart(
                    data = listOf(
                        "Sun" to 0, "Mon" to 90, "Tue" to 45, "Wed" to 120,
                        "Thu" to 30, "Fri" to 60, "Sat" to 15
                    ),
                    modifier = Modifier.fillMaxWidth().height(160.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "SubjectStudyRow – sample")
@Composable
private fun SubjectStudyRowPreview() {
    StudyPlannerTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            SubjectStudyRow(
                data = SubjectStudyData(
                    subject = Subject(id = 1, name = "Mathematics", colorHex = "#4A45C0"),
                    totalMinutes = 135
                ),
                maxMinutes = 200
            )
        }
    }
}
