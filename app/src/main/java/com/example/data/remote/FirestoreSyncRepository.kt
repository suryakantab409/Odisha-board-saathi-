package com.example.data.remote

import android.content.Context
import com.example.R
import com.example.data.local.BookmarkEntity
import com.example.data.local.ChapterEntity
import com.example.data.local.ChapterProgressEntity
import com.example.data.local.ImportantQuestionEntity
import com.example.data.local.McqEntity
import com.example.data.local.MockExamEntity
import com.example.data.local.PyqPaperEntity
import com.example.data.local.QuizAttemptEntity
import com.example.data.local.StudentPreferences
import com.example.data.local.StudyNoteEntity
import com.example.data.local.SubjectEntity
import com.example.model.AppLanguage
import com.example.model.BoardType
import com.example.model.ChapterModel
import com.example.model.ChapterProgressModel
import com.example.model.ImportantQuestionModel
import com.example.model.McqModel
import com.example.model.MockExamModel
import com.example.model.PyqPaperModel
import com.example.model.QuizAttemptModel
import com.example.model.StudyNoteModel
import com.example.model.SubjectModel
import com.example.model.UserBookmarkModel
import com.example.model.UserProfileModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

data class AdminStudentSummary(
    val userId: String,
    val displayName: String,
    val boardId: String,
    val streakDays: Long,
    val recentAttempts: List<QuizAttemptModel>
)

class FirestoreSyncRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        val uid = auth.currentUser?.uid
        require(!uid.isNullOrBlank()) { "User must be signed in with Google before accessing Firestore." }
        return uid
    }

    private fun sanitizeId(raw: String): String =
        raw.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(120).ifBlank { "item_${System.currentTimeMillis()}" }

    suspend fun checkIsAdmin(): Boolean {
        val user = auth.currentUser ?: return false
        if (user.email == BOOTSTRAPPED_ADMIN_EMAIL && user.isEmailVerified) {
            return true
        }
        return try {
            val snap = db.collection("admins").document(user.uid).get().await()
            snap.exists()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            false
        }
    }

    suspend fun syncUserProfileUp(prefs: StudentPreferences): Result<Unit> {
        var path = "users"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid)
            path = docRef.path
            val existing = docRef.get().await()
            if (existing.exists()) {
                docRef.update(
                    mapOf(
                        "displayName" to prefs.displayName.take(80),
                        "boardId" to prefs.board.id,
                        "language" to prefs.language.code,
                        "examDateEpochDays" to prefs.examDateEpochDays,
                        "dailyTargetQuestions" to prefs.dailyTargetQuestions.toLong(),
                        "streakDays" to prefs.streakDays.toLong(),
                        "lastStudyEpochDay" to prefs.lastStudyEpochDay,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "displayName" to prefs.displayName.take(80),
                        "boardId" to prefs.board.id,
                        "language" to prefs.language.code,
                        "examDateEpochDays" to prefs.examDateEpochDays,
                        "dailyTargetQuestions" to prefs.dailyTargetQuestions.toLong(),
                        "streakDays" to prefs.streakDays.toLong(),
                        "lastStudyEpochDay" to prefs.lastStudyEpochDay,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun fetchUserProfile(): Result<UserProfileModel?> {
        var path = "users"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid)
            path = docRef.path
            val snap = docRef.get().await()
            val model = if (snap.exists()) {
                snap.toObject(UserProfileModel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else null
            Result.success(model)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            Result.failure(e)
        }
    }

    suspend fun upsertBookmark(bookmark: BookmarkEntity): Result<Unit> {
        var path = "users/bookmarks"
        return try {
            val uid = requireUserId()
            val docId = sanitizeId(bookmark.id)
            val docRef = db.collection("users").document(uid).collection("bookmarks").document(docId)
            path = docRef.path
            val existing = docRef.get().await()
            if (existing.exists()) {
                docRef.update(
                    mapOf(
                        "inRevisionList" to bookmark.inRevisionList,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "itemId" to sanitizeId(bookmark.itemId),
                        "itemType" to bookmark.itemType,
                        "boardId" to bookmark.boardId,
                        "subjectId" to sanitizeId(bookmark.subjectId),
                        "inRevisionList" to bookmark.inRevisionList,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteBookmark(bookmarkId: String): Result<Unit> {
        var path = "users/bookmarks"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid).collection("bookmarks").document(sanitizeId(bookmarkId))
            path = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun upsertChapterProgress(progress: ChapterProgressEntity): Result<Unit> {
        var path = "users/chapter_progress"
        return try {
            val uid = requireUserId()
            val docId = sanitizeId(progress.chapterId)
            val docRef = db.collection("users").document(uid).collection("chapter_progress").document(docId)
            path = docRef.path
            val existing = docRef.get().await()
            if (existing.exists()) {
                docRef.update(
                    mapOf(
                        "completed" to progress.completed,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "chapterId" to docId,
                        "boardId" to progress.boardId,
                        "subjectId" to sanitizeId(progress.subjectId),
                        "completed" to progress.completed,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun uploadQuizAttempt(attempt: QuizAttemptEntity): Result<Unit> {
        var path = "users/attempts"
        return try {
            val uid = requireUserId()
            val docId = sanitizeId(attempt.id)
            val docRef = db.collection("users").document(uid).collection("attempts").document(docId)
            path = docRef.path
            val existing = docRef.get().await()
            val cleanSubject = attempt.subjectId.takeIf { it.isNotBlank() }?.let { sanitizeId(it) }
            val cleanWrong = attempt.wrongMcqIds.take(100).map { sanitizeId(it) }
            if (existing.exists()) {
                val status = existing.getString("status")
                if (status != "COMPLETED") {
                    docRef.update(
                        mapOf(
                            "correctCount" to attempt.correctCount,
                            "wrongCount" to attempt.wrongCount,
                            "scoreMarks" to attempt.scoreMarks,
                            "durationSeconds" to attempt.durationSeconds,
                            "status" to attempt.status,
                            "wrongMcqIds" to cleanWrong,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            } else {
                val payload = mutableMapOf<String, Any>(
                    "boardId" to attempt.boardId,
                    "mode" to attempt.mode,
                    "title" to attempt.title.take(160).ifBlank { "Quiz" },
                    "totalQuestions" to attempt.totalQuestions.coerceAtLeast(1L),
                    "correctCount" to attempt.correctCount.coerceAtLeast(0L),
                    "wrongCount" to attempt.wrongCount.coerceAtLeast(0L),
                    "scoreMarks" to attempt.scoreMarks.coerceAtLeast(0L),
                    "maxMarks" to attempt.maxMarks.coerceAtLeast(1L),
                    "durationSeconds" to attempt.durationSeconds.coerceAtLeast(0L),
                    "status" to attempt.status,
                    "wrongMcqIds" to cleanWrong,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (cleanSubject != null) {
                    payload["subjectId"] = cleanSubject
                }
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun fetchUserBookmarks(): Result<List<BookmarkEntity>> {
        var path = "users/bookmarks"
        return try {
            val uid = requireUserId()
            val colRef = db.collection("users").document(uid).collection("bookmarks")
            path = colRef.path
            val snap = colRef.get().await()
            val items = snap.toObjects(UserBookmarkModel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                .map {
                    BookmarkEntity(
                        id = it.id,
                        itemId = it.itemId,
                        itemType = it.itemType,
                        boardId = it.boardId,
                        subjectId = it.subjectId,
                        inRevisionList = it.inRevisionList,
                        updatedAtMillis = it.updatedAt?.toDate()?.time ?: System.currentTimeMillis()
                    )
                }
            Result.success(items)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            Result.failure(e)
        }
    }

    suspend fun fetchUserChapterProgress(): Result<List<ChapterProgressEntity>> {
        var path = "users/chapter_progress"
        return try {
            val uid = requireUserId()
            val colRef = db.collection("users").document(uid).collection("chapter_progress")
            path = colRef.path
            val snap = colRef.get().await()
            val items = snap.toObjects(ChapterProgressModel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                .map {
                    ChapterProgressEntity(
                        chapterId = it.chapterId.ifBlank { it.id },
                        boardId = it.boardId,
                        subjectId = it.subjectId,
                        completed = it.completed,
                        updatedAtMillis = it.updatedAt?.toDate()?.time ?: System.currentTimeMillis()
                    )
                }
            Result.success(items)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            Result.failure(e)
        }
    }

    suspend fun fetchUserAttempts(): Result<List<QuizAttemptEntity>> {
        var path = "users/attempts"
        return try {
            val uid = requireUserId()
            val colRef = db.collection("users").document(uid).collection("attempts")
            path = colRef.path
            val snap = colRef.get().await()
            val items = snap.toObjects(QuizAttemptModel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                .map {
                    QuizAttemptEntity(
                        id = it.id,
                        boardId = it.boardId,
                        subjectId = it.subjectId,
                        mode = it.mode,
                        title = it.title,
                        totalQuestions = it.totalQuestions,
                        correctCount = it.correctCount,
                        wrongCount = it.wrongCount,
                        scoreMarks = it.scoreMarks,
                        maxMarks = it.maxMarks,
                        durationSeconds = it.durationSeconds,
                        status = it.status,
                        wrongMcqIds = it.wrongMcqIds,
                        createdAtMillis = it.createdAt?.toDate()?.time ?: System.currentTimeMillis()
                    )
                }
            Result.success(items)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            Result.failure(e)
        }
    }

    suspend fun fetchPublishedCatalog(isAdmin: Boolean): Result<CatalogSnapshot> {
        return try {
            requireUserId()
            fun <T> queryOrAll(col: String, clazz: Class<T>): com.google.firebase.firestore.Query {
                return if (isAdmin) db.collection(col) else db.collection(col).whereEqualTo("published", true)
            }

            val subjects = queryOrAll("subjects", SubjectModel::class.java).get().await()
                .toObjects(SubjectModel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                .map { SubjectEntity(it.id, it.boardId, it.nameEn, it.nameOr, it.sortOrder, it.published, it.syllabusNote) }

            val chapters = queryOrAll("chapters", ChapterModel::class.java).get().await()
                .toObjects(ChapterModel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                .map { ChapterEntity(it.id, it.boardId, it.subjectId, it.titleEn, it.titleOr, it.chapterNumber, it.published) }

            val questions = queryOrAll("important_questions", ImportantQuestionModel::class.java).get().await()
                .toObjects(ImportantQuestionModel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                .map {
                    ImportantQuestionEntity(
                        it.id, it.boardId, it.subjectId, it.chapterId, it.marks, it.category,
                        it.difficulty, it.contentLabel, it.sourceYear, it.questionEn, it.questionOr,
                        it.answerEn, it.answerOr, it.published
                    )
                }

            val mcqs = queryOrAll("mcqs", McqModel::class.java).get().await()
                .toObjects(McqModel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                .map {
                    McqEntity(
                        it.id, it.boardId, it.subjectId, it.chapterId, it.questionEn, it.questionOr,
                        it.optionsEn, it.optionsOr, it.correctIndex, it.explanationEn, it.explanationOr,
                        it.difficulty, it.contentLabel, it.sourceYear, it.published
                    )
                }

            val mocks = queryOrAll("mock_exams", MockExamModel::class.java).get().await()
                .toObjects(MockExamModel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                .map {
                    MockExamEntity(
                        it.id, it.boardId, it.subjectId, it.titleEn, it.titleOr,
                        it.durationMinutes, it.totalMarks, it.questionIds, it.patternNote, it.published
                    )
                }

            val pyqs = queryOrAll("pyq_papers", PyqPaperModel::class.java).get().await()
                .toObjects(PyqPaperModel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                .map {
                    PyqPaperEntity(
                        it.id, it.boardId, it.subjectId, it.examYear, it.questionType,
                        it.titleEn, it.titleOr, it.searchableText, it.pdfUri, it.contentLabel, it.published
                    )
                }

            val notes = queryOrAll("study_notes", StudyNoteModel::class.java).get().await()
                .toObjects(StudyNoteModel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                .map {
                    StudyNoteEntity(
                        it.id, it.boardId, it.subjectId, it.chapterId, it.noteType,
                        it.titleEn, it.titleOr, it.contentEn, it.contentOr, it.pdfUri, it.published
                    )
                }

            Result.success(CatalogSnapshot(subjects, chapters, questions, mcqs, mocks, pyqs, notes))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, "catalog")
            Result.failure(e)
        }
    }

    // --- Admin Cloud Write Operations ---

    suspend fun adminUpsertSubject(entity: SubjectEntity): Result<Unit> = adminUpsertDoc(
        collection = "subjects",
        docId = sanitizeId(entity.id),
        createPayload = mapOf(
            "boardId" to entity.boardId,
            "nameEn" to entity.nameEn.take(120),
            "nameOr" to entity.nameOr.take(180).ifBlank { entity.nameEn.take(120) },
            "sortOrder" to entity.sortOrder,
            "published" to entity.published,
            "syllabusNote" to entity.syllabusNote.take(500),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null },
        updatePayload = mapOf(
            "boardId" to entity.boardId,
            "nameEn" to entity.nameEn.take(120),
            "nameOr" to entity.nameOr.take(180).ifBlank { entity.nameEn.take(120) },
            "sortOrder" to entity.sortOrder,
            "published" to entity.published,
            "syllabusNote" to entity.syllabusNote.take(500),
            "updatedAt" to FieldValue.serverTimestamp()
        )
    )

    suspend fun adminUpsertChapter(entity: ChapterEntity): Result<Unit> = adminUpsertDoc(
        collection = "chapters",
        docId = sanitizeId(entity.id),
        createPayload = mapOf(
            "boardId" to entity.boardId,
            "subjectId" to sanitizeId(entity.subjectId),
            "titleEn" to entity.titleEn.take(160),
            "titleOr" to entity.titleOr.take(220).ifBlank { entity.titleEn.take(160) },
            "chapterNumber" to entity.chapterNumber.coerceAtLeast(1L),
            "published" to entity.published,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ),
        updatePayload = mapOf(
            "boardId" to entity.boardId,
            "subjectId" to sanitizeId(entity.subjectId),
            "titleEn" to entity.titleEn.take(160),
            "titleOr" to entity.titleOr.take(220).ifBlank { entity.titleEn.take(160) },
            "chapterNumber" to entity.chapterNumber.coerceAtLeast(1L),
            "published" to entity.published,
            "updatedAt" to FieldValue.serverTimestamp()
        )
    )

    suspend fun adminUpsertImportantQuestion(entity: ImportantQuestionEntity): Result<Unit> {
        val createMap = mutableMapOf<String, Any>(
            "boardId" to entity.boardId,
            "subjectId" to sanitizeId(entity.subjectId),
            "chapterId" to sanitizeId(entity.chapterId),
            "marks" to entity.marks.coerceIn(1L, 20L),
            "category" to entity.category,
            "difficulty" to entity.difficulty,
            "contentLabel" to entity.contentLabel,
            "questionEn" to entity.questionEn.take(2000),
            "questionOr" to entity.questionOr.take(2500).ifBlank { entity.questionEn.take(2000) },
            "answerEn" to entity.answerEn.take(6000),
            "answerOr" to entity.answerOr.take(7000).ifBlank { entity.answerEn.take(6000) },
            "published" to entity.published,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (entity.sourceYear.isNotBlank()) createMap["sourceYear"] = entity.sourceYear.take(40)

        val updateMap = createMap.toMutableMap().apply { remove("createdAt") }
        return adminUpsertDoc("important_questions", sanitizeId(entity.id), createMap, updateMap)
    }

    suspend fun adminUpsertMcq(entity: McqEntity): Result<Unit> {
        val optsEn = entity.optionsEn.take(4).let { if (it.size == 4) it else listOf("Option A", "Option B", "Option C", "Option D") }
        val optsOr = entity.optionsOr.take(4).let { if (it.size == 4) it else optsEn }
        val createMap = mutableMapOf<String, Any>(
            "boardId" to entity.boardId,
            "subjectId" to sanitizeId(entity.subjectId),
            "chapterId" to sanitizeId(entity.chapterId),
            "questionEn" to entity.questionEn.take(2000),
            "questionOr" to entity.questionOr.take(2500).ifBlank { entity.questionEn.take(2000) },
            "optionsEn" to optsEn,
            "optionsOr" to optsOr,
            "correctIndex" to entity.correctIndex.coerceIn(0L, 3L),
            "explanationEn" to entity.explanationEn.take(2000).ifBlank { "See chapter notes." },
            "explanationOr" to entity.explanationOr.take(2500).ifBlank { entity.explanationEn.take(2000).ifBlank { "ଅଧ୍ୟାୟ ନୋଟ୍ସ ଦେଖନ୍ତୁ।" } },
            "difficulty" to entity.difficulty,
            "contentLabel" to entity.contentLabel,
            "published" to entity.published,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (entity.sourceYear.isNotBlank()) createMap["sourceYear"] = entity.sourceYear.take(40)
        val updateMap = createMap.toMutableMap().apply { remove("createdAt") }
        return adminUpsertDoc("mcqs", sanitizeId(entity.id), createMap, updateMap)
    }

    suspend fun adminUpsertMockExam(entity: MockExamEntity): Result<Unit> = adminUpsertDoc(
        collection = "mock_exams",
        docId = sanitizeId(entity.id),
        createPayload = mapOf(
            "boardId" to entity.boardId,
            "subjectId" to sanitizeId(entity.subjectId),
            "titleEn" to entity.titleEn.take(160),
            "titleOr" to entity.titleOr.take(220).ifBlank { entity.titleEn.take(160) },
            "durationMinutes" to entity.durationMinutes.coerceIn(5L, 300L),
            "totalMarks" to entity.totalMarks.coerceIn(1L, 500L),
            "questionIds" to entity.questionIds.take(100).map { sanitizeId(it) },
            "patternNote" to entity.patternNote.take(500),
            "published" to entity.published,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ),
        updatePayload = mapOf(
            "boardId" to entity.boardId,
            "subjectId" to sanitizeId(entity.subjectId),
            "titleEn" to entity.titleEn.take(160),
            "titleOr" to entity.titleOr.take(220).ifBlank { entity.titleEn.take(160) },
            "durationMinutes" to entity.durationMinutes.coerceIn(5L, 300L),
            "totalMarks" to entity.totalMarks.coerceIn(1L, 500L),
            "questionIds" to entity.questionIds.take(100).map { sanitizeId(it) },
            "patternNote" to entity.patternNote.take(500),
            "published" to entity.published,
            "updatedAt" to FieldValue.serverTimestamp()
        )
    )

    suspend fun adminUpsertPyq(entity: PyqPaperEntity): Result<Unit> {
        val createMap = mutableMapOf<String, Any>(
            "boardId" to entity.boardId,
            "subjectId" to sanitizeId(entity.subjectId),
            "examYear" to entity.examYear.take(20),
            "questionType" to entity.questionType.take(60),
            "titleEn" to entity.titleEn.take(180),
            "titleOr" to entity.titleOr.take(220).ifBlank { entity.titleEn.take(180) },
            "searchableText" to entity.searchableText.take(8000),
            "contentLabel" to if (entity.contentLabel == "VERIFIED_PYQ") "VERIFIED_PYQ" else "PRACTICE_QUESTION",
            "published" to entity.published,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (entity.pdfUri.isNotBlank()) createMap["pdfUri"] = entity.pdfUri.take(1000)
        val updateMap = createMap.toMutableMap().apply { remove("createdAt") }
        return adminUpsertDoc("pyq_papers", sanitizeId(entity.id), createMap, updateMap)
    }

    suspend fun adminUpsertNote(entity: StudyNoteEntity): Result<Unit> {
        val createMap = mutableMapOf<String, Any>(
            "boardId" to entity.boardId,
            "subjectId" to sanitizeId(entity.subjectId),
            "chapterId" to sanitizeId(entity.chapterId),
            "noteType" to entity.noteType,
            "titleEn" to entity.titleEn.take(180),
            "titleOr" to entity.titleOr.take(220).ifBlank { entity.titleEn.take(180) },
            "contentEn" to entity.contentEn.take(12000),
            "contentOr" to entity.contentOr.take(14000).ifBlank { entity.contentEn.take(12000) },
            "published" to entity.published,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (entity.pdfUri.isNotBlank()) createMap["pdfUri"] = entity.pdfUri.take(1000)
        val updateMap = createMap.toMutableMap().apply { remove("createdAt") }
        return adminUpsertDoc("study_notes", sanitizeId(entity.id), createMap, updateMap)
    }

    suspend fun adminDeleteDoc(collection: String, docId: String): Result<Unit> {
        var path = "$collection/$docId"
        return try {
            requireUserId()
            val ref = db.collection(collection).document(sanitizeId(docId))
            path = ref.path
            ref.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun adminFetchStudentActivity(): Result<List<AdminStudentSummary>> {
        var path = "users"
        return try {
            requireUserId()
            val usersSnap = db.collection("users").limit(25).get().await()
            val summaries = usersSnap.toObjects(UserProfileModel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                .map { user ->
                    val attemptsSnap = db.collection("users").document(user.userId)
                        .collection("attempts").limit(10).get().await()
                    val attempts = attemptsSnap.toObjects(QuizAttemptModel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    AdminStudentSummary(
                        userId = user.userId,
                        displayName = user.displayName.ifBlank { "Student (${user.userId.take(6)})" },
                        boardId = user.boardId,
                        streakDays = user.streakDays,
                        recentAttempts = attempts
                    )
                }
            Result.success(summaries)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            Result.failure(e)
        }
    }

    private suspend fun adminUpsertDoc(
        collection: String,
        docId: String,
        createPayload: Map<String, Any>,
        updatePayload: Map<String, Any>
    ): Result<Unit> {
        var path = "$collection/$docId"
        return try {
            requireUserId()
            val docRef = db.collection(collection).document(docId)
            path = docRef.path
            val existing = docRef.get().await()
            if (existing.exists()) {
                docRef.update(updatePayload).await()
            } else {
                docRef.set(createPayload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    companion object {
        const val BOOTSTRAPPED_ADMIN_EMAIL = "suryakantabarik216@gmail.com"
    }
}

data class CatalogSnapshot(
    val subjects: List<SubjectEntity>,
    val chapters: List<ChapterEntity>,
    val importantQuestions: List<ImportantQuestionEntity>,
    val mcqs: List<McqEntity>,
    val mockExams: List<MockExamEntity>,
    val pyqPapers: List<PyqPaperEntity>,
    val studyNotes: List<StudyNoteEntity>
)
