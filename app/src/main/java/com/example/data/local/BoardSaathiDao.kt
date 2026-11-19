package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BoardSaathiDao {
    // Subjects
    @Query("SELECT * FROM subjects WHERE boardId = :boardId AND published = 1 ORDER BY sortOrder ASC")
    fun observePublishedSubjects(boardId: String): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects ORDER BY boardId ASC, sortOrder ASC")
    fun observeAllSubjectsAdmin(): Flow<List<SubjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSubjects(items: List<SubjectEntity>)

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun deleteSubject(id: String)

    // Chapters
    @Query("SELECT * FROM chapters WHERE boardId = :boardId AND published = 1 ORDER BY chapterNumber ASC")
    fun observePublishedChapters(boardId: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters ORDER BY boardId ASC, subjectId ASC, chapterNumber ASC")
    fun observeAllChaptersAdmin(): Flow<List<ChapterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChapters(items: List<ChapterEntity>)

    @Query("DELETE FROM chapters WHERE id = :id")
    suspend fun deleteChapter(id: String)

    // Important Questions
    @Query("SELECT * FROM important_questions WHERE boardId = :boardId AND published = 1")
    fun observePublishedImportantQuestions(boardId: String): Flow<List<ImportantQuestionEntity>>

    @Query("SELECT * FROM important_questions")
    fun observeAllImportantQuestionsAdmin(): Flow<List<ImportantQuestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertImportantQuestions(items: List<ImportantQuestionEntity>)

    @Query("DELETE FROM important_questions WHERE id = :id")
    suspend fun deleteImportantQuestion(id: String)

    // MCQs
    @Query("SELECT * FROM mcqs WHERE boardId = :boardId AND published = 1")
    fun observePublishedMcqs(boardId: String): Flow<List<McqEntity>>

    @Query("SELECT * FROM mcqs")
    fun observeAllMcqsAdmin(): Flow<List<McqEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMcqs(items: List<McqEntity>)

    @Query("DELETE FROM mcqs WHERE id = :id")
    suspend fun deleteMcq(id: String)

    // Mock Exams
    @Query("SELECT * FROM mock_exams WHERE boardId = :boardId AND published = 1")
    fun observePublishedMockExams(boardId: String): Flow<List<MockExamEntity>>

    @Query("SELECT * FROM mock_exams")
    fun observeAllMockExamsAdmin(): Flow<List<MockExamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMockExams(items: List<MockExamEntity>)

    @Query("DELETE FROM mock_exams WHERE id = :id")
    suspend fun deleteMockExam(id: String)

    // PYQ Papers
    @Query("SELECT * FROM pyq_papers WHERE boardId = :boardId AND published = 1 ORDER BY examYear DESC")
    fun observePublishedPyqs(boardId: String): Flow<List<PyqPaperEntity>>

    @Query("SELECT * FROM pyq_papers")
    fun observeAllPyqsAdmin(): Flow<List<PyqPaperEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPyqs(items: List<PyqPaperEntity>)

    @Query("DELETE FROM pyq_papers WHERE id = :id")
    suspend fun deletePyq(id: String)

    // Study Notes
    @Query("SELECT * FROM study_notes WHERE boardId = :boardId AND published = 1")
    fun observePublishedNotes(boardId: String): Flow<List<StudyNoteEntity>>

    @Query("SELECT * FROM study_notes")
    fun observeAllNotesAdmin(): Flow<List<StudyNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNotes(items: List<StudyNoteEntity>)

    @Query("UPDATE study_notes SET savedOffline = :saved WHERE id = :id")
    suspend fun setNoteOfflineSaved(id: String, saved: Boolean)

    @Query("DELETE FROM study_notes WHERE id = :id")
    suspend fun deleteNote(id: String)

    // Bookmarks
    @Query("SELECT * FROM bookmarks WHERE boardId = :boardId ORDER BY updatedAtMillis DESC")
    fun observeBookmarks(boardId: String): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBookmark(item: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmark(id: String)

    // Chapter Progress
    @Query("SELECT * FROM chapter_progress WHERE boardId = :boardId")
    fun observeChapterProgress(boardId: String): Flow<List<ChapterProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChapterProgress(item: ChapterProgressEntity)

    // Quiz Attempts
    @Query("SELECT * FROM quiz_attempts WHERE boardId = :boardId ORDER BY createdAtMillis DESC")
    fun observeQuizAttempts(boardId: String): Flow<List<QuizAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQuizAttempt(item: QuizAttemptEntity)

    // Saved Unfinished Quiz Session
    @Query("SELECT * FROM saved_quiz_sessions WHERE boardId = :boardId")
    fun observeSavedQuizSession(boardId: String): Flow<SavedQuizSessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSavedQuizSession(session: SavedQuizSessionEntity)

    @Query("DELETE FROM saved_quiz_sessions WHERE boardId = :boardId")
    suspend fun clearSavedQuizSession(boardId: String)

    @Query("SELECT COUNT(*) FROM subjects")
    suspend fun countSubjects(): Int
}
