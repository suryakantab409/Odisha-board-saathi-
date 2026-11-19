package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AppLanguage
import com.example.model.BoardType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

private val Context.dataStore by preferencesDataStore(name = "boardsaathi_prefs")

data class StudentPreferences(
    val onboarded: Boolean = false,
    val board: BoardType = BoardType.BSE_10,
    val language: AppLanguage = AppLanguage.EN,
    val displayName: String = "",
    val examDateEpochDays: Long = currentEpochDay() + 90L,
    val dailyTargetQuestions: Int = 20,
    val streakDays: Int = 1,
    val lastStudyEpochDay: Long = currentEpochDay(),
    val todaySolvedCount: Int = 0,
    val todaySolvedEpochDay: Long = currentEpochDay(),
    val weeklyCounts: List<Int> = listOf(0, 0, 0, 0, 0, 0, 0)
) {
    val daysRemaining: Long
        get() = (examDateEpochDays - currentEpochDay()).coerceAtLeast(0L)

    companion object {
        fun currentEpochDay(): Long = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
    }
}

class UserPreferencesDataStore(private val context: Context) {

    private object Keys {
        val ONBOARDED = booleanPreferencesKey("onboarded")
        val BOARD_ID = stringPreferencesKey("board_id")
        val LANGUAGE = stringPreferencesKey("language")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val EXAM_DATE_EPOCH_DAYS = longPreferencesKey("exam_date_epoch_days")
        val DAILY_TARGET = intPreferencesKey("daily_target")
        val STREAK_DAYS = intPreferencesKey("streak_days")
        val LAST_STUDY_EPOCH_DAY = longPreferencesKey("last_study_epoch_day")
        val TODAY_SOLVED_COUNT = intPreferencesKey("today_solved_count")
        val TODAY_SOLVED_EPOCH_DAY = longPreferencesKey("today_solved_epoch_day")
        val WEEKLY_COUNTS = stringPreferencesKey("weekly_counts")
    }

    val preferencesFlow: Flow<StudentPreferences> = context.dataStore.data.map { prefs ->
        val today = StudentPreferences.currentEpochDay()
        val storedDay = prefs[Keys.TODAY_SOLVED_EPOCH_DAY] ?: today
        val todayCount = if (storedDay == today) (prefs[Keys.TODAY_SOLVED_COUNT] ?: 0) else 0
        val weeklyRaw = prefs[Keys.WEEKLY_COUNTS] ?: "0,0,0,0,0,0,0"
        val weeklyParsed = weeklyRaw.split(",").mapNotNull { it.toIntOrNull() }
            .let { if (it.size == 7) it else listOf(0, 0, 0, 0, 0, 0, 0) }

        StudentPreferences(
            onboarded = prefs[Keys.ONBOARDED] ?: false,
            board = BoardType.fromId(prefs[Keys.BOARD_ID]),
            language = AppLanguage.fromCode(prefs[Keys.LANGUAGE]),
            displayName = prefs[Keys.DISPLAY_NAME] ?: "",
            examDateEpochDays = prefs[Keys.EXAM_DATE_EPOCH_DAYS] ?: (today + 90L),
            dailyTargetQuestions = prefs[Keys.DAILY_TARGET] ?: 20,
            streakDays = prefs[Keys.STREAK_DAYS] ?: 1,
            lastStudyEpochDay = prefs[Keys.LAST_STUDY_EPOCH_DAY] ?: today,
            todaySolvedCount = todayCount,
            todaySolvedEpochDay = today,
            weeklyCounts = weeklyParsed
        )
    }

    suspend fun completeOnboarding(
        board: BoardType,
        language: AppLanguage,
        displayName: String,
        daysUntilExam: Long,
        dailyTarget: Int
    ) {
        val today = StudentPreferences.currentEpochDay()
        context.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDED] = true
            prefs[Keys.BOARD_ID] = board.id
            prefs[Keys.LANGUAGE] = language.code
            prefs[Keys.DISPLAY_NAME] = displayName.trim().take(80)
            prefs[Keys.EXAM_DATE_EPOCH_DAYS] = today + daysUntilExam.coerceIn(1L, 730L)
            prefs[Keys.DAILY_TARGET] = dailyTarget.coerceIn(5, 200)
            prefs[Keys.STREAK_DAYS] = prefs[Keys.STREAK_DAYS] ?: 1
            prefs[Keys.LAST_STUDY_EPOCH_DAY] = today
        }
    }

    suspend fun setBoard(board: BoardType) {
        context.dataStore.edit { it[Keys.BOARD_ID] = board.id }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language.code }
    }

    suspend fun setExamDaysFromNow(days: Long) {
        val today = StudentPreferences.currentEpochDay()
        context.dataStore.edit { it[Keys.EXAM_DATE_EPOCH_DAYS] = today + days.coerceIn(1L, 730L) }
    }

    suspend fun setDailyTarget(target: Int) {
        context.dataStore.edit { it[Keys.DAILY_TARGET] = target.coerceIn(5, 200) }
    }

    suspend fun setDisplayName(name: String) {
        context.dataStore.edit { it[Keys.DISPLAY_NAME] = name.trim().take(80) }
    }

    suspend fun recordQuestionsSolved(count: Int) {
        if (count <= 0) return
        val today = StudentPreferences.currentEpochDay()
        context.dataStore.edit { prefs ->
            val prevDay = prefs[Keys.TODAY_SOLVED_EPOCH_DAY] ?: today
            val prevCount = if (prevDay == today) (prefs[Keys.TODAY_SOLVED_COUNT] ?: 0) else 0
            prefs[Keys.TODAY_SOLVED_EPOCH_DAY] = today
            prefs[Keys.TODAY_SOLVED_COUNT] = prevCount + count

            val lastStudy = prefs[Keys.LAST_STUDY_EPOCH_DAY] ?: today
            val currentStreak = prefs[Keys.STREAK_DAYS] ?: 1
            val newStreak = when {
                today == lastStudy -> currentStreak
                today == lastStudy + 1L -> currentStreak + 1
                else -> 1
            }
            prefs[Keys.STREAK_DAYS] = newStreak
            prefs[Keys.LAST_STUDY_EPOCH_DAY] = today

            val weeklyRaw = prefs[Keys.WEEKLY_COUNTS] ?: "0,0,0,0,0,0,0"
            val list = weeklyRaw.split(",").mapNotNull { it.toIntOrNull() }
                .let { if (it.size == 7) it.toMutableList() else mutableListOf(0, 0, 0, 0, 0, 0, 0) }
            val dayShift = (today - prevDay).toInt().coerceIn(0, 7)
            if (dayShift > 0) {
                repeat(dayShift) {
                    list.removeAt(0)
                    list.add(0)
                }
            }
            list[6] = list[6] + count
            prefs[Keys.WEEKLY_COUNTS] = list.joinToString(",")
        }
    }

    suspend fun applyRemoteProfile(
        displayName: String,
        board: BoardType,
        language: AppLanguage,
        examDateEpochDays: Long,
        dailyTarget: Int,
        streakDays: Int,
        lastStudyEpochDay: Long
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDED] = true
            if (displayName.isNotBlank()) prefs[Keys.DISPLAY_NAME] = displayName
            prefs[Keys.BOARD_ID] = board.id
            prefs[Keys.LANGUAGE] = language.code
            prefs[Keys.EXAM_DATE_EPOCH_DAYS] = examDateEpochDays
            prefs[Keys.DAILY_TARGET] = dailyTarget
            prefs[Keys.STREAK_DAYS] = streakDays
            prefs[Keys.LAST_STUDY_EPOCH_DAY] = lastStudyEpochDay
        }
    }
}
