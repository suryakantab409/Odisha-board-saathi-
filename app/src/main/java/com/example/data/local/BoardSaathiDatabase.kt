package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        SubjectEntity::class,
        ChapterEntity::class,
        ImportantQuestionEntity::class,
        McqEntity::class,
        MockExamEntity::class,
        PyqPaperEntity::class,
        StudyNoteEntity::class,
        BookmarkEntity::class,
        ChapterProgressEntity::class,
        QuizAttemptEntity::class,
        SavedQuizSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class BoardSaathiDatabase : RoomDatabase() {
    abstract fun dao(): BoardSaathiDao

    companion object {
        @Volatile
        private var INSTANCE: BoardSaathiDatabase? = null

        fun getInstance(context: Context): BoardSaathiDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BoardSaathiDatabase::class.java,
                    "boardsaathi_odisha.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
