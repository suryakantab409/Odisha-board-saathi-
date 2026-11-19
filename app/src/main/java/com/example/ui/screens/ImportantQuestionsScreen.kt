package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.BookmarkEntity
import com.example.model.AppLanguage
import com.example.model.AppStrings
import com.example.model.BookmarkItemType
import com.example.model.Difficulty
import com.example.model.QuestionCategory
import com.example.ui.components.AcademicDisclaimerBanner
import com.example.ui.components.ContentLabelBadge
import com.example.ui.components.DifficultyBadge
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.Navy100
import com.example.ui.theme.Navy50
import com.example.ui.theme.Navy800
import com.example.ui.theme.Saffron100
import com.example.ui.theme.Saffron600
import com.example.viewmodel.BoardCatalogState

@Composable
fun ImportantQuestionsScreen(
    state: BoardCatalogState,
    onToggleBookmark: (String, BookmarkItemType, String, String, BookmarkEntity?, Boolean) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val lang = state.preferences.language
    val s = AppStrings.get(lang)

    var selectedSubjectId by remember { mutableStateOf<String?>(null) }
    var selectedChapterId by remember { mutableStateOf<String?>(null) }
    var selectedMarks by remember { mutableStateOf<Long?>(null) }
    var selectedDifficulty by remember { mutableStateOf<String?>(null) }
    var expandedQuestionIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    val availableChapters = remember(state.chapters, selectedSubjectId) {
        if (selectedSubjectId == null) state.chapters
        else state.chapters.filter { it.subjectId == selectedSubjectId }
    }

    val filteredQuestions = remember(
        state.importantQuestions,
        selectedSubjectId,
        selectedChapterId,
        selectedMarks,
        selectedDifficulty
    ) {
        state.importantQuestions.filter { q ->
            (selectedSubjectId == null || q.subjectId == selectedSubjectId) &&
                (selectedChapterId == null || q.chapterId == selectedChapterId) &&
                (selectedMarks == null || q.marks == selectedMarks) &&
                (selectedDifficulty == null || q.difficulty == selectedDifficulty)
        }
    }

    val bookmarkByItemId = remember(state.bookmarks) {
        state.bookmarks.filter { it.itemType == BookmarkItemType.IMPORTANT_Q.name }
            .associateBy { it.itemId }
    }
    val subjectMap = remember(state.subjects) { state.subjects.associateBy { it.id } }
    val chapterMap = remember(state.chapters) { state.chapters.associateBy { it.id } }

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize()
        ) {
            item {
                AcademicDisclaimerBanner(text = s.disclaimerBanner)
            }

            // Subject Filter Chips
            item {
                Text(s.filterSubject, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
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

            // Chapter Filter Chips (when subject selected or chapters exist)
            if (availableChapters.isNotEmpty()) {
                item {
                    Text(s.filterChapter, style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(6.dp))
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

            // Marks & Difficulty Filter Row
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf(1L, 2L, 3L, 5L)) { marks ->
                        FilterChip(
                            selected = selectedMarks == marks,
                            onClick = { selectedMarks = if (selectedMarks == marks) null else marks },
                            label = { Text("$marks ${s.filterMarks}") }
                        )
                    }
                    items(Difficulty.entries) { diff ->
                        FilterChip(
                            selected = selectedDifficulty == diff.name,
                            onClick = { selectedDifficulty = if (selectedDifficulty == diff.name) null else diff.name },
                            label = { Text(if (lang == AppLanguage.OR) diff.labelOr else diff.labelEn) }
                        )
                    }
                }
            }

            if (filteredQuestions.isEmpty()) {
                item {
                    EmptyStateCard(icon = Icons.Default.QuestionAnswer, message = s.noItemsMessage)
                }
            } else {
                items(filteredQuestions, key = { it.id }) { q ->
                    val model = q.toModel()
                    val expanded = q.id in expandedQuestionIds
                    val bm = bookmarkByItemId[q.id]
                    val subjectName = subjectMap[q.subjectId]?.displayName(lang) ?: ""
                    val chapterName = chapterMap[q.chapterId]?.displayTitle(lang) ?: ""
                    val cat = QuestionCategory.fromRaw(q.category)

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
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    StatusBadge("${q.marks}M · ${if (lang == AppLanguage.OR) cat.labelOr else cat.labelEn}", Navy100, Navy800)
                                    DifficultyBadge(q.difficulty, lang)
                                }
                                Row {
                                    IconButton(
                                        onClick = {
                                            onToggleBookmark(
                                                q.id,
                                                BookmarkItemType.IMPORTANT_Q,
                                                q.boardId,
                                                q.subjectId,
                                                bm,
                                                false
                                            )
                                        },
                                        modifier = Modifier.testTag("bookmark_imp_${q.id}")
                                    ) {
                                        Icon(
                                            imageVector = if (bm != null) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                            contentDescription = s.bookmarkLabel,
                                            tint = if (bm != null) Saffron600 else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (bm != null) {
                                        IconButton(
                                            onClick = {
                                                onToggleBookmark(
                                                    q.id,
                                                    BookmarkItemType.IMPORTANT_Q,
                                                    q.boardId,
                                                    q.subjectId,
                                                    bm,
                                                    true
                                                )
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlaylistAddCheck,
                                                contentDescription = s.revisionListLabel,
                                                tint = if (bm.inRevisionList) Navy800 else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(6.dp))
                            ContentLabelBadge(q.contentLabel, lang)
                            if (q.sourceYear.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Source: ${q.sourceYear}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "$subjectName · $chapterName",
                                style = MaterialTheme.typography.labelMedium,
                                color = Saffron600
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = model.displayQuestion(lang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(Modifier.height(8.dp))
                            TextButton(
                                onClick = {
                                    expandedQuestionIds = if (expanded) expandedQuestionIds - q.id else expandedQuestionIds + q.id
                                },
                                modifier = Modifier.testTag("toggle_ans_${q.id}")
                            ) {
                                Text(if (expanded) s.hideAnswer else s.showAnswer)
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            AnimatedVisibility(visible = expanded) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Navy50,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = model.displayAnswer(lang),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Navy800,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
