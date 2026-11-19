package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.local.BookmarkEntity
import com.example.data.local.McqEntity
import com.example.model.AppStrings
import com.example.model.BookmarkItemType
import com.example.ui.components.ContentLabelBadge
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.Crimson100
import com.example.ui.theme.Crimson600
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Navy100
import com.example.ui.theme.Navy800
import com.example.ui.theme.Saffron100
import com.example.ui.theme.Saffron500
import com.example.ui.theme.Saffron600
import com.example.viewmodel.BoardCatalogState

private enum class McqPracticeMode {
    SUBJECT_OR_CHAPTER,
    RANDOM,
    WRONG_RETRY,
    BOOKMARKED
}

@Composable
fun McqPracticeScreen(
    state: BoardCatalogState,
    onToggleBookmark: (String, BookmarkItemType, String, String, BookmarkEntity?, Boolean) -> Unit,
    onRecordPracticeScore: (boardId: String, subjectId: String, title: String, total: Int, correct: Int, wrongIds: List<String>) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val lang = state.preferences.language
    val s = AppStrings.get(lang)

    var mode by remember { mutableStateOf(McqPracticeMode.SUBJECT_OR_CHAPTER) }
    var selectedSubjectId by remember { mutableStateOf<String?>(null) }
    var selectedChapterId by remember { mutableStateOf<String?>(null) }

    val wrongIds = remember(state.attempts) {
        state.attempts.flatMap { it.wrongMcqIds }.toSet()
    }
    val bookmarkedIds = remember(state.bookmarks) {
        state.bookmarks.filter { it.itemType == BookmarkItemType.MCQ.name }.map { it.itemId }.toSet()
    }
    val bookmarkByItemId = remember(state.bookmarks) {
        state.bookmarks.filter { it.itemType == BookmarkItemType.MCQ.name }.associateBy { it.itemId }
    }

    val filteredMcqs: List<McqEntity> = remember(
        state.mcqs, mode, selectedSubjectId, selectedChapterId, wrongIds, bookmarkedIds
    ) {
        when (mode) {
            McqPracticeMode.SUBJECT_OR_CHAPTER -> state.mcqs.filter { m ->
                (selectedSubjectId == null || m.subjectId == selectedSubjectId) &&
                    (selectedChapterId == null || m.chapterId == selectedChapterId)
            }
            McqPracticeMode.RANDOM -> state.mcqs.shuffled()
            McqPracticeMode.WRONG_RETRY -> state.mcqs.filter { it.id in wrongIds }
            McqPracticeMode.BOOKMARKED -> state.mcqs.filter { it.id in bookmarkedIds }
        }
    }

    var currentIndex by remember(filteredMcqs) { mutableIntStateOf(0) }
    var selectedOption by remember(filteredMcqs, currentIndex) { mutableStateOf<Int?>(null) }
    var sessionCorrect by remember(mode, selectedSubjectId, selectedChapterId) { mutableIntStateOf(0) }
    var sessionAttempted by remember(mode, selectedSubjectId, selectedChapterId) { mutableIntStateOf(0) }
    var sessionWrongIds by remember(mode, selectedSubjectId, selectedChapterId) { mutableStateOf<List<String>>(emptyList()) }
    var sessionSaved by remember(mode, selectedSubjectId, selectedChapterId) { mutableStateOf(false) }

    val availableChapters = remember(state.chapters, selectedSubjectId) {
        if (selectedSubjectId == null) emptyList()
        else state.chapters.filter { it.subjectId == selectedSubjectId }
    }

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize()
        ) {
            // Mode Chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = mode == McqPracticeMode.SUBJECT_OR_CHAPTER,
                            onClick = { mode = McqPracticeMode.SUBJECT_OR_CHAPTER },
                            label = { Text("${s.subjectWiseMode} / ${s.chapterWiseMode}") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = mode == McqPracticeMode.RANDOM,
                            onClick = { mode = McqPracticeMode.RANDOM },
                            label = { Text(s.randomMode) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = mode == McqPracticeMode.WRONG_RETRY,
                            onClick = { mode = McqPracticeMode.WRONG_RETRY },
                            label = { Text("${s.wrongRetryMode} (${wrongIds.size})") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = mode == McqPracticeMode.BOOKMARKED,
                            onClick = { mode = McqPracticeMode.BOOKMARKED },
                            label = { Text("${s.bookmarkedMcqMode} (${bookmarkedIds.size})") }
                        )
                    }
                }
            }

            if (mode == McqPracticeMode.SUBJECT_OR_CHAPTER) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedSubjectId == null,
                                onClick = { selectedSubjectId = null; selectedChapterId = null },
                                label = { Text(s.allLabel) }
                            )
                        }
                        items(state.subjects, key = { it.id }) { sub ->
                            FilterChip(
                                selected = selectedSubjectId == sub.id,
                                onClick = {
                                    selectedSubjectId = if (selectedSubjectId == sub.id) null else sub.id
                                    selectedChapterId = null
                                },
                                label = { Text(sub.displayName(lang)) }
                            )
                        }
                    }
                }
                if (availableChapters.isNotEmpty()) {
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedChapterId == null,
                                    onClick = { selectedChapterId = null },
                                    label = { Text(s.allLabel) }
                                )
                            }
                            items(availableChapters, key = { it.id }) { ch ->
                                FilterChip(
                                    selected = selectedChapterId == ch.id,
                                    onClick = { selectedChapterId = if (selectedChapterId == ch.id) null else ch.id },
                                    label = { Text(ch.displayTitle(lang)) }
                                )
                            }
                        }
                    }
                }
            }

            // Live Score Strip
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Navy100.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "${s.scoreLabel}: $sessionCorrect / $sessionAttempted",
                            style = MaterialTheme.typography.titleSmall,
                            color = Navy800,
                            fontWeight = FontWeight.Bold
                        )
                        if (filteredMcqs.isNotEmpty()) {
                            Text(
                                text = "${currentIndex + 1} / ${filteredMcqs.size}",
                                style = MaterialTheme.typography.labelLarge,
                                color = Saffron600,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (filteredMcqs.isEmpty()) {
                item {
                    EmptyStateCard(icon = Icons.Default.Quiz, message = s.noItemsMessage)
                }
            } else {
                val mcqEntity = filteredMcqs[currentIndex.coerceIn(0, filteredMcqs.lastIndex)]
                val mcq = mcqEntity.toModel()
                val options = mcq.displayOptions(lang)
                val bm = bookmarkByItemId[mcq.id]

                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ContentLabelBadge(mcq.contentLabel, lang)
                                IconButton(
                                    onClick = {
                                        onToggleBookmark(
                                            mcq.id,
                                            BookmarkItemType.MCQ,
                                            mcq.boardId,
                                            mcq.subjectId,
                                            bm,
                                            false
                                        )
                                    },
                                    modifier = Modifier.testTag("bookmark_mcq_${mcq.id}")
                                ) {
                                    Icon(
                                        imageVector = if (bm != null) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = s.bookmarkLabel,
                                        tint = if (bm != null) Saffron600 else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = mcq.displayQuestion(lang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(14.dp))

                            options.forEachIndexed { idx, optText ->
                                val answered = selectedOption != null
                                val isCorrectOpt = idx.toLong() == mcq.correctIndex
                                val isChosen = selectedOption == idx
                                val bg = when {
                                    !answered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                    isCorrectOpt -> Emerald100
                                    isChosen -> Crimson100
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                }
                                val borderColor = when {
                                    !answered -> MaterialTheme.colorScheme.outline
                                    isCorrectOpt -> Emerald600
                                    isChosen -> Crimson600
                                    else -> MaterialTheme.colorScheme.outline
                                }

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = bg,
                                    border = BorderStroke(1.5.dp, borderColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable(enabled = !answered) {
                                            selectedOption = idx
                                            sessionAttempted++
                                            if (isCorrectOpt) {
                                                sessionCorrect++
                                            } else {
                                                sessionWrongIds = (sessionWrongIds + mcq.id).distinct()
                                            }
                                        }
                                        .testTag("mcq_option_$idx")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(14.dp)
                                    ) {
                                        Text(
                                            text = "${('A' + idx)}.",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Navy800
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            text = optText,
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (answered && isCorrectOpt) {
                                            Icon(Icons.Default.CheckCircle, null, tint = Emerald600)
                                        } else if (answered && isChosen && !isCorrectOpt) {
                                            Icon(Icons.Default.Cancel, null, tint = Crimson600)
                                        }
                                    }
                                }
                            }

                            if (selectedOption != null) {
                                Spacer(Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Saffron100.copy(alpha = 0.65f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = s.explanationLabel,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = Saffron600,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = mcq.displayExplanation(lang),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(14.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedButton(
                                    onClick = { if (currentIndex > 0) currentIndex-- },
                                    enabled = currentIndex > 0,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(s.previousLabel)
                                }
                                if (currentIndex < filteredMcqs.lastIndex) {
                                    Button(
                                        onClick = { currentIndex++ },
                                        colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(s.nextLabel)
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            if (!sessionSaved && sessionAttempted > 0) {
                                                sessionSaved = true
                                                onRecordPracticeScore(
                                                    state.preferences.board.id,
                                                    selectedSubjectId ?: "",
                                                    "MCQ Practice (${mode.name})",
                                                    sessionAttempted,
                                                    sessionCorrect,
                                                    sessionWrongIds
                                                )
                                            }
                                            onBack()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Saffron500),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (sessionSaved) "Done" else "Save & Finish")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
