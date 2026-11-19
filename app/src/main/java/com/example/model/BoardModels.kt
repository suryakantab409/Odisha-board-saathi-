package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

enum class BoardType(
    val id: String,
    val titleEn: String,
    val titleOr: String,
    val subtitleEn: String,
    val subtitleOr: String,
    val classLabelEn: String,
    val classLabelOr: String
) {
    BSE_10(
        id = "BSE_10",
        titleEn = "BSE Odisha",
        titleOr = "ବିଏସ୍ଇ ଓଡ଼ିଶା",
        subtitleEn = "Class 10 (Matric)",
        subtitleOr = "ଦଶମ ଶ୍ରେଣୀ (ମାଟ୍ରିକ୍)",
        classLabelEn = "Class 10 · Matric",
        classLabelOr = "ଦଶମ ଶ୍ରେଣୀ · ମାଟ୍ରିକ୍"
    ),
    CHSE_12_ARTS(
        id = "CHSE_12_ARTS",
        titleEn = "CHSE Odisha",
        titleOr = "ସିଏଚ୍ଏସ୍ଇ ଓଡ଼ିଶା",
        subtitleEn = "+2 Arts Stream",
        subtitleOr = "+୨ କଳା ବିଭାଗ",
        classLabelEn = "+2 Arts · Class 12",
        classLabelOr = "+୨ କଳା · ଦ୍ୱାଦଶ ଶ୍ରେଣୀ"
    );

    companion object {
        fun fromId(raw: String?): BoardType =
            entries.firstOrNull { it.id == raw || it.name == raw } ?: BSE_10
    }
}

enum class AppLanguage(val code: String, val label: String, val nativeLabel: String) {
    EN("EN", "English", "English"),
    OR("OR", "Odia", "ଓଡ଼ିଆ");

    companion object {
        fun fromCode(raw: String?): AppLanguage =
            entries.firstOrNull { it.code == raw || it.name == raw } ?: EN
    }
}

enum class Difficulty(val labelEn: String, val labelOr: String) {
    EASY("Easy", "ସହଜ"),
    MEDIUM("Medium", "ମଧ୍ୟମ"),
    HARD("Hard", "କଠିନ");

    companion object {
        fun fromRaw(raw: String?): Difficulty =
            entries.firstOrNull { it.name == raw } ?: MEDIUM
    }
}

enum class ContentLabel(val labelEn: String, val labelOr: String) {
    VERIFIED_PYQ("Verified PYQ", "ଯାଞ୍ଚ ହୋଇଥିବା PYQ"),
    ADMIN_APPROVED("Admin-approved Important Q", "ଆଡମିନ୍ ଅନୁମୋଦିତ ଗୁରୁତ୍ୱପୂର୍ଣ୍ଣ"),
    PRACTICE_QUESTION("Practice Question", "ଅଭ୍ୟାସ ପ୍ରଶ୍ନ"),
    AI_PRACTICE("AI-generated Practice Q", "AI ଅଭ୍ୟାସ ପ୍ରଶ୍ନ");

    companion object {
        fun fromRaw(raw: String?): ContentLabel =
            entries.firstOrNull { it.name == raw } ?: PRACTICE_QUESTION
    }
}

enum class QuestionCategory(val labelEn: String, val labelOr: String) {
    ONE_MARK("1 Mark", "୧ ମାର୍କ"),
    TWO_MARKS("2 Marks", "୨ ମାର୍କ"),
    THREE_MARKS("3 Marks", "୩ ମାର୍କ"),
    FIVE_MARKS("5 Marks", "୫ ମାର୍କ"),
    LONG_ANSWER("Long Answer", "ଦୀର୍ଘ ଉତ୍ତର"),
    VERY_IMPORTANT("Very Important", "ଅତି ଗୁରୁତ୍ୱପୂର୍ଣ୍ଣ"),
    FAQ("Frequently Asked", "ବାରମ୍ବାର ପଚରାଯାଉଥିବା"),
    BOARD_PRACTICE("Board Practice", "ବୋର୍ଡ ଅଭ୍ୟାସ");

    companion object {
        fun fromRaw(raw: String?): QuestionCategory =
            entries.firstOrNull { it.name == raw } ?: BOARD_PRACTICE
    }
}

enum class NoteType(val labelEn: String, val labelOr: String) {
    CHAPTER_NOTES("Chapter Notes", "ଅଧ୍ୟାୟ ନୋଟ୍ସ"),
    DEFINITIONS("Important Definitions", "ଗୁରୁତ୍ୱପୂର୍ଣ୍ଣ ସଂଜ୍ଞା"),
    SHORT_ANSWERS("Short Answers", "ସଂକ୍ଷିପ୍ତ ଉତ୍ତର"),
    LONG_ANSWERS("Long Answers", "ଦୀର୍ଘ ଉତ୍ତର"),
    REVISION("Revision Notes", "ପୁନରାବୃତ୍ତି ନୋଟ୍ସ");

    companion object {
        fun fromRaw(raw: String?): NoteType =
            entries.firstOrNull { it.name == raw } ?: CHAPTER_NOTES
    }
}

enum class AttemptMode {
    QUIZ,
    MOCK_TEST,
    MCQ_PRACTICE;

    companion object {
        fun fromRaw(raw: String?): AttemptMode =
            entries.firstOrNull { it.name == raw } ?: QUIZ
    }
}

enum class AttemptStatus {
    IN_PROGRESS,
    COMPLETED;

    companion object {
        fun fromRaw(raw: String?): AttemptStatus =
            entries.firstOrNull { it.name == raw } ?: COMPLETED
    }
}

enum class BookmarkItemType {
    IMPORTANT_Q,
    MCQ,
    NOTE;

    companion object {
        fun fromRaw(raw: String?): BookmarkItemType =
            entries.firstOrNull { it.name == raw } ?: IMPORTANT_Q
    }
}

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

// --- Firestore & Domain Models (No `is`-prefixed Boolean properties; all defaults provided) ---

@Keep
@IgnoreExtraProperties
data class SubjectModel(
    @DocumentId val id: String = "",
    val boardId: String = BoardType.BSE_10.id,
    val nameEn: String = "",
    val nameOr: String = "",
    val sortOrder: Long = 0L,
    val published: Boolean = true,
    val syllabusNote: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun displayName(lang: AppLanguage): String =
        if (lang == AppLanguage.OR && nameOr.isNotBlank()) nameOr else nameEn
}

@Keep
@IgnoreExtraProperties
data class ChapterModel(
    @DocumentId val id: String = "",
    val boardId: String = BoardType.BSE_10.id,
    val subjectId: String = "",
    val titleEn: String = "",
    val titleOr: String = "",
    val chapterNumber: Long = 1L,
    val published: Boolean = true,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun displayTitle(lang: AppLanguage): String =
        if (lang == AppLanguage.OR && titleOr.isNotBlank()) titleOr else titleEn
}

@Keep
@IgnoreExtraProperties
data class ImportantQuestionModel(
    @DocumentId val id: String = "",
    val boardId: String = BoardType.BSE_10.id,
    val subjectId: String = "",
    val chapterId: String = "",
    val marks: Long = 2L,
    val category: String = QuestionCategory.BOARD_PRACTICE.name,
    val difficulty: String = Difficulty.MEDIUM.name,
    val contentLabel: String = ContentLabel.PRACTICE_QUESTION.name,
    val sourceYear: String = "",
    val questionEn: String = "",
    val questionOr: String = "",
    val answerEn: String = "",
    val answerOr: String = "",
    val published: Boolean = true,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun displayQuestion(lang: AppLanguage): String =
        if (lang == AppLanguage.OR && questionOr.isNotBlank()) questionOr else questionEn

    fun displayAnswer(lang: AppLanguage): String =
        if (lang == AppLanguage.OR && answerOr.isNotBlank()) answerOr else answerEn
}

@Keep
@IgnoreExtraProperties
data class McqModel(
    @DocumentId val id: String = "",
    val boardId: String = BoardType.BSE_10.id,
    val subjectId: String = "",
    val chapterId: String = "",
    val questionEn: String = "",
    val questionOr: String = "",
    val optionsEn: List<String> = emptyList(),
    val optionsOr: List<String> = emptyList(),
    val correctIndex: Long = 0L,
    val explanationEn: String = "",
    val explanationOr: String = "",
    val difficulty: String = Difficulty.MEDIUM.name,
    val contentLabel: String = ContentLabel.PRACTICE_QUESTION.name,
    val sourceYear: String = "",
    val published: Boolean = true,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun displayQuestion(lang: AppLanguage): String =
        if (lang == AppLanguage.OR && questionOr.isNotBlank()) questionOr else questionEn

    fun displayOptions(lang: AppLanguage): List<String> =
        if (lang == AppLanguage.OR && optionsOr.size == 4 && optionsOr.all { it.isNotBlank() }) optionsOr else optionsEn

    fun displayExplanation(lang: AppLanguage): String =
        if (lang == AppLanguage.OR && explanationOr.isNotBlank()) explanationOr else explanationEn
}

@Keep
@IgnoreExtraProperties
data class MockExamModel(
    @DocumentId val id: String = "",
    val boardId: String = BoardType.BSE_10.id,
    val subjectId: String = "",
    val titleEn: String = "",
    val titleOr: String = "",
    val durationMinutes: Long = 30L,
    val totalMarks: Long = 20L,
    val questionIds: List<String> = emptyList(),
    val patternNote: String = "",
    val published: Boolean = true,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun displayTitle(lang: AppLanguage): String =
        if (lang == AppLanguage.OR && titleOr.isNotBlank()) titleOr else titleEn
}

@Keep
@IgnoreExtraProperties
data class PyqPaperModel(
    @DocumentId val id: String = "",
    val boardId: String = BoardType.BSE_10.id,
    val subjectId: String = "",
    val examYear: String = "",
    val questionType: String = "",
    val titleEn: String = "",
    val titleOr: String = "",
    val searchableText: String = "",
    val pdfUri: String = "",
    val contentLabel: String = ContentLabel.PRACTICE_QUESTION.name,
    val published: Boolean = true,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun displayTitle(lang: AppLanguage): String =
        if (lang == AppLanguage.OR && titleOr.isNotBlank()) titleOr else titleEn
}

@Keep
@IgnoreExtraProperties
data class StudyNoteModel(
    @DocumentId val id: String = "",
    val boardId: String = BoardType.BSE_10.id,
    val subjectId: String = "",
    val chapterId: String = "",
    val noteType: String = NoteType.CHAPTER_NOTES.name,
    val titleEn: String = "",
    val titleOr: String = "",
    val contentEn: String = "",
    val contentOr: String = "",
    val pdfUri: String = "",
    val published: Boolean = true,
    val savedOffline: Boolean = false,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun displayTitle(lang: AppLanguage): String =
        if (lang == AppLanguage.OR && titleOr.isNotBlank()) titleOr else titleEn

    fun displayContent(lang: AppLanguage): String =
        if (lang == AppLanguage.OR && contentOr.isNotBlank()) contentOr else contentEn
}

@Keep
@IgnoreExtraProperties
data class UserProfileModel(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val boardId: String = BoardType.BSE_10.id,
    val language: String = AppLanguage.EN.code,
    val examDateEpochDays: Long = 20900L,
    val dailyTargetQuestions: Long = 20L,
    val streakDays: Long = 1L,
    val lastStudyEpochDay: Long = 0L,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class UserBookmarkModel(
    @DocumentId val id: String = "",
    val itemId: String = "",
    val itemType: String = BookmarkItemType.IMPORTANT_Q.name,
    val boardId: String = BoardType.BSE_10.id,
    val subjectId: String = "",
    val inRevisionList: Boolean = false,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ChapterProgressModel(
    @DocumentId val id: String = "",
    val chapterId: String = "",
    val boardId: String = BoardType.BSE_10.id,
    val subjectId: String = "",
    val completed: Boolean = false,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class QuizAttemptModel(
    @DocumentId val id: String = "",
    val boardId: String = BoardType.BSE_10.id,
    val subjectId: String = "",
    val mode: String = AttemptMode.QUIZ.name,
    val title: String = "",
    val totalQuestions: Long = 0L,
    val correctCount: Long = 0L,
    val wrongCount: Long = 0L,
    val scoreMarks: Long = 0L,
    val maxMarks: Long = 1L,
    val durationSeconds: Long = 0L,
    val status: String = AttemptStatus.COMPLETED.name,
    val wrongMcqIds: List<String> = emptyList(),
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
