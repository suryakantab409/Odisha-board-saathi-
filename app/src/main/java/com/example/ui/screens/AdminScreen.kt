package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.ChapterEntity
import com.example.data.local.ImportantQuestionEntity
import com.example.data.local.McqEntity
import com.example.data.local.MockExamEntity
import com.example.data.local.PyqPaperEntity
import com.example.data.local.StudyNoteEntity
import com.example.data.local.SubjectEntity
import com.example.model.BoardType
import com.example.model.ContentLabel
import com.example.model.Difficulty
import com.example.model.NoteType
import com.example.model.QuestionCategory
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.Crimson600
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Navy100
import com.example.ui.theme.Navy800
import com.example.ui.theme.Saffron100
import com.example.ui.theme.Saffron500
import com.example.ui.theme.Saffron600
import com.example.viewmodel.AdminViewModel
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun AdminScreen(
    isAdmin: Boolean,
    adminViewModel: AdminViewModel,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    if (!isAdmin) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(24.dp)) {
            EmptyStateCard(
                icon = Icons.Default.Lock,
                message = "Admin authorization required. Sign in with an authorized admin Google account."
            )
        }
        return
    }

    val catalog by adminViewModel.catalogState.collectAsStateWithLifecycle()
    val statusMsg by adminViewModel.statusMessage.collectAsStateWithLifecycle()
    val isBusy by adminViewModel.isBusy.collectAsStateWithLifecycle()
    val students by adminViewModel.studentSummaries.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) }
    var boardFilter by remember { mutableStateOf(BoardType.BSE_10) }

    val tabNames = listOf(
        "Subjects", "Chapters", "Important Qs", "MCQs", "Mocks", "PYQs", "Notes", "Import/Export", "Students"
    )

    // Dialog states
    var editingSubject by remember { mutableStateOf<SubjectEntity?>(null) }
    var editingChapter by remember { mutableStateOf<ChapterEntity?>(null) }
    var editingImpQ by remember { mutableStateOf<ImportantQuestionEntity?>(null) }
    var editingMcq by remember { mutableStateOf<McqEntity?>(null) }
    var editingMock by remember { mutableStateOf<MockExamEntity?>(null) }
    var editingPyq by remember { mutableStateOf<PyqPaperEntity?>(null) }
    var editingNote by remember { mutableStateOf<StudyNoteEntity?>(null) }

    // SAF Launchers (Zero Storage Permission)
    var importText by remember { mutableStateOf("") }
    val importFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
                    importText = reader.readText()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Read error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val exportBackupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val json = adminViewModel.generateBackupJson()
                    context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(json) }
                    Toast.makeText(context, "Backup exported", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Export failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 8) adminViewModel.loadStudentActivity()
    }

    Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize()
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tabNames.indices.toList()) { idx ->
                    FilterChip(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        label = { Text(tabNames[idx]) },
                        modifier = Modifier.testTag("admin_tab_$idx")
                    )
                }
            }

            if (selectedTab < 7) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BoardType.entries.forEach { b ->
                            FilterChip(
                                selected = boardFilter == b,
                                onClick = { boardFilter = b },
                                label = { Text(b.titleEn) }
                            )
                        }
                    }
                    Button(
                        onClick = {
                            val defaultSubj = catalog.subjects.firstOrNull { it.boardId == boardFilter.id }?.id ?: ""
                            val defaultChap = catalog.chapters.firstOrNull { it.boardId == boardFilter.id }?.id ?: ""
                            when (selectedTab) {
                                0 -> editingSubject = SubjectEntity(
                                    id = newId("subj"), boardId = boardFilter.id, nameEn = "", nameOr = "",
                                    sortOrder = (catalog.subjects.size + 1).toLong(), published = true, syllabusNote = ""
                                )
                                1 -> editingChapter = ChapterEntity(
                                    id = newId("chap"), boardId = boardFilter.id, subjectId = defaultSubj,
                                    titleEn = "", titleOr = "", chapterNumber = 1L, published = true
                                )
                                2 -> editingImpQ = ImportantQuestionEntity(
                                    id = newId("imp"), boardId = boardFilter.id, subjectId = defaultSubj, chapterId = defaultChap,
                                    marks = 2L, category = QuestionCategory.BOARD_PRACTICE.name, difficulty = Difficulty.MEDIUM.name,
                                    contentLabel = ContentLabel.ADMIN_APPROVED.name, sourceYear = "",
                                    questionEn = "", questionOr = "", answerEn = "", answerOr = "", published = true
                                )
                                3 -> editingMcq = McqEntity(
                                    id = newId("mcq"), boardId = boardFilter.id, subjectId = defaultSubj, chapterId = defaultChap,
                                    questionEn = "", questionOr = "", optionsEn = listOf("", "", "", ""), optionsOr = listOf("", "", "", ""),
                                    correctIndex = 0L, explanationEn = "", explanationOr = "",
                                    difficulty = Difficulty.MEDIUM.name, contentLabel = ContentLabel.ADMIN_APPROVED.name,
                                    sourceYear = "", published = true
                                )
                                4 -> editingMock = MockExamEntity(
                                    id = newId("mock"), boardId = boardFilter.id, subjectId = defaultSubj,
                                    titleEn = "", titleOr = "", durationMinutes = 30L, totalMarks = 20L,
                                    questionIds = catalog.mcqs.filter { it.boardId == boardFilter.id }.take(10).map { it.id },
                                    patternNote = "Admin-configured mock pattern", published = true
                                )
                                5 -> editingPyq = PyqPaperEntity(
                                    id = newId("pyq"), boardId = boardFilter.id, subjectId = defaultSubj,
                                    examYear = "2024", questionType = "Annual Set", titleEn = "", titleOr = "",
                                    searchableText = "", pdfUri = "", contentLabel = ContentLabel.VERIFIED_PYQ.name, published = true
                                )
                                6 -> editingNote = StudyNoteEntity(
                                    id = newId("note"), boardId = boardFilter.id, subjectId = defaultSubj, chapterId = defaultChap,
                                    noteType = NoteType.CHAPTER_NOTES.name, titleEn = "", titleOr = "",
                                    contentEn = "", contentOr = "", pdfUri = "", published = true
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Saffron500),
                        modifier = Modifier.testTag("admin_add_button")
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add")
                    }
                }
            }

            if (!statusMsg.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Saffron100,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(statusMsg ?: "", style = MaterialTheme.typography.labelMedium, color = Saffron600)
                        TextButton(onClick = { adminViewModel.clearStatus() }) { Text("Dismiss") }
                    }
                }
            }

            if (isBusy) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                when (selectedTab) {
                    0 -> items(catalog.subjects.filter { it.boardId == boardFilter.id }, key = { it.id }) { s ->
                        AdminItemRow(
                            title = "${s.sortOrder}. ${s.nameEn}",
                            subtitle = s.nameOr.ifBlank { s.syllabusNote },
                            published = s.published,
                            onTogglePublish = { adminViewModel.saveSubject(s.copy(published = !s.published)) },
                            onEdit = { editingSubject = s },
                            onDelete = { adminViewModel.deleteSubject(s.id) }
                        )
                    }
                    1 -> items(catalog.chapters.filter { it.boardId == boardFilter.id }, key = { it.id }) { c ->
                        AdminItemRow(
                            title = "Ch ${c.chapterNumber}: ${c.titleEn}",
                            subtitle = "${c.subjectId} · ${c.titleOr}",
                            published = c.published,
                            onTogglePublish = { adminViewModel.saveChapter(c.copy(published = !c.published)) },
                            onEdit = { editingChapter = c },
                            onDelete = { adminViewModel.deleteChapter(c.id) }
                        )
                    }
                    2 -> items(catalog.importantQuestions.filter { it.boardId == boardFilter.id }, key = { it.id }) { q ->
                        AdminItemRow(
                            title = "[${q.marks}M] ${q.questionEn}",
                            subtitle = "${q.contentLabel} · ${q.difficulty}",
                            published = q.published,
                            onTogglePublish = { adminViewModel.saveImportantQuestion(q.copy(published = !q.published)) },
                            onEdit = { editingImpQ = q },
                            onDelete = { adminViewModel.deleteImportantQuestion(q.id) }
                        )
                    }
                    3 -> items(catalog.mcqs.filter { it.boardId == boardFilter.id }, key = { it.id }) { m ->
                        AdminItemRow(
                            title = m.questionEn,
                            subtitle = "${m.contentLabel} · Ans: ${'A' + m.correctIndex.toInt()}",
                            published = m.published,
                            onTogglePublish = { adminViewModel.saveMcq(m.copy(published = !m.published)) },
                            onEdit = { editingMcq = m },
                            onDelete = { adminViewModel.deleteMcq(m.id) }
                        )
                    }
                    4 -> items(catalog.mockExams.filter { it.boardId == boardFilter.id }, key = { it.id }) { mk ->
                        AdminItemRow(
                            title = mk.titleEn,
                            subtitle = "${mk.durationMinutes} min · ${mk.totalMarks} Marks · ${mk.questionIds.size} Qs",
                            published = mk.published,
                            onTogglePublish = { adminViewModel.saveMockExam(mk.copy(published = !mk.published)) },
                            onEdit = { editingMock = mk },
                            onDelete = { adminViewModel.deleteMockExam(mk.id) }
                        )
                    }
                    5 -> items(catalog.pyqs.filter { it.boardId == boardFilter.id }, key = { it.id }) { pyq ->
                        AdminItemRow(
                            title = "${pyq.examYear} · ${pyq.titleEn}",
                            subtitle = "${pyq.contentLabel} · PDF: ${if (pyq.pdfUri.isBlank()) "None" else "Attached"}",
                            published = pyq.published,
                            onTogglePublish = { adminViewModel.savePyq(pyq.copy(published = !pyq.published)) },
                            onEdit = { editingPyq = pyq },
                            onDelete = { adminViewModel.deletePyq(pyq.id) }
                        )
                    }
                    6 -> items(catalog.notes.filter { it.boardId == boardFilter.id }, key = { it.id }) { n ->
                        AdminItemRow(
                            title = n.titleEn,
                            subtitle = "${n.noteType} · PDF: ${if (n.pdfUri.isBlank()) "None" else "Attached"}",
                            published = n.published,
                            onTogglePublish = { adminViewModel.saveNote(n.copy(published = !n.published)) },
                            onEdit = { editingNote = n },
                            onDelete = { adminViewModel.deleteNote(n.id) }
                        )
                    }
                    7 -> item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Bulk Import MCQs (JSON or CSV)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "CSV columns: boardId,subjectId,chapterId,questionEn,questionOr,optA,optB,optC,optD,correctIndex,explanationEn,contentLabel",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = importText,
                                    onValueChange = { importText = it },
                                    label = { Text("Paste JSON or CSV") },
                                    minLines = 5,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { importFileLauncher.launch(arrayOf("application/json", "text/csv", "text/plain")) }) {
                                        Icon(Icons.Default.UploadFile, null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Load File")
                                    }
                                    Button(
                                        onClick = { adminViewModel.importQuestions(importText) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Navy800)
                                    ) {
                                        Text("Import Now")
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                                OutlinedButton(
                                    onClick = { exportBackupLauncher.launch("boardsaathi_backup.json") },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Download, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Export Question Bank Backup (JSON)")
                                }
                            }
                        }
                    }
                    8 -> {
                        if (students.isEmpty()) {
                            item { EmptyStateCard(Icons.Default.Edit, "No student activity records found in cloud yet.") }
                        } else {
                            items(students, key = { it.userId }) { st ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(st.displayName.ifBlank { st.userId }, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "${st.boardId} · Streak: ${st.streakDays}d · Recent attempts: ${st.recentAttempts.size}",
                                            style = MaterialTheme.typography.bodySmall
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

    // --- Edit Dialogs ---
    editingSubject?.let { item ->
        AdminSubjectDialog(
            initial = item,
            onDismiss = { editingSubject = null },
            onSave = {
                adminViewModel.saveSubject(it)
                editingSubject = null
            }
        )
    }
    editingChapter?.let { item ->
        AdminChapterDialog(
            initial = item,
            onDismiss = { editingChapter = null },
            onSave = {
                adminViewModel.saveChapter(it)
                editingChapter = null
            }
        )
    }
    editingImpQ?.let { item ->
        AdminImpQDialog(
            initial = item,
            onDismiss = { editingImpQ = null },
            onSave = {
                adminViewModel.saveImportantQuestion(it)
                editingImpQ = null
            }
        )
    }
    editingMcq?.let { item ->
        AdminMcqDialog(
            initial = item,
            onDismiss = { editingMcq = null },
            onSave = {
                adminViewModel.saveMcq(it)
                editingMcq = null
            }
        )
    }
    editingMock?.let { item ->
        AdminMockDialog(
            initial = item,
            onDismiss = { editingMock = null },
            onSave = {
                adminViewModel.saveMockExam(it)
                editingMock = null
            }
        )
    }
    editingPyq?.let { item ->
        AdminPyqDialog(
            initial = item,
            onDismiss = { editingPyq = null },
            onSave = {
                adminViewModel.savePyq(it)
                editingPyq = null
            }
        )
    }
    editingNote?.let { item ->
        AdminNoteDialog(
            initial = item,
            onDismiss = { editingNote = null },
            onSave = {
                adminViewModel.saveNote(it)
                editingNote = null
            }
        )
    }
}

@Composable
private fun AdminItemRow(
    title: String,
    subtitle: String,
    published: Boolean,
    onTogglePublish: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 2)
                if (subtitle.isNotBlank()) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
                Spacer(Modifier.height(4.dp))
                StatusBadge(
                    text = if (published) "Published" else "Draft",
                    containerColor = if (published) Emerald100 else Navy100,
                    contentColor = if (published) Emerald600 else Navy800
                )
            }
            IconButton(onClick = onTogglePublish) {
                Icon(if (published) Icons.Default.VisibilityOff else Icons.Default.Publish, "Toggle publish")
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete", tint = Crimson600)
            }
        }
    }
}

@Composable
private fun AdminSubjectDialog(initial: SubjectEntity, onDismiss: () -> Unit, onSave: (SubjectEntity) -> Unit) {
    var nameEn by remember { mutableStateOf(initial.nameEn) }
    var nameOr by remember { mutableStateOf(initial.nameOr) }
    var order by remember { mutableStateOf(initial.sortOrder.toString()) }
    var note by remember { mutableStateOf(initial.syllabusNote) }
    var published by remember { mutableStateOf(initial.published) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Subject (${initial.boardId})") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(nameEn, { nameEn = it }, label = { Text("Name (English)") }, singleLine = true)
                OutlinedTextField(nameOr, { nameOr = it }, label = { Text("Name (Odia)") }, singleLine = true)
                OutlinedTextField(order, { order = it }, label = { Text("Sort Order") }, singleLine = true)
                OutlinedTextField(note, { note = it }, label = { Text("Syllabus Note") })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Published", modifier = Modifier.weight(1f))
                    Switch(published, { published = it })
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(initial.copy(nameEn = nameEn, nameOr = nameOr, sortOrder = order.toLongOrNull() ?: 1L, syllabusNote = note, published = published))
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AdminChapterDialog(initial: ChapterEntity, onDismiss: () -> Unit, onSave: (ChapterEntity) -> Unit) {
    var subjectId by remember { mutableStateOf(initial.subjectId) }
    var titleEn by remember { mutableStateOf(initial.titleEn) }
    var titleOr by remember { mutableStateOf(initial.titleOr) }
    var num by remember { mutableStateOf(initial.chapterNumber.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chapter") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(subjectId, { subjectId = it }, label = { Text("Subject ID") }, singleLine = true)
                OutlinedTextField(titleEn, { titleEn = it }, label = { Text("Title (English)") }, singleLine = true)
                OutlinedTextField(titleOr, { titleOr = it }, label = { Text("Title (Odia)") }, singleLine = true)
                OutlinedTextField(num, { num = it }, label = { Text("Chapter Number") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(initial.copy(subjectId = subjectId, titleEn = titleEn, titleOr = titleOr, chapterNumber = num.toLongOrNull() ?: 1L))
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AdminImpQDialog(initial: ImportantQuestionEntity, onDismiss: () -> Unit, onSave: (ImportantQuestionEntity) -> Unit) {
    var marks by remember { mutableStateOf(initial.marks.toString()) }
    var qEn by remember { mutableStateOf(initial.questionEn) }
    var qOr by remember { mutableStateOf(initial.questionOr) }
    var aEn by remember { mutableStateOf(initial.answerEn) }
    var aOr by remember { mutableStateOf(initial.answerOr) }
    var year by remember { mutableStateOf(initial.sourceYear) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Important Question") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(marks, { marks = it }, label = { Text("Marks (1,2,3,5)") }, singleLine = true)
                OutlinedTextField(qEn, { qEn = it }, label = { Text("Question (EN)") })
                OutlinedTextField(qOr, { qOr = it }, label = { Text("Question (OR)") })
                OutlinedTextField(aEn, { aEn = it }, label = { Text("Answer (EN)") })
                OutlinedTextField(aOr, { aOr = it }, label = { Text("Answer (OR)") })
                OutlinedTextField(year, { year = it }, label = { Text("Source / Year (if verified)") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(initial.copy(marks = marks.toLongOrNull() ?: 2L, questionEn = qEn, questionOr = qOr, answerEn = aEn, answerOr = aOr, sourceYear = year))
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AdminMcqDialog(initial: McqEntity, onDismiss: () -> Unit, onSave: (McqEntity) -> Unit) {
    var qEn by remember { mutableStateOf(initial.questionEn) }
    var qOr by remember { mutableStateOf(initial.questionOr) }
    var optA by remember { mutableStateOf(initial.optionsEn.getOrElse(0) { "" }) }
    var optB by remember { mutableStateOf(initial.optionsEn.getOrElse(1) { "" }) }
    var optC by remember { mutableStateOf(initial.optionsEn.getOrElse(2) { "" }) }
    var optD by remember { mutableStateOf(initial.optionsEn.getOrElse(3) { "" }) }
    var correctIdx by remember { mutableStateOf(initial.correctIndex.toString()) }
    var expEn by remember { mutableStateOf(initial.explanationEn) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("MCQ Editor") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(qEn, { qEn = it }, label = { Text("Question (EN)") })
                OutlinedTextField(qOr, { qOr = it }, label = { Text("Question (OR)") })
                OutlinedTextField(optA, { optA = it }, label = { Text("Option A") }, singleLine = true)
                OutlinedTextField(optB, { optB = it }, label = { Text("Option B") }, singleLine = true)
                OutlinedTextField(optC, { optC = it }, label = { Text("Option C") }, singleLine = true)
                OutlinedTextField(optD, { optD = it }, label = { Text("Option D") }, singleLine = true)
                OutlinedTextField(correctIdx, { correctIdx = it }, label = { Text("Correct Index (0-3)") }, singleLine = true)
                OutlinedTextField(expEn, { expEn = it }, label = { Text("Explanation (EN)") })
            }
        },
        confirmButton = {
            Button(onClick = {
                val opts = listOf(optA, optB, optC, optD)
                onSave(
                    initial.copy(
                        questionEn = qEn,
                        questionOr = qOr.ifBlank { qEn },
                        optionsEn = opts,
                        optionsOr = if (initial.optionsOr.size == 4) initial.optionsOr else opts,
                        correctIndex = (correctIdx.toLongOrNull() ?: 0L).coerceIn(0L, 3L),
                        explanationEn = expEn
                    )
                )
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AdminMockDialog(initial: MockExamEntity, onDismiss: () -> Unit, onSave: (MockExamEntity) -> Unit) {
    var titleEn by remember { mutableStateOf(initial.titleEn) }
    var duration by remember { mutableStateOf(initial.durationMinutes.toString()) }
    var marks by remember { mutableStateOf(initial.totalMarks.toString()) }
    var patternNote by remember { mutableStateOf(initial.patternNote) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mock Exam Pattern") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(titleEn, { titleEn = it }, label = { Text("Exam Title") }, singleLine = true)
                OutlinedTextField(duration, { duration = it }, label = { Text("Duration (Minutes)") }, singleLine = true)
                OutlinedTextField(marks, { marks = it }, label = { Text("Total Marks") }, singleLine = true)
                OutlinedTextField(patternNote, { patternNote = it }, label = { Text("Pattern Description") })
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(
                    initial.copy(
                        titleEn = titleEn,
                        titleOr = initial.titleOr.ifBlank { titleEn },
                        durationMinutes = duration.toLongOrNull() ?: 30L,
                        totalMarks = marks.toLongOrNull() ?: 20L,
                        patternNote = patternNote
                    )
                )
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AdminPyqDialog(initial: PyqPaperEntity, onDismiss: () -> Unit, onSave: (PyqPaperEntity) -> Unit) {
    val context = LocalContext.current
    var year by remember { mutableStateOf(initial.examYear) }
    var titleEn by remember { mutableStateOf(initial.titleEn) }
    var type by remember { mutableStateOf(initial.questionType) }
    var text by remember { mutableStateOf(initial.searchableText) }
    var pdfUri by remember { mutableStateOf(initial.pdfUri) }
    val pdfPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) { }
            pdfUri = uri.toString()
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("PYQ Paper") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(year, { year = it }, label = { Text("Exam Year") }, singleLine = true)
                OutlinedTextField(titleEn, { titleEn = it }, label = { Text("Title (EN)") }, singleLine = true)
                OutlinedTextField(type, { type = it }, label = { Text("Question Type") }, singleLine = true)
                OutlinedTextField(text, { text = it }, label = { Text("Searchable Summary Text") })
                OutlinedButton(onClick = { pdfPicker.launch(arrayOf("application/pdf")) }) {
                    Text(if (pdfUri.isBlank()) "Attach PDF" else "PDF Attached ✓")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(initial.copy(examYear = year, titleEn = titleEn, titleOr = initial.titleOr.ifBlank { titleEn }, questionType = type, searchableText = text, pdfUri = pdfUri))
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AdminNoteDialog(initial: StudyNoteEntity, onDismiss: () -> Unit, onSave: (StudyNoteEntity) -> Unit) {
    val context = LocalContext.current
    var titleEn by remember { mutableStateOf(initial.titleEn) }
    var contentEn by remember { mutableStateOf(initial.contentEn) }
    var contentOr by remember { mutableStateOf(initial.contentOr) }
    var pdfUri by remember { mutableStateOf(initial.pdfUri) }
    val pdfPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) { }
            pdfUri = uri.toString()
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Study Note") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(titleEn, { titleEn = it }, label = { Text("Title (EN)") }, singleLine = true)
                OutlinedTextField(contentEn, { contentEn = it }, label = { Text("Content (EN)") })
                OutlinedTextField(contentOr, { contentOr = it }, label = { Text("Content (OR)") })
                OutlinedButton(onClick = { pdfPicker.launch(arrayOf("application/pdf")) }) {
                    Text(if (pdfUri.isBlank()) "Attach PDF" else "PDF Attached ✓")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(initial.copy(titleEn = titleEn, titleOr = initial.titleOr.ifBlank { titleEn }, contentEn = contentEn, contentOr = contentOr, pdfUri = pdfUri))
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun newId(prefix: String): String =
    "${prefix}_${UUID.randomUUID().toString().replace("-", "").take(10)}"
