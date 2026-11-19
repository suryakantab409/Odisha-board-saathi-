package com.example.data.repository

import com.example.data.local.BoardSaathiDao
import com.example.data.local.BookmarkEntity
import com.example.data.local.ChapterEntity
import com.example.data.local.ChapterProgressEntity
import com.example.data.local.ImportantQuestionEntity
import com.example.data.local.McqEntity
import com.example.data.local.MockExamEntity
import com.example.data.local.PyqPaperEntity
import com.example.data.local.QuizAttemptEntity
import com.example.data.local.SavedQuizSessionEntity
import com.example.data.local.SeedDataProvider
import com.example.data.local.StudentPreferences
import com.example.data.local.StudyNoteEntity
import com.example.data.local.SubjectEntity
import com.example.data.local.UserPreferencesDataStore
import com.example.data.remote.AdminStudentSummary
import com.example.data.remote.FirestoreSyncRepository
import com.example.model.AppLanguage
import com.example.model.AttemptMode
import com.example.model.AttemptStatus
import com.example.model.BoardType
import com.example.model.BookmarkItemType
import com.example.model.ContentLabel
import com.example.model.Difficulty
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class StudyRepository(
    private val dao: BoardSaathiDao,
    private val prefsStore: UserPreferencesDataStore,
    private val remote: FirestoreSyncRepository,
    private val auth: FirebaseAuth
) {
    val preferencesFlow: Flow<StudentPreferences> = prefsStore.preferencesFlow

    suspend fun ensureSeeded() {
        if (dao.countSubjects() == 0) {
            dao.upsertSubjects(SeedDataProvider.starterSubjects)
            dao.upsertChapters(SeedDataProvider.starterChapters)
            dao.upsertImportantQuestions(SeedDataProvider.starterImportantQuestions)
            dao.upsertMcqs(SeedDataProvider.starterMcqs)
            dao.upsertMockExams(SeedDataProvider.starterMockExams)
            dao.upsertPyqs(SeedDataProvider.starterPyqs)
            dao.upsertNotes(SeedDataProvider.starterNotes)
        }
    }

    fun observeSubjects(boardId: String): Flow<List<SubjectEntity>> = dao.observePublishedSubjects(boardId)
    fun observeChapters(boardId: String): Flow<List<ChapterEntity>> = dao.observePublishedChapters(boardId)
    fun observeImportantQuestions(boardId: String): Flow<List<ImportantQuestionEntity>> =
        dao.observePublishedImportantQuestions(boardId)

    fun observeMcqs(boardId: String): Flow<List<McqEntity>> = dao.observePublishedMcqs(boardId)
    fun observeMockExams(boardId: String): Flow<List<MockExamEntity>> = dao.observePublishedMockExams(boardId)
    fun observePyqs(boardId: String): Flow<List<PyqPaperEntity>> = dao.observePublishedPyqs(boardId)
    fun observeNotes(boardId: String): Flow<List<StudyNoteEntity>> = dao.observePublishedNotes(boardId)
    fun observeBookmarks(boardId: String): Flow<List<BookmarkEntity>> = dao.observeBookmarks(boardId)
    fun observeChapterProgress(boardId: String): Flow<List<ChapterProgressEntity>> =
        dao.observeChapterProgress(boardId)

    fun observeAttempts(boardId: String): Flow<List<QuizAttemptEntity>> = dao.observeQuizAttempts(boardId)
    fun observeSavedQuizSession(boardId: String): Flow<SavedQuizSessionEntity?> =
        dao.observeSavedQuizSession(boardId)

    // Admin streams
    fun observeAllSubjectsAdmin(): Flow<List<SubjectEntity>> = dao.observeAllSubjectsAdmin()
    fun observeAllChaptersAdmin(): Flow<List<ChapterEntity>> = dao.observeAllChaptersAdmin()
    fun observeAllImportantQuestionsAdmin(): Flow<List<ImportantQuestionEntity>> =
        dao.observeAllImportantQuestionsAdmin()

    fun observeAllMcqsAdmin(): Flow<List<McqEntity>> = dao.observeAllMcqsAdmin()
    fun observeAllMockExamsAdmin(): Flow<List<MockExamEntity>> = dao.observeAllMockExamsAdmin()
    fun observeAllPyqsAdmin(): Flow<List<PyqPaperEntity>> = dao.observeAllPyqsAdmin()
    fun observeAllNotesAdmin(): Flow<List<StudyNoteEntity>> = dao.observeAllNotesAdmin()

    suspend fun completeOnboarding(
        board: BoardType,
        language: AppLanguage,
        name: String,
        daysUntilExam: Long,
        dailyTarget: Int
    ) {
        prefsStore.completeOnboarding(board, language, name, daysUntilExam, dailyTarget)
        if (auth.currentUser != null) {
            val prefs = prefsStore.preferencesFlow.first()
            remote.syncUserProfileUp(prefs)
        }
    }

    suspend fun updateBoard(board: BoardType) {
        prefsStore.setBoard(board)
        if (auth.currentUser != null) {
            remote.syncUserProfileUp(prefsStore.preferencesFlow.first())
        }
    }

    suspend fun updateLanguage(language: AppLanguage) {
        prefsStore.setLanguage(language)
        if (auth.currentUser != null) {
            remote.syncUserProfileUp(prefsStore.preferencesFlow.first())
        }
    }

    suspend fun updateExamCountdownDays(days: Long) {
        prefsStore.setExamDaysFromNow(days)
        if (auth.currentUser != null) {
            remote.syncUserProfileUp(prefsStore.preferencesFlow.first())
        }
    }

    suspend fun updateDailyTarget(target: Int) {
        prefsStore.setDailyTarget(target)
        if (auth.currentUser != null) {
            remote.syncUserProfileUp(prefsStore.preferencesFlow.first())
        }
    }

    suspend fun toggleBookmark(
        itemId: String,
        itemType: BookmarkItemType,
        boardId: String,
        subjectId: String,
        existingBookmark: BookmarkEntity?,
        toggleRevisionList: Boolean = false
    ) {
        val bmId = "${itemType.name}_${itemId}"
        if (existingBookmark == null) {
            val entity = BookmarkEntity(
                id = bmId,
                itemId = itemId,
                itemType = itemType.name,
                boardId = boardId,
                subjectId = subjectId,
                inRevisionList = toggleRevisionList,
                updatedAtMillis = System.currentTimeMillis()
            )
            dao.upsertBookmark(entity)
            if (auth.currentUser != null) remote.upsertBookmark(entity)
        } else if (toggleRevisionList) {
            val updated = existingBookmark.copy(
                inRevisionList = !existingBookmark.inRevisionList,
                updatedAtMillis = System.currentTimeMillis()
            )
            dao.upsertBookmark(updated)
            if (auth.currentUser != null) remote.upsertBookmark(updated)
        } else {
            dao.deleteBookmark(existingBookmark.id)
            if (auth.currentUser != null) remote.deleteBookmark(existingBookmark.id)
        }
    }

    suspend fun toggleChapterCompleted(chapter: ChapterEntity, currentlyCompleted: Boolean) {
        val entity = ChapterProgressEntity(
            chapterId = chapter.id,
            boardId = chapter.boardId,
            subjectId = chapter.subjectId,
            completed = !currentlyCompleted,
            updatedAtMillis = System.currentTimeMillis()
        )
        dao.upsertChapterProgress(entity)
        if (auth.currentUser != null) remote.upsertChapterProgress(entity)
    }

    suspend fun setNoteOfflineSaved(noteId: String, saved: Boolean) {
        dao.setNoteOfflineSaved(noteId, saved)
    }

    suspend fun recordQuestionsSolved(count: Int) {
        prefsStore.recordQuestionsSolved(count)
        if (auth.currentUser != null) {
            remote.syncUserProfileUp(prefsStore.preferencesFlow.first())
        }
    }

    suspend fun saveCompletedAttempt(
        boardId: String,
        subjectId: String,
        mode: AttemptMode,
        title: String,
        totalQuestions: Int,
        correctCount: Int,
        wrongCount: Int,
        scoreMarks: Int,
        maxMarks: Int,
        durationSeconds: Long,
        wrongMcqIds: List<String>
    ) {
        val attempt = QuizAttemptEntity(
            id = "att_${UUID.randomUUID().toString().replace("-", "").take(16)}",
            boardId = boardId,
            subjectId = subjectId,
            mode = mode.name,
            title = title,
            totalQuestions = totalQuestions.toLong(),
            correctCount = correctCount.toLong(),
            wrongCount = wrongCount.toLong(),
            scoreMarks = scoreMarks.toLong(),
            maxMarks = maxMarks.toLong(),
            durationSeconds = durationSeconds,
            status = AttemptStatus.COMPLETED.name,
            wrongMcqIds = wrongMcqIds,
            createdAtMillis = System.currentTimeMillis()
        )
        dao.upsertQuizAttempt(attempt)
        prefsStore.recordQuestionsSolved(totalQuestions)
        if (auth.currentUser != null) {
            remote.uploadQuizAttempt(attempt)
            remote.syncUserProfileUp(prefsStore.preferencesFlow.first())
        }
    }

    suspend fun saveQuizSessionForLater(session: SavedQuizSessionEntity) {
        dao.upsertSavedQuizSession(session)
    }

    suspend fun clearSavedQuizSession(boardId: String) {
        dao.clearSavedQuizSession(boardId)
    }

    suspend fun syncWithCloud(): Result<String> {
        if (auth.currentUser == null) return Result.failure(IllegalStateException("Not signed in"))
        val isAdmin = remote.checkIsAdmin()
        val prefs = prefsStore.preferencesFlow.first()

        // Pull or push user profile
        val profileRes = remote.fetchUserProfile()
        val remoteProfile = profileRes.getOrNull()
        if (remoteProfile != null && !prefs.onboarded) {
            prefsStore.applyRemoteProfile(
                displayName = remoteProfile.displayName,
                board = BoardType.fromId(remoteProfile.boardId),
                language = AppLanguage.fromCode(remoteProfile.language),
                examDateEpochDays = remoteProfile.examDateEpochDays,
                dailyTarget = remoteProfile.dailyTargetQuestions.toInt(),
                streakDays = remoteProfile.streakDays.toInt(),
                lastStudyEpochDay = remoteProfile.lastStudyEpochDay
            )
        } else {
            remote.syncUserProfileUp(prefs)
        }

        // Pull user bookmarks, chapter progress, attempts
        remote.fetchUserBookmarks().getOrNull()?.forEach { dao.upsertBookmark(it) }
        remote.fetchUserChapterProgress().getOrNull()?.forEach { dao.upsertChapterProgress(it) }
        remote.fetchUserAttempts().getOrNull()?.forEach { dao.upsertQuizAttempt(it) }

        // Pull published catalog updates from Firestore and merge into local Room cache
        val catalogRes = remote.fetchPublishedCatalog(isAdmin)
        catalogRes.onSuccess { cat ->
            if (cat.subjects.isNotEmpty()) dao.upsertSubjects(cat.subjects)
            if (cat.chapters.isNotEmpty()) dao.upsertChapters(cat.chapters)
            if (cat.importantQuestions.isNotEmpty()) dao.upsertImportantQuestions(cat.importantQuestions)
            if (cat.mcqs.isNotEmpty()) dao.upsertMcqs(cat.mcqs)
            if (cat.mockExams.isNotEmpty()) dao.upsertMockExams(cat.mockExams)
            if (cat.pyqPapers.isNotEmpty()) dao.upsertPyqs(cat.pyqPapers)
            if (cat.studyNotes.isNotEmpty()) dao.upsertNotes(cat.studyNotes)
        }

        return if (catalogRes.isSuccess) {
            Result.success("Cloud sync completed")
        } else {
            Result.failure(catalogRes.exceptionOrNull() ?: Exception("Sync failed"))
        }
    }

    // --- Admin Actions ---
    suspend fun isCurrentUserAdmin(): Boolean = remote.checkIsAdmin()

    suspend fun adminSaveSubject(item: SubjectEntity): Result<Unit> {
        dao.upsertSubjects(listOf(item))
        return if (auth.currentUser != null) remote.adminUpsertSubject(item) else Result.success(Unit)
    }

    suspend fun adminDeleteSubject(id: String): Result<Unit> {
        dao.deleteSubject(id)
        return if (auth.currentUser != null) remote.adminDeleteDoc("subjects", id) else Result.success(Unit)
    }

    suspend fun adminSaveChapter(item: ChapterEntity): Result<Unit> {
        dao.upsertChapters(listOf(item))
        return if (auth.currentUser != null) remote.adminUpsertChapter(item) else Result.success(Unit)
    }

    suspend fun adminDeleteChapter(id: String): Result<Unit> {
        dao.deleteChapter(id)
        return if (auth.currentUser != null) remote.adminDeleteDoc("chapters", id) else Result.success(Unit)
    }

    suspend fun adminSaveImportantQuestion(item: ImportantQuestionEntity): Result<Unit> {
        dao.upsertImportantQuestions(listOf(item))
        return if (auth.currentUser != null) remote.adminUpsertImportantQuestion(item) else Result.success(Unit)
    }

    suspend fun adminDeleteImportantQuestion(id: String): Result<Unit> {
        dao.deleteImportantQuestion(id)
        return if (auth.currentUser != null) remote.adminDeleteDoc("important_questions", id) else Result.success(Unit)
    }

    suspend fun adminSaveMcq(item: McqEntity): Result<Unit> {
        dao.upsertMcqs(listOf(item))
        return if (auth.currentUser != null) remote.adminUpsertMcq(item) else Result.success(Unit)
    }

    suspend fun adminDeleteMcq(id: String): Result<Unit> {
        dao.deleteMcq(id)
        return if (auth.currentUser != null) remote.adminDeleteDoc("mcqs", id) else Result.success(Unit)
    }

    suspend fun adminSaveMockExam(item: MockExamEntity): Result<Unit> {
        dao.upsertMockExams(listOf(item))
        return if (auth.currentUser != null) remote.adminUpsertMockExam(item) else Result.success(Unit)
    }

    suspend fun adminDeleteMockExam(id: String): Result<Unit> {
        dao.deleteMockExam(id)
        return if (auth.currentUser != null) remote.adminDeleteDoc("mock_exams", id) else Result.success(Unit)
    }

    suspend fun adminSavePyq(item: PyqPaperEntity): Result<Unit> {
        dao.upsertPyqs(listOf(item))
        return if (auth.currentUser != null) remote.adminUpsertPyq(item) else Result.success(Unit)
    }

    suspend fun adminDeletePyq(id: String): Result<Unit> {
        dao.deletePyq(id)
        return if (auth.currentUser != null) remote.adminDeleteDoc("pyq_papers", id) else Result.success(Unit)
    }

    suspend fun adminSaveNote(item: StudyNoteEntity): Result<Unit> {
        dao.upsertNotes(listOf(item))
        return if (auth.currentUser != null) remote.adminUpsertNote(item) else Result.success(Unit)
    }

    suspend fun adminDeleteNote(id: String): Result<Unit> {
        dao.deleteNote(id)
        return if (auth.currentUser != null) remote.adminDeleteDoc("study_notes", id) else Result.success(Unit)
    }

    suspend fun adminFetchStudentActivity(): Result<List<AdminStudentSummary>> =
        remote.adminFetchStudentActivity()

    suspend fun adminImportQuestionsJsonOrCsv(rawText: String): Result<Int> {
        val trimmed = rawText.trim()
        if (trimmed.isEmpty()) return Result.failure(IllegalArgumentException("Empty import input"))
        return try {
            var count = 0
            if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                val array = if (trimmed.startsWith("[")) JSONArray(trimmed)
                else JSONObject(trimmed).optJSONArray("mcqs") ?: JSONArray()
                val imported = mutableListOf<McqEntity>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val id = obj.optString("id").ifBlank { "mcq_imp_${System.currentTimeMillis()}_$i" }
                    val optEnArr = obj.optJSONArray("optionsEn")
                    val optOrArr = obj.optJSONArray("optionsOr")
                    val optionsEn = (0 until 4).map { idx -> optEnArr?.optString(idx) ?: "Option ${idx + 1}" }
                    val optionsOr = (0 until 4).map { idx -> optOrArr?.optString(idx)?.ifBlank { optionsEn[idx] } ?: optionsEn[idx] }
                    val mcq = McqEntity(
                        id = id,
                        boardId = BoardType.fromId(obj.optString("boardId")).id,
                        subjectId = obj.optString("subjectId").ifBlank { "bse_math" },
                        chapterId = obj.optString("chapterId").ifBlank { "bse_math_ch1" },
                        questionEn = obj.getString("questionEn"),
                        questionOr = obj.optString("questionOr").ifBlank { obj.getString("questionEn") },
                        optionsEn = optionsEn,
                        optionsOr = optionsOr,
                        correctIndex = obj.optLong("correctIndex", 0L).coerceIn(0L, 3L),
                        explanationEn = obj.optString("explanationEn").ifBlank { "Verified by Admin." },
                        explanationOr = obj.optString("explanationOr").ifBlank { "ଆଡମିନ୍ ଦ୍ୱାରା ଯାଞ୍ଚିତ।" },
                        difficulty = Difficulty.fromRaw(obj.optString("difficulty")).name,
                        contentLabel = ContentLabel.fromRaw(obj.optString("contentLabel")).name,
                        sourceYear = obj.optString("sourceYear"),
                        published = obj.optBoolean("published", true)
                    )
                    imported.add(mcq)
                    if (auth.currentUser != null) remote.adminUpsertMcq(mcq)
                    count++
                }
                if (imported.isNotEmpty()) dao.upsertMcqs(imported)
            } else {
                // CSV format: boardId,subjectId,chapterId,questionEn,questionOr,optA,optB,optC,optD,correctIndex,explanationEn,contentLabel
                val lines = trimmed.lines().filter { it.isNotBlank() }
                val startIdx = if (lines.firstOrNull()?.contains("questionEn", ignoreCase = true) == true) 1 else 0
                val imported = mutableListOf<McqEntity>()
                for (idx in startIdx until lines.size) {
                    val cols = lines[idx].split(",").map { it.trim() }
                    if (cols.size < 10) continue
                    val mcq = McqEntity(
                        id = "mcq_csv_${System.currentTimeMillis()}_$idx",
                        boardId = BoardType.fromId(cols[0]).id,
                        subjectId = cols[1],
                        chapterId = cols[2],
                        questionEn = cols[3],
                        questionOr = cols[4].ifBlank { cols[3] },
                        optionsEn = listOf(cols[5], cols[6], cols[7], cols[8]),
                        optionsOr = listOf(cols[5], cols[6], cols[7], cols[8]),
                        correctIndex = (cols[9].toLongOrNull() ?: 0L).coerceIn(0L, 3L),
                        explanationEn = cols.getOrNull(10)?.ifBlank { "Imported via CSV" } ?: "Imported via CSV",
                        explanationOr = cols.getOrNull(10)?.ifBlank { "CSV ଦ୍ୱାରା ଆମଦାନୀ" } ?: "CSV ଦ୍ୱାରା ଆମଦାନୀ",
                        difficulty = Difficulty.MEDIUM.name,
                        contentLabel = ContentLabel.fromRaw(cols.getOrNull(11)).name,
                        sourceYear = "",
                        published = true
                    )
                    imported.add(mcq)
                    if (auth.currentUser != null) remote.adminUpsertMcq(mcq)
                    count++
                }
                if (imported.isNotEmpty()) dao.upsertMcqs(imported)
            }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportQuestionBankBackupJson(): String {
        val mcqs = dao.observeAllMcqsAdmin().first()
        val imps = dao.observeAllImportantQuestionsAdmin().first()
        val root = JSONObject()
        val mcqArr = JSONArray()
        mcqs.forEach { m ->
            mcqArr.put(JSONObject().apply {
                put("id", m.id)
                put("boardId", m.boardId)
                put("subjectId", m.subjectId)
                put("chapterId", m.chapterId)
                put("questionEn", m.questionEn)
                put("questionOr", m.questionOr)
                put("optionsEn", JSONArray(m.optionsEn))
                put("optionsOr", JSONArray(m.optionsOr))
                put("correctIndex", m.correctIndex)
                put("explanationEn", m.explanationEn)
                put("explanationOr", m.explanationOr)
                put("difficulty", m.difficulty)
                put("contentLabel", m.contentLabel)
                put("sourceYear", m.sourceYear)
                put("published", m.published)
            })
        }
        val impArr = JSONArray()
        imps.forEach { q ->
            impArr.put(JSONObject().apply {
                put("id", q.id)
                put("boardId", q.boardId)
                put("subjectId", q.subjectId)
                put("chapterId", q.chapterId)
                put("marks", q.marks)
                put("category", q.category)
                put("difficulty", q.difficulty)
                put("contentLabel", q.contentLabel)
                put("sourceYear", q.sourceYear)
                put("questionEn", q.questionEn)
                put("questionOr", q.questionOr)
                put("answerEn", q.answerEn)
                put("answerOr", q.answerOr)
                put("published", q.published)
            })
        }
        root.put("exportedAt", System.currentTimeMillis())
        root.put("mcqs", mcqArr)
        root.put("importantQuestions", impArr)
        return root.toString(2)
    }
}
