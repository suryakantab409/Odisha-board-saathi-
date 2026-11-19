package com.example.ui.navigation

object NavRoutes {
    const val HOME = "home"
    const val PRACTICE = "practice"
    const val LIBRARY = "library"
    const val PROGRESS = "progress"

    const val IMPORTANT_QUESTIONS = "important_questions"
    const val MCQ_PRACTICE = "mcq_practice"
    const val QUIZ_SETUP = "quiz_setup"
    const val MOCK_TESTS = "mock_tests"
    const val ACTIVE_QUIZ = "active_quiz"
    const val ADMIN = "admin"

    val topLevelRoutes = setOf(HOME, PRACTICE, LIBRARY, PROGRESS)
}
