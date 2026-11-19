package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.remote.attemptAutoSignIn
import com.example.data.remote.authStateFlow
import com.example.model.AppLanguage
import com.example.model.AppStrings
import com.example.ui.components.BoardSaathiTopBar
import com.example.ui.navigation.NavRoutes
import com.example.ui.screens.ActiveQuizRunnerScreen
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ImportantQuestionsScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.McqPracticeScreen
import com.example.ui.screens.MockTestListScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PracticeHubScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.QuizSetupScreen
import com.example.ui.theme.Navy800
import com.example.ui.theme.Saffron100
import com.example.ui.theme.Saffron600
import com.example.viewmodel.AdminViewModel
import com.example.viewmodel.MainViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

private data class TopLevelNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val tag: String
)

@Composable
fun BoardSaathiApp(
    mainViewModel: MainViewModel,
    adminViewModel: AdminViewModel
) {
    val state by mainViewModel.catalogState.collectAsStateWithLifecycle()
    val quizState by mainViewModel.quizState.collectAsStateWithLifecycle()
    val isAdmin by mainViewModel.isAdmin.collectAsStateWithLifecycle()
    val isSyncing by mainViewModel.isSyncing.collectAsStateWithLifecycle()
    val syncMessage by mainViewModel.syncMessage.collectAsStateWithLifecycle()

    val currentUser by remember { Firebase.auth.authStateFlow() }
        .collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = { mainViewModel.refreshAdminAndSync() },
            onUnauthenticated = {},
            scope = scope
        )
    }

    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            mainViewModel.refreshAdminAndSync()
        } else {
            mainViewModel.onSignedOut()
        }
    }

    val prefs = state.preferences
    val lang = prefs.language
    val s = AppStrings.get(lang)
    val isSignedIn = currentUser != null

    if (!prefs.onboarded) {
        OnboardingScreen(
            initialBoard = prefs.board,
            initialLanguage = prefs.language,
            isSignedIn = isSignedIn,
            onSignedIn = { mainViewModel.refreshAdminAndSync() },
            onComplete = { board, chosenLang, name, days, target ->
                mainViewModel.completeOnboarding(board, chosenLang, name, days, target)
            }
        )
        return
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: NavRoutes.HOME
    var initialLibraryTab by remember { mutableIntStateOf(0) }

    val topItems = listOf(
        TopLevelNavItem(NavRoutes.HOME, s.navHome, Icons.Default.Home, "nav_home"),
        TopLevelNavItem(NavRoutes.PRACTICE, s.navPractice, Icons.Default.Quiz, "nav_practice"),
        TopLevelNavItem(NavRoutes.LIBRARY, s.navLibrary, Icons.Default.MenuBook, "nav_library"),
        TopLevelNavItem(NavRoutes.PROGRESS, s.navProgress, Icons.Default.Analytics, "nav_progress")
    )

    val isTopLevel = currentRoute in NavRoutes.topLevelRoutes
    val topBarTitle = when (currentRoute) {
        NavRoutes.HOME -> s.appTitle
        NavRoutes.PRACTICE -> s.navPractice
        NavRoutes.LIBRARY -> s.navLibrary
        NavRoutes.PROGRESS -> s.navProgress
        NavRoutes.IMPORTANT_QUESTIONS -> s.importantBankTitle
        NavRoutes.MCQ_PRACTICE -> s.mcqPracticeTitle
        NavRoutes.QUIZ_SETUP -> s.quizEngineTitle
        NavRoutes.MOCK_TESTS -> s.mockExamTitle
        NavRoutes.ACTIVE_QUIZ -> quizState.title.ifBlank { s.quizEngineTitle }
        NavRoutes.ADMIN -> s.adminPanelTitle
        else -> s.appTitle
    }

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val toggleLang: () -> Unit = {
        val next = if (lang == AppLanguage.EN) AppLanguage.OR else AppLanguage.EN
        mainViewModel.switchLanguage(next)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                BoardSaathiTopBar(
                    title = topBarTitle,
                    language = lang,
                    onToggleLanguage = toggleLang,
                    isSignedIn = isSignedIn,
                    onBack = if (!isTopLevel) {
                        {
                            if (currentRoute == NavRoutes.ACTIVE_QUIZ) {
                                if (quizState.submitted) {
                                    mainViewModel.exitQuizSession()
                                } else {
                                    mainViewModel.pauseAndSaveQuiz(prefs.board.id)
                                }
                            }
                            navController.popBackStack()
                        }
                    } else null
                )
            },
            bottomBar = {
                if (isTopLevel && !useRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        topItems.forEach { item ->
                            val selected = currentRoute == item.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (item.route == NavRoutes.LIBRARY) initialLibraryTab = 0
                                    navigateTopLevel(item.route)
                                },
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label, maxLines = 1) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Saffron600,
                                    selectedTextColor = Navy800,
                                    indicatorColor = Saffron100
                                ),
                                modifier = Modifier.testTag(item.tag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isTopLevel && useRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        topItems.forEach { item ->
                            val selected = currentRoute == item.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = {
                                    if (item.route == NavRoutes.LIBRARY) initialLibraryTab = 0
                                    navigateTopLevel(item.route)
                                },
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label, maxLines = 1) },
                                modifier = Modifier.testTag(item.tag)
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = NavRoutes.HOME
                    ) {
                        composable(NavRoutes.HOME) {
                            DashboardScreen(
                                state = state,
                                onOpenMcqPractice = { navController.navigate(NavRoutes.MCQ_PRACTICE) },
                                onOpenImportantQuestions = { navController.navigate(NavRoutes.IMPORTANT_QUESTIONS) },
                                onOpenQuizSetup = { navController.navigate(NavRoutes.QUIZ_SETUP) },
                                onOpenMockTests = { navController.navigate(NavRoutes.MOCK_TESTS) },
                                onOpenPyqs = {
                                    initialLibraryTab = 1
                                    navigateTopLevel(NavRoutes.LIBRARY)
                                },
                                onOpenNotes = {
                                    initialLibraryTab = 2
                                    navigateTopLevel(NavRoutes.LIBRARY)
                                },
                                onResumeSavedQuiz = {
                                    state.savedQuizSession?.let { saved ->
                                        mainViewModel.resumeSavedQuiz(saved, state.mcqs)
                                        navController.navigate(NavRoutes.ACTIVE_QUIZ)
                                    }
                                },
                                onUpdateExamCountdown = { days -> mainViewModel.updateExamCountdown(days) },
                                onUpdateDailyTarget = { target -> mainViewModel.updateDailyTarget(target) }
                            )
                        }

                        composable(NavRoutes.PRACTICE) {
                            PracticeHubScreen(
                                state = state,
                                onOpenImportantQuestions = { navController.navigate(NavRoutes.IMPORTANT_QUESTIONS) },
                                onOpenMcqPractice = { navController.navigate(NavRoutes.MCQ_PRACTICE) },
                                onOpenQuizSetup = { navController.navigate(NavRoutes.QUIZ_SETUP) },
                                onOpenMockTests = { navController.navigate(NavRoutes.MOCK_TESTS) }
                            )
                        }

                        composable(NavRoutes.LIBRARY) {
                            LibraryScreen(
                                state = state,
                                initialTab = initialLibraryTab,
                                onToggleChapterCompleted = { chapter, currentlyDone ->
                                    mainViewModel.toggleChapterCompleted(chapter, currentlyDone)
                                },
                                onToggleNoteOffline = { noteId, saved ->
                                    mainViewModel.toggleNoteOffline(noteId, saved)
                                },
                                onToggleNoteBookmark = { itemId, itemType, boardId, subjectId, existing, revOnly ->
                                    mainViewModel.toggleBookmark(itemId, itemType, boardId, subjectId, existing, revOnly)
                                }
                            )
                        }

                        composable(NavRoutes.PROGRESS) {
                            ProgressScreen(
                                state = state,
                                isSignedIn = isSignedIn,
                                userEmail = currentUser?.email,
                                isAdmin = isAdmin,
                                isSyncing = isSyncing,
                                syncMessage = syncMessage,
                                onSwitchBoard = { mainViewModel.switchBoard(it) },
                                onSwitchLanguage = { mainViewModel.switchLanguage(it) },
                                onToggleBookmark = { itemId, itemType, boardId, subjectId, existing, revOnly ->
                                    mainViewModel.toggleBookmark(itemId, itemType, boardId, subjectId, existing, revOnly)
                                },
                                onSyncNow = { mainViewModel.syncNow() },
                                onSignedIn = { mainViewModel.refreshAdminAndSync() },
                                onSignedOut = { mainViewModel.onSignedOut() },
                                onOpenAdminPanel = { navController.navigate(NavRoutes.ADMIN) },
                                onOpenMcqRetry = { navController.navigate(NavRoutes.MCQ_PRACTICE) }
                            )
                        }

                        composable(NavRoutes.IMPORTANT_QUESTIONS) {
                            ImportantQuestionsScreen(
                                state = state,
                                onToggleBookmark = { itemId, itemType, boardId, subjectId, existing, revOnly ->
                                    mainViewModel.toggleBookmark(itemId, itemType, boardId, subjectId, existing, revOnly)
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(NavRoutes.MCQ_PRACTICE) {
                            McqPracticeScreen(
                                state = state,
                                onToggleBookmark = { itemId, itemType, boardId, subjectId, existing, revOnly ->
                                    mainViewModel.toggleBookmark(itemId, itemType, boardId, subjectId, existing, revOnly)
                                },
                                onRecordPracticeScore = { boardId, subjectId, title, total, correct, wrongIds ->
                                    mainViewModel.recordMcqPracticeAttempt(
                                        boardId = boardId,
                                        subjectId = subjectId,
                                        title = title,
                                        totalQuestions = total,
                                        correctCount = correct,
                                        wrongMcqIds = wrongIds
                                    )
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(NavRoutes.QUIZ_SETUP) {
                            QuizSetupScreen(
                                state = state,
                                onStartQuiz = { title, mode, subjectId, questions, durationMin ->
                                    mainViewModel.startQuizSession(title, mode, subjectId, questions, durationMin)
                                    navController.navigate(NavRoutes.ACTIVE_QUIZ)
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(NavRoutes.MOCK_TESTS) {
                            MockTestListScreen(
                                state = state,
                                onStartMockExam = { title, mode, subjectId, questions, durationMin ->
                                    mainViewModel.startQuizSession(title, mode, subjectId, questions, durationMin)
                                    navController.navigate(NavRoutes.ACTIVE_QUIZ)
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(NavRoutes.ACTIVE_QUIZ) {
                            ActiveQuizRunnerScreen(
                                quizState = quizState,
                                lang = lang,
                                onSelectAnswer = { qIdx, optIdx -> mainViewModel.selectQuizAnswer(qIdx, optIdx) },
                                onToggleReview = { qIdx -> mainViewModel.toggleMarkForReview(qIdx) },
                                onJumpQuestion = { qIdx -> mainViewModel.jumpToQuizQuestion(qIdx) },
                                onSaveAndPause = {
                                    mainViewModel.pauseAndSaveQuiz(prefs.board.id)
                                    navController.popBackStack()
                                },
                                onSubmit = { mainViewModel.submitQuiz() },
                                onExit = {
                                    mainViewModel.exitQuizSession()
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(NavRoutes.ADMIN) {
                            AdminScreen(
                                isAdmin = isAdmin,
                                adminViewModel = adminViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
