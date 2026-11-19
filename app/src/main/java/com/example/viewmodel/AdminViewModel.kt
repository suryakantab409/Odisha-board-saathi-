package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ChapterEntity
import com.example.data.local.ImportantQuestionEntity
import com.example.data.local.McqEntity
import com.example.data.local.MockExamEntity
import com.example.data.local.PyqPaperEntity
import com.example.data.local.StudyNoteEntity
import com.example.data.local.SubjectEntity
import com.example.data.remote.AdminStudentSummary
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AdminCatalogState(
    val subjects: List<SubjectEntity> = emptyList(),
    val chapters: List<ChapterEntity> = emptyList(),
    val importantQuestions: List<ImportantQuestionEntity> = emptyList(),
    val mcqs: List<McqEntity> = emptyList(),
    val mockExams: List<MockExamEntity> = emptyList(),
    val pyqs: List<PyqPaperEntity> = emptyList(),
    val notes: List<StudyNoteEntity> = emptyList()
)

class AdminViewModel(
    private val repository: StudyRepository
) : ViewModel() {

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _studentSummaries = MutableStateFlow<List<AdminStudentSummary>>(emptyList())
    val studentSummaries: StateFlow<List<AdminStudentSummary>> = _studentSummaries.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    val catalogState: StateFlow<AdminCatalogState> = combine(
        repository.observeAllSubjectsAdmin(),
        repository.observeAllChaptersAdmin(),
        repository.observeAllImportantQuestionsAdmin(),
        repository.observeAllMcqsAdmin()
    ) { subjects, chapters, imps, mcqs ->
        AdminCatalogState(
            subjects = subjects,
            chapters = chapters,
            importantQuestions = imps,
            mcqs = mcqs
        )
    }.combine(
        combine(
            repository.observeAllMockExamsAdmin(),
            repository.observeAllPyqsAdmin(),
            repository.observeAllNotesAdmin()
        ) { mocks, pyqs, notes ->
            Triple(mocks, pyqs, notes)
        }
    ) { base, extra ->
        base.copy(
            mockExams = extra.first,
            pyqs = extra.second,
            notes = extra.third
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = AdminCatalogState()
    )

    fun clearStatus() {
        _statusMessage.value = null
    }

    fun saveSubject(item: SubjectEntity) = runAdminAction("Subject saved") {
        repository.adminSaveSubject(item)
    }

    fun deleteSubject(id: String) = runAdminAction("Subject deleted") {
        repository.adminDeleteSubject(id)
    }

    fun saveChapter(item: ChapterEntity) = runAdminAction("Chapter saved") {
        repository.adminSaveChapter(item)
    }

    fun deleteChapter(id: String) = runAdminAction("Chapter deleted") {
        repository.adminDeleteChapter(id)
    }

    fun saveImportantQuestion(item: ImportantQuestionEntity) = runAdminAction("Important question saved") {
        repository.adminSaveImportantQuestion(item)
    }

    fun deleteImportantQuestion(id: String) = runAdminAction("Important question deleted") {
        repository.adminDeleteImportantQuestion(id)
    }

    fun saveMcq(item: McqEntity) = runAdminAction("MCQ saved") {
        repository.adminSaveMcq(item)
    }

    fun deleteMcq(id: String) = runAdminAction("MCQ deleted") {
        repository.adminDeleteMcq(id)
    }

    fun saveMockExam(item: MockExamEntity) = runAdminAction("Mock exam saved") {
        repository.adminSaveMockExam(item)
    }

    fun deleteMockExam(id: String) = runAdminAction("Mock exam deleted") {
        repository.adminDeleteMockExam(id)
    }

    fun savePyq(item: PyqPaperEntity) = runAdminAction("PYQ record saved") {
        repository.adminSavePyq(item)
    }

    fun deletePyq(id: String) = runAdminAction("PYQ record deleted") {
        repository.adminDeletePyq(id)
    }

    fun saveNote(item: StudyNoteEntity) = runAdminAction("Study note saved") {
        repository.adminSaveNote(item)
    }

    fun deleteNote(id: String) = runAdminAction("Study note deleted") {
        repository.adminDeleteNote(id)
    }

    fun importQuestions(rawText: String) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.adminImportQuestionsJsonOrCsv(rawText)
            _isBusy.value = false
            _statusMessage.value = res.fold(
                onSuccess = { count -> "Imported $count questions successfully" },
                onFailure = { "Import failed: ${it.localizedMessage}" }
            )
        }
    }

    fun loadStudentActivity() {
        viewModelScope.launch {
            _isBusy.value = true
            val res = repository.adminFetchStudentActivity()
            _isBusy.value = false
            res.onSuccess { _studentSummaries.value = it }
            res.onFailure { _statusMessage.value = "Could not load student activity: ${it.localizedMessage}" }
        }
    }

    suspend fun generateBackupJson(): String = repository.exportQuestionBankBackupJson()

    private fun runAdminAction(successMsg: String, block: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = block()
            _isBusy.value = false
            _statusMessage.value = res.fold(
                onSuccess = { successMsg },
                onFailure = { "Action failed: ${it.localizedMessage}" }
            )
        }
    }
}
