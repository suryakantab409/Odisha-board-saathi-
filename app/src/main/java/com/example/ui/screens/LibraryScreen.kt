package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.BookmarkEntity
import com.example.data.local.ChapterEntity
import com.example.model.AppLanguage
import com.example.model.AppStrings
import com.example.model.BookmarkItemType
import com.example.model.NoteType
import com.example.ui.components.AcademicDisclaimerBanner
import com.example.ui.components.ContentLabelBadge
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Navy100
import com.example.ui.theme.Navy50
import com.example.ui.theme.Navy800
import com.example.ui.theme.Saffron100
import com.example.ui.theme.Saffron500
import com.example.ui.theme.Saffron600
import com.example.viewmodel.BoardCatalogState

@Composable
fun LibraryScreen(
    state: BoardCatalogState,
    initialTab: Int = 0,
    onToggleChapterCompleted: (ChapterEntity, Boolean) -> Unit,
    onToggleNoteOffline: (String, Boolean) -> Unit,
    onToggleNoteBookmark: (String, BookmarkItemType, String, String, BookmarkEntity?, Boolean) -> Unit
) {
    val lang = state.preferences.language
    val s = AppStrings.get(lang)
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(initialTab.coerceIn(0, 2)) }
    LaunchedEffect(initialTab) {
        selectedTab = initialTab.coerceIn(0, 2)
    }

    val tabs = listOf(s.subjectsTab, s.pyqTab, s.notesTab)
    val completedChapterIds = remember(state.chapterProgress) {
        state.chapterProgress.filter { it.completed }.map { it.chapterId }.toSet()
    }
    val subjectMap = remember(state.subjects) { state.subjects.associateBy { it.id } }

    Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize()
        ) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                tabs.forEachIndexed { index, label ->
                    SegmentedButton(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = tabs.size),
                        modifier = Modifier.testTag("library_tab_$index")
                    ) {
                        Text(label, maxLines = 1)
                    }
                }
            }

            when (selectedTab) {
                0 -> SyllabusTab(
                    state = state,
                    completedChapterIds = completedChapterIds,
                    onToggleChapterCompleted = onToggleChapterCompleted
                )
                1 -> PyqTab(
                    state = state,
                    subjectMap = subjectMap,
                    onOpenPdf = { uri -> openPdfSafely(context, uri) }
                )
                2 -> NotesTab(
                    state = state,
                    subjectMap = subjectMap,
                    onToggleNoteOffline = onToggleNoteOffline,
                    onToggleNoteBookmark = onToggleNoteBookmark,
                    onOpenPdf = { uri -> openPdfSafely(context, uri) }
                )
            }
        }
    }
}

@Composable
private fun SyllabusTab(
    state: BoardCatalogState,
    completedChapterIds: Set<String>,
    onToggleChapterCompleted: (ChapterEntity, Boolean) -> Unit
) {
    val lang = state.preferences.language
    val s = AppStrings.get(lang)
    var selectedSubjectId by remember { mutableStateOf<String?>(null) }

    val subjectsToShow = remember(state.subjects, selectedSubjectId) {
        if (selectedSubjectId == null) state.subjects
        else state.subjects.filter { it.id == selectedSubjectId }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            AcademicDisclaimerBanner(text = s.starterSyllabusNote)
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedSubjectId == null,
                        onClick = { selectedSubjectId = null },
                        label = { Text(s.allLabel) }
                    )
                }
                items(state.subjects, key = { it.id }) { subject ->
                    FilterChip(
                        selected = selectedSubjectId == subject.id,
                        onClick = { selectedSubjectId = subject.id },
                        label = { Text(subject.toModel().displayName(lang)) }
                    )
                }
            }
        }

        if (subjectsToShow.isEmpty()) {
            item { EmptyStateCard(Icons.Default.MenuBook, s.noItemsMessage) }
        } else {
            items(subjectsToShow, key = { it.id }) { subject ->
                val chapters = state.chapters
                    .filter { it.subjectId == subject.id }
                    .sortedBy { it.chapterNumber }
                val doneCount = chapters.count { it.id in completedChapterIds }

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = subject.toModel().displayName(lang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            StatusBadge(
                                text = "$doneCount/${chapters.size}",
                                containerColor = if (chapters.isNotEmpty() && doneCount == chapters.size) Emerald100 else Navy100,
                                contentColor = if (chapters.isNotEmpty() && doneCount == chapters.size) Emerald600 else Navy800
                            )
                        }
                        if (subject.syllabusNote.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = subject.syllabusNote,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        if (chapters.isEmpty()) {
                            Text(
                                text = s.noItemsMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            chapters.forEach { chapter ->
                                val completed = chapter.id in completedChapterIds
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (completed) Emerald100.copy(alpha = 0.45f) else Navy50,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "${chapter.chapterNumber}. ${chapter.toModel().displayTitle(lang)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = { onToggleChapterCompleted(chapter, completed) },
                                            modifier = Modifier.testTag("toggle_chapter_${chapter.id}")
                                        ) {
                                            Icon(
                                                imageVector = if (completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                                contentDescription = if (completed) s.chapterCompleted else s.markComplete,
                                                tint = if (completed) Emerald600 else Navy800
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
    }
}

@Composable
private fun PyqTab(
    state: BoardCatalogState,
    subjectMap: Map<String, com.example.data.local.SubjectEntity>,
    onOpenPdf: (String) -> Unit
) {
    val lang = state.preferences.language
    val s = AppStrings.get(lang)
    var searchQuery by remember { mutableStateOf("") }
    var selectedSubjectId by remember { mutableStateOf<String?>(null) }
    var selectedYear by remember { mutableStateOf<String?>(null) }

    val availableYears = remember(state.pyqs) {
        state.pyqs.map { it.examYear }.filter { it.isNotBlank() }.distinct().sortedDescending()
    }

    val filteredPyqs = remember(state.pyqs, searchQuery, selectedSubjectId, selectedYear) {
        val q = searchQuery.trim().lowercase()
        state.pyqs.filter { paper ->
            (selectedSubjectId == null || paper.subjectId == selectedSubjectId) &&
                (selectedYear == null || paper.examYear == selectedYear) &&
                (q.isEmpty() ||
                    paper.titleEn.lowercase().contains(q) ||
                    paper.titleOr.lowercase().contains(q) ||
                    paper.searchableText.lowercase().contains(q) ||
                    paper.examYear.lowercase().contains(q) ||
                    paper.questionType.lowercase().contains(q))
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            AcademicDisclaimerBanner(text = s.disclaimerBanner)
        }
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(s.searchPyqHint) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pyq_search_input")
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedSubjectId == null,
                        onClick = { selectedSubjectId = null },
                        label = { Text(s.allLabel) }
                    )
                }
                items(state.subjects, key = { it.id }) { subject ->
                    FilterChip(
                        selected = selectedSubjectId == subject.id,
                        onClick = { selectedSubjectId = subject.id },
                        label = { Text(subject.toModel().displayName(lang)) }
                    )
                }
            }
        }
        if (availableYears.isNotEmpty()) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedYear == null,
                            onClick = { selectedYear = null },
                            label = { Text("All Years") }
                        )
                    }
                    items(availableYears, key = { it }) { yr ->
                        FilterChip(
                            selected = selectedYear == yr,
                            onClick = { selectedYear = yr },
                            label = { Text(yr) }
                        )
                    }
                }
            }
        }

        if (filteredPyqs.isEmpty()) {
            item { EmptyStateCard(Icons.Default.Description, s.noItemsMessage) }
        } else {
            items(filteredPyqs, key = { it.id }) { paper ->
                val subjName = subjectMap[paper.subjectId]?.toModel()?.displayName(lang) ?: paper.subjectId
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ContentLabelBadge(paper.contentLabel, lang)
                            if (paper.examYear.isNotBlank()) {
                                StatusBadge(paper.examYear, Navy100, Navy800)
                            }
                            if (paper.questionType.isNotBlank()) {
                                StatusBadge(paper.questionType, Saffron100, Saffron600)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = paper.toModel().displayTitle(lang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = subjName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (paper.searchableText.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = paper.searchableText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (paper.pdfUri.isNotBlank()) {
                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = { onOpenPdf(paper.pdfUri) },
                                colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                                modifier = Modifier.testTag("open_pyq_pdf_${paper.id}")
                            ) {
                                Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(s.openPdfLabel)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotesTab(
    state: BoardCatalogState,
    subjectMap: Map<String, com.example.data.local.SubjectEntity>,
    onToggleNoteOffline: (String, Boolean) -> Unit,
    onToggleNoteBookmark: (String, BookmarkItemType, String, String, BookmarkEntity?, Boolean) -> Unit,
    onOpenPdf: (String) -> Unit
) {
    val lang = state.preferences.language
    val s = AppStrings.get(lang)
    var selectedSubjectId by remember { mutableStateOf<String?>(null) }
    var selectedNoteType by remember { mutableStateOf<String?>(null) }
    var expandedBilingualIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    val bookmarkByNoteId = remember(state.bookmarks) {
        state.bookmarks.filter { it.itemType == BookmarkItemType.NOTE.name }.associateBy { it.itemId }
    }

    val filteredNotes = remember(state.notes, selectedSubjectId, selectedNoteType) {
        state.notes.filter { note ->
            (selectedSubjectId == null || note.subjectId == selectedSubjectId) &&
                (selectedNoteType == null || note.noteType == selectedNoteType)
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedSubjectId == null,
                        onClick = { selectedSubjectId = null },
                        label = { Text(s.allLabel) }
                    )
                }
                items(state.subjects, key = { it.id }) { subject ->
                    FilterChip(
                        selected = selectedSubjectId == subject.id,
                        onClick = { selectedSubjectId = subject.id },
                        label = { Text(subject.toModel().displayName(lang)) }
                    )
                }
            }
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedNoteType == null,
                        onClick = { selectedNoteType = null },
                        label = { Text(s.allLabel) }
                    )
                }
                items(NoteType.entries, key = { it.name }) { nt ->
                    FilterChip(
                        selected = selectedNoteType == nt.name,
                        onClick = { selectedNoteType = nt.name },
                        label = { Text(if (lang == AppLanguage.OR) nt.labelOr else nt.labelEn) }
                    )
                }
            }
        }

        if (filteredNotes.isEmpty()) {
            item { EmptyStateCard(Icons.Default.MenuBook, s.noItemsMessage) }
        } else {
            items(filteredNotes, key = { it.id }) { note ->
                val nt = NoteType.fromRaw(note.noteType)
                val subjName = subjectMap[note.subjectId]?.toModel()?.displayName(lang) ?: ""
                val bm = bookmarkByNoteId[note.id]
                val showBothLang = note.id in expandedBilingualIds

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                StatusBadge(
                                    text = if (lang == AppLanguage.OR) nt.labelOr else nt.labelEn,
                                    containerColor = Navy100,
                                    contentColor = Navy800
                                )
                                if (note.savedOffline) {
                                    StatusBadge(
                                        text = s.savedOfflineLabel,
                                        containerColor = Emerald100,
                                        contentColor = Emerald600
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    onToggleNoteBookmark(
                                        note.id,
                                        BookmarkItemType.NOTE,
                                        note.boardId,
                                        note.subjectId,
                                        bm,
                                        false
                                    )
                                },
                                modifier = Modifier.testTag("bookmark_note_${note.id}")
                            ) {
                                Icon(
                                    imageVector = if (bm != null) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = s.bookmarkLabel,
                                    tint = if (bm != null) Saffron500 else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (subjName.isNotBlank()) {
                            Text(
                                text = subjName,
                                style = MaterialTheme.typography.labelMedium,
                                color = Saffron600,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = note.toModel().displayTitle(lang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = note.toModel().displayContent(lang),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (showBothLang) {
                            val altLang = if (lang == AppLanguage.EN) AppLanguage.OR else AppLanguage.EN
                            val altText = note.toModel().displayContent(altLang)
                            if (altText.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Navy50,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = altText,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { onToggleNoteOffline(note.id, note.savedOffline) },
                                modifier = Modifier.testTag("offline_note_${note.id}")
                            ) {
                                Icon(
                                    imageVector = if (note.savedOffline) Icons.Default.DownloadDone else Icons.Default.OfflinePin,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(if (note.savedOffline) s.savedOfflineLabel else s.saveOfflineLabel)
                            }
                            TextButton(
                                onClick = {
                                    expandedBilingualIds = if (showBothLang) {
                                        expandedBilingualIds - note.id
                                    } else {
                                        expandedBilingualIds + note.id
                                    }
                                }
                            ) {
                                Text(if (lang == AppLanguage.EN) "Read Odia" else "Read English")
                            }
                        }
                        if (note.pdfUri.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Button(
                                onClick = { onOpenPdf(note.pdfUri) },
                                colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                                modifier = Modifier.testTag("open_note_pdf_${note.id}")
                            ) {
                                Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(s.openPdfLabel)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun openPdfSafely(context: Context, uriString: String) {
    try {
        val uri = Uri.parse(uriString)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(
            context,
            "No PDF viewer available or attached file URI is inaccessible on this device.",
            Toast.LENGTH_SHORT
        ).show()
    }
}
