package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.model.AppLanguage
import com.example.model.ChapterModel
import com.example.model.ImportantQuestionModel
import com.example.model.McqModel
import com.example.model.MockExamModel
import com.example.model.PyqPaperModel
import com.example.model.QuizAttemptModel
import com.example.model.StudyNoteModel
import com.example.model.SubjectModel
import com.example.model.UserBookmarkModel

class RoomConverters {
    private val separator = "\u001F"

    @TypeConverter
    fun fromStringList(list: List<String>?): String =
        list?.joinToString(separator) ?: ""

    @TypeConverter
    fun toStringList(raw: String?): List<String> =
        if (raw.isNullOrEmpty()) emptyList() else raw.split(separator)
}

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val boardId: String,
    val nameEn: String,
    val nameOr: String,
    val sortOrder: Long,
    val published: Boolean,
    val syllabusNote: String
) {
    fun displayName(lang: AppLanguage): String =
        if (lang == AppLanguage.OR && nameOr.isNotBlank()) nameOr else nameEn

    fun toModel() = SubjectModel(
        id = id,
        boardId = boardId,
        nameEn = nameEn,
        nameOr = nameOr,
        sortOrder = sortOrder,
        published = published,
        syllabusNote = syllabusNote
    )
}

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey val id: String,
    val boardId: String,
    val subjectId: String,
    val titleEn: String,
    val titleOr: String,
    val chapterNumber: Long,
    val published: Boolean
) {
    fun displayTitle(lang: AppLanguage): String =
        if (lang == AppLanguage.OR && titleOr.isNotBlank()) titleOr else titleEn

    fun toModel() = ChapterModel(
        id = id,
        boardId = boardId,
        subjectId = subjectId,
        titleEn = titleEn,
        titleOr = titleOr,
        chapterNumber = chapterNumber,
        published = published
    )
}

@Entity(tableName = "important_questions")
data class ImportantQuestionEntity(
    @PrimaryKey val id: String,
    val boardId: String,
    val subjectId: String,
    val chapterId: String,
    val marks: Long,
    val category: String,
    val difficulty: String,
    val contentLabel: String,
    val sourceYear: String,
    val questionEn: String,
    val questionOr: String,
    val answerEn: String,
    val answerOr: String,
    val published: Boolean
) {
    fun toModel() = ImportantQuestionModel(
        id = id,
        boardId = boardId,
        subjectId = subjectId,
        chapterId = chapterId,
        marks = marks,
        category = category,
        difficulty = difficulty,
        contentLabel = contentLabel,
        sourceYear = sourceYear,
        questionEn = questionEn,
        questionOr = questionOr,
        answerEn = answerEn,
        answerOr = answerOr,
        published = published
    )
}

@Entity(tableName = "mcqs")
data class McqEntity(
    @PrimaryKey val id: String,
    val boardId: String,
    val subjectId: String,
    val chapterId: String,
    val questionEn: String,
    val questionOr: String,
    val optionsEn: List<String>,
    val optionsOr: List<String>,
    val correctIndex: Long,
    val explanationEn: String,
    val explanationOr: String,
    val difficulty: String,
    val contentLabel: String,
    val sourceYear: String,
    val published: Boolean
) {
    fun toModel() = McqModel(
        id = id,
        boardId = boardId,
        subjectId = subjectId,
        chapterId = chapterId,
        questionEn = questionEn,
        questionOr = questionOr,
        optionsEn = optionsEn,
        optionsOr = optionsOr,
        correctIndex = correctIndex,
        explanationEn = explanationEn,
        explanationOr = explanationOr,
        difficulty = difficulty,
        contentLabel = contentLabel,
        sourceYear = sourceYear,
        published = published
    )
}

@Entity(tableName = "mock_exams")
data class MockExamEntity(
    @PrimaryKey val id: String,
    val boardId: String,
    val subjectId: String,
    val titleEn: String,
    val titleOr: String,
    val durationMinutes: Long,
    val totalMarks: Long,
    val questionIds: List<String>,
    val patternNote: String,
    val published: Boolean
) {
    fun toModel() = MockExamModel(
        id = id,
        boardId = boardId,
        subjectId = subjectId,
        titleEn = titleEn,
        titleOr = titleOr,
        durationMinutes = durationMinutes,
        totalMarks = totalMarks,
        questionIds = questionIds,
        patternNote = patternNote,
        published = published
    )
}

@Entity(tableName = "pyq_papers")
data class PyqPaperEntity(
    @PrimaryKey val id: String,
    val boardId: String,
    val subjectId: String,
    val examYear: String,
    val questionType: String,
    val titleEn: String,
    val titleOr: String,
    val searchableText: String,
    val pdfUri: String,
    val contentLabel: String,
    val published: Boolean
) {
    fun toModel() = PyqPaperModel(
        id = id,
        boardId = boardId,
        subjectId = subjectId,
        examYear = examYear,
        questionType = questionType,
        titleEn = titleEn,
        titleOr = titleOr,
        searchableText = searchableText,
        pdfUri = pdfUri,
        contentLabel = contentLabel,
        published = published
    )
}

@Entity(tableName = "study_notes")
data class StudyNoteEntity(
    @PrimaryKey val id: String,
    val boardId: String,
    val subjectId: String,
    val chapterId: String,
    val noteType: String,
    val titleEn: String,
    val titleOr: String,
    val contentEn: String,
    val contentOr: String,
    val pdfUri: String,
    val published: Boolean,
    val savedOffline: Boolean = false
) {
    fun toModel() = StudyNoteModel(
        id = id,
        boardId = boardId,
        subjectId = subjectId,
        chapterId = chapterId,
        noteType = noteType,
        titleEn = titleEn,
        titleOr = titleOr,
        contentEn = contentEn,
        contentOr = contentOr,
        pdfUri = pdfUri,
        published = published,
        savedOffline = savedOffline
    )
}

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val id: String,
    val itemId: String,
    val itemType: String,
    val boardId: String,
    val subjectId: String,
    val inRevisionList: Boolean,
    val updatedAtMillis: Long
) {
    fun toModel() = UserBookmarkModel(
        id = id,
        itemId = itemId,
        itemType = itemType,
        boardId = boardId,
        subjectId = subjectId,
        inRevisionList = inRevisionList
    )
}

@Entity(tableName = "chapter_progress")
data class ChapterProgressEntity(
    @PrimaryKey val chapterId: String,
    val boardId: String,
    val subjectId: String,
    val completed: Boolean,
    val updatedAtMillis: Long
)

@Entity(tableName = "quiz_attempts")
data class QuizAttemptEntity(
    @PrimaryKey val id: String,
    val boardId: String,
    val subjectId: String,
    val mode: String,
    val title: String,
    val totalQuestions: Long,
    val correctCount: Long,
    val wrongCount: Long,
    val scoreMarks: Long,
    val maxMarks: Long,
    val durationSeconds: Long,
    val status: String,
    val wrongMcqIds: List<String>,
    val createdAtMillis: Long
) {
    fun toModel() = QuizAttemptModel(
        id = id,
        boardId = boardId,
        subjectId = subjectId,
        mode = mode,
        title = title,
        totalQuestions = totalQuestions,
        correctCount = correctCount,
        wrongCount = wrongCount,
        scoreMarks = scoreMarks,
        maxMarks = maxMarks,
        durationSeconds = durationSeconds,
        status = status,
        wrongMcqIds = wrongMcqIds
    )
}

@Entity(tableName = "saved_quiz_sessions")
data class SavedQuizSessionEntity(
    @PrimaryKey val boardId: String,
    val title: String,
    val mode: String,
    val subjectId: String,
    val questionIds: List<String>,
    val selectedAnswers: List<String>,
    val markedForReview: List<String>,
    val currentIndex: Int,
    val remainingSeconds: Long,
    val updatedAtMillis: Long
)
