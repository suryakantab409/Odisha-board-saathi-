package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.AppLanguage
import com.example.model.AppStrings
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Indigo100
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Navy100
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.Saffron100
import com.example.ui.theme.Saffron500
import com.example.ui.theme.Saffron600
import com.example.viewmodel.BoardCatalogState
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    state: BoardCatalogState,
    onOpenMcqPractice: () -> Unit,
    onOpenImportantQuestions: () -> Unit,
    onOpenQuizSetup: () -> Unit,
    onOpenMockTests: () -> Unit,
    onOpenPyqs: () -> Unit,
    onOpenNotes: () -> Unit,
    onResumeSavedQuiz: () -> Unit,
    onUpdateExamCountdown: (Long) -> Unit,
    onUpdateDailyTarget: (Int) -> Unit
) {
    val prefs = state.preferences
    val lang = prefs.language
    val s = AppStrings.get(lang)
    var showCountdownDialog by remember { mutableStateOf(false) }

    val completedChapterIds = remember(state.chapterProgress) {
        state.chapterProgress.filter { it.completed }.map { it.chapterId }.toSet()
    }
    val overallChapterProgress = if (state.chapters.isEmpty()) 0f
    else completedChapterIds.size.toFloat() / state.chapters.size.toFloat()

    val nextChapter = remember(state.chapters, completedChapterIds) {
        state.chapters.firstOrNull { it.id !in completedChapterIds } ?: state.chapters.firstOrNull()
    }

    val dailyProgress = (prefs.todaySolvedCount.toFloat() / prefs.dailyTargetQuestions.coerceAtLeast(1)).coerceIn(0f, 1f)

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize()
        ) {
            // 1. Hero Greeting Banner with Board Badge & Exam Countdown
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_dashboard_banner),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Navy900.copy(alpha = 0.92f),
                                        Navy800.copy(alpha = 0.72f)
                                    )
                                )
                            )
                    )
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Saffron500
                            ) {
                                Text(
                                    text = if (lang == AppLanguage.OR) {
                                        "${prefs.board.titleOr} · ${prefs.board.classLabelOr}"
                                    } else {
                                        "${prefs.board.titleEn} · ${prefs.board.classLabelEn}"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.White.copy(alpha = 0.16f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = null,
                                        tint = Saffron500,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "${prefs.streakDays} ${s.streakLabel}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        val studentDisplay = prefs.displayName.ifBlank { s.defaultStudentName }
                        Text(
                            text = "${s.greetingPrefix}, $studentDisplay",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(12.dp))

                        // Exam Countdown Strip
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.14f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { showCountdownDialog = true }
                                .testTag("exam_countdown_card")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = Saffron500,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "${prefs.daysRemaining} ${s.daysLeftExam}",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = s.configureCountdown,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.78f)
                                        )
                                    }
                                }
                                Text(
                                    text = "${(overallChapterProgress * 100).roundToInt()}%",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Saffron500,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 2. Daily Study Target & Overall Preparation Cards
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = s.dailyStudyTarget,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "${prefs.todaySolvedCount} / ${prefs.dailyTargetQuestions}",
                                style = MaterialTheme.typography.titleLarge,
                                color = Saffron600,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { dailyProgress },
                                color = Saffron500,
                                trackColor = Saffron100,
                                strokeCap = StrokeCap.Round,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = s.overallPrep,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "${completedChapterIds.size} / ${state.chapters.size}",
                                style = MaterialTheme.typography.titleLarge,
                                color = Navy800,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { overallChapterProgress },
                                color = Navy800,
                                trackColor = Navy100,
                                strokeCap = StrokeCap.Round,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                            )
                        }
                    }
                }
            }

            // 3. Resume Unfinished Quiz OR Continue Learning
            if (state.savedQuizSession != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Saffron100,
                        border = BorderStroke(1.dp, Saffron500),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = Saffron600,
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = s.resumeQuiz,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Navy900,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = state.savedQuizSession.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Navy800,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Button(
                                onClick = onResumeSavedQuiz,
                                colors = ButtonDefaults.buttonColors(containerColor = Saffron500),
                                modifier = Modifier.testTag("resume_quiz_button")
                            ) {
                                Text("Resume")
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable(onClick = onOpenMcqPractice)
                            .testTag("recommended_practice_card")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = s.todaysRecommended,
                                style = MaterialTheme.typography.labelSmall,
                                color = Saffron600,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "${s.mcqPracticeTitle} (${state.mcqs.size} Qs)",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayArrow, null, tint = Saffron500, modifier = Modifier.size(16.dp))
                                Text("Start Now", style = MaterialTheme.typography.labelMedium, color = Saffron600)
                            }
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable(onClick = onOpenNotes)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = s.continueLearning,
                                style = MaterialTheme.typography.labelSmall,
                                color = Navy700,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = nextChapter?.displayTitle(lang) ?: s.notesLabel,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // 4. Quick Access Tiles
            item {
                Text(
                    text = s.quickAccess,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(10.dp))
                val quickItems = listOf(
                    QuickActionTileData(s.mcqsLabel, Icons.Default.Quiz, Saffron100, Saffron600, "quick_mcq", onOpenMcqPractice),
                    QuickActionTileData(s.importantQsLabel, Icons.Default.Star, Navy100, Navy800, "quick_imp", onOpenImportantQuestions),
                    QuickActionTileData(s.quizzesLabel, Icons.Default.Timer, Indigo100, Indigo600, "quick_quiz", onOpenQuizSetup),
                    QuickActionTileData(s.mockTestsLabel, Icons.Default.Assignment, Emerald100, Emerald600, "quick_mock", onOpenMockTests),
                    QuickActionTileData(s.pyqLabel, Icons.Default.HistoryEdu, Saffron100, Saffron600, "quick_pyq", onOpenPyqs),
                    QuickActionTileData(s.notesLabel, Icons.Default.AutoStories, Navy100, Navy800, "quick_notes", onOpenNotes)
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(quickItems, key = { it.tag }) { item ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .width(114.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(onClick = item.onClick)
                                .testTag(item.tag)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(item.bg)
                                ) {
                                    Icon(item.icon, contentDescription = item.label, tint = item.fg)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // 5. Subject-wise Progress
            item {
                Text(
                    text = s.subjectProgress,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        state.subjects.forEach { subject ->
                            val subChapters = state.chapters.filter { it.subjectId == subject.id }
                            val subDone = subChapters.count { it.id in completedChapterIds }
                            val frac = if (subChapters.isEmpty()) 0f else subDone.toFloat() / subChapters.size.toFloat()
                            Column {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = subject.displayName(lang),
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = "$subDone/${subChapters.size}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(Modifier.height(5.dp))
                                LinearProgressIndicator(
                                    progress = { frac },
                                    color = Saffron500,
                                    trackColor = Navy100,
                                    strokeCap = StrokeCap.Round,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 6. Recent Test Results
            if (state.attempts.isNotEmpty()) {
                item {
                    Text(
                        text = s.recentTests,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.attempts.take(3).forEach { att ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.padding(14.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Emerald600,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Column {
                                            Text(att.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text("${att.correctCount}/${att.totalQuestions} ${s.correctLabel}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    val pct = ((att.correctCount.toFloat() / att.totalQuestions.coerceAtLeast(1L)) * 100).roundToInt()
                                    Text(
                                        text = "$pct%",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Navy800,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCountdownDialog) {
        var sliderDays by remember { mutableFloatStateOf(prefs.daysRemaining.toFloat().coerceIn(1f, 365f)) }
        var sliderTarget by remember { mutableFloatStateOf(prefs.dailyTargetQuestions.toFloat().coerceIn(10f, 100f)) }
        AlertDialog(
            onDismissRequest = { showCountdownDialog = false },
            title = { Text(s.configureCountdown) },
            text = {
                Column {
                    Text("${s.examTargetDaysLabel}: ${sliderDays.roundToInt()}")
                    Slider(
                        value = sliderDays,
                        onValueChange = { sliderDays = it },
                        valueRange = 1f..365f,
                        colors = SliderDefaults.colors(thumbColor = Saffron500, activeTrackColor = Saffron500)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("${s.dailyTargetLabel}: ${sliderTarget.roundToInt()}")
                    Slider(
                        value = sliderTarget,
                        onValueChange = { sliderTarget = it },
                        valueRange = 10f..100f,
                        colors = SliderDefaults.colors(thumbColor = Navy800, activeTrackColor = Navy800)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateExamCountdown(sliderDays.roundToInt().toLong())
                        onUpdateDailyTarget(sliderTarget.roundToInt())
                        showCountdownDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Navy800)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCountdownDialog = false }) { Text("Cancel") }
            }
        )
    }
}

private data class QuickActionTileData(
    val label: String,
    val icon: ImageVector,
    val bg: Color,
    val fg: Color,
    val tag: String,
    val onClick: () -> Unit
)
