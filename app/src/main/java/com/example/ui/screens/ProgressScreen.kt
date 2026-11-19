package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.data.local.BookmarkEntity
import com.example.data.remote.onGoogleSignInClicked
import com.example.data.remote.signOutGoogle
import com.example.model.AppLanguage
import com.example.model.AppStrings
import com.example.model.AttemptMode
import com.example.model.BoardType
import com.example.model.BookmarkItemType
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.GoogleSignInActionButton
import com.example.ui.components.StatusBadge
import com.example.ui.theme.Crimson100
import com.example.ui.theme.Crimson600
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Navy100
import com.example.ui.theme.Navy50
import com.example.ui.theme.Navy800
import com.example.ui.theme.Saffron100
import com.example.ui.theme.Saffron500
import com.example.ui.theme.Saffron600
import com.example.viewmodel.BoardCatalogState
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

@Composable
fun ProgressScreen(
    state: BoardCatalogState,
    isSignedIn: Boolean,
    userEmail: String?,
    isAdmin: Boolean,
    isSyncing: Boolean,
    syncMessage: String?,
    onSwitchBoard: (BoardType) -> Unit,
    onSwitchLanguage: (AppLanguage) -> Unit,
    onToggleBookmark: (String, BookmarkItemType, String, String, BookmarkEntity?, Boolean) -> Unit,
    onSyncNow: () -> Unit,
    onSignedIn: () -> Unit,
    onSignedOut: () -> Unit,
    onOpenAdminPanel: () -> Unit,
    onOpenMcqRetry: () -> Unit
) {
    val lang = state.preferences.language
    val s = AppStrings.get(lang)
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(s.analyticsTab, s.bookmarksTab, s.settingsTab)

    Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize()
        ) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                tabs.forEachIndexed { index, label ->
                    SegmentedButton(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = tabs.size),
                        modifier = Modifier.testTag("progress_tab_$index")
                    ) {
                        Text(label, maxLines = 1)
                    }
                }
            }

            when (selectedTab) {
                0 -> AnalyticsTab(state = state)
                1 -> BookmarksTab(
                    state = state,
                    onToggleBookmark = onToggleBookmark,
                    onOpenMcqRetry = onOpenMcqRetry
                )
                2 -> ProfileAndSettingsTab(
                    state = state,
                    isSignedIn = isSignedIn,
                    userEmail = userEmail,
                    isAdmin = isAdmin,
                    isSyncing = isSyncing,
                    syncMessage = syncMessage,
                    onSwitchBoard = onSwitchBoard,
                    onSwitchLanguage = onSwitchLanguage,
                    onSyncNow = onSyncNow,
                    onSignedIn = onSignedIn,
                    onSignedOut = onSignedOut,
                    onOpenAdminPanel = onOpenAdminPanel
                )
            }
        }
    }
}

@Composable
private fun AnalyticsTab(state: BoardCatalogState) {
    val lang = state.preferences.language
    val s = AppStrings.get(lang)

    val totalAttempted = state.attempts.sumOf { it.totalQuestions }
    val totalCorrect = state.attempts.sumOf { it.correctCount }
    val mcqAccuracyPct = if (totalAttempted == 0L) 0
    else ((totalCorrect.toDouble() / totalAttempted.toDouble()) * 100.0).roundToInt()

    val mockAttempts = state.attempts.filter { it.mode == AttemptMode.MOCK_TEST.name }
    val avgMockPct = if (mockAttempts.isEmpty()) 0
    else mockAttempts.map {
        ((it.scoreMarks.toDouble() / it.maxMarks.coerceAtLeast(1L)) * 100.0)
    }.average().roundToInt()

    // Weekly activity (last 7 days bucketed by day)
    val weeklyCounts = remember(state.attempts) {
        val nowDays = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
        (6 downTo 0).map { offset ->
            val targetDay = nowDays - offset
            state.attempts.filter { TimeUnit.MILLISECONDS.toDays(it.createdAtMillis) == targetDay }
                .sumOf { it.totalQuestions }
                .toFloat()
        }
    }

    val completedChapterIds = remember(state.chapterProgress) {
        state.chapterProgress.filter { it.completed }.map { it.chapterId }.toSet()
    }

    val weakSubjects = remember(state.subjects, state.attempts, state.chapters, completedChapterIds) {
        state.subjects.filter { subj ->
            val subjAttempts = state.attempts.filter { it.subjectId == subj.id }
            val subjAcc = if (subjAttempts.isEmpty()) 100
            else {
                val tot = subjAttempts.sumOf { it.totalQuestions }.coerceAtLeast(1)
                ((subjAttempts.sumOf { it.correctCount }.toFloat() / tot) * 100).roundToInt()
            }
            val subjChapters = state.chapters.filter { it.subjectId == subj.id }
            val done = subjChapters.count { it.id in completedChapterIds }
            subjAcc < 70 || (subjChapters.isNotEmpty() && done == 0)
        }
    }

    val incompleteChapters = remember(state.chapters, completedChapterIds) {
        state.chapters.filter { it.id !in completedChapterIds }.take(5)
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MetricBox(s.totalAttempted, "$totalAttempted", Navy800, Modifier.weight(1f))
                MetricBox(s.mcqAccuracy, "$mcqAccuracyPct%", Emerald600, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MetricBox(s.mockScores, "$avgMockPct%", Saffron600, Modifier.weight(1f))
                MetricBox(s.streakLabel, "${state.preferences.streakDays} d", Navy800, Modifier.weight(1f))
            }
        }
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = s.weeklyActivity,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))
                    val barColor = Saffron500
                    val trackColor = Navy100
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        val maxVal = (weeklyCounts.maxOrNull() ?: 0f).coerceAtLeast(10f)
                        val barCount = weeklyCounts.size.coerceAtLeast(1)
                        val slotWidth = size.width / barCount
                        val barWidth = slotWidth * 0.48f
                        weeklyCounts.forEachIndexed { idx, value ->
                            val left = idx * slotWidth + (slotWidth - barWidth) / 2f
                            drawRoundRect(
                                color = trackColor,
                                topLeft = Offset(left, 0f),
                                size = Size(barWidth, size.height),
                                cornerRadius = CornerRadius(12f, 12f)
                            )
                            val barHeight = (value / maxVal) * size.height
                            if (barHeight > 0f) {
                                drawRoundRect(
                                    color = barColor,
                                    topLeft = Offset(left, size.height - barHeight),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(12f, 12f)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("D-6", "D-5", "D-4", "D-3", "D-2", "Yest", "Today").forEach { lbl ->
                            Text(
                                text = lbl,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        if (weakSubjects.isNotEmpty()) {
            item {
                Text(s.weakSubjects, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(weakSubjects, key = { it.id }) { subj ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Saffron100.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "• ${subj.toModel().displayName(lang)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }

        if (incompleteChapters.isNotEmpty()) {
            item {
                Text(s.weakChapters, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(incompleteChapters, key = { it.id }) { ch ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Navy50,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${ch.chapterNumber}. ${ch.toModel().displayTitle(lang)}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricBox(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = accent)
        }
    }
}

@Composable
private fun BookmarksTab(
    state: BoardCatalogState,
    onToggleBookmark: (String, BookmarkItemType, String, String, BookmarkEntity?, Boolean) -> Unit,
    onOpenMcqRetry: () -> Unit
) {
    val lang = state.preferences.language
    val s = AppStrings.get(lang)
    var filterMode by remember { mutableStateOf("ALL") }

    val impMap = remember(state.importantQuestions) { state.importantQuestions.associateBy { it.id } }
    val mcqMap = remember(state.mcqs) { state.mcqs.associateBy { it.id } }
    val noteMap = remember(state.notes) { state.notes.associateBy { it.id } }
    val wrongIdsCount = remember(state.attempts) { state.attempts.flatMap { it.wrongMcqIds }.distinct().size }

    val filteredBookmarks = remember(state.bookmarks, filterMode) {
        when (filterMode) {
            "REVISION" -> state.bookmarks.filter { it.inRevisionList }
            "IMP" -> state.bookmarks.filter { it.itemType == BookmarkItemType.IMPORTANT_Q.name }
            "MCQ" -> state.bookmarks.filter { it.itemType == BookmarkItemType.MCQ.name }
            "NOTE" -> state.bookmarks.filter { it.itemType == BookmarkItemType.NOTE.name }
            else -> state.bookmarks
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        if (wrongIdsCount > 0) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Crimson100.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "$wrongIdsCount Wrong MCQs recorded",
                                style = MaterialTheme.typography.titleSmall,
                                color = Crimson600,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Practice your missed MCQs again to improve accuracy.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = onOpenMcqRetry,
                            colors = ButtonDefaults.buttonColors(containerColor = Crimson600),
                            modifier = Modifier.testTag("retry_wrong_shortcut_button")
                        ) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(s.wrongRetryMode)
                        }
                    }
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(selected = filterMode == "ALL", onClick = { filterMode = "ALL" }, label = { Text(s.allLabel) })
                }
                item {
                    FilterChip(selected = filterMode == "REVISION", onClick = { filterMode = "REVISION" }, label = { Text(s.revisionListLabel) })
                }
                item {
                    FilterChip(selected = filterMode == "IMP", onClick = { filterMode = "IMP" }, label = { Text(s.importantQsLabel) })
                }
                item {
                    FilterChip(selected = filterMode == "MCQ", onClick = { filterMode = "MCQ" }, label = { Text(s.mcqsLabel) })
                }
                item {
                    FilterChip(selected = filterMode == "NOTE", onClick = { filterMode = "NOTE" }, label = { Text(s.notesLabel) })
                }
            }
        }

        if (filteredBookmarks.isEmpty()) {
            item { EmptyStateCard(Icons.Default.Bookmark, s.noItemsMessage) }
        } else {
            items(filteredBookmarks, key = { it.id }) { bm ->
                val title = when (BookmarkItemType.fromRaw(bm.itemType)) {
                    BookmarkItemType.IMPORTANT_Q -> impMap[bm.itemId]?.toModel()?.displayQuestion(lang) ?: bm.itemId
                    BookmarkItemType.MCQ -> mcqMap[bm.itemId]?.toModel()?.displayQuestion(lang) ?: bm.itemId
                    BookmarkItemType.NOTE -> noteMap[bm.itemId]?.toModel()?.displayTitle(lang) ?: bm.itemId
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusBadge(bm.itemType, Navy100, Navy800)
                            if (bm.inRevisionList) {
                                StatusBadge(s.revisionListLabel, Saffron100, Saffron600)
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            TextButton(
                                onClick = {
                                    onToggleBookmark(
                                        bm.itemId,
                                        BookmarkItemType.fromRaw(bm.itemType),
                                        bm.boardId,
                                        bm.subjectId,
                                        bm,
                                        true
                                    )
                                }
                            ) {
                                Icon(Icons.Default.PlaylistAddCheck, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(s.revisionListLabel)
                            }
                            IconButton(
                                onClick = {
                                    onToggleBookmark(
                                        bm.itemId,
                                        BookmarkItemType.fromRaw(bm.itemType),
                                        bm.boardId,
                                        bm.subjectId,
                                        bm,
                                        false
                                    )
                                }
                            ) {
                                Icon(Icons.Default.DeleteOutline, "Remove bookmark", tint = Crimson600)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileAndSettingsTab(
    state: BoardCatalogState,
    isSignedIn: Boolean,
    userEmail: String?,
    isAdmin: Boolean,
    isSyncing: Boolean,
    syncMessage: String?,
    onSwitchBoard: (BoardType) -> Unit,
    onSwitchLanguage: (AppLanguage) -> Unit,
    onSyncNow: () -> Unit,
    onSignedIn: () -> Unit,
    onSignedOut: () -> Unit,
    onOpenAdminPanel: () -> Unit
) {
    val prefs = state.preferences
    val lang = prefs.language
    val s = AppStrings.get(lang)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var authLoading by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(s.switchBoard, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        BoardType.entries.forEach { board ->
                            val selected = prefs.board == board
                            OutlinedButton(
                                onClick = { onSwitchBoard(board) },
                                border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) Saffron500 else MaterialTheme.colorScheme.outline),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("switch_board_${board.id}")
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(if (lang == AppLanguage.OR) board.titleOr else board.titleEn, fontWeight = FontWeight.Bold)
                                    Text(if (lang == AppLanguage.OR) board.subtitleOr else board.subtitleEn, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(s.switchLanguage, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        AppLanguage.entries.forEach { option ->
                            FilterChip(
                                selected = prefs.language == option,
                                onClick = { onSwitchLanguage(option) },
                                label = { Text("${option.nativeLabel} (${option.label})") },
                                modifier = Modifier.testTag("switch_lang_${option.code}")
                            )
                        }
                    }
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(s.cloudSyncTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = if (isSignedIn) s.cloudSyncDescSignedIn else s.cloudSyncDescGuest,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isSignedIn && !userEmail.isNullOrBlank()) {
                        Spacer(Modifier.height(6.dp))
                        StatusBadge(userEmail, Emerald100, Emerald600)
                    }
                    if (!syncMessage.isNullOrBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(syncMessage, style = MaterialTheme.typography.labelMedium, color = Saffron600)
                    }
                    if (!authError.isNullOrBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(authError ?: "", style = MaterialTheme.typography.labelMedium, color = Crimson600)
                    }
                    Spacer(Modifier.height(12.dp))
                    if (!isSignedIn) {
                        GoogleSignInActionButton(
                            text = s.signInGoogleSync,
                            isLoading = authLoading,
                            onClick = {
                                authLoading = true
                                authError = null
                                onGoogleSignInClicked(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onAuthSuccess = {
                                        authLoading = false
                                        onSignedIn()
                                    },
                                    onAuthError = { msg ->
                                        authLoading = false
                                        authError = msg
                                    },
                                    scope = scope,
                                    onAuthCancelled = { authLoading = false }
                                )
                            }
                        )
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = onSyncNow,
                                enabled = !isSyncing,
                                colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("sync_now_button")
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Sync, null, modifier = Modifier.size(18.dp))
                                }
                                Spacer(Modifier.width(6.dp))
                                Text(s.syncNowLabel)
                            }
                            OutlinedButton(
                                onClick = {
                                    signOutGoogle(credentialManager, scope) {
                                        onSignedOut()
                                    }
                                },
                                modifier = Modifier.testTag("sign_out_button")
                            ) {
                                Text(s.signOutLabel)
                            }
                        }
                    }
                }
            }
        }

        if (isAdmin) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Navy800,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AdminPanelSettings, null, tint = Saffron500)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = s.adminPanelTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Manage official syllabus, questions, mock patterns, PYQs, notes & student analytics.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = onOpenAdminPanel,
                            colors = ButtonDefaults.buttonColors(containerColor = Saffron500),
                            modifier = Modifier.testTag("open_admin_panel_button")
                        ) {
                            Text(s.openAdminPanel)
                        }
                    }
                }
            }
        }
    }
}
