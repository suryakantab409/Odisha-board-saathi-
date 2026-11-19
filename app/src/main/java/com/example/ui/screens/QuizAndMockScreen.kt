package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.local.McqEntity
import com.example.model.AppStrings
import com.example.model.AttemptMode
import com.example.ui.components.AcademicDisclaimerBanner
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber600
import com.example.ui.theme.Crimson100
import com.example.ui.theme.Crimson600
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Navy100
import com.example.ui.theme.Navy800
import com.example.ui.theme.Saffron100
import com.example.ui.theme.Saffron500
import com.example.ui.theme.Saffron600
import com.example.viewmodel.ActiveQuizState
import com.example.viewmodel.BoardCatalogState
import kotlin.math.roundToInt

@Composable
fun QuizSetupScreen(
    state: BoardCatalogState,
    onStartQuiz: (title: String, mode: AttemptMode, subjectId: String, questions: List<McqEntity>, durationMin: Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val lang = state.preferences.language
    val s = AppStrings.get(lang)
    var selectedCount by remember { mutableIntStateOf(10) }
    var durationMinutes by remember { mutableFloatStateOf(15f) }
    var selectedSubjectId by remember { mutableStateOf<String?>(null) }

    val pool = remember(state.mcqs, selectedSubjectId) {
        val base = if (selectedSubjectId == null) state.mcqs else state.mcqs.filter { it.subjectId == selectedSubjectId }
        base.shuffled()
    }

    Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .widthIn(max = 700.dp)
                .fillMaxSize()
        ) {
            item {
                Text(s.quizEngineTitle, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            item {
                Text(s.filterSubject, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedSubjectId == null,
                        onClick = { selectedSubjectId = null },
                        label = { Text(s.allLabel) }
                    )
                    state.subjects.take(3).forEach { sub ->
                        FilterChip(
                            selected = selectedSubjectId == sub.id,
                            onClick = { selectedSubjectId = sub.id },
                            label = { Text(sub.displayName(lang)) }
                        )
                    }
                }
            }
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Question Count", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(10, 20, 30, 50).forEach { count ->
                                FilterChip(
                                    selected = selectedCount == count,
                                    onClick = { selectedCount = count },
                                    label = { Text("$count Qs") }
                                )
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Timer Duration", style = MaterialTheme.typography.titleSmall)
                            Text("${durationMinutes.roundToInt()} min", style = MaterialTheme.typography.titleSmall, color = Saffron600)
                        }
                        Slider(
                            value = durationMinutes,
                            onValueChange = { durationMinutes = it },
                            valueRange = 5f..60f,
                            colors = SliderDefaults.colors(thumbColor = Saffron500, activeTrackColor = Saffron500)
                        )
                        Text(
                            text = "Available questions in selected bank: ${pool.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            item {
                Button(
                    onClick = {
                        val picked = pool.take(selectedCount)
                        onStartQuiz(
                            "Timed Quiz (${picked.size} Qs)",
                            AttemptMode.QUIZ,
                            selectedSubjectId ?: "",
                            picked,
                            durationMinutes.roundToInt()
                        )
                    },
                    enabled = pool.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Saffron500),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("start_timed_quiz_button")
                ) {
                    Text(s.startQuiz, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun MockTestListScreen(
    state: BoardCatalogState,
    onStartMockExam: (title: String, mode: AttemptMode, subjectId: String, questions: List<McqEntity>, durationMin: Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val lang = state.preferences.language
    val s = AppStrings.get(lang)
    val mcqMap = remember(state.mcqs) { state.mcqs.associateBy { it.id } }

    Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize()
        ) {
            item {
                AcademicDisclaimerBanner(
                    text = "Mock exam patterns are admin-configurable. Starter exams below use demonstration patterns until updated by Admin."
                )
            }
            if (state.mockExams.isEmpty()) {
                item { EmptyStateCard(Icons.Default.Assignment, s.noItemsMessage) }
            } else {
                items(state.mockExams, key = { it.id }) { exam ->
                    val resolvedQs = exam.questionIds.mapNotNull { mcqMap[it] }
                        .ifEmpty { state.mcqs.filter { it.subjectId == exam.subjectId } }
                        .ifEmpty { state.mcqs }
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = exam.toModel().displayTitle(lang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "${exam.durationMinutes} min · ${exam.totalMarks} Marks · ${resolvedQs.size} Questions",
                                style = MaterialTheme.typography.labelLarge,
                                color = Saffron600
                            )
                            if (exam.patternNote.isNotBlank()) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = exam.patternNote,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    onStartMockExam(
                                        exam.toModel().displayTitle(lang),
                                        AttemptMode.MOCK_TEST,
                                        exam.subjectId,
                                        resolvedQs,
                                        exam.durationMinutes.toInt()
                                    )
                                },
                                enabled = resolvedQs.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                                modifier = Modifier.testTag("start_mock_${exam.id}")
                            ) {
                                Text("Start Mock Exam")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ActiveQuizRunnerScreen(
    quizState: ActiveQuizState,
    lang: com.example.model.AppLanguage,
    onSelectAnswer: (Int, Int) -> Unit,
    onToggleReview: (Int) -> Unit,
    onJumpQuestion: (Int) -> Unit,
    onSaveAndPause: () -> Unit,
    onSubmit: () -> Unit,
    onExit: () -> Unit
) {
    BackHandler {
        if (quizState.submitted) onExit() else onSaveAndPause()
    }

    val s = AppStrings.get(lang)
    var showPalette by remember { mutableStateOf(false) }

    if (quizState.submitted) {
        val total = quizState.questions.size.coerceAtLeast(1)
        val correct = quizState.questions.indices.count { idx ->
            quizState.selectedAnswers[idx]?.toLong() == quizState.questions[idx].correctIndex
        }
        val wrong = total - correct
        val pct = ((correct.toFloat() / total) * 100f).roundToInt()

        Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxSize()
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Navy800,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = Saffron500, modifier = Modifier.size(44.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(s.resultSummary, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(10.dp))
                            Text("$correct / $total ($pct%)", style = MaterialTheme.typography.displaySmall, color = Saffron500, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            Text("${s.correctLabel}: $correct  ·  ${s.wrongLabel}: $wrong", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f))
                        }
                    }
                }
                items(quizState.questions.indices.toList()) { idx ->
                    val q = quizState.questions[idx].toModel()
                    val chosen = quizState.selectedAnswers[idx]
                    val isRight = chosen?.toLong() == q.correctIndex
                    val opts = q.displayOptions(lang)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (isRight) Emerald600 else Crimson600),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Q${idx + 1}. ${q.displayQuestion(lang)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Your answer: ${chosen?.let { opts.getOrNull(it) } ?: "Skipped"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isRight) Emerald600 else Crimson600
                            )
                            Text(
                                text = "Correct: ${opts.getOrNull(q.correctIndex.toInt()) ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Emerald600,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "${s.explanationLabel}: ${q.displayExplanation(lang)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                item {
                    Button(
                        onClick = onExit,
                        colors = ButtonDefaults.buttonColors(containerColor = Saffron500),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("Back to Practice")
                    }
                }
            }
        }
        return
    }

    val currentQ = quizState.questions.getOrNull(quizState.currentIndex)?.toModel() ?: return
    val options = currentQ.displayOptions(lang)
    val mins = quizState.remainingSeconds / 60
    val secs = quizState.remainingSeconds % 60
    val isMarked = quizState.currentIndex in quizState.markedForReview

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Timer & Palette Header
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Navy800,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, null, tint = Saffron500, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = String.format("%02d:%02d", mins, secs),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Q ${quizState.currentIndex + 1} / ${quizState.questions.size}",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White
                )
                OutlinedButton(
                    onClick = { showPalette = true },
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.GridView, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(s.questionPalette, color = Color.White, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Question & Options
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(quizState.title, style = MaterialTheme.typography.labelMedium, color = Saffron600)
                        FilterChip(
                            selected = isMarked,
                            onClick = { onToggleReview(quizState.currentIndex) },
                            leadingIcon = { Icon(Icons.Default.Flag, null, modifier = Modifier.size(16.dp)) },
                            label = { Text(if (isMarked) s.markedForReview else s.markForReview) }
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = currentQ.displayQuestion(lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                }
                items(options.indices.toList()) { idx ->
                    val selected = quizState.selectedAnswers[quizState.currentIndex] == idx
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) Saffron100 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) Saffron500 else MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectAnswer(quizState.currentIndex, idx) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Text("${('A' + idx)}.", fontWeight = FontWeight.Bold, color = Navy800)
                            Spacer(Modifier.width(10.dp))
                            Text(options[idx], style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = onSaveAndPause,
                modifier = Modifier.weight(1f)
            ) {
                Text(s.saveAndExit, maxLines = 1)
            }
            OutlinedButton(
                onClick = { onJumpQuestion(quizState.currentIndex - 1) },
                enabled = quizState.currentIndex > 0,
                modifier = Modifier.weight(1f)
            ) {
                Text(s.previousLabel)
            }
            if (quizState.currentIndex < quizState.questions.lastIndex) {
                Button(
                    onClick = { onJumpQuestion(quizState.currentIndex + 1) },
                    colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(s.nextLabel)
                }
            } else {
                Button(
                    onClick = onSubmit,
                    colors = ButtonDefaults.buttonColors(containerColor = Saffron500),
                    modifier = Modifier.weight(1f).testTag("submit_quiz_button")
                ) {
                    Text(s.submitLabel)
                }
            }
        }
    }

    if (showPalette) {
        ModalBottomSheet(onDismissRequest = { showPalette = false }) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(s.questionPalette, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    quizState.questions.indices.forEach { idx ->
                        val answered = quizState.selectedAnswers.containsKey(idx)
                        val marked = idx in quizState.markedForReview
                        val bg = when {
                            marked -> Amber100
                            answered -> Emerald100
                            else -> Navy100
                        }
                        val fg = when {
                            marked -> Amber600
                            answered -> Emerald600
                            else -> Navy800
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(bg)
                                .clickable {
                                    onJumpQuestion(idx)
                                    showPalette = false
                                }
                        ) {
                            Text("${idx + 1}", color = fg, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = {
                        showPalette = false
                        onSubmit()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Saffron500),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(s.submitLabel)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
