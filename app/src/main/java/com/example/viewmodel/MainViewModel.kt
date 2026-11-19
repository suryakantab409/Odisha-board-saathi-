package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BookmarkEntity
import com.example.data.local.ChapterEntity
import com.example.data.local.ChapterProgressEntity
import com.example.data.local.ImportantQuestionEntity
import com.example.data.local.McqEntity
import com.example.data.local.MockExamEntity
import com.example.data.local.PyqPaperEntity
import com.example.data.local.QuizAttemptEntity
import com.example.data.local.SavedQuizSessionEntity
import com.example.data.local.StudentPreferences
import com.example.data.local.StudyNoteEntity
import com.example.data.local.SubjectEntity
import com.example.data.repository.StudyRepository
import com.example.model.AppLanguage
import com.example.model.AttemptMode
import com.example.model.BoardType
import com.example.model.BookmarkItemType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BoardCatalogState(
    val preferences: StudentPreferences = StudentPreferences(),
    val subjects: List<SubjectEntity> = emptyList(),
    val chapters: List<ChapterEntity> = emptyList(),
    val importantQuestions: List<ImportantQuestionEntity> = emptyList(),
    val mcqs: List<McqEntity> = emptyList(),
    val mockExams: List<MockExamEntity> = emptyList(),
    val pyqs: List<PyqPaperEntity> = emptyList(),
    val notes: List<StudyNoteEntity> = emptyList(),
    val bookmarks: List<BookmarkEntity> = emptyList(),
    val chapterProgress: List<ChapterProgressEntity> = emptyList(),
    val attempts: List<QuizAttemptEntity> = emptyList(),
    val savedQuizSession: SavedQuizSessionEntity? = null
)

data class ActiveQuizState(
    val active: Boolean = false,
    val title: String = "",
    val mode: AttemptMode = AttemptMode.QUIZ,
    val subjectId: String = "",
    val questions: List<McqEntity> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswers: Map<Int, Int> = emptyMap(),
    val markedForReview: Set<Int> = emptySet(),
    val totalSeconds: Long = 600L,
    val remainingSeconds: Long = 600L,
    val submitted: Boolean = false
)

class MainViewModel(
    private val repository: StudyRepository
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _quizState = MutableStateFlow(ActiveQuizState())
    val quizState: StateFlow<ActiveQuizState> = _quizState.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val catalogState: StateFlow<BoardCatalogState> = repository.preferencesFlow
        .map { it.board.id }
        .distinctUntilChanged()
        .flatMapLatest { boardId ->
            val flowPart1 = combine(
                repository.preferencesFlow,
                repository.observeSubjects(boardId),
                repository.observeChapters(boardId),
                repository.observeImportantQuestions(boardId),
                repository.observeMcqs(boardId)
            ) { prefs, subjects, chapters, imps, mcqs ->
                BoardCatalogState(
                    preferences = prefs,
                    subjects = subjects,
                    chapters = chapters,
                    importantQuestions = imps,
                    mcqs = mcqs
                )
            }
            val flowPart2 = combine(
                repository.observeMockExams(boardId),
                repository.observePyqs(boardId),
                repository.observeNotes(boardId),
                repository.observeBookmarks(boardId),
                repository.observeChapterProgress(boardId)
            ) { mocks, pyqs, notes, bms, progress ->
                PartialB(mocks, pyqs, notes, bms, progress)
            }
            val flowPart3 = combine(
                repository.observeAttempts(boardId),
                repository.observeSavedQuizSession(boardId)
            ) { attempts, saved ->
                attempts to saved
            }
            combine(flowPart1, flowPart2, flowPart3) { part1, part2, part3 ->
                part1.copy(
                    mockExams = part2.mocks,
                    pyqs = part2.pyqs,
                    notes = part2.notes,
                    bookmarks = part2.bookmarks,
                    chapterProgress = part2.progress,
                    attempts = part3.first,
                    savedQuizSession = part3.second
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = BoardCatalogState()
        )

    private data class PartialB(
        val mocks: List<MockExamEntity>,
        val pyqs: List<PyqPaperEntity>,
        val notes: List<StudyNoteEntity>,
        val bookmarks: List<BookmarkEntity>,
        val progress: List<ChapterProgressEntity>
    )

    fun refreshAdminAndSync() {
        viewModelScope.launch {
            _isAdmin.value = repository.isCurrentUserAdmin()
            syncNow()
        }
    }

    fun onSignedOut() {
        _isAdmin.value = false
        _syncMessage.value = null
    }

    fun syncNow() {
        if (_isSyncing.value) return
        viewModelScope.launch {
            _isSyncing.value = true
            _isAdmin.value = repository.isCurrentUserAdmin()
            val res = repository.syncWithCloud()
            _isSyncing.value = false
            _syncMessage.value = res.fold(
                onSuccess = { it },
                onFailure = { it.localizedMessage ?: "Sync unavailable offline" }
            )
        }
    }

    fun clearSyncMessage() {
        _syncMessage.value = null
    }

    fun completeOnboarding(
        board: BoardType,
        language: AppLanguage,
        name: String,
        daysUntilExam: Long,
        dailyTarget: Int
    ) {
        viewModelScope.launch {
            repository.completeOnboarding(board, language, name, daysUntilExam, dailyTarget)
        }
    }

    fun switchBoard(board: BoardType) {
        viewModelScope.launch { repository.updateBoard(board) }
    }

    fun switchLanguage(language: AppLanguage) {
        viewModelScope.launch { repository.updateLanguage(language) }
    }

    fun updateExamCountdown(days: Long) {
        viewModelScope.launch { repository.updateExamCountdownDays(days) }
    }

    fun updateDailyTarget(target: Int) {
        viewModelScope.launch { repository.updateDailyTarget(target) }
    }

    fun toggleBookmark(
        itemId: String,
        itemType: BookmarkItemType,
        boardId: String,
        subjectId: String,
        existingBookmark: BookmarkEntity?,
        toggleRevisionOnly: Boolean = false
    ) {
        viewModelScope.launch {
            repository.toggleBookmark(itemId, itemType, boardId, subjectId, existingBookmark, toggleRevisionOnly)
        }
    }

    fun toggleChapterCompleted(chapter: ChapterEntity, currentlyCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleChapterCompleted(chapter, currentlyCompleted)
        }
    }

    fun toggleNoteOffline(noteId: String, currentlySaved: Boolean) {
        viewModelScope.launch {
            repository.setNoteOfflineSaved(noteId, !currentlySaved)
        }
    }

    fun recordMcqPracticeAttempt(
        boardId: String,
        subjectId: String,
        title: String,
        totalQuestions: Int,
        correctCount: Int,
        wrongMcqIds: List<String>
    ) {
        viewModelScope.launch {
            repository.saveCompletedAttempt(
                boardId = boardId,
                subjectId = subjectId,
                mode = AttemptMode.MCQ_PRACTICE,
                title = title,
                totalQuestions = totalQuestions,
                correctCount = correctCount,
                wrongCount = (totalQuestions - correctCount).coerceAtLeast(0),
                scoreMarks = correctCount,
                maxMarks = totalQuestions.coerceAtLeast(1),
                durationSeconds = 0L,
                wrongMcqIds = wrongMcqIds
            )
        }
    }

    // --- Interactive Quiz & Mock Exam Engine ---

    fun startQuizSession(
        title: String,
        mode: AttemptMode,
        subjectId: String,
        questions: List<McqEntity>,
        durationMinutes: Int
    ) {
        if (questions.isEmpty()) return
        timerJob?.cancel()
        val totalSec = durationMinutes.coerceAtLeast(1) * 60L
        _quizState.value = ActiveQuizState(
            active = true,
            title = title,
            mode = mode,
            subjectId = subjectId,
            questions = questions,
            currentIndex = 0,
            selectedAnswers = emptyMap(),
            markedForReview = emptySet(),
            totalSeconds = totalSec,
            remainingSeconds = totalSec,
            submitted = false
        )
        startCountdownTimer()
    }

    fun resumeSavedQuiz(saved: SavedQuizSessionEntity, allMcqs: List<McqEntity>) {
        val mcqMap = allMcqs.associateBy { it.id }
        val resolvedQuestions = saved.questionIds.mapNotNull { mcqMap[it] }
        if (resolvedQuestions.isEmpty()) return
        val restoredAnswers = saved.selectedAnswers.mapIndexedNotNull { idx, v ->
            v.toIntOrNull()?.takeIf { it >= 0 }?.let { idx to it }
        }.toMap()
        val restoredMarked = saved.markedForReview.mapNotNull { it.toIntOrNull() }.toSet()

        timerJob?.cancel()
        _quizState.value = ActiveQuizState(
            active = true,
            title = saved.title,
            mode = AttemptMode.fromRaw(saved.mode),
            subjectId = saved.subjectId,
            questions = resolvedQuestions,
            currentIndex = saved.currentIndex.coerceIn(0, resolvedQuestions.lastIndex),
            selectedAnswers = restoredAnswers,
            markedForReview = restoredMarked,
            totalSeconds = saved.remainingSeconds.coerceAtLeast(60L),
            remainingSeconds = saved.remainingSeconds.coerceAtLeast(10L),
            submitted = false
        )
        startCountdownTimer()
    }

    private fun startCountdownTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val current = _quizState.value
                if (!current.active || current.submitted) break
                val next = current.remainingSeconds - 1L
                if (next <= 0L) {
                    _quizState.update { it.copy(remainingSeconds = 0L) }
                    submitQuiz()
                    break
                } else {
                    _quizState.update { it.copy(remainingSeconds = next) }
                }
            }
        }
    }

    fun selectQuizAnswer(questionIndex: Int, optionIndex: Int) {
        _quizState.update { state ->
            if (state.submitted) state
            else state.copy(selectedAnswers = state.selectedAnswers + (questionIndex to optionIndex))
        }
    }

    fun toggleMarkForReview(questionIndex: Int) {
        _quizState.update { state ->
            val updated = if (questionIndex in state.markedForReview) {
                state.markedForReview - questionIndex
            } else {
                state.markedForReview + questionIndex
            }
            state.copy(markedForReview = updated)
        }
    }

    fun jumpToQuizQuestion(index: Int) {
        _quizState.update { state ->
            if (index in state.questions.indices) state.copy(currentIndex = index) else state
        }
    }

    fun pauseAndSaveQuiz(boardId: String) {
        val state = _quizState.value
        if (!state.active || state.submitted) return
        timerJob?.cancel()
        viewModelScope.launch {
            val selectedList = state.questions.indices.map { idx ->
                (state.selectedAnswers[idx] ?: -1).toString()
            }
            val session = SavedQuizSessionEntity(
                boardId = boardId,
                title = state.title,
                mode = state.mode.name,
                subjectId = state.subjectId,
                questionIds = state.questions.map { it.id },
                selectedAnswers = selectedList,
                markedForReview = state.markedForReview.map { it.toString() },
                currentIndex = state.currentIndex,
                remainingSeconds = state.remainingSeconds,
                updatedAtMillis = System.currentTimeMillis()
            )
            repository.saveQuizSessionForLater(session)
            _quizState.value = ActiveQuizState()
        }
    }

    fun submitQuiz() {
        val state = _quizState.value
        if (!state.active || state.submitted) return
        timerJob?.cancel()
        val boardId = state.questions.firstOrNull()?.boardId ?: catalogState.value.preferences.board.id
        var correct = 0
        val wrongIds = mutableListOf<String>()
        state.questions.forEachIndexed { idx, mcq ->
            val chosen = state.selectedAnswers[idx]
            if (chosen != null && chosen.toLong() == mcq.correctIndex) {
                correct++
            } else {
                wrongIds.add(mcq.id)
            }
        }
        val total = state.questions.size
        val wrong = total - correct
        val elapsed = (state.totalSeconds - state.remainingSeconds).coerceAtLeast(1L)

        _quizState.update { it.copy(submitted = true) }

        viewModelScope.launch {
            repository.saveCompletedAttempt(
                boardId = boardId,
                subjectId = state.subjectId,
                mode = state.mode,
                title = state.title,
                totalQuestions = total,
                correctCount = correct,
                wrongCount = wrong,
                scoreMarks = correct,
                maxMarks = total.coerceAtLeast(1),
                durationSeconds = elapsed,
                wrongMcqIds = wrongIds
            )
            repository.clearSavedQuizSession(boardId)
        }
    }

    fun exitQuizSession() {
        timerJob?.cancel()
        _quizState.value = ActiveQuizState()
    }
}
